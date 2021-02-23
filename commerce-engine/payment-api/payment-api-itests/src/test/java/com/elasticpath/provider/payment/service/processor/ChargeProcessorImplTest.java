/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.provider.payment.service.processor;

import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.APPROVED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.FAILED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.SKIPPED;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.CANCEL_RESERVE;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.CHARGE;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.MODIFY_RESERVE;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.RESERVE;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Test;

import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ModifyCapability;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.OrderContext;
import com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIResponse;
import com.elasticpath.provider.payment.domain.transaction.charge.ChargeRequest;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;
import com.elasticpath.service.payment.provider.PaymentProviderPluginForIntegrationTesting;

public class ChargeProcessorImplTest extends AbstractProcessorImplTestBase {

	@Inject
	@Named(PaymentProviderApiContextIdNames.CHARGE_PROCESSOR)
	private ChargeProcessor testee;

	@Test
	public void chargeLessThanOrderAmount() {
		final MoneyDTO chargedAmount = createMoney(THREE);

		final PaymentAPIResponse response = testee.chargePayment(createChargeRequest(chargedAmount));

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(chargedAmount.getAmount(), SEVEN);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(CHARGE, RESERVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED, APPROVED);
	}

	@Test
	public void chargeLessThanOrderAmountWithMultipleLimitedInstruments() {
		final MoneyDTO chargedAmount = createMoney(THREE);
		final MoneyDTO limit = createMoney(FIVE);
		final OrderPaymentInstrumentDTO limitedInstrument1 = createLimitedOrderPaymentInstrument(limit);
		final OrderPaymentInstrumentDTO limitedInstrument2 = createLimitedOrderPaymentInstrument(limit);
		final PaymentEvent firstReservationEvent = createReservationEvent(limitedInstrument1, limit);
		final PaymentEvent secondReservationEvent = createReservationEvent(limitedInstrument2, limit);
		final ChargeRequest chargeRequest = createChargeRequest(asList(firstReservationEvent, secondReservationEvent),
				chargedAmount, createOrderContext(), asList(limitedInstrument1, limitedInstrument2));

		final PaymentAPIResponse response = testee.chargePayment(chargeRequest);

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.filteredOn(event -> event.getPaymentType() == CHARGE)
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(THREE);
		assertThat(response.getEvents())
				.filteredOn(event -> event.getPaymentType() == RESERVE)
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(TWO);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsOnly(APPROVED);
	}

	@Test
	public void chargeOrderAmount() {
		final MoneyDTO chargedAmount = DEFAULT_RESERVED_AMOUNT_10_USD;

		final PaymentAPIResponse response = testee.chargePayment(createChargeRequest(chargedAmount));

		checkChargeResponse(response, chargedAmount.getAmount());
		checkSinglePaymentEventApproved(response);
	}

	@Test
	public void chargeWhenMultipleReservation() {
		final ChargeRequest chargeRequest = createChargeRequest(DEFAULT_RESERVED_AMOUNT_10_USD);
		final OrderPaymentInstrumentDTO orderPaymentInstrumentDTO = chargeRequest.getOrderPaymentInstruments().get(0);

		final List<PaymentEvent> ledger = new ArrayList<>();
		ledger.add(createReservationEvent(orderPaymentInstrumentDTO, DEFAULT_RESERVED_AMOUNT_10_USD));
		ledger.add(createReservationEvent(orderPaymentInstrumentDTO, DEFAULT_RESERVED_AMOUNT_10_USD));

		chargeRequest.setLedger(ledger);

		final PaymentAPIResponse response = testee.chargePayment(chargeRequest);

		assertThat(response.isSuccess()).isTrue();
		final List<PaymentEvent> paymentEvents = response.getEvents();
		assertThat(paymentEvents).hasSize(1);
		assertThat(paymentEvents).extracting(PaymentEvent::getAmount).extracting(MoneyDTO::getAmount).containsExactly(TEN);
		assertThat(paymentEvents).extracting(PaymentEvent::getPaymentType).containsExactly(CHARGE);
		assertThat(paymentEvents).extracting(PaymentEvent::getPaymentStatus).containsExactly(APPROVED);
	}

	@Test
	public void chargeMoreThanOrderAmount() {
		final MoneyDTO chargedAmount = createMoney(THIRTEEN);

		final PaymentAPIResponse response = testee.chargePayment(createChargeRequest(chargedAmount));

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(THIRTEEN, THIRTEEN);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(MODIFY_RESERVE, CHARGE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED, APPROVED);
	}

	@Test
	public void chargeOrderAmountModified() {
		final MoneyDTO chargedAmount = createMoney(EIGHT);
		final ChargeRequest chargeRequest = createChargeRequest(chargedAmount);
		final List<PaymentEvent> newLedger = new ArrayList<>(chargeRequest.getLedger());
		newLedger.add(createModifyReservationEvent(chargeRequest.getLedger().get(0), chargedAmount));
		chargeRequest.setLedger(newLedger);
		chargeRequest.getOrderContext().setOrderTotal(chargedAmount);

		final PaymentAPIResponse response = testee.chargePayment(chargeRequest);

		checkChargeResponse(response, chargedAmount.getAmount());
		checkSinglePaymentEventApproved(response);
	}

	@Test
	public void chargeOrderAmountWhereModificationWasSkipped() {
		final MoneyDTO chargedAmount = createMoney(EIGHT);
		final ChargeRequest chargeRequest = createChargeRequest(chargedAmount);
		final List<PaymentEvent> newLedger = new ArrayList<>(chargeRequest.getLedger());
		final PaymentEvent reservationEvent = chargeRequest.getLedger().get(0);
		newLedger.add(createModifyReservationEvent(reservationEvent, chargedAmount, SKIPPED));
		chargeRequest.setLedger(newLedger);
		chargeRequest.getOrderContext().setOrderTotal(chargedAmount);

		final PaymentAPIResponse response = testee.chargePayment(chargeRequest);

		checkChargeResponse(response, chargedAmount.getAmount());
		checkSinglePaymentEventApproved(response);
	}

	@Test
	public void chargeOrderAmountModifiedWithSimulationOfDecrease() {
		final MoneyDTO chargedAmount = createMoney(EIGHT);
		final ChargeRequest chargeRequest = createChargeRequest(chargedAmount);
		final List<PaymentEvent> newLedger = new ArrayList<>(chargeRequest.getLedger());
		final PaymentEvent reservationEvent = chargeRequest.getLedger().get(0);
		newLedger.add(createReservationEvent(reservationEvent.getOrderPaymentInstrumentDTO(), chargedAmount));
		newLedger.add(createCancelReservationEvent(reservationEvent));
		chargeRequest.setLedger(newLedger);
		chargeRequest.getOrderContext().setOrderTotal(chargedAmount);

		final PaymentAPIResponse response = testee.chargePayment(chargeRequest);

		checkChargeResponse(response, chargedAmount.getAmount());
		checkSinglePaymentEventApproved(response);
	}

	@Test
	public void chargeOrderAmountModifiedWithSimulationOfIncrease() {
		final MoneyDTO chargedAmount = createMoney(THIRTEEN);
		final ChargeRequest chargeRequest = createChargeRequest(chargedAmount);
		final List<PaymentEvent> newLedger = new ArrayList<>(chargeRequest.getLedger());
		final PaymentEvent reservationEvent = chargeRequest.getLedger().get(0);
		newLedger.add(createReservationEvent(reservationEvent.getOrderPaymentInstrumentDTO(), createMoney(THREE)));
		chargeRequest.setLedger(newLedger);
		chargeRequest.getOrderContext().setOrderTotal(chargedAmount);

		final PaymentAPIResponse response = testee.chargePayment(chargeRequest);

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsOnly(DEFAULT_RESERVED_AMOUNT_10_USD.getAmount(), THREE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(CHARGE, CHARGE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED, APPROVED);
	}

	@Test
	public void chargeOrderAmountModifiedWithSimulationWhenCancelIsFailing() {
		final MoneyDTO chargedAmount = createMoney(EIGHT);
		final ChargeRequest chargeRequest = createChargeRequest(chargedAmount);
		final List<PaymentEvent> newLedger = new ArrayList<>(chargeRequest.getLedger());
		final PaymentEvent reservationEvent = chargeRequest.getLedger().get(0);
		newLedger.add(createReservationEvent(reservationEvent.getOrderPaymentInstrumentDTO(), chargedAmount));
		newLedger.add(createCancelReservationEvent(reservationEvent, DEFAULT_RESERVED_AMOUNT_10_USD, FAILED));
		chargeRequest.setLedger(newLedger);
		chargeRequest.getOrderContext().setOrderTotal(chargedAmount);

		final PaymentAPIResponse response = testee.chargePayment(chargeRequest);

		checkChargeResponse(response, chargedAmount.getAmount());
		checkSinglePaymentEventApproved(response);
	}

	@Test
	public void chargeOrderAmountModifiedWithSimulationWhenCancelFailedAndReservationWasUnsupported() {
		final MoneyDTO chargedAmount = createMoney(EIGHT);
		final ChargeRequest chargeRequest = createChargeRequest(chargedAmount);
		final List<PaymentEvent> newLedger = new ArrayList<>(chargeRequest.getLedger());
		final PaymentEvent reservationEvent = chargeRequest.getLedger().get(0);
		newLedger.add(createReservationEvent(reservationEvent.getOrderPaymentInstrumentDTO(), chargedAmount, SKIPPED));
		newLedger.add(createCancelReservationEvent(reservationEvent, DEFAULT_RESERVED_AMOUNT_10_USD, FAILED));
		chargeRequest.setLedger(newLedger);
		chargeRequest.getOrderContext().setOrderTotal(chargedAmount);

		final PaymentAPIResponse response = testee.chargePayment(chargeRequest);

		checkChargeResponse(response, chargedAmount.getAmount());
		checkSinglePaymentEventApproved(response);
	}

	@Test
	public void chargeOrderAmountModifiedWithSimulationWhenFailedReservationWasReattempted() {
		final MoneyDTO chargedAmount = createMoney(FIFTEEN);
		final ChargeRequest chargeRequest = createChargeRequest(chargedAmount);
		final List<PaymentEvent> newLedger = new ArrayList<>(chargeRequest.getLedger());
		final PaymentEvent reservationEvent = chargeRequest.getLedger().get(0);
		newLedger.add(createReservationEvent(reservationEvent.getOrderPaymentInstrumentDTO(), createMoney(FIVE), FAILED));
		newLedger.add(createReservationEvent(reservationEvent.getOrderPaymentInstrumentDTO(), createMoney(FIVE), APPROVED));
		chargeRequest.setLedger(newLedger);
		chargeRequest.getOrderContext().setOrderTotal(chargedAmount);

		final PaymentAPIResponse response = testee.chargePayment(chargeRequest);

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsOnly(DEFAULT_RESERVED_AMOUNT_10_USD.getAmount(), FIVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(CHARGE, CHARGE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED, APPROVED);
	}

	@Test
	public void chargeOrderPartiallyWhenOtherPartWasRefundedTwiceAndFully() {
		final ChargeRequest chargeRequest = createChargeRequest(DEFAULT_RESERVED_AMOUNT_10_USD);
		final List<PaymentEvent> newLedger = new ArrayList<>(chargeRequest.getLedger());
		final PaymentEvent firstReservationEvent = chargeRequest.getLedger().get(0);
		final PaymentEvent chargeEvent = createChargeEvent(firstReservationEvent, createMoney(FIVE));
		final PaymentEvent secondReservationEvent = createReservationEvent(firstReservationEvent.getOrderPaymentInstrumentDTO(), createMoney(FIVE));
		final PaymentEvent firstCreditEvent = createCreditEvent(chargeEvent, createMoney(THREE));
		final PaymentEvent secondCreditEvent = createCreditEvent(chargeEvent, createMoney(TWO));
		newLedger.add(chargeEvent);
		newLedger.add(secondReservationEvent);
		newLedger.add(firstCreditEvent);
		newLedger.add(secondCreditEvent);
		chargeRequest.setLedger(newLedger);

		final PaymentAPIResponse response = testee.chargePayment(chargeRequest);

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsOnly(FIVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(CHARGE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED);
	}

	@Test
	public void chargeOrderForTheRestOfReservedMoneyAfterPartialCharge() {
		final ChargeRequest chargeRequest = createChargeRequest(DEFAULT_RESERVED_AMOUNT_10_USD);
		List<PaymentEvent> newLedger = new ArrayList<>(chargeRequest.getLedger());
		final PaymentEvent reservationEvent = chargeRequest.getLedger().get(0);
		newLedger.add(createChargeEvent(reservationEvent, createMoney(TWO)));
		newLedger.add(createReservationEvent(reservationEvent.getOrderPaymentInstrumentDTO(), createMoney(EIGHT)));
		chargeRequest.setLedger(newLedger);

		final PaymentAPIResponse paymentAPIResponse = testee.chargePayment(chargeRequest);

		assertThat(paymentAPIResponse.isSuccess()).isTrue();
		assertThat(paymentAPIResponse.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED);
		assertThat(paymentAPIResponse.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(CHARGE);
		assertThat(paymentAPIResponse.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(EIGHT);
	}

	@Test
	public void chargeOrderForTheRestOfReservedMoneyAfterRefund() {
		final ChargeRequest chargeRequest = createChargeRequest(DEFAULT_RESERVED_AMOUNT_10_USD);
		List<PaymentEvent> newLedger = new ArrayList<>(chargeRequest.getLedger());
		final PaymentEvent firstReservationEvent = chargeRequest.getLedger().get(0);
		final PaymentEvent chargeEvent = createChargeEvent(firstReservationEvent, createMoney(TWO));
		final PaymentEvent secondReservationEvent = createReservationEvent(firstReservationEvent.getOrderPaymentInstrumentDTO(), createMoney(EIGHT));
		final PaymentEvent creditEvent = createCreditEvent(chargeEvent, createMoney(TWO));
		newLedger.add(chargeEvent);
		newLedger.add(secondReservationEvent);
		newLedger.add(creditEvent);
		chargeRequest.setLedger(newLedger);

		final PaymentAPIResponse paymentAPIResponse = testee.chargePayment(chargeRequest);

		assertThat(paymentAPIResponse.isSuccess()).isTrue();
		assertThat(paymentAPIResponse.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED);
		assertThat(paymentAPIResponse.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(CHARGE);
		assertThat(paymentAPIResponse.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(EIGHT);
	}

	@Test
	public void shouldReturnSuccessfulResponseWithEmptyEventListWhenChargeNotFinalPaymentForSingleReservePerPIPaymentInstrument() {
		final ChargeRequest chargeRequest = createChargeRequestWithSingleReservePerPIPaymentInstrument(DEFAULT_RESERVED_AMOUNT_10_USD);
		chargeRequest.setFinalPayment(false);
		chargeRequest.setSingleReservePerPI(true);

		final PaymentAPIResponse paymentAPIResponse = testee.chargePayment(chargeRequest);

		assertThat(paymentAPIResponse.isSuccess()).isTrue();
		assertThat(paymentAPIResponse.getEvents()).isEmpty();
	}

	@Test
	public void shouldBeApprovedReserveEventFor10AndChargeEventFor10WhenChargePaymentForLedgerWithFailedChargeEventFor10() {
		final int expectedNumberPaymentEvents = 2;
		final OrderPaymentInstrumentDTO limitedOrderPaymentInstrument = createLimitedOrderPaymentInstrument(DEFAULT_RESERVED_AMOUNT_10_USD);
		final OrderPaymentInstrumentDTO unlimitedOrderPaymentInstrument = createOrderPaymentInstrumentDTO();

		final PaymentEvent firstReservationEvent = createReservationEvent(limitedOrderPaymentInstrument, DEFAULT_RESERVED_AMOUNT_10_USD);
		final PaymentEvent firstChargeEvent = createChargeEvent(firstReservationEvent, DEFAULT_RESERVED_AMOUNT_10_USD);
		final PaymentEvent secondReservationEvent = createReservationEvent(unlimitedOrderPaymentInstrument, DEFAULT_RESERVED_AMOUNT_10_USD);
		final PaymentEvent secondChargeEvent = createChargeEvent(secondReservationEvent, DEFAULT_RESERVED_AMOUNT_10_USD);
		secondChargeEvent.setPaymentStatus(FAILED);

		final List<PaymentEvent> ledger = asList(firstReservationEvent, firstChargeEvent, secondReservationEvent, secondChargeEvent);

		final OrderContext orderContext = createOrderContext();
		final ChargeRequest chargeRequest = createChargeRequest(ledger, DEFAULT_MODIFIED_AMOUNT_20_USD, orderContext,
				asList(limitedOrderPaymentInstrument, unlimitedOrderPaymentInstrument));

		final PaymentAPIResponse paymentAPIResponse = testee.chargePayment(chargeRequest);

		assertThat(paymentAPIResponse.isSuccess()).isTrue();
		assertThat(paymentAPIResponse.getEvents()).hasSize(expectedNumberPaymentEvents);
		assertThat(paymentAPIResponse.getEvents()).filteredOn(paymentEvent -> paymentEvent.getPaymentType().equals(RESERVE))
				.extracting(PaymentEvent::getPaymentStatus).containsOnly(APPROVED);
		assertThat(paymentAPIResponse.getEvents()).filteredOn(paymentEvent -> paymentEvent.getPaymentType().equals(RESERVE))
				.extracting(PaymentEvent::getAmount).extracting(MoneyDTO::getAmount).containsOnlyOnce(TEN);
		assertThat(paymentAPIResponse.getEvents()).filteredOn(paymentEvent -> paymentEvent.getPaymentType().equals(CHARGE))
				.extracting(PaymentEvent::getPaymentStatus).containsOnly(APPROVED);
		assertThat(paymentAPIResponse.getEvents()).filteredOn(paymentEvent -> paymentEvent.getPaymentType().equals(CHARGE))
				.extracting(PaymentEvent::getAmount).extracting(MoneyDTO::getAmount).containsOnlyOnce(TEN);
	}

	@Test
	public void shouldBeApprovedReserveEventsAndChargeEventFor10WhenChargePaymentForLedgerWithReverseChargeEventFor10() {
		final OrderPaymentInstrumentDTO limitedOrderPaymentInstrument = createLimitedOrderPaymentInstrument(DEFAULT_RESERVED_AMOUNT_10_USD);
		final OrderPaymentInstrumentDTO unlimitedOrderPaymentInstrument = createOrderPaymentInstrumentDTO();

		final PaymentEvent firstReservationEvent = createReservationEvent(unlimitedOrderPaymentInstrument, DEFAULT_RESERVED_AMOUNT_10_USD);
		final PaymentEvent secondReservationEvent = createReservationEvent(limitedOrderPaymentInstrument, DEFAULT_RESERVED_AMOUNT_10_USD);
		final PaymentEvent firstChargeEvent = createChargeEvent(firstReservationEvent, DEFAULT_RESERVED_AMOUNT_10_USD);
		final PaymentEvent secondChargeEvent = createChargeEvent(secondReservationEvent, DEFAULT_RESERVED_AMOUNT_10_USD);
		final PaymentEvent reverseChargeEvent = createReverseChargeEvent(secondChargeEvent);

		final List<PaymentEvent> ledger = asList(firstReservationEvent, firstChargeEvent, secondReservationEvent, secondChargeEvent,
				reverseChargeEvent);

		final OrderContext orderContext = createOrderContext();
		final ChargeRequest chargeRequest = createChargeRequest(ledger, DEFAULT_MODIFIED_AMOUNT_20_USD, orderContext,
				asList(limitedOrderPaymentInstrument, unlimitedOrderPaymentInstrument));

		final PaymentAPIResponse paymentAPIResponse = testee.chargePayment(chargeRequest);

		assertThat(paymentAPIResponse.isSuccess()).isTrue();
		assertThat(paymentAPIResponse.getEvents()).filteredOn(paymentEvent -> paymentEvent.getPaymentType().equals(RESERVE))
				.extracting(PaymentEvent::getPaymentStatus).containsOnly(APPROVED);
		assertThat(paymentAPIResponse.getEvents()).filteredOn(paymentEvent -> paymentEvent.getPaymentType().equals(CHARGE))
				.extracting(PaymentEvent::getPaymentStatus).containsOnly(APPROVED);
		assertThat(paymentAPIResponse.getEvents()).filteredOn(paymentEvent -> paymentEvent.getPaymentType().equals(CHARGE))
				.extracting(PaymentEvent::getAmount).extracting(MoneyDTO::getAmount).containsOnlyOnce(TEN);
	}

	@Test
	public void chargeWhenCapabilityThrowsException() {
		PaymentProviderPluginForIntegrationTesting.addCapability(getClass(), ChargeCapability.class, request -> {
			throw CAPABILITY_EXCEPTION;
		});

		final MoneyDTO chargedAmount = DEFAULT_RESERVED_AMOUNT_10_USD;

		final PaymentAPIResponse response = testee.chargePayment(createChargeRequest(chargedAmount));

		assertThat(response.isSuccess()).isEqualTo(false);
		assertThat(response.getInternalMessage()).isEqualTo("The request failed.");
		assertThat(response.getExternalMessage()).isEqualTo("The capability throws exception.");
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(TEN, TEN, TEN, TEN);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(CHARGE, CANCEL_RESERVE, RESERVE, CHARGE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(FAILED, APPROVED, APPROVED, FAILED);
	}

	@Test
	public void chargeWhenCapabilityUnsupported() {
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), ChargeCapability.class);

		final MoneyDTO chargedAmount = DEFAULT_RESERVED_AMOUNT_10_USD;

		assertThatThrownBy(() -> testee.chargePayment(createChargeRequest(chargedAmount)))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Mandatory plugin charge capability is absent for payment provider:");
	}

	@Test
	public void chargeWithoutChargeableEvent() {
		final MoneyDTO chargedAmount = DEFAULT_RESERVED_AMOUNT_10_USD;

		final OrderContext orderContext = createOrderContext();
		final OrderPaymentInstrumentDTO unlimitedOrderPaymentInstrument = createOrderPaymentInstrumentDTO();
		final PaymentAPIResponse response = testee.chargePayment(createChargeRequest(Collections.emptyList(), chargedAmount, orderContext,
				asList(unlimitedOrderPaymentInstrument)));

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(TEN, TEN);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(RESERVE, CHARGE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED, APPROVED);
	}

	@Test
	public void chargeWhenDoesNotEnoughMoney() {
		PaymentProviderPluginForIntegrationTesting.addCapability(getClass(), ModifyCapability.class, request -> {
			throw CAPABILITY_EXCEPTION;
		});
		final OrderContext orderContext = createOrderContext();
		final OrderPaymentInstrumentDTO unlimitedOrderPaymentInstrument = createOrderPaymentInstrumentDTO();
		final PaymentEvent firstReservationEvent = createReservationEvent(unlimitedOrderPaymentInstrument, DEFAULT_RESERVED_AMOUNT_10_USD);
		final PaymentAPIResponse response = testee.chargePayment(createChargeRequest(asList(firstReservationEvent),
				createMoney(BigDecimal.valueOf(40)), orderContext, asList(unlimitedOrderPaymentInstrument)));

		assertThat(response.isSuccess()).isEqualTo(false);
		assertThat(response.getExternalMessage()).isEqualTo("Charge request failed due to insufficient funds reserved in the Payment instrument.");
		assertThat(response.getInternalMessage()).isEqualTo("The charge failed because there was not enough amount reserved on the payment "
				+ "instrument.");
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(TEN);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(RESERVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED);
	}

	private ChargeRequest createChargeRequest(final List<PaymentEvent> ledger, final MoneyDTO totalChargeableAmount, final OrderContext orderContext,
											  final List<OrderPaymentInstrumentDTO> orderPaymentInstrumentDTOs) {
		final ChargeRequest chargeRequest = new ChargeRequest();
		chargeRequest.setLedger(ledger);
		chargeRequest.setTotalChargeableAmount(totalChargeableAmount);
		chargeRequest.setOrderContext(orderContext);
		chargeRequest.setOrderPaymentInstruments(orderPaymentInstrumentDTOs);
		chargeRequest.setCustomRequestData(Collections.emptyMap());

		return chargeRequest;
	}

}
