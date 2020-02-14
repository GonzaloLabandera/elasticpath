/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.domain.transaction;

import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.CANCEL_ALL_RESERVATIONS_REQUEST;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.CANCEL_RESERVATION_REQUEST;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.CHARGE_REQUEST;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.CREDIT_REQUEST;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.MODIFY_RESERVATION_REQUEST;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.ORDER_PAYMENT_INSTRUMENT_DTO;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PAYMENT_EVENT;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PAYMENT_INSTRUMENT_DTO;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.RESERVE_REQUEST;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.plugin.payment.provider.dto.AddressDTO;
import com.elasticpath.plugin.payment.provider.dto.AddressDTOBuilder;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTOBuilder;
import com.elasticpath.plugin.payment.provider.dto.OrderContext;
import com.elasticpath.plugin.payment.provider.dto.OrderContextBuilder;
import com.elasticpath.plugin.payment.provider.dto.OrderSkuDTO;
import com.elasticpath.plugin.payment.provider.dto.OrderSkuDTOBuilder;
import com.elasticpath.plugin.payment.provider.dto.PaymentStatus;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.provider.payment.domain.transaction.cancel.CancelAllReservationsRequest;
import com.elasticpath.provider.payment.domain.transaction.cancel.CancelAllReservationsRequestBuilder;
import com.elasticpath.provider.payment.domain.transaction.cancel.CancelReservationRequest;
import com.elasticpath.provider.payment.domain.transaction.cancel.CancelReservationRequestBuilder;
import com.elasticpath.provider.payment.domain.transaction.charge.ChargeRequest;
import com.elasticpath.provider.payment.domain.transaction.charge.ChargeRequestBuilder;
import com.elasticpath.provider.payment.domain.transaction.credit.CreditRequest;
import com.elasticpath.provider.payment.domain.transaction.credit.CreditRequestBuilder;
import com.elasticpath.provider.payment.domain.transaction.modify.ModifyReservationRequest;
import com.elasticpath.provider.payment.domain.transaction.modify.ModifyReservationRequestBuilder;
import com.elasticpath.provider.payment.domain.transaction.reservation.ReserveRequest;
import com.elasticpath.provider.payment.domain.transaction.reservation.ReserveRequestBuilder;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.event.PaymentEventBuilder;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTOBuilder;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTOBuilder;

@RunWith(MockitoJUnitRunner.class)
public class PaymentAPIRequestSerializationTest {

	private static final String CUSTOMER_EMAIL = "customer@email.com";
	private static final String ORDER_NUMBER = "12000-1";
	private static final String CURRENCY_CODE = "USD";
	private static final Map<String, String> INSTRUMENT_DATA = ImmutableMap.of("instrument-data-key", "instrument-data-value");
	private static final Map<String, String> PROVIDER_CONFIG_DATA = ImmutableMap.of("provider-config-key", "provider-config-value");
	private static final Map<String, String> CUSTOM_REQUEST_DATA = ImmutableMap.of("custom-request-data-key", "custom-request-data-value");
	private static final Map<String, String> ORDER_INSTRUMENT_DATA = ImmutableMap.of("opi-data-key", "opi-data-value");
	private static final Map<String, String> PAYMENT_EVENT_DATA = ImmutableMap.of("payment-event-data-key", "payment-event-data-value");

	private final ObjectMapper mapper = new ObjectMapper();

	@Mock
	private BeanFactory beanFactory;

	@Before
	public void setUp() throws Exception {
		when(beanFactory.getPrototypeBean(RESERVE_REQUEST, ReserveRequest.class))
				.thenReturn(new ReserveRequest());
		when(beanFactory.getPrototypeBean(CHARGE_REQUEST, ChargeRequest.class))
				.thenReturn(new ChargeRequest());
		when(beanFactory.getPrototypeBean(MODIFY_RESERVATION_REQUEST, ModifyReservationRequest.class))
				.thenReturn(new ModifyReservationRequest());
		when(beanFactory.getPrototypeBean(CANCEL_RESERVATION_REQUEST, CancelReservationRequest.class))
				.thenReturn(new CancelReservationRequest());
		when(beanFactory.getPrototypeBean(CANCEL_ALL_RESERVATIONS_REQUEST, CancelAllReservationsRequest.class))
				.thenReturn(new CancelAllReservationsRequest());
		when(beanFactory.getPrototypeBean(CREDIT_REQUEST, CreditRequest.class))
				.thenReturn(new CreditRequest());
		when(beanFactory.getPrototypeBean(PAYMENT_INSTRUMENT_DTO, PaymentInstrumentDTO.class))
				.thenReturn(new PaymentInstrumentDTO());
		when(beanFactory.getPrototypeBean(ORDER_PAYMENT_INSTRUMENT_DTO, OrderPaymentInstrumentDTO.class))
				.thenReturn(new OrderPaymentInstrumentDTO());
		when(beanFactory.getPrototypeBean(PAYMENT_EVENT, PaymentEvent.class))
				.thenReturn(new PaymentEvent());
	}

	@Test
	public void validateReservationRequest() throws IOException {
		validate(createReservationRequest());
	}

	@Test
	public void validateChargeRequest() throws IOException {
		validate(createChargeRequest());
	}

	@Test
	public void validateModifyReservationRequest() throws IOException {
		validate(createModifyReservationRequest());
	}

	@Test
	public void validateCancelReservationRequest() throws IOException {
		validate(createCancelReservationRequest());
	}

	@Test
	public void validateCancelAllReservationsRequest() throws IOException {
		validate(createCancelAllReservationsRequest());
	}

	@Test
	public void validateCreditRequest() throws IOException {
		validate(createCreditRequest());
	}

	private void validate(final Object request) throws IOException {
		final String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request);
		final Object deserialized = mapper.readValue(json, request.getClass());
		assertThat(deserialized).isEqualToComparingFieldByFieldRecursively(request);
	}

	private ReserveRequest createReservationRequest() {
		return ReserveRequestBuilder.builder()
				.withSelectedOrderPaymentInstruments(asList(createOrderPaymentInstrumentDTO(), createOrderPaymentInstrumentDTO()))
				.withAmount(createMoney(BigDecimal.TEN))
				.withOrderContext(createOrderContext())
				.withCustomRequestData(CUSTOM_REQUEST_DATA)
				.build(beanFactory);
	}

	private ChargeRequest createChargeRequest() {
		final PaymentEvent firstReservationEvent = createPaymentEvent();
		final PaymentEvent secondReservationEvent = createPaymentEvent();
		return ChargeRequestBuilder.builder()
				.withOrderPaymentInstruments(asList(firstReservationEvent.getOrderPaymentInstrumentDTO(),
						secondReservationEvent.getOrderPaymentInstrumentDTO()))
				.withTotalChargeableAmount(createMoney(BigDecimal.TEN))
				.withLedger(asList(secondReservationEvent, secondReservationEvent))
				.withOrderContext(createOrderContext())
				.withCustomRequestData(CUSTOM_REQUEST_DATA)
				.build(beanFactory);
	}

	private ModifyReservationRequest createModifyReservationRequest() {
		final PaymentEvent firstReservationEvent = createPaymentEvent();
		final PaymentEvent secondReservationEvent = createPaymentEvent();
		return ModifyReservationRequestBuilder.builder()
				.withOrderPaymentInstruments(asList(firstReservationEvent.getOrderPaymentInstrumentDTO(),
						secondReservationEvent.getOrderPaymentInstrumentDTO()))
				.withAmount(createMoney(BigDecimal.ONE))
				.withLedger(asList(secondReservationEvent, secondReservationEvent))
				.withOrderContext(createOrderContext())
				.withCustomRequestData(CUSTOM_REQUEST_DATA)
				.build(beanFactory);
	}

	private CancelReservationRequest createCancelReservationRequest() {
		final PaymentEvent firstReservationEvent = createPaymentEvent();
		final PaymentEvent secondReservationEvent = createPaymentEvent();
		return CancelReservationRequestBuilder.builder()
				.withOrderPaymentInstruments(asList(firstReservationEvent.getOrderPaymentInstrumentDTO(),
						secondReservationEvent.getOrderPaymentInstrumentDTO()))
				.withLedger(asList(secondReservationEvent, secondReservationEvent))
				.withSelectedPaymentEventsToCancel(asList(createPaymentEvent(), createPaymentEvent()))
				.withAmount(createMoney(BigDecimal.ONE))
				.withCustomRequestData(CUSTOM_REQUEST_DATA)
				.withOrderContext(createOrderContext())
				.build(beanFactory);
	}

	private CancelAllReservationsRequest createCancelAllReservationsRequest() {
		final PaymentEvent firstReservationEvent = createPaymentEvent();
		final PaymentEvent secondReservationEvent = createPaymentEvent();
		return CancelAllReservationsRequestBuilder.builder()
				.withOrderPaymentInstruments(asList(firstReservationEvent.getOrderPaymentInstrumentDTO(),
						secondReservationEvent.getOrderPaymentInstrumentDTO()))
				.withLedger(asList(secondReservationEvent, secondReservationEvent))
				.withCustomRequestData(CUSTOM_REQUEST_DATA)
				.withOrderContext(createOrderContext())
				.build(beanFactory);
	}

	private CreditRequest createCreditRequest() {
		final PaymentEvent firstReservationEvent = createPaymentEvent();
		final PaymentEvent secondReservationEvent = createPaymentEvent();
		return CreditRequestBuilder.builder()
				.withOrderPaymentInstruments(asList(firstReservationEvent.getOrderPaymentInstrumentDTO(),
						secondReservationEvent.getOrderPaymentInstrumentDTO()))
				.withSelectedOrderPaymentInstruments(Collections.emptyList())
				.withLedger(asList(firstReservationEvent, secondReservationEvent))
				.withAmount(createMoney(BigDecimal.ONE))
				.withCustomRequestData(CUSTOM_REQUEST_DATA)
				.withOrderContext(createOrderContext())
				.build(beanFactory);
	}

	private PaymentEvent createPaymentEvent() {
		final String guid = UUID.randomUUID().toString();
		return PaymentEventBuilder.aPaymentEvent()
				.withGuid(guid)
				.withParentGuid(guid)
				.withReferenceId(ORDER_NUMBER)
				.withOrderPaymentInstrumentDTO(createOrderPaymentInstrumentDTO())
				.withOriginalPaymentInstrument(true)
				.withAmount(createMoney(BigDecimal.TEN))
				.withPaymentType(TransactionType.RESERVE)
				.withPaymentStatus(PaymentStatus.APPROVED)
				.withDate(new Date())
				.withPaymentEventData(PAYMENT_EVENT_DATA)
				.build(beanFactory);
	}

	private OrderPaymentInstrumentDTO createOrderPaymentInstrumentDTO() {
		return OrderPaymentInstrumentDTOBuilder.builder()
				.withGuid(UUID.randomUUID().toString())
				.withPaymentInstrument(createPaymentInstrumentDTO())
				.withLimit(createMoney(BigDecimal.ZERO))
				.withOrderNumber(ORDER_NUMBER)
				.withBillingAddress(createBillingAddress())
				.withCustomerEmail(CUSTOMER_EMAIL)
				.withOrderPaymentInstrumentData(ORDER_INSTRUMENT_DATA)
				.build(beanFactory);
	}

	private static AddressDTO createBillingAddress() {
		return AddressDTOBuilder.builder()
				.withGuid(UUID.randomUUID().toString())
				.withFirstName("John")
				.withLastName("Doe")
				.withStreet1("123 Fake St.")
				.withPhoneNumber("1-123-444-5678")
				.withCity("Vancouver")
				.withZipOrPostalCode("V6A2P5")
				.withSubCountry("BC")
				.withCountry("Canada")
				.build(new AddressDTO());
	}

	private PaymentInstrumentDTO createPaymentInstrumentDTO() {
		return PaymentInstrumentDTOBuilder.builder()
				.withGuid(UUID.randomUUID().toString())
				.withName("PayPal")
				.withPaymentProviderConfigurationGuid(UUID.randomUUID().toString())
				.withPaymentProviderConfiguration(PROVIDER_CONFIG_DATA)
				.withData(INSTRUMENT_DATA)
				.withSingleReservePerPI(false)
				.withSupportingMultiCharges(false)
				.build(beanFactory);
	}

	private static MoneyDTO createMoney(final BigDecimal amount) {
		return MoneyDTOBuilder.builder()
				.withAmount(amount)
				.withCurrencyCode(CURRENCY_CODE)
				.build(new MoneyDTO());
	}

	private static OrderContext createOrderContext() {
		return OrderContextBuilder.builder()
				.withOrderSkus(asList(
						OrderSkuDTOBuilder.builder()
								.withDisplayName("Device 1")
								.withQuantity(2)
								.withPrice(BigDecimal.TEN)
								.withTaxAmount(BigDecimal.ONE)
								.withTotal(BigDecimal.TEN)
								.withSkuCode("skuCode1")
								.build(new OrderSkuDTO()),
						OrderSkuDTOBuilder.builder()
								.withDisplayName("Device 2")
								.withQuantity(1)
								.withPrice(BigDecimal.TEN)
								.withTaxAmount(BigDecimal.ONE)
								.withTotal(BigDecimal.TEN)
								.withSkuCode("skuCode2")
								.build(new OrderSkuDTO())
				))
				.withOrderTotal(createMoney(BigDecimal.TEN))
				.withOrderNumber(ORDER_NUMBER)
				.build(new OrderContext());
	}

}