/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.domain.event.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.event.EventOriginator;
import com.elasticpath.domain.event.EventOriginatorHelper;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderEvent;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.domain.order.PhysicalOrderShipment;
import com.elasticpath.domain.order.impl.OrderEventImpl;
import com.elasticpath.domain.order.impl.OrderImpl;
import com.elasticpath.domain.order.impl.PhysicalOrderShipmentImpl;
import com.elasticpath.money.Money;
import com.elasticpath.money.MoneyFormatter;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTOBuilder;
import com.elasticpath.plugin.payment.provider.dto.PaymentStatus;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.provider.payment.workflow.PaymentInstrumentWorkflow;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.test.BeanFactoryExpectationsFactory;

/**
 * Test for {@link OrderEventHelperImpl}.
 */
@SuppressWarnings({"PMD.TooManyMethods"})
@RunWith(MockitoJUnitRunner.class)
public class OrderEventHelperImplTest {
	private static final String TEST_FORMATTED_AMOUNT = "formatted amount";

	private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
	private static final String PAYMENT_EVENT_GUID = "Payment_GUID";
	private static final String CURRENCY_CODE_CAD = "CAD";
	private static final String INSTRUMENT_NAME = "InstrumentName";
	private static final String ORDER_PAYMENT_INSTRUMENT_GUID = "OPI_GUID";
	private static final String PAYMENT_INSTRUMENT_GUID = "PI_GUID";

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();

	@Mock
	private PaymentInstrumentWorkflow paymentInstrumentWorkflow;

	private OrderEventHelperImpl orderEventHelper;
	private final BeanFactory beanFactory = context.mock(BeanFactory.class);
	private final MoneyFormatter moneyFormatter = context.mock(MoneyFormatter.class);
	private final TimeService timeService = context.mock(TimeService.class);
	private final PricingSnapshotService pricingSnapshotService = context.mock(PricingSnapshotService.class);
	private final Order order = context.mock(Order.class);
	private OrderEvent orderEvent;
	private final EventOriginator scapegoat = new EventOriginatorImpl();

	/**
	 * Sets up mock bean factory, sets up OrderEventHelper with mock services, and mocks an Order for testing.
	 *
	 * @throws Exception the exception
	 */
	@Before
	public void setUp() throws Exception {
		orderEvent = new OrderEventImpl();
		final PaymentInstrumentDTO paymentInstrumentDTO = createPaymentInstrumentDTO();
		when(paymentInstrumentWorkflow.findByGuid(PAYMENT_INSTRUMENT_GUID)).thenReturn(paymentInstrumentDTO);
		BeanFactoryExpectationsFactory bfef = new BeanFactoryExpectationsFactory(context, beanFactory);
		bfef.allowingBeanFactoryGetPrototypeBean(ContextIdNames.ORDER_EVENT, OrderEvent.class, orderEvent);
		bfef.allowingBeanFactoryGetSingletonBean(ContextIdNames.EVENT_ORIGINATOR_HELPER, EventOriginatorHelper.class,
				new EventOriginatorHelperImpl());
		bfef.allowingBeanFactoryGetPrototypeBean(ContextIdNames.EVENT_ORIGINATOR, EventOriginator.class, EventOriginatorImpl.class);

		context.checking(new Expectations() {
			{
				allowing(order).getModifiedBy();
				will(returnValue(scapegoat));
				allowing(order).getLocale();
				will(returnValue(Locale.CANADA));

				allowing(timeService).getCurrentTime();
				will(returnValue(new Date()));

				allowing(moneyFormatter).formatCurrency(with(any(Money.class)), with(any(Locale.class)));
				will(returnValue(TEST_FORMATTED_AMOUNT));
			}
		});

		orderEventHelper = new OrderEventHelperImpl();
		orderEventHelper.setBeanFactory(beanFactory);
		orderEventHelper.setMoneyFormatter(moneyFormatter);
		orderEventHelper.setPricingSnapshotService(pricingSnapshotService);
		orderEventHelper.setTimeService(timeService);

		orderEventHelper.setPaymentInstrumentWorkflow(paymentInstrumentWorkflow);
	}

	/**
	 * Test logging when order shipment method is changed.
	 */
	@Test
	public void testLogOrderShipmentMethodChangedWithCarrierCode() {
		// Given
		final String shippingOptionName = "Sample Shipping Option";
		final String shippingOptionCode = "ship_option_1";
		final String carrierCode = "carrier_1";
		PhysicalOrderShipment physicalShipment = new PhysicalOrderShipmentImpl();
		physicalShipment.setShippingOptionName(shippingOptionName);
		physicalShipment.setShippingOptionCode(shippingOptionCode);
		physicalShipment.setCarrierCode(carrierCode);

		// Expectations
		context.checking(new Expectations() {
			{
				oneOf(order).addOrderEvent(orderEvent);
			}
		});

		// When
		orderEventHelper.logOrderShipmentMethodChanged(order, physicalShipment);

		// Then
		assertTrue("Message should contain shippingOption name", orderEvent.getNote().contains(shippingOptionName));
		assertTrue("Message should contain shippingOption code", orderEvent.getNote().contains("Shipping Option Code: " + shippingOptionCode));
		assertTrue("Message should contain shippingOption carrier code", orderEvent.getNote().contains("Carrier Code: " + carrierCode));
	}

	/**
	 * Test logging when order shipment method is changed and no carrier code specified.
	 */
	@Test
	public void testLogOrderShipmentMethodChangedWithNoCarrierCode() {
		// Given
		final String shippingOptionCode = "ship_option_1";
		PhysicalOrderShipment physicalShipment = new PhysicalOrderShipmentImpl();
		physicalShipment.setShippingOptionCode(shippingOptionCode);

		// Expectations
		context.checking(new Expectations() {
			{
				oneOf(order).addOrderEvent(orderEvent);
			}
		});

		// When
		orderEventHelper.logOrderShipmentMethodChanged(order, physicalShipment);

		// Then
		assertFalse("Message should not contain carrier code in message as not specified", orderEvent.getNote().contains("carrier code:"));
	}

	/**
	 * Ensure logging of capture payment event has correct format.
	 */
	@Test
	public void ensureLoggingOfCapturePaymentEventHasCorrectFormat() {
		Order order = createBlankOrderWithLocale();
		orderEventHelper.logOrderPaymentCaptured(order, createChargePaymentEvent());
		OrderEvent captureOrderEvent = getSingleOrderEventFromOrder(order);
		String captureEventNote = captureOrderEvent.getNote();
		assertThatNoteIsCapture(captureEventNote);
		assertThatNoteIsOnCorrectPaymentInstrument(captureEventNote);
		assertThatNoteHasFormattedAmount(captureEventNote);
	}

	/**
	 * Ensure logging of refund payment event has correct format.
	 */
	@Test
	public void ensureLoggingOfRefundPaymentEventHasCorrectFormat() {
		Order order = createBlankOrderWithLocale();
		orderEventHelper.logOrderPaymentRefund(order, createCreditPaymentEvent());
		OrderEvent refundOrderEvent = getSingleOrderEventFromOrder(order);
		String refundEventNote = refundOrderEvent.getNote();
		assertNoteIsRefund(refundEventNote);
		assertThatNoteIsOnCorrectPaymentInstrument(refundEventNote);
		assertThatNoteHasFormattedAmount(refundEventNote);
	}

	/**
	 * Ensure logging of manual refund payment event has correct format.
	 */
	@Test
	public void ensureLoggingOfManualRefundPaymentEventHasCorrectFormat() {
		Order order = createBlankOrderWithLocale();
		orderEventHelper.logOrderPaymentManualRefund(order, createManualCreditPaymentEvent());
		OrderEvent refundOrderEvent = getSingleOrderEventFromOrder(order);
		String refundEventNote = refundOrderEvent.getNote();
		assertNoteIsManualRefund(refundEventNote);
		assertThatNoteHasFormattedAmount(refundEventNote);
	}

	@Test
	public void verifyLoggingOfOrderReleasedForFulfilmentHasCorrectFormat() {
		final Order order = createBlankOrderWithLocale();
		order.setModifiedBy(scapegoat);

		orderEventHelper.logOrderReleasedForFulfilment(order);

		final OrderEvent orderReleasedEvent = getSingleOrderEventFromOrder(order);

		assertEquals("Unexpected event title", "Order Released for Fulfilment", orderReleasedEvent.getTitle());
		assertEquals("Unexpected event note", "Order is released for fulfilment.", orderReleasedEvent.getNote());
	}

	@Test
	public void shouldLogOrderShipmentReleased() {
		final Order order = createBlankOrderWithLocale();
		final OrderShipment shipment = createOrderShipment();
		order.setModifiedBy(scapegoat);

		orderEventHelper.logOrderShipmentReleased(order, shipment);

		final OrderEvent orderReleasedEvent = getSingleOrderEventFromOrder(order);

		assertEquals("Unexpected event title", "Order Shipment Released", orderReleasedEvent.getTitle());
		assertEquals("Unexpected event note", "Order shipment #" + shipment.getShipmentNumber() + " is released.", orderReleasedEvent.getNote());
	}

	private OrderShipment createOrderShipment() {
		OrderShipment orderShipment = new PhysicalOrderShipmentImpl();
		orderShipment.setShipmentNumber("000001-1");

		return orderShipment;
	}
	private Order createBlankOrderWithLocale() {
		Order order = new OrderImpl();
		order.setLocale(Locale.CANADA);
		order.setCurrency(Currency.getInstance("CAD"));
		return order;
	}

	private PaymentEvent createManualCreditPaymentEvent() {
		final PaymentEvent paymentEvent = new PaymentEvent();
		paymentEvent.setParentGuid(OrderEventHelperImplTest.PAYMENT_EVENT_GUID);
		paymentEvent.setPaymentType(TransactionType.MANUAL_CREDIT);
		paymentEvent.setPaymentStatus(PaymentStatus.APPROVED);
		paymentEvent.setAmount(create100CAD());
		paymentEvent.setDate(new Date());
		return paymentEvent;
	}

	private PaymentEvent createCreditPaymentEvent() {
		final PaymentEvent paymentEvent = new PaymentEvent();
		paymentEvent.setParentGuid(OrderEventHelperImplTest.PAYMENT_EVENT_GUID);
		paymentEvent.setPaymentType(TransactionType.CREDIT);
		paymentEvent.setPaymentStatus(PaymentStatus.APPROVED);
		paymentEvent.setAmount(create100CAD());
		paymentEvent.setDate(new Date());
		paymentEvent.setOrderPaymentInstrumentDTO(createOrderPaymentInstrumentDTO());
		return paymentEvent;
	}

	private PaymentEvent createChargePaymentEvent() {
		final PaymentEvent paymentEvent = new PaymentEvent();
		paymentEvent.setParentGuid(OrderEventHelperImplTest.PAYMENT_EVENT_GUID);
		paymentEvent.setPaymentType(TransactionType.CHARGE);
		paymentEvent.setPaymentStatus(PaymentStatus.APPROVED);
		paymentEvent.setAmount(create100CAD());
		paymentEvent.setDate(new Date());
		paymentEvent.setOrderPaymentInstrumentDTO(createOrderPaymentInstrumentDTO());
		return paymentEvent;
	}

	private MoneyDTO create100CAD() {
		return MoneyDTOBuilder.builder()
				.withAmount(ONE_HUNDRED)
				.withCurrencyCode(CURRENCY_CODE_CAD)
				.build(new MoneyDTO());
	}

	private OrderPaymentInstrumentDTO createOrderPaymentInstrumentDTO() {
		final OrderPaymentInstrumentDTO orderInstrument = new OrderPaymentInstrumentDTO();
		orderInstrument.setPaymentInstrument(createPaymentInstrumentDTO());
		orderInstrument.setGUID(ORDER_PAYMENT_INSTRUMENT_GUID);
		return orderInstrument;
	}

	private PaymentInstrumentDTO createPaymentInstrumentDTO() {
		final PaymentInstrumentDTO paymentInstrument = new PaymentInstrumentDTO();
		paymentInstrument.setName(INSTRUMENT_NAME);
		paymentInstrument.setGUID(PAYMENT_INSTRUMENT_GUID);
		paymentInstrument.setSupportingMultiCharges(false);
		paymentInstrument.setSingleReservePerPI(false);
		return paymentInstrument;
	}

	private OrderEvent getSingleOrderEventFromOrder(final Order order) {
		Set<OrderEvent> orderEvents = order.getOrderEvents();
		assertEquals("Only one order event should be added.", 1, orderEvents.size());
		return orderEvents.iterator().next();
	}

	private void assertNoteIsRefund(final String refundEventNote) {
		assertThat("Note should be a refund.", refundEventNote, containsString("Refund"));
	}

	private void assertNoteIsManualRefund(final String refundEventNote) {
		assertThat("Note should be a manual refund.", refundEventNote, containsString("Manual refund"));
	}

	private void assertThatNoteIsCapture(final String captureEventNote) {
		assertThat("Note should be a capture.", captureEventNote, containsString("Capture"));
	}

	private void assertThatNoteHasFormattedAmount(final String eventNote) {
		assertThat("Payment amount should be included in note.", eventNote, containsString(TEST_FORMATTED_AMOUNT));
	}

	private void assertThatNoteIsOnCorrectPaymentInstrument(final String eventNote) {
		assertThat("Note must have a correct payment instrument name.", eventNote, containsString(INSTRUMENT_NAME));
	}
}
