/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.service.processor.impl;

import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.APPROVED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.FAILED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.SKIPPED;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.CANCEL_RESERVE;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.CANCEL_CAPABILITY_REQUEST;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Multimap;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequestFailedException;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityResponse;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.CancelCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.CancelCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.CancelCapabilityRequestBuilder;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIRequest;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIResponse;
import com.elasticpath.provider.payment.domain.transaction.cancel.CancelAllReservationsRequest;
import com.elasticpath.provider.payment.domain.transaction.cancel.CancelReservationRequest;
import com.elasticpath.provider.payment.domain.transaction.cancel.CancelReservationRequestBuilder;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigurationService;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.event.PaymentEventBuilder;
import com.elasticpath.provider.payment.service.history.PaymentHistory;
import com.elasticpath.provider.payment.service.history.util.MoneyDtoCalculator;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.provider.payment.service.processor.AbstractProcessor;
import com.elasticpath.provider.payment.service.processor.CancelReservationProcessor;
import com.elasticpath.provider.payment.service.provider.PaymentProviderService;
import com.elasticpath.provider.payment.workflow.PaymentAPIWorkflow;

/**
 * Default implementation of {@link CancelReservationProcessor}.
 */
public class CancelReservationProcessorImpl extends AbstractProcessor implements CancelReservationProcessor {

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
	public CancelReservationProcessorImpl(final PaymentProviderConfigurationService paymentProviderConfigurationService,
										  final PaymentProviderService paymentProviderService,
										  final PaymentHistory paymentHistory,
										  final MoneyDtoCalculator moneyDtoCalculator,
										  final PaymentAPIWorkflow paymentAPIWorkflow,
										  final BeanFactory beanFactory) {
		super(paymentProviderConfigurationService, paymentProviderService, paymentHistory, moneyDtoCalculator, paymentAPIWorkflow, beanFactory);
	}

	@Override
	public PaymentAPIResponse cancelReservation(final CancelReservationRequest cancelRequest) {
		final List<PaymentEvent> paymentEvents = cancelRequest.getSelectedPaymentEventsToCancel()
				.stream()
				.map(reservationEvent -> getPaymentProvider(reservationEvent.getOrderPaymentInstrumentDTO()
						.getPaymentInstrument().getPaymentProviderConfigurationGuid())
						.getCapability(CancelCapability.class)
						.map(cancelCapability -> executeCapability(cancelCapability, cancelRequest, reservationEvent))
						.orElse(buildSkippedCancelPaymentEvent(cancelRequest, reservationEvent)))
				.collect(Collectors.toList());

		return createPaymentAPIResponse(paymentEvents, cancelRequest);
	}

	/**
	 * This method create PaymentApiResponse object with external and internal messages in case when something went wrong and without this messages
	 * if everything went fine.
	 *
	 * @param paymentEvents list of payment events.
	 * @param cancelRequest cancel reservation request.
	 * @return PaymentAPIResponse object
	 */
	protected PaymentAPIResponse createPaymentAPIResponse(final List<PaymentEvent> paymentEvents, final CancelReservationRequest cancelRequest) {
		final List<PaymentEvent> ledger = cancelRequest.getLedger();
		final MoneyDTO requestedAmount = cancelRequest.getAmount();
		final MoneyDTO amountBeforeCancel = getPaymentHistory().getAvailableReservedAmount(ledger);
		final List<PaymentEvent> newLedger = new ArrayList<>();
		newLedger.addAll(ledger);
		newLedger.addAll(paymentEvents);
		boolean isCancelSuccessful = isCancelSuccessful(requestedAmount, amountBeforeCancel, newLedger);

		if (paymentEvents.isEmpty() && !isCancelSuccessful) {
			return new PaymentAPIResponse(paymentEvents,
					"The system could not cancel the reservation.",
					"The cancel reservation process failed due to no cancelable payment events.");
		} else {
			return new PaymentAPIResponse(paymentEvents, isCancelSuccessful);
		}
	}

	@Override
	public PaymentAPIResponse cancelAllReservations(final CancelAllReservationsRequest request) {
		final Multimap<PaymentEvent, MoneyDTO> chargeablePaymentEvents = getPaymentHistory().getChargeablePaymentEvents(request.getLedger());
		final List<PaymentEvent> reservationEvents = new ArrayList<>(chargeablePaymentEvents.keys());
		final MoneyDTO availableReservedAmount = getPaymentHistory().getAvailableReservedAmount(request.getLedger());
		final CancelReservationRequest cancelRequest = createCancelReservationRequest(request, reservationEvents, availableReservedAmount);
		return getPaymentAPIWorkflow().cancelReservation(cancelRequest);
	}

	/**
	 * Create Payment API cancellation request from {@link CancelAllReservationsRequest}.
	 *
	 * @param request                 original "cancel all reservations" request
	 * @param reservationEvents       reservation payment events to cancel
	 * @param availableReservedAmount total available reserved amount
	 * @return Payment API cancellation request
	 */
	protected CancelReservationRequest createCancelReservationRequest(final CancelAllReservationsRequest request,
																	  final List<PaymentEvent> reservationEvents,
																	  final MoneyDTO availableReservedAmount) {
		return CancelReservationRequestBuilder.builder()
				.withOrderPaymentInstruments(request.getOrderPaymentInstruments())
				.withAmount(availableReservedAmount)
				.withSelectedPaymentEventsToCancel(reservationEvents)
				.withCustomRequestData(request.getCustomRequestData())
				.withLedger(request.getLedger())
				.withOrderContext(request.getOrderContext())
				.build(getBeanFactory());
	}

	/**
	 * Executes the cancel reservation capability producing success/failure payment event.
	 *
	 * @param cancelCapability  cancel reservation plugin capability
	 * @param paymentAPIRequest cancel reservation request
	 * @param reservationEvent  existing reservation payment event
	 * @return payment event
	 */
	protected PaymentEvent executeCapability(final CancelCapability cancelCapability,
											 final PaymentAPIRequest paymentAPIRequest,
											 final PaymentEvent reservationEvent) {
		try {
			final CancelCapabilityRequest cancelCapabilityRequest = createCancelCapabilityRequest(reservationEvent, paymentAPIRequest);
			final PaymentCapabilityResponse response = cancelCapability.cancel(cancelCapabilityRequest);
			return buildSucceededCancelPaymentEvent(paymentAPIRequest, reservationEvent, response);
		} catch (PaymentCapabilityRequestFailedException exception) {
			return buildFailedCancelPaymentEvent(paymentAPIRequest, reservationEvent, exception);
		}
	}

	/**
	 * Creates cancel reservation plugin capability request.
	 *
	 * @param reservationEvent  existing reservation payment event
	 * @param paymentAPIRequest cancel reservation request
	 * @return cancel reservation plugin capability request
	 */
	protected CancelCapabilityRequest createCancelCapabilityRequest(final PaymentEvent reservationEvent,
																	final PaymentAPIRequest paymentAPIRequest) {
		final OrderPaymentInstrumentDTO orderPaymentInstrumentDTO = reservationEvent.getOrderPaymentInstrumentDTO();
		final PaymentInstrumentDTO paymentInstrumentDTO = orderPaymentInstrumentDTO.getPaymentInstrument();
		return CancelCapabilityRequestBuilder.builder()
				.withAmount(reservationEvent.getAmount())
				.withPaymentInstrumentData(paymentInstrumentDTO.getData())
				.withCustomRequestData(paymentAPIRequest.getCustomRequestData())
				.withReservationData(reservationEvent.getPaymentEventData())
				.withOrderContext(paymentAPIRequest.getOrderContext())
				.build(getBeanFactory().getPrototypeBean(CANCEL_CAPABILITY_REQUEST, CancelCapabilityRequest.class));
	}

	/**
	 * Creates successful cancellation event.
	 *
	 * @param paymentAPIRequest original Payment API cancellation request
	 * @param reservationEvent  existing reservation payment event
	 * @param response          plugin capability response
	 * @return approved cancellation payment event
	 */
	protected PaymentEvent buildSucceededCancelPaymentEvent(final PaymentAPIRequest paymentAPIRequest,
															final PaymentEvent reservationEvent,
															final PaymentCapabilityResponse response) {

		return PaymentEventBuilder.aPaymentEvent()
				.withParentGuid(reservationEvent.getGuid())
				.withOrderPaymentInstrumentDTO(reservationEvent.getOrderPaymentInstrumentDTO())
				.withPaymentType(CANCEL_RESERVE)
				.withPaymentStatus(APPROVED)
				.withReferenceId(paymentAPIRequest.getOrderContext().getOrderNumber())
				.withAmount(reservationEvent.getAmount())
				.withOriginalPaymentInstrument(true)
				.withPaymentEventData(response.getData())
				.build(getBeanFactory());
	}

	/**
	 * Creates cancellation failed event.
	 *
	 * @param paymentAPIRequest original Payment API cancellation request
	 * @param reservationEvent  existing reservation payment event
	 * @param exception         plugin capability exception
	 * @return failed payment event
	 */
	protected PaymentEvent buildFailedCancelPaymentEvent(final PaymentAPIRequest paymentAPIRequest,
														 final PaymentEvent reservationEvent,
														 final PaymentCapabilityRequestFailedException exception) {
		return PaymentEventBuilder.aPaymentEvent()
				.withParentGuid(reservationEvent.getGuid())
				.withOrderPaymentInstrumentDTO(reservationEvent.getOrderPaymentInstrumentDTO())
				.withPaymentType(CANCEL_RESERVE)
				.withPaymentStatus(FAILED)
				.withReferenceId(paymentAPIRequest.getOrderContext().getOrderNumber())
				.withAmount(reservationEvent.getAmount())
				.withOriginalPaymentInstrument(true)
				.withTemporaryFailure(exception.isTemporaryFailure())
				.withExternalMessage(exception.getExternalMessage())
				.withInternalMessage(exception.getInternalMessage())
				.build(getBeanFactory());
	}

	/**
	 * Creates skipped cancellation event.
	 *
	 * @param paymentAPIRequest original Payment API cancellation request
	 * @param reservationEvent  existing reservation payment event
	 * @return skipped cancellation payment event
	 */
	protected PaymentEvent buildSkippedCancelPaymentEvent(final PaymentAPIRequest paymentAPIRequest,
														  final PaymentEvent reservationEvent) {
		return PaymentEventBuilder.aPaymentEvent()
				.withParentGuid(reservationEvent.getGuid())
				.withOrderPaymentInstrumentDTO(reservationEvent.getOrderPaymentInstrumentDTO())
				.withPaymentType(CANCEL_RESERVE)
				.withPaymentStatus(SKIPPED)
				.withReferenceId(paymentAPIRequest.getOrderContext().getOrderNumber())
				.withAmount(reservationEvent.getAmount())
				.withOriginalPaymentInstrument(true)
				.build(getBeanFactory());
	}

	/**
	 * Checks if cancellation is considered to be fulfilled for specified ledger sequence.
	 *
	 * @param cancelledAmount    requested amount to cancel
	 * @param amountBeforeCancel total reserved amount before execution
	 * @param ledger             ledger sequence
	 * @return true if cancellation request is considered to be fulfilled
	 */
	protected boolean isCancelSuccessful(final MoneyDTO cancelledAmount, final MoneyDTO amountBeforeCancel, final List<PaymentEvent> ledger) {
		final MoneyDtoCalculator moneyDtoCalculator = getMoneyDtoCalculator();
		final MoneyDTO amountAfterCancel = getPaymentHistory().getAvailableReservedAmount(ledger);
		final MoneyDTO amountExpected = moneyDtoCalculator.minus(amountBeforeCancel, cancelledAmount);
		return moneyDtoCalculator.compare(amountAfterCancel, amountExpected) == 0;
	}

}
