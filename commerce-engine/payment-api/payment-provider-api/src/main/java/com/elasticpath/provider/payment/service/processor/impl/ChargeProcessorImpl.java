/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.provider.payment.service.processor.impl;

import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.APPROVED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.FAILED;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.CHARGE_CAPABILITY_REQUEST;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Multimap;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequestFailedException;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityResponse;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapability;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapabilityRequestBuilder;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.OrderContext;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.provider.payment.domain.PaymentProvider;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIRequest;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIResponse;
import com.elasticpath.provider.payment.domain.transaction.cancel.CancelReservationRequest;
import com.elasticpath.provider.payment.domain.transaction.cancel.CancelReservationRequestBuilder;
import com.elasticpath.provider.payment.domain.transaction.charge.ChargeRequest;
import com.elasticpath.provider.payment.domain.transaction.modify.ModifyReservationRequest;
import com.elasticpath.provider.payment.domain.transaction.modify.ModifyReservationRequestBuilder;
import com.elasticpath.provider.payment.domain.transaction.reservation.ReserveRequest;
import com.elasticpath.provider.payment.domain.transaction.reservation.ReserveRequestBuilder;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigurationService;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.event.PaymentEventBuilder;
import com.elasticpath.provider.payment.service.history.PaymentHistory;
import com.elasticpath.provider.payment.service.history.util.MoneyDtoCalculator;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;
import com.elasticpath.provider.payment.service.processor.AbstractProcessor;
import com.elasticpath.provider.payment.service.processor.ChargeProcessor;
import com.elasticpath.provider.payment.service.processor.ReservationProcessor;
import com.elasticpath.provider.payment.service.provider.PaymentProviderService;
import com.elasticpath.provider.payment.workflow.PaymentAPIWorkflow;

/**
 * Default implementation of {@link ChargeProcessor}.
 */
public class ChargeProcessorImpl extends AbstractProcessor implements ChargeProcessor {

	private final ReservationProcessor reservationProcessor;

	/**
	 * Constructor.
	 *
	 * @param paymentProviderConfigurationService payment provider configuration service
	 * @param paymentProviderService              payment provider service
	 * @param paymentHistory                      payment history helper service
	 * @param moneyDtoCalculator                  calculator of mathematical operation with MoneyDto
	 * @param paymentAPIWorkflow                  payment API workflow facade
	 * @param reservationProcessor                reservation processor
	 * @param beanFactory                         EP bean factory
	 */
	public ChargeProcessorImpl(final PaymentProviderConfigurationService paymentProviderConfigurationService,
							   final PaymentProviderService paymentProviderService,
							   final PaymentHistory paymentHistory,
							   final MoneyDtoCalculator moneyDtoCalculator,
							   final PaymentAPIWorkflow paymentAPIWorkflow,
							   final ReservationProcessor reservationProcessor,
							   final BeanFactory beanFactory) {
		super(paymentProviderConfigurationService, paymentProviderService, paymentHistory, moneyDtoCalculator, paymentAPIWorkflow, beanFactory);
		this.reservationProcessor = reservationProcessor;
	}

	@Override
	public PaymentAPIResponse chargePayment(final ChargeRequest chargeRequest) {
		final List<PaymentEvent> generatedEvents = new ArrayList<>();

		if (!getMoneyDtoCalculator().hasBalance(chargeRequest.getOrderContext().getOrderTotal())) {
			return new PaymentAPIResponse(generatedEvents, true);
		}

		final List<PaymentEvent> existingLedger = new ArrayList<>(chargeRequest.getLedger());
		final MoneyDTO chargedAmount = getPaymentHistory().getChargedAmount(existingLedger);
		final MoneyDTO toBeCharged = getMoneyDtoCalculator().minus(chargeRequest.getTotalChargeableAmount(), chargedAmount);

		if (doesNotContainEnoughReservedAmount(existingLedger, toBeCharged)) {
			final ModifyReservationRequest modifyReservationRequest = createModifyReservationRequest(chargeRequest);
			final PaymentAPIResponse paymentAPIResponse = getPaymentAPIWorkflow().modifyReservation(modifyReservationRequest);

			existingLedger.addAll(paymentAPIResponse.getEvents());
			generatedEvents.addAll(paymentAPIResponse.getEvents());

			if (doesNotContainEnoughReservedAmount(existingLedger, toBeCharged)) {
				return new PaymentAPIResponse(existingLedger,
						"Charge request failed due to insufficient funds reserved in the Payment instrument.",
						"The charge failed because there was not enough amount reserved on the payment instrument.");
			}
		}

		if (chargeRequest.hasSingleReservePerPI() && !chargeRequest.isFinalPayment()) {
			return new PaymentAPIResponse(generatedEvents, true);
		}

		final Multimap<PaymentEvent, MoneyDTO> chargeablePaymentEvents = getPaymentHistory().getChargeablePaymentEvents(existingLedger);

		for (PaymentEvent reservationEvent : chargeablePaymentEvents.keySet()) {
			if (getMoneyDtoCalculator().hasBalance(toBeCharged)) {
				generatedEvents.addAll(chargePayment(toBeCharged, reservationEvent, chargeRequest));
			}
		}

		return createPaymentAPIResponse(generatedEvents, chargeRequest);
	}

	/**
	 * This method create PaymentApiResponse object with external and internal messages in case when something went wrong and without this messages
	 * if everything went fine.
	 *
	 * @param paymentEvents list of payment events.
	 * @param chargeRequest the charge request.
	 * @return PaymentAPIResponse object
	 */
	protected PaymentAPIResponse createPaymentAPIResponse(final List<PaymentEvent> paymentEvents, final ChargeRequest chargeRequest) {
		final List<PaymentEvent> ledger = chargeRequest.getLedger();
		List<PaymentEvent> newLedger = new ArrayList<>();
		newLedger.addAll(ledger);
		newLedger.addAll(paymentEvents);

		boolean isChargeSuccessful = isChargeSuccessful(chargeRequest, newLedger);

		if (paymentEvents.isEmpty() && !isChargeSuccessful) {
			return new PaymentAPIResponse(paymentEvents,
					"The system could not charge the reservation.",
					"The charge process failed as reverse charge capability is not supported by the payment provider.");
		} else {
			return paymentEvents.stream()
					.filter(paymentEvent -> FAILED.equals(paymentEvent.getPaymentStatus())).reduce((firstElement, lastElement) -> lastElement)
					.map(lastFailedPE -> new PaymentAPIResponse(paymentEvents, lastFailedPE.getExternalMessage(),
							lastFailedPE.getInternalMessage()))
					.orElse(new PaymentAPIResponse(paymentEvents, isChargeSuccessful));
		}
	}

	/**
	 * Check if the ledger contains required total amount of reservations.
	 *
	 * @param ledger           the ledger
	 * @param shouldBeReserved required total amount of reservations
	 * @return true if we DID NOT reserve enough money
	 */
	protected boolean doesNotContainEnoughReservedAmount(final List<PaymentEvent> ledger, final MoneyDTO shouldBeReserved) {
		final MoneyDTO availableReservedAmount = getPaymentHistory().getAvailableReservedAmount(ledger);
		return getMoneyDtoCalculator().compare(shouldBeReserved, availableReservedAmount) > 0;
	}

	/**
	 * Charges specified amount against the reservation, reattempting in case if reservation was expired.
	 * Creates reservation for leftovers if charge was partial.
	 *
	 * @param toBeCharged      amount to be charged
	 * @param reservationEvent existing reservation event
	 * @param chargeRequest    original Payment API charge request
	 * @return the new ledger
	 */
	protected List<PaymentEvent> chargePayment(final MoneyDTO toBeCharged,
											   final PaymentEvent reservationEvent,
											   final ChargeRequest chargeRequest) {
		final MoneyDTO reservedAmount = reservationEvent.getAmount();
		final List<PaymentEvent> paymentEvents = new ArrayList<>();

		if (getMoneyDtoCalculator().compare(toBeCharged, reservedAmount) == 0) {
			try {
				paymentEvents.add(processChargeOnPaymentProvider(toBeCharged, reservationEvent, chargeRequest));
				getMoneyDtoCalculator().resetToZero(toBeCharged);

				return paymentEvents;
			} catch (PaymentCapabilityRequestFailedException e) {
				processFailedRequest(toBeCharged, reservationEvent, chargeRequest, paymentEvents, e);
			}
		}

		if (getMoneyDtoCalculator().compare(toBeCharged, reservedAmount) > 0) {
			try {
				paymentEvents.add(processChargeOnPaymentProvider(reservedAmount, reservationEvent, chargeRequest));
				getMoneyDtoCalculator().decrease(toBeCharged, reservedAmount);

				return paymentEvents;
			} catch (PaymentCapabilityRequestFailedException e) {
				processFailedRequest(toBeCharged, reservationEvent, chargeRequest, paymentEvents, e);
			}
		}

		if (getMoneyDtoCalculator().compare(toBeCharged, reservedAmount) < 0) {
			try {
				paymentEvents.add(processChargeOnPaymentProvider(toBeCharged, reservationEvent, chargeRequest));

				if (!chargeRequest.isFinalPayment()) {
					final MoneyDTO toBeReserved = getMoneyDtoCalculator().minus(reservedAmount, toBeCharged);
					final OrderPaymentInstrumentDTO instrumentDTO = reservationEvent.getOrderPaymentInstrumentDTO();
					final Map<String, String> customRequestData = chargeRequest.getCustomRequestData();
					final OrderContext orderContext = chargeRequest.getOrderContext();
					final PaymentAPIResponse reservationResponse = reservationProcessor.reserveToSimulateModify(
							toBeReserved, instrumentDTO, customRequestData, orderContext, calculateReservationCount(chargeRequest));
					paymentEvents.addAll(reservationResponse.getEvents());
				}
				getMoneyDtoCalculator().resetToZero(toBeCharged);

				return paymentEvents;
			} catch (PaymentCapabilityRequestFailedException e) {
				processFailedRequest(toBeCharged, reservationEvent, chargeRequest, paymentEvents, e);
			}
		}

		return paymentEvents;
	}

	private int calculateReservationCount(final ChargeRequest request) {
		return Math.toIntExact(request.getLedger()
				.stream()
				.filter(payment -> payment.getPaymentType() == TransactionType.RESERVE
						|| payment.getPaymentType() == TransactionType.MODIFY_RESERVE)
				.count());
	}

	private void processFailedRequest(final MoneyDTO toBeCharged, final PaymentEvent reservationEvent, final ChargeRequest chargeRequest,
									  final List<PaymentEvent> paymentEvents, final PaymentCapabilityRequestFailedException failedException) {
		// Record the exception
		paymentEvents.add(buildFailedChargeEvent(toBeCharged, chargeRequest, reservationEvent, failedException));
		paymentEvents.addAll(processChargeOnExpiredReserve(toBeCharged, reservationEvent, chargeRequest));
	}

	/**
	 * Cancels expired reservation, creates a new one and immediately charges specified amount against it.
	 * Generates failed event if attempt was unsuccessful.
	 *
	 * @param toBeCharged      amount to be charged
	 * @param reservationEvent existing reservation event
	 * @param chargeRequest    original Payment API charge request
	 * @return the new ledger
	 */
	protected List<PaymentEvent> processChargeOnExpiredReserve(final MoneyDTO toBeCharged,
															   final PaymentEvent reservationEvent,
															   final ChargeRequest chargeRequest) {
		final CancelReservationRequest cancelReservationRequest = createCancelReservationRequest(reservationEvent, chargeRequest);
		final PaymentAPIResponse cancelReservationPaymentAPIResponse = getPaymentAPIWorkflow().cancelReservation(cancelReservationRequest);

		final List<PaymentEvent> paymentEvents = new ArrayList<>(cancelReservationPaymentAPIResponse.getEvents());
		final OrderPaymentInstrumentDTO instrumentDTO = reservationEvent.getOrderPaymentInstrumentDTO();
		final ReserveRequest reserveRequest = createReserveRequest(instrumentDTO, toBeCharged, chargeRequest);
		final PaymentAPIResponse reservePaymentApiResponse = getPaymentAPIWorkflow().reserve(reserveRequest);
		paymentEvents.addAll(reservePaymentApiResponse.getEvents());

		for (PaymentEvent event : reservePaymentApiResponse.getEvents()) {
			try {
				paymentEvents.add(processChargeOnPaymentProvider(toBeCharged, event, chargeRequest));
			} catch (PaymentCapabilityRequestFailedException exception) {
				paymentEvents.add(buildFailedChargeEvent(toBeCharged, chargeRequest, event, exception));
			}
		}

		return paymentEvents;
	}

	/**
	 * Creates charge failed event.
	 *
	 * @param amount            charged amount
	 * @param paymentAPIRequest original Payment API charge request
	 * @param reservationEvent  existing reservation payment event we attempted to charge against
	 * @param exception         plugin capability exception
	 * @return failed payment event
	 */
	protected PaymentEvent buildFailedChargeEvent(final MoneyDTO amount,
												  final PaymentAPIRequest paymentAPIRequest,
												  final PaymentEvent reservationEvent,
												  final PaymentCapabilityRequestFailedException exception) {
		return PaymentEventBuilder.aPaymentEvent()
				.withParentGuid(reservationEvent.getGuid())
				.withOrderPaymentInstrumentDTO(reservationEvent.getOrderPaymentInstrumentDTO())
				.withPaymentType(TransactionType.CHARGE)
				.withPaymentStatus(FAILED)
				.withReferenceId(paymentAPIRequest.getOrderContext().getOrderNumber())
				.withAmount(amount)
				.withOriginalPaymentInstrument(true)
				.withTemporaryFailure(exception.isTemporaryFailure())
				.withExternalMessage(exception.getExternalMessage())
				.withInternalMessage(exception.getInternalMessage())
				.build(getBeanFactory());
	}

	/**
	 * Charges specified amount against the reservation.
	 *
	 * @param toBeCharged       amount to be charged
	 * @param reservationEvent  existing reservation event
	 * @param paymentAPIRequest original Payment API charge request
	 * @return payment event
	 * @throws PaymentCapabilityRequestFailedException plugin capability exception
	 */
	protected PaymentEvent processChargeOnPaymentProvider(final MoneyDTO toBeCharged,
														  final PaymentEvent reservationEvent,
														  final PaymentAPIRequest paymentAPIRequest) throws PaymentCapabilityRequestFailedException {
		final OrderPaymentInstrumentDTO instrument = reservationEvent.getOrderPaymentInstrumentDTO();
		final PaymentProvider paymentProvider = getPaymentProvider(instrument.getPaymentInstrument().getPaymentProviderConfigurationGuid());
		final ChargeCapability chargeCapability = paymentProvider
				.getCapability(ChargeCapability.class)
				.orElseThrow(() -> new IllegalStateException(
						"Mandatory plugin charge capability is absent for payment provider: " + paymentProvider.getConfigurationName()));

		final ChargeCapabilityRequest chargeCapabilityRequest = createChargeCapabilityRequest(toBeCharged, reservationEvent, paymentAPIRequest);
		final PaymentCapabilityResponse paymentCapabilityResponse = chargeCapability.charge(chargeCapabilityRequest);

		return buildSucceededChargeEvent(toBeCharged, reservationEvent, paymentAPIRequest, paymentCapabilityResponse);
	}

	/**
	 * Creates successful charge event.
	 *
	 * @param amount            charged amount
	 * @param paymentAPIRequest original Payment API charge request
	 * @param reservationEvent  existing reservation payment event we charged against
	 * @param response          plugin capability response
	 * @return approved charge payment event
	 */
	protected PaymentEvent buildSucceededChargeEvent(final MoneyDTO amount,
													 final PaymentEvent reservationEvent,
													 final PaymentAPIRequest paymentAPIRequest,
													 final PaymentCapabilityResponse response) {
		return PaymentEventBuilder.aPaymentEvent()
				.withParentGuid(reservationEvent.getGuid())
				.withOrderPaymentInstrumentDTO(reservationEvent.getOrderPaymentInstrumentDTO())
				.withPaymentType(TransactionType.CHARGE)
				.withPaymentStatus(APPROVED)
				.withReferenceId(paymentAPIRequest.getOrderContext().getOrderNumber())
				.withAmount(getMoneyDtoCalculator().cloneMoneyDto(amount))
				.withOriginalPaymentInstrument(true)
				.withPaymentEventData(response.getData())
				.build(getBeanFactory());
	}

	/**
	 * Checks if charge is considered to be fulfilled for specified ledger sequence.
	 *
	 * @param chargeRequest charge request
	 * @param paymentEvents ledger sequence
	 * @return true if charge request is considered to be fulfilled
	 */
	protected boolean isChargeSuccessful(final ChargeRequest chargeRequest, final List<PaymentEvent> paymentEvents) {
		final MoneyDTO chargedAmount = getPaymentHistory().getChargedAmount(paymentEvents);

		return getMoneyDtoCalculator().compare(chargeRequest.getTotalChargeableAmount(),
				chargedAmount) == 0;
	}

	/**
	 * Create Payment API reservation request.
	 *
	 * @param instrument        order payment instrument
	 * @param amount            amount to be reserved
	 * @param paymentAPIRequest original Payment API charge request
	 * @return Payment API reservation request
	 */
	protected ReserveRequest createReserveRequest(final OrderPaymentInstrumentDTO instrument,
												  final MoneyDTO amount,
												  final PaymentAPIRequest paymentAPIRequest) {
		return ReserveRequestBuilder.builder()
				.withSelectedOrderPaymentInstruments(Collections.singletonList(instrument))
				.withAmount(amount)
				.withOrderContext(paymentAPIRequest.getOrderContext())
				.withCustomRequestData(paymentAPIRequest.getCustomRequestData())
				.build(getBeanFactory());
	}

	/**
	 * Create Payment API cancel reservation request.
	 *
	 * @param reservationEvent existing reservation payment event
	 * @param chargeRequest    original Payment API charge request
	 * @return Payment API cancel reservation request
	 */
	protected CancelReservationRequest createCancelReservationRequest(final PaymentEvent reservationEvent,
																	  final ChargeRequest chargeRequest) {
		return CancelReservationRequestBuilder.builder()
				.withOrderPaymentInstruments(chargeRequest.getOrderPaymentInstruments())
				.withSelectedPaymentEventsToCancel(Collections.singletonList(reservationEvent))
				.withLedger(chargeRequest.getLedger())
				.withCustomRequestData(chargeRequest.getCustomRequestData())
				.withAmount(reservationEvent.getAmount())
				.withOrderContext(chargeRequest.getOrderContext())
				.build(getBeanFactory());
	}

	/**
	 * Creates charge plugin capability request.
	 *
	 * @param paymentAPIRequest original Payment API charge request
	 * @param reservationEvent  existing reservation payment event to charge against
	 * @param toBeCharged       charged amount
	 * @return charge plugin capability request
	 */
	protected ChargeCapabilityRequest createChargeCapabilityRequest(final MoneyDTO toBeCharged,
																	final PaymentEvent reservationEvent,
																	final PaymentAPIRequest paymentAPIRequest) {
		return ChargeCapabilityRequestBuilder.builder()
				.withAmount(getMoneyDtoCalculator().cloneMoneyDto(toBeCharged))
				.withPaymentInstrumentData(reservationEvent.getOrderPaymentInstrumentDTO().getPaymentInstrument().getData())
				.withCustomRequestData(paymentAPIRequest.getCustomRequestData())
				.withReservationData(reservationEvent.getPaymentEventData())
				.withOrderContext(paymentAPIRequest.getOrderContext())
				.build(getBeanFactory().getPrototypeBean(CHARGE_CAPABILITY_REQUEST, ChargeCapabilityRequest.class));
	}

	/**
	 * Create Payment API modify reservation request in order to reserve enough money to charge.
	 *
	 * @param chargeRequest original Payment API charge request
	 * @return Payment API modify reservation request
	 */
	protected ModifyReservationRequest createModifyReservationRequest(final ChargeRequest chargeRequest) {
		return ModifyReservationRequestBuilder.builder()
				.withOrderPaymentInstruments(chargeRequest.getOrderPaymentInstruments())
				.withLedger(chargeRequest.getLedger())
				.withAmount(chargeRequest.getTotalChargeableAmount())
				.withCustomRequestData(chargeRequest.getCustomRequestData())
				.withOrderContext(chargeRequest.getOrderContext())
				.build(getBeanFactory());
	}
}
