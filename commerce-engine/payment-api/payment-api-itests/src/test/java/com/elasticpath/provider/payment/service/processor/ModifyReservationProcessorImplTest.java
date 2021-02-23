/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.provider.payment.service.processor;

import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.APPROVED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.FAILED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.SKIPPED;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.CANCEL_RESERVE;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.MODIFY_RESERVE;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.RESERVE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Test;

import com.elasticpath.plugin.payment.provider.capabilities.reservation.CancelCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ModifyCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ReserveCapability;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIResponse;
import com.elasticpath.provider.payment.domain.transaction.modify.ModifyReservationRequest;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;
import com.elasticpath.service.payment.provider.PaymentProviderPluginForIntegrationTesting;

public class ModifyReservationProcessorImplTest extends AbstractProcessorImplTestBase {

	@Inject
	@Named(PaymentProviderApiContextIdNames.MODIFY_RESERVATION_PROCESSOR)
	private ModifyReservationProcessor testee;

	@Test
	public void reserveWhenNoReservationsFound() {
		final MoneyDTO newAmount = createMoney(THREE);

		final ModifyReservationRequest request = createModifyReservationRequest(newAmount);
		request.setLedger(Collections.emptyList());
		final PaymentAPIResponse response = testee.modifyReservation(request);

		checkReserveResponse(response, newAmount.getAmount());
		checkSinglePaymentEventApproved(response);
	}

	@Test
	public void decreaseWithSupportedModifyCapability() {
		final MoneyDTO newAmount = createMoney(THREE);

		final PaymentAPIResponse response = testee.modifyReservation(createModifyReservationRequest(newAmount));

		checkModifyResponse(response, newAmount.getAmount());
		checkSinglePaymentEventApproved(response);
	}

	@Test
	public void decreaseWithFailingModifyCapabilityAlwaysSucceedsAndGeneratesSkippedEvent() {
		PaymentProviderPluginForIntegrationTesting.addCapability(getClass(), ModifyCapability.class, request -> {
			throw CAPABILITY_EXCEPTION;
		});
		final MoneyDTO newAmount = createMoney(THREE);

		final PaymentAPIResponse response = testee.modifyReservation(createModifyReservationRequest(newAmount));

		assertThat(response.isSuccess()).isEqualTo(true);
		checkModifyResponse(response, newAmount.getAmount());
		checkSinglePaymentEventSkipped(response);
	}

	@Test
	public void increaseWithSupportedModifyCapability() {
		final MoneyDTO newAmount = createMoney(FIFTEEN);

		final PaymentAPIResponse response = testee.modifyReservation(createModifyReservationRequest(newAmount));

		checkModifyResponse(response, newAmount.getAmount());
		checkSinglePaymentEventApproved(response);
	}

	@Test
	public void increaseWithFailingModifyCapabilityFailsAndGeneratesNoEvents() {
		PaymentProviderPluginForIntegrationTesting.addCapability(getClass(), ModifyCapability.class, request -> {
			throw CAPABILITY_EXCEPTION;
		});
		final MoneyDTO newAmount = createMoney(FIFTEEN);

		final PaymentAPIResponse response = testee.modifyReservation(createModifyReservationRequest(newAmount));

		assertThat(response.isSuccess()).isFalse();
		assertThat(response.getExternalMessage()).isEqualTo("Increase amount modification failed.");
		assertThat(response.getInternalMessage()).isEqualTo("Elastic Path attempted to replace the existing reservation with a new, higher amount, "
				+ "but was unsuccessful.\nTherefore, the order modification cannot be completed.");
		assertThat(response.getEvents()).isEmpty();
	}

	@Test
	public void decreaseToZeroWithoutSupportedModifyCapability() {
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), ModifyCapability.class);
		final MoneyDTO newAmount = createMoney(BigDecimal.ZERO);

		final PaymentAPIResponse response = testee.modifyReservation(createModifyReservationRequest(newAmount));

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(DEFAULT_RESERVED_AMOUNT_10_USD.getAmount());
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(CANCEL_RESERVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED);
	}

	@Test
	public void decreaseWithoutSupportedModifyCapability() {
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), ModifyCapability.class);
		final MoneyDTO newAmount = createMoney(THREE);

		final PaymentAPIResponse response = testee.modifyReservation(createModifyReservationRequest(newAmount));

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(newAmount.getAmount(), DEFAULT_RESERVED_AMOUNT_10_USD.getAmount());
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(RESERVE, CANCEL_RESERVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED, APPROVED);
	}

	@Test
	public void decreaseWithoutSupportedModifyAndCancelCapabilitiesSucceedsKeepingAlreadyReservedAmount() {
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), ModifyCapability.class);
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), CancelCapability.class);
		final MoneyDTO newAmount = createMoney(THREE);

		final PaymentAPIResponse response = testee.modifyReservation(createModifyReservationRequest(newAmount));

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents()).isEmpty();
	}

	@Test
	public void increaseWithoutSupportedModifyCapability() {
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), ModifyCapability.class);
		final MoneyDTO newAmount = createMoney(FIFTEEN);

		final PaymentAPIResponse response = testee.modifyReservation(createModifyReservationRequest(newAmount));

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(FIVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(RESERVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED);
	}

	@Test
	public void decreaseWithSupportedModifyCapabilityUsingMultipleInstrumentsUsesRandomInstrumentByDefault() {
		final MoneyDTO newAmount = createMoney(THIRTEEN);
		final ModifyReservationRequest modifyReservationRequest = createModifyReservationRequest(newAmount);
		simulateLimitedInstrumentReservation(modifyReservationRequest, FIVE);

		final PaymentAPIResponse response = testee.modifyReservation(modifyReservationRequest);

		assertThat(response.isSuccess()).isEqualTo(true);
		checkModifyResponse(response, THREE, EIGHT);
		checkSinglePaymentEventApproved(response);
	}

	@Test
	public void increaseWithSupportedModifyCapabilityUsingMultipleInstruments() {
		final MoneyDTO newAmount = createMoney(EIGHTEEN);
		final ModifyReservationRequest modifyReservationRequest = createModifyReservationRequest(newAmount);
		simulateLimitedInstrumentReservation(modifyReservationRequest, FIVE);

		final PaymentAPIResponse response = testee.modifyReservation(modifyReservationRequest);

		assertThat(response.isSuccess()).isEqualTo(true);
		checkModifyResponse(response, THIRTEEN);
		checkSinglePaymentEventApproved(response);
	}

	@Test
	public void increasePreviouslyDecreasedLimitedInstrumentReservationUsesUnlimitedInstrumentByDefault() {
		final MoneyDTO newAmount = createMoney(EIGHTEEN);
		final ModifyReservationRequest modifyReservationRequest = createModifyReservationRequest(newAmount);
		simulateLimitedInstrumentReservation(modifyReservationRequest, FIVE, EIGHT);

		final PaymentAPIResponse response = testee.modifyReservation(modifyReservationRequest);

		assertThat(response.isSuccess()).isEqualTo(true);
		checkModifyResponse(response, THIRTEEN);
		checkSinglePaymentEventApproved(response);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getOrderPaymentInstrumentDTO)
				.extracting(OrderPaymentInstrumentDTO::hasLimit)
				.containsExactly(false);
	}

	@Test
	public void increaseUnlimitedInstrumentReservationWithPartiallyChargedLimitedInstrumentReservation() {
		final MoneyDTO newAmount = createMoney(EIGHTEEN);
		final ModifyReservationRequest modifyReservationRequest = createModifyReservationRequest(newAmount);
		simulateLimitedInstrumentReservation(modifyReservationRequest, FIVE);
		simulatePartialCharge(modifyReservationRequest, TWO, THREE);

		final PaymentAPIResponse response = testee.modifyReservation(modifyReservationRequest);

		assertThat(response.isSuccess()).isEqualTo(true);
		checkModifyResponse(response, THIRTEEN);
		checkSinglePaymentEventApproved(response);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getOrderPaymentInstrumentDTO)
				.extracting(OrderPaymentInstrumentDTO::hasLimit)
				.containsExactly(false);
	}

	@Test
	public void decreaseWithoutModifyCapabilityUsingMultipleInstruments() {
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), ModifyCapability.class);
		final ModifyReservationRequest modifyReservationRequest = createModifyReservationRequest(createMoney(THIRTEEN));
		simulateLimitedInstrumentReservation(modifyReservationRequest, FIVE);

		final PaymentAPIResponse response = testee.modifyReservation(modifyReservationRequest);

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.filteredOn(event -> event.getPaymentType() == CANCEL_RESERVE)
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsAnyOf(DEFAULT_RESERVED_AMOUNT_10_USD.getAmount(), FIVE);
		assertThat(response.getEvents())
				.filteredOn(event -> event.getPaymentType() == RESERVE)
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsAnyOf(EIGHT, THREE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(RESERVE, CANCEL_RESERVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED, APPROVED);
	}

	@Test
	public void increaseWithoutModifyCapabilityUsingMultipleInstruments() {
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), ModifyCapability.class);
		final MoneyDTO newAmount = createMoney(EIGHTEEN);
		final ModifyReservationRequest modifyReservationRequest = createModifyReservationRequest(newAmount);
		simulateLimitedInstrumentReservation(modifyReservationRequest, FIVE);

		final PaymentAPIResponse response = testee.modifyReservation(modifyReservationRequest);

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(THREE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(RESERVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED);
	}

	@Test
	public void increaseWithSupportedModifyCapabilityWhenOrderIsPartlyCharged() {
		final ModifyReservationRequest modifyReservationRequest = createModifyReservationRequest(createMoney(FIFTEEN));
		simulatePartialCharge(modifyReservationRequest, THREE, SEVEN);

		final PaymentAPIResponse response = testee.modifyReservation(modifyReservationRequest);

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(TWELVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(MODIFY_RESERVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED);
	}

	@Test
	public void decreaseWithSupportedModifyCapabilityWhenOrderIsPartlyCharged() {
		final ModifyReservationRequest modifyReservationRequest = createModifyReservationRequest(createMoney(FIVE));
		simulatePartialCharge(modifyReservationRequest, THREE, SEVEN);

		final PaymentAPIResponse response = testee.modifyReservation(modifyReservationRequest);

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(TWO);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(MODIFY_RESERVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED);
	}

	@Test
	public void increaseWithoutReserveAndModifyCapabilitiesWhenOrderIsPartlyCharged() {
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), ReserveCapability.class);
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), ModifyCapability.class);
		final ModifyReservationRequest modifyReservationRequest = createModifyReservationRequest(createMoney(FIFTEEN));
		simulatePartialCharge(modifyReservationRequest, THREE, SEVEN);

		final PaymentAPIResponse response = testee.modifyReservation(modifyReservationRequest);

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(FIVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(RESERVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(SKIPPED);
	}

	@Test
	public void decreaseWithoutReserveAndModifyCapabilitiesWhenOrderIsPartlyCharged() {
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), ReserveCapability.class);
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), ModifyCapability.class);
		final ModifyReservationRequest modifyReservationRequest = createModifyReservationRequest(createMoney(FIVE));
		simulatePartialCharge(modifyReservationRequest, THREE, SEVEN);

		final PaymentAPIResponse response = testee.modifyReservation(modifyReservationRequest);

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(BigDecimal.valueOf(2), SEVEN);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(RESERVE, CANCEL_RESERVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(SKIPPED, APPROVED);
	}

	@Test
	public void increaseWithoutModifyAndCancelCapabilitiesWhenOrderIsPartlyCharged() {
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), CancelCapability.class);
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), ModifyCapability.class);
		final ModifyReservationRequest modifyReservationRequest = createModifyReservationRequest(createMoney(FIFTEEN));
		simulatePartialCharge(modifyReservationRequest, THREE, SEVEN);

		final PaymentAPIResponse response = testee.modifyReservation(modifyReservationRequest);

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(FIVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(RESERVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED);
	}

	@Test
	public void shouldReturnUnsuccessfulResponseWithEmptyEventListWhenIncreasingReserveAmountForSingleReservePerPIPaymentInstrument() {
		final ModifyReservationRequest modifyReservationRequest =
				createModifyReservationRequestWithSingleReservePerPIPaymentInstrument(createMoney(FIFTEEN));
		modifyReservationRequest.setSingleReservePerPI(true);

		final PaymentAPIResponse response = testee.modifyReservation(modifyReservationRequest);

		assertThat(response.isSuccess()).isEqualTo(false);
		assertThat(response.getExternalMessage()).isEqualTo("The system could not reserve funds.");
		assertThat(response.getInternalMessage()).isEqualTo("The reserve process cannot continue because increase amount is impossible.");
		assertThat(response.getEvents()).isEmpty();
	}

	@Test
	public void decreaseWithoutModifyAndCancelCapabilitiesWhenOrderIsPartlyCharged() {
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), CancelCapability.class);
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), ModifyCapability.class);
		final ModifyReservationRequest modifyReservationRequest = createModifyReservationRequest(createMoney(FIVE));
		simulatePartialCharge(modifyReservationRequest, THREE, SEVEN);

		final PaymentAPIResponse response = testee.modifyReservation(modifyReservationRequest);

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents()).isEmpty();
	}

	@Test
	public void decreaseWithoutModifyAndFailingReserveCapability() {
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), ModifyCapability.class);
		PaymentProviderPluginForIntegrationTesting.addCapability(getClass(), ReserveCapability.class, request -> {
			throw CAPABILITY_EXCEPTION;
		});

		final PaymentAPIResponse response = testee.modifyReservation(createModifyReservationRequest(createMoney(THREE)));

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(THREE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(RESERVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(FAILED);
	}

	@Test
	public void shouldReturnSuccessfulResponseWithEmptyEventListWhenDecreasingReserveAmountForSingleReservePerPIPaymentInstrument() {
		final ModifyReservationRequest modifyReservationRequest =
				createModifyReservationRequestWithSingleReservePerPIPaymentInstrument(createMoney(FIVE));
		modifyReservationRequest.setSingleReservePerPI(true);

		final PaymentAPIResponse response = testee.modifyReservation(modifyReservationRequest);

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents()).isEmpty();
	}

	@Test
	public void shouldReturnSuccessfulResponseWithCancelPaymentEventWhenDecreasingReserveAmountForSingleReservePerPIPaymentInstrument() {
		final ModifyReservationRequest modifyReservationRequest =
				createModifyReservationRequestWithSingleReservePerPIPaymentInstrument(createMoney(BigDecimal.ZERO));
		modifyReservationRequest.setSingleReservePerPI(true);
		modifyReservationRequest.setFinalPayment(true);

		final PaymentAPIResponse response = testee.modifyReservation(modifyReservationRequest);

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

	private void simulateLimitedInstrumentReservation(final ModifyReservationRequest modifyReservationRequest,
													  final BigDecimal reserved, final BigDecimal limit) {
		final List<PaymentEvent> newLedger = new ArrayList<>();
		final List<OrderPaymentInstrumentDTO> newInstruments = new ArrayList<>(modifyReservationRequest.getOrderPaymentInstruments());
		OrderPaymentInstrumentDTO limitedInstrument = createLimitedOrderPaymentInstrument(createMoney(limit));
		newInstruments.add(limitedInstrument);
		newLedger.add(createReservationEvent(limitedInstrument, createMoney(reserved)));
		newLedger.addAll(modifyReservationRequest.getLedger());
		modifyReservationRequest.setLedger(newLedger);
		modifyReservationRequest.setOrderPaymentInstruments(newInstruments);
	}

	private void simulateLimitedInstrumentReservation(final ModifyReservationRequest modifyReservationRequest, final BigDecimal reserved) {
		simulateLimitedInstrumentReservation(modifyReservationRequest, reserved, reserved);
	}

	private void simulatePartialCharge(final ModifyReservationRequest request, final BigDecimal charged, final BigDecimal reserved) {
		final List<PaymentEvent> newLedger = new ArrayList<>(request.getLedger());
		final PaymentEvent reservationEvent = request.getLedger().get(0);
		newLedger.add(createChargeEvent(reservationEvent, createMoney(charged)));
		newLedger.add(createReservationEvent(reservationEvent.getOrderPaymentInstrumentDTO(), createMoney(reserved)));
		request.setLedger(newLedger);
	}

}