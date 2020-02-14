/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.workflow;

import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.APPROVED;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.CHARGE;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.CREDIT;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.plugin.payment.provider.dto.AddressDTO;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTOBuilder;
import com.elasticpath.plugin.payment.provider.dto.PaymentStatus;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.provider.payment.domain.history.PaymentEventHistoryRequest;
import com.elasticpath.provider.payment.domain.history.PaymentEventHistoryRequestBuilder;
import com.elasticpath.provider.payment.domain.history.PaymentEventHistoryResponse;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.event.PaymentEventBuilder;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.test.integration.BasicSpringContextTest;

public class PaymentHistoryWorkflowImplTest extends BasicSpringContextTest {

	@Autowired
	private PaymentHistoryWorkflow testee;

	@Autowired
	private BeanFactory beanFactory;


	private static final BigDecimal ZERO = BigDecimal.valueOf(0);
	private static final BigDecimal TEN = BigDecimal.valueOf(10);
	private static final BigDecimal SEVENTY = BigDecimal.valueOf(70);
	private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);


	private static final String CURRENCY_CODE_CAD = "CAD";

	private static final String PAYMENT_PROVIDER_CONFIGURATION_GUID = "PAYMENT_PROVIDER_CONFIGURATION_GUID";
	private static final AddressDTO BILLING_ADDRESS = new AddressDTO();
	private static final String CUSTOMER_EMAIL = "customer@email.com";
	private static final String ORDER_NUMBER = "12000-1";
	private static final Map<String, String> ORDER_INSTRUMENT_DATA = ImmutableMap.of("opi-data-key", "opi-data-value");
	private static final Map<String, String> INSTRUMENT_DATA = ImmutableMap.of("instrument-data-key", "instrument-data-value");
	private static final Map<String, String> PROVIDER_CONFIG_DATA = ImmutableMap.of("provider-config-key", "provider-config-value");

	private final GregorianCalendar calendar = new GregorianCalendar(2019, Calendar.JANUARY, 1, 1, 0, 0);
	private int eventOrderNumber;

	@Before
	public void setUp() {
		eventOrderNumber = 0;
	}

	@Test
	public void testCreatePaymentInstrumentCharged() {
		PaymentEvent reservationEvent = createReservationPaymentEvent(ONE_HUNDRED);
		PaymentEvent chargeEvent = createChargeEvent(reservationEvent, SEVENTY);

		PaymentEventHistoryRequest paymentEventHistoryRequest =
				PaymentEventHistoryRequestBuilder.builder()
						.withLedger(ImmutableList.of(reservationEvent, chargeEvent))
						.build(beanFactory);

		PaymentEventHistoryResponse paymentEventHistoryAmounts = testee.getPaymentEventHistoryAmounts(paymentEventHistoryRequest);
		assertThat(paymentEventHistoryAmounts.getAmountCharged().getAmount().compareTo(SEVENTY))
				.isEqualTo(0);
		assertThat(paymentEventHistoryAmounts.getAmountRefunded().getAmount().compareTo(ZERO))
				.isEqualTo(0);
	}

	@Test
	public void testCreatePaymentInstrumentRefunded() {
		PaymentEvent reservationEvent = createReservationPaymentEvent(ONE_HUNDRED);
		PaymentEvent chargeEvent = createChargeEvent(reservationEvent, SEVENTY);
		PaymentEvent creditEvent = createCreditEvent(chargeEvent, TEN);

		PaymentEventHistoryRequest paymentEventHistoryRequest =
				PaymentEventHistoryRequestBuilder.builder()
						.withLedger(ImmutableList.of(reservationEvent, chargeEvent, creditEvent))
						.build(beanFactory);

		PaymentEventHistoryResponse paymentEventHistoryAmounts = testee.getPaymentEventHistoryAmounts(paymentEventHistoryRequest);
		assertThat(paymentEventHistoryAmounts.getAmountCharged().getAmount().compareTo(SEVENTY))
				.isEqualTo(0);
		assertThat(paymentEventHistoryAmounts.getAmountRefunded().getAmount().compareTo(TEN))
				.isEqualTo(0);
	}

	private PaymentEvent createReservationPaymentEvent(final BigDecimal amount) {
		calendar.add(Calendar.MINUTE, eventOrderNumber++);
		final String guid = UUID.randomUUID().toString();
		return PaymentEventBuilder.aPaymentEvent()
				.withGuid(guid)
				.withParentGuid(null)
				.withPaymentType(TransactionType.RESERVE)
				.withPaymentStatus(PaymentStatus.APPROVED)
				.withAmount(createMoneyDto(amount))
				.withOrderPaymentInstrumentDTO(createOrderPaymentInstrumentDTO())
				.withOriginalPaymentInstrument(true)
				.withDate(calendar.getTime())
				.withReferenceId(ORDER_NUMBER)
				.build(getBeanFactory());
	}

	private PaymentEvent createChargeEvent(final PaymentEvent reservationEvent, final BigDecimal chargedAmount) {
		final String guid = UUID.randomUUID().toString();
		calendar.add(Calendar.MINUTE, eventOrderNumber++);
		return PaymentEventBuilder.aPaymentEvent()
				.withGuid(guid)
				.withParentGuid(reservationEvent.getGuid())
				.withOrderPaymentInstrumentDTO(reservationEvent.getOrderPaymentInstrumentDTO())
				.withOriginalPaymentInstrument(true)
				.withAmount(createMoneyDto(chargedAmount))
				.withPaymentType(CHARGE)
				.withPaymentStatus(APPROVED)
				.withDate(calendar.getTime())
				.withPaymentEventData(emptyMap())
				.withReferenceId(ORDER_NUMBER)
				.build(getBeanFactory());
	}

	private PaymentEvent createCreditEvent(final PaymentEvent chargeEvent, final BigDecimal refundAmount) {
		final String guid = UUID.randomUUID().toString();
		calendar.add(Calendar.MINUTE, eventOrderNumber++);
		return PaymentEventBuilder.aPaymentEvent()
				.withGuid(guid)
				.withParentGuid(chargeEvent.getGuid())
				.withOrderPaymentInstrumentDTO(chargeEvent.getOrderPaymentInstrumentDTO())
				.withOriginalPaymentInstrument(true)
				.withAmount(createMoneyDto(refundAmount))
				.withPaymentType(CREDIT)
				.withPaymentStatus(APPROVED)
				.withDate(calendar.getTime())
				.withPaymentEventData(emptyMap())
				.withReferenceId(ORDER_NUMBER)
				.build(getBeanFactory());
	}


	private static MoneyDTO createMoneyDto(final BigDecimal amount) {
		return MoneyDTOBuilder.builder()
				.withAmount(amount)
				.withCurrencyCode(CURRENCY_CODE_CAD)
				.build(new MoneyDTO());
	}

	private static OrderPaymentInstrumentDTO createOrderPaymentInstrumentDTO() {
		final OrderPaymentInstrumentDTO orderInstrument = new OrderPaymentInstrumentDTO();
		orderInstrument.setGUID(UUID.randomUUID().toString());
		orderInstrument.setBillingAddress(BILLING_ADDRESS);
		orderInstrument.setCustomerEmail(CUSTOMER_EMAIL);
		orderInstrument.setOrderNumber(ORDER_NUMBER);
		orderInstrument.setPaymentInstrument(createPaymentInstrumentDTO());
		orderInstrument.setOrderPaymentInstrumentData(ORDER_INSTRUMENT_DATA);
		orderInstrument.setLimit(createMoneyDto(BigDecimal.ZERO));
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

}
