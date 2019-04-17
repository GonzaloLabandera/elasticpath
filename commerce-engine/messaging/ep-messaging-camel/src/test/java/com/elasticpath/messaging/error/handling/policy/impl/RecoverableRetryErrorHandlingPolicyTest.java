/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.messaging.error.handling.policy.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.model.RedeliveryPolicyDefinition;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.messaging.camel.test.support.TransactionPolicyRegistryManager;
import com.elasticpath.messaging.error.handling.policy.ErrorHandlingPolicy;

/**
 * Test class for {@link RecoverableRetryErrorHandlingPolicy}.
 */
@RunWith(MockitoJUnitRunner.class)
public class RecoverableRetryErrorHandlingPolicyTest extends CamelTestSupport {

	private static final int MAX_REDELIVERY_ATTEMPTS = 2;
	private static final String MESSAGE_BODY = "Sample, unimportant message contents";
	private static final long TIMEOUT_SECONDS = 2L;

	private Endpoint sourceEndpoint;
	private MockEndpoint targetEndpoint;
	private MockEndpoint dlqEndpoint;

	private RecoverableRetryErrorHandlingPolicy errorHandlingPolicy;

	private SampleRoute sampleRoute;

	@Mock
	private Processor processor;

	@SuppressWarnings("unchecked")
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		sourceEndpoint = getMandatoryEndpoint("direct:RecoverableRetryErrorHandlerPolicyTest.source");
		targetEndpoint = getMockEndpoint("mock:RecoverableRetryErrorHandlerPolicyTest.target");
		dlqEndpoint = getMockEndpoint("mock:RecoverableRetryErrorHandlerPolicyTest.dlq");

		final RedeliveryPolicyDefinition redeliveryPolicy = new RedeliveryPolicyDefinition()
				.maximumRedeliveries(MAX_REDELIVERY_ATTEMPTS)
				.redeliveryDelay(0L); // go fast

		errorHandlingPolicy = new RecoverableRetryErrorHandlingPolicy();
		errorHandlingPolicy.setDeadLetterQueueEndpoint(dlqEndpoint);
		errorHandlingPolicy.setRedeliveryPolicy(redeliveryPolicy);
		errorHandlingPolicy.setRecoverableExceptions(SampleRecoverableException.class);
		sampleRoute = new SampleRoute(sourceEndpoint, targetEndpoint, processor, errorHandlingPolicy);

		context().addRoutes(sampleRoute);
	}

	@Override
	protected JndiRegistry createRegistry() throws Exception {
		final JndiRegistry registry = super.createRegistry();

		new TransactionPolicyRegistryManager(registry).registerDefaultTransactionPolicy();

		return registry;
	}

	@Test
	public void testRecoverableErrorsAreRetriedAndCanSucceed() throws Exception {
		// Fail once, then succeed
		doThrow(new SampleRecoverableException())
				.doNothing()
				.when(processor).process(any(Exchange.class));

		targetEndpoint.expectedMessageCount(1);

		final NotifyBuilder notifyBuilder = new NotifyBuilder(context())
				.whenDoneSatisfied(targetEndpoint)
				.create();

		template().sendBody(sourceEndpoint, MESSAGE_BODY);

		assertConditionsSatisfiedInTime(notifyBuilder, TIMEOUT_SECONDS);

		verify(processor, times(2)).process(any());
	}

	@Test
	public void verifyMessageFailsWhenAttemptsAreExhausted() throws Exception {
		doThrow(new SampleRecoverableException())
				.when(processor).process(any(Exchange.class));

		Assertions.assertThatThrownBy(() -> template().sendBody(sourceEndpoint, MESSAGE_BODY))
				.as("Messages should fail when retry attempts are exhausted")
				.hasCauseInstanceOf(RuntimeCamelException.class)
				.hasRootCauseInstanceOf(SampleRecoverableException.class);

		// +1 because of the initial attempt, which doesn't count as a redelivery. It's just a delivery.
		verify(processor, times(MAX_REDELIVERY_ATTEMPTS + 1)).process(any());
	}

	@Test
	public void verifyUnrecoverableErrorsAreSentDirectlyToJailAndDoNotPassGoAndCertainlyDoNotCollectTwoHundredDollarsNoSir() throws Exception {
		doThrow(new SampleUnrecoverableException())
				.when(processor).process(any(Exchange.class));

		dlqEndpoint.expectedMessageCount(1);

		final NotifyBuilder notifyBuilder = new NotifyBuilder(context())
				.whenDoneSatisfied(dlqEndpoint)
				.create();

		template().sendBody(sourceEndpoint, MESSAGE_BODY);

		assertConditionsSatisfiedInTime(notifyBuilder, TIMEOUT_SECONDS);

		verify(processor, times(1)).process(any());
	}

	private void assertConditionsSatisfiedInTime(final NotifyBuilder notifyBuilder, final long timeoutSeconds) {
		Assertions.assertThat(notifyBuilder.matches(TIMEOUT_SECONDS, TimeUnit.SECONDS))
				.as("Did not receive message(s) on endpoint within " + timeoutSeconds + " seconds")
				.isTrue();
	}

	private static final class SampleRoute extends RouteBuilder {

		private final Endpoint sourceEndpointUri;
		private final Endpoint targetEndpointUri;
		private final Processor processor;
		private final ErrorHandlingPolicy errorHandlingPolicy;

		SampleRoute(final Endpoint sourceEndpointUri, final Endpoint targetEndpointUri, final Processor processor, final ErrorHandlingPolicy
				errorHandlingPolicy) {
			this.sourceEndpointUri = sourceEndpointUri;
			this.targetEndpointUri = targetEndpointUri;
			this.processor = processor;
			this.errorHandlingPolicy = errorHandlingPolicy;
		}

		@Override
		public void configure() throws Exception {
			errorHandlingPolicy.configureErrorHandlingPolicy(this);

			from(sourceEndpointUri)
					.process(processor)
					.to(targetEndpointUri);
		}
	}

	private static final class SampleRecoverableException extends Exception {
		private static final long serialVersionUID = -2076384643225685079L;
	}

	private static final class SampleUnrecoverableException extends Exception {
		private static final long serialVersionUID = 7690146972567013807L;
	}


}
