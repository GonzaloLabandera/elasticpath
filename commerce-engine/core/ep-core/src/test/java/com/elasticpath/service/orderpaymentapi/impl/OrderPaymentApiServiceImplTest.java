/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.orderpaymentapi.impl;

import static com.elasticpath.commons.constants.ContextIdNames.ORDER_PAYMENT;
import static com.elasticpath.commons.constants.ContextIdNames.ORDER_PAYMENT_AMOUNTS;
import static com.elasticpath.commons.constants.ContextIdNames.PAYMENT_ADDRESS_DTO;
import static com.elasticpath.commons.constants.ContextIdNames.PAYMENT_CUSTOMER_CONTEXT_DTO;
import static com.elasticpath.commons.constants.ContextIdNames.PAYMENT_MONEY_DTO;
import static com.elasticpath.commons.constants.ContextIdNames.PAYMENT_ORDER_CONTEXT_DTO;
import static com.elasticpath.commons.constants.ContextIdNames.PIC_FIELDS_REQUEST_CONTEXT_DTO;
import static com.elasticpath.commons.constants.ContextIdNames.PIC_REQUEST_CONTEXT_DTO;
import static com.elasticpath.domain.order.OrderPaymentStatus.APPROVED;
import static com.elasticpath.domain.order.OrderPaymentStatus.FAILED;
import static com.elasticpath.domain.order.OrderPaymentStatus.SKIPPED;
import static com.elasticpath.domain.order.OrderShipmentStatus.CANCELLED;
import static com.elasticpath.domain.order.OrderShipmentStatus.INVENTORY_ASSIGNED;
import static com.elasticpath.domain.order.OrderShipmentStatus.RELEASED;
import static com.elasticpath.domain.order.OrderShipmentStatus.SHIPPED;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.CANCEL_RESERVE;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.CHARGE;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.CREDIT;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.MANUAL_CREDIT;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.MODIFY_RESERVE;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.RESERVE;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.CHARGE_REQUEST;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.CREDIT_REQUEST;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.MANUAL_CREDIT_REQUEST;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.MODIFY_RESERVATION_REQUEST;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.ORDER_PAYMENT_INSTRUMENT_DTO;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PAYMENT_EVENT;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PAYMENT_EVENT_HISTORY_REQUEST;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PAYMENT_EVENT_HISTORY_RESPONSE;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PAYMENT_INSTRUMENT_DTO;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PIC_FIELDS_DTO;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PIC_INSTRUCTIONS_DTO;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PIC_INSTRUCTIONS_FIELDS_DTO;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.RESERVE_REQUEST;
import static com.elasticpath.provider.payment.service.PaymentsExceptionMessageId.PAYMENT_FAILED;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.ImmutableMap;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.event.OrderEventHelper;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderAddress;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.domain.orderpaymentapi.OrderPayment;
import com.elasticpath.domain.orderpaymentapi.OrderPaymentAmounts;
import com.elasticpath.domain.orderpaymentapi.OrderPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.impl.CustomerContext;
import com.elasticpath.domain.orderpaymentapi.impl.OrderPaymentImpl;
import com.elasticpath.domain.orderpaymentapi.impl.PICFieldsRequestContext;
import com.elasticpath.domain.orderpaymentapi.impl.PICRequestContext;
import com.elasticpath.money.Money;
import com.elasticpath.plugin.payment.provider.dto.AddressDTO;
import com.elasticpath.plugin.payment.provider.dto.CustomerContextDTO;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTOBuilder;
import com.elasticpath.plugin.payment.provider.dto.OrderContext;
import com.elasticpath.plugin.payment.provider.dto.PICFieldsRequestContextDTO;
import com.elasticpath.plugin.payment.provider.dto.PICRequestContextDTO;
import com.elasticpath.plugin.payment.provider.dto.PaymentStatus;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.provider.payment.domain.history.PaymentEventHistoryRequest;
import com.elasticpath.provider.payment.domain.history.PaymentEventHistoryResponse;
import com.elasticpath.provider.payment.domain.history.PaymentEventHistoryResponseBuilder;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIResponse;
import com.elasticpath.provider.payment.domain.transaction.charge.ChargeRequest;
import com.elasticpath.provider.payment.domain.transaction.credit.CreditRequest;
import com.elasticpath.provider.payment.domain.transaction.credit.ManualCreditRequest;
import com.elasticpath.provider.payment.domain.transaction.modify.ModifyReservationRequest;
import com.elasticpath.provider.payment.domain.transaction.reservation.ReserveRequest;
import com.elasticpath.provider.payment.service.PaymentsException;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.instructions.PICInstructionsDTO;
import com.elasticpath.provider.payment.service.instructions.PICInstructionsDTOBuilder;
import com.elasticpath.provider.payment.service.instructions.PICInstructionsFieldsDTO;
import com.elasticpath.provider.payment.service.instructions.PICInstructionsFieldsDTOBuilder;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentCreationFieldsDTO;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentCreationFieldsDTOBuilder;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTOBuilder;
import com.elasticpath.provider.payment.workflow.PaymentAPIWorkflow;
import com.elasticpath.provider.payment.workflow.PaymentHistoryWorkflow;
import com.elasticpath.provider.payment.workflow.PaymentInstrumentWorkflow;
import com.elasticpath.service.customer.dao.CustomerAddressDao;
import com.elasticpath.service.orderpaymentapi.OrderPaymentInstrumentService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentService;

/**
 * Unit test for {@link OrderPaymentApiServiceImpl}.
 */
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods", "PMD.CouplingBetweenObjects"})
@RunWith(MockitoJUnitRunner.class)
public class OrderPaymentApiServiceImplTest {
	private static final String PAYMENT_PROVIDER_CONFIG_GUID = "paymentProviderConfigGuid";
	private static final String PAYMENT_INSTRUMENT_GUID = "paymentInstrumentGuid";
	private static final BigDecimal AMOUNT = BigDecimal.valueOf(20);
	private static final BigDecimal NEGATIVE_AMOUNT = BigDecimal.valueOf(-1);
	private static final BigDecimal AMOUNT_18 = AMOUNT.subtract(BigDecimal.ONE).subtract(BigDecimal.ONE);
	private static final BigDecimal AMOUNT_1 = BigDecimal.ONE;
	private static final BigDecimal UNLIMITED_AMOUNT = BigDecimal.ZERO;
	private static final String PAYMENT_EVENT_DATA_KEY = "key";
	private static final String RESERVATION_GUID = "reservationGuid";
	private static final String PAYMENT_EVENT_DATA_VALUE = "value";
	private static final String CURRENCY_CODE = "AUD";
	private static final String CUSTOMER_EMAIL = "Email";
	private static final String CUSTOMER_FIRST_NAME = "firstName";
	private static final String CUSTOMER_LAST_NAME = "lastName";
	private static final String CUSTOMER_ID = "customerId";
	private static final String ORDER_NUMBER = "OrderNumber";
	private static final String ORDER_GUID = "orderGuid";
	private static final int RESERVATION_ORDER_PAYMENT_UID_PK = 12345;
	private static final Currency CURRENCY = Currency.getInstance("USD");
	private static final Locale LOCALE = Locale.CANADA;
	private static final BigDecimal ZERO = BigDecimal.ZERO;
	private static final BigDecimal FRACTIONAL_VALUE = BigDecimal.valueOf(0.75);
	private static final String BILLING_ADDRESS_GUID = "billingAddressGuid";
	public static final BigDecimal TEN = BigDecimal.TEN;
	public static final String EXTERNAL_MESSAGE_FOR_RESPONSE = "external message";
	public static final String INTERNAL_MESSAGE_FOR_RESPONSE = "internal message";

	@Mock
	private PaymentAPIWorkflow paymentAPIWorkflow;

	@Mock
	private PaymentHistoryWorkflow paymentHistoryWorkflow;

	@Mock
	private BeanFactory beanFactory;

	@Mock
	private OrderPaymentInstrumentService orderPaymentInstrumentService;

	@Mock
	private OrderPaymentService orderPaymentService;

	@Mock
	private OrderEventHelper orderEventHelper;

	@Mock
	private PaymentInstrumentWorkflow paymentInstrumentWorkflow;

	@Mock
	private CustomerAddressDao customerAddressDao;

	@InjectMocks
	private OrderPaymentApiServiceImpl orderPaymentApiService;

	@Captor
	private ArgumentCaptor<ModifyReservationRequest> modifyReservationRequestArgumentCaptor;

	@Captor
	private ArgumentCaptor<ChargeRequest> chargeRequestArgumentCaptor;

	@Captor
	private ArgumentCaptor<PICRequestContextDTO> contextDTOArgumentCaptor;

	private OrderPayment orderPayment;
	private Order order;
	private OrderShipment orderShipment;

	@Before
	public void setUp() {
		when(beanFactory.getPrototypeBean(ORDER_PAYMENT_INSTRUMENT_DTO, OrderPaymentInstrumentDTO.class))
				.thenAnswer((Answer<OrderPaymentInstrumentDTO>) invocation -> new OrderPaymentInstrumentDTO());
		when(beanFactory.getPrototypeBean(PAYMENT_INSTRUMENT_DTO, PaymentInstrumentDTO.class))
				.thenAnswer((Answer<PaymentInstrumentDTO>) invocation -> new PaymentInstrumentDTO());
		when(beanFactory.getPrototypeBean(PAYMENT_ADDRESS_DTO, AddressDTO.class))
				.thenAnswer((Answer<AddressDTO>) invocation -> new AddressDTO());
		when(beanFactory.getPrototypeBean(PAYMENT_MONEY_DTO, MoneyDTO.class))
				.thenAnswer((Answer<MoneyDTO>) invocation -> new MoneyDTO());
		when(beanFactory.getPrototypeBean(PAYMENT_EVENT, PaymentEvent.class))
				.thenAnswer((Answer<PaymentEvent>) invocation -> new PaymentEvent());
		when(beanFactory.getPrototypeBean(PAYMENT_ORDER_CONTEXT_DTO, OrderContext.class))
				.thenAnswer((Answer<OrderContext>) invocation -> new OrderContext());
		when(beanFactory.getPrototypeBean(PAYMENT_CUSTOMER_CONTEXT_DTO, CustomerContextDTO.class))
				.thenAnswer((Answer<CustomerContextDTO>) invocation -> new CustomerContextDTO());
		when(beanFactory.getPrototypeBean(PIC_REQUEST_CONTEXT_DTO, PICRequestContextDTO.class))
				.thenAnswer((Answer<PICRequestContextDTO>) invocation -> new PICRequestContextDTO());
		when(beanFactory.getPrototypeBean(PIC_FIELDS_REQUEST_CONTEXT_DTO, PICFieldsRequestContextDTO.class))
				.thenAnswer((Answer<PICFieldsRequestContextDTO>) invocation -> new PICFieldsRequestContextDTO());
		when(beanFactory.getPrototypeBean(PIC_FIELDS_DTO, PaymentInstrumentCreationFieldsDTO.class))
				.thenAnswer((Answer<PaymentInstrumentCreationFieldsDTO>) invocation -> new PaymentInstrumentCreationFieldsDTO());
		when(beanFactory.getPrototypeBean(PIC_INSTRUCTIONS_FIELDS_DTO, PICInstructionsFieldsDTO.class))
				.thenAnswer((Answer<PICInstructionsFieldsDTO>) invocation -> new PICInstructionsFieldsDTO());
		when(beanFactory.getPrototypeBean(PIC_INSTRUCTIONS_DTO, PICInstructionsDTO.class))
				.thenAnswer((Answer<PICInstructionsDTO>) invocation -> new PICInstructionsDTO());
		when(beanFactory.getPrototypeBean(ORDER_PAYMENT_AMOUNTS, OrderPaymentAmounts.class))
				.thenReturn(new OrderPaymentAmounts());

		when(beanFactory.getPrototypeBean(RESERVE_REQUEST, ReserveRequest.class))
				.thenReturn(new ReserveRequest());
		when(beanFactory.getPrototypeBean(CHARGE_REQUEST, ChargeRequest.class))
				.thenReturn(new ChargeRequest());
		when(beanFactory.getPrototypeBean(MODIFY_RESERVATION_REQUEST, ModifyReservationRequest.class))
				.thenReturn(new ModifyReservationRequest());
		when(beanFactory.getPrototypeBean(CREDIT_REQUEST, CreditRequest.class))
				.thenReturn(new CreditRequest());
		when(beanFactory.getPrototypeBean(MANUAL_CREDIT_REQUEST, ManualCreditRequest.class))
				.thenReturn(new ManualCreditRequest());
		when(beanFactory.getPrototypeBean(PAYMENT_EVENT_HISTORY_REQUEST, PaymentEventHistoryRequest.class))
				.thenReturn(new PaymentEventHistoryRequest());
        when(beanFactory.getPrototypeBean(PAYMENT_EVENT_HISTORY_RESPONSE, PaymentEventHistoryResponse.class))
                .thenReturn(new PaymentEventHistoryResponse());

        final PaymentInstrumentDTO paymentInstrumentDTO = createTestPaymentInstrumentDTO();
        when(paymentInstrumentWorkflow.findByGuid(PAYMENT_INSTRUMENT_GUID)).thenReturn(paymentInstrumentDTO);

        orderPayment = new OrderPaymentImpl();
        when(beanFactory.getPrototypeBean(ORDER_PAYMENT, OrderPayment.class)).thenReturn(orderPayment);

        order = createOrder();
        final OrderPaymentInstrument orderPaymentInstrument = createOrderPaymentInstrument();
        when(orderPaymentInstrumentService.findByOrder(order)).thenReturn(singletonList(orderPaymentInstrument));
        when(orderPaymentInstrumentService.findByOrderPayment(any(OrderPayment.class))).thenReturn(orderPaymentInstrument);

        orderShipment = mockOrderShipment();

        final List<OrderPayment> orderPayments = singletonList(createReservationOrderPayment());
        when(orderPaymentService.findByOrder(order)).thenReturn(orderPayments);
        final OrderAddress customerAddress = mockOrderAddress();
        when(customerAddressDao.findByGuid(BILLING_ADDRESS_GUID)).thenReturn(customerAddress);
    }

	private OrderPayment createReservationOrderPayment() {
		final OrderPayment orderPayment = new OrderPaymentImpl();

		orderPayment.setUidPk(RESERVATION_ORDER_PAYMENT_UID_PK);
		orderPayment.setGuid(RESERVATION_GUID);
        orderPayment.setParentOrderPaymentGuid(orderPayment.getGuid());

        orderPayment.setCreatedDate(DateTime.now().toDate());

        orderPayment.setAmount(AMOUNT);
        orderPayment.setCurrency(Currency.getInstance("USD"));

        orderPayment.setPaymentInstrumentGuid(PAYMENT_INSTRUMENT_GUID);
        orderPayment.setOriginalPI(true);

        orderPayment.setOrderNumber(order.getOrderNumber());
        orderPayment.setOrderPaymentData(emptySet());

        orderPayment.setOrderPaymentStatus(APPROVED);
        orderPayment.setTransactionType(RESERVE);
        return orderPayment;
    }

	@Test
	public void testGetPICInstructionFieldsShouldReturnPICInstructionsFieldsFromPaymentAPIWorkflow() {
		final String field = "Field";
		final PICInstructionsFieldsDTO fieldsDTO = PICInstructionsFieldsDTOBuilder.builder()
				.withFields(singletonList(field))
				.withStructuredErrorMessages(emptyList())
				.build(beanFactory);
		when(paymentAPIWorkflow.getPICInstructionFields(any(), any())).thenReturn(fieldsDTO);

		final PICFieldsRequestContext picFieldsRequestContext = mockPICFieldsRequestContext();
		final PICInstructionsFieldsDTO picInstructionFields = orderPaymentApiService.getPICInstructionsFields(
				PAYMENT_PROVIDER_CONFIG_GUID, picFieldsRequestContext);

		assertThat(picInstructionFields.getFields()).containsOnly(field);
	}

	@Test
	public void testGetPICInstructionsShouldReturnPICInstructionsFromPaymentAPIWorkflow() {
		final String expectedControlKey = "expectedControlKey";
		final String expectedControlValue = "expectedControlValue";
		final String expectedPayloadKey = "expectedPayloadKey";
		final String expectedPayloadValue = "expectedPayloadValue";
		final Map<String, String> communicationInstructions = Collections.singletonMap(expectedControlKey, expectedControlValue);
		final Map<String, String> expectedPayload = Collections.singletonMap(expectedPayloadKey, expectedPayloadValue);
		final Map<String, String> formData = Collections.singletonMap(PAYMENT_EVENT_DATA_KEY, PAYMENT_EVENT_DATA_VALUE);
		final PICInstructionsDTO instructionsDTO = PICInstructionsDTOBuilder.builder()
				.withCommunicationInstructions(communicationInstructions)
				.withPayload(expectedPayload)
				.build(beanFactory);
		when(paymentAPIWorkflow.getPICInstructions(eq(PAYMENT_PROVIDER_CONFIG_GUID), eq(formData), any())).thenReturn(instructionsDTO);

		final PICInstructionsDTO picInstructions = orderPaymentApiService.getPICInstructions(
				PAYMENT_PROVIDER_CONFIG_GUID, formData, mockPICRequestContext());

		assertThat(picInstructions.getCommunicationInstructions().keySet()).containsOnly(expectedControlKey);
		assertThat(picInstructions.getCommunicationInstructions().values()).containsOnly(expectedControlValue);
		assertThat(picInstructions.getPayload().keySet()).containsOnly(expectedPayloadKey);
		assertThat(picInstructions.getPayload().values()).containsOnly(expectedPayloadValue);
		verify(paymentAPIWorkflow).getPICInstructions(eq(PAYMENT_PROVIDER_CONFIG_GUID), eq(formData), contextDTOArgumentCaptor.capture());
		final PICRequestContextDTO capturedContext = contextDTOArgumentCaptor.getValue();
		assertThat(capturedContext.getAddressDTO()).isEqualToComparingFieldByField(createTestAddressDTO());
		assertThat(capturedContext.getCurrency()).isEqualTo(CURRENCY);
		assertThat(capturedContext.getLocale()).isEqualTo(LOCALE);
		assertThat(capturedContext.getCustomerContextDTO().getUserId()).isEqualTo(CUSTOMER_ID);
		assertThat(capturedContext.getCustomerContextDTO().getEmail()).isEqualTo(CUSTOMER_EMAIL);
		assertThat(capturedContext.getCustomerContextDTO().getFirstName()).isEqualTo(CUSTOMER_FIRST_NAME);
		assertThat(capturedContext.getCustomerContextDTO().getLastName()).isEqualTo(CUSTOMER_LAST_NAME);
	}

	@Test
	public void testGetPICFieldsShouldReturnPICFieldsFromPaymentAPIWorkflow() {
		final String field = "Field";
		final boolean saveable = false;
		final PaymentInstrumentCreationFieldsDTO fieldsDTO = PaymentInstrumentCreationFieldsDTOBuilder.builder()
				.withFields(singletonList(field))
				.withBlockingFields(emptyList())
				.withIsSaveable(saveable)
				.build(beanFactory);
		when(paymentAPIWorkflow.getPICFields(any(), any())).thenReturn(fieldsDTO);

		final PICFieldsRequestContext picFieldsRequestContext = mockPICFieldsRequestContext();
		final PaymentInstrumentCreationFieldsDTO picFields = orderPaymentApiService.getPICFields(PAYMENT_PROVIDER_CONFIG_GUID,
				picFieldsRequestContext);

		assertThat(picFields.getFields()).containsOnly(field);
		assertThat(picFields.isSaveable()).isEqualTo(saveable);
	}

	@Test
	public void testCreatePI() {
		final Map<String, String> instrumentFormData = ImmutableMap.of(PAYMENT_EVENT_DATA_KEY, "data");
		when(paymentAPIWorkflow.createPI(eq(PAYMENT_PROVIDER_CONFIG_GUID), eq(instrumentFormData), any())).thenReturn(PAYMENT_INSTRUMENT_GUID);

		final PICRequestContext picRequestContext = mockPICRequestContext();
		String guid = orderPaymentApiService.createPI(PAYMENT_PROVIDER_CONFIG_GUID, instrumentFormData, picRequestContext);

		assertThat(guid).isEqualTo(PAYMENT_INSTRUMENT_GUID);
		verify(paymentAPIWorkflow).createPI(eq(PAYMENT_PROVIDER_CONFIG_GUID), eq(instrumentFormData), contextDTOArgumentCaptor.capture());
		final PICRequestContextDTO capturedContext = contextDTOArgumentCaptor.getValue();
		assertThat(capturedContext.getAddressDTO()).isEqualToComparingFieldByField(createTestAddressDTO());
		assertThat(capturedContext.getCurrency()).isEqualTo(CURRENCY);
		assertThat(capturedContext.getLocale()).isEqualTo(LOCALE);
		assertThat(capturedContext.getCustomerContextDTO().getUserId()).isEqualTo(CUSTOMER_ID);
		assertThat(capturedContext.getCustomerContextDTO().getEmail()).isEqualTo(CUSTOMER_EMAIL);
		assertThat(capturedContext.getCustomerContextDTO().getFirstName()).isEqualTo(CUSTOMER_FIRST_NAME);
		assertThat(capturedContext.getCustomerContextDTO().getLastName()).isEqualTo(CUSTOMER_LAST_NAME);
	}

	@Test
	public void testOrderCreatedWhenPaymentEventStatusIsApproved() {
		PaymentAPIResponse paymentAPIResponse =
				new PaymentAPIResponse(singletonList(createPaymentEvent(PaymentStatus.APPROVED, RESERVE)), true);
		when(paymentAPIWorkflow.reserve(any())).thenReturn(paymentAPIResponse);

		orderPaymentApiService.orderCreated(order);

		verify(orderPaymentService).saveOrUpdate(orderPayment);
		assertThat(orderPayment.getTransactionType()).isEqualTo(RESERVE);
		assertThat(orderPayment.getOrderPaymentStatus()).isEqualTo(APPROVED);
		assertOrderPaymentsFields();
	}

	@Test
	public void testOrderCreatedWhenPaymentEventStatusIsSkipped() {
		PaymentAPIResponse paymentAPIResponse =
				new PaymentAPIResponse(singletonList(createPaymentEvent(PaymentStatus.SKIPPED, RESERVE)), true);
		when(paymentAPIWorkflow.reserve(any())).thenReturn(paymentAPIResponse);

		orderPaymentApiService.orderCreated(order);

		verify(orderPaymentService).saveOrUpdate(orderPayment);
		assertThat(orderPayment.getTransactionType()).isEqualTo(RESERVE);
		assertThat(orderPayment.getOrderPaymentStatus()).isEqualTo(SKIPPED);
		assertOrderPaymentsFields();
	}

	@Test
	public void testOrderCreatedWhenPaymentEventStatusIsFailedAndThrowPaymentsException() {
		PaymentAPIResponse paymentAPIResponse =
				new PaymentAPIResponse(singletonList(createPaymentEvent(PaymentStatus.FAILED, RESERVE)),
						EXTERNAL_MESSAGE_FOR_RESPONSE, INTERNAL_MESSAGE_FOR_RESPONSE);
		when(paymentAPIWorkflow.reserve(any())).thenReturn(paymentAPIResponse);

		assertThatThrownBy(() -> orderPaymentApiService.orderCreated(order)).isInstanceOf(PaymentsException.class)
				.isEqualToComparingFieldByField(createPaymentsException(paymentAPIResponse));
		verify(orderPaymentService).saveOrUpdate(orderPayment);
		assertThat(orderPayment.getTransactionType()).isEqualTo(RESERVE);
		assertThat(orderPayment.getOrderPaymentStatus()).isEqualTo(FAILED);
		assertOrderPaymentsFields();
	}

	@Test
	public void testOrderCreatedExceptionThrownForLatestPaymentEvent() {
		PaymentEvent paymentEvent1 = createPaymentEvent(PaymentStatus.FAILED, RESERVE);
		PaymentEvent paymentEvent2 = createPaymentEvent(PaymentStatus.FAILED, RESERVE);
		paymentEvent1.setDate(new Date(1L));
		paymentEvent2.setDate(new Date(2L));

		PaymentAPIResponse paymentAPIResponse =
				new PaymentAPIResponse(Arrays.asList(paymentEvent1, paymentEvent2), EXTERNAL_MESSAGE_FOR_RESPONSE, INTERNAL_MESSAGE_FOR_RESPONSE);
		when(paymentAPIWorkflow.reserve(any())).thenReturn(paymentAPIResponse);

		assertThatThrownBy(() -> orderPaymentApiService.orderCreated(order))
				.isInstanceOf(PaymentsException.class)
				.isEqualToComparingFieldByField(createPaymentsException(paymentAPIResponse));
		verify(orderPaymentService, times(2)).saveOrUpdate(orderPayment);
	}

	@Test
	public void testOrderCreatedWithMultiOPIsWhenLimitsAreCorrect() {
		PaymentAPIResponse paymentAPIResponse = setupMultiPaymentOPIs(AMOUNT_18, AMOUNT_1.add(AMOUNT_1), UNLIMITED_AMOUNT);

		when(paymentAPIWorkflow.reserve(any())).thenReturn(paymentAPIResponse);

		orderPaymentApiService.orderCreated(order);

		assertOrderPaymentsFields();
	}

	@Test
	public void testOrderCreatedWithMultiOPIsWhenLimitsAreTooMuchShouldThrowPaymentsException() {
		setupMultiPaymentOPIs(AMOUNT_18, AMOUNT_18, UNLIMITED_AMOUNT);

		Throwable throwable = catchThrowable(() -> orderPaymentApiService.orderCreated(order));

		assertThat(throwable).isInstanceOf(PaymentsException.class);
		PaymentsException exception = (PaymentsException) throwable;
		StructuredErrorMessage errorMessage = exception.getStructuredErrorMessages().iterator().next();
		assertThat(errorMessage.getData().get("reason")).isEqualTo("Totalled limit amounts: 36, exceed order total: 20");
	}

	@Test
	public void testOrderCreatedWithMultiOPIsWithTooManyUnlimitedOPIsShouldThrowPaymentsException() {
		setupMultiPaymentOPIs(AMOUNT_18, UNLIMITED_AMOUNT, UNLIMITED_AMOUNT);

		Throwable throwable = catchThrowable(() -> orderPaymentApiService.orderCreated(order));

		assertThat(throwable).isInstanceOf(PaymentsException.class);
		PaymentsException exception = (PaymentsException) throwable;
		StructuredErrorMessage errorMessage = exception.getStructuredErrorMessages().iterator().next();
		assertThat(errorMessage.getData().get("reason")).isEqualTo("Only 1 unlimited Order Payment Instrument allowed");
	}

	@Test
	public void testOrderCreatedWithMultiOPIsWithNotEnoughUnlimitedOPIsShouldThrowPaymentsException() {
		setupMultiPaymentOPIs(AMOUNT_18);

		Throwable throwable = catchThrowable(() -> orderPaymentApiService.orderCreated(order));

		assertThat(throwable).isInstanceOf(PaymentsException.class);
		PaymentsException exception = (PaymentsException) throwable;
		StructuredErrorMessage errorMessage = exception.getStructuredErrorMessages().iterator().next();
		assertThat(errorMessage.getData().get("reason")).isEqualTo("At least 1 unlimited Order Payment Instrument required");
	}

	private PaymentAPIResponse setupMultiPaymentOPIs(final BigDecimal... amounts) {
		List<OrderPaymentInstrument> orderPaymentInstruments = new ArrayList<>();
		List<PaymentEvent> paymentEvents = new ArrayList<>();

		for (BigDecimal amount : amounts) {
            final String paymentInstrumentGuid = UUID.randomUUID().toString();
            final OrderPaymentInstrument orderPaymentInstrument = createOrderPaymentInstrument(paymentInstrumentGuid, amount);

            final PaymentInstrumentDTO testPaymentInstrumentDTO = createTestPaymentInstrumentDTO(paymentInstrumentGuid);
            when(paymentInstrumentWorkflow.findByGuid(paymentInstrumentGuid)).thenReturn(testPaymentInstrumentDTO);

            orderPaymentInstruments.add(orderPaymentInstrument);
            PaymentEvent paymentEvent = createPaymentEvent(PaymentStatus.APPROVED, RESERVE);
            paymentEvent.setDate(new Date(paymentEvents.size()));
            paymentEvents.add(paymentEvent);
        }

		when(orderPaymentInstrumentService.findByOrder(order)).thenReturn(orderPaymentInstruments);

		return new PaymentAPIResponse(paymentEvents, true);
	}

	@Test
	public void testRefundCompletedWhenPaymentEventStatusIsApproved() {
		PaymentEvent paymentEvent = createPaymentEvent(PaymentStatus.APPROVED, CREDIT);
		PaymentAPIResponse paymentAPIResponse = new PaymentAPIResponse(singletonList(paymentEvent), true);
		when(paymentAPIWorkflow.credit(any())).thenReturn(paymentAPIResponse);

		orderPaymentApiService.refund(order, singletonList(createTestPaymentInstrumentDTO()), createMoney(AMOUNT));

		verify(orderEventHelper).logOrderPaymentRefund(order, paymentEvent);
		verify(orderPaymentService).saveOrUpdate(orderPayment);
		assertThat(orderPayment.getTransactionType()).isEqualTo(CREDIT);
		assertThat(orderPayment.getOrderPaymentStatus()).isEqualTo(APPROVED);
		assertThat(orderPayment.getParentOrderPaymentGuid()).isEqualTo(RESERVATION_GUID);
		assertOrderPaymentsFields();
	}

	@Test
	public void testRefundThrowsExceptionWhenPaymentEventStatusIsFailed() {
		final PaymentEvent paymentEvent = createPaymentEvent(PaymentStatus.FAILED, CREDIT);
		final PaymentAPIResponse paymentAPIResponse = new PaymentAPIResponse(singletonList(paymentEvent),
				EXTERNAL_MESSAGE_FOR_RESPONSE, INTERNAL_MESSAGE_FOR_RESPONSE);
		when(paymentAPIWorkflow.credit(any())).thenReturn(paymentAPIResponse);

		assertThatThrownBy(() -> orderPaymentApiService.refund(order, singletonList(createTestPaymentInstrumentDTO()), createMoney(AMOUNT)))
				.isInstanceOf(PaymentsException.class)
				.hasStackTraceContaining(paymentAPIResponse.getExternalMessage())
				.extracting(exception -> ((PaymentsException) exception).getMessageId())
				.isEqualTo(PAYMENT_FAILED);

		verify(orderPaymentService).saveOrUpdate(orderPayment);
		assertThat(orderPayment.getTransactionType()).isEqualTo(CREDIT);
		assertThat(orderPayment.getOrderPaymentStatus()).isEqualTo(FAILED);
		assertThat(orderPayment.getParentOrderPaymentGuid()).isEqualTo(RESERVATION_GUID);
		assertOrderPaymentsFields();
		verify(orderEventHelper, never()).logOrderPaymentRefund(order, paymentEvent);
	}

	@Test
	public void testRefundThrowPaymentsExceptionWhenMoneyToRefundIsNegative() {
		assertThatThrownBy(() -> orderPaymentApiService.refund(order,
				singletonList(createTestPaymentInstrumentDTO()),
				createMoney(NEGATIVE_AMOUNT)))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testManualRefundCompletedWhenPaymentEventStatusIsApproved() {
		final PaymentEvent paymentEvent = createPaymentEvent(PaymentStatus.APPROVED, MANUAL_CREDIT);
		final PaymentAPIResponse paymentAPIResponse = new PaymentAPIResponse(singletonList(paymentEvent), true);
		when(paymentAPIWorkflow.manualCredit(any())).thenReturn(paymentAPIResponse);

		orderPaymentApiService.manualRefund(order, createMoney(AMOUNT));

		verify(orderEventHelper).logOrderPaymentManualRefund(order, paymentEvent);
		verify(orderPaymentService).saveOrUpdate(orderPayment);
		assertThat(orderPayment.getTransactionType()).isEqualTo(MANUAL_CREDIT);
		assertThat(orderPayment.getOrderPaymentStatus()).isEqualTo(APPROVED);
		assertThat(orderPayment.getParentOrderPaymentGuid()).isEqualTo(RESERVATION_GUID);
		assertOrderPaymentsFields();
	}

	@Test
	public void testManualRefundThrowsExceptionWhenAmountIsNegative() {
		assertThatThrownBy(() -> orderPaymentApiService.manualRefund(order, createMoney(NEGATIVE_AMOUNT)))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testShipmentCompletedWhenPaymentEventStatusIsApproved() {

		PaymentAPIResponse paymentAPIResponse =
				new PaymentAPIResponse(singletonList(createPaymentEvent(PaymentStatus.APPROVED, CHARGE)), true);
		when(paymentAPIWorkflow.chargePayment(any())).thenReturn(paymentAPIResponse);

		orderPaymentApiService.shipmentCompleted(orderShipment);

		verify(orderEventHelper).logOrderPaymentCaptured(order, paymentAPIResponse.getEvents().get(0));
		verify(orderPaymentService).saveOrUpdate(orderPayment);
		assertThat(orderPayment.getTransactionType()).isEqualTo(CHARGE);
		assertThat(orderPayment.getOrderPaymentStatus()).isEqualTo(APPROVED);
		assertThat(orderPayment.getParentOrderPaymentGuid()).isEqualTo(RESERVATION_GUID);
		assertOrderPaymentsFields();
	}

	@Test
	public void testShipmentCompletedWhenShipmentIsLastShippable() {
		final OrderShipment orderShipment1 = mockOrderShipment();
		when(orderShipment1.getShipmentStatus()).thenReturn(SHIPPED);
		final OrderShipment orderShipment = mockOrderShipment();
		when(orderShipment.getShipmentStatus()).thenReturn(RELEASED);
		when(order.getAllShipments()).thenReturn(Arrays.asList(orderShipment, orderShipment1));

		PaymentAPIResponse paymentAPIResponse =
				new PaymentAPIResponse(singletonList(createPaymentEvent(PaymentStatus.APPROVED, CHARGE)), true);
		when(paymentAPIWorkflow.chargePayment(any())).thenReturn(paymentAPIResponse);

		orderPaymentApiService.shipmentCompleted(orderShipment);

		final ArgumentCaptor<ChargeRequest> chargeRequestArgumentCaptor = ArgumentCaptor.forClass(ChargeRequest.class);
		verify(paymentAPIWorkflow).chargePayment(chargeRequestArgumentCaptor.capture());
		assertThat(chargeRequestArgumentCaptor.getValue().isFinalPayment()).isTrue();
	}

	@Test
	public void testShipmentCompletedWhenShipmentIsNotLastShippable() {
		final OrderShipment orderShipment = mockOrderShipment();
		when(orderShipment.getShipmentStatus()).thenReturn(RELEASED);
		final OrderShipment orderShipment1 = mockOrderShipment();
		when(orderShipment1.getShipmentStatus()).thenReturn(RELEASED);
		when(order.getAllShipments()).thenReturn(Arrays.asList(orderShipment, orderShipment1));

		PaymentAPIResponse paymentAPIResponse =
				new PaymentAPIResponse(singletonList(createPaymentEvent(PaymentStatus.APPROVED, CHARGE)), true);
		when(paymentAPIWorkflow.chargePayment(any())).thenReturn(paymentAPIResponse);

		orderPaymentApiService.shipmentCompleted(orderShipment);

		final ArgumentCaptor<ChargeRequest> chargeRequestArgumentCaptor = ArgumentCaptor.forClass(ChargeRequest.class);
		verify(paymentAPIWorkflow).chargePayment(chargeRequestArgumentCaptor.capture());
		assertThat(chargeRequestArgumentCaptor.getValue().isFinalPayment()).isFalse();
	}

	@Test
	public void testShipmentCompletedSendsDownTheLedger() {

		PaymentAPIResponse paymentAPIResponse =
				new PaymentAPIResponse(singletonList(createPaymentEvent(PaymentStatus.APPROVED, CHARGE)), true);
		when(paymentAPIWorkflow.chargePayment(any())).thenReturn(paymentAPIResponse);

		orderPaymentApiService.shipmentCompleted(orderShipment);

		final ArgumentCaptor<ChargeRequest> chargeRequestArgumentCaptor = ArgumentCaptor.forClass(ChargeRequest.class);

		verify(orderEventHelper).logOrderPaymentCaptured(order, paymentAPIResponse.getEvents().get(0));
		verify(paymentAPIWorkflow).chargePayment(chargeRequestArgumentCaptor.capture());
		final List<PaymentEvent> ledger = chargeRequestArgumentCaptor.getValue().getLedger();
		assertThat(ledger).extracting(PaymentEvent::getGuid).containsOnly(RESERVATION_GUID);
		assertThat(ledger).extracting(PaymentEvent::getParentGuid).containsOnly(RESERVATION_GUID);
	}

	@Test
	public void testShipmentCompletedSendsDownCorrectAmounts() {
		PaymentAPIResponse paymentAPIResponse =
				new PaymentAPIResponse(singletonList(createPaymentEvent(PaymentStatus.APPROVED, CHARGE)), true);
		when(paymentAPIWorkflow.chargePayment(any())).thenReturn(paymentAPIResponse);

		orderPaymentApiService.shipmentCompleted(orderShipment);

		final ArgumentCaptor<ChargeRequest> chargeRequestArgumentCaptor = ArgumentCaptor.forClass(ChargeRequest.class);

		verify(orderEventHelper).logOrderPaymentCaptured(order, paymentAPIResponse.getEvents().get(0));
		verify(paymentAPIWorkflow).chargePayment(chargeRequestArgumentCaptor.capture());
		assertThat(chargeRequestArgumentCaptor.getValue().getOrderContext().getOrderTotal().getAmount()).isEqualTo(order.getTotal());
		assertThat(chargeRequestArgumentCaptor.getValue().getOrderContext().getOrderNumber()).isEqualTo(order.getOrderNumber());
		assertThat(chargeRequestArgumentCaptor.getValue().getTotalChargeableAmount().getAmount()).isEqualTo(AMOUNT);
	}

	@Test
	public void testShipmentCompletedWhenPaymentEventStatusIsFailedAndThrowPaymentsException() {

		PaymentAPIResponse paymentAPIResponse =
				new PaymentAPIResponse(singletonList(createPaymentEvent(PaymentStatus.FAILED, CHARGE)),
						EXTERNAL_MESSAGE_FOR_RESPONSE, INTERNAL_MESSAGE_FOR_RESPONSE);
		when(paymentAPIWorkflow.chargePayment(any())).thenReturn(paymentAPIResponse);

		assertThatThrownBy(() -> orderPaymentApiService.shipmentCompleted(orderShipment))
				.isInstanceOf(PaymentsException.class)
				.hasStackTraceContaining(paymentAPIResponse.getExternalMessage());
		verify(orderPaymentService).saveOrUpdate(orderPayment);
		assertThat(orderPayment.getTransactionType()).isEqualTo(CHARGE);
		assertThat(orderPayment.getOrderPaymentStatus()).isEqualTo(FAILED);
		assertThat(orderPayment.getParentOrderPaymentGuid()).isEqualTo(RESERVATION_GUID);
		assertOrderPaymentsFields();
	}

	@Test
	public void testShipmentCompletedWhenPaymentTransactionIsNotSuccessfulAndThrowPaymentsException() {

		PaymentAPIResponse paymentAPIResponse =
				new PaymentAPIResponse(singletonList(createPaymentEvent(PaymentStatus.APPROVED, CHARGE)),
						EXTERNAL_MESSAGE_FOR_RESPONSE, INTERNAL_MESSAGE_FOR_RESPONSE);
		when(paymentAPIWorkflow.chargePayment(any())).thenReturn(paymentAPIResponse);

		assertThatThrownBy(() -> orderPaymentApiService.shipmentCompleted(orderShipment)).isInstanceOf(PaymentsException.class);
	}

	@Test
	public void testShipmentCanceledWhenOrderShipmentIsNotCompleted() {
		when(orderShipment.getShipmentStatus()).thenReturn(SHIPPED);

		assertThatThrownBy(() -> orderPaymentApiService.shipmentCanceled(orderShipment)).isInstanceOf(IllegalArgumentException.class);

		verify(orderPaymentService, never()).saveOrUpdate(orderPayment);
	}

	@Test
	public void testShipmentCanceledWhenSubtractingOfTotalsIsNegativeInModifyReservationRequest() {
		when(paymentAPIWorkflow.modifyReservation(any())).thenReturn(new PaymentAPIResponse(emptyList(), true));
		when(orderShipment.getShipmentStatus()).thenReturn(INVENTORY_ASSIGNED);
		when(orderShipment.getTotal()).thenReturn(AMOUNT);
		when(order.getTotal()).thenReturn(ZERO);

		orderPaymentApiService.shipmentCanceled(orderShipment);

		verify(paymentAPIWorkflow).modifyReservation(modifyReservationRequestArgumentCaptor.capture());
		assertThat(modifyReservationRequestArgumentCaptor.getValue().getAmount().getAmount()).isEqualByComparingTo(ZERO);
	}

	@Test
	public void testShipmentCanceledWhenSubtractingOfTotalsIsPositiveInModifyReservationRequest() {
		when(paymentAPIWorkflow.modifyReservation(any())).thenReturn(new PaymentAPIResponse(emptyList(), true));
		when(orderShipment.getShipmentStatus()).thenReturn(INVENTORY_ASSIGNED);
		when(orderShipment.getTotal()).thenReturn(FRACTIONAL_VALUE);
		when(order.getTotal()).thenReturn(AMOUNT);

		orderPaymentApiService.shipmentCanceled(orderShipment);

		verify(paymentAPIWorkflow).modifyReservation(modifyReservationRequestArgumentCaptor.capture());
		assertThat(modifyReservationRequestArgumentCaptor.getValue().getAmount().getAmount()).isEqualTo(AMOUNT.subtract(FRACTIONAL_VALUE));
	}

	@Test
	public void testShipmentCanceledWhenPaymentEventStatusIsApproved() {
		final PaymentEvent paymentEvent = createPaymentEvent(PaymentStatus.APPROVED, MODIFY_RESERVE);
		when(paymentAPIWorkflow.modifyReservation(any())).thenReturn(new PaymentAPIResponse(singletonList(paymentEvent), true));
		when(orderShipment.getShipmentStatus()).thenReturn(INVENTORY_ASSIGNED);

		orderPaymentApiService.shipmentCanceled(orderShipment);

		verify(orderPaymentService).saveOrUpdate(orderPayment);
		assertThat(orderPayment.getTransactionType()).isEqualTo(MODIFY_RESERVE);
		assertThat(orderPayment.getOrderPaymentStatus()).isEqualTo(APPROVED);
		assertThat(orderPayment.getParentOrderPaymentGuid()).isEqualTo(RESERVATION_GUID);
		assertOrderPaymentsFields();
	}

	@Test
	public void testShipmentCanceledWhenPaymentEventStatusIsFailedAndThrowPaymentsException() {
		final PaymentEvent paymentEvent = createPaymentEvent(PaymentStatus.FAILED, MODIFY_RESERVE);
		final PaymentAPIResponse paymentAPIResponse = new PaymentAPIResponse(singletonList(paymentEvent),
				EXTERNAL_MESSAGE_FOR_RESPONSE, INTERNAL_MESSAGE_FOR_RESPONSE);
		when(paymentAPIWorkflow.modifyReservation(any())).thenReturn(paymentAPIResponse);
		when(orderShipment.getShipmentStatus()).thenReturn(INVENTORY_ASSIGNED);

		assertThatThrownBy(() -> orderPaymentApiService.shipmentCanceled(orderShipment)).isInstanceOf(PaymentsException.class)
				.hasStackTraceContaining(paymentAPIResponse.getInternalMessage());

		verify(orderPaymentService).saveOrUpdate(orderPayment);
		assertThat(orderPayment.getTransactionType()).isEqualTo(MODIFY_RESERVE);
		assertThat(orderPayment.getOrderPaymentStatus()).isEqualTo(FAILED);
		assertThat(orderPayment.getParentOrderPaymentGuid()).isEqualTo(RESERVATION_GUID);
		assertOrderPaymentsFields();
	}

	@Test
	public void testGetOrderPaymentAmounts() {
		PaymentEventHistoryResponse paymentEventHistoryResponse = PaymentEventHistoryResponseBuilder.builder()
				.withAmountRefunded(createMoneyDTO(BigDecimal.ZERO))
				.withAmountCharged(createMoneyDTO(AMOUNT_1))
				.build(beanFactory);
		when(order.getAdjustedOrderTotalMoney()).thenReturn(Money.valueOf(AMOUNT, CURRENCY));
		when(paymentHistoryWorkflow.getPaymentEventHistoryAmounts(any())).thenReturn(paymentEventHistoryResponse);

		OrderPaymentAmounts orderPaymentAmounts = orderPaymentApiService.getOrderPaymentAmounts(order);

		assertThat(orderPaymentAmounts.getAmountDue().getAmount().compareTo(AMOUNT.subtract(AMOUNT_1))).isEqualTo(0);
		assertThat(orderPaymentAmounts.getAmountPaid().getAmount().compareTo(AMOUNT_1)).isEqualTo(0);
	}

	@Test
	public void testGetOrderPaymentAmountsWithAdjustedRefundable() {
		PaymentEventHistoryResponse paymentEventHistoryResponse = PaymentEventHistoryResponseBuilder.builder()
				.withAmountRefunded(createMoneyDTO(AMOUNT_1))
				.withAmountCharged(createMoneyDTO(AMOUNT_1))
				.build(beanFactory);
		when(order.getAdjustedOrderTotalMoney()).thenReturn(Money.valueOf(AMOUNT, CURRENCY));
		when(paymentHistoryWorkflow.getPaymentEventHistoryAmounts(any())).thenReturn(paymentEventHistoryResponse);

		OrderPaymentAmounts orderPaymentAmounts = orderPaymentApiService.getOrderPaymentAmounts(order);

		assertThat(orderPaymentAmounts.getAmountRefunded().getAmount().compareTo(AMOUNT_1)).isEqualTo(0);
		assertThat(orderPaymentAmounts.getAmountRefundable().getAmount().compareTo(BigDecimal.ZERO)).isEqualTo(0);
	}

	@Test
	public void testGetOrderPaymentAmountsWithAdjustedTotal() {
		BigDecimal adjustedAmountDue = AMOUNT.subtract(AMOUNT_1).setScale(2, BigDecimal.ROUND_UNNECESSARY);
		BigDecimal paidAmount = AMOUNT_1.setScale(2, BigDecimal.ROUND_UNNECESSARY);
		PaymentEventHistoryResponse paymentEventHistoryResponse = PaymentEventHistoryResponseBuilder.builder()
				.withAmountRefunded(createMoneyDTO(BigDecimal.ZERO))
				.withAmountCharged(createMoneyDTO(paidAmount))
				.build(beanFactory);
		when(order.getAdjustedOrderTotalMoney()).thenReturn(Money.valueOf(adjustedAmountDue, CURRENCY));

		when(paymentHistoryWorkflow.getPaymentEventHistoryAmounts(any())).thenReturn(paymentEventHistoryResponse);

		OrderPaymentAmounts orderPaymentAmounts = orderPaymentApiService.getOrderPaymentAmounts(order);

		assertThat(orderPaymentAmounts.getAmountDue().getAmount())
				.isEqualTo(adjustedAmountDue.subtract(paidAmount));
		assertThat(orderPaymentAmounts.getAmountPaid().getAmount().compareTo(paidAmount)).isEqualTo(0);
	}

	@Test
	public void testShipmentCanceledWithSingleReservePerPIPaymentInstrumentWithTrueLastShipmentWithShippedShipments() {
		OrderShipment firstShipment = mockOrderShipment();
		when(firstShipment.getShipmentStatus()).thenReturn(SHIPPED);
        when(firstShipment.getTotal()).thenReturn(AMOUNT);

        OrderShipment lastShipment = mockOrderShipment();
        when(lastShipment.getShipmentStatus()).thenReturn(INVENTORY_ASSIGNED);
        lenient().when(lastShipment.getTotal()).thenReturn(AMOUNT);
        when(order.getAllShipments()).thenReturn(Arrays.asList(firstShipment, lastShipment));

        final PaymentEvent paymentEvent = createPaymentEvent(PaymentStatus.APPROVED, CHARGE);
        when(paymentAPIWorkflow.chargePayment(any())).thenReturn(new PaymentAPIResponse(singletonList(paymentEvent), true));

        final OrderPaymentInstrument orderPaymentInstrument = createOrderPaymentInstrument();

        final PaymentInstrumentDTO testPaymentInstrumentDTO = createTestPaymentInstrumentDTO(PAYMENT_INSTRUMENT_GUID);
        testPaymentInstrumentDTO.setSingleReservePerPI(true);
        when(paymentInstrumentWorkflow.findByGuid(PAYMENT_INSTRUMENT_GUID)).thenReturn(testPaymentInstrumentDTO);
        when(orderPaymentInstrumentService.findByOrder(order)).thenReturn(singletonList(orderPaymentInstrument));

        orderPaymentApiService.shipmentCanceled(lastShipment);

        verify(paymentAPIWorkflow).chargePayment(chargeRequestArgumentCaptor.capture());
        assertThat(chargeRequestArgumentCaptor.getValue().getTotalChargeableAmount().getAmount()).isEqualTo(AMOUNT);
	}

	@Test
	public void testShipmentCanceledWithSingleReservePerPIPaymentInstrumentWithTrueLastShipmentWithoutShippedShipments() {
		final OrderShipment firstShipment = mockOrderShipment();
		when(firstShipment.getShipmentStatus()).thenReturn(CANCELLED);

		final OrderShipment lastShipment = mockOrderShipment();
		when(lastShipment.getShipmentStatus()).thenReturn(INVENTORY_ASSIGNED);
		when(lastShipment.getTotal()).thenReturn(AMOUNT);
		when(order.getAllShipments()).thenReturn(Arrays.asList(firstShipment, lastShipment));

		final PaymentEvent paymentEvent = createPaymentEvent(PaymentStatus.APPROVED, CANCEL_RESERVE);
		when(paymentAPIWorkflow.modifyReservation(any())).thenReturn(new PaymentAPIResponse(singletonList(paymentEvent), true));

		orderPaymentApiService.shipmentCanceled(lastShipment);

		verify(paymentAPIWorkflow).modifyReservation(modifyReservationRequestArgumentCaptor.capture());
		assertThat(modifyReservationRequestArgumentCaptor.getValue().getAmount().getAmount()).isEqualByComparingTo(ZERO);
	}

	@Test
	public void testShipmentCanceledWithoutSingleReservePerPIPaymentInstrumentWithTrueLastShipment() {
		final OrderShipment firstShipment = mockOrderShipment();
		when(firstShipment.getShipmentStatus()).thenReturn(RELEASED);

		final OrderShipment lastShipment = mockOrderShipment();
		when(lastShipment.getShipmentStatus()).thenReturn(INVENTORY_ASSIGNED);
		when(lastShipment.getTotal()).thenReturn(TEN);
		when(order.getAllShipments()).thenReturn(Arrays.asList(firstShipment, lastShipment));

		final PaymentEvent paymentEvent = createPaymentEvent(PaymentStatus.APPROVED, CANCEL_RESERVE);
		when(paymentAPIWorkflow.modifyReservation(any())).thenReturn(new PaymentAPIResponse(singletonList(paymentEvent), true));

		orderPaymentApiService.shipmentCanceled(lastShipment);

		verify(paymentAPIWorkflow).modifyReservation(modifyReservationRequestArgumentCaptor.capture());
		assertThat(modifyReservationRequestArgumentCaptor.getValue().getAmount().getAmount()).isEqualByComparingTo(TEN);
	}

	@Test
	public void testShipmentCanceledWithSingleReservePerPIPaymentInstrumentWithFalseLastShipment() {
		final OrderShipment firstShipment = mockOrderShipment();
		when(firstShipment.getShipmentStatus()).thenReturn(RELEASED);
		when(firstShipment.getTotal()).thenReturn(TEN);

		final OrderShipment lastShipment = mockOrderShipment();
		when(lastShipment.getShipmentStatus()).thenReturn(INVENTORY_ASSIGNED);
		when(order.getAllShipments()).thenReturn(Arrays.asList(firstShipment, lastShipment));

		final PaymentEvent paymentEvent = createPaymentEvent(PaymentStatus.APPROVED, CANCEL_RESERVE);
		when(paymentAPIWorkflow.modifyReservation(any())).thenReturn(new PaymentAPIResponse(singletonList(paymentEvent), true));

		orderPaymentApiService.shipmentCanceled(firstShipment);

		verify(paymentAPIWorkflow).modifyReservation(modifyReservationRequestArgumentCaptor.capture());
		assertThat(modifyReservationRequestArgumentCaptor.getValue().getAmount().getAmount()).isEqualByComparingTo(TEN);
	}

	@Test
	public void testShipmentCanceledWithSingleReservePerPIPaymentInstrumentWithTrueReleasedLastShipmentWithoutShippedShipments() {
		final OrderShipment firstShipment = mockOrderShipment();
		when(firstShipment.getShipmentStatus()).thenReturn(CANCELLED);

		final OrderShipment lastShipment = mockOrderShipment();
		when(lastShipment.getShipmentStatus()).thenReturn(RELEASED);
		when(lastShipment.getTotal()).thenReturn(AMOUNT);
		when(order.getAllShipments()).thenReturn(Arrays.asList(firstShipment, lastShipment));

		final PaymentEvent paymentEvent = createPaymentEvent(PaymentStatus.APPROVED, CANCEL_RESERVE);
		when(paymentAPIWorkflow.modifyReservation(any())).thenReturn(new PaymentAPIResponse(singletonList(paymentEvent), true));

		orderPaymentApiService.shipmentCanceled(lastShipment);

		verify(paymentAPIWorkflow).modifyReservation(modifyReservationRequestArgumentCaptor.capture());
		assertThat(modifyReservationRequestArgumentCaptor.getValue().getAmount().getAmount()).isEqualByComparingTo(ZERO);
	}

	private Order createOrder() {
        final Customer customer = createCustomer();
        final OrderShipment orderShipment = mockOrderShipment();
        when(orderShipment.getShipmentStatus()).thenReturn(SHIPPED);

        final Order order = mock(Order.class);
        when(order.getOrderNumber()).thenReturn(ORDER_NUMBER);
        when(order.getCustomer()).thenReturn(customer);
        when(order.getCurrency()).thenReturn(CURRENCY);
        when(order.getTotal()).thenReturn(AMOUNT);
        when(order.getAllShipments()).thenReturn(singletonList(orderShipment));
        when(order.getAdjustedOrderTotalMoney()).thenReturn(Money.valueOf(AMOUNT, CURRENCY));

        final OrderAddress orderAddress = mockOrderAddress();
        when(order.getBillingAddress()).thenReturn(orderAddress);
        return order;
    }

	private Customer createCustomer() {
		final Customer customer = mock(Customer.class);
		when(customer.getEmail()).thenReturn(CUSTOMER_EMAIL);
		return customer;
	}

	private void assertOrderPaymentsFields() {
        assertThat(orderPayment.getOrderNumber()).isEqualTo(order.getOrderNumber());
        assertThat(orderPayment.getAmount()).isEqualTo(AMOUNT);
        assertThat(orderPayment.getCurrency().getCurrencyCode()).isEqualTo(CURRENCY_CODE);
        assertThat(orderPayment.getPaymentInstrumentGuid()).isEqualTo(PAYMENT_INSTRUMENT_GUID);
    }

	private PaymentInstrumentDTO createTestPaymentInstrumentDTO() {
		return createTestPaymentInstrumentDTO(PAYMENT_INSTRUMENT_GUID);
	}

	private PaymentInstrumentDTO createTestPaymentInstrumentDTO(final String paymentInstrumentGuid) {
		return PaymentInstrumentDTOBuilder.builder()
				.withGuid(paymentInstrumentGuid)
                .withName("Test Name")
                .withData(ImmutableMap.of(PAYMENT_EVENT_DATA_KEY, "data"))
                .withPaymentProviderConfigurationGuid("test-payment-provider-config-guid")
                .withPaymentProviderConfiguration(ImmutableMap.of("configKey", "configValue"))
                .withBillingAddressGuid(BILLING_ADDRESS_GUID)
                .withSingleReservePerPI(false)
                .withSupportingMultiCharges(false)
                .build(beanFactory);
    }

    private OrderPaymentInstrument createOrderPaymentInstrument() {
        return createOrderPaymentInstrument(PAYMENT_INSTRUMENT_GUID, UNLIMITED_AMOUNT);
    }

    private OrderPaymentInstrument createOrderPaymentInstrument(final String guid, final BigDecimal limitAmount) {
        final OrderPaymentInstrument orderPaymentInstrument = mock(OrderPaymentInstrument.class);
        when(orderPaymentInstrument.getGuid()).thenReturn(UUID.randomUUID().toString());
        when(orderPaymentInstrument.getPaymentInstrumentGuid()).thenReturn(guid);
        when(orderPaymentInstrument.getLimitAmount()).thenReturn(limitAmount);
        when(orderPaymentInstrument.getCurrency()).thenReturn(Currency.getInstance(CURRENCY_CODE));
        lenient().when(orderPaymentInstrument.getOrderNumber()).thenReturn(ORDER_NUMBER);
        return orderPaymentInstrument;
    }

    private PaymentEvent createPaymentEvent(final PaymentStatus status, final TransactionType transactionType) {
		final PaymentEvent paymentEvent = new PaymentEvent();
		paymentEvent.setDate(new Date());
		paymentEvent.setAmount(createMoneyDTO());
		paymentEvent.setPaymentStatus(status);
		paymentEvent.setPaymentType(transactionType);
		paymentEvent.setParentGuid(RESERVATION_GUID);
		paymentEvent.setOrderPaymentInstrumentDTO(createOrderPaymentInstrumentDTO());
		paymentEvent.setPaymentEventData(Collections.singletonMap(PAYMENT_EVENT_DATA_KEY, PAYMENT_EVENT_DATA_VALUE));
		if (PaymentStatus.FAILED.equals(status)) {
			paymentEvent.setExternalMessage("Cannot process payment");
			paymentEvent.setInternalMessage("Payment provider crashed");
		}
		return paymentEvent;
	}

	private OrderPaymentInstrumentDTO createOrderPaymentInstrumentDTO() {
		final OrderPaymentInstrumentDTO orderInstrument = new OrderPaymentInstrumentDTO();
		orderInstrument.setBillingAddress(new AddressDTO());
		orderInstrument.setCustomerEmail(CUSTOMER_EMAIL);
		orderInstrument.setOrderNumber(ORDER_NUMBER);
		orderInstrument.setGUID(ORDER_GUID);
		orderInstrument.setLimit(createMoneyDTO(UNLIMITED_AMOUNT));
		orderInstrument.setOrderPaymentInstrumentData(Collections.emptyMap());
		orderInstrument.setPaymentInstrument(createTestPaymentInstrumentDTO());
		return orderInstrument;
	}

	private MoneyDTO createMoneyDTO(final BigDecimal amount) {
		return MoneyDTOBuilder.builder()
				.withAmount(amount)
				.withCurrencyCode(CURRENCY_CODE)
				.build(new MoneyDTO());
	}

	private MoneyDTO createMoneyDTO() {
		return createMoneyDTO(AMOUNT);
	}

	private Money createMoney(final BigDecimal amount) {
		return Money.valueOf(amount, Currency.getInstance(CURRENCY_CODE));
	}

	private OrderShipment mockOrderShipment() {
		final OrderShipment orderShipment = mock(OrderShipment.class);
		final String shipmentNumber = UUID.randomUUID().toString();
		when(orderShipment.getOrder()).thenReturn(order);
		when(orderShipment.getTotal()).thenReturn(BigDecimal.TEN);
		when(orderShipment.getTotalMoney()).thenReturn(Money.valueOf(AMOUNT, Currency.getInstance("USD")));
		when(orderShipment.getShipmentNumber()).thenReturn(shipmentNumber);
		return orderShipment;
	}

	private PICRequestContext mockPICRequestContext() {
		final PICRequestContext requestContext = mock(PICRequestContext.class);
		final CustomerContext customerContext = mockCustomerContext();
		final com.elasticpath.common.dto.AddressDTO addressDTO = createTestAddressDTO();

		when(requestContext.getCurrency()).thenReturn(CURRENCY);
		when(requestContext.getLocale()).thenReturn(LOCALE);
		when(requestContext.getCustomerContext()).thenReturn(customerContext);
		when(requestContext.getBillingAddress()).thenReturn(addressDTO);

		return requestContext;
	}

	private PICFieldsRequestContext mockPICFieldsRequestContext() {
		final PICFieldsRequestContext requestContext = mock(PICFieldsRequestContext.class);
		final CustomerContext customerContext = mockCustomerContext();

		when(requestContext.getCurrency()).thenReturn(CURRENCY);
		when(requestContext.getLocale()).thenReturn(LOCALE);
		when(requestContext.getCustomerContext()).thenReturn(customerContext);

		return requestContext;
	}

	private CustomerContext mockCustomerContext() {
		final CustomerContext customerContext = mock(CustomerContext.class);

		when(customerContext.getCustomerId()).thenReturn(CUSTOMER_ID);
		when(customerContext.getFirstName()).thenReturn(CUSTOMER_FIRST_NAME);
		when(customerContext.getLastName()).thenReturn(CUSTOMER_LAST_NAME);
		when(customerContext.getEmailAddress()).thenReturn(CUSTOMER_EMAIL);

		return customerContext;
	}

	private OrderAddress mockOrderAddress() {
		final com.elasticpath.common.dto.AddressDTO addressDTO = createTestAddressDTO();
		final OrderAddress orderAddress = mock(OrderAddress.class);
		when(orderAddress.getGuid()).thenReturn(addressDTO.getGuid());
		when(orderAddress.getCity()).thenReturn(addressDTO.getCity());
		when(orderAddress.getCountry()).thenReturn(addressDTO.getCountry());
		when(orderAddress.getFirstName()).thenReturn(addressDTO.getFirstName());
		when(orderAddress.getLastName()).thenReturn(addressDTO.getLastName());
		when(orderAddress.getPhoneNumber()).thenReturn(addressDTO.getPhoneNumber());
		when(orderAddress.getStreet1()).thenReturn(addressDTO.getStreet1());
		when(orderAddress.getStreet2()).thenReturn(addressDTO.getStreet2());
		when(orderAddress.getSubCountry()).thenReturn(addressDTO.getSubCountry());
		when(orderAddress.getZipOrPostalCode()).thenReturn(addressDTO.getZipOrPostalCode());
		return orderAddress;
	}

	private com.elasticpath.common.dto.AddressDTO createTestAddressDTO() {
		final com.elasticpath.common.dto.AddressDTO addressDTO = new com.elasticpath.common.dto.AddressDTO();

		addressDTO.setGuid("address-guid");
		addressDTO.setCity("City");
		addressDTO.setCountry("Country");
		addressDTO.setFirstName("FirstName");
		addressDTO.setLastName("LastName");
		addressDTO.setPhoneNumber("PhoneNumber");
		addressDTO.setStreet1("Street1");
		addressDTO.setStreet2("Street2");
		addressDTO.setSubCountry("SubCountry");
		addressDTO.setZipOrPostalCode("ZipOrPostalCode");

		return addressDTO;
	}

	private PaymentsException createPaymentsException(final PaymentAPIResponse paymentAPIResponse) {
		Map<String, String> exceptionData = new HashMap<>();
		exceptionData.put("ExternalMessage", paymentAPIResponse.getExternalMessage());
		exceptionData.put("InternalMessage", paymentAPIResponse.getInternalMessage());
		return new PaymentsException(PAYMENT_FAILED, exceptionData);
	}
}
