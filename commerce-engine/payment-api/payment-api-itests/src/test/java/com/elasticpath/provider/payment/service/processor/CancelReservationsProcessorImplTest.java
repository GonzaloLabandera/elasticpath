/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.service.processor;

import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.APPROVED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.FAILED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.SKIPPED;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.CANCEL_RESERVE;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Test;

import com.elasticpath.plugin.payment.provider.capabilities.reservation.CancelCapability;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIResponse;
import com.elasticpath.provider.payment.domain.transaction.cancel.CancelReservationRequest;
import com.elasticpath.provider.payment.domain.transaction.cancel.CancelReservationRequestBuilder;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.service.payment.provider.PaymentProviderPluginForIntegrationTesting;

public class CancelReservationsProcessorImplTest extends AbstractProcessorImplTestBase {

	@Inject
	@Named(PaymentProviderApiContextIdNames.CANCEL_RESERVATION_PROCESSOR)
	private CancelReservationProcessor testee;

	@Test
	public void cancelAllReservationsWithTwoReserveEvent() {
		PaymentEvent firstReservationEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), DEFAULT_RESERVED_AMOUNT_10_USD);
		PaymentEvent secondReservationEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), DEFAULT_RESERVED_AMOUNT_10_USD);
		List<PaymentEvent> paymentEventList = Arrays.asList(firstReservationEvent, secondReservationEvent);
		final PaymentAPIResponse response = testee.cancelAllReservations(createCancelAllReservationsRequest(paymentEventList));

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(TEN, TEN);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(CANCEL_RESERVE, CANCEL_RESERVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED, APPROVED);
	}

	@Test
	public void cancelAllReservationsWithTwoReserveEventAndOneChargeEvent() {
		PaymentEvent firstReservationEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), DEFAULT_RESERVED_AMOUNT_10_USD);
		PaymentEvent secondReservationEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), DEFAULT_RESERVED_AMOUNT_10_USD);
		PaymentEvent chargeEvent = createChargeEvent(firstReservationEvent, DEFAULT_RESERVED_AMOUNT_10_USD);
		List<PaymentEvent> paymentEventList = Arrays.asList(firstReservationEvent, chargeEvent, secondReservationEvent);
		final PaymentAPIResponse response = testee.cancelAllReservations(createCancelAllReservationsRequest(paymentEventList));

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(TEN);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(CANCEL_RESERVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED);
	}

	@Test
	public void cancelAllReservationsWithOneReserveEventAndOneModifyEvent() {
		PaymentEvent firstReservationEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), DEFAULT_RESERVED_AMOUNT_10_USD);
		PaymentEvent modifyReservationEvent = createModifyReservationEvent(firstReservationEvent, DEFAULT_MODIFIED_AMOUNT_20_USD);
		List<PaymentEvent> paymentEventList = Arrays.asList(firstReservationEvent, modifyReservationEvent);
		final PaymentAPIResponse response = testee.cancelAllReservations(createCancelAllReservationsRequest(paymentEventList));

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(TWENTY);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(CANCEL_RESERVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED);
	}

	@Test
	public void cancelAllReservationsWithReserveEventPlusChargeEventAndReserveEventPlusModifyEvent() {
		PaymentEvent firstReservationEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), DEFAULT_RESERVED_AMOUNT_10_USD);
		PaymentEvent secondReservationEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), DEFAULT_RESERVED_AMOUNT_10_USD);
		PaymentEvent chargeEvent = createChargeEvent(firstReservationEvent, DEFAULT_RESERVED_AMOUNT_10_USD);
		PaymentEvent modifyReservationEvent = createModifyReservationEvent(secondReservationEvent, DEFAULT_MODIFIED_AMOUNT_20_USD);
		List<PaymentEvent> paymentEventList = Arrays.asList(firstReservationEvent, secondReservationEvent, chargeEvent, modifyReservationEvent);
		final PaymentAPIResponse response = testee.cancelAllReservations(createCancelAllReservationsRequest(paymentEventList));

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(TWENTY);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(CANCEL_RESERVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED);
	}

	@Test
	public void cancelAllReservationsWithOneReserveEventPlusChargeEventAndCreditEvent() {
		PaymentEvent firstReservationEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), DEFAULT_RESERVED_AMOUNT_10_USD);
		PaymentEvent chargeEvent = createChargeEvent(firstReservationEvent, DEFAULT_RESERVED_AMOUNT_10_USD);
		PaymentEvent creditEvent = createCreditEvent(chargeEvent, DEFAULT_RESERVED_AMOUNT_10_USD);
		List<PaymentEvent> paymentEventList = Arrays.asList(firstReservationEvent, chargeEvent, creditEvent);
		final PaymentAPIResponse response = testee.cancelAllReservations(createCancelAllReservationsRequest(paymentEventList));

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents()).isEmpty();
	}

	@Test
	public void cancelAllReservationsWithReserveEventPlusChargeEventAndReverseChargeEvent() {
		PaymentEvent firstReservationEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), DEFAULT_RESERVED_AMOUNT_10_USD);
		PaymentEvent chargeEvent = createChargeEvent(firstReservationEvent, DEFAULT_RESERVED_AMOUNT_10_USD);
		PaymentEvent reverseChargeEvent = createReverseChargeEvent(chargeEvent);
		List<PaymentEvent> paymentEventList = Arrays.asList(firstReservationEvent, chargeEvent, reverseChargeEvent);
		final PaymentAPIResponse response = testee.cancelAllReservations(createCancelAllReservationsRequest(paymentEventList));

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents()).isEmpty();
	}

	@Test
	public void cancelAllReservationsWithTwoReservationEventAndCancelCapabilityThrowsException() {
		PaymentProviderPluginForIntegrationTesting.addCapability(getClass(), CancelCapability.class, request -> {
			throw CAPABILITY_EXCEPTION;
		});
		PaymentEvent firstReservationEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), DEFAULT_RESERVED_AMOUNT_10_USD);
		PaymentEvent secondReservationEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), DEFAULT_RESERVED_AMOUNT_10_USD);
		List<PaymentEvent> paymentEventList = Arrays.asList(firstReservationEvent, secondReservationEvent);

		final PaymentAPIResponse response = testee.cancelAllReservations(createCancelAllReservationsRequest(paymentEventList));

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsOnly(TEN);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsOnly(CANCEL_RESERVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsOnly(FAILED);
	}

	@Test
	public void cancelAllReservationsWithTwoReservationEventAndCancelCapabilityIsUnsupported() {
		PaymentEvent firstReservationEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), DEFAULT_RESERVED_AMOUNT_10_USD);
		PaymentEvent secondReservationEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), DEFAULT_RESERVED_AMOUNT_10_USD);
		List<PaymentEvent> paymentEventList = Arrays.asList(firstReservationEvent, secondReservationEvent);
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), CancelCapability.class);

		final PaymentAPIResponse response = testee.cancelAllReservations(createCancelAllReservationsRequest(paymentEventList));

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsOnly(TEN);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsOnly(CANCEL_RESERVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsOnly(SKIPPED);
	}

	@Test
	public void cancelReservationWithEmptySelectedEvents() {
		PaymentEvent reservationEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), DEFAULT_RESERVED_AMOUNT_10_USD);

		final PaymentAPIResponse response = testee.cancelReservation(createCancelReservationRequestWithoutSelectedEvents(reservationEvent,
				DEFAULT_RESERVED_AMOUNT_10_USD));

		assertThat(response.isSuccess()).isEqualTo(false);
		assertThat(response.getExternalMessage()).isEqualTo("The system could not cancel the reservation.");
		assertThat(response.getInternalMessage()).isEqualTo("The cancel reservation process failed due to no cancelable payment events.");
		assertThat(response.getEvents()).isEmpty();
	}


	/**
	 * Creates cancel all reservations request to Payment API.
	 *
	 * @param reservationEvent original reservation event
	 * @param amount           reserved amount
	 * @return request
	 */
	CancelReservationRequest createCancelReservationRequestWithoutSelectedEvents(final PaymentEvent reservationEvent, final MoneyDTO amount) {
		return CancelReservationRequestBuilder.builder()
				.withOrderPaymentInstruments(Collections.singletonList(reservationEvent.getOrderPaymentInstrumentDTO()))
				.withSelectedPaymentEventsToCancel(emptyList())
				.withLedger(Collections.singletonList(reservationEvent))
				.withAmount(amount)
				.withCustomRequestData(Collections.emptyMap())
				.withOrderContext(createOrderContext())
				.build(getBeanFactory());
	}
}