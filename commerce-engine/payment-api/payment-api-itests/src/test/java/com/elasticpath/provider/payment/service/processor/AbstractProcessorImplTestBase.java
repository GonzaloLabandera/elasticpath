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
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.REVERSE_CHARGE;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.assertj.core.data.Index;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequestFailedException;
import com.elasticpath.plugin.payment.provider.dto.AddressDTO;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTOBuilder;
import com.elasticpath.plugin.payment.provider.dto.OrderContext;
import com.elasticpath.plugin.payment.provider.dto.OrderContextBuilder;
import com.elasticpath.plugin.payment.provider.dto.OrderSkuDTO;
import com.elasticpath.plugin.payment.provider.dto.OrderSkuDTOBuilder;
import com.elasticpath.plugin.payment.provider.dto.PaymentStatus;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames;
import com.elasticpath.provider.payment.domain.PaymentProviderConfiguration;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIResponse;
import com.elasticpath.provider.payment.domain.transaction.cancel.CancelAllReservationsRequest;
import com.elasticpath.provider.payment.domain.transaction.cancel.CancelAllReservationsRequestBuilder;
import com.elasticpath.provider.payment.domain.transaction.charge.ChargeRequest;
import com.elasticpath.provider.payment.domain.transaction.charge.ChargeRequestBuilder;
import com.elasticpath.provider.payment.domain.transaction.credit.CreditRequest;
import com.elasticpath.provider.payment.domain.transaction.credit.CreditRequestBuilder;
import com.elasticpath.provider.payment.domain.transaction.credit.ManualCreditRequest;
import com.elasticpath.provider.payment.domain.transaction.credit.ManualCreditRequestBuilder;
import com.elasticpath.provider.payment.domain.transaction.credit.ReverseChargeRequest;
import com.elasticpath.provider.payment.domain.transaction.credit.ReverseChargeRequestBuilder;
import com.elasticpath.provider.payment.domain.transaction.modify.ModifyReservationRequest;
import com.elasticpath.provider.payment.domain.transaction.modify.ModifyReservationRequestBuilder;
import com.elasticpath.provider.payment.domain.transaction.reservation.ReserveRequest;
import com.elasticpath.provider.payment.domain.transaction.reservation.ReserveRequestBuilder;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigurationService;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.event.PaymentEventBuilder;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.service.payment.provider.PaymentProviderPluginForIntegrationTesting;
import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.test.util.Utils;

/**
 * Abstract base class for processor tests with helper methods and DB setup.
 */
@SuppressWarnings({"PMD.AbstractClassWithoutAbstractMethod"})
public abstract class AbstractProcessorImplTestBase extends BasicSpringContextTest {

	/**
	 * Default capability exception.
	 */
	static final PaymentCapabilityRequestFailedException CAPABILITY_EXCEPTION =
			new PaymentCapabilityRequestFailedException("The request failed.", "The capability throws exception.", true);
	/**
	 * 2.
	 */
	static final BigDecimal TWO = BigDecimal.valueOf(2);
	/**
	 * 3.
	 */
	static final BigDecimal THREE = BigDecimal.valueOf(3);
	/***
	 * 5.
	 */
	static final BigDecimal FIVE = BigDecimal.valueOf(5);
	/**
	 * 7.
	 */
	static final BigDecimal SEVEN = BigDecimal.valueOf(7);
	/**
	 * 8.
	 */
	static final BigDecimal EIGHT = BigDecimal.valueOf(8);
	/**
	 * 10.
	 */
	static final BigDecimal TEN = BigDecimal.valueOf(10);
	/**
	 * 12.
	 */
	static final BigDecimal TWELVE = BigDecimal.valueOf(12);
	/**
	 * 13.
	 */
	static final BigDecimal THIRTEEN = BigDecimal.valueOf(13);
	/**
	 * 15.
	 */
	static final BigDecimal FIFTEEN = BigDecimal.valueOf(15);
	/**
	 * 18.
	 */
	static final BigDecimal EIGHTEEN = BigDecimal.valueOf(18);
	/**
	 * 20.
	 */
	static final BigDecimal TWENTY = BigDecimal.valueOf(20);
	private static final String PAYMENT_PROVIDER_CONFIGURATION_GUID = "PAYMENT_PROVIDER_CONFIGURATION_GUID";
	private static final String CURRENCY_CODE = "USD";
	/**
	 * Default reserved amount: $10 US.
	 */
	static final MoneyDTO DEFAULT_RESERVED_AMOUNT_10_USD = createMoney(BigDecimal.TEN);
	/**
	 * Default modified amount: $20 US.
	 */
	static final MoneyDTO DEFAULT_MODIFIED_AMOUNT_20_USD = createMoney(BigDecimal.valueOf(20));
	private static final AddressDTO BILLING_ADDRESS = new AddressDTO();
	private static final String CUSTOMER_EMAIL = "customer@email.com";
	private static final String ORDER_NUMBER = "12000-1";
	private static final Map<String, String> CUSTOM_REQUEST_DATA = ImmutableMap.of("custom-request-data-key", "custom-request-data-value");
	private static final Map<String, String> RESERVATION_EVENT_DATA = ImmutableMap.of("reservation-data-key", "reservation-data-value");
	private static final Map<String, String> ORDER_INSTRUMENT_DATA = ImmutableMap.of("opi-data-key", "opi-data-value");
	private static final Map<String, String> INSTRUMENT_DATA = ImmutableMap.of("instrument-data-key", "instrument-data-value");
	private static final Map<String, String> PROVIDER_CONFIG_DATA = ImmutableMap.of("provider-config-key", "provider-config-value");
	private static final Map<String, String> RESPONSE_DATA = ImmutableMap.of("test-key", "test-value");
	private final GregorianCalendar calendar = new GregorianCalendar(2019, Calendar.JANUARY, 1, 1, 0, 0);
	private int eventOrderNumber;

	@Autowired
	private PaymentProviderConfigurationService paymentProviderConfigurationService;

	/**
	 * Creates limited payment instrument.
	 *
	 * @param limit limit
	 * @return instrument
	 */
	static OrderPaymentInstrumentDTO createLimitedOrderPaymentInstrument(final MoneyDTO limit) {
		final OrderPaymentInstrumentDTO orderPaymentInstrumentDTO = createOrderPaymentInstrumentDTO();
		orderPaymentInstrumentDTO.setLimit(limit);
		return orderPaymentInstrumentDTO;
	}

	/**
	 * Creates unlimited order payment instrument.
	 *
	 * @return instrument
	 */
	static OrderPaymentInstrumentDTO createOrderPaymentInstrumentDTO() {
		final OrderPaymentInstrumentDTO orderInstrument = new OrderPaymentInstrumentDTO();
		orderInstrument.setGUID(UUID.randomUUID().toString());
		orderInstrument.setBillingAddress(BILLING_ADDRESS);
		orderInstrument.setCustomerEmail(CUSTOMER_EMAIL);
		orderInstrument.setOrderNumber(ORDER_NUMBER);
		orderInstrument.setPaymentInstrument(createPaymentInstrumentDTO());
		orderInstrument.setOrderPaymentInstrumentData(ORDER_INSTRUMENT_DATA);
		orderInstrument.setLimit(createMoney(BigDecimal.ZERO));
		return orderInstrument;
	}

	/**
	 * Creates unlimited single reserve per order payment instrument.
	 *
	 * @return instrument
	 */
	static OrderPaymentInstrumentDTO createSingleReservePerPIOrderPaymentInstrumentDTO() {
		final OrderPaymentInstrumentDTO orderInstrument = new OrderPaymentInstrumentDTO();
		orderInstrument.setGUID(UUID.randomUUID().toString());
		orderInstrument.setBillingAddress(BILLING_ADDRESS);
		orderInstrument.setCustomerEmail(CUSTOMER_EMAIL);
		orderInstrument.setOrderNumber(ORDER_NUMBER);
		orderInstrument.setPaymentInstrument(createSingleReservePerPIPaymentInstrumentDTO());
		orderInstrument.setOrderPaymentInstrumentData(ORDER_INSTRUMENT_DATA);
		orderInstrument.setLimit(createMoney(BigDecimal.ZERO));
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

	private static PaymentInstrumentDTO createSingleReservePerPIPaymentInstrumentDTO() {
		final PaymentInstrumentDTO paymentInstrument = new PaymentInstrumentDTO();
		paymentInstrument.setPaymentProviderConfigurationGuid(PAYMENT_PROVIDER_CONFIGURATION_GUID);
		paymentInstrument.setData(INSTRUMENT_DATA);
		paymentInstrument.setName("PayPal");
		paymentInstrument.setGUID(UUID.randomUUID().toString());
		paymentInstrument.setPaymentProviderConfiguration(PROVIDER_CONFIG_DATA);
		paymentInstrument.setSupportingMultiCharges(false);
		paymentInstrument.setSingleReservePerPI(true);
		return paymentInstrument;
	}

	/**
	 * Creates US dollars.
	 *
	 * @param amount amount
	 * @return {@link MoneyDTO}
	 */
	static MoneyDTO createMoney(final BigDecimal amount) {
		return MoneyDTOBuilder.builder()
				.withAmount(amount)
				.withCurrencyCode(CURRENCY_CODE)
				.build(new MoneyDTO());
	}

	/**
	 * Creates order context.
	 *
	 * @return order context
	 */
	static OrderContext createOrderContext() {
		int quantity = 2;
		BigDecimal unitPrice = BigDecimal.TEN;
		BigDecimal taxAmount = BigDecimal.ONE;
		BigDecimal discount = BigDecimal.ZERO;
		String displayName = "Device";
		String sku = "skuCode";
		BigDecimal total = unitPrice
				.multiply(new BigDecimal(quantity))
				.add(taxAmount)
				.subtract(discount);
		return OrderContextBuilder.builder()
				.withOrderSkus(ImmutableList.of(OrderSkuDTOBuilder.builder()
						.withDisplayName(displayName)
						.withQuantity(quantity)
						.withPrice(unitPrice)
						.withTaxAmount(taxAmount)
						.withTotal(total)
						.withSkuCode(sku)
						.build(new OrderSkuDTO())))
				.withBillingAddress(BILLING_ADDRESS)
				.withCustomerEmail(CUSTOMER_EMAIL)
				.withOrderNumber(ORDER_NUMBER)
				.withOrderTotal(DEFAULT_RESERVED_AMOUNT_10_USD)
				.build(new OrderContext());
	}

	/**
	 * Checks that there is a single payment event in the response and it got approved.
	 *
	 * @param response Payment API response
	 */
	static void checkSinglePaymentEventApproved(final PaymentAPIResponse response) {
		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response).extracting(paymentAPIResponse -> paymentAPIResponse.getEvents().size()).isEqualTo(1);
		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentStatus).containsOnly(APPROVED);
		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentEventData).contains(RESPONSE_DATA, Index.atIndex(0));
	}

	/**
	 * Checks that there is a single payment event in the response and it got skipped.
	 *
	 * @param response Payment API response
	 */
	static void checkSinglePaymentEventSkipped(final PaymentAPIResponse response) {
		assertThat(response).extracting(paymentAPIResponse -> paymentAPIResponse.getEvents().size()).isEqualTo(1);
		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentStatus).containsOnly(SKIPPED);
	}

	/**
	 * Checks that there is a single payment event in the response and it got failed.
	 *
	 * @param response Payment API response
	 */
	static void checkSinglePaymentEventFailed(final PaymentAPIResponse response) {
		assertThat(response.isSuccess()).isEqualTo(false);
		assertThat(response).extracting(paymentAPIResponse -> paymentAPIResponse.getEvents().size()).isEqualTo(1);
		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentStatus).containsOnly(FAILED);
		assertThat(response.getEvents()).extracting(PaymentEvent::getInternalMessage).containsOnly(CAPABILITY_EXCEPTION.getInternalMessage());
		assertThat(response.getEvents()).extracting(PaymentEvent::getExternalMessage).containsOnly(CAPABILITY_EXCEPTION.getExternalMessage());
		assertThat(response.getEvents()).extracting(PaymentEvent::isTemporaryFailure).containsOnly(CAPABILITY_EXCEPTION.isTemporaryFailure());
	}

	/**
	 * Checks that response has single modify event.
	 *
	 * @param response        Payment API response
	 * @param possibleAmounts possible reserved amounts
	 */
	static void checkModifyResponse(final PaymentAPIResponse response, final BigDecimal... possibleAmounts) {
		assertThat(response).extracting(paymentAPIResponse -> paymentAPIResponse.getEvents().size()).isEqualTo(1);
		assertThat(response.getEvents()).extracting(PaymentEvent::getAmount).extracting(MoneyDTO::getAmount).containsAnyOf(possibleAmounts);
		assertThat(response.getEvents()).extracting(PaymentEvent::getAmount).extracting(MoneyDTO::getCurrencyCode).containsOnly(CURRENCY_CODE);
		assertThat(response.getEvents()).extracting(PaymentEvent::getReferenceId).containsOnly(ORDER_NUMBER);
		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentType).containsOnly(MODIFY_RESERVE);
		assertThat(response.getEvents()).extracting(PaymentEvent::getOrderPaymentInstrumentDTO)
				.allSatisfy(AbstractProcessorImplTestBase::checkOrderPaymentInstrument);
	}

	/**
	 * Checks that response has single reserve event.
	 *
	 * @param response        Payment API response
	 * @param possibleAmounts possible reserved amounts
	 */
	static void checkReserveResponse(final PaymentAPIResponse response, final BigDecimal... possibleAmounts) {
		assertThat(response).extracting(paymentAPIResponse -> paymentAPIResponse.getEvents().size()).isEqualTo(1);
		assertThat(response.getEvents()).extracting(PaymentEvent::getAmount).extracting(MoneyDTO::getAmount).containsAnyOf(possibleAmounts);
		assertThat(response.getEvents()).extracting(PaymentEvent::getAmount).extracting(MoneyDTO::getCurrencyCode).containsOnly(CURRENCY_CODE);
		assertThat(response.getEvents()).extracting(PaymentEvent::getReferenceId).containsOnly(ORDER_NUMBER);
		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentType).containsOnly(RESERVE);
		assertThat(response.getEvents()).extracting(PaymentEvent::getOrderPaymentInstrumentDTO)
				.allSatisfy(AbstractProcessorImplTestBase::checkOrderPaymentInstrument);
	}

	/**
	 * Checks that response has single charge event.
	 *
	 * @param response        Payment API response
	 * @param possibleAmounts possible charged amounts
	 */
	static void checkChargeResponse(final PaymentAPIResponse response, final BigDecimal... possibleAmounts) {
		assertThat(response).extracting(paymentAPIResponse -> paymentAPIResponse.getEvents().size()).isEqualTo(1);
		assertThat(response.getEvents()).extracting(PaymentEvent::getAmount).extracting(MoneyDTO::getAmount).containsAnyOf(possibleAmounts);
		assertThat(response.getEvents()).extracting(PaymentEvent::getAmount).extracting(MoneyDTO::getCurrencyCode).containsOnly(CURRENCY_CODE);
		assertThat(response.getEvents()).extracting(PaymentEvent::getReferenceId).containsOnly(ORDER_NUMBER);
		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentType).containsOnly(CHARGE);
		assertThat(response.getEvents()).extracting(PaymentEvent::getOrderPaymentInstrumentDTO)
				.allSatisfy(AbstractProcessorImplTestBase::checkOrderPaymentInstrument);
	}

	private static void checkOrderPaymentInstrument(final OrderPaymentInstrumentDTO orderPaymentInstrumentDTO) {
		assertThat(orderPaymentInstrumentDTO).extracting(OrderPaymentInstrumentDTO::getOrderNumber).isEqualTo(ORDER_NUMBER);
		assertThat(orderPaymentInstrumentDTO).extracting(OrderPaymentInstrumentDTO::getCustomerEmail).isEqualTo(CUSTOMER_EMAIL);
		assertThat(orderPaymentInstrumentDTO).extracting(OrderPaymentInstrumentDTO::getBillingAddress).isEqualTo(BILLING_ADDRESS);
		assertThat(orderPaymentInstrumentDTO.getPaymentInstrument()).satisfies(instrument -> {
			assertThat(instrument.getPaymentProviderConfigurationGuid()).isEqualTo(PAYMENT_PROVIDER_CONFIGURATION_GUID);
			assertThat(instrument.getData()).isEqualTo(INSTRUMENT_DATA);
			assertThat(instrument.getPaymentProviderConfiguration()).isEqualTo(PROVIDER_CONFIG_DATA);
		});
	}

	/**
	 * Test setup. Creates and persists provider configuration.
	 */
	@Before
	public void setUp() {
		eventOrderNumber = 0;
		createAndPersistTestPaymentProviderConfiguration();
	}

	/**
	 * Test tear down. Cleans up provider configuration and plugin capabilities setup.
	 */
	@After
	public void tearDown() {
		PaymentProviderPluginForIntegrationTesting.resetCapabilities();
		paymentProviderConfigurationService.remove(paymentProviderConfigurationService.findByGuid(PAYMENT_PROVIDER_CONFIGURATION_GUID));
	}

	private void createAndPersistTestPaymentProviderConfiguration() {
		PaymentProviderConfiguration configuration = getBeanFactory().getPrototypeBean(
				PaymentProviderApiContextIdNames.PAYMENT_PROVIDER_CONFIGURATION, PaymentProviderConfiguration.class);

		configuration.setGuid(PAYMENT_PROVIDER_CONFIGURATION_GUID);
		configuration.setConfigurationName(Utils.uniqueCode("CONFIGURATION_NAME"));
		configuration.setPaymentProviderPluginId("paymentProviderPluginForIntegrationTesting");
		configuration.setPaymentConfigurationData(Collections.emptySet());

		paymentProviderConfigurationService.saveOrUpdate(configuration);
	}

	/**
	 * Creates approved reservation event.
	 *
	 * @param instrument instrument
	 * @param amount     reserved amount
	 * @return event
	 */
	PaymentEvent createReservationEvent(final OrderPaymentInstrumentDTO instrument, final MoneyDTO amount) {
		return createReservationEvent(instrument, amount, APPROVED);
	}

	/**
	 * Creates reservation event.
	 *
	 * @param instrument    instrument
	 * @param amount        reserved amount
	 * @param paymentStatus event status
	 * @return event
	 */
	PaymentEvent createReservationEvent(final OrderPaymentInstrumentDTO instrument,
										final MoneyDTO amount,
										final PaymentStatus paymentStatus) {
		final String guid = UUID.randomUUID().toString();
		calendar.add(Calendar.MINUTE, eventOrderNumber++);
		return PaymentEventBuilder.aPaymentEvent()
				.withGuid(guid)
				.withParentGuid(null)
				.withOrderPaymentInstrumentDTO(instrument)
				.withOriginalPaymentInstrument(true)
				.withAmount(amount)
				.withPaymentType(RESERVE)
				.withPaymentStatus(paymentStatus)
				.withDate(calendar.getTime())
				.withPaymentEventData(RESERVATION_EVENT_DATA)
				.withReferenceId(ORDER_NUMBER)
				.build(getBeanFactory());
	}

	/**
	 * Creates reservation modification event.
	 *
	 * @param reservationEvent original reservation event
	 * @param newAmount        new amount
	 * @return event
	 */
	PaymentEvent createModifyReservationEvent(final PaymentEvent reservationEvent, final MoneyDTO newAmount) {
		return createModifyReservationEvent(reservationEvent, newAmount, APPROVED);
	}

	/**
	 * Creates reservation modification event.
	 *
	 * @param reservationEvent original reservation event
	 * @param newAmount        new amount
	 * @param paymentStatus    event status
	 * @return event
	 */
	PaymentEvent createModifyReservationEvent(final PaymentEvent reservationEvent,
											  final MoneyDTO newAmount,
											  final PaymentStatus paymentStatus) {
		final String guid = UUID.randomUUID().toString();
		calendar.add(Calendar.MINUTE, eventOrderNumber++);
		return PaymentEventBuilder.aPaymentEvent()
				.withGuid(guid)
				.withParentGuid(reservationEvent.getGuid())
				.withOrderPaymentInstrumentDTO(reservationEvent.getOrderPaymentInstrumentDTO())
				.withOriginalPaymentInstrument(true)
				.withAmount(newAmount)
				.withPaymentType(MODIFY_RESERVE)
				.withPaymentStatus(paymentStatus)
				.withDate(calendar.getTime())
				.withPaymentEventData(emptyMap())
				.withReferenceId(ORDER_NUMBER)
				.build(getBeanFactory());
	}

	/**
	 * Create cancel reservation event.
	 *
	 * @param reservationEvent original reservation event
	 * @return event
	 */
	PaymentEvent createCancelReservationEvent(final PaymentEvent reservationEvent) {
		return createCancelReservationEvent(reservationEvent, reservationEvent.getAmount(), APPROVED);
	}

	/**
	 * Create cancel reservation event.
	 *
	 * @param reservationEvent original reservation event
	 * @param cancelledAmount  amount being cancelled
	 * @param paymentStatus    event status
	 * @return event
	 */
	PaymentEvent createCancelReservationEvent(final PaymentEvent reservationEvent,
											  final MoneyDTO cancelledAmount,
											  final PaymentStatus paymentStatus) {
		final String guid = UUID.randomUUID().toString();
		calendar.add(Calendar.MINUTE, eventOrderNumber++);
		return PaymentEventBuilder.aPaymentEvent()
				.withGuid(guid)
				.withParentGuid(reservationEvent.getGuid())
				.withOrderPaymentInstrumentDTO(reservationEvent.getOrderPaymentInstrumentDTO())
				.withOriginalPaymentInstrument(true)
				.withAmount(cancelledAmount)
				.withPaymentType(CANCEL_RESERVE)
				.withPaymentStatus(paymentStatus)
				.withDate(calendar.getTime())
				.withPaymentEventData(emptyMap())
				.withReferenceId(ORDER_NUMBER)
				.build(getBeanFactory());
	}

	/**
	 * Creates cancel all reservations request to Payment API.
	 *
	 * @param events list of payment events
	 * @return request
	 */
	CancelAllReservationsRequest createCancelAllReservationsRequest(final List<PaymentEvent> events) {
		List<OrderPaymentInstrumentDTO> orderPaymentInstrumentDTOList = events.stream()
				.filter(event -> event.getPaymentType().equals(RESERVE))
				.filter(event -> event.getPaymentStatus().equals(PaymentStatus.APPROVED))
				.collect(Collectors.toList())
				.stream()
				.map(PaymentEvent::getOrderPaymentInstrumentDTO)
				.collect(Collectors.toList());
		return CancelAllReservationsRequestBuilder.builder()
				.withLedger(events)
				.withOrderPaymentInstruments(orderPaymentInstrumentDTOList)
				.withCustomRequestData(CUSTOM_REQUEST_DATA)
				.withOrderContext(createOrderContext())
				.build(getBeanFactory());
	}

	/**
	 * Creates reverse charge event.
	 *
	 * @param chargeEvent original charge event
	 * @return event
	 */
	PaymentEvent createReverseChargeEvent(final PaymentEvent chargeEvent) {
		final String guid = UUID.randomUUID().toString();
		calendar.add(Calendar.MINUTE, eventOrderNumber++);
		return PaymentEventBuilder.aPaymentEvent()
				.withGuid(guid)
				.withParentGuid(chargeEvent.getGuid())
				.withOrderPaymentInstrumentDTO(chargeEvent.getOrderPaymentInstrumentDTO())
				.withOriginalPaymentInstrument(true)
				.withAmount(chargeEvent.getAmount())
				.withPaymentType(REVERSE_CHARGE)
				.withPaymentStatus(APPROVED)
				.withDate(calendar.getTime())
				.withPaymentEventData(emptyMap())
				.withReferenceId(ORDER_NUMBER)
				.build(getBeanFactory());
	}

	/**
	 * Creates charge event.
	 *
	 * @param reservationEvent original reservation event
	 * @param chargedAmount    charged amount
	 * @return event
	 */
	PaymentEvent createChargeEvent(final PaymentEvent reservationEvent, final MoneyDTO chargedAmount) {
		final String guid = UUID.randomUUID().toString();
		calendar.add(Calendar.MINUTE, eventOrderNumber++);
		return PaymentEventBuilder.aPaymentEvent()
				.withGuid(guid)
				.withParentGuid(reservationEvent.getGuid())
				.withOrderPaymentInstrumentDTO(reservationEvent.getOrderPaymentInstrumentDTO())
				.withOriginalPaymentInstrument(true)
				.withAmount(chargedAmount)
				.withPaymentType(CHARGE)
				.withPaymentStatus(APPROVED)
				.withDate(calendar.getTime())
				.withPaymentEventData(emptyMap())
				.withReferenceId(ORDER_NUMBER)
				.build(getBeanFactory());
	}

	/**
	 * Creates credit event with reasonable defaults.
	 *
	 * @param chargeEvent  original charge event
	 * @param creditAmount credit amount
	 * @return payment event
	 */
	PaymentEvent createCreditEvent(final PaymentEvent chargeEvent, final MoneyDTO creditAmount) {
		final String guid = UUID.randomUUID().toString();
		calendar.add(Calendar.MINUTE, eventOrderNumber++);
		return PaymentEventBuilder.aPaymentEvent()
				.withGuid(guid)
				.withParentGuid(chargeEvent.getGuid())
				.withOrderPaymentInstrumentDTO(chargeEvent.getOrderPaymentInstrumentDTO())
				.withOriginalPaymentInstrument(true)
				.withAmount(creditAmount)
				.withPaymentType(TransactionType.CREDIT)
				.withPaymentStatus(PaymentStatus.APPROVED)
				.withPaymentEventData(Collections.emptyMap())
				.withReferenceId(ORDER_NUMBER)
				.build(getBeanFactory());
	}

	/**
	 * Creates modify reservation request to Payment API.
	 *
	 * @param amount new amount
	 * @return request
	 */
	ModifyReservationRequest createModifyReservationRequest(final MoneyDTO amount) {
		final PaymentEvent reservationEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), DEFAULT_RESERVED_AMOUNT_10_USD);
		return ModifyReservationRequestBuilder.builder()
				.withOrderPaymentInstruments(Collections.singletonList(reservationEvent.getOrderPaymentInstrumentDTO()))
				.withAmount(amount)
				.withLedger(Collections.singletonList(reservationEvent))
				.withOrderContext(createOrderContext())
				.withCustomRequestData(CUSTOM_REQUEST_DATA)
				.build(getBeanFactory());
	}

	/**
	 * Creates modify reservation request to Payment API with single reserve per payment instrument.
	 *
	 * @param amount new amount
	 * @return request
	 */
	ModifyReservationRequest createModifyReservationRequestWithSingleReservePerPIPaymentInstrument(final MoneyDTO amount) {
		final PaymentEvent reservationEvent = createReservationEvent(createSingleReservePerPIOrderPaymentInstrumentDTO(),
				DEFAULT_RESERVED_AMOUNT_10_USD);
		return ModifyReservationRequestBuilder.builder()
				.withOrderPaymentInstruments(Collections.singletonList(reservationEvent.getOrderPaymentInstrumentDTO()))
				.withAmount(amount)
				.withLedger(Collections.singletonList(reservationEvent))
				.withOrderContext(createOrderContext())
				.withCustomRequestData(CUSTOM_REQUEST_DATA)
				.withFinalPayment(false)
				.withSingleReservePerPI(false)
				.build(getBeanFactory());
	}

	/**
	 * Creates reserve request to Payment API.
	 *
	 * @param amount new amount
	 * @return request
	 */
	ReserveRequest createReserveRequest(final MoneyDTO amount) {
		return ReserveRequestBuilder.builder()
				.withAmount(amount)
				.withOrderContext(createOrderContext())
				.withCustomRequestData(CUSTOM_REQUEST_DATA)
				.withSelectedOrderPaymentInstruments(Collections.singletonList(createOrderPaymentInstrumentDTO()))
				.build(getBeanFactory());
	}

	/**
	 * Creates charge request to Payment API.
	 *
	 * @param totalChargeableAmount total chargeable amount allowed for the order at this point
	 * @return request
	 */
	ChargeRequest createChargeRequest(final MoneyDTO totalChargeableAmount) {
		final PaymentEvent reservationEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), DEFAULT_RESERVED_AMOUNT_10_USD);
		return ChargeRequestBuilder.builder()
				.withOrderPaymentInstruments(Collections.singletonList(reservationEvent.getOrderPaymentInstrumentDTO()))
				.withLedger(Collections.singletonList(reservationEvent))
				.withTotalChargeableAmount(totalChargeableAmount)
				.withOrderContext(createOrderContext())
				.withCustomRequestData(CUSTOM_REQUEST_DATA)
				.withFinalPayment(false)
				.withSingleReservePerPI(false)
				.build(getBeanFactory());
	}

	/**
	 * Creates charge request to Payment API with single reserve per payment instrument.
	 *
	 * @param totalChargeableAmount total chargeable amount allowed for the order at this point
	 * @return request
	 */
	ChargeRequest createChargeRequestWithSingleReservePerPIPaymentInstrument(final MoneyDTO totalChargeableAmount) {
		final PaymentEvent reservationEvent = createReservationEvent(createSingleReservePerPIOrderPaymentInstrumentDTO(),
				DEFAULT_RESERVED_AMOUNT_10_USD);
		return ChargeRequestBuilder.builder()
				.withOrderPaymentInstruments(Collections.singletonList(reservationEvent.getOrderPaymentInstrumentDTO()))
				.withLedger(Collections.singletonList(reservationEvent))
				.withTotalChargeableAmount(totalChargeableAmount)
				.withOrderContext(createOrderContext())
				.withCustomRequestData(CUSTOM_REQUEST_DATA)
				.withFinalPayment(false)
				.build(getBeanFactory());
	}

	/**
	 * Creates credit request to Payment API.
	 *
	 * @param amountToRefund refund amount
	 * @return request
	 */
	CreditRequest createCreditRequest(final MoneyDTO amountToRefund) {
		PaymentEvent reservationEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), DEFAULT_RESERVED_AMOUNT_10_USD);
		PaymentEvent chargeEvent = createChargeEvent(reservationEvent, DEFAULT_RESERVED_AMOUNT_10_USD);
		return CreditRequestBuilder.builder()
				.withOrderPaymentInstruments(Collections.singletonList(chargeEvent.getOrderPaymentInstrumentDTO()))
				.withSelectedOrderPaymentInstruments(Collections.emptyList())
				.withLedger(ImmutableList.of(reservationEvent, chargeEvent))
				.withCustomRequestData(Collections.emptyMap())
				.withAmount(amountToRefund)
				.withOrderContext(createOrderContext())
				.build(getBeanFactory());
	}

	/**
	 * Creates manual credit request to Payment API.
	 *
	 * @param amountToRefund refund amount
	 * @return request
	 */
	ManualCreditRequest createManualCreditRequest(final MoneyDTO amountToRefund) {
		PaymentEvent reservationEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), DEFAULT_RESERVED_AMOUNT_10_USD);
		PaymentEvent chargeEvent = createChargeEvent(reservationEvent, DEFAULT_RESERVED_AMOUNT_10_USD);
		return ManualCreditRequestBuilder.builder()
				.withOrderPaymentInstruments(Collections.singletonList(reservationEvent.getOrderPaymentInstrumentDTO()))
				.withLedger(ImmutableList.of(reservationEvent, chargeEvent))
				.withCustomRequestData(Collections.emptyMap())
				.withAmount(amountToRefund)
				.withOrderContext(createOrderContext())
				.build(getBeanFactory());
	}

	/**
	 * Creates reverse charge request to Payment API.
	 *
	 * @return request
	 */
	ReverseChargeRequest createReverseChargeRequest() {
		PaymentEvent reservationEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), DEFAULT_RESERVED_AMOUNT_10_USD);
		PaymentEvent chargeEvent = createChargeEvent(reservationEvent, DEFAULT_RESERVED_AMOUNT_10_USD);
		return ReverseChargeRequestBuilder.builder()
				.withOrderPaymentInstruments(Collections.singletonList(reservationEvent.getOrderPaymentInstrumentDTO()))
				.withSelectedPaymentEvents(Collections.singletonList(chargeEvent))
				.withLedger(ImmutableList.of(reservationEvent, chargeEvent))
				.withCustomRequestData(Collections.emptyMap())
				.withOrderContext(createOrderContext())
				.build(getBeanFactory());
	}
}
