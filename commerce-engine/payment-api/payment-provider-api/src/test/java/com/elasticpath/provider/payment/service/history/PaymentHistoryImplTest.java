/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.provider.payment.service.history;

import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.APPROVED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.FAILED;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.CANCEL_RESERVE;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.CHARGE;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.CREDIT;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.MANUAL_CREDIT;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.MODIFY_RESERVE;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.RESERVE;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.REVERSE_CHARGE;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PAYMENT_EVENT_CHAIN;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PAYMENT_EVENT_RELATIONSHIP_REGISTRY;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Multimap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.PaymentStatus;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.history.handler.CancelReservePaymentEventHandler;
import com.elasticpath.provider.payment.service.history.handler.ChargePaymentEventHandler;
import com.elasticpath.provider.payment.service.history.handler.CreditPaymentEventHandler;
import com.elasticpath.provider.payment.service.history.handler.ModifyReservePaymentEventHandler;
import com.elasticpath.provider.payment.service.history.handler.PaymentEventHandler;
import com.elasticpath.provider.payment.service.history.handler.ReservePaymentEventHandler;
import com.elasticpath.provider.payment.service.history.handler.ReverseChargePaymentEventHandler;
import com.elasticpath.provider.payment.service.history.util.MoneyDtoCalculator;
import com.elasticpath.provider.payment.service.history.validator.PaymentEventValidator;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;

/**
 * Tests for {@link PaymentHistoryImpl}.
 */
@SuppressWarnings("PMD.TooManyMethods")
@RunWith(MockitoJUnitRunner.class)
public class PaymentHistoryImplTest {

	private static final Logger LOG = LogManager.getLogger(PaymentHistoryImplTest.class);

	private static final String CURRENCY_CODE = "CAD";

	private static final MoneyDTO ZERO = createMoneyDto(0, CURRENCY_CODE);
	private static final MoneyDTO TEN = createMoneyDto(10, CURRENCY_CODE);
	private static final MoneyDTO TWENTY = createMoneyDto(20, CURRENCY_CODE);
	private static final MoneyDTO THIRTY = createMoneyDto(30, CURRENCY_CODE);
	private static final MoneyDTO FORTY = createMoneyDto(40, CURRENCY_CODE);
	private static final MoneyDTO FIFTY = createMoneyDto(50, CURRENCY_CODE);
	private static final MoneyDTO SIXTY = createMoneyDto(60, CURRENCY_CODE);
	private static final MoneyDTO EIGHTY = createMoneyDto(80, CURRENCY_CODE);
	private static final MoneyDTO NINETY = createMoneyDto(90, CURRENCY_CODE);
	private static final MoneyDTO ONE_HUNDRED = createMoneyDto(100, CURRENCY_CODE);
	private static final MoneyDTO ONE_HUNDRED_TWENTY = createMoneyDto(120, CURRENCY_CODE);
	private static final MoneyDTO ONE_HUNDRED_THIRTY = createMoneyDto(130, CURRENCY_CODE);
	private static final MoneyDTO TWO_HUNDRED = createMoneyDto(200, CURRENCY_CODE);
	private static final MoneyDTO TWENTY_THOUSAND = createMoneyDto(20000, CURRENCY_CODE);
	private static final MoneyDTO TWENTY_THOUSAND_THIRTY = createMoneyDto(20030, CURRENCY_CODE);

	private static final String ORDER_PAYMENT_INSTRUMENT_GUID1 = "opi1";
	private static final String ORDER_PAYMENT_INSTRUMENT_GUID2 = "opi2";

	private PaymentHistory paymentHistory;

	private static PaymentEvent mockPaymentEvent(final String parentGuid,
												 final TransactionType paymentType,
												 final PaymentStatus paymentStatus,
												 final MoneyDTO amount,
												 final OrderPaymentInstrumentDTO orderPaymentInstrumentDTO) {
		final PaymentEvent paymentEvent = mockPaymentEvent(parentGuid, paymentType, paymentStatus, amount);
		paymentEvent.setOrderPaymentInstrumentDTO(orderPaymentInstrumentDTO);
		return paymentEvent;
	}

	private static OrderPaymentInstrumentDTO createOrderPaymentInstrumentDTO(final String orderPaymentInstrumentGuid, final MoneyDTO limit) {
		final OrderPaymentInstrumentDTO orderPaymentInstrumentDTO = new OrderPaymentInstrumentDTO();
		orderPaymentInstrumentDTO.setGUID(orderPaymentInstrumentGuid);
		orderPaymentInstrumentDTO.setLimit(limit);
		return orderPaymentInstrumentDTO;
	}

	private static PaymentEvent mockPaymentEvent(final String parentGuid,
												 final TransactionType paymentType,
												 final PaymentStatus paymentStatus,
												 final MoneyDTO amount) {
		final PaymentEvent paymentEvent = new PaymentEvent();
		paymentEvent.setGuid("guid-" + new Random().nextInt());
		paymentEvent.setParentGuid(parentGuid);
		paymentEvent.setPaymentType(paymentType);
		paymentEvent.setPaymentStatus(paymentStatus);
		paymentEvent.setAmount(amount);
		return paymentEvent;
	}

	private static MoneyDTO createMoneyDto(final long amount, final String currencyCode) {
		final MoneyDTO moneyDTO = new MoneyDTO();
		moneyDTO.setAmount(BigDecimal.valueOf(amount));
		moneyDTO.setCurrencyCode(currencyCode);
		return moneyDTO;
	}

	@Before
	public void setUp() {
		final Map<TransactionType, PaymentEventHandler> paymentEventHandlers = new HashMap<>();
		paymentEventHandlers.put(RESERVE, new ReservePaymentEventHandler());
		paymentEventHandlers.put(CHARGE, new ChargePaymentEventHandler());
		paymentEventHandlers.put(MODIFY_RESERVE, new ModifyReservePaymentEventHandler());
		paymentEventHandlers.put(CANCEL_RESERVE, new CancelReservePaymentEventHandler());
		paymentEventHandlers.put(REVERSE_CHARGE, new ReverseChargePaymentEventHandler());
		paymentEventHandlers.put(CREDIT, new CreditPaymentEventHandler());
		paymentEventHandlers.put(MANUAL_CREDIT, new CreditPaymentEventHandler());

		final MoneyDtoCalculator moneyDtoCalculator = new MoneyDtoCalculator();

		final PaymentEventValidator paymentEventValidator = list -> LOG.info("Validation of: " + list);

		final BeanFactory beanFactory = mock(BeanFactory.class);
		when(beanFactory.getPrototypeBean(PAYMENT_EVENT_CHAIN, PaymentEventChain.class)).then(invocation -> new PaymentEventChain());
		when(beanFactory.getPrototypeBean(PAYMENT_EVENT_RELATIONSHIP_REGISTRY, PaymentEventRelationshipRegistry.class))
				.then(invocation -> new PaymentEventRelationshipRegistry());

		paymentHistory = new PaymentHistoryImpl(paymentEventHandlers, moneyDtoCalculator, Collections.singletonList(paymentEventValidator),
				beanFactory);
	}

	@Test
	public void availableReservedAmountShouldBeTwoHundredAndChargedAmountShouldBeZeroWhenTwoReserveByOneHundredApproved() {
		final PaymentEvent seq1Reservation = mockPaymentEvent(null, RESERVE, APPROVED, ONE_HUNDRED);
		final PaymentEvent seq2Reservation = mockPaymentEvent(null, RESERVE, APPROVED, ONE_HUNDRED);

		final List<PaymentEvent> paymentEvents = asList(seq1Reservation, seq2Reservation);

		final MoneyDTO availableReservedAmount = paymentHistory.getAvailableReservedAmount(paymentEvents);
		final MoneyDTO chargedAmount = paymentHistory.getChargedAmount(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> chargeablePaymentInstruments = paymentHistory.getChargeablePaymentEvents(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> refundablePaymentInstruments = paymentHistory.getRefundablePaymentEvents(paymentEvents);

		assertThat(availableReservedAmount.getAmount()).isEqualTo(TWO_HUNDRED.getAmount());
		assertThat(chargedAmount.getAmount()).isEqualTo(ZERO.getAmount());
		assertThat(chargeablePaymentInstruments).extracting(Multimap::size).isEqualTo(2);
		assertThat(chargeablePaymentInstruments.get(seq1Reservation)).extracting(MoneyDTO::getAmount).containsOnly(ONE_HUNDRED.getAmount());
		assertThat(chargeablePaymentInstruments.get(seq2Reservation)).extracting(MoneyDTO::getAmount).containsOnly(ONE_HUNDRED.getAmount());
		assertThat(refundablePaymentInstruments).extracting(Multimap::size).isEqualTo(0);
	}

	@Test
	public void availableReservedAmountShouldBeOneHundredTwentyAndChargedAmountShouldBeZeroWhenModifyReserveWithOneHundredTwentyApproved() {
		final PaymentEvent seq1Reservation = mockPaymentEvent(null, RESERVE, APPROVED, ONE_HUNDRED);
		final PaymentEvent seq1Modification = mockPaymentEvent(seq1Reservation.getGuid(), MODIFY_RESERVE, APPROVED, ONE_HUNDRED_TWENTY);

		final List<PaymentEvent> paymentEvents = asList(seq1Reservation, seq1Modification);

		final MoneyDTO availableReservedAmount = paymentHistory.getAvailableReservedAmount(paymentEvents);
		final MoneyDTO chargedAmount = paymentHistory.getChargedAmount(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> chargeablePaymentInstruments = paymentHistory.getChargeablePaymentEvents(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> refundablePaymentInstruments = paymentHistory.getRefundablePaymentEvents(paymentEvents);

		assertThat(availableReservedAmount.getAmount()).isEqualTo(ONE_HUNDRED_TWENTY.getAmount());
		assertThat(chargedAmount.getAmount()).isEqualTo(ZERO.getAmount());
		assertThat(chargeablePaymentInstruments).extracting(Multimap::size).isEqualTo(1);
		assertThat(chargeablePaymentInstruments.get(seq1Modification)).extracting(MoneyDTO::getAmount).containsOnly(ONE_HUNDRED_TWENTY.getAmount());
		assertThat(refundablePaymentInstruments).extracting(Multimap::size).isEqualTo(0);
	}

	@Test
	public void availableReservedAmountShouldBeZeroAndChargedAmountShouldBeZeroWhenCancelReserveApproved() {
		final PaymentEvent seq1Reservation = mockPaymentEvent(null, RESERVE, APPROVED, ONE_HUNDRED);
		final PaymentEvent seq1Cancel = mockPaymentEvent(seq1Reservation.getGuid(), CANCEL_RESERVE, APPROVED, ONE_HUNDRED);

		final List<PaymentEvent> paymentEvents = asList(seq1Reservation, seq1Cancel);

		final MoneyDTO availableReservedAmount = paymentHistory.getAvailableReservedAmount(paymentEvents);
		final MoneyDTO chargedAmount = paymentHistory.getChargedAmount(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> chargeablePaymentInstruments = paymentHistory.getChargeablePaymentEvents(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> refundablePaymentInstruments = paymentHistory.getRefundablePaymentEvents(paymentEvents);

		assertThat(availableReservedAmount.getAmount()).isEqualTo(ZERO.getAmount());
		assertThat(chargedAmount.getAmount()).isEqualTo(ZERO.getAmount());
		assertThat(chargeablePaymentInstruments).extracting(Multimap::size).isEqualTo(0);
		assertThat(refundablePaymentInstruments).extracting(Multimap::size).isEqualTo(0);
	}

	@Test
	public void availableReservedAmountShouldBeZeroAndChargedAmountShouldBeZeroWhenReserveAndChargeEventsFailed() {
		final PaymentEvent seq1Reservation = mockPaymentEvent(null, RESERVE, APPROVED, ONE_HUNDRED);
		final PaymentEvent seq2FailedReservation = mockPaymentEvent(null, RESERVE, FAILED, ONE_HUNDRED);
		final PaymentEvent seq1Charge = mockPaymentEvent(seq1Reservation.getGuid(), CHARGE, FAILED, ONE_HUNDRED);

		final List<PaymentEvent> paymentEvents = asList(seq1Reservation, seq2FailedReservation, seq1Charge);

		final MoneyDTO availableReservedAmount = paymentHistory.getAvailableReservedAmount(paymentEvents);
		final MoneyDTO chargedAmount = paymentHistory.getChargedAmount(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> chargeablePaymentInstruments = paymentHistory.getChargeablePaymentEvents(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> refundablePaymentInstruments = paymentHistory.getRefundablePaymentEvents(paymentEvents);

		assertThat(availableReservedAmount.getAmount()).isEqualTo(ZERO.getAmount());
		assertThat(chargedAmount.getAmount()).isEqualTo(ZERO.getAmount());
		assertThat(chargeablePaymentInstruments).extracting(Multimap::size).isEqualTo(0);
		assertThat(refundablePaymentInstruments).extracting(Multimap::size).isEqualTo(0);
	}

	@Test
	public void availableReservedAmountShouldBeZeroAndChargedAmountShouldBeTwoHundredWhenTwoChargeEventsApproved() {
		final PaymentEvent seq1Reservation = mockPaymentEvent(null, RESERVE, APPROVED, ONE_HUNDRED);
		final PaymentEvent seq2Reservation = mockPaymentEvent(null, RESERVE, APPROVED, ONE_HUNDRED);
		final PaymentEvent seq1Charge = mockPaymentEvent(seq1Reservation.getGuid(), CHARGE, APPROVED, ONE_HUNDRED);
		final PaymentEvent seq2Charge = mockPaymentEvent(seq2Reservation.getGuid(), CHARGE, APPROVED, ONE_HUNDRED);

		final List<PaymentEvent> paymentEvents = asList(seq1Reservation, seq2Reservation, seq1Charge, seq2Charge);

		final MoneyDTO availableReservedAmount = paymentHistory.getAvailableReservedAmount(paymentEvents);
		final MoneyDTO chargedAmount = paymentHistory.getChargedAmount(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> refundablePaymentInstruments = paymentHistory.getRefundablePaymentEvents(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> chargeablePaymentInstruments = paymentHistory.getChargeablePaymentEvents(paymentEvents);

		assertThat(availableReservedAmount.getAmount()).isEqualTo(ZERO.getAmount());
		assertThat(chargedAmount.getAmount()).isEqualTo(TWO_HUNDRED.getAmount());
		assertThat(chargeablePaymentInstruments).extracting(Multimap::size).isEqualTo(0);
		assertThat(refundablePaymentInstruments).extracting(Multimap::size).isEqualTo(2);
		assertThat(refundablePaymentInstruments.get(seq1Charge)).extracting(MoneyDTO::getAmount).containsOnly(ONE_HUNDRED.getAmount());
		assertThat(refundablePaymentInstruments.get(seq2Charge)).extracting(MoneyDTO::getAmount).containsOnly(ONE_HUNDRED.getAmount());
	}

	@Test
	public void availableReservedAmountShouldBeZeroAndChargedAmountShouldBeEightyWhenOneChargeEventsApproved() {
		final PaymentEvent seq1Reservation = mockPaymentEvent(null, RESERVE, APPROVED, ONE_HUNDRED);
		final PaymentEvent seq2Reservation = mockPaymentEvent(null, RESERVE, APPROVED, ONE_HUNDRED);
		final PaymentEvent seq1FailedCharge = mockPaymentEvent(seq1Reservation.getGuid(), CHARGE, FAILED, ONE_HUNDRED);
		final PaymentEvent seq2Charge = mockPaymentEvent(seq2Reservation.getGuid(), CHARGE, APPROVED, EIGHTY);

		final List<PaymentEvent> paymentEvents = asList(seq1Reservation, seq2Reservation, seq1FailedCharge, seq2Charge);

		final MoneyDTO availableReservedAmount = paymentHistory.getAvailableReservedAmount(paymentEvents);
		final MoneyDTO chargedAmount = paymentHistory.getChargedAmount(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> chargeablePaymentInstruments = paymentHistory.getChargeablePaymentEvents(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> refundablePaymentInstruments = paymentHistory.getRefundablePaymentEvents(paymentEvents);

		assertThat(availableReservedAmount.getAmount()).isEqualTo(ZERO.getAmount());
		assertThat(chargedAmount.getAmount()).isEqualTo(EIGHTY.getAmount());
		assertThat(chargeablePaymentInstruments).extracting(Multimap::size).isEqualTo(0);
		assertThat(refundablePaymentInstruments).extracting(Multimap::size).isEqualTo(1);
		assertThat(refundablePaymentInstruments.get(seq2Charge)).extracting(MoneyDTO::getAmount).containsOnly(EIGHTY.getAmount());
	}

	@Test
	public void getPaymentHistoryWhenModifyToOrderToIncreaseReservedAmount() {
		final int expectedCountOfRefundablePaymentInstruments = 3;

		final PaymentEvent seq1Reservation = mockPaymentEvent(null, RESERVE, APPROVED, ONE_HUNDRED);
		final PaymentEvent seq2FailedReservation = mockPaymentEvent(null, RESERVE, FAILED, ONE_HUNDRED);
		final PaymentEvent seq3Reservation = mockPaymentEvent(null, RESERVE, APPROVED, ONE_HUNDRED);
		final PaymentEvent seq1Modification = mockPaymentEvent(seq1Reservation.getGuid(), MODIFY_RESERVE, APPROVED, ONE_HUNDRED_TWENTY);
		final PaymentEvent seq1Charge = mockPaymentEvent(seq1Modification.getGuid(), CHARGE, APPROVED, SIXTY);
		final PaymentEvent seq4Reservation = mockPaymentEvent(null, RESERVE, APPROVED, SIXTY);
		final PaymentEvent seq4Charge = mockPaymentEvent(seq4Reservation.getGuid(), CHARGE, APPROVED, SIXTY);
		final PaymentEvent seq3Charge = mockPaymentEvent(seq3Reservation.getGuid(), CHARGE, APPROVED, TEN);
		final PaymentEvent seq5Reservation = mockPaymentEvent(null, RESERVE, APPROVED, NINETY);

		final List<PaymentEvent> paymentEvents = asList(
				seq1Reservation, seq2FailedReservation, seq3Reservation, seq1Modification, seq1Charge,
				seq4Reservation, seq4Charge, seq3Charge, seq5Reservation);

		final MoneyDTO availableReservedAmount = paymentHistory.getAvailableReservedAmount(paymentEvents);
		final MoneyDTO chargedAmount = paymentHistory.getChargedAmount(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> chargeablePaymentInstruments = paymentHistory.getChargeablePaymentEvents(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> refundablePaymentInstruments = paymentHistory.getRefundablePaymentEvents(paymentEvents);

		assertThat(availableReservedAmount.getAmount()).isEqualTo(NINETY.getAmount());
		assertThat(chargedAmount.getAmount()).isEqualTo(ONE_HUNDRED_THIRTY.getAmount());
		assertThat(chargeablePaymentInstruments).extracting(Multimap::size).isEqualTo(1);
		assertThat(chargeablePaymentInstruments.get(seq5Reservation)).extracting(MoneyDTO::getAmount).containsOnly(NINETY.getAmount());
		assertThat(refundablePaymentInstruments).extracting(Multimap::size).isEqualTo(expectedCountOfRefundablePaymentInstruments);
		assertThat(refundablePaymentInstruments.get(seq1Charge)).extracting(MoneyDTO::getAmount).containsOnly(SIXTY.getAmount());
		assertThat(refundablePaymentInstruments.get(seq4Charge)).extracting(MoneyDTO::getAmount).containsOnly(SIXTY.getAmount());
		assertThat(refundablePaymentInstruments.get(seq3Charge)).extracting(MoneyDTO::getAmount).containsOnly(TEN.getAmount());
	}

	@Test
	public void getPaymentHistoryWhenModifyToOrderToDecreaseReservedAmount() {
		final int expectedCountOfRefundablePaymentInstruments = 3;

		final PaymentEvent seq1Reservation = mockPaymentEvent(null, RESERVE, APPROVED, ONE_HUNDRED);
		final PaymentEvent seq2FailedReservation = mockPaymentEvent(null, RESERVE, FAILED, ONE_HUNDRED);
		final PaymentEvent seq3Reservation = mockPaymentEvent(null, RESERVE, APPROVED, ONE_HUNDRED);
		final PaymentEvent seq1Modification = mockPaymentEvent(seq1Reservation.getGuid(), MODIFY_RESERVE, APPROVED, EIGHTY);
		final PaymentEvent seq1FailedCharge = mockPaymentEvent(seq1Modification.getGuid(), CHARGE, FAILED, SIXTY);
		final PaymentEvent seq4Reservation = mockPaymentEvent(null, RESERVE, APPROVED, TWENTY_THOUSAND);
		final PaymentEvent seq4Charge = mockPaymentEvent(seq4Reservation.getGuid(), CHARGE, APPROVED, TWENTY_THOUSAND);
		final PaymentEvent seq5Reservation = mockPaymentEvent(null, RESERVE, APPROVED, TWENTY);
		final PaymentEvent seq5Charge = mockPaymentEvent(seq5Reservation.getGuid(), CHARGE, APPROVED, TWENTY);
		final PaymentEvent seq3Charge = mockPaymentEvent(seq3Reservation.getGuid(), CHARGE, APPROVED, TEN);
		final PaymentEvent seq6Reservation = mockPaymentEvent(null, RESERVE, APPROVED, NINETY);

		final List<PaymentEvent> paymentEvents = asList(
				seq1Reservation, seq2FailedReservation, seq3Reservation, seq1Modification, seq1FailedCharge,
				seq4Reservation, seq4Charge, seq5Reservation, seq5Charge, seq3Charge, seq6Reservation);

		final MoneyDTO availableReservedAmount = paymentHistory.getAvailableReservedAmount(paymentEvents);
		final MoneyDTO chargedAmount = paymentHistory.getChargedAmount(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> chargeablePaymentInstruments = paymentHistory.getChargeablePaymentEvents(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> refundablePaymentInstruments = paymentHistory.getRefundablePaymentEvents(paymentEvents);

		assertThat(availableReservedAmount.getAmount()).isEqualTo(NINETY.getAmount());
		assertThat(chargedAmount.getAmount()).isEqualTo(TWENTY_THOUSAND_THIRTY.getAmount());
		assertThat(chargeablePaymentInstruments).extracting(Multimap::size).isEqualTo(1);
		assertThat(chargeablePaymentInstruments.get(seq6Reservation)).extracting(MoneyDTO::getAmount).containsOnly(NINETY.getAmount());
		assertThat(refundablePaymentInstruments).extracting(Multimap::size).isEqualTo(expectedCountOfRefundablePaymentInstruments);
		assertThat(refundablePaymentInstruments.get(seq4Charge)).extracting(MoneyDTO::getAmount).containsOnly(TWENTY_THOUSAND.getAmount());
		assertThat(refundablePaymentInstruments.get(seq5Charge)).extracting(MoneyDTO::getAmount).containsOnly(TWENTY.getAmount());
		assertThat(refundablePaymentInstruments.get(seq3Charge)).extracting(MoneyDTO::getAmount).containsOnly(TEN.getAmount());
	}

	@Test
	public void availableReservedAmountShouldBeZeroAndChargedAmountShouldBeOneHundredWhenReserveByOneHundredAndChargeAndCreditByFiftyAreApproved() {
		final PaymentEvent seq1Reservation = mockPaymentEvent(null, RESERVE, APPROVED, ONE_HUNDRED);
		final PaymentEvent seq1Charge = mockPaymentEvent(seq1Reservation.getGuid(), CHARGE, APPROVED, ONE_HUNDRED);
		final PaymentEvent seq1Credit = mockPaymentEvent(seq1Charge.getGuid(), CREDIT, APPROVED, FIFTY);

		final List<PaymentEvent> paymentEvents = asList(seq1Reservation, seq1Charge, seq1Credit);

		final MoneyDTO availableReservedAmount = paymentHistory.getAvailableReservedAmount(paymentEvents);
		final MoneyDTO chargedAmount = paymentHistory.getChargedAmount(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> refundablePaymentEvents = paymentHistory.getRefundablePaymentEvents(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> chargeablePaymentEvents = paymentHistory.getChargeablePaymentEvents(paymentEvents);

		assertThat(availableReservedAmount.getAmount()).isEqualTo(ZERO.getAmount());
		assertThat(chargedAmount.getAmount()).isEqualTo(ONE_HUNDRED.getAmount());
		assertThat(chargeablePaymentEvents).extracting(Multimap::size).isEqualTo(0);
		assertThat(refundablePaymentEvents).extracting(Multimap::size).isEqualTo(1);
		assertThat(refundablePaymentEvents.get(seq1Charge)).extracting(MoneyDTO::getAmount).containsOnly(FIFTY.getAmount());
	}

	@Test
	public void availableReservedAmountShouldBeZeroAndChargedAmountShouldBeZeroWhenReserveByOneHundredAndChargeAndReverseChargeAreApproved() {
		final PaymentEvent seq1Reservation = mockPaymentEvent(null, RESERVE, APPROVED, ONE_HUNDRED);
		final PaymentEvent seq1Charge = mockPaymentEvent(seq1Reservation.getGuid(), CHARGE, APPROVED, ONE_HUNDRED);
		final PaymentEvent seq1ReverseCharge = mockPaymentEvent(seq1Charge.getGuid(), REVERSE_CHARGE, APPROVED, ONE_HUNDRED);

		final List<PaymentEvent> paymentEvents = asList(seq1Reservation, seq1Charge, seq1ReverseCharge);

		final MoneyDTO availableReservedAmount = paymentHistory.getAvailableReservedAmount(paymentEvents);
		final MoneyDTO chargedAmount = paymentHistory.getChargedAmount(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> chargeablePaymentInstruments = paymentHistory.getChargeablePaymentEvents(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> refundablePaymentInstruments = paymentHistory.getRefundablePaymentEvents(paymentEvents);

		assertThat(availableReservedAmount.getAmount()).isEqualTo(ZERO.getAmount());
		assertThat(chargedAmount.getAmount()).isEqualTo(ZERO.getAmount());
		assertThat(chargeablePaymentInstruments).extracting(Multimap::size).isEqualTo(0);
		assertThat(refundablePaymentInstruments).extracting(Multimap::size).isEqualTo(0);
	}

	@Test
	public void manualCreditBehavesExactlyAsRegularCredit() {
		final PaymentEvent seq1Reservation = mockPaymentEvent(null, RESERVE, APPROVED, ONE_HUNDRED);
		final PaymentEvent seq1Charge = mockPaymentEvent(seq1Reservation.getGuid(), CHARGE, APPROVED, ONE_HUNDRED);
		final PaymentEvent seq1ManualCredit = mockPaymentEvent(seq1Charge.getGuid(), MANUAL_CREDIT, APPROVED, FIFTY);
		final List<PaymentEvent> paymentEvents = asList(seq1Reservation, seq1Charge, seq1ManualCredit);

		final MoneyDTO availableReservedAmount = paymentHistory.getAvailableReservedAmount(paymentEvents);
		final MoneyDTO chargedAmount = paymentHistory.getChargedAmount(paymentEvents);
		final MoneyDTO refundedAmount = paymentHistory.getRefundedAmount(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> refundablePaymentInstruments = paymentHistory.getRefundablePaymentEvents(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> chargeablePaymentInstruments = paymentHistory.getChargeablePaymentEvents(paymentEvents);

		assertThat(availableReservedAmount.getAmount()).isEqualTo(ZERO.getAmount());
		assertThat(chargedAmount.getAmount()).isEqualTo(ONE_HUNDRED.getAmount());
		assertThat(refundedAmount.getAmount()).isEqualTo(FIFTY.getAmount());
		assertThat(chargeablePaymentInstruments).extracting(Multimap::size).isEqualTo(0);
		assertThat(refundablePaymentInstruments).extracting(Multimap::size).isEqualTo(1);
		assertThat(refundablePaymentInstruments.get(seq1Charge)).extracting(MoneyDTO::getAmount).containsOnly(FIFTY.getAmount());
	}

	@Test
	public void manualCreditAndRegularCreditAddUp() {
		final PaymentEvent seq1Reservation = mockPaymentEvent(null, RESERVE, APPROVED, ONE_HUNDRED);
		final PaymentEvent seq1Charge = mockPaymentEvent(seq1Reservation.getGuid(), CHARGE, APPROVED, ONE_HUNDRED);
		final PaymentEvent seq1ManualCredit = mockPaymentEvent(seq1Charge.getGuid(), MANUAL_CREDIT, APPROVED, FIFTY);
		final PaymentEvent seq1Credit = mockPaymentEvent(seq1ManualCredit.getGuid(), CREDIT, APPROVED, FIFTY);
		final List<PaymentEvent> paymentEvents = asList(seq1Reservation, seq1Charge, seq1ManualCredit, seq1Credit);

		final MoneyDTO availableReservedAmount = paymentHistory.getAvailableReservedAmount(paymentEvents);
		final MoneyDTO chargedAmount = paymentHistory.getChargedAmount(paymentEvents);
		final MoneyDTO refundedAmount = paymentHistory.getRefundedAmount(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> refundablePaymentInstruments = paymentHistory.getRefundablePaymentEvents(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> chargeablePaymentInstruments = paymentHistory.getChargeablePaymentEvents(paymentEvents);

		assertThat(availableReservedAmount.getAmount()).isEqualTo(ZERO.getAmount());
		assertThat(chargedAmount.getAmount()).isEqualTo(ONE_HUNDRED.getAmount());
		assertThat(refundedAmount.getAmount()).isEqualTo(ONE_HUNDRED.getAmount());
		assertThat(chargeablePaymentInstruments).extracting(Multimap::size).isEqualTo(0);
		assertThat(refundablePaymentInstruments).extracting(Multimap::size).isEqualTo(0);
	}

	@Test
	public void reservableOrderPaymentInstrumentsShouldContainLimitedAndUnlimitedOrderPaymentInstrumentWhenReserveApprovedAndChargeFailed() {
		final OrderPaymentInstrumentDTO unlimitedOrderPaymentInstrument = createOrderPaymentInstrumentDTO(ORDER_PAYMENT_INSTRUMENT_GUID1, ZERO);
		final OrderPaymentInstrumentDTO limitedOrderPaymentInstrument = createOrderPaymentInstrumentDTO(ORDER_PAYMENT_INSTRUMENT_GUID2, FIFTY);

		final PaymentEvent reservationEvent1 = mockPaymentEvent(null, RESERVE, APPROVED, FIFTY, unlimitedOrderPaymentInstrument);
		final PaymentEvent reservationEvent2 = mockPaymentEvent(null, RESERVE, APPROVED, FIFTY, limitedOrderPaymentInstrument);
		final PaymentEvent approvedChargeEvent = mockPaymentEvent(reservationEvent1.getGuid(), CHARGE, APPROVED, FIFTY,
				unlimitedOrderPaymentInstrument);
		final PaymentEvent failedChargeEvent = mockPaymentEvent(reservationEvent2.getGuid(), CHARGE, FAILED, FIFTY, limitedOrderPaymentInstrument);

		final Map<OrderPaymentInstrumentDTO, MoneyDTO> reservableOrderPaymentInstruments = paymentHistory.getReservableOrderPaymentInstruments(
				asList(reservationEvent1, reservationEvent2, approvedChargeEvent, failedChargeEvent),
				asList(unlimitedOrderPaymentInstrument, limitedOrderPaymentInstrument));

		assertThat(reservableOrderPaymentInstruments).hasSize(2);
		assertThat(reservableOrderPaymentInstruments.get(unlimitedOrderPaymentInstrument)).extracting(MoneyDTO::getAmount)
				.isEqualTo(ZERO.getAmount());
		assertThat(reservableOrderPaymentInstruments.get(limitedOrderPaymentInstrument)).extracting(MoneyDTO::getAmount).isEqualTo(FIFTY.getAmount());
	}

	@Test
	public void reservableOrderPaymentInstrumentsShouldContainOnlyUnlimitedOrderPaymentInstrumentWhenReserveApprovedAndChargeApproved() {
		final OrderPaymentInstrumentDTO unlimitedOrderPaymentInstrument = createOrderPaymentInstrumentDTO(ORDER_PAYMENT_INSTRUMENT_GUID1, ZERO);
		final OrderPaymentInstrumentDTO limitedOrderPaymentInstrument = createOrderPaymentInstrumentDTO(ORDER_PAYMENT_INSTRUMENT_GUID2, FIFTY);

		final PaymentEvent reservationEvent1 = mockPaymentEvent(null, RESERVE, APPROVED, FIFTY, unlimitedOrderPaymentInstrument);
		final PaymentEvent reservationEvent2 = mockPaymentEvent(null, RESERVE, APPROVED, FIFTY, limitedOrderPaymentInstrument);
		final PaymentEvent chargeEvent1 = mockPaymentEvent(reservationEvent1.getGuid(), CHARGE, APPROVED, FIFTY, unlimitedOrderPaymentInstrument);
		final PaymentEvent chargeEvent2 = mockPaymentEvent(reservationEvent2.getGuid(), CHARGE, APPROVED, FIFTY, limitedOrderPaymentInstrument);

		final Map<OrderPaymentInstrumentDTO, MoneyDTO> reservableOrderPaymentInstruments = paymentHistory.getReservableOrderPaymentInstruments(
				asList(reservationEvent1, reservationEvent2, chargeEvent1, chargeEvent2),
				asList(unlimitedOrderPaymentInstrument, limitedOrderPaymentInstrument));

		assertThat(reservableOrderPaymentInstruments).hasSize(1);
		assertThat(reservableOrderPaymentInstruments.get(unlimitedOrderPaymentInstrument)).extracting(MoneyDTO::getAmount)
				.isEqualTo(ZERO.getAmount());
	}

	@Test
	public void reservableAmountShouldBeFiftyWhenSuccessfullyReserveAndChargeAndCreditFifty() {
		final OrderPaymentInstrumentDTO unlimitedOrderPaymentInstrument = createOrderPaymentInstrumentDTO(ORDER_PAYMENT_INSTRUMENT_GUID1, ZERO);
		final OrderPaymentInstrumentDTO limitedOrderPaymentInstrument = createOrderPaymentInstrumentDTO(ORDER_PAYMENT_INSTRUMENT_GUID2, FIFTY);

		final PaymentEvent reservationEvent1 = mockPaymentEvent(null, RESERVE, APPROVED, FIFTY, unlimitedOrderPaymentInstrument);
		final PaymentEvent reservationEvent2 = mockPaymentEvent(null, RESERVE, APPROVED, FIFTY, limitedOrderPaymentInstrument);
		final PaymentEvent chargeEvent1 = mockPaymentEvent(reservationEvent1.getGuid(), CHARGE, APPROVED, FIFTY, unlimitedOrderPaymentInstrument);
		final PaymentEvent chargeEvent2 = mockPaymentEvent(reservationEvent2.getGuid(), CHARGE, APPROVED, FIFTY, limitedOrderPaymentInstrument);
		final PaymentEvent creditEvent = mockPaymentEvent(chargeEvent2.getGuid(), CREDIT, APPROVED, TWENTY, limitedOrderPaymentInstrument);

		final Map<OrderPaymentInstrumentDTO, MoneyDTO> reservableOrderPaymentInstruments = paymentHistory.getReservableOrderPaymentInstruments(
				asList(reservationEvent1, reservationEvent2, chargeEvent1, chargeEvent2, creditEvent),
				asList(unlimitedOrderPaymentInstrument, limitedOrderPaymentInstrument));

		assertThat(reservableOrderPaymentInstruments).hasSize(2);
		assertThat(reservableOrderPaymentInstruments.get(unlimitedOrderPaymentInstrument)).extracting(MoneyDTO::getAmount)
				.isEqualTo(ZERO.getAmount());
		assertThat(reservableOrderPaymentInstruments.get(limitedOrderPaymentInstrument)).extracting(MoneyDTO::getAmount)
				.isEqualTo(TWENTY.getAmount());
	}

	@Test
	public void reservableAmountShouldBeFiftyWhenSuccessfullyReserveAndChargeAndReverseChargeFifty() {
		final OrderPaymentInstrumentDTO unlimitedOrderPaymentInstrument = createOrderPaymentInstrumentDTO(ORDER_PAYMENT_INSTRUMENT_GUID1, ZERO);
		final OrderPaymentInstrumentDTO limitedOrderPaymentInstrument = createOrderPaymentInstrumentDTO(ORDER_PAYMENT_INSTRUMENT_GUID2, FIFTY);

		final PaymentEvent reservationEvent1 = mockPaymentEvent(null, RESERVE, APPROVED, FIFTY, unlimitedOrderPaymentInstrument);
		final PaymentEvent reservationEvent2 = mockPaymentEvent(null, RESERVE, APPROVED, FIFTY, limitedOrderPaymentInstrument);
		final PaymentEvent chargeEvent1 = mockPaymentEvent(reservationEvent1.getGuid(), CHARGE, APPROVED, FIFTY, unlimitedOrderPaymentInstrument);
		final PaymentEvent chargeEvent2 = mockPaymentEvent(reservationEvent2.getGuid(), CHARGE, APPROVED, FIFTY, limitedOrderPaymentInstrument);
		final PaymentEvent reverseChargeEvent = mockPaymentEvent(chargeEvent2.getGuid(), REVERSE_CHARGE, APPROVED, FIFTY,
				limitedOrderPaymentInstrument);

		final Map<OrderPaymentInstrumentDTO, MoneyDTO> reservableOrderPaymentInstruments = paymentHistory.getReservableOrderPaymentInstruments(
				asList(reservationEvent1, reservationEvent2, chargeEvent1, chargeEvent2, reverseChargeEvent),
				asList(unlimitedOrderPaymentInstrument, limitedOrderPaymentInstrument));

		assertThat(reservableOrderPaymentInstruments).hasSize(2);
		assertThat(reservableOrderPaymentInstruments.get(unlimitedOrderPaymentInstrument)).extracting(MoneyDTO::getAmount)
				.isEqualTo(ZERO.getAmount());
		assertThat(reservableOrderPaymentInstruments.get(limitedOrderPaymentInstrument)).extracting(MoneyDTO::getAmount).isEqualTo(FIFTY.getAmount());
	}

	@Test
	public void reservableAmountShouldBeFiftyForLimitedOrderPaymentInstrumentWhenSuccessfullyReverseChargeFifty() {
		final OrderPaymentInstrumentDTO unlimitedOrderPaymentInstrument = createOrderPaymentInstrumentDTO(ORDER_PAYMENT_INSTRUMENT_GUID1, ZERO);
		final OrderPaymentInstrumentDTO limitedOrderPaymentInstrument = createOrderPaymentInstrumentDTO(ORDER_PAYMENT_INSTRUMENT_GUID2, FIFTY);

		final PaymentEvent reservationEvent = mockPaymentEvent(null, RESERVE, APPROVED, FIFTY, limitedOrderPaymentInstrument);
		final PaymentEvent chargeEvent = mockPaymentEvent(reservationEvent.getGuid(), CHARGE, APPROVED, FIFTY, limitedOrderPaymentInstrument);
		final PaymentEvent reverseChargeEvent = mockPaymentEvent(chargeEvent.getGuid(), REVERSE_CHARGE, APPROVED, FIFTY,
				limitedOrderPaymentInstrument);

		final Map<OrderPaymentInstrumentDTO, MoneyDTO> reservableOrderPaymentInstruments = paymentHistory.getReservableOrderPaymentInstruments(
				asList(reservationEvent, chargeEvent, reverseChargeEvent),
				asList(unlimitedOrderPaymentInstrument, limitedOrderPaymentInstrument));

		assertThat(reservableOrderPaymentInstruments).hasSize(2);
		assertThat(reservableOrderPaymentInstruments.get(unlimitedOrderPaymentInstrument)).extracting(MoneyDTO::getAmount)
				.isEqualTo(ZERO.getAmount());
		assertThat(reservableOrderPaymentInstruments.get(limitedOrderPaymentInstrument)).extracting(MoneyDTO::getAmount)
				.isEqualTo(FIFTY.getAmount());
	}

	@Test
	public void reservableOrderPaymentInstrumentsShouldContainUnlimitedOrderPaymentInstrumentAndLimitedOrderPaymentInstrument() {
		final OrderPaymentInstrumentDTO unlimitedOrderPaymentInstrument = createOrderPaymentInstrumentDTO(ORDER_PAYMENT_INSTRUMENT_GUID1, ZERO);
		final OrderPaymentInstrumentDTO limitedOrderPaymentInstrument = createOrderPaymentInstrumentDTO(ORDER_PAYMENT_INSTRUMENT_GUID2, FIFTY);

		final PaymentEvent reservationEvent = mockPaymentEvent(null, RESERVE, APPROVED, FIFTY, unlimitedOrderPaymentInstrument);
		final PaymentEvent chargeEvent = mockPaymentEvent(reservationEvent.getGuid(), CHARGE, APPROVED, FIFTY, unlimitedOrderPaymentInstrument);

		final Map<OrderPaymentInstrumentDTO, MoneyDTO> reservableOrderPaymentInstruments = paymentHistory.getReservableOrderPaymentInstruments(
				asList(reservationEvent, chargeEvent),
				asList(unlimitedOrderPaymentInstrument, limitedOrderPaymentInstrument));

		assertThat(reservableOrderPaymentInstruments).hasSize(2);
		assertThat(reservableOrderPaymentInstruments.get(unlimitedOrderPaymentInstrument)).extracting(MoneyDTO::getAmount)
				.isEqualTo(ZERO.getAmount());
		assertThat(reservableOrderPaymentInstruments.get(limitedOrderPaymentInstrument)).extracting(MoneyDTO::getAmount)
				.isEqualTo(FIFTY.getAmount());
	}

	@Test
	public void reservableAmountShouldBeThirtyWhenOrderPaymentInstrumentHasLimitFiftyAndSuccessfullyReserveTwenty() {
		final OrderPaymentInstrumentDTO unlimitedOrderPaymentInstrument = createOrderPaymentInstrumentDTO(ORDER_PAYMENT_INSTRUMENT_GUID1, ZERO);
		final OrderPaymentInstrumentDTO limitedOrderPaymentInstrument = createOrderPaymentInstrumentDTO(ORDER_PAYMENT_INSTRUMENT_GUID2, FIFTY);

		final PaymentEvent reservationEvent1 = mockPaymentEvent(null, RESERVE, APPROVED, FIFTY, unlimitedOrderPaymentInstrument);
		final PaymentEvent reservationEvent2 = mockPaymentEvent(null, RESERVE, APPROVED, TWENTY, limitedOrderPaymentInstrument);
		final PaymentEvent chargeEvent = mockPaymentEvent(reservationEvent1.getGuid(), CHARGE, APPROVED, FORTY, unlimitedOrderPaymentInstrument);

		final Map<OrderPaymentInstrumentDTO, MoneyDTO> reservableOrderPaymentInstruments = paymentHistory.getReservableOrderPaymentInstruments(
				asList(reservationEvent1, reservationEvent2, chargeEvent),
				asList(unlimitedOrderPaymentInstrument, limitedOrderPaymentInstrument));

		assertThat(reservableOrderPaymentInstruments).hasSize(2);
		assertThat(reservableOrderPaymentInstruments.get(unlimitedOrderPaymentInstrument)).extracting(MoneyDTO::getAmount)
				.isEqualTo(ZERO.getAmount());
		assertThat(reservableOrderPaymentInstruments.get(limitedOrderPaymentInstrument)).extracting(MoneyDTO::getAmount)
				.isEqualTo(THIRTY.getAmount());
	}

	@Test
	public void reservableAmountShouldBeLimitMinusReservedAndChargedForLimitedInstrumentAndZeroForUnlimited() {
		final OrderPaymentInstrumentDTO unlimitedOrderPaymentInstrument = createOrderPaymentInstrumentDTO(ORDER_PAYMENT_INSTRUMENT_GUID1, ZERO);
		final OrderPaymentInstrumentDTO limitedOrderPaymentInstrument = createOrderPaymentInstrumentDTO(ORDER_PAYMENT_INSTRUMENT_GUID2, FIFTY);

		final PaymentEvent reservationEvent1 = mockPaymentEvent(null, RESERVE, APPROVED, FIFTY, unlimitedOrderPaymentInstrument);
		final PaymentEvent reservationEvent2 = mockPaymentEvent(null, RESERVE, APPROVED, TWENTY, limitedOrderPaymentInstrument);
		final PaymentEvent chargeEvent1 = mockPaymentEvent(reservationEvent1.getGuid(), CHARGE, APPROVED, FORTY, unlimitedOrderPaymentInstrument);
		final PaymentEvent chargeEvent2 = mockPaymentEvent(reservationEvent2.getGuid(), CHARGE, APPROVED, TEN, limitedOrderPaymentInstrument);
		final PaymentEvent leftoverReservationEvent1 = mockPaymentEvent(null, RESERVE, APPROVED, TEN, unlimitedOrderPaymentInstrument);
		final PaymentEvent leftoverReservationEvent2 = mockPaymentEvent(null, RESERVE, APPROVED, TEN, limitedOrderPaymentInstrument);

		final Map<OrderPaymentInstrumentDTO, MoneyDTO> reservableOrderPaymentInstruments = paymentHistory.getReservableOrderPaymentInstruments(
				asList(reservationEvent1, reservationEvent2, chargeEvent1, leftoverReservationEvent1, chargeEvent2, leftoverReservationEvent2),
				asList(unlimitedOrderPaymentInstrument, limitedOrderPaymentInstrument));

		assertThat(reservableOrderPaymentInstruments).hasSize(2);
		assertThat(reservableOrderPaymentInstruments.get(unlimitedOrderPaymentInstrument))
				.extracting(MoneyDTO::getAmount).isEqualTo(ZERO.getAmount());
		assertThat(reservableOrderPaymentInstruments.get(limitedOrderPaymentInstrument))
				.extracting(MoneyDTO::getAmount).isEqualTo(THIRTY.getAmount());
	}

	@Test
	public void reservableAmountShouldBeLimitMinusChargedForLimitedInstrumentAndZeroForUnlimited() {
		final OrderPaymentInstrumentDTO unlimitedOrderPaymentInstrument = createOrderPaymentInstrumentDTO(ORDER_PAYMENT_INSTRUMENT_GUID1, ZERO);
		final OrderPaymentInstrumentDTO limitedOrderPaymentInstrument = createOrderPaymentInstrumentDTO(ORDER_PAYMENT_INSTRUMENT_GUID2, FIFTY);

		final PaymentEvent reservationEvent1 = mockPaymentEvent(null, RESERVE, APPROVED, FIFTY, unlimitedOrderPaymentInstrument);
		final PaymentEvent reservationEvent2 = mockPaymentEvent(null, RESERVE, APPROVED, TWENTY, limitedOrderPaymentInstrument);
		final PaymentEvent chargeEvent1 = mockPaymentEvent(reservationEvent1.getGuid(), CHARGE, APPROVED, FORTY, unlimitedOrderPaymentInstrument);
		final PaymentEvent chargeEvent2 = mockPaymentEvent(reservationEvent2.getGuid(), CHARGE, APPROVED, TEN, limitedOrderPaymentInstrument);
		final PaymentEvent leftoverReservationEvent1 = mockPaymentEvent(null, RESERVE, APPROVED, TEN, unlimitedOrderPaymentInstrument);
		final PaymentEvent leftoverReservationEvent2 = mockPaymentEvent(null, RESERVE, APPROVED, TEN, limitedOrderPaymentInstrument);
		final PaymentEvent cancelLeftoverReservationOnLimitedInstrumentEvent = mockPaymentEvent(leftoverReservationEvent2.getGuid(),
				CANCEL_RESERVE, APPROVED, TEN, limitedOrderPaymentInstrument);

		final Map<OrderPaymentInstrumentDTO, MoneyDTO> reservableOrderPaymentInstruments = paymentHistory.getReservableOrderPaymentInstruments(
				asList(reservationEvent1, reservationEvent2, chargeEvent1, leftoverReservationEvent1,
						chargeEvent2, leftoverReservationEvent2, cancelLeftoverReservationOnLimitedInstrumentEvent),
				asList(unlimitedOrderPaymentInstrument, limitedOrderPaymentInstrument));

		assertThat(reservableOrderPaymentInstruments).hasSize(2);
		assertThat(reservableOrderPaymentInstruments.get(unlimitedOrderPaymentInstrument))
				.extracting(MoneyDTO::getAmount).isEqualTo(ZERO.getAmount());
		assertThat(reservableOrderPaymentInstruments.get(limitedOrderPaymentInstrument))
				.extracting(MoneyDTO::getAmount).isEqualTo(FORTY.getAmount());
	}

	@Test
	public void reservableAmountShouldBeLimitMinusModifiedAndChargedForLimitedInstrumentAndZeroForUnlimited() {
		final OrderPaymentInstrumentDTO unlimitedInstrument = createOrderPaymentInstrumentDTO(ORDER_PAYMENT_INSTRUMENT_GUID1, ZERO);
		final OrderPaymentInstrumentDTO limitedInstrument = createOrderPaymentInstrumentDTO(ORDER_PAYMENT_INSTRUMENT_GUID2, ONE_HUNDRED);

		final PaymentEvent reservationEvent1 = mockPaymentEvent(null, RESERVE, APPROVED, FIFTY, unlimitedInstrument);
		final PaymentEvent chargeEvent1 = mockPaymentEvent(reservationEvent1.getGuid(), CHARGE, APPROVED, FORTY, unlimitedInstrument);
		final PaymentEvent leftoverReservationEvent1 = mockPaymentEvent(null, RESERVE, APPROVED, TEN, unlimitedInstrument);
		final PaymentEvent reservationEvent2 = mockPaymentEvent(null, RESERVE, APPROVED, FIFTY, limitedInstrument);
		final PaymentEvent modifyEvent2 = mockPaymentEvent(reservationEvent2.getGuid(), MODIFY_RESERVE, APPROVED, FORTY, limitedInstrument);

		final Map<OrderPaymentInstrumentDTO, MoneyDTO> reservableOrderPaymentInstruments = paymentHistory.getReservableOrderPaymentInstruments(
				asList(reservationEvent1, chargeEvent1, leftoverReservationEvent1, reservationEvent2, modifyEvent2),
				asList(unlimitedInstrument, limitedInstrument));

		assertThat(reservableOrderPaymentInstruments).hasSize(2);
		assertThat(reservableOrderPaymentInstruments.get(unlimitedInstrument))
				.extracting(MoneyDTO::getAmount).isEqualTo(ZERO.getAmount());
		assertThat(reservableOrderPaymentInstruments.get(limitedInstrument))
				.extracting(MoneyDTO::getAmount).isEqualTo(SIXTY.getAmount());
	}

	@Test
	public void reservableAmountShouldBeFortyWhenOrderPaymentInstrumentHasLimitFiftyAndSuccessfullyReserveTwentyAndSuccessfullyChargeTen3() {
		final OrderPaymentInstrumentDTO unlimitedOrderPaymentInstrument = createOrderPaymentInstrumentDTO(ORDER_PAYMENT_INSTRUMENT_GUID1, ZERO);
		final OrderPaymentInstrumentDTO limitedOrderPaymentInstrument = createOrderPaymentInstrumentDTO(ORDER_PAYMENT_INSTRUMENT_GUID2, ONE_HUNDRED);

		final PaymentEvent reservationEvent1 = mockPaymentEvent(null, RESERVE, APPROVED, FIFTY, unlimitedOrderPaymentInstrument);
		final PaymentEvent chargeEvent1 = mockPaymentEvent(reservationEvent1.getGuid(), CHARGE, APPROVED, FORTY, unlimitedOrderPaymentInstrument);
		final PaymentEvent leftoverReservationEvent1 = mockPaymentEvent(null, RESERVE, APPROVED, TEN, unlimitedOrderPaymentInstrument);
		final PaymentEvent reservationEvent2 = mockPaymentEvent(null, RESERVE, APPROVED, FIFTY, limitedOrderPaymentInstrument);

		final Map<OrderPaymentInstrumentDTO, MoneyDTO> reservableOrderPaymentInstruments = paymentHistory.getReservableOrderPaymentInstruments(
				asList(reservationEvent1, chargeEvent1, leftoverReservationEvent1, reservationEvent2),
				asList(unlimitedOrderPaymentInstrument, limitedOrderPaymentInstrument));

		assertThat(reservableOrderPaymentInstruments).hasSize(2);
		assertThat(reservableOrderPaymentInstruments.get(unlimitedOrderPaymentInstrument))
				.extracting(MoneyDTO::getAmount).isEqualTo(ZERO.getAmount());
		assertThat(reservableOrderPaymentInstruments.get(limitedOrderPaymentInstrument))
				.extracting(MoneyDTO::getAmount).isEqualTo(FIFTY.getAmount());
	}

	@Test
	public void refundedAmountIsASumOfAllApprovedCredits() {
		final PaymentEvent seq1Reservation = mockPaymentEvent(null, RESERVE, APPROVED, ONE_HUNDRED);
		final PaymentEvent seq1Charge = mockPaymentEvent(seq1Reservation.getGuid(), CHARGE, APPROVED, ONE_HUNDRED);
		final PaymentEvent seq1Credit1 = mockPaymentEvent(seq1Charge.getGuid(), CREDIT, APPROVED, FIFTY);
		final PaymentEvent seq1Credit2 = mockPaymentEvent(seq1Charge.getGuid(), CREDIT, APPROVED, THIRTY);

		final List<PaymentEvent> paymentEvents = asList(seq1Reservation, seq1Charge, seq1Credit1, seq1Credit2);

		final MoneyDTO refundedAmount = paymentHistory.getRefundedAmount(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> refundablePaymentEvents = paymentHistory.getRefundablePaymentEvents(paymentEvents);

		assertThat(refundedAmount.getAmount()).isEqualByComparingTo(EIGHTY.getAmount());
		assertThat(refundablePaymentEvents.keySet()).hasSize(1);
		assertThat(refundablePaymentEvents.get(seq1Charge))
				.extracting(MoneyDTO::getAmount)
				.containsExactly(TWENTY.getAmount());
	}

	@Test
	public void refundedAmountIsASumOfOnlyApprovedCredits() {
		final PaymentEvent seq1Reservation = mockPaymentEvent(null, RESERVE, APPROVED, ONE_HUNDRED);
		final PaymentEvent seq1Charge = mockPaymentEvent(seq1Reservation.getGuid(), CHARGE, APPROVED, ONE_HUNDRED);
		final PaymentEvent seq1Credit1 = mockPaymentEvent(seq1Charge.getGuid(), CREDIT, FAILED, FIFTY);
		final PaymentEvent seq1Credit2 = mockPaymentEvent(seq1Charge.getGuid(), CREDIT, APPROVED, TWENTY);

		final List<PaymentEvent> paymentEvents = asList(seq1Reservation, seq1Charge, seq1Credit1, seq1Credit2);

		final MoneyDTO refundedAmount = paymentHistory.getRefundedAmount(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> refundablePaymentEvents = paymentHistory.getRefundablePaymentEvents(paymentEvents);

		assertThat(refundedAmount.getAmount()).isEqualByComparingTo(TWENTY.getAmount());
		assertThat(refundablePaymentEvents.keySet()).hasSize(1);
		assertThat(refundablePaymentEvents.get(seq1Charge))
				.extracting(MoneyDTO::getAmount)
				.containsExactly(EIGHTY.getAmount());
	}

}
