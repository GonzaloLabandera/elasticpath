/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.messaging.camel.itest;

import static org.awaitility.Awaitility.await;
import static org.awaitility.Duration.TEN_SECONDS;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.EndpointInject;
import org.apache.camel.ServiceStatus;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.elasticpath.test.integration.junit.DatabaseHandlingTestExecutionListener;
import com.elasticpath.test.support.junit.JmsRegistrationTestExecutionListener;

/**
 * Functional test class that verifies that routes can be configured to support ActiveMQ Virtual Topics.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/integration-context.xml")
@TestExecutionListeners({
		JmsRegistrationTestExecutionListener.class,
		DatabaseHandlingTestExecutionListener.class,
		DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class,
		TransactionalTestExecutionListener.class
})
public class ActiveMqVirtualTopicConsumptionITest {

	private static final long SECONDS_TO_WAIT_FOR_MESSAGE_CONSUMPTION = 10;
	private static final String ROUTE_ID_ONE = "r1";
	private static final String ROUTE_ID_TWO = "r2";

	@Autowired
	@Qualifier("ep-messaging-camel-itest")
	private CamelContext context;

	@Autowired
	private Endpoint publishingEndpoint;

	@Autowired
	private Endpoint consumerEndpointOne;

	@Autowired
	private Endpoint consumerEndpointTwo;

	// Used to validate that each consumer actually consumed a message.
	@EndpointInject(uri = "mock:monitor", context = "ep-messaging-camel-itest")
	private MockEndpoint monitor;

	/**
	 * <p>Verifies that the Settings Framework values, in conjunction with the SettingValueBackedCamelEndpointFactoryBean,
	 * can be used to configure different endpoint URIs for messaging publishing and consumption.  This is required when using Virtual Topics,
	 * a feature supported by many JMS brokers whereby messages sent to a JMS Topic are forwarded to one or many JMS queues.  This allows both a
	 * pub/sub pattern alongside competing consumers for HA and throughput optimisation.</p>
	 * <p/>
	 * <p>The test steps are as follows:</p>
	 * <ol>
	 * <li>Send a message to VirtualTopic.Foo</li>
	 * <li>Show that the message can be consumed on a Consumer.A.VirtualTopic.Foo queue</li>
	 * <li>Show that the message can also be consumed on a Consumer.B.VirtualTopic.Foo queue</li>
	 * </ol>
	 * <p>The objective is to be able to enable this functionality by introducing settings context values,
	 * rather than modifying code or Spring configuration.</p>
	 *
	 * @throws Exception
	 */
	@Test
	public void verifySettingFrameworkEndpointUrisSupportAsymmetricalPublishAndSubscribeEndpointUris() throws Exception {
		// one per consumer
		final int expectedNumberOfReceivedMessages = 2;

		monitor.expectedMessageCount(expectedNumberOfReceivedMessages);
		RouteBuilder r1 = new MessageConsumer(consumerEndpointOne, ROUTE_ID_ONE);
		RouteBuilder r2 = new MessageConsumer(consumerEndpointTwo, ROUTE_ID_TWO);

		context.addRoutes(r1);
		context.addRoutes(r2);

		await().atMost(TEN_SECONDS).until(() -> context.getRouteStatus(ROUTE_ID_ONE) == ServiceStatus.Started);
		await().atMost(TEN_SECONDS).until(() -> context.getRouteStatus(ROUTE_ID_TWO) == ServiceStatus.Started);


		context.createProducerTemplate().sendBody(publishingEndpoint, "Message");

		monitor.assertIsSatisfied(SECONDS_TO_WAIT_FOR_MESSAGE_CONSUMPTION);
	}

	/**
	 * Consumes messages from a dedicated queue, forwarded by the Virtual Topic.
	 */
	class MessageConsumer extends RouteBuilder {

		private final Endpoint incomingEndpoint;
		private final String id;

		MessageConsumer(final Endpoint incomingEndpoint, final String id) {
			this.incomingEndpoint = incomingEndpoint;
			this.id = id;
		}

		@Override
		public void configure() {
			from(incomingEndpoint)
					.routeId(id)
					.to(monitor);
		}

	}

}