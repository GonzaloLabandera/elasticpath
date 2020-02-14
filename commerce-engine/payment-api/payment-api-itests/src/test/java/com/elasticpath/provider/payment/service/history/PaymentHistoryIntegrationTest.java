/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.history;

import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.APPROVED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.FAILED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.SKIPPED;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.CHARGE;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.CREDIT;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.MODIFY_RESERVE;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.RESERVE;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.plugin.payment.provider.dto.AddressDTO;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTOBuilder;
import com.elasticpath.plugin.payment.provider.dto.PaymentStatus;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.event.PaymentEventBuilder;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.test.integration.BasicSpringContextTest;

/**
 * Integration tests for {@link PaymentHistory}.
 */
public class PaymentHistoryIntegrationTest extends BasicSpringContextTest {

	private static final BigDecimal TWENTY = BigDecimal.valueOf(20);
	private static final BigDecimal SIXTY = BigDecimal.valueOf(60);
	private static final BigDecimal SEVENTY = BigDecimal.valueOf(70);
	private static final BigDecimal EIGHTY = BigDecimal.valueOf(80);
	private static final BigDecimal NINETY = BigDecimal.valueOf(90);
	private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
	private static final BigDecimal TWENTY_THOUSAND = BigDecimal.valueOf(20000);
	private static final BigDecimal TWENTY_THOUSAND_THIRTY = BigDecimal.valueOf(20030);

	private static final String CURRENCY_CODE_CAD = "CAD";
	private static final String CURRENCY_CODE_USD = "USD";

	private static final String PAYMENT_PROVIDER_CONFIGURATION_GUID = "PAYMENT_PROVIDER_CONFIGURATION_GUID";
	private static final AddressDTO BILLING_ADDRESS = new AddressDTO();
	private static final String CUSTOMER_EMAIL = "customer@email.com";
	private static final String ORDER_NUMBER = "12000-1";
	private static final Map<String, String> ORDER_INSTRUMENT_DATA = ImmutableMap.of("opi-data-key", "opi-data-value");
	private static final Map<String, String> INSTRUMENT_DATA = ImmutableMap.of("instrument-data-key", "instrument-data-value");
	private static final Map<String, String> PROVIDER_CONFIG_DATA = ImmutableMap.of("provider-config-key", "provider-config-value");

	@Autowired
	private PaymentHistory paymentHistory;

	private final GregorianCalendar calendar = new GregorianCalendar(2019, Calendar.JANUARY, 1, 1, 0, 0);
	private int eventOrderNumber;

	@Before
	public void setUp() {
		eventOrderNumber = 0;
	}

	@Test
	public void testGetPaymentHistoryWhenModifyToOrderToDecreaseReservedAmount() {
		final int expectedCountOfRefundablePaymentInstruments = 3;

		final PaymentEvent seq1Reservation = createPaymentEvent(null, RESERVE, APPROVED, ONE_HUNDRED, CURRENCY_CODE_CAD);
		final PaymentEvent seq2FailedReservation = createPaymentEvent(null, RESERVE, FAILED, ONE_HUNDRED, CURRENCY_CODE_CAD);
		final PaymentEvent seq3Reservation = createPaymentEvent(null, RESERVE, APPROVED, ONE_HUNDRED, CURRENCY_CODE_CAD);
		final PaymentEvent seq1Modification = createPaymentEvent(seq1Reservation.getGuid(), MODIFY_RESERVE, APPROVED, EIGHTY, CURRENCY_CODE_CAD);
		final PaymentEvent seq1FailedCharge = createPaymentEvent(seq1Modification.getGuid(), CHARGE, FAILED, SIXTY, CURRENCY_CODE_CAD);
		final PaymentEvent seq4Reservation = createPaymentEvent(null, RESERVE, APPROVED, TWENTY_THOUSAND, CURRENCY_CODE_CAD);
		final PaymentEvent seq4Charge = createPaymentEvent(seq4Reservation.getGuid(), CHARGE, APPROVED, TWENTY_THOUSAND, CURRENCY_CODE_CAD);
		final PaymentEvent seq5Reservation = createPaymentEvent(null, RESERVE, APPROVED, TWENTY, CURRENCY_CODE_CAD);
		final PaymentEvent seq5Charge = createPaymentEvent(seq5Reservation.getGuid(), CHARGE, APPROVED, TWENTY, CURRENCY_CODE_CAD);
		final PaymentEvent seq3Charge = createPaymentEvent(seq3Reservation.getGuid(), CHARGE, APPROVED, TEN, CURRENCY_CODE_CAD);
		final PaymentEvent seq6Reservation = createPaymentEvent(null, RESERVE, APPROVED, NINETY, CURRENCY_CODE_CAD);

		final List<PaymentEvent> paymentEvents = Arrays.asList(
				seq1Reservation, seq2FailedReservation, seq3Reservation, seq1Modification, seq1FailedCharge,
				seq4Reservation, seq4Charge, seq5Reservation, seq5Charge, seq3Charge, seq6Reservation);

		final MoneyDTO availableReservedAmount = paymentHistory.getAvailableReservedAmount(paymentEvents);
		final MoneyDTO chargedAmount = paymentHistory.getChargedAmount(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> chargeablePaymentEvents = paymentHistory.getChargeablePaymentEvents(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> refundablePaymentEvents = paymentHistory.getRefundablePaymentEvents(paymentEvents);

		assertThat(availableReservedAmount.getAmount()).isEqualTo(NINETY);
		assertThat(chargedAmount.getAmount()).isEqualTo(TWENTY_THOUSAND_THIRTY);
		assertThat(chargeablePaymentEvents).extracting(Multimap::size).isEqualTo(1);
		assertThat(chargeablePaymentEvents.get(seq6Reservation)).extracting(MoneyDTO::getAmount).containsOnly(NINETY);
		assertThat(refundablePaymentEvents).extracting(Multimap::size).isEqualTo(expectedCountOfRefundablePaymentInstruments);
		assertThat(refundablePaymentEvents.get(seq4Charge)).extracting(MoneyDTO::getAmount).containsOnly(TWENTY_THOUSAND);
		assertThat(refundablePaymentEvents.get(seq5Charge)).extracting(MoneyDTO::getAmount).containsOnly(TWENTY);
		assertThat(refundablePaymentEvents.get(seq3Charge)).extracting(MoneyDTO::getAmount).containsOnly(TEN);
	}

	@Test
	public void shouldThrowIllegalStateExceptionWhenDifferentEventsInOneOrderPaymentSequenceContainSameDate() {
		final PaymentEvent seq1Reservation = createPaymentEvent(null, RESERVE, APPROVED, ONE_HUNDRED, CURRENCY_CODE_CAD);
		eventOrderNumber = 0;
		final PaymentEvent seq1Modification1 = createPaymentEvent(seq1Reservation.getGuid(), MODIFY_RESERVE, APPROVED, ONE_HUNDRED,
				CURRENCY_CODE_CAD);
		eventOrderNumber = 0;
		final PaymentEvent seq1Modification2 = createPaymentEvent(seq1Modification1.getGuid(), MODIFY_RESERVE, APPROVED, ONE_HUNDRED,
				CURRENCY_CODE_CAD);

		final List<PaymentEvent> paymentEvents = Arrays.asList(seq1Reservation, seq1Modification1, seq1Modification2);

		assertThatThrownBy(() -> paymentHistory.getAvailableReservedAmount(paymentEvents)).isInstanceOf(IllegalStateException.class);
		assertThatThrownBy(() -> paymentHistory.getChargedAmount(paymentEvents)).isInstanceOf(IllegalStateException.class);
		assertThatThrownBy(() -> paymentHistory.getChargeablePaymentEvents(paymentEvents)).isInstanceOf(IllegalStateException.class);
		assertThatThrownBy(() -> paymentHistory.getRefundablePaymentEvents(paymentEvents)).isInstanceOf(IllegalStateException.class);
	}

	@Test
	public void shouldThrowIllegalStateExceptionWhenPaymentEventsSequenceIsIllegal() {
		final PaymentEvent seq1Reservation = createPaymentEvent(null, RESERVE, APPROVED, ONE_HUNDRED, CURRENCY_CODE_CAD);
		final PaymentEvent seq2Reservation = createPaymentEvent(seq1Reservation.getGuid(), RESERVE, APPROVED, ONE_HUNDRED, CURRENCY_CODE_CAD);

		final List<PaymentEvent> paymentEvents = Arrays.asList(seq1Reservation, seq2Reservation);

		assertThatThrownBy(() -> paymentHistory.getAvailableReservedAmount(paymentEvents)).isInstanceOf(IllegalStateException.class);
		assertThatThrownBy(() -> paymentHistory.getChargedAmount(paymentEvents)).isInstanceOf(IllegalStateException.class);
		assertThatThrownBy(() -> paymentHistory.getChargeablePaymentEvents(paymentEvents)).isInstanceOf(IllegalStateException.class);
		assertThatThrownBy(() -> paymentHistory.getRefundablePaymentEvents(paymentEvents)).isInstanceOf(IllegalStateException.class);
	}

	@Test
	public void shouldThrowIllegalStateExceptionWhenThereAreTwoDifferentCurrencyCodesInOneOrderPaymentSequence() {
		final PaymentEvent seq1Reservation = createPaymentEvent(null, RESERVE, APPROVED, ONE_HUNDRED, CURRENCY_CODE_CAD);
		final PaymentEvent seq1Charge = createPaymentEvent(seq1Reservation.getGuid(), CHARGE, APPROVED, ONE_HUNDRED, CURRENCY_CODE_USD);

		final List<PaymentEvent> paymentEvents = Arrays.asList(seq1Reservation, seq1Charge);

		assertThatThrownBy(() -> paymentHistory.getAvailableReservedAmount(paymentEvents)).isInstanceOf(IllegalStateException.class);
		assertThatThrownBy(() -> paymentHistory.getChargedAmount(paymentEvents)).isInstanceOf(IllegalStateException.class);
		assertThatThrownBy(() -> paymentHistory.getChargeablePaymentEvents(paymentEvents)).isInstanceOf(IllegalStateException.class);
		assertThatThrownBy(() -> paymentHistory.getRefundablePaymentEvents(paymentEvents)).isInstanceOf(IllegalStateException.class);
	}

	@Test
	public void testGetPaymentHistoryWhenModifyOrderToDecreaseReservedAmountAndRefundSomeAmount() {
		final int expectedCountOfChargeablePaymentInstruments = 2;
		final int expectedCountOfRefundablePaymentInstruments = 2;

		final PaymentEvent firstReserve = createPaymentEvent(null, RESERVE, APPROVED, ONE_HUNDRED, CURRENCY_CODE_CAD);
		final PaymentEvent secondReserve = createPaymentEvent(null, RESERVE, FAILED, TEN, CURRENCY_CODE_CAD);
		final PaymentEvent thirdReserve = createPaymentEvent(null, RESERVE, APPROVED, TEN, CURRENCY_CODE_CAD);
		final PaymentEvent foursReserve = createPaymentEvent(null, RESERVE, APPROVED, TEN, CURRENCY_CODE_CAD);
		final PaymentEvent firstModify = createPaymentEvent(firstReserve.getGuid(), MODIFY_RESERVE, APPROVED, EIGHTY, CURRENCY_CODE_CAD);
		final PaymentEvent firstCharge = createPaymentEvent(firstModify.getGuid(), CHARGE, APPROVED, SEVENTY, CURRENCY_CODE_CAD);
		final PaymentEvent fifthReserve = createPaymentEvent(null, RESERVE, APPROVED, TEN, CURRENCY_CODE_CAD);
		final PaymentEvent secondCharge = createPaymentEvent(thirdReserve.getGuid(), CHARGE, APPROVED, TEN, CURRENCY_CODE_CAD);
		final PaymentEvent thirdCharge = createPaymentEvent(foursReserve.getGuid(), CHARGE, APPROVED, TEN, CURRENCY_CODE_CAD);
		final PaymentEvent sixthReserve = createPaymentEvent(null, RESERVE, APPROVED, NINETY, CURRENCY_CODE_CAD);
		final PaymentEvent firstCredit = createPaymentEvent(secondCharge.getGuid(), CREDIT, APPROVED, TEN, CURRENCY_CODE_CAD);
		final PaymentEvent secondCredit = createPaymentEvent(thirdCharge.getGuid(), CREDIT, FAILED, ONE, CURRENCY_CODE_CAD);

		final List<PaymentEvent> paymentEvents = Arrays.asList(
				firstReserve,   	// 1 => -0, +100, 0
				secondReserve,   	// 5 => -0, +0, 0
				thirdReserve,   	// 2 => -0, +10, 0
				foursReserve,    	// 3 => -0, +10, 0
				firstModify,    	// 1 => -0, +80, 0
				firstCharge,    	// 1 => -70, +0, 0
				fifthReserve,    	// 1 => -70, +10, 0 <-
				secondCharge,    	// 2 => -10, +0, 0
				thirdCharge,    	// 3 => -10, +0, 0
				sixthReserve,    	// 4 => -0, +90, 0  <-
				firstCredit,    	// 2 => -10, +0, 10 <-
				secondCredit    	// 3 => -10, +0, 0 	<-
		);

		final MoneyDTO reservedAmount = paymentHistory.getAvailableReservedAmount(paymentEvents);
		final MoneyDTO chargedAmount = paymentHistory.getChargedAmount(paymentEvents);
		final MoneyDTO refundedAmount = paymentHistory.getRefundedAmount(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> chargeablePaymentEvents = paymentHistory.getChargeablePaymentEvents(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> refundablePaymentEvents = paymentHistory.getRefundablePaymentEvents(paymentEvents);

		assertThat(reservedAmount.getAmount()).isEqualTo(ONE_HUNDRED);
		assertThat(chargedAmount.getAmount()).isEqualTo(NINETY);
		assertThat(refundedAmount.getAmount()).isEqualTo(TEN);
		assertThat(chargeablePaymentEvents).extracting(Multimap::size).isEqualTo(expectedCountOfChargeablePaymentInstruments);
		assertThat(refundablePaymentEvents).extracting(Multimap::size).isEqualTo(expectedCountOfRefundablePaymentInstruments);
		assertThat(chargeablePaymentEvents.values()).extracting(MoneyDTO::getAmount).containsOnly(NINETY, TEN);
		assertThat(refundablePaymentEvents.values()).extracting(MoneyDTO::getAmount).containsOnly(SEVENTY, TEN);
	}

	@Test
	public void modifyReservationOverridesOriginalReservation() {
		final PaymentEvent reservationEvent = createReservationPaymentEvent(ONE_HUNDRED, APPROVED);
		final PaymentEvent modificationEvent = createPaymentEvent(reservationEvent, MODIFY_RESERVE, EIGHTY);

		final List<PaymentEvent> paymentEvents = Arrays.asList(reservationEvent, modificationEvent);
		final MoneyDTO availableReservedAmount = paymentHistory.getAvailableReservedAmount(paymentEvents);
		final MoneyDTO chargedAmount = paymentHistory.getChargedAmount(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> chargeablePaymentEvents = paymentHistory.getChargeablePaymentEvents(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> refundablePaymentEvents = paymentHistory.getRefundablePaymentEvents(paymentEvents);

		assertThat(availableReservedAmount.getAmount()).isEqualTo(EIGHTY);
		assertThat(chargedAmount.getAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(chargeablePaymentEvents).extracting(Multimap::keySet).isEqualToComparingFieldByField(modificationEvent);
		assertThat(refundablePaymentEvents.isEmpty()).isTrue();
	}

	@Test
	public void lastModifyReservationOverridesPreviousOne() {
		final PaymentEvent reservationEvent = createReservationPaymentEvent(ONE_HUNDRED, APPROVED);
		final PaymentEvent modificationEvent = createPaymentEvent(reservationEvent, MODIFY_RESERVE, EIGHTY);
		final PaymentEvent lastModificationEvent = createPaymentEvent(modificationEvent, MODIFY_RESERVE, SIXTY);

		final List<PaymentEvent> paymentEvents = Arrays.asList(reservationEvent, modificationEvent, lastModificationEvent);
		final MoneyDTO availableReservedAmount = paymentHistory.getAvailableReservedAmount(paymentEvents);
		final MoneyDTO chargedAmount = paymentHistory.getChargedAmount(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> chargeablePaymentEvents = paymentHistory.getChargeablePaymentEvents(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> refundablePaymentEvents = paymentHistory.getRefundablePaymentEvents(paymentEvents);

		assertThat(availableReservedAmount.getAmount()).isEqualTo(SIXTY);
		assertThat(chargedAmount.getAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(chargeablePaymentEvents).extracting(Multimap::keySet).isEqualToComparingFieldByField(lastModificationEvent);
		assertThat(refundablePaymentEvents.isEmpty()).isTrue();
	}

	@Test
	public void modifyReservationOverridesOriginalReservationWhenOrderIsPartlyCharged() {
		final PaymentEvent reservationEvent = createReservationPaymentEvent(ONE_HUNDRED, APPROVED);
		final PaymentEvent chargeEvent = createPaymentEvent(reservationEvent, CHARGE, EIGHTY);
		final PaymentEvent reReservationEvent = createReservationPaymentEvent(TWENTY, APPROVED);
		final PaymentEvent modificationEvent = createPaymentEvent(reReservationEvent, MODIFY_RESERVE, SIXTY);

		final List<PaymentEvent> paymentEvents = Arrays.asList(reservationEvent, chargeEvent, reReservationEvent, modificationEvent);
		final MoneyDTO availableReservedAmount = paymentHistory.getAvailableReservedAmount(paymentEvents);
		final MoneyDTO chargedAmount = paymentHistory.getChargedAmount(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> chargeablePaymentEvents = paymentHistory.getChargeablePaymentEvents(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> refundablePaymentEvents = paymentHistory.getRefundablePaymentEvents(paymentEvents);

		assertThat(availableReservedAmount.getAmount()).isEqualTo(SIXTY);
		assertThat(chargedAmount.getAmount()).isEqualTo(EIGHTY);
		assertThat(chargeablePaymentEvents).extracting(Multimap::keySet).isEqualToComparingFieldByField(modificationEvent);
		assertThat(refundablePaymentEvents).extracting(Multimap::keySet).isEqualToComparingFieldByField(chargeEvent);
	}

	@Test
	public void lastModifyReservationOverridesPreviousOneWhenOrderIsPartlyCharged() {
		final PaymentEvent reservationEvent = createReservationPaymentEvent(ONE_HUNDRED, APPROVED);
		final PaymentEvent chargeEvent = createPaymentEvent(reservationEvent, CHARGE, EIGHTY);
		final PaymentEvent reReservationEvent = createReservationPaymentEvent(TWENTY, APPROVED);
		final PaymentEvent modificationEvent = createPaymentEvent(reReservationEvent, MODIFY_RESERVE, SIXTY);
		final PaymentEvent lastModificationEvent = createPaymentEvent(modificationEvent, MODIFY_RESERVE, TWENTY);

		final List<PaymentEvent> paymentEvents = Arrays.asList(reservationEvent, chargeEvent,
				reReservationEvent, modificationEvent, lastModificationEvent);
		final MoneyDTO availableReservedAmount = paymentHistory.getAvailableReservedAmount(paymentEvents);
		final MoneyDTO chargedAmount = paymentHistory.getChargedAmount(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> chargeablePaymentEvents = paymentHistory.getChargeablePaymentEvents(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> refundablePaymentEvents = paymentHistory.getRefundablePaymentEvents(paymentEvents);

		assertThat(availableReservedAmount.getAmount()).isEqualTo(TWENTY);
		assertThat(chargedAmount.getAmount()).isEqualTo(EIGHTY);
		assertThat(chargeablePaymentEvents).extracting(Multimap::keySet).isEqualToComparingFieldByField(lastModificationEvent);
		assertThat(refundablePaymentEvents).extracting(Multimap::keySet).isEqualToComparingFieldByField(chargeEvent);
	}

	@Test
	public void modifyReservationOverridesSkippedReservation() {
		final PaymentEvent reservationEvent = createReservationPaymentEvent(ONE_HUNDRED, SKIPPED);
		final PaymentEvent modificationEvent = createPaymentEvent(reservationEvent, MODIFY_RESERVE, SIXTY);

		final List<PaymentEvent> paymentEvents = Arrays.asList(reservationEvent, modificationEvent);
		final MoneyDTO availableReservedAmount = paymentHistory.getAvailableReservedAmount(paymentEvents);
		final MoneyDTO chargedAmount = paymentHistory.getChargedAmount(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> chargeablePaymentEvents = paymentHistory.getChargeablePaymentEvents(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> refundablePaymentEvents = paymentHistory.getRefundablePaymentEvents(paymentEvents);

		assertThat(availableReservedAmount.getAmount()).isEqualTo(SIXTY);
		assertThat(chargedAmount.getAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(chargeablePaymentEvents).extracting(Multimap::keySet).isEqualToComparingFieldByField(modificationEvent);
		assertThat(refundablePaymentEvents.isEmpty()).isTrue();
	}

	@Test
	public void modifyReservationOverridesSkippedReservationWhenOrderIsPartlyCharged() {
		final PaymentEvent reservationEvent = createReservationPaymentEvent(ONE_HUNDRED, SKIPPED);
		final PaymentEvent chargeEvent = createPaymentEvent(reservationEvent, CHARGE, EIGHTY);
		final PaymentEvent reReservationEvent = createReservationPaymentEvent(TWENTY, SKIPPED);
		final PaymentEvent modificationEvent = createPaymentEvent(reReservationEvent, MODIFY_RESERVE, SIXTY);

		final List<PaymentEvent> paymentEvents = Arrays.asList(reservationEvent, chargeEvent, reReservationEvent, modificationEvent);
		final MoneyDTO availableReservedAmount = paymentHistory.getAvailableReservedAmount(paymentEvents);
		final MoneyDTO chargedAmount = paymentHistory.getChargedAmount(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> chargeablePaymentEvents = paymentHistory.getChargeablePaymentEvents(paymentEvents);
		final Multimap<PaymentEvent, MoneyDTO> refundablePaymentEvents = paymentHistory.getRefundablePaymentEvents(paymentEvents);

		assertThat(availableReservedAmount.getAmount()).isEqualTo(SIXTY);
		assertThat(chargedAmount.getAmount()).isEqualTo(EIGHTY);
		assertThat(chargeablePaymentEvents).extracting(Multimap::keySet).isEqualToComparingFieldByField(modificationEvent);
		assertThat(refundablePaymentEvents).extracting(Multimap::keySet).isEqualToComparingFieldByField(chargeEvent);
	}

	private PaymentEvent createReservationPaymentEvent(final BigDecimal amount, final PaymentStatus paymentStatus) {
		calendar.add(Calendar.MINUTE, eventOrderNumber++);
		final String guid = UUID.randomUUID().toString();
		return PaymentEventBuilder.aPaymentEvent()
				.withGuid(guid)
				.withParentGuid(null)
				.withPaymentType(TransactionType.RESERVE)
				.withPaymentStatus(paymentStatus)
				.withAmount(createMoneyDto(amount, CURRENCY_CODE_CAD))
				.withOrderPaymentInstrumentDTO(createOrderPaymentInstrumentDTO())
				.withOriginalPaymentInstrument(true)
				.withDate(calendar.getTime())
				.withReferenceId(ORDER_NUMBER)
				.build(getBeanFactory());
	}

	private PaymentEvent createPaymentEvent(final PaymentEvent parentEvent,
											final TransactionType transactionType,
											final BigDecimal amount) {
		calendar.add(Calendar.MINUTE, eventOrderNumber++);
		final String guid = UUID.randomUUID().toString();
		return PaymentEventBuilder.aPaymentEvent()
				.withGuid(guid)
				.withParentGuid(parentEvent.getGuid())
				.withPaymentType(transactionType)
				.withPaymentStatus(APPROVED)
				.withAmount(createMoneyDto(amount, CURRENCY_CODE_CAD))
				.withOrderPaymentInstrumentDTO(parentEvent.getOrderPaymentInstrumentDTO())
				.withOriginalPaymentInstrument(true)
				.withDate(calendar.getTime())
				.withReferenceId(ORDER_NUMBER)
				.build(getBeanFactory());
	}

	private PaymentEvent createPaymentEvent(final String parentEventGuid,
											final TransactionType transactionType,
											final PaymentStatus paymentStatus,
											final BigDecimal amount,
											final String currencyCode) {
		calendar.add(Calendar.MINUTE, eventOrderNumber++);
		return PaymentEventBuilder.aPaymentEvent()
				.withParentGuid(parentEventGuid)
				.withPaymentType(transactionType)
				.withPaymentStatus(paymentStatus)
				.withAmount(createMoneyDto(amount, currencyCode))
				.withOrderPaymentInstrumentDTO(createOrderPaymentInstrumentDTO())
				.withOriginalPaymentInstrument(true)
				.withDate(calendar.getTime())
				.withReferenceId(ORDER_NUMBER)
				.build(getBeanFactory());
	}

	private static OrderPaymentInstrumentDTO createOrderPaymentInstrumentDTO() {
		final OrderPaymentInstrumentDTO orderInstrument = new OrderPaymentInstrumentDTO();
		orderInstrument.setGUID(UUID.randomUUID().toString());
		orderInstrument.setBillingAddress(BILLING_ADDRESS);
		orderInstrument.setCustomerEmail(CUSTOMER_EMAIL);
		orderInstrument.setOrderNumber(ORDER_NUMBER);
		orderInstrument.setPaymentInstrument(createPaymentInstrumentDTO());
		orderInstrument.setOrderPaymentInstrumentData(ORDER_INSTRUMENT_DATA);
		orderInstrument.setLimit(createMoneyDto(BigDecimal.ZERO, CURRENCY_CODE_CAD));
		return orderInstrument;
	}

	private static PaymentInstrumentDTO createPaymentInstrumentDTO() {
		final PaymentInstrumentDTO paymentInstrument = new PaymentInstrumentDTO();
		paymentInstrument.setPaymentProviderConfigurationGuid(PAYMENT_PROVIDER_CONFIGURATION_GUID);
		paymentInstrument.setData(INSTRUMENT_DATA);
		paymentInstrument.setName("PayPal");
		paymentInstrument.setGUID(UUID.randomUUID().toString());
		paymentInstrument.setPaymentProviderConfiguration(PROVIDER_CONFIG_DATA);
		paymentInstrument.setSupportingMultiCharges(false);
		paymentInstrument.setSingleReservePerPI(false);
		return paymentInstrument;
	}

	private static MoneyDTO createMoneyDto(final BigDecimal amount, final String currencyCode) {
		return MoneyDTOBuilder.builder()
				.withAmount(amount)
				.withCurrencyCode(currencyCode)
				.build(new MoneyDTO());
	}

}
