/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.orderprocessing.giftcertificate.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Sets;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.core.messaging.customer.CustomerEventType;
import com.elasticpath.core.messaging.giftcertificate.GiftCertificateEventType;
import com.elasticpath.core.messaging.order.OrderEventType;
import com.elasticpath.domain.catalog.GiftCertificate;
import com.elasticpath.domain.order.ElectronicOrderShipment;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.domain.order.OrderSku;
import com.elasticpath.domain.order.impl.OrderSkuImpl;
import com.elasticpath.domain.shipping.ShipmentType;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.EventType;
import com.elasticpath.messaging.camel.test.support.TransactionPolicyRegistryManager;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.messaging.impl.EventMessageImpl;
import com.elasticpath.messaging.predicate.impl.PropertyAndCompatibleEventTypePredicate;
import com.elasticpath.money.Money;
import com.elasticpath.service.order.OrderService;

/**
 * Test class for {@link com.elasticpath.orderprocessing.giftcertificate.messaging.GiftCertificateCreationMessagePublisherRouteBuilder}.
 */
@SuppressWarnings("PMD.TestClassWithoutTestCases")//PMD warning doesn't make sense
@RunWith(MockitoJUnitRunner.class)
public class GiftCertificateCreationMessagePublisherRouteBuilderTest extends CamelTestSupport {

	private static final long SECONDS_TO_WAIT = 2L;
	private static final String ROUTE_ID = "TEST_ROUTE_ID";
	private static final String ORDER_SHIPMENT_NUMBER = "12345-1";
	private static final String ORDER_GUID = "12345";
	private static final String STORE_CODE = "MOBEE";
	private static final ShipmentType SHIPMENT_TYPE = ShipmentType.ELECTRONIC;

	private Endpoint incomingEndpoint;
	
	@Mock(name = "event message data format")
	private DataFormat eventMessageDataFormat;
	@Mock
	private EventMessageFactory eventMessageFactory;
	private final PropertyAndCompatibleEventTypePredicate eventMessagePredicateFilter = new PropertyAndCompatibleEventTypePredicate();
	@Mock
	private EventMessagePublisher eventMessagePublisher;
	@Mock
	private OrderService orderService;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		incomingEndpoint = getMandatoryEndpoint("direct:incoming");

		eventMessagePredicateFilter.setCompatibleEventTypes(OrderEventType.ORDER_CREATED, GiftCertificateEventType.RESEND_GIFT_CERTIFICATE);
		eventMessagePredicateFilter.setPropertyName("hasGCs");
		eventMessagePredicateFilter.setPropertyValue("true");

		final GiftCertificateCreationMessagePublisherRouteBuilder routeBuilder = new GiftCertificateCreationMessagePublisherRouteBuilder();
		routeBuilder.setRouteId(ROUTE_ID);
		routeBuilder.setIncomingEndpoint(incomingEndpoint);
		routeBuilder.setOrderService(orderService);
		routeBuilder.setEventMessageDataFormat(eventMessageDataFormat);
		routeBuilder.setEventMessageFactory(eventMessageFactory);
		routeBuilder.setEventMessagePredicateFilter(eventMessagePredicateFilter);
		routeBuilder.setEventMessagePublisher(eventMessagePublisher);

		context().addRoutes(routeBuilder);
	}

	@Override
	protected JndiRegistry createRegistry() throws Exception {
		final JndiRegistry registry = super.createRegistry();

		new TransactionPolicyRegistryManager(registry).registerDefaultTransactionPolicy();

		return registry;
	}

	@Test
	public void verifyNoMessagesProducedWhenOrderContainsNoGiftCertificates() throws Exception {
		givenInboundJsonMessageIsUnmarshalledToEventMessage(OrderEventType.ORDER_CREATED, false, null);

		whenOrderCreatedMessageIsSent("order-created-msg-with-valid-event-and-missing-gc-shipment");
		
		verify(orderService, never()).findOrderByOrderNumber(ORDER_GUID);
		verify(eventMessagePublisher, never()).publish(any(EventMessage.class));
	}

	@Test
	public void verifyNoMessagesProducedWhenIncomingMessageIsOfIncompatibleType() throws Exception {
		givenInboundJsonMessageIsUnmarshalledToEventMessage(CustomerEventType.CUSTOMER_REGISTERED, true, null);

		whenOrderCreatedMessageIsSent("order-created-msg-with-invalid-event-type");
		
		verify(orderService, never()).findOrderByOrderNumber(ORDER_GUID);
		verify(eventMessagePublisher, never()).publish(any(EventMessage.class));
	}

	@Test
	public void verifyMessageIsCreatedForEachGiftCertificate() throws Exception {
		givenInboundJsonMessageIsUnmarshalledToEventMessage(OrderEventType.ORDER_CREATED, true, null);

		final ElectronicOrderShipment orderShipment = mock(ElectronicOrderShipment.class);

		final String skuGuid1 = "GiftCertSku-1";
		final String giftCertGuid1 = "GIFTCERT001";
		final String giftCertPrice1 = "10.00";

		Map<String, String> gcFields1 = new HashMap<>();
		gcFields1.put(GiftCertificate.KEY_GUID, giftCertGuid1);

		final String skuGuid2 = "GiftCertSku-2";
		final String giftCertGuid2 = "GIFTCERT002";
		final String giftCertPrice2 = "20.00";

		Map<String, String> gcFields2 = new HashMap<>();
		gcFields2.put(GiftCertificate.KEY_GUID, giftCertGuid2);

		final OrderSku giftCertificateSku1 = createOrderSku(skuGuid1, new BigDecimal(giftCertPrice1), gcFields1, orderShipment);
		final OrderSku giftCertificateSku2 = createOrderSku(skuGuid2,  new BigDecimal(giftCertPrice2), gcFields2, orderShipment);
		final OrderSku nonGiftCertificateSku = createOrderSku("nonGiftCertSku", new BigDecimal("30.12"), new HashMap<>(), orderShipment);

		givenOrderShipmentContainsSkus(orderShipment, giftCertificateSku1, giftCertificateSku2, nonGiftCertificateSku);

		final Order order = mock(Order.class);

		when(orderService.findOrderByOrderNumber(ORDER_GUID)).thenReturn(order);
		when(order.getLocale()).thenReturn(Locale.ENGLISH);
		when(order.getStoreCode()).thenReturn(STORE_CODE);
		when(order.getElectronicShipments()).thenReturn(Sets.newHashSet(orderShipment));
		when(orderShipment.getOrder()).thenReturn(order);
		when(orderShipment.getShipmentNumber()).thenReturn(ORDER_SHIPMENT_NUMBER);
		when(orderShipment.getOrderShipmentType()).thenReturn(SHIPMENT_TYPE);

		final Map<String, Object> data1 = createGiftCertificateCreatedEventMessageDataMap(skuGuid1, giftCertPrice1, gcFields1, null);
		final Map<String, Object> data2 = createGiftCertificateCreatedEventMessageDataMap(skuGuid2, giftCertPrice2, gcFields2, null);

		final EventMessage giftCertificateCreatedEventMessage1 = expectEventMessageFactoryCreatesEventMessage(
				GiftCertificateEventType.GIFT_CERTIFICATE_CREATED, giftCertGuid1, data1);
		final EventMessage giftCertificateCreatedEventMessage2 = expectEventMessageFactoryCreatesEventMessage(
				GiftCertificateEventType.GIFT_CERTIFICATE_CREATED, giftCertGuid2, data2);

		whenOrderCreatedMessageIsSent("order-created-event-with-two-GCs-and-one-nonGC-shipments");

		// Then
		verify(orderService).findOrderByOrderNumber(ORDER_GUID);
		verify(eventMessagePublisher).publish(giftCertificateCreatedEventMessage1);
		verify(eventMessagePublisher).publish(giftCertificateCreatedEventMessage2);
	}

	@Test
	public void verifyMessageIsCreatedWhenGiftCertificateIsReSent() throws Exception {
		String additionalRecipient = "additional.recipient@xyz.com";

		givenInboundJsonMessageIsUnmarshalledToEventMessage(GiftCertificateEventType.RESEND_GIFT_CERTIFICATE, true,
				additionalRecipient);

		final ElectronicOrderShipment orderShipment = mock(ElectronicOrderShipment.class);

		final String skuGuid = "gcSKU";
		final String giftCertGuid = "gcGuid";
		final String giftCertPrice = "50.00";

		Map<String, String> gcFields = new HashMap<>();
		gcFields.put(GiftCertificate.KEY_GUID, giftCertGuid);

		final OrderSku gcOrderSku = createOrderSku(skuGuid, new BigDecimal(giftCertPrice), gcFields, orderShipment);

		givenOrderShipmentContainsSkus(orderShipment, gcOrderSku);

		final Order order = mock(Order.class);

		when(orderService.findOrderByOrderNumber(ORDER_GUID)).thenReturn(order);
		when(order.getLocale()).thenReturn(Locale.ENGLISH);
		when(order.getStoreCode()).thenReturn(STORE_CODE);
		when(order.getElectronicShipments()).thenReturn(Sets.newHashSet(orderShipment));
		when(orderShipment.getOrder()).thenReturn(order);
		when(orderShipment.getShipmentNumber()).thenReturn(ORDER_SHIPMENT_NUMBER);
		when(orderShipment.getOrderShipmentType()).thenReturn(SHIPMENT_TYPE);

		final Map<String, Object> data = createGiftCertificateCreatedEventMessageDataMap(skuGuid, giftCertPrice, gcFields, additionalRecipient);

		final EventMessage giftCertificateCreatedEventMessage = expectEventMessageFactoryCreatesEventMessage(
				GiftCertificateEventType.GIFT_CERTIFICATE_CREATED, giftCertGuid, data);

		whenOrderCreatedMessageIsSent("gc-resent-event");

		// Then
		verify(orderService).findOrderByOrderNumber(ORDER_GUID);
		verify(eventMessagePublisher).publish(giftCertificateCreatedEventMessage);
	}

	private Map<String, Object> createGiftCertificateCreatedEventMessageDataMap(final String skuGuid, final String price,
																				final Map<String, String> gcFields,
																				final String additionalRecipient) {
		final Map<String, Object> data = new HashMap<>(9);

		data.put("orderLocale", Locale.ENGLISH);
		data.put("orderStoreCode", STORE_CODE);
		data.put("shipmentNumber", ORDER_SHIPMENT_NUMBER);
		data.put("shipmentType", SHIPMENT_TYPE.toString());
		data.put("orderSkuGuid", skuGuid);
		data.put("orderSkuTotalAmount", price);
		data.put("orderSkuTotalCurrency", Currency.getInstance("CAD"));
		data.put("gcFields", gcFields);
		if (additionalRecipient != null) {
			data.put("emailAddress", additionalRecipient);
		}

		return data;
	}

	private EventMessage givenInboundJsonMessageIsUnmarshalledToEventMessage(final EventType eventType, final boolean hasGCs,
																			 final String additionalRecipient) throws Exception {

		final Map<String, Object> data = new HashMap<>(1);
		data.put("hasGCs", hasGCs);
		if (additionalRecipient != null) {
			data.put("emailAddress", additionalRecipient);
		}

		final EventMessage eventMessage = new EventMessageImpl(eventType, ORDER_GUID, data);

		when(eventMessageDataFormat.unmarshal(any(Exchange.class), any(InputStream.class))).thenReturn(eventMessage);
			
		return eventMessage;
	}

	private OrderSku createOrderSku(final String orderSkuGuid, final BigDecimal unitPrice, final Map<String, String> gcFields,
									final OrderShipment orderShipment) {
		final OrderSkuImpl giftCertificateOrderSku = mock(OrderSkuImpl.class, RETURNS_DEEP_STUBS);
		
		when(giftCertificateOrderSku.getGuid()).thenReturn(orderSkuGuid);
		when(giftCertificateOrderSku.getTotal()).thenReturn(Money.valueOf(unitPrice, Currency.getInstance("CAD")));
		when(giftCertificateOrderSku.getModifierFields().getMap()).thenReturn(gcFields);
		when(giftCertificateOrderSku.isGiftCertificate()).thenReturn(true);
		when(giftCertificateOrderSku.getShipment()).thenReturn(orderShipment);

		return giftCertificateOrderSku;
	}

	private void givenOrderShipmentContainsSkus(final OrderShipment orderShipment, final OrderSku... orderSkus) {
		when(orderShipment.getShipmentOrderSkus()).thenReturn(new HashSet<>(Arrays.asList(orderSkus)));
	}

	private EventMessage expectEventMessageFactoryCreatesEventMessage(final EventType eventType, final String guid, final Map<String, Object> data) {
		final EventMessage eventMessage = new EventMessageImpl(eventType, guid, data);

		when(eventMessageFactory.createEventMessage(eventType, guid, data)).thenReturn(eventMessage);

		return eventMessage;
	}

	private void whenOrderCreatedMessageIsSent(final String orderCreatedJsonMessage) {
		final NotifyBuilder notifyBuilder = new NotifyBuilder(context())
				.from(incomingEndpoint.getEndpointUri())
				.whenDone(1)
				.create();

		template().sendBody(incomingEndpoint, orderCreatedJsonMessage);

		assertTrue("Did not receive message(s) on outgoing endpoint within " + SECONDS_TO_WAIT + " seconds",
				   notifyBuilder.matches(SECONDS_TO_WAIT, TimeUnit.SECONDS));
	}
}
