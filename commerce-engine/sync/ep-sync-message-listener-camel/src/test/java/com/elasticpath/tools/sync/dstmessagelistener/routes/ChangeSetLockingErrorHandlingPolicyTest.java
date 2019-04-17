/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.routes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.model.RedeliveryPolicyDefinition;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.core.messaging.changeset.ChangeSetEventType;
import com.elasticpath.domain.changeset.ChangeSet;
import com.elasticpath.domain.changeset.ChangeSetStateCode;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.camel.test.support.TransactionPolicyRegistryManager;
import com.elasticpath.messaging.impl.EventMessageImpl;
import com.elasticpath.tools.sync.dstmessagelistener.changesets.ChangeSetLoader;
import com.elasticpath.tools.sync.dstmessagelistener.changesets.ChangeSetStateUpdater;

/**
 * Test class for {@link ChangeSetLockingErrorHandlingPolicy}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ChangeSetLockingErrorHandlingPolicyTest extends CamelTestSupport {

	private static final int MAX_REDELIVERY_ATTEMPTS = 2;
	private static final long TIMEOUT_SECONDS = 2L;
	private static final String CHANGE_SET_GUID = UUID.randomUUID().toString();

	private ChangeSetLockingErrorHandlingPolicy errorHandlingPolicy;

	private Endpoint sourceEndpoint;
	private MockEndpoint outputEndpoint;
	private MockEndpoint dlqEndpoint;

	@Mock
	private Processor failureGeneratingProcessor;

	@Mock
	private ChangeSetLoader changeSetLoader;

	@Mock
	private ChangeSetStateUpdater changeSetStateUpdater;

	@Mock
	private DataFormat dataFormat;

	@Mock
	private ChangeSet changeSet;

	private EventMessage eventMessage;

	@SuppressWarnings("unchecked")
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		eventMessage = new EventMessageImpl(ChangeSetEventType.CHANGE_SET_READY_FOR_PUBLISH, CHANGE_SET_GUID);

		sourceEndpoint = getMandatoryEndpoint("direct:ChangeSetLockingErrorHandlingPolicyTest.source");
		outputEndpoint = getMockEndpoint("mock:ChangeSetLockingErrorHandlingPolicyTest.target");
		dlqEndpoint = getMockEndpoint("mock:ChangeSetLockingErrorHandlingPolicyTest.dlq");

		final RedeliveryPolicyDefinition redeliveryPolicy = new RedeliveryPolicyDefinition()
				.maximumRedeliveries(MAX_REDELIVERY_ATTEMPTS)
				.redeliveryDelay(0L);

		when(changeSetLoader.load(CHANGE_SET_GUID)).thenReturn(changeSet);
		when(changeSet.getGuid()).thenReturn(CHANGE_SET_GUID);

		errorHandlingPolicy = new ChangeSetLockingErrorHandlingPolicy();
		errorHandlingPolicy.setDeadLetterQueueEndpoint(dlqEndpoint);
		errorHandlingPolicy.setRedeliveryPolicy(redeliveryPolicy);
		errorHandlingPolicy.setRecoverableExceptions(SampleRecoverableException.class);
		errorHandlingPolicy.setChangeSetLoader(changeSetLoader);
		errorHandlingPolicy.setChangeSetStateUpdater(changeSetStateUpdater);
		errorHandlingPolicy.setEventMessageDataFormat(dataFormat);

		context().addRoutes(new RouteBuilder() {
			@Override
			public void configure() {
				errorHandlingPolicy.configureErrorHandlingPolicy(this);

				from(sourceEndpoint)
						.process(failureGeneratingProcessor)
						.to(outputEndpoint);
			}
		});
	}

	@Override
	protected JndiRegistry createRegistry() throws Exception {
		final JndiRegistry registry = super.createRegistry();

		new TransactionPolicyRegistryManager(registry).registerDefaultTransactionPolicy();

		return registry;
	}

	@Test
	public void testSuccessfullyRetriedMessagesDoNotLockTheChangeSet() throws Exception {
		// Fail once, then succeed
		doThrow(new SampleRecoverableException())
				.doNothing()
				.when(failureGeneratingProcessor).process(any(Exchange.class));

		executeAndWaitForMessageOnEndpoint(() -> template().sendBody(sourceEndpoint, json(eventMessage)), outputEndpoint, TIMEOUT_SECONDS);

		verify(changeSetStateUpdater, never()).updateState(changeSet, ChangeSetStateCode.LOCKED);
	}

	@Test
	public void verifyChangeSetLockedWhenAttemptsAreExhausted() throws Exception {
		doThrow(new SampleRecoverableException())
				.when(failureGeneratingProcessor).process(any(Exchange.class));

		Assertions.assertThatThrownBy(() -> template().sendBody(sourceEndpoint, json(eventMessage)))
				.as("Messages should fail when retry attempts are exhausted")
				.hasCauseInstanceOf(RuntimeCamelException.class)
				.hasRootCauseInstanceOf(SampleRecoverableException.class);

		verify(changeSetStateUpdater).updateState(changeSet, ChangeSetStateCode.LOCKED);
	}

	@Test
	public void verifyChangeSetLockedOnUnrecoverableErrors() throws Exception {
		doThrow(new SampleUnrecoverableException())
				.when(failureGeneratingProcessor).process(any(Exchange.class));

		given(changeSet.getStateCode()).willReturn(ChangeSetStateCode.READY_TO_PUBLISH);

		executeAndWaitForMessageOnEndpoint(() -> template().sendBody(sourceEndpoint, json(eventMessage)),
										   dlqEndpoint,
										   TIMEOUT_SECONDS
		);

		verify(changeSetStateUpdater).updateState(changeSet, ChangeSetStateCode.LOCKED);
	}

	@Test
	public void verifyChangeSetNotLockedIfChangeSetAlreadyFinalised() throws Exception {
		doThrow(new SampleUnrecoverableException())
				.when(failureGeneratingProcessor).process(any(Exchange.class));

		given(changeSet.getStateCode()).willReturn(ChangeSetStateCode.FINALIZED);

		executeAndWaitForMessageOnEndpoint(() -> template().sendBody(sourceEndpoint, json(eventMessage)),
										   dlqEndpoint,
										   TIMEOUT_SECONDS);

		verify(changeSetStateUpdater, never()).updateState(changeSet, ChangeSetStateCode.LOCKED);

	}

	private void executeAndWaitForMessageOnEndpoint(final Runnable runnable, final MockEndpoint targetEndpoint, final long timeoutSeconds) {
		targetEndpoint.expectedMessageCount(1);

		final NotifyBuilder notifyBuilder = new NotifyBuilder(context())
				.whenDoneSatisfied(targetEndpoint)
				.create();

		runnable.run();

		Assertions.assertThat(notifyBuilder.matches(TIMEOUT_SECONDS, TimeUnit.SECONDS))
				.as("Did not receive message(s) on endpoint within " + timeoutSeconds + " seconds")
				.isTrue();
	}

	private static final class SampleRecoverableException extends Exception {
		private static final long serialVersionUID = -4537598125134146448L;
	}

	private static final class SampleUnrecoverableException extends Exception {
		private static final long serialVersionUID = -7545805146072820652L;
	}

	private String json(final EventMessage eventMessage) {
		try {
			return new ObjectMapper().writeValueAsString(eventMessage);
		} catch (JsonProcessingException exception) {
			throw new EpServiceException("Exception encountered while marshalling JSON", exception);
		}
	}

}