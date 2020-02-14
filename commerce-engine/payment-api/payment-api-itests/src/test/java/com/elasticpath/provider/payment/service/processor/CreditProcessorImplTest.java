/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.provider.payment.service.processor;

import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.APPROVED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.FAILED;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.CREDIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Test;

import com.elasticpath.plugin.payment.provider.capabilities.credit.CreditCapability;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIResponse;
import com.elasticpath.provider.payment.domain.transaction.credit.CreditRequest;
import com.elasticpath.provider.payment.service.PaymentsException;
import com.elasticpath.provider.payment.service.PaymentsExceptionMessageId;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;
import com.elasticpath.service.payment.provider.PaymentProviderPluginForIntegrationTesting;

public class CreditProcessorImplTest extends AbstractProcessorImplTestBase {

	@Inject
	@Named(PaymentProviderApiContextIdNames.CREDIT_PROCESSOR)
	private CreditProcessor testee;

	@Test
	public void refundAmountLessThanOrderAmountWithOneChargeEvent() {
		final MoneyDTO creditAmount = createMoney(THREE);
		final CreditRequest creditRequest = createCreditRequest(creditAmount);

		final PaymentAPIResponse response = testee.credit(creditRequest);

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(THREE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(CREDIT);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED);
	}

	@Test
	public void refundAmountLessThanOrderAmountWithTwoChargeEvents() {
		final MoneyDTO creditAmount = createMoney(THIRTEEN);
		final CreditRequest creditRequest = createCreditRequestWithTwoChargeEvents(creditAmount);

		final PaymentAPIResponse response = testee.credit(creditRequest);

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(TEN, THREE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(CREDIT, CREDIT);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED, APPROVED);
	}

	@Test
	public void refundAmountLessThanOrderAmountWithTwoChargeEventsAndOneExistingCredit() {
		final MoneyDTO creditAmount = createMoney(THIRTEEN);
		final CreditRequest creditRequest = createCreditRequest(creditAmount);
		final OrderPaymentInstrumentDTO limitedInstrument = createLimitedOrderPaymentInstrument(DEFAULT_RESERVED_AMOUNT_10_USD);
		final List<PaymentEvent> newLedger = new ArrayList<>(creditRequest.getLedger());
		final PaymentEvent chargeEvent = newLedger.get(1);
		final PaymentEvent secondReservation = createReservationEvent(limitedInstrument, DEFAULT_RESERVED_AMOUNT_10_USD);
		newLedger.add(secondReservation);
		newLedger.add(createCreditEvent(chargeEvent, createMoney(FIVE))); // credit against existing reservation-charge sequence
		newLedger.add(createChargeEvent(secondReservation, DEFAULT_RESERVED_AMOUNT_10_USD));
		creditRequest.setLedger(newLedger);

		final PaymentAPIResponse response = testee.credit(creditRequest);

		assertThat(response.isSuccess()).isEqualTo(true);
		final List<BigDecimal> amounts = response.getEvents()
				.stream()
				.map(event -> event.getAmount().getAmount())
				.collect(Collectors.toList());
		assertThat(amounts).hasSize(2); // it's either 5 + 8 or 10 + 3
		if (amounts.contains(FIVE)) {
			assertThat(amounts).containsExactly(FIVE, EIGHT);
		} else {
			assertThat(amounts).containsExactly(TEN, THREE);
		}
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(CREDIT, CREDIT);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED, APPROVED);
	}

	@Test
	public void refundAmountEqualThanOrderAmountWithOneChargeEvent() {
		final MoneyDTO creditAmount = createMoney(TEN);
		final CreditRequest creditRequest = createCreditRequest(creditAmount);

		final PaymentAPIResponse response = testee.credit(creditRequest);

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(TEN);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(CREDIT);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED);
	}

	@Test
	public void refundAmountEqualThanOrderAmountWithTwoChargeEvents() {
		final MoneyDTO creditAmount = createMoney(TWENTY);
		final CreditRequest creditRequest = createCreditRequestWithTwoChargeEvents(creditAmount);

		final PaymentAPIResponse response = testee.credit(creditRequest);

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(TEN, TEN);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(CREDIT, CREDIT);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED, APPROVED);
	}

	@Test(expected = PaymentsException.class)
	public void refundAmountBiggerThanChargeAmount() {
		final MoneyDTO creditAmount = createMoney(THIRTEEN);
		final CreditRequest creditRequest = createCreditRequest(creditAmount);

		testee.credit(creditRequest);
	}

	@Test(expected = PaymentsException.class)
	public void creditWhenCreditCapabilityUnsupported() {
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), CreditCapability.class);

		final MoneyDTO creditAmount = createMoney(THREE);
		final CreditRequest creditRequest = createCreditRequest(creditAmount);

		testee.credit(creditRequest);
	}

	@Test
	public void creditWhenCapabilityThrowsException() {
		PaymentProviderPluginForIntegrationTesting.addCapability(getClass(), CreditCapability.class, request -> {
			throw CAPABILITY_EXCEPTION;
		});

		final MoneyDTO creditAmount = createMoney(THREE);
		final CreditRequest creditRequest = createCreditRequest(creditAmount);

		final PaymentAPIResponse response = testee.credit(creditRequest);

		assertThat(response.isSuccess()).isEqualTo(false);
		assertThat(response.getInternalMessage()).isEqualTo("The request failed.");
		assertThat(response.getExternalMessage()).isEqualTo("The capability throws exception.");
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(THREE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(CREDIT);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(FAILED);
	}

	@Test
	public void creditWhenLedgerHasOneCredit() {
		final MoneyDTO creditAmount = createMoney(THREE);
		final CreditRequest creditRequest = createCreditRequest(creditAmount);
		final List<PaymentEvent> newLedger = new ArrayList<>(creditRequest.getLedger());
		final PaymentEvent chargeEvent = creditRequest.getLedger().get(1);
		newLedger.add(createCreditEvent(chargeEvent, creditAmount));
		creditRequest.setLedger(newLedger);

		final PaymentAPIResponse secondResponse = testee.credit(creditRequest);

		assertThat(secondResponse.isSuccess()).isEqualTo(true);
		assertThat(secondResponse.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(THREE);
		assertThat(secondResponse.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(CREDIT);
		assertThat(secondResponse.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED);
	}

	@Test
	public void creditThrownExceptionWhenAmountToRefundBiggerThanOrderTotalAndCapabilityIsSupported() {
		final CreditRequest creditRequest = createCreditRequest(createMoney(TWENTY));

		assertThatThrownBy(() -> testee.credit(creditRequest))
				.isInstanceOf(PaymentsException.class)
				.extracting(exception -> ((PaymentsException) exception).getMessageId())
				.isEqualTo(PaymentsExceptionMessageId.PAYMENT_INSUFFICIENT_FUNDS);
	}

	@Test
	public void creditThrowsExceptionWhenTryingToRefundFreeOrder() {
		final CreditRequest creditRequest = createCreditRequest(createMoney(TEN));
		creditRequest.setLedger(Collections.emptyList());

		assertThatThrownBy(() -> testee.credit(creditRequest)).isInstanceOf(PaymentsException.class);
	}

	@Test
	public void creditApprovedWhenAmountToRefundEqualThanOrderTotalMinusExistingCredit() {
		final BigDecimal halfOfReservation = DEFAULT_RESERVED_AMOUNT_10_USD.getAmount().divide(TWO, RoundingMode.HALF_UP);
		final MoneyDTO creditAmount = createMoney(halfOfReservation);
		final CreditRequest creditRequest = createCreditRequest(creditAmount);
		final List<PaymentEvent> newLedger = new ArrayList<>(creditRequest.getLedger());
		final PaymentEvent chargeEvent = creditRequest.getLedger().get(1);
		newLedger.add(createCreditEvent(chargeEvent, creditAmount));
		creditRequest.setLedger(newLedger);

		final PaymentAPIResponse response = testee.credit(creditRequest);

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentType).containsExactly(CREDIT);
		checkSinglePaymentEventApproved(response);
	}

	@Test
	public void creditThrowsExceptionWhenAmountToRefundBiggerThanOrderTotalMinusExistingCredit() {
		final CreditRequest creditRequest = createCreditRequest(createMoney(BigDecimal.ONE));
		final List<PaymentEvent> newLedger = new ArrayList<>(creditRequest.getLedger());
		final PaymentEvent chargeEvent = creditRequest.getLedger().get(1);
		newLedger.add(createCreditEvent(chargeEvent, DEFAULT_RESERVED_AMOUNT_10_USD));
		creditRequest.setLedger(newLedger);

		assertThatThrownBy(() -> testee.credit(creditRequest)).isInstanceOf(PaymentsException.class);
	}

	private CreditRequest createCreditRequestWithTwoChargeEvents(final MoneyDTO amountToRefund) {
		final CreditRequest creditRequest = createCreditRequest(amountToRefund);
		final OrderPaymentInstrumentDTO limitedOrderPaymentInstrument =
				createLimitedOrderPaymentInstrument(DEFAULT_RESERVED_AMOUNT_10_USD);
		final PaymentEvent secondReservationEvent = createReservationEvent(limitedOrderPaymentInstrument, DEFAULT_RESERVED_AMOUNT_10_USD);
		final PaymentEvent secondChargeEvent = createChargeEvent(secondReservationEvent, DEFAULT_RESERVED_AMOUNT_10_USD);
		final List<PaymentEvent> newLedger = new ArrayList<>(creditRequest.getLedger());
		newLedger.add(secondReservationEvent);
		newLedger.add(secondChargeEvent);
		creditRequest.setLedger(newLedger);
		return creditRequest;
	}

	@Test
	public void creditRequestWithZeroAmount() {
		final MoneyDTO creditAmount = createMoney(BigDecimal.ZERO);

		final PaymentAPIResponse response = testee.credit(createCreditRequest(creditAmount));

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents()).isEmpty();
	}

}