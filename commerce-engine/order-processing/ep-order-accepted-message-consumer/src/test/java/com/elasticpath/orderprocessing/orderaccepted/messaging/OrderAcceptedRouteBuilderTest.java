/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.orderprocessing.orderaccepted.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.InputStream;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePredicate;
import com.elasticpath.service.shoppingcart.actions.PostCaptureCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.PostCaptureCheckoutService;

@RunWith(MockitoJUnitRunner.class)
public class OrderAcceptedRouteBuilderTest extends CamelTestSupport {

	private static final String ROUTE_ID = "testRouteId";

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();

	@Mock
	private EventMessagePredicate orderEventTypePredicate;

	@Mock
	private OrderToPostCaptureCheckoutActionContextTransformer transformer;

	@Mock
	private PostCaptureCheckoutService postCaptureCheckoutService;

	@InjectMocks
	@Spy
	private OrderAcceptedRouteBuilder targetRouteBuilder;

	private Endpoint incomingEndpoint;

	@Mock
	private EventMessage eventMessage;

	@Mock
	private DataFormat eventMessageDataFormat;

	@Mock
	private PostCaptureCheckoutActionContext postCaptureCheckoutActionContext;

	private static final String EVENT_MESSAGE_STRING = "{\n"
			+ "  \"eventType\": {\n"
			+ "    \"@class\": \"OrderEventType\",\n"
			+ "    \"name\": \"ORDER_ACCEPTED\"\n"
			+ "  },\n"
			+ "  \"guid\": \"testOrderNumber\",\n"
			+ "  \"data\": {}\n"
			+ "}";

	private static final String EVENT_MESSAGE_WITH_CMUSER_STRING = "{\n"
			+ "  \"eventType\": {\n"
			+ "    \"@class\": \"OrderEventType\",\n"
			+ "    \"name\": \"ORDER_ACCEPTED\"\n"
			+ "  },\n"
			+ "  \"guid\": \"testOrderNumber\",\n"
			+ "  \"data\": {\n"
			+ "     \"cm_username\": \"admin\",\n"
			+ "  }\n"
			+ "}";

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		incomingEndpoint = getMandatoryEndpoint("direct:incoming");

		targetRouteBuilder.setRouteId(ROUTE_ID);
		targetRouteBuilder.setIncomingEndpoint(incomingEndpoint);

		context().addRoutes(targetRouteBuilder);

		given(eventMessageDataFormat.unmarshal(any(Exchange.class), any(InputStream.class))).willReturn(eventMessage);
		given(orderEventTypePredicate.apply(eventMessage)).willReturn(true);
		given(transformer.transform(eventMessage)).willReturn(postCaptureCheckoutActionContext);

	}

	@Test
	public void testRoute() throws Exception {

		template().sendBody(incomingEndpoint, EVENT_MESSAGE_STRING);

		verify(eventMessageDataFormat).unmarshal(any(), any());
		verify(postCaptureCheckoutService).completeCheckout(postCaptureCheckoutActionContext);

	}

	@Test
	public void testRouteWithUnexpectedEventType() throws Exception {

		given(orderEventTypePredicate.apply(eventMessage)).willReturn(false);

		template().sendBody(incomingEndpoint, EVENT_MESSAGE_STRING);

		verify(eventMessageDataFormat).unmarshal(any(), any());
		verify(postCaptureCheckoutService, never()).completeCheckout(any());

	}

	@Test
	public void testRouteWithCmUser() throws Exception {

		template().sendBody(incomingEndpoint, EVENT_MESSAGE_WITH_CMUSER_STRING);

		verify(eventMessageDataFormat).unmarshal(any(), any());
		verify(postCaptureCheckoutService).completeCheckout(postCaptureCheckoutActionContext);

	}
}
