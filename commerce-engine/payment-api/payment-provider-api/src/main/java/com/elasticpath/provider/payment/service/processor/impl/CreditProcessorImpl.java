/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.service.processor.impl;

import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.APPROVED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.FAILED;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.CHARGE;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.CREDIT;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.REVERSE_CHARGE;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.CREDIT_CAPABILITY_REQUEST;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.REVERSE_CHARGE_CAPABILITY_REQUEST;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequestFailedException;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityResponse;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ReverseChargeCapability;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ReverseChargeCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ReverseChargeCapabilityRequestBuilder;
import com.elasticpath.plugin.payment.provider.capabilities.credit.CreditCapability;
import com.elasticpath.plugin.payment.provider.capabilities.credit.CreditCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.credit.CreditCapabilityRequestBuilder;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.provider.payment.domain.PaymentProvider;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIRequest;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIResponse;
import com.elasticpath.provider.payment.domain.transaction.credit.CreditRequest;
import com.elasticpath.provider.payment.domain.transaction.credit.ManualCreditRequest;
import com.elasticpath.provider.payment.domain.transaction.credit.ReverseChargeRequest;
import com.elasticpath.provider.payment.service.PaymentsException;
import com.elasticpath.provider.payment.service.PaymentsExceptionMessageId;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigurationService;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.event.PaymentEventBuilder;
import com.elasticpath.provider.payment.service.history.PaymentHistory;
import com.elasticpath.provider.payment.service.history.util.MoneyDtoCalculator;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.provider.payment.service.processor.AbstractProcessor;
import com.elasticpath.provider.payment.service.processor.CreditProcessor;
import com.elasticpath.provider.payment.service.provider.PaymentProviderService;
import com.elasticpath.provider.payment.workflow.PaymentAPIWorkflow;

/**
 * Default implementation of {@link CreditProcessor}.
 */
@SuppressWarnings({"PMD.GodClass"})
public class CreditProcessorImpl extends AbstractProcessor implements CreditProcessor {

	private static final String PAYMENTS_EXCEPTION_DATA_KEY = "reason";

	/**
	 * Constructor.
	 *
	 * @param paymentProviderConfigurationService payment provider configuration service
	 * @param paymentProviderService              payment provider service
	 * @param paymentHistory                      payment history helper service
	 * @param moneyDtoCalculator                  calculator of mathematical operation with MoneyDto
	 * @param paymentAPIWorkflow                  payment API workflow facade
	 * @param beanFactory                         EP bean factory
	 */
	public CreditProcessorImpl(final PaymentProviderConfigurationService paymentProviderConfigurationService,
							   final PaymentProviderService paymentProviderService,
							   final PaymentHistory paymentHistory,
							   final MoneyDtoCalculator moneyDtoCalculator,
							   final PaymentAPIWorkflow paymentAPIWorkflow,
							   final BeanFactory beanFactory) {
		super(paymentProviderConfigurationService, paymentProviderService, paymentHistory, moneyDtoCalculator, paymentAPIWorkflow, beanFactory);
	}

	@Override
	public PaymentAPIResponse credit(final CreditRequest creditRequest) {
		final List<PaymentEvent> ledger = creditRequest.getLedger();
		final MoneyDTO chargedAmount = getPaymentHistory().getChargedAmount(ledger);
		final MoneyDTO refundedAmount = getPaymentHistory().getRefundedAmount(ledger);
		final MoneyDTO refundableAmount = getMoneyDtoCalculator().minus(chargedAmount, refundedAmount);
		if (!hasEnoughCharged(refundableAmount, creditRequest.getAmount())) {
			throw new PaymentsException(PaymentsExceptionMessageId.PAYMENT_INSUFFICIENT_FUNDS,
					ImmutableMap.of(PAYMENTS_EXCEPTION_DATA_KEY, "Trying to refund more than had been charged"));
		}

		MoneyDTO moneyToRefund = getMoneyDtoCalculator().cloneMoneyDto(creditRequest.getAmount());
		final Multimap<PaymentEvent, MoneyDTO> refundablePaymentEvents = getPaymentHistory().getRefundablePaymentEvents(ledger);
		final List<PaymentEvent> paymentEvents = new ArrayList<>();
		for (Map.Entry<PaymentEvent, MoneyDTO> refundableEvent : refundablePaymentEvents.entries()) {
			final PaymentEvent chargeEvent = refundableEvent.getKey();
			final MoneyDTO eventAmount = refundableEvent.getValue();
			final PaymentInstrumentDTO instrument = chargeEvent.getOrderPaymentInstrumentDTO().getPaymentInstrument();
			final PaymentProvider paymentProvider = getPaymentProvider(instrument.getPaymentProviderConfigurationGuid());
			if (hasAny(moneyToRefund)) {
				if (hasEnoughCharged(eventAmount, moneyToRefund)) {
					final MoneyDTO refundLeftover = getMoneyDtoCalculator().cloneMoneyDto(moneyToRefund);
					moneyToRefund.setAmount(BigDecimal.ZERO);
					paymentEvents.addAll(paymentProvider.getCapability(CreditCapability.class)
							.map(creditCapability -> executeCapability(creditCapability, creditRequest, chargeEvent, refundLeftover))
							.orElseThrow(() -> new PaymentsException(PaymentsExceptionMessageId.PAYMENT_CAPABILITY_UNSUPPORTED,
									ImmutableMap.of(PAYMENTS_EXCEPTION_DATA_KEY, "Credit capability is not supported by payment provider")))
							.getEvents());
				} else {
					moneyToRefund = getMoneyDtoCalculator().minus(moneyToRefund, eventAmount);
					paymentEvents.addAll(paymentProvider.getCapability(CreditCapability.class)
							.map(creditCapability -> executeCapability(creditCapability, creditRequest, chargeEvent, eventAmount))
							.orElseThrow(() -> new PaymentsException(PaymentsExceptionMessageId.PAYMENT_CAPABILITY_UNSUPPORTED,
									ImmutableMap.of(PAYMENTS_EXCEPTION_DATA_KEY, "Credit capability is not supported by payment provider")))
							.getEvents());
				}
			}
		}

		return createPaymentAPIResponseForRefund(paymentEvents, creditRequest);
	}

	/**
	 * This method create PaymentApiResponse object with external and internal messages in case when something went wrong and without this messages
	 * if everything went fine.
	 *
	 * @param paymentEvents list of payment events.
	 * @param creditRequest the implicit credit request.
	 * @return PaymentAPIResponse object
	 */
	protected PaymentAPIResponse createPaymentAPIResponseForRefund(final List<PaymentEvent> paymentEvents, final CreditRequest creditRequest) {
		final List<PaymentEvent> ledger = creditRequest.getLedger();
		List<PaymentEvent> newLedger = new ArrayList<>();
		newLedger.addAll(ledger);
		newLedger.addAll(paymentEvents);
		final MoneyDTO refundedAmount = getPaymentHistory().getRefundedAmount(ledger);

		boolean isRefundSuccessful = isRefundSuccessful(creditRequest, newLedger, refundedAmount);

		return paymentEvents.stream()
				.filter(paymentEvent -> FAILED.equals(paymentEvent.getPaymentStatus())).reduce((firstElement, lastElement) -> lastElement)
				.map(lastFailedPE -> new PaymentAPIResponse(paymentEvents, lastFailedPE.getExternalMessage(),
						lastFailedPE.getInternalMessage()))
				.orElse(getPaymentApiResponse(paymentEvents, isRefundSuccessful));
	}

	private PaymentAPIResponse getPaymentApiResponse(final List<PaymentEvent> paymentEvents, final boolean isRefundSuccessful) {
		if (isRefundSuccessful) {
			return new PaymentAPIResponse(paymentEvents, isRefundSuccessful);
		}
		return new PaymentAPIResponse(paymentEvents, "The system could not cancel the reservation.",
				"The cancel reservation process failed due to no cancelable payment events.");
	}

	@Override
	public PaymentAPIResponse manualCredit(final ManualCreditRequest creditRequest) {
		final List<PaymentEvent> ledger = creditRequest.getLedger();
		final MoneyDTO chargedAmount = getPaymentHistory().getChargedAmount(ledger);
		final MoneyDTO refundedAmount = getPaymentHistory().getRefundedAmount(ledger);
		final MoneyDTO refundableAmount = getMoneyDtoCalculator().minus(chargedAmount, refundedAmount);
		if (!hasEnoughCharged(refundableAmount, creditRequest.getAmount())) {
			throw new PaymentsException(PaymentsExceptionMessageId.PAYMENT_INSUFFICIENT_FUNDS,
					ImmutableMap.of(PAYMENTS_EXCEPTION_DATA_KEY, "Trying to refund more than had been charged"));
		}

		MoneyDTO moneyToRefund = getMoneyDtoCalculator().cloneMoneyDto(creditRequest.getAmount());
		final Multimap<PaymentEvent, MoneyDTO> refundablePaymentEvents = getPaymentHistory().getRefundablePaymentEvents(ledger);
		final List<PaymentEvent> paymentEvents = new ArrayList<>();
		for (Map.Entry<PaymentEvent, MoneyDTO> refundableEvent : refundablePaymentEvents.entries()) {
			final PaymentEvent chargeEvent = refundableEvent.getKey();
			final MoneyDTO eventAmount = refundableEvent.getValue();
			if (hasAny(moneyToRefund)) {
				if (hasEnoughCharged(eventAmount, moneyToRefund)) {
					final MoneyDTO refundLeftover = getMoneyDtoCalculator().cloneMoneyDto(moneyToRefund);
					moneyToRefund.setAmount(BigDecimal.ZERO);
					paymentEvents.add(buildSucceededManualCreditPaymentEvent(creditRequest, refundLeftover, chargeEvent));
				} else {
					moneyToRefund = getMoneyDtoCalculator().minus(moneyToRefund, eventAmount);
					paymentEvents.add(buildSucceededManualCreditPaymentEvent(creditRequest, eventAmount, chargeEvent));
				}
			}
		}

		return createPaymentAPIResponseForManualRefund(paymentEvents, creditRequest);
	}

	/**
	 * This method create PaymentApiResponse object with external and internal messages in case when something went wrong and without this messages
	 * if everything went fine.
	 *
	 * @param paymentEvents list of payment events.
	 * @param creditRequest manual credit request.
	 * @return PaymentAPIResponse object
	 */
	protected PaymentAPIResponse createPaymentAPIResponseForManualRefund(final List<PaymentEvent> paymentEvents,
																		 final ManualCreditRequest creditRequest) {
		final List<PaymentEvent> ledger = creditRequest.getLedger();
		List<PaymentEvent> newLedger = new ArrayList<>();
		newLedger.addAll(ledger);
		newLedger.addAll(paymentEvents);
		final MoneyDTO refundedAmount = getPaymentHistory().getRefundedAmount(ledger);

		boolean isManualRefundSuccessful = isManualRefundSuccessful(creditRequest, newLedger, refundedAmount);

		return paymentEvents.stream()
				.filter(paymentEvent -> FAILED.equals(paymentEvent.getPaymentStatus())).reduce((firstElement, lastElement) -> lastElement)
				.map(lastFailedPE -> new PaymentAPIResponse(paymentEvents, lastFailedPE.getExternalMessage(),
						lastFailedPE.getInternalMessage()))
				.orElse(new PaymentAPIResponse(paymentEvents, isManualRefundSuccessful));
	}

	@Override
	public PaymentAPIResponse reverseCharge(final ReverseChargeRequest reverseChargeRequest) {
		final List<PaymentEvent> chargePaymentEvents = reverseChargeRequest.getSelectedPaymentEvents();
		sanityCheckChargePaymentEvents(chargePaymentEvents);
		final List<PaymentEvent> paymentEvents = new ArrayList<>();
		for (PaymentEvent chargePaymentEvent : chargePaymentEvents) {
			final PaymentInstrumentDTO instrument = chargePaymentEvent.getOrderPaymentInstrumentDTO().getPaymentInstrument();
			final PaymentProvider paymentProvider = getPaymentProvider(instrument.getPaymentProviderConfigurationGuid());
			paymentEvents.addAll(paymentProvider.getCapability(ReverseChargeCapability.class)
					.map(capability -> reverseChargeOrCredit(capability, reverseChargeRequest, chargePaymentEvent, paymentProvider))
					.orElseGet(() -> simulateReverseChargeWithCredit(reverseChargeRequest, chargePaymentEvent, paymentProvider)));
		}

		return createPaymentAPIResponseForReverseCharge(paymentEvents, reverseChargeRequest);
	}

	/**
	 * This method create PaymentApiResponse object with external and internal messages in case when something went wrong and without this messages
	 * if everything went fine.
	 *
	 * @param paymentEvents        list of payment events.
	 * @param reverseChargeRequest the reverse charge request.
	 * @return PaymentAPIResponse object
	 */
	protected PaymentAPIResponse createPaymentAPIResponseForReverseCharge(final List<PaymentEvent> paymentEvents,
																		  final ReverseChargeRequest reverseChargeRequest) {
		final List<PaymentEvent> ledger = reverseChargeRequest.getLedger();
		List<PaymentEvent> newLedger = new ArrayList<>();
		newLedger.addAll(ledger);
		newLedger.addAll(paymentEvents);

		boolean isReverseChargeSuccessful = isReverseChargeSuccessful(newLedger);

		if (paymentEvents.isEmpty() && !isReverseChargeSuccessful) {
			return new PaymentAPIResponse(paymentEvents,
					"The reverse charge request failed.",
					"The reverse charge process failed as there are no chargeable events or the reverse charge capability "
							+ "is not supported(has thrown exception) by the payment provider.");
		} else {
			return paymentEvents.stream()
					.filter(paymentEvent -> FAILED.equals(paymentEvent.getPaymentStatus())).reduce((firstElement, lastElement) -> lastElement)
					.map(lastFailedPE -> new PaymentAPIResponse(paymentEvents, lastFailedPE.getExternalMessage(),
							lastFailedPE.getInternalMessage()))
					.orElse(new PaymentAPIResponse(paymentEvents, isReverseChargeSuccessful));
		}
	}

	/**
	 * Make sure that all charge payment events are approved charges.
	 *
	 * @param chargePaymentEventCandidates candidates
	 */
	protected void sanityCheckChargePaymentEvents(final List<PaymentEvent> chargePaymentEventCandidates) {
		for (PaymentEvent paymentEvent : chargePaymentEventCandidates) {
			if (paymentEvent.getPaymentType() != CHARGE || paymentEvent.getPaymentStatus() != APPROVED) {
				throw new IllegalStateException("Attempting to make a reverse charge operation on non-refundable events");
			}
		}
	}

	/**
	 * Executes credit plugin capability.
	 *
	 * @param creditCapability   credit plugin capability
	 * @param creditRequest      original Payment API credit request
	 * @param chargePaymentEvent existing charge payment event to refund
	 * @param amount             credited amount
	 * @return Payment API response with the new ledger
	 */
	protected PaymentAPIResponse executeCapability(final CreditCapability creditCapability,
												   final PaymentAPIRequest creditRequest,
												   final PaymentEvent chargePaymentEvent,
												   final MoneyDTO amount) {
		final CreditCapabilityRequest creditCapabilityRequest = createCreditCapabilityRequest(chargePaymentEvent, amount, creditRequest);
		try {
			final PaymentCapabilityResponse response = creditCapability.credit(creditCapabilityRequest);
			final PaymentEvent creditPaymentEvent = buildSucceededCreditPaymentEvent(creditRequest, amount, chargePaymentEvent, response);
			return new PaymentAPIResponse(ImmutableList.of(creditPaymentEvent), true);
		} catch (PaymentCapabilityRequestFailedException ex) {
			final PaymentEvent creditPaymentEvent = buildFailedPaymentEvent(creditRequest, amount, chargePaymentEvent, ex, CREDIT);
			return new PaymentAPIResponse(ImmutableList.of(creditPaymentEvent), false);
		}
	}

	/**
	 * Executes reverse charge plugin capability or if it fails - simulates it with credit plugin capability.
	 *
	 * @param reverseChargeCapability reverse charge plugin capability
	 * @param reverseChargeRequest    original Payment API reverse charge request
	 * @param chargePaymentEvent      existing charge payment event to reverse
	 * @param paymentProvider         payment provider
	 * @return the new ledger
	 */
	protected List<PaymentEvent> reverseChargeOrCredit(final ReverseChargeCapability reverseChargeCapability,
													   final ReverseChargeRequest reverseChargeRequest,
													   final PaymentEvent chargePaymentEvent,
													   final PaymentProvider paymentProvider) {
		final ReverseChargeCapabilityRequest capabilityRequest = createReverseChargeCapabilityRequest(reverseChargeRequest, chargePaymentEvent);
		try {
			final PaymentCapabilityResponse response = reverseChargeCapability.reverseCharge(capabilityRequest);
			return ImmutableList.of(buildSucceededReverseChargePaymentEvent(reverseChargeRequest, chargePaymentEvent, response));
		} catch (PaymentCapabilityRequestFailedException exception) {
			return simulateReverseChargeWithCredit(reverseChargeRequest, chargePaymentEvent, paymentProvider);
		}
	}

	/**
	 * Executes credit plugin capability as as simulation of reverse charge request.
	 *
	 * @param reverseChargeRequest original Payment API reverse charge request
	 * @param chargePaymentEvent   existing charge payment event to reverse
	 * @param paymentProvider      payment provider
	 * @return the new ledger
	 */
	protected List<PaymentEvent> simulateReverseChargeWithCredit(final ReverseChargeRequest reverseChargeRequest,
																 final PaymentEvent chargePaymentEvent,
																 final PaymentProvider paymentProvider) {
		final MoneyDTO reversedAmount = chargePaymentEvent.getAmount();
		return paymentProvider.getCapability(CreditCapability.class)
				.map(creditCapability -> executeCreditCapabilityForReverseCharge(creditCapability,
						reverseChargeRequest, chargePaymentEvent, reversedAmount).getEvents())
				.orElseGet(Collections::emptyList);
	}

	/**
	 * Executes credit plugin capability.
	 *
	 * @param creditCapability   credit plugin capability
	 * @param creditRequest      original Payment API credit request
	 * @param chargePaymentEvent existing charge payment event to refund
	 * @param amount             credited amount
	 * @return Payment API response with the new ledger
	 */
	protected PaymentAPIResponse executeCreditCapabilityForReverseCharge(final CreditCapability creditCapability,
																		 final PaymentAPIRequest creditRequest,
																		 final PaymentEvent chargePaymentEvent,
																		 final MoneyDTO amount) {
		try {
			final PaymentCapabilityResponse response = creditCapability
					.credit(createCreditCapabilityRequest(chargePaymentEvent, amount, creditRequest));
			final PaymentEvent reverseChargePaymentEvent = buildSucceededReverseChargePaymentEvent(creditRequest, chargePaymentEvent, response);
			return new PaymentAPIResponse(ImmutableList.of(reverseChargePaymentEvent), true);
		} catch (PaymentCapabilityRequestFailedException ex) {
			final PaymentEvent reverseChargePaymentEvent = buildFailedPaymentEvent(creditRequest, amount, chargePaymentEvent, ex, REVERSE_CHARGE);
			return new PaymentAPIResponse(ImmutableList.of(reverseChargePaymentEvent), false);
		}
	}

	/**
	 * Creates reverse charge plugin capability request.
	 *
	 * @param paymentAPIRequest  original Payment API reverse charge request
	 * @param chargePaymentEvent existing charge payment event to reverse
	 * @return reverse charge plugin capability request
	 */
	protected ReverseChargeCapabilityRequest createReverseChargeCapabilityRequest(final PaymentAPIRequest paymentAPIRequest,
																				  final PaymentEvent chargePaymentEvent) {
		final PaymentInstrumentDTO instrument = chargePaymentEvent.getOrderPaymentInstrumentDTO().getPaymentInstrument();
		return ReverseChargeCapabilityRequestBuilder.builder()
				.withPaymentInstrumentData(instrument.getData())
				.withChargeData(chargePaymentEvent.getPaymentEventData())
				.withCustomRequestData(paymentAPIRequest.getCustomRequestData())
				.withOrderContext(paymentAPIRequest.getOrderContext())
				.build(getBeanFactory().getPrototypeBean(REVERSE_CHARGE_CAPABILITY_REQUEST, ReverseChargeCapabilityRequest.class));
	}

	/**
	 * Creates credit plugin capability request.
	 *
	 * @param chargePaymentEvent existing charge payment event to refund
	 * @param amount             credited amount
	 * @param creditRequest      original Payment API credit request or Payment API reverse charge request
	 * @return credit plugin capability request
	 */
	protected CreditCapabilityRequest createCreditCapabilityRequest(final PaymentEvent chargePaymentEvent,
																	final MoneyDTO amount,
																	final PaymentAPIRequest creditRequest) {
		final OrderPaymentInstrumentDTO instrument = chargePaymentEvent.getOrderPaymentInstrumentDTO();
		return CreditCapabilityRequestBuilder.builder()
				.withAmount(amount)
				.withCustomRequestData(creditRequest.getCustomRequestData())
				.withChargeData(chargePaymentEvent.getPaymentEventData())
				.withPaymentInstrumentData(instrument.getPaymentInstrument().getData())
				.withOrderContext(creditRequest.getOrderContext())
				.build(getBeanFactory().getPrototypeBean(CREDIT_CAPABILITY_REQUEST, CreditCapabilityRequest.class));
	}

	/**
	 * Creates failed payment event.
	 *
	 * @param creditRequest      original Payment API credit request
	 * @param moneyToRefund      refunded/credited amount
	 * @param chargePaymentEvent existing charge payment event we attempted to refund
	 * @param exception          plugin capability exception
	 * @param transactionType    transaction type
	 * @return failed payment event
	 */
	protected PaymentEvent buildFailedPaymentEvent(final PaymentAPIRequest creditRequest,
												   final MoneyDTO moneyToRefund,
												   final PaymentEvent chargePaymentEvent,
												   final PaymentCapabilityRequestFailedException exception,
												   final TransactionType transactionType) {
		final OrderPaymentInstrumentDTO instrumentDTO = chargePaymentEvent.getOrderPaymentInstrumentDTO();
		final String guid = UUID.randomUUID().toString();
		return PaymentEventBuilder.aPaymentEvent()
				.withGuid(guid)
				.withParentGuid(chargePaymentEvent.getGuid())
				.withOrderPaymentInstrumentDTO(instrumentDTO)
				.withPaymentType(transactionType)
				.withPaymentStatus(FAILED)
				.withReferenceId(creditRequest.getOrderContext().getOrderNumber())
				.withAmount(moneyToRefund)
				.withOriginalPaymentInstrument(true)
				.withTemporaryFailure(exception.isTemporaryFailure())
				.withExternalMessage(exception.getExternalMessage())
				.withInternalMessage(exception.getInternalMessage())
				.build(getBeanFactory());
	}

	/**
	 * Creates successful credit event.
	 *
	 * @param creditRequest      original Payment API credit request
	 * @param moneyToRefund      refunded/credited amount
	 * @param chargePaymentEvent existing charge payment event we refunded
	 * @param response           plugin capability response
	 * @return approved credit payment event
	 */
	protected PaymentEvent buildSucceededCreditPaymentEvent(final PaymentAPIRequest creditRequest,
															final MoneyDTO moneyToRefund,
															final PaymentEvent chargePaymentEvent,
															final PaymentCapabilityResponse response) {
		final OrderPaymentInstrumentDTO instrumentDTO = chargePaymentEvent.getOrderPaymentInstrumentDTO();
		final String guid = UUID.randomUUID().toString();
		return PaymentEventBuilder.aPaymentEvent()
				.withGuid(guid)
				.withParentGuid(chargePaymentEvent.getGuid())
				.withOrderPaymentInstrumentDTO(instrumentDTO)
				.withPaymentType(CREDIT)
				.withPaymentStatus(APPROVED)
				.withReferenceId(creditRequest.getOrderContext().getOrderNumber())
				.withAmount(moneyToRefund)
				.withOriginalPaymentInstrument(true)
				.withPaymentEventData(response.getData())
				.build(getBeanFactory());
	}

	/**
	 * Creates successful manual credit event.
	 *
	 * @param creditRequest      original Payment API credit request
	 * @param moneyToRefund      refunded/credited amount
	 * @param chargePaymentEvent existing charge payment event we refunded
	 * @return approved manual credit payment event
	 */
	protected PaymentEvent buildSucceededManualCreditPaymentEvent(final PaymentAPIRequest creditRequest,
																  final MoneyDTO moneyToRefund,
																  final PaymentEvent chargePaymentEvent) {
		final OrderPaymentInstrumentDTO instrumentDTO = chargePaymentEvent.getOrderPaymentInstrumentDTO();
		final String guid = UUID.randomUUID().toString();
		return PaymentEventBuilder.aPaymentEvent()
				.withGuid(guid)
				.withParentGuid(chargePaymentEvent.getGuid())
				.withOrderPaymentInstrumentDTO(instrumentDTO)
				.withPaymentType(TransactionType.MANUAL_CREDIT)
				.withPaymentStatus(APPROVED)
				.withReferenceId(creditRequest.getOrderContext().getOrderNumber())
				.withAmount(moneyToRefund)
				.withOriginalPaymentInstrument(true)
				.withPaymentEventData(Collections.emptyMap())
				.build(getBeanFactory());
	}

	/**
	 * Creates successful reverse charge event.
	 *
	 * @param reverseChargeRequest original Payment API reverse charge request
	 * @param chargePaymentEvent   existing charge payment event we reversed
	 * @param response             plugin capability response
	 * @return approved reverse charge event
	 */
	protected PaymentEvent buildSucceededReverseChargePaymentEvent(final PaymentAPIRequest reverseChargeRequest,
																   final PaymentEvent chargePaymentEvent,
																   final PaymentCapabilityResponse response) {

		final OrderPaymentInstrumentDTO instrumentDTO = chargePaymentEvent.getOrderPaymentInstrumentDTO();
		final String guid = UUID.randomUUID().toString();
		return PaymentEventBuilder.aPaymentEvent()
				.withGuid(guid)
				.withParentGuid(chargePaymentEvent.getGuid())
				.withOrderPaymentInstrumentDTO(instrumentDTO)
				.withPaymentType(REVERSE_CHARGE)
				.withPaymentStatus(APPROVED)
				.withReferenceId(reverseChargeRequest.getOrderContext().getOrderNumber())
				.withAmount(chargePaymentEvent.getAmount())
				.withOriginalPaymentInstrument(true)
				.withPaymentEventData(response.getData())
				.build(getBeanFactory());
	}

	/**
	 * Checks if refund/credit is considered to be fulfilled for specified ledger sequence.
	 *
	 * @param creditRequest  credit request
	 * @param newLedger      new ledger sequence
	 * @param refundedBefore amount that was already refunded/credited before execution
	 * @return true if refund/credit request is considered to be fulfilled
	 */
	protected boolean isRefundSuccessful(final CreditRequest creditRequest, final List<PaymentEvent> newLedger, final MoneyDTO refundedBefore) {
		final MoneyDTO refundedAmount = getPaymentHistory().getRefundedAmount(newLedger);
		final MoneyDTO amountAfterRefund = getMoneyDtoCalculator().plus(refundedBefore, creditRequest.getAmount());
		return getMoneyDtoCalculator().compare(amountAfterRefund, refundedAmount) == 0;
	}

	/**
	 * Checks if manual refund/credit is considered to be fulfilled for specified ledger sequence.
	 *
	 * @param creditRequest  manual credit request
	 * @param newLedger      new ledger sequence
	 * @param refundedBefore amount that was already refunded/credited before execution
	 * @return true if manual refund/credit request is considered to be fulfilled
	 */
	protected boolean isManualRefundSuccessful(final ManualCreditRequest creditRequest,
											   final List<PaymentEvent> newLedger,
											   final MoneyDTO refundedBefore) {
		final MoneyDTO refundedAmount = getPaymentHistory().getRefundedAmount(newLedger);
		final MoneyDTO amountAfterRefund = getMoneyDtoCalculator().plus(refundedBefore, creditRequest.getAmount());
		return getMoneyDtoCalculator().compare(amountAfterRefund, refundedAmount) == 0;
	}

	/**
	 * Checks if reverse charge is considered to be fulfilled for specified ledger sequence.
	 *
	 * @param newLedger new ledger sequence
	 * @return true if reverse charge request is considered to be fulfilled
	 */
	protected boolean isReverseChargeSuccessful(final List<PaymentEvent> newLedger) {
		final MoneyDTO chargedAmount = getPaymentHistory().getChargedAmount(newLedger);
		return !getMoneyDtoCalculator().hasBalance(chargedAmount);
	}

	private boolean hasAny(final MoneyDTO moneyToRefund) {
		return getMoneyDtoCalculator().isPositive(moneyToRefund);
	}

	private boolean hasEnoughCharged(final MoneyDTO chargedMoney, final MoneyDTO moneyToRefund) {
		return getMoneyDtoCalculator().compare(chargedMoney, moneyToRefund) >= 0;
	}
}
