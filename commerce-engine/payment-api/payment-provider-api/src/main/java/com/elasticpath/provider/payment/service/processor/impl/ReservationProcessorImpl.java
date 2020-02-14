/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.provider.payment.service.processor.impl;

import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.APPROVED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.FAILED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.SKIPPED;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.RESERVE_CAPABILITY_REQUEST;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequestFailedException;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityResponse;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ReserveCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ReserveCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ReserveCapabilityRequestBuilder;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.OrderContext;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIRequest;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIResponse;
import com.elasticpath.provider.payment.domain.transaction.reservation.ReserveRequest;
import com.elasticpath.provider.payment.domain.transaction.reservation.ReserveRequestBuilder;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigurationService;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.event.PaymentEventBuilder;
import com.elasticpath.provider.payment.service.history.PaymentHistory;
import com.elasticpath.provider.payment.service.history.util.MoneyDtoCalculator;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;
import com.elasticpath.provider.payment.service.processor.AbstractProcessor;
import com.elasticpath.provider.payment.service.processor.ReservationProcessor;
import com.elasticpath.provider.payment.service.provider.PaymentProviderService;
import com.elasticpath.provider.payment.workflow.PaymentAPIWorkflow;

/**
 * Default implementation of {@link ReservationProcessor}.
 */
public class ReservationProcessorImpl extends AbstractProcessor implements ReservationProcessor {

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
	public ReservationProcessorImpl(final PaymentProviderConfigurationService paymentProviderConfigurationService,
									final PaymentProviderService paymentProviderService,
									final PaymentHistory paymentHistory,
									final MoneyDtoCalculator moneyDtoCalculator,
									final PaymentAPIWorkflow paymentAPIWorkflow,
									final BeanFactory beanFactory) {
		super(paymentProviderConfigurationService, paymentProviderService, paymentHistory, moneyDtoCalculator, paymentAPIWorkflow, beanFactory);
	}

	@Override
	public PaymentAPIResponse reserve(final ReserveRequest reserveRequest) {
		final MoneyDTO amountAfterLimits = getAmountRemainingAfterLimits(reserveRequest);

		final List<PaymentEvent> paymentEvents;
		if (getMoneyDtoCalculator().hasBalance(reserveRequest.getAmount())) {
			paymentEvents = reserveRequest.getSelectedOrderPaymentInstruments().stream()
					.map(instrument -> {
						final MoneyDTO reservationAmount = getCorrectedReservationAmount(amountAfterLimits, instrument);
						if (getMoneyDtoCalculator().hasBalance(reservationAmount)) {
							return getPaymentProvider(instrument.getPaymentInstrument().getPaymentProviderConfigurationGuid())
									.getCapability(ReserveCapability.class)
									.map(capability -> Optional.of(executeCapability(capability, reserveRequest, instrument, reservationAmount)))
									.orElse(Optional.of(buildSkippedPaymentEvent(reserveRequest, instrument, reservationAmount)));
						}
						return Optional.<PaymentEvent>empty();
					})
					.filter(Optional::isPresent)
					.map(Optional::get)
					.collect(Collectors.toList());
		} else {
			paymentEvents = Collections.emptyList();
		}

		return createPaymentAPIResponse(paymentEvents, reserveRequest);
	}

	/**
	 * This method create PaymentApiResponse object with external and internal messages in case when something went wrong and without this messages
	 * if everything went fine.
	 *
	 * @param paymentEvents  list of payment events.
	 * @param reserveRequest the reservation request.
	 * @return PaymentAPIResponse object
	 */
	protected PaymentAPIResponse createPaymentAPIResponse(final List<PaymentEvent> paymentEvents, final ReserveRequest reserveRequest) {
		boolean isReserveSuccessful = isReserveSuccessful(reserveRequest, paymentEvents);

		return paymentEvents.stream()
				.filter(paymentEvent -> FAILED.equals(paymentEvent.getPaymentStatus()))
				.reduce((firstElement, lastElement) -> lastElement)
				.map(lastFailedPE -> new PaymentAPIResponse(paymentEvents,
						lastFailedPE.getExternalMessage(), lastFailedPE.getInternalMessage()))
				.orElse(new PaymentAPIResponse(paymentEvents, isReserveSuccessful));
	}

	@Override
	public PaymentAPIResponse reserveToSimulateModify(final MoneyDTO amount, final OrderPaymentInstrumentDTO paymentInstrument,
													  final Map<String, String> customRequestData, final OrderContext orderContext) {
		final ReserveRequest reserveRequest = ReserveRequestBuilder.builder()
				.withAmount(amount)
				.withSelectedOrderPaymentInstruments(Collections.singletonList(paymentInstrument))
				.withCustomRequestData(customRequestData)
				.withOrderContext(orderContext)
				.build(getBeanFactory());
		final PaymentEvent paymentEvent = getPaymentProvider(paymentInstrument.getPaymentInstrument().getPaymentProviderConfigurationGuid())
				.getCapability(ReserveCapability.class)
				.map(capability -> executeCapability(capability, reserveRequest, paymentInstrument, amount))
				.orElse(buildSkippedPaymentEvent(reserveRequest, paymentInstrument, amount));
		return createPaymentAPIResponse(Collections.singletonList(paymentEvent), reserveRequest);
	}

	/**
	 * Gets the total amount leftover from the limit amounts vs the requested amount.
	 *
	 * @param reserveRequest the reservation request
	 * @return corrected reservation amount
	 */
	protected MoneyDTO getAmountRemainingAfterLimits(final ReserveRequest reserveRequest) {
		final MoneyDtoCalculator calculator = getMoneyDtoCalculator();
		if (calculator.compare(calculator.zeroMoneyDto(), reserveRequest.getAmount()) == 0) { // free
			return reserveRequest.getAmount();
		}

		MoneyDTO limitsTotal = reserveRequest.getSelectedOrderPaymentInstruments().stream()
				.filter(OrderPaymentInstrumentDTO::hasLimit)
				.map(orderPaymentInstrumentDTO -> orderPaymentInstrumentDTO.getLimit().getAmount())
				.map(amount -> calculator.create(amount, reserveRequest.getAmount().getCurrencyCode()))
				.reduce(calculator.zeroMoneyDto(), calculator::plus);

		final MoneyDTO remainingAmount = calculator.minus(reserveRequest.getAmount(), limitsTotal);
		if (calculator.isNegative(remainingAmount)) {
			throw new IllegalStateException("Total limit for all instruments overflows the reservation request amount");
		}
		return remainingAmount;
	}

	/**
	 * Gets the corrected reservation amount, which is one of:
	 * <ol>
	 * <li>Instrument limit if it has one</li>
	 * <li>Remaining amount leftover from the limit amounts vs the originally requested amount if instrument is unlimited</li>
	 * </ol>
	 * <i>Note: there can be only one unlimited instrument.</i>
	 *
	 * @param remainingTotal total amount leftover from the limit amounts vs the requested amount
	 * @param instrument     limited or unlimited payment instrument
	 * @return corrected reservation amount
	 */
	protected MoneyDTO getCorrectedReservationAmount(final MoneyDTO remainingTotal, final OrderPaymentInstrumentDTO instrument) {
		final MoneyDTO reservationAmount = getMoneyDtoCalculator().cloneMoneyDto(remainingTotal);
		if (instrument.hasLimit()) {
			reservationAmount.setAmount(instrument.getLimit().getAmount());
		}
		return reservationAmount;
	}

	/**
	 * Executes the reservation capability producing success/failure payment event.
	 *
	 * @param capability        reservation plugin capability
	 * @param paymentAPIRequest reservation request
	 * @param instrument        order payment instrument
	 * @param amount            reservation amount
	 * @return payment event
	 */
	protected PaymentEvent executeCapability(final ReserveCapability capability,
											 final PaymentAPIRequest paymentAPIRequest,
											 final OrderPaymentInstrumentDTO instrument,
											 final MoneyDTO amount) {
		try {
			final ReserveCapabilityRequest request = createReserveCapabilityRequest(paymentAPIRequest, instrument, amount);
			final PaymentCapabilityResponse response = capability.reserve(request);
			return buildSucceededPaymentEvent(paymentAPIRequest, instrument, amount, response);
		} catch (PaymentCapabilityRequestFailedException ex) {
			return buildFailedPaymentEvent(paymentAPIRequest, instrument, amount, ex);
		}
	}

	/**
	 * Checks if reservation is considered to be fulfilled for specified ledger sequence.
	 *
	 * @param reserveRequest reservation request
	 * @param paymentEvents  ledger sequence
	 * @return true if reservation request is considered to be fulfilled
	 */
	protected boolean isReserveSuccessful(final ReserveRequest reserveRequest, final List<PaymentEvent> paymentEvents) {
		final MoneyDTO reservedAmount = getPaymentHistory().getAvailableReservedAmount(paymentEvents);
		return getMoneyDtoCalculator().compare(reserveRequest.getAmount(), reservedAmount) == 0;
	}

	/**
	 * Creates reservation plugin capability request.
	 *
	 * @param paymentAPIRequest original Payment API reservation request
	 * @param instrument        order payment instrument
	 * @param amount            actual reservation amount
	 * @return reservation plugin capability request
	 */
	protected ReserveCapabilityRequest createReserveCapabilityRequest(final PaymentAPIRequest paymentAPIRequest,
																	  final OrderPaymentInstrumentDTO instrument,
																	  final MoneyDTO amount) {
		return ReserveCapabilityRequestBuilder.builder()
				.withCustomRequestData(paymentAPIRequest.getCustomRequestData())
				.withAmount(amount)
				.withPaymentInstrumentData(instrument.getPaymentInstrument().getData())
				.withOrderContext(paymentAPIRequest.getOrderContext())
				.build(getBeanFactory().getPrototypeBean(RESERVE_CAPABILITY_REQUEST, ReserveCapabilityRequest.class));
	}

	/**
	 * Creates reservation failed event.
	 *
	 * @param paymentAPIRequest original Payment API reservation request
	 * @param instrument        order payment instrument
	 * @param reservationAmount actual reservation amount
	 * @param exception         plugin capability exception
	 * @return failed payment event
	 */
	protected PaymentEvent buildFailedPaymentEvent(final PaymentAPIRequest paymentAPIRequest,
												   final OrderPaymentInstrumentDTO instrument,
												   final MoneyDTO reservationAmount,
												   final PaymentCapabilityRequestFailedException exception) {
		final String guid = generateGuid();
		return PaymentEventBuilder.aPaymentEvent()
				.withGuid(guid)
				.withParentGuid(null)
				.withOrderPaymentInstrumentDTO(instrument)
				.withPaymentType(TransactionType.RESERVE)
				.withPaymentStatus(FAILED)
				.withReferenceId(paymentAPIRequest.getOrderContext().getOrderNumber())
				.withAmount(reservationAmount)
				.withOriginalPaymentInstrument(true)
				.withTemporaryFailure(exception.isTemporaryFailure())
				.withExternalMessage(exception.getExternalMessage())
				.withInternalMessage(exception.getInternalMessage())
				.build(getBeanFactory());
	}

	/**
	 * Creates skipped reservation event.
	 *
	 * @param paymentAPIRequest original Payment API reservation request
	 * @param instrument        order payment instrument
	 * @param reservationAmount actual reservation amount
	 * @return skipped reservation payment event
	 */
	protected PaymentEvent buildSkippedPaymentEvent(final PaymentAPIRequest paymentAPIRequest,
													final OrderPaymentInstrumentDTO instrument,
													final MoneyDTO reservationAmount) {
		final String guid = generateGuid();
		return PaymentEventBuilder.aPaymentEvent()
				.withGuid(guid)
				.withParentGuid(null)
				.withOrderPaymentInstrumentDTO(instrument)
				.withPaymentType(TransactionType.RESERVE)
				.withPaymentStatus(SKIPPED)
				.withReferenceId(paymentAPIRequest.getOrderContext().getOrderNumber())
				.withAmount(reservationAmount)
				.withOriginalPaymentInstrument(true)
				.build(getBeanFactory());
	}

	/**
	 * Creates successful reservation event.
	 *
	 * @param paymentAPIRequest original Payment API reservation request
	 * @param instrument        order payment instrument
	 * @param reservationAmount actual reservation amount
	 * @param response          plugin capability response
	 * @return approved reservation payment event
	 */
	protected PaymentEvent buildSucceededPaymentEvent(final PaymentAPIRequest paymentAPIRequest,
													  final OrderPaymentInstrumentDTO instrument,
													  final MoneyDTO reservationAmount,
													  final PaymentCapabilityResponse response) {
		final String guid = generateGuid();
		return PaymentEventBuilder.aPaymentEvent()
				.withGuid(guid)
				.withParentGuid(null)
				.withOrderPaymentInstrumentDTO(instrument)
				.withPaymentType(TransactionType.RESERVE)
				.withPaymentStatus(APPROVED)
				.withReferenceId(paymentAPIRequest.getOrderContext().getOrderNumber())
				.withAmount(reservationAmount)
				.withOriginalPaymentInstrument(true)
				.withPaymentEventData(response.getData())
				.build(getBeanFactory());
	}

	/**
	 * Generates guid.
	 *
	 * @return guid.
	 */
	protected String generateGuid() {
		return UUID.randomUUID().toString();
	}

}
