/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.orderprocessing.orderhold.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePredicate;

/**
 * Tests for {@link OrderHoldResolutionRouteBuilder}.
 */
@RunWith(MockitoJUnitRunner.class)
public class OrderHoldResolutionRouteBuilderTest extends CamelTestSupport {

	private static final int MAXIMUM_EXECUTION_TIMES = 6;
	
	private Endpoint incomingEndpoint;

	private MockEndpoint errorEndpoint;

	private MockEndpoint outgoingEndpoint;

	@Mock
	private EventMessage eventMessage;

	@Mock
	private EventMessagePredicate predicate;

	@Mock
	private DataFormat eventMessageDataFormat;

	@Mock
	private HoldResolutionMessageTransformer transformer;

	@Mock
	private HoldResolutionMessageProcessor processor;

	@InjectMocks
	private OrderHoldResolutionRouteBuilder targetRouteBuilder;

	private static final String EVENT_MESSAGE_STRING = "{\n"
			+ "  \"eventType\": {\n"
			+ "    \"@class\": \"OrderEventType\",\n"
			+ "    \"name\": \"ORDER_HOLDS_RESOLVED\"\n"
			+ "  },\n"
			+ "  \"guid\": \"testOrderNumber\",\n"
			+ "  \"data\": {\n"
			+ "    \"orderhold_guid\": \"987654\",\n"
			+ "    \"cm_username\": \"admin\",\n"
			+ "    \"comment\": "
			+ "			\"The provided check did not clear.  Please call customer service or submit a new order if you feel this is in error.\",\n"
			+ "    \"status\": \"UNRESOLVABLE\""
			+ "	  }\n"
			+ "}";

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		incomingEndpoint = getMandatoryEndpoint("direct:incoming");
		errorEndpoint = getMockEndpoint("mock:error");
		outgoingEndpoint = getMockEndpoint("mock:outgoing");

		targetRouteBuilder.setRouteId("testRouteId");
		targetRouteBuilder.setIncomingEndpoint(incomingEndpoint);
		targetRouteBuilder.setErrorEndpoint(errorEndpoint);

		given(eventMessageDataFormat.unmarshal(any(Exchange.class), any(InputStream.class))).willReturn(eventMessage);
		given(predicate.apply(eventMessage)).willReturn(true);

		context().addRoutes(targetRouteBuilder);

		//adds mock endpoints since actual route does not have outgoing endpoints
		context().getRouteDefinitions().get(0).adviceWith(context(), new AdviceWithRouteBuilder() {
			@Override
			public void configure() {
				weaveById("holdResolutionMessageProcessor").after().to(outgoingEndpoint);
			}
		});
	}

	@Test
	public void testRoute() throws Exception {

		errorEndpoint.expectedMessageCount(0);
		outgoingEndpoint.expectedMessageCount(1);

		template().sendBody(incomingEndpoint, buildEventMessage());

		assertMockEndpointsSatisfied();

		verify(transformer).transform(any());
		verify(processor).process(any());

	}

	@Test
	public void testRouteWithIncorrectEventMessageType() throws Exception {

		given(predicate.apply(eventMessage)).willReturn(false);

		errorEndpoint.expectedMessageCount(0);
		outgoingEndpoint.expectedMessageCount(0);

		template().sendBody(incomingEndpoint, buildEventMessage());

		verify(transformer, never()).transform(any());
		verify(processor, never()).process(any());

		assertMockEndpointsSatisfied();

	}

	@Test
	public void testRouteWithRetries() throws Exception {

		doThrow(UnableToLockOrderException.class).when(processor).process(any());

		errorEndpoint.expectedMessageCount(1);
		outgoingEndpoint.expectedMessageCount(0);

		template().sendBody(incomingEndpoint, buildEventMessage());
		verify(processor, times(MAXIMUM_EXECUTION_TIMES)).process(any());

		assertMockEndpointsSatisfied();

	}

	private String buildEventMessage() {
		return EVENT_MESSAGE_STRING;
	}
}
