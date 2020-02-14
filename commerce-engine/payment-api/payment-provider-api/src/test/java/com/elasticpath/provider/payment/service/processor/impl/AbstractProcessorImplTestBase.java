/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.service.processor.impl;

import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.CANCEL_ALL_RESERVATIONS_REQUEST;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.CANCEL_CAPABILITY_REQUEST;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.CANCEL_RESERVATION_REQUEST;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.CHARGE_CAPABILITY_REQUEST;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.CHARGE_REQUEST;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.CREDIT_CAPABILITY_REQUEST;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.CREDIT_REQUEST;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.MODIFY_CAPABILITY_REQUEST;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.MODIFY_RESERVATION_REQUEST;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PAYMENT_EVENT;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.RESERVE_CAPABILITY_REQUEST;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.RESERVE_REQUEST;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.REVERSE_CHARGE_CAPABILITY_REQUEST;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.REVERSE_CHARGE_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.assertj.core.data.Index;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequestFailedException;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityResponse;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ReverseChargeCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.credit.CreditCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.CancelCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ModifyCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ReserveCapabilityRequest;
import com.elasticpath.plugin.payment.provider.dto.AddressDTO;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTOBuilder;
import com.elasticpath.plugin.payment.provider.dto.OrderContext;
import com.elasticpath.plugin.payment.provider.dto.OrderContextBuilder;
import com.elasticpath.plugin.payment.provider.dto.OrderSkuDTO;
import com.elasticpath.plugin.payment.provider.dto.OrderSkuDTOBuilder;
import com.elasticpath.plugin.payment.provider.dto.PaymentStatus;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIResponse;
import com.elasticpath.provider.payment.domain.transaction.cancel.CancelAllReservationsRequest;
import com.elasticpath.provider.payment.domain.transaction.cancel.CancelAllReservationsRequestBuilder;
import com.elasticpath.provider.payment.domain.transaction.cancel.CancelReservationRequest;
import com.elasticpath.provider.payment.domain.transaction.cancel.CancelReservationRequestBuilder;
import com.elasticpath.provider.payment.domain.transaction.charge.ChargeRequest;
import com.elasticpath.provider.payment.domain.transaction.charge.ChargeRequestBuilder;
import com.elasticpath.provider.payment.domain.transaction.credit.CreditRequest;
import com.elasticpath.provider.payment.domain.transaction.credit.CreditRequestBuilder;
import com.elasticpath.provider.payment.domain.transaction.credit.ReverseChargeRequest;
import com.elasticpath.provider.payment.domain.transaction.modify.ModifyReservationRequest;
import com.elasticpath.provider.payment.domain.transaction.modify.ModifyReservationRequestBuilder;
import com.elasticpath.provider.payment.domain.transaction.reservation.ReserveRequest;
import com.elasticpath.provider.payment.domain.transaction.reservation.ReserveRequestBuilder;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.event.PaymentEventBuilder;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;

/**
 * Abstract base class for all processor tests dealing with money calculation.
 */
@SuppressWarnings({"PMD.AbstractClassWithoutAbstractMethod", "PMD.TooManyMethods", "PMD.ExcessiveImports"})
abstract class AbstractProcessorImplTestBase {
	/**
	 * Payment provider configuration GUID.
	 */
	static final String PAYMENT_PROVIDER_CONFIGURATION_GUID = "PAYMENT_PROVIDER_CONFIGURATION_GUID";
	/**
	 * Reservation event data.
	 */
	static final Map<String, String> RESERVATION_EVENT_DATA = ImmutableMap.of("reservation-data-key", "reservation-data-value");
	/**
	 * Charge event data.
	 */
	static final Map<String, String> CHARGE_EVENT_DATA = ImmutableMap.of("charge-data-key", "charge-data-value");
	/**
	 * Capability response data.
	 */
	static final Map<String, String> RESPONSE_DATA = ImmutableMap.of("response-key", "response-value");
	/**
	 * Default capability exception.
	 */
	static final PaymentCapabilityRequestFailedException CAPABILITY_EXCEPTION =
			new PaymentCapabilityRequestFailedException("internalMessage", "externalMessage", true);
	private static final String CURRENCY_CODE = "USD";
	/**
	 * Reserved by default amount of money - $10 (USD).
	 */
	static final MoneyDTO RESERVED_AMOUNT = createMoney(BigDecimal.TEN);
	private static final AddressDTO BILLING_ADDRESS = new AddressDTO();
	private static final String CUSTOMER_EMAIL = "customer@email.com";
	private static final String ORDER_NUMBER = "12000-1";
	private static final Map<String, String> CUSTOM_REQUEST_DATA = ImmutableMap.of("custom-request-data-key", "custom-request-data-value");
	private static final Map<String, String> CANCEL_EVENT_DATA = ImmutableMap.of("cancel-data-key", "cancel-data-value");
	private static final Map<String, String> ORDER_INSTRUMENT_DATA = ImmutableMap.of("opi-data-key", "opi-data-value");
	private static final Map<String, String> INSTRUMENT_DATA = ImmutableMap.of("instrument-data-key", "instrument-data-value");
	private static final Map<String, String> PROVIDER_CONFIG_DATA = ImmutableMap.of("provider-config-key", "provider-config-value");

	private final GregorianCalendar calendar = new GregorianCalendar(2019, Calendar.JANUARY, 1, 1, 0, 0);
	private int eventOrderNumber;

	@Mock
	private BeanFactory beanFactory;

	/**
	 * Test setup. Creates and persists provider configuration.
	 *
	 * @throws PaymentCapabilityRequestFailedException if the process failed
	 */
	@Before
	public void setUp() throws PaymentCapabilityRequestFailedException {
		eventOrderNumber = 0;
		lenient().when(beanFactory.getPrototypeBean(RESERVE_REQUEST, ReserveRequest.class))
				.then((Answer<ReserveRequest>) invocation -> new ReserveRequest());
		lenient().when(beanFactory.getPrototypeBean(CHARGE_REQUEST, ChargeRequest.class))
				.then((Answer<ChargeRequest>) invocation -> new ChargeRequest());
		lenient().when(beanFactory.getPrototypeBean(MODIFY_RESERVATION_REQUEST, ModifyReservationRequest.class))
				.then((Answer<ModifyReservationRequest>) invocation -> new ModifyReservationRequest());
		lenient().when(beanFactory.getPrototypeBean(CANCEL_RESERVATION_REQUEST, CancelReservationRequest.class))
				.then((Answer<CancelReservationRequest>) invocation -> new CancelReservationRequest());
		lenient().when(beanFactory.getPrototypeBean(CANCEL_ALL_RESERVATIONS_REQUEST, CancelAllReservationsRequest.class))
				.then((Answer<CancelAllReservationsRequest>) invocation -> new CancelAllReservationsRequest());
		lenient().when(beanFactory.getPrototypeBean(CREDIT_REQUEST, CreditRequest.class))
				.then((Answer<CreditRequest>) invocation -> new CreditRequest());
		lenient().when(beanFactory.getPrototypeBean(REVERSE_CHARGE_REQUEST, ReverseChargeRequest.class))
				.then((Answer<ReverseChargeRequest>) invocation -> new ReverseChargeRequest());
		lenient().when(beanFactory.getPrototypeBean(PAYMENT_EVENT, PaymentEvent.class))
				.then((Answer<PaymentEvent>) invocation -> new PaymentEvent());

		lenient().when(beanFactory.getPrototypeBean(RESERVE_CAPABILITY_REQUEST, ReserveCapabilityRequest.class))
				.then((Answer<ReserveCapabilityRequest>) invocation -> new ReserveCapabilityRequest());
		lenient().when(beanFactory.getPrototypeBean(CHARGE_CAPABILITY_REQUEST, ChargeCapabilityRequest.class))
				.then((Answer<ChargeCapabilityRequest>) invocation -> new ChargeCapabilityRequest());
		lenient().when(beanFactory.getPrototypeBean(CANCEL_CAPABILITY_REQUEST, CancelCapabilityRequest.class))
				.then((Answer<CancelCapabilityRequest>) invocation -> new CancelCapabilityRequest());
		lenient().when(beanFactory.getPrototypeBean(MODIFY_CAPABILITY_REQUEST, ModifyCapabilityRequest.class))
				.then((Answer<ModifyCapabilityRequest>) invocation -> new ModifyCapabilityRequest());
		lenient().when(beanFactory.getPrototypeBean(CREDIT_CAPABILITY_REQUEST, CreditCapabilityRequest.class))
				.then((Answer<CreditCapabilityRequest>) invocation -> new CreditCapabilityRequest());
		lenient().when(beanFactory.getPrototypeBean(REVERSE_CHARGE_CAPABILITY_REQUEST, ReverseChargeCapabilityRequest.class))
				.then((Answer<ReverseChargeCapabilityRequest>) invocation -> new ReverseChargeCapabilityRequest());
	}

	/**
	 * Creates reservation modification request with reasonable defaults, referring to default reservation of $10.
	 *
	 * @param amount new amount
	 * @return request
	 */
	ModifyReservationRequest createModifyReservationRequest(final BigDecimal amount) {
		final PaymentEvent reservationEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), RESERVED_AMOUNT);
		return ModifyReservationRequestBuilder.builder()
				.withOrderPaymentInstruments(Collections.singletonList(reservationEvent.getOrderPaymentInstrumentDTO()))
				.withAmount(createMoney(amount))
				.withLedger(Collections.singletonList(reservationEvent))
				.withOrderContext(createOrderContext())
				.withCustomRequestData(CUSTOM_REQUEST_DATA)
				.build(beanFactory);
	}

	/**
	 * Creates reservation event with reasonable defaults.
	 *
	 * @param instrument order payment instrument
	 * @param amount     reserved amount
	 * @return payment event
	 */
	PaymentEvent createReservationEvent(final OrderPaymentInstrumentDTO instrument, final MoneyDTO amount) {
		final String guid = UUID.randomUUID().toString();
		return PaymentEventBuilder.aPaymentEvent()
				.withGuid(guid)
				.withParentGuid(null)
				.withReferenceId(ORDER_NUMBER)
				.withOrderPaymentInstrumentDTO(instrument)
				.withOriginalPaymentInstrument(true)
				.withAmount(amount)
				.withPaymentType(TransactionType.RESERVE)
				.withPaymentStatus(PaymentStatus.APPROVED)
				.withDate(new Date())
				.withPaymentEventData(RESERVATION_EVENT_DATA)
				.build(beanFactory);
	}

	/**
	 * Creates charged event with reasonable defaults.
	 *
	 * @param reservationPaymentEvent original reservation event
	 * @param chargedAmount           charged amount
	 * @return payment event
	 */
	PaymentEvent createChargeEvent(final PaymentEvent reservationPaymentEvent, final MoneyDTO chargedAmount) {
		final String guid = UUID.randomUUID().toString();
		calendar.add(Calendar.MINUTE, eventOrderNumber++);
		return PaymentEventBuilder.aPaymentEvent()
				.withReferenceId(ORDER_NUMBER)
				.withGuid(guid)
				.withParentGuid(reservationPaymentEvent.getGuid())
				.withOrderPaymentInstrumentDTO(reservationPaymentEvent.getOrderPaymentInstrumentDTO())
				.withOriginalPaymentInstrument(true)
				.withAmount(chargedAmount)
				.withPaymentType(TransactionType.CHARGE)
				.withPaymentStatus(PaymentStatus.APPROVED)
				.withPaymentEventData(CHARGE_EVENT_DATA)
				.build(beanFactory);
	}

	/**
	 * Creates credit event with reasonable defaults.
	 *
	 * @param reservationPaymentEvent original reservation event
	 * @param creditAmount            credit amount
	 * @return payment event
	 */
	PaymentEvent createCreditEvent(final PaymentEvent reservationPaymentEvent, final MoneyDTO creditAmount) {
		final String guid = UUID.randomUUID().toString();
		calendar.add(Calendar.MINUTE, eventOrderNumber++);
		return PaymentEventBuilder.aPaymentEvent()
				.withReferenceId(ORDER_NUMBER)
				.withGuid(guid)
				.withParentGuid(reservationPaymentEvent.getGuid())
				.withOrderPaymentInstrumentDTO(reservationPaymentEvent.getOrderPaymentInstrumentDTO())
				.withOriginalPaymentInstrument(true)
				.withAmount(creditAmount)
				.withPaymentType(TransactionType.CREDIT)
				.withPaymentStatus(PaymentStatus.APPROVED)
				.withPaymentEventData(Collections.emptyMap())
				.build(beanFactory);
	}

	/**
	 * Creates reverse charge event with reasonable defaults.
	 *
	 * @param chargeEvent charge event.
	 * @return payment event
	 */
	PaymentEvent createReverseChargeEvent(final PaymentEvent chargeEvent) {
		final String guid = UUID.randomUUID().toString();
		calendar.add(Calendar.MINUTE, eventOrderNumber++);
		return PaymentEventBuilder.aPaymentEvent()
				.withReferenceId(ORDER_NUMBER)
				.withGuid(guid)
				.withParentGuid(chargeEvent.getGuid())
				.withOrderPaymentInstrumentDTO(chargeEvent.getOrderPaymentInstrumentDTO())
				.withOriginalPaymentInstrument(true)
				.withAmount(chargeEvent.getAmount())
				.withPaymentType(TransactionType.REVERSE_CHARGE)
				.withPaymentStatus(PaymentStatus.APPROVED)
				.withPaymentEventData(Collections.emptyMap())
				.build(beanFactory);
	}

	/**
	 * Creates order payment instrument with a limit.
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

	/**
	 * Creates money DTO using USD currency code.
	 *
	 * @param amount amount of dollars
	 * @return money DTO
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
		BigDecimal total = unitPrice
				.multiply(new BigDecimal(quantity))
				.add(taxAmount)
				.subtract(discount);
		final OrderSkuDTO orderSku = OrderSkuDTOBuilder.builder()
				.withDisplayName("Device")
				.withQuantity(quantity)
				.withPrice(unitPrice)
				.withTaxAmount(taxAmount)
				.withTotal(total)
				.withSkuCode("skuCode")
				.build(new OrderSkuDTO());
		return OrderContextBuilder.builder()
				.withOrderSkus(ImmutableList.of(orderSku))
				.withOrderTotal(createMoney(BigDecimal.TEN))
				.withOrderNumber(ORDER_NUMBER)
				.withCustomerEmail(CUSTOMER_EMAIL)
				.withBillingAddress(BILLING_ADDRESS)
				.build(new OrderContext());
	}

	/**
	 * Creates reservation request with reasonable defaults.
	 *
	 * @return request
	 */
	ReserveRequest createReservationRequest() {
		return ReserveRequestBuilder.builder()
				.withSelectedOrderPaymentInstruments(Collections.singletonList(createOrderPaymentInstrumentDTO()))
				.withAmount(RESERVED_AMOUNT)
				.withOrderContext(createOrderContext())
				.withCustomRequestData(CUSTOM_REQUEST_DATA)
				.build(beanFactory);
	}

	/**
	 * Creates charge request with reasonable defaults.
	 *
	 * @param ledger ledger.
	 * @param orderTotal order total amount.
	 * @return request
	 */
	ChargeRequest createChargeRequest(final List<PaymentEvent> ledger, final MoneyDTO orderTotal) {
		final OrderContext orderContext = new OrderContext();
		orderContext.setOrderTotal(orderTotal);
		orderContext.setOrderNumber(ORDER_NUMBER);

		return ChargeRequestBuilder.builder()
				.withLedger(ledger)
				.withOrderContext(orderContext)
				.withCustomRequestData(Collections.emptyMap())
				.withFinalPayment(false)
				.withTotalChargeableAmount(orderTotal)
				.withOrderPaymentInstruments(Collections.emptyList())
				.build(beanFactory);
	}

	/**
	 * Creates reservation cancellation request with reasonable defaults, referring to default reservation of $10.
	 *
	 * @return request
	 */
	CancelReservationRequest createCancelReservationRequest() {
		final PaymentEvent reservationEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), RESERVED_AMOUNT);
		return CancelReservationRequestBuilder.builder()
				.withOrderPaymentInstruments(Collections.singletonList(reservationEvent.getOrderPaymentInstrumentDTO()))
				.withSelectedPaymentEventsToCancel(Collections.singletonList(reservationEvent))
				.withLedger(Collections.singletonList(reservationEvent))
				.withAmount(RESERVED_AMOUNT)
				.withCustomRequestData(CUSTOM_REQUEST_DATA)
				.withOrderContext(createOrderContext())
				.build(beanFactory);
	}

	/**
	 * Creates reservation cancellation request with reasonable defaults, referring to default reservation of $10.
	 *
	 * @return request
	 */
	CancelAllReservationsRequest createCancelAllReservationsRequest() {
		final PaymentEvent firstReservationEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), RESERVED_AMOUNT);
		final PaymentEvent secondReservationEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), RESERVED_AMOUNT);
		return CancelAllReservationsRequestBuilder.builder()
				.withOrderPaymentInstruments(Arrays.asList(firstReservationEvent.getOrderPaymentInstrumentDTO(),
						secondReservationEvent.getOrderPaymentInstrumentDTO()))
				.withLedger(Arrays.asList(firstReservationEvent, secondReservationEvent))
				.withCustomRequestData(CUSTOM_REQUEST_DATA)
				.withOrderContext(createOrderContext())
				.build(beanFactory);
	}

	/**
	 * Create credit request.
	 *
	 * @param paymentEvents  list of payment events.
	 * @param amountToRefund amount to refund
	 * @return request
	 */
	CreditRequest createCreditRequest(final List<PaymentEvent> paymentEvents, final MoneyDTO amountToRefund) {
		return CreditRequestBuilder.builder()
				.withOrderPaymentInstruments(paymentEvents
						.stream()
						.filter(event -> event.getPaymentType() == TransactionType.CHARGE)
						.map(PaymentEvent::getOrderPaymentInstrumentDTO)
						.distinct()
						.collect(Collectors.toList()))
				.withSelectedOrderPaymentInstruments(Collections.emptyList())
				.withLedger(paymentEvents)
				.withAmount(amountToRefund)
				.withCustomRequestData(CUSTOM_REQUEST_DATA)
				.withOrderContext(createOrderContext())
				.build(beanFactory);
	}

	/**
	 * Simulates cancel capability approving all requests.
	 *
	 * @param selectedPaymentEventsToCancel payment events to cancel
	 * @return payment API response
	 */
	PaymentAPIResponse simulateCancel(final List<PaymentEvent> selectedPaymentEventsToCancel) {
		List<PaymentEvent> paymentEvents = new ArrayList<>();
		for (PaymentEvent paymentEvent : selectedPaymentEventsToCancel) {
			paymentEvents.add(PaymentEventBuilder.aPaymentEvent()
					.withReferenceId(ORDER_NUMBER)
					.withParentGuid(paymentEvent.getGuid())
					.withOrderPaymentInstrumentDTO(paymentEvent.getOrderPaymentInstrumentDTO())
					.withOriginalPaymentInstrument(paymentEvent.isOriginalPaymentInstrument())
					.withAmount(paymentEvent.getAmount())
					.withPaymentType(TransactionType.CANCEL_RESERVE)
					.withPaymentStatus(PaymentStatus.APPROVED)
					.withPaymentEventData(CANCEL_EVENT_DATA)
					.build(beanFactory));
		}
		return new PaymentAPIResponse(paymentEvents, true);
	}

	/**
	 * Simulates cancel capability failing all requests.
	 *
	 * @param selectedPaymentEventsToCancel payment events to cancel
	 * @return payment API response
	 */
	PaymentAPIResponse simulateCancelFailure(final List<PaymentEvent> selectedPaymentEventsToCancel) {
		List<PaymentEvent> paymentEvents = new ArrayList<>();
		for (PaymentEvent paymentEvent : selectedPaymentEventsToCancel) {
			paymentEvents.add(PaymentEventBuilder.aPaymentEvent()
					.withReferenceId(ORDER_NUMBER)
					.withParentGuid(paymentEvent.getGuid())
					.withOrderPaymentInstrumentDTO(paymentEvent.getOrderPaymentInstrumentDTO())
					.withOriginalPaymentInstrument(paymentEvent.isOriginalPaymentInstrument())
					.withAmount(paymentEvent.getAmount())
					.withPaymentType(TransactionType.CANCEL_RESERVE)
					.withPaymentStatus(PaymentStatus.FAILED)
					.withPaymentEventData(CANCEL_EVENT_DATA)
					.build(beanFactory));
		}
		return new PaymentAPIResponse(paymentEvents, false);
	}

	/**
	 * Simulates cancel capability skipping all requests.
	 *
	 * @param selectedPaymentEventsToCancel payment events to cancel
	 * @return payment API response
	 */
	PaymentAPIResponse simulateCancelSkip(final List<PaymentEvent> selectedPaymentEventsToCancel) {
		List<PaymentEvent> paymentEvents = new ArrayList<>();
		for (PaymentEvent paymentEvent : selectedPaymentEventsToCancel) {
			paymentEvents.add(PaymentEventBuilder.aPaymentEvent()
					.withReferenceId(ORDER_NUMBER)
					.withParentGuid(paymentEvent.getGuid())
					.withOrderPaymentInstrumentDTO(paymentEvent.getOrderPaymentInstrumentDTO())
					.withOriginalPaymentInstrument(paymentEvent.isOriginalPaymentInstrument())
					.withAmount(paymentEvent.getAmount())
					.withPaymentType(TransactionType.CANCEL_RESERVE)
					.withPaymentStatus(PaymentStatus.SKIPPED)
					.withPaymentEventData(CANCEL_EVENT_DATA)
					.build(beanFactory));
		}
		return new PaymentAPIResponse(paymentEvents, false);
	}

	/**
	 * Mocks any payment capability response.
	 *
	 * @param data response data
	 * @return response mock
	 */
	PaymentCapabilityResponse mockPaymentCapabilityResponse(final Map<String, String> data) {
		final PaymentCapabilityResponse paymentCapabilityResponse = mock(PaymentCapabilityResponse.class);
		when(paymentCapabilityResponse.getData()).thenReturn(data);
		lenient().when(paymentCapabilityResponse.getProcessedDateTime()).thenReturn(LocalDateTime.now());
		return paymentCapabilityResponse;
	}

	/**
	 * Checks that modify response maps all values correctly and has single event.
	 *
	 * @param response        modify response
	 * @param possibleAmounts possible money amounts of single event
	 */
	void checkModifyResponse(final PaymentAPIResponse response, final BigDecimal... possibleAmounts) {
		assertThat(response).extracting(paymentAPIResponse -> paymentAPIResponse.getEvents().size()).isEqualTo(1);
		assertThat(response.getEvents()).extracting(PaymentEvent::getAmount).extracting(MoneyDTO::getAmount).containsAnyOf(possibleAmounts);
		assertThat(response.getEvents()).extracting(PaymentEvent::getAmount).extracting(MoneyDTO::getCurrencyCode).containsOnly(CURRENCY_CODE);
		assertThat(response.getEvents()).extracting(PaymentEvent::getReferenceId).containsOnly(ORDER_NUMBER);
		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentType).containsOnly(TransactionType.MODIFY_RESERVE);
		assertThat(response.getEvents()).extracting(PaymentEvent::getOrderPaymentInstrumentDTO).allSatisfy(this::checkOrderPaymentInstrument);
	}

	/**
	 * Checks that reservation response maps all values correctly and has single event reserving $10.
	 *
	 * @param response reservation response
	 */
	void checkReservationResponse(final PaymentAPIResponse response) {
		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response).extracting(paymentAPIResponse -> paymentAPIResponse.getEvents().size()).isEqualTo(1);
		assertThat(response.getEvents()).allSatisfy(paymentEvent ->
				checkReservationPaymentEvent(paymentEvent, RESERVED_AMOUNT.getAmount()));
	}

	/**
	 * Checks that single payment event was approved and capability response data is mapped correctly.
	 *
	 * @param response Payment API response
	 */
	void checkSinglePaymentEventApproved(final PaymentAPIResponse response) {
		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentStatus).containsOnly(PaymentStatus.APPROVED);
		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentEventData).contains(RESPONSE_DATA, Index.atIndex(0));
	}

	/**
	 * Checks that response failed and capability exception data is mapped correctly.
	 *
	 * @param response Payment API failing response
	 */
	void checkSinglePaymentEventFailed(final PaymentAPIResponse response) {
		assertThat(response.isSuccess()).isEqualTo(false);
		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentStatus).containsOnly(PaymentStatus.FAILED);
		assertThat(response.getEvents()).extracting(PaymentEvent::getInternalMessage).containsOnly(CAPABILITY_EXCEPTION.getInternalMessage());
		assertThat(response.getEvents()).extracting(PaymentEvent::getExternalMessage).containsOnly(CAPABILITY_EXCEPTION.getExternalMessage());
		assertThat(response.getEvents()).extracting(PaymentEvent::isTemporaryFailure).containsOnly(CAPABILITY_EXCEPTION.isTemporaryFailure());
	}

	/**
	 * Checks that response failed and due to capability producing skipped event.
	 *
	 * @param response Payment API response
	 */
	void checkSinglePaymentEventSkipped(final PaymentAPIResponse response) {
		assertThat(response).extracting(paymentAPIResponse -> paymentAPIResponse.getEvents().size()).isEqualTo(1);
		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentStatus).containsOnly(PaymentStatus.SKIPPED);
	}

	/**
	 * Checks that cancel reservation capability request maps all values correctly.
	 *
	 * @param request         cancel reservation capability request
	 * @param possibleAmounts possible money amounts
	 */
	void checkRequest(final CancelReservationRequest request, final BigDecimal... possibleAmounts) {
		assertThat(request).extracting(reservationRequest -> reservationRequest.getAmount().getAmount()).isIn((Object[]) possibleAmounts);
		assertThat(request).extracting(reservationRequest -> reservationRequest.getAmount().getCurrencyCode()).isEqualTo(CURRENCY_CODE);
		assertThat(request).extracting(CancelReservationRequest::getCustomRequestData).isEqualTo(CUSTOM_REQUEST_DATA);
		assertThat(request.getLedger().size()).isEqualTo(1);
		assertThat(request.getLedger()).allSatisfy(paymentEvent ->
				checkReservationPaymentEvent(paymentEvent, possibleAmounts));
		assertThat(request.getSelectedPaymentEventsToCancel().size()).isEqualTo(1);
		assertThat(request.getSelectedPaymentEventsToCancel()).allSatisfy(paymentEvent ->
				checkReservationPaymentEvent(paymentEvent, possibleAmounts));
	}

	private void checkReservationPaymentEvent(final PaymentEvent paymentEvent, final BigDecimal... possibleAmounts) {
		assertThat(paymentEvent).extracting(reserveRequest -> reserveRequest.getAmount().getAmount()).isIn((Object[]) possibleAmounts);
		assertThat(paymentEvent).extracting(reserveRequest -> reserveRequest.getAmount().getCurrencyCode()).isEqualTo(CURRENCY_CODE);
		assertThat(paymentEvent).extracting(PaymentEvent::getReferenceId).isEqualTo(ORDER_NUMBER);
		assertThat(paymentEvent).extracting(PaymentEvent::getPaymentType).isEqualTo(TransactionType.RESERVE);
		assertThat(paymentEvent).extracting(PaymentEvent::getPaymentStatus).isEqualTo(PaymentStatus.APPROVED);
		assertThat(paymentEvent).extracting(PaymentEvent::getPaymentEventData).isEqualTo(RESERVATION_EVENT_DATA);
		checkOrderPaymentInstrument(paymentEvent.getOrderPaymentInstrumentDTO());
	}

	/**
	 * Checks that reservation capability request maps all values correctly.
	 *
	 * @param request         reservation capability request
	 * @param possibleAmounts possible money amounts
	 */
	void checkReservationRequest(final ReserveRequest request, final BigDecimal... possibleAmounts) {
		assertThat(request).extracting(reserveRequest -> reserveRequest.getAmount().getAmount()).isIn((Object[]) possibleAmounts);
		assertThat(request).extracting(reserveRequest -> reserveRequest.getAmount().getCurrencyCode()).isEqualTo(CURRENCY_CODE);
		assertThat(request).extracting(ReserveRequest::getCustomRequestData).isEqualTo(CUSTOM_REQUEST_DATA);
		assertThat(request.getOrderContext()).isEqualToComparingFieldByFieldRecursively(createOrderContext());
		assertThat(request.getSelectedOrderPaymentInstruments()).allSatisfy(this::checkOrderPaymentInstrument);
	}

	private void checkOrderPaymentInstrument(final OrderPaymentInstrumentDTO orderPaymentInstrumentDTO) {
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
	 * Checks that modify reservation capability request maps all values correctly.
	 *
	 * @param request         modify reservation capability request
	 * @param possibleAmounts possible money amounts
	 */
	void checkCapabilityRequest(final ModifyCapabilityRequest request, final BigDecimal... possibleAmounts) {
		assertThat(request).extracting(modifyCapabilityRequest -> modifyCapabilityRequest.getAmount().getAmount()).isIn((Object[]) possibleAmounts);
		assertThat(request).extracting(modifyCapabilityRequest -> modifyCapabilityRequest.getAmount().getCurrencyCode()).isEqualTo(CURRENCY_CODE);

		assertThat(request).extracting(ModifyCapabilityRequest::getPaymentInstrumentData).isEqualTo(INSTRUMENT_DATA);
		assertThat(request).extracting(ModifyCapabilityRequest::getReservationData).isEqualTo(RESERVATION_EVENT_DATA);
		assertThat(request).extracting(ModifyCapabilityRequest::getCustomRequestData).isEqualTo(CUSTOM_REQUEST_DATA);

		checkOrderContext(request);
	}

	/**
	 * Checks that credit capability request maps all values correctly.
	 *
	 * @param request         credit capability request
	 * @param possibleAmounts possible money amounts
	 */
	void checkCapabilityRequest(final CreditCapabilityRequest request, final BigDecimal... possibleAmounts) {
		assertThat(request).extracting(creditCapabilityRequest -> creditCapabilityRequest.getAmount().getAmount()).isIn((Object[]) possibleAmounts);
		assertThat(request).extracting(creditCapabilityRequest -> creditCapabilityRequest.getAmount().getCurrencyCode()).isEqualTo(CURRENCY_CODE);

		assertThat(request).extracting(CreditCapabilityRequest::getPaymentInstrumentData).isEqualTo(INSTRUMENT_DATA);
		assertThat(request).extracting(CreditCapabilityRequest::getCustomRequestData).isEqualTo(CUSTOM_REQUEST_DATA);
		assertThat(request).extracting(CreditCapabilityRequest::getChargeData).isEqualTo(CHARGE_EVENT_DATA);

		checkOrderContext(request);
	}

	/**
	 * Checks that reverse charge capability request maps all values correctly.
	 *
	 * @param request reverse charge capability request
	 */
	void checkCapabilityRequest(final ReverseChargeCapabilityRequest request) {
		assertThat(request).extracting(ReverseChargeCapabilityRequest::getPaymentInstrumentData).isEqualTo(INSTRUMENT_DATA);
		assertThat(request).extracting(ReverseChargeCapabilityRequest::getCustomRequestData).isEqualTo(CUSTOM_REQUEST_DATA);
		assertThat(request).extracting(ReverseChargeCapabilityRequest::getChargeData).isEqualTo(CHARGE_EVENT_DATA);
	}

	/**
	 * Checks that cancel reservation capability request maps all values correctly.
	 *
	 * @param request         cancel reservation capability request
	 * @param possibleAmounts possible money amounts
	 */
	void checkCapabilityRequest(final CancelCapabilityRequest request, final BigDecimal... possibleAmounts) {
		assertThat(request).extracting(modifyCapabilityRequest -> modifyCapabilityRequest.getAmount().getAmount()).isIn((Object[]) possibleAmounts);
		assertThat(request).extracting(modifyCapabilityRequest -> modifyCapabilityRequest.getAmount().getCurrencyCode()).isEqualTo(CURRENCY_CODE);

		assertThat(request).extracting(CancelCapabilityRequest::getPaymentInstrumentData).isEqualTo(INSTRUMENT_DATA);
		assertThat(request).extracting(CancelCapabilityRequest::getReservationData).isEqualTo(RESERVATION_EVENT_DATA);
		assertThat(request).extracting(CancelCapabilityRequest::getCustomRequestData).isEqualTo(CUSTOM_REQUEST_DATA);

		checkOrderContext(request);
	}

	private void checkOrderContext(final PaymentCapabilityRequest request) {
		assertThat(request.getOrderContext()).extracting(OrderContext::getBillingAddress).isEqualTo(BILLING_ADDRESS);
		assertThat(request.getOrderContext()).extracting(OrderContext::getCustomerEmail).isEqualTo(CUSTOMER_EMAIL);
		assertThat(request.getOrderContext()).extracting(OrderContext::getOrderNumber).isEqualTo(ORDER_NUMBER);
	}

	/**
	 * Gets EP bean factory.
	 *
	 * @return EP bean factory
	 */
	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}

}
