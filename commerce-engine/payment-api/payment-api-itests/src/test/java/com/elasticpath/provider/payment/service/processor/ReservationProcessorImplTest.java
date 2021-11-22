/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.provider.payment.service.processor;

import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.APPROVED;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Test;

import com.elasticpath.plugin.payment.provider.capabilities.reservation.ReserveCapability;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIResponse;
import com.elasticpath.provider.payment.domain.transaction.reservation.ReserveRequest;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;
import com.elasticpath.service.payment.provider.PaymentProviderPluginForIntegrationTesting;

public class ReservationProcessorImplTest extends AbstractProcessorImplTestBase {

	@Inject
	@Named(PaymentProviderApiContextIdNames.RESERVATION_PROCESSOR)
	private ReservationProcessor testee;

	@Test
	public void reserveShouldRespondWithAcceptedEventAndInformationSuppliedByCapability() {
		final ReserveRequest reserveRequest = createReserveRequest(createMoney(TEN));

		final PaymentAPIResponse response = testee.reserve(reserveRequest);

		checkReserveResponse(response, reserveRequest.getAmount().getAmount());
		checkSinglePaymentEventApproved(response);
	}

	@Test
	public void reserveWithZeroAmount() {
		final ReserveRequest reserveRequest = createReserveRequest(createMoney(BigDecimal.ZERO));

		final PaymentAPIResponse response = testee.reserve(reserveRequest);

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents()).isEmpty();
	}

	@Test
	public void reserveShouldRespondWithSkippedEventWhenCapabilityIsUnavailable() {
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), ReserveCapability.class);
		final ReserveRequest reserveRequest = createReserveRequest(createMoney(TEN));

		final PaymentAPIResponse response = testee.reserve(reserveRequest);

		checkReserveResponse(response, reserveRequest.getAmount().getAmount());
		assertThat(response.isSuccess()).isEqualTo(true);
		checkSinglePaymentEventSkipped(response);
	}

	@Test
	public void reserveShouldRespondWithFailedEventWhenCapabilityThrowsException() {
		PaymentProviderPluginForIntegrationTesting.addCapability(getClass(), ReserveCapability.class, request -> {
			throw CAPABILITY_EXCEPTION;
		});
		final ReserveRequest reserveRequest = createReserveRequest(createMoney(TEN));

		final PaymentAPIResponse response = testee.reserve(reserveRequest);

		checkReserveResponse(response, reserveRequest.getAmount().getAmount());
		checkSinglePaymentEventFailed(response);
		assertThat(response.getExternalMessage()).isEqualTo("The capability throws exception.");
		assertThat(response.getInternalMessage()).isEqualTo("The request failed.");
	}

	@Test
	public void reserveToSimulateModifyShouldRespondWithAcceptedEventAndInformationSuppliedByCapability() {
		final ReserveRequest reserveRequest = createReserveRequest(createMoney(TEN));
		final OrderPaymentInstrumentDTO unlimitedInstrument = reserveRequest.getSelectedOrderPaymentInstruments().get(0);

		final PaymentAPIResponse response = testee.reserveToSimulateModify(reserveRequest.getAmount(),
				unlimitedInstrument, reserveRequest.getCustomRequestData(), reserveRequest.getOrderContext(), 0);

		checkReserveResponse(response, reserveRequest.getAmount().getAmount());
		checkSinglePaymentEventApproved(response);
	}

	@Test
	public void reserveToSimulateModifyShouldRespondWithSkippedEventWhenCapabilityIsUnavailable() {
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), ReserveCapability.class);
		final ReserveRequest reserveRequest = createReserveRequest(createMoney(TEN));
		final OrderPaymentInstrumentDTO unlimitedInstrument = reserveRequest.getSelectedOrderPaymentInstruments().get(0);

		final PaymentAPIResponse response = testee.reserveToSimulateModify(reserveRequest.getAmount(),
				unlimitedInstrument, reserveRequest.getCustomRequestData(), reserveRequest.getOrderContext(), 0);

		checkReserveResponse(response, reserveRequest.getAmount().getAmount());
		assertThat(response.isSuccess()).isEqualTo(true);
		checkSinglePaymentEventSkipped(response);
	}

	@Test
	public void reserveToSimulateModifyShouldRespondWithFailedEventWhenCapabilityThrowsException() {
		PaymentProviderPluginForIntegrationTesting.addCapability(getClass(), ReserveCapability.class, request -> {
			throw CAPABILITY_EXCEPTION;
		});
		final ReserveRequest reserveRequest = createReserveRequest(createMoney(TEN));
		final OrderPaymentInstrumentDTO unlimitedInstrument = reserveRequest.getSelectedOrderPaymentInstruments().get(0);

		final PaymentAPIResponse response = testee.reserveToSimulateModify(reserveRequest.getAmount(),
				unlimitedInstrument, reserveRequest.getCustomRequestData(), reserveRequest.getOrderContext(), 0);

		checkReserveResponse(response, reserveRequest.getAmount().getAmount());
		checkSinglePaymentEventFailed(response);
		assertThat(response.getExternalMessage()).isEqualTo("The capability throws exception.");
		assertThat(response.getInternalMessage()).isEqualTo("The request failed.");
	}

	@Test
	public void reserveShouldOnlyUseLimitedInstrumentWhenLimitIsCoveringReservationAmount() {
		final MoneyDTO reservationAmount = createMoney(TWO);
		final ReserveRequest reserveRequest = createReserveRequest(reservationAmount);
		final List<OrderPaymentInstrumentDTO> selectedInstruments = new ArrayList<>(reserveRequest.getSelectedOrderPaymentInstruments());
		selectedInstruments.add(createLimitedOrderPaymentInstrument(createMoney(TWO)));
		reserveRequest.setSelectedOrderPaymentInstruments(selectedInstruments);

		final PaymentAPIResponse response = testee.reserve(reserveRequest);

		checkReserveResponse(response, reservationAmount.getAmount());
		checkSinglePaymentEventApproved(response);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getOrderPaymentInstrumentDTO)
				.extracting(OrderPaymentInstrumentDTO::getLimit)
				.hasOnlyOneElementSatisfying(limit -> assertThat(limit.getAmount()).isEqualByComparingTo(TWO));
	}

	@Test
	public void reserveShouldUseBothInstrumentsWhenLimitIsLessThanReservationAmount() {
		final MoneyDTO reservationAmount = createMoney(TEN);
		final ReserveRequest reserveRequest = createReserveRequest(reservationAmount);
		final List<OrderPaymentInstrumentDTO> selectedInstruments = new ArrayList<>(reserveRequest.getSelectedOrderPaymentInstruments());
		selectedInstruments.add(createLimitedOrderPaymentInstrument(createMoney(TWO)));
		reserveRequest.setSelectedOrderPaymentInstruments(selectedInstruments);

		final PaymentAPIResponse response = testee.reserve(reserveRequest);

		assertThat(response).extracting(paymentAPIResponse -> paymentAPIResponse.getEvents().size()).isEqualTo(2);
		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentStatus).containsOnly(APPROVED);
		assertThat(response.getEvents()).extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactlyInAnyOrder(TWO, EIGHT);
		assertThat(response.getEvents()).extracting(PaymentEvent::getOrderPaymentInstrumentDTO).hasSize(2);
		assertThat(response.getEvents()).extracting(PaymentEvent::getOrderPaymentInstrumentDTO)
				.extracting(OrderPaymentInstrumentDTO::getLimit)
				.extracting(MoneyDTO::getAmount)
				.containsExactlyInAnyOrder(TWO, BigDecimal.ZERO);
	}
}
