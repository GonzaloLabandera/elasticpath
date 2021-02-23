/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.service.processor.impl;

import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.APPROVED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.SKIPPED;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.MODIFY_CAPABILITY_REQUEST;
import static java.util.Collections.emptyList;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequestFailedException;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityResponse;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.CancelCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ModifyCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ModifyCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ModifyCapabilityRequestBuilder;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.provider.payment.domain.PaymentProvider;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIRequest;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIResponse;
import com.elasticpath.provider.payment.domain.transaction.cancel.CancelReservationRequest;
import com.elasticpath.provider.payment.domain.transaction.cancel.CancelReservationRequestBuilder;
import com.elasticpath.provider.payment.domain.transaction.modify.ModifyReservationRequest;
import com.elasticpath.provider.payment.domain.transaction.reservation.ReserveRequest;
import com.elasticpath.provider.payment.domain.transaction.reservation.ReserveRequestBuilder;
import com.elasticpath.provider.payment.service.PaymentsException;
import com.elasticpath.provider.payment.service.PaymentsExceptionMessageId;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigurationService;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.event.PaymentEventBuilder;
import com.elasticpath.provider.payment.service.history.PaymentHistory;
import com.elasticpath.provider.payment.service.history.util.MoneyDtoCalculator;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;
import com.elasticpath.provider.payment.service.processor.AbstractProcessor;
import com.elasticpath.provider.payment.service.processor.ModifyReservationProcessor;
import com.elasticpath.provider.payment.service.processor.ReservationProcessor;
import com.elasticpath.provider.payment.service.provider.PaymentProviderService;
import com.elasticpath.provider.payment.workflow.PaymentAPIWorkflow;

/**
 * Default implementation of {@link ModifyReservationProcessor}.
 */
public class ModifyReservationProcessorImpl extends AbstractProcessor implements ModifyReservationProcessor {

	private static final String PAYMENT_EXCEPTION_REASON = "reason";
	/**
	 * ReservationProcessor bean, only to be directly used when modifying reservation in absence of modify capability.
	 */
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
	public ModifyReservationProcessorImpl(final PaymentProviderConfigurationService paymentProviderConfigurationService,
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
	public PaymentAPIResponse modifyReservation(final ModifyReservationRequest modifyRequest) {
		final Multimap<PaymentEvent, MoneyDTO> chargeablePaymentEvents = getPaymentHistory().getChargeablePaymentEvents(modifyRequest.getLedger());
		final MoneyDTO availableReservedAmount = getPaymentHistory().getAvailableReservedAmount(modifyRequest.getLedger());
		final MoneyDTO chargedAmount = getPaymentHistory().getChargedAmount(modifyRequest.getLedger());
		final MoneyDTO orderAmount = getMoneyDtoCalculator().plus(availableReservedAmount, chargedAmount);
		final MoneyDTO differenceAmount = getMoneyDtoCalculator().minus(modifyRequest.getAmount(), orderAmount);

		if (chargeablePaymentEvents.isEmpty()) {
			final Map<OrderPaymentInstrumentDTO, MoneyDTO> reservableOrderPaymentInstruments = getPaymentHistory()
					.getReservableOrderPaymentInstruments(modifyRequest.getLedger(), modifyRequest.getOrderPaymentInstruments());
			final ReserveRequest reserveRequest =
					createReserveRequest(new ArrayList<>(reservableOrderPaymentInstruments.keySet()), differenceAmount, modifyRequest);

			return getPaymentAPIWorkflow().reserve(reserveRequest);
		}

		if (getMoneyDtoCalculator().isPositive(differenceAmount)) {
			return increaseAmountForUnlimitedInstrument(modifyRequest, chargeablePaymentEvents, differenceAmount);
		} else if (getMoneyDtoCalculator().isNegative(differenceAmount)) {
			return decreaseAmountOverAllInstruments(modifyRequest, chargeablePaymentEvents, differenceAmount);
		}
		return new PaymentAPIResponse(emptyList(), true);
	}

	/**
	 * Increases the amount reserved for unlimited instrument.
	 *
	 * @param modifyRequest     original Payment API modification request
	 * @param reservationEvents ledger of reservation events
	 * @param amount            new amount
	 * @return Payment API response with the new ledger
	 */
	protected PaymentAPIResponse increaseAmountForUnlimitedInstrument(final ModifyReservationRequest modifyRequest,
																	  final Multimap<PaymentEvent, MoneyDTO> reservationEvents,
																	  final MoneyDTO amount) {
		if (modifyRequest.hasSingleReservePerPI()) {
			return new PaymentAPIResponse(emptyList(),
					"The system could not reserve funds.",
					"The reserve process cannot continue because increase amount is impossible.");
		}

		for (Map.Entry<PaymentEvent, MoneyDTO> paymentInstrumentInfo : reservationEvents.entries()) {
			final PaymentEvent reservationPaymentEvent = paymentInstrumentInfo.getKey();
			final OrderPaymentInstrumentDTO instrumentDTO = reservationPaymentEvent.getOrderPaymentInstrumentDTO();
			if (instrumentDTO.getLimit() == null || instrumentDTO.getLimit().getAmount().compareTo(BigDecimal.ZERO) == 0) {
				final MoneyDTO newAmount = getMoneyDtoCalculator().plus(paymentInstrumentInfo.getValue(), amount);
				return modifyReservation(modifyRequest, ImmutableMap.of(reservationPaymentEvent, newAmount));
			}
		}
		throw new PaymentsException(PaymentsExceptionMessageId.PAYMENT_FAILED,
				ImmutableMap.of(PAYMENT_EXCEPTION_REASON, "Unlimited payment instrument not found"));
	}

	/**
	 * Decreases the amount reserved over all instruments.
	 *
	 * @param modifyRequest     original Payment API modification request
	 * @param reservationEvents ledger of reservation events
	 * @param amount            new amount
	 * @return Payment API response with the new ledger
	 */
	protected PaymentAPIResponse decreaseAmountOverAllInstruments(final ModifyReservationRequest modifyRequest,
																  final Multimap<PaymentEvent, MoneyDTO> reservationEvents,
																  final MoneyDTO amount) {

		if (modifyRequest.hasSingleReservePerPI() && !modifyRequest.isFinalPayment()) {
			return new PaymentAPIResponse(emptyList(), true);
		}

		final MoneyDTO amountToDecrease = getMoneyDtoCalculator().abs(amount);
		Map<PaymentEvent, MoneyDTO> modifiedEvents = new HashMap<>();
		for (Map.Entry<PaymentEvent, MoneyDTO> paymentInstrumentInfo : reservationEvents.entries()) {
			if (!getMoneyDtoCalculator().hasBalance(amountToDecrease)) {
				return modifyReservation(modifyRequest, modifiedEvents);
			}
			final MoneyDTO reservationAmount = paymentInstrumentInfo.getValue();
			final MoneyDTO newAmount = getMoneyDtoCalculator().minus(amountToDecrease, reservationAmount);
			if (getMoneyDtoCalculator().isPositive(newAmount)) {
				modifiedEvents.put(paymentInstrumentInfo.getKey(), getMoneyDtoCalculator().zeroMoneyDto());
				getMoneyDtoCalculator().decrease(amountToDecrease, reservationAmount);
			} else {
				modifiedEvents.put(paymentInstrumentInfo.getKey(), getMoneyDtoCalculator().abs(newAmount));
				getMoneyDtoCalculator().resetToZero(amountToDecrease);
			}
		}
		if (getMoneyDtoCalculator().hasBalance(amountToDecrease)) {
			throw new PaymentsException(PaymentsExceptionMessageId.PAYMENT_FAILED,
					ImmutableMap.of(PAYMENT_EXCEPTION_REASON, "Not enough reservation events found"));
		}
		return modifyReservation(modifyRequest, modifiedEvents);
	}

	/**
	 * Modifies the reservation amount.
	 *
	 * @param modifyRequest         original Payment API modification request
	 * @param modifiedPaymentEvents map of reservation events to their new amounts
	 * @return Payment API response with the new ledger
	 */
	protected PaymentAPIResponse modifyReservation(final ModifyReservationRequest modifyRequest,
												   final Map<PaymentEvent, MoneyDTO> modifiedPaymentEvents) {
		final List<PaymentEvent> paymentEvents = new ArrayList<>();
		for (Map.Entry<PaymentEvent, MoneyDTO> paymentInstrumentInfo : modifiedPaymentEvents.entrySet()) {
			final PaymentEvent reservationEvent = paymentInstrumentInfo.getKey();
			final MoneyDTO newAmount = paymentInstrumentInfo.getValue();
			final OrderPaymentInstrumentDTO instrumentDTO = reservationEvent.getOrderPaymentInstrumentDTO();
			final PaymentProvider paymentProvider = getPaymentProvider(instrumentDTO.getPaymentInstrument().getPaymentProviderConfigurationGuid());
			if (getMoneyDtoCalculator().hasBalance(newAmount)) {
				if (getMoneyDtoCalculator().isPositive(newAmount)) {
					paymentEvents.addAll(paymentProvider.getCapability(ModifyCapability.class)
							.map(modifyCapability -> executeCapability(modifyCapability, modifyRequest, reservationEvent, newAmount))
							.orElseGet(() -> simulateCapability(paymentProvider, modifyRequest, reservationEvent, newAmount)));
				} else {
					throw new PaymentsException(PaymentsExceptionMessageId.PAYMENT_FAILED,
							ImmutableMap.of(
									PAYMENT_EXCEPTION_REASON, "Attempting to use negative value for reservation",
									"amount", newAmount.getAmount().toString(),
									"currency", newAmount.getCurrencyCode()));
				}
			} else {
				final CancelReservationRequest cancelReservationRequest = createCancelReservationRequest(modifyRequest, reservationEvent);
				final PaymentAPIResponse cancelReservationResponse = getPaymentAPIWorkflow().cancelReservation(cancelReservationRequest);
				paymentEvents.addAll(cancelReservationResponse.getEvents());
			}
		}

		return createPaymentAPIResponse(paymentEvents, modifyRequest);
	}

	/**
	 * This method create PaymentApiResponse object with external and internal messages in case when something went wrong and without this messages
	 * if everything went fine.
	 *
	 * @param paymentEvents list of payment events.
	 * @param modifyRequest the modification of reservation request.
	 * @return PaymentAPIResponse object
	 */
	protected PaymentAPIResponse createPaymentAPIResponse(final List<PaymentEvent> paymentEvents, final ModifyReservationRequest modifyRequest) {
		final List<PaymentEvent> ledger = modifyRequest.getLedger();
		final List<PaymentEvent> newLedger = new ArrayList<>();
		newLedger.addAll(ledger);
		newLedger.addAll(paymentEvents);

		boolean isModifyReservationSuccessful = isModifyReservationSuccessful(modifyRequest, newLedger);
		if (isModifyReservationSuccessful) {
			return new PaymentAPIResponse(paymentEvents, true);
		} else {
			return new PaymentAPIResponse(paymentEvents,
					"Increase amount modification failed.",
					"Elastic Path attempted to replace the existing reservation with a new, higher amount, but was unsuccessful.\n"
							+ "Therefore, the order modification cannot be completed.");
		}
	}

	/**
	 * Executes modify reservation plugin capability.
	 *
	 * @param modifyCapability  modify reservation plugin capability
	 * @param paymentAPIRequest original Payment API modification request
	 * @param reservationEvent  existing reservation payment event
	 * @param newAmount         new reserved amount
	 * @return the new ledger with "reservation modified" events
	 */
	protected List<PaymentEvent> executeCapability(final ModifyCapability modifyCapability,
												   final PaymentAPIRequest paymentAPIRequest,
												   final PaymentEvent reservationEvent,
												   final MoneyDTO newAmount) {
		final ModifyCapabilityRequest modifyCapabilityRequest = createModifyCapabilityRequest(paymentAPIRequest, reservationEvent, newAmount);
		try {
			final PaymentCapabilityResponse response = modifyCapability.modify(modifyCapabilityRequest);
			return ImmutableList.of(buildSucceededPaymentEvent(modifyCapabilityRequest, reservationEvent, response));
		} catch (PaymentCapabilityRequestFailedException ex) {
			if (getMoneyDtoCalculator().compare(newAmount, reservationEvent.getAmount()) >= 0) {
				return emptyList();
			}
			return ImmutableList.of(buildSkippedPaymentEvent(modifyCapabilityRequest, reservationEvent, ex));
		}
	}

	/**
	 * Simulates modify reservation plugin capability by using cancel and reserve capabilities.
	 *
	 * @param paymentProvider  payment provider
	 * @param modifyRequest    original Payment API modification request
	 * @param reservationEvent existing reservation payment event
	 * @param newAmount        new reserved amount
	 * @return the new ledger with "reserved" and "cancelled" events
	 */
	protected List<PaymentEvent> simulateCapability(final PaymentProvider paymentProvider,
													final ModifyReservationRequest modifyRequest,
													final PaymentEvent reservationEvent,
													final MoneyDTO newAmount) {
		final OrderPaymentInstrumentDTO instrumentDTO = reservationEvent.getOrderPaymentInstrumentDTO();
		final MoneyDTO deltaAmount = getMoneyDtoCalculator().minus(newAmount, reservationEvent.getAmount());
		if (getMoneyDtoCalculator().isPositive(deltaAmount)) {
			final ReserveRequest reservationRequest = createReservationRequest(instrumentDTO, deltaAmount, modifyRequest);
			return getPaymentAPIWorkflow().reserve(reservationRequest).getEvents();
		} else {
			final List<PaymentEvent> paymentEvents = new ArrayList<>();
			if (paymentProvider.getCapability(CancelCapability.class).isPresent()) {
				final PaymentAPIResponse reservationResponse = reservationProcessor.reserveToSimulateModify(newAmount, instrumentDTO,
						modifyRequest.getCustomRequestData(), modifyRequest.getOrderContext());
				paymentEvents.addAll(reservationResponse.getEvents());
				if (reservationResponse.isSuccess()) {
					final CancelReservationRequest cancelRequest = createCancelReservationRequest(modifyRequest, reservationEvent);
					final PaymentAPIResponse cancelResponse = getPaymentAPIWorkflow().cancelReservation(cancelRequest);
					paymentEvents.addAll(cancelResponse.getEvents());
				}
			}
			return paymentEvents;
		}
	}

	/**
	 * Checks if modification is considered to be fulfilled for specified ledger sequence.
	 *
	 * @param modifyRequest modification request
	 * @param paymentEvents ledger sequence
	 * @return true if modification request is considered to be fulfilled
	 */
	protected boolean isModifyReservationSuccessful(final ModifyReservationRequest modifyRequest, final List<PaymentEvent> paymentEvents) {
		final MoneyDTO availableReservedAmount = getPaymentHistory().getAvailableReservedAmount(paymentEvents);
		final MoneyDTO chargedAmount = getPaymentHistory().getChargedAmount(paymentEvents);
		final MoneyDTO orderAmount = getMoneyDtoCalculator().plus(availableReservedAmount, chargedAmount);
		final MoneyDTO refundedAmount = getPaymentHistory().getRefundedAmount(paymentEvents);
		final MoneyDTO originalOrderAmount = getMoneyDtoCalculator().plus(orderAmount, refundedAmount);
		return getMoneyDtoCalculator().compare(modifyRequest.getAmount(), originalOrderAmount) <= 0;
	}

	/**
	 * Creates Payment API reservation request.
	 *
	 * @param instrument        order payment instrument
	 * @param amount            amount to reserve
	 * @param paymentAPIRequest original Payment API modification request
	 * @return Payment API reservation request
	 */
	protected ReserveRequest createReservationRequest(final OrderPaymentInstrumentDTO instrument,
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
	 * Creates Payment API cancel reservation request.
	 *
	 * @param modifyRequest    original Payment API modification request
	 * @param reservationEvent existing reservation payment event to cancel
	 * @return Payment API cancel reservation request
	 */
	protected CancelReservationRequest createCancelReservationRequest(final ModifyReservationRequest modifyRequest,
																	  final PaymentEvent reservationEvent) {
		return CancelReservationRequestBuilder.builder()
				.withSelectedPaymentEventsToCancel(Collections.singletonList(reservationEvent))
				.withOrderPaymentInstruments(modifyRequest.getOrderPaymentInstruments())
				.withLedger(Collections.singletonList(reservationEvent))
				.withCustomRequestData(modifyRequest.getCustomRequestData())
				.withAmount(reservationEvent.getAmount())
				.withOrderContext(modifyRequest.getOrderContext())
				.build(getBeanFactory());
	}

	/**
	 * Creates modify reservation plugin capability request.
	 *
	 * @param paymentAPIRequest original Payment API modification request
	 * @param reservationEvent  existing reservation payment event to modify
	 * @param newAmount         new reserved amount
	 * @return modify reservation plugin capability request
	 */
	protected ModifyCapabilityRequest createModifyCapabilityRequest(final PaymentAPIRequest paymentAPIRequest,
																	final PaymentEvent reservationEvent,
																	final MoneyDTO newAmount) {
		return ModifyCapabilityRequestBuilder.builder()
				.withAmount(newAmount)
				.withPaymentInstrumentData(reservationEvent.getOrderPaymentInstrumentDTO().getPaymentInstrument().getData())
				.withCustomRequestData(paymentAPIRequest.getCustomRequestData())
				.withOrderContext(paymentAPIRequest.getOrderContext())
				.withReservationData(reservationEvent.getPaymentEventData())
				.build(getBeanFactory().getPrototypeBean(MODIFY_CAPABILITY_REQUEST, ModifyCapabilityRequest.class));
	}

	/**
	 * Creates skipped modification event.
	 *
	 * @param modifyRequest    original Payment API modification request
	 * @param reservationEvent existing reservation payment event we attempted to modify
	 * @param exception        plugin capability exception
	 * @return skipped modification payment event
	 */
	protected PaymentEvent buildSkippedPaymentEvent(final ModifyCapabilityRequest modifyRequest,
													final PaymentEvent reservationEvent,
													final PaymentCapabilityRequestFailedException exception) {
		final OrderPaymentInstrumentDTO instrument = reservationEvent.getOrderPaymentInstrumentDTO();
		return PaymentEventBuilder.aPaymentEvent()
				.withParentGuid(reservationEvent.getGuid())
				.withOrderPaymentInstrumentDTO(instrument)
				.withPaymentType(TransactionType.MODIFY_RESERVE)
				.withPaymentStatus(SKIPPED)
				.withReferenceId(modifyRequest.getOrderContext().getOrderNumber())
				.withAmount(modifyRequest.getAmount())
				.withOriginalPaymentInstrument(true)
				.withTemporaryFailure(exception.isTemporaryFailure())
				.withExternalMessage(exception.getExternalMessage())
				.withInternalMessage(exception.getInternalMessage())
				.build(getBeanFactory());
	}

	/**
	 * Creates successful modification event.
	 *
	 * @param modifyRequest    original Payment API modification request
	 * @param reservationEvent existing reservation payment event we modified
	 * @param response         plugin capability response
	 * @return approved modification payment event
	 */
	protected PaymentEvent buildSucceededPaymentEvent(final ModifyCapabilityRequest modifyRequest,
													  final PaymentEvent reservationEvent,
													  final PaymentCapabilityResponse response) {
		final OrderPaymentInstrumentDTO instrument = reservationEvent.getOrderPaymentInstrumentDTO();
		return PaymentEventBuilder.aPaymentEvent()
				.withParentGuid(reservationEvent.getGuid())
				.withOrderPaymentInstrumentDTO(instrument)
				.withPaymentType(TransactionType.MODIFY_RESERVE)
				.withPaymentStatus(APPROVED)
				.withReferenceId(modifyRequest.getOrderContext().getOrderNumber())
				.withAmount(modifyRequest.getAmount())
				.withOriginalPaymentInstrument(true)
				.withPaymentEventData(response.getData())
				.build(getBeanFactory());
	}

	/**
	 * Create Payment API reservation request.
	 *
	 * @param instruments       list of order payment instruments
	 * @param amount            amount to be reserved
	 * @param paymentAPIRequest original payment API request
	 * @return Payment API reservation request
	 */
	protected ReserveRequest createReserveRequest(final List<OrderPaymentInstrumentDTO> instruments,
												  final MoneyDTO amount,
												  final PaymentAPIRequest paymentAPIRequest) {
		return ReserveRequestBuilder.builder()
				.withSelectedOrderPaymentInstruments(instruments)
				.withAmount(amount)
				.withOrderContext(paymentAPIRequest.getOrderContext())
				.withCustomRequestData(paymentAPIRequest.getCustomRequestData())
				.build(getBeanFactory());
	}

}
