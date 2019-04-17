/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.routes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import org.apache.camel.Endpoint;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.model.RedeliveryPolicyDefinition;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.core.messaging.changeset.ChangeSetEventType;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePredicate;
import com.elasticpath.messaging.camel.jackson.EventMessageObjectMapper;
import com.elasticpath.messaging.error.handling.policy.impl.RecoverableRetryErrorHandlingPolicy;
import com.elasticpath.messaging.factory.impl.EventMessageFactoryImpl;
import com.elasticpath.messaging.impl.EventMessageImpl;
import com.elasticpath.messaging.spi.impl.EventTypeProviderImpl;
import com.elasticpath.tools.sync.dstmessagelistener.messages.ChangeSetSummaryMessage;
import com.elasticpath.tools.sync.dstmessagelistener.messages.DataSyncEventMessageBuilder;
import com.elasticpath.tools.sync.dstmessagelistener.messages.DataSyncEventMessageService;
import com.elasticpath.tools.sync.dstmessagelistener.publishing.ChangeSetPublisher;
import com.elasticpath.tools.sync.dstmessagelistener.publishing.impl.ChangeSetUserConstants;
import com.elasticpath.tools.sync.target.result.SyncErrorResultItem;
import com.elasticpath.tools.sync.target.result.SyncResultItem;

/**
 * Tests for {@link ChangeSetPublishEventRoute}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ChangeSetPublishEventRouteTest extends CamelTestSupport {

	private static final long TIMEOUT_SECONDS = 2L;
	private static final String DST_PUBLISH_SUCCESS_PROPERTY = "DST_PUBLISH_SUCCESS";
	private static final int MAX_REDELIVERY_ATTEMPTS = 2;
	private static final String PUBLISH_SUMMARY = "Change Set published.";

	@Mock
	private ChangeSetPublisher changeSetPublisher;

	@Mock
	private ChangeSetSummaryMessage changeSetSummaryMessage;

	@Mock
	private DataSyncEventMessageService dataSyncEventMessageService;

	private DataSyncEventMessageBuilder<DataSyncEventMessageBuilder<?>> dataSyncEventMessageBuilder;

	@Mock
	private EventMessagePredicate eventMessagePredicateFilter;

	@InjectMocks
	private ChangeSetPublishEventRoute changeSetPublishEventRoute;

	private Endpoint incomingEndpoint;
	private MockEndpoint deadLetterEndpoint;

	private EmbeddedDatabase database;
	private RecoverableRetryErrorHandlingPolicy errorHandlingPolicy;

	private Map<String, String> changeSetCreator;
	private Map<String, String> changeSetPublishInitiator;

	private final Collection<SyncResultItem> nonEmptySuccessResults = Collections.singleton(mock(SyncResultItem.class));
	private final Collection<SyncErrorResultItem> nonEmptyFailureResults = Collections.singleton(mock(SyncErrorResultItem.class));

	@Override
	@Before
	@SuppressWarnings("unchecked")
	public void setUp() throws Exception {
		super.setUp();

		dataSyncEventMessageBuilder = mock(
				DataSyncEventMessageBuilder.class,
				(Answer<DataSyncEventMessageBuilder<DataSyncEventMessageBuilder<?>>>) invocation -> dataSyncEventMessageBuilder);

		doReturn(dataSyncEventMessageBuilder).when(dataSyncEventMessageService).prepareMessage();

		incomingEndpoint = getMandatoryEndpoint("direct:incoming");
		deadLetterEndpoint = getMockEndpoint("mock:dlq");

		final RedeliveryPolicyDefinition redeliveryPolicy = new RedeliveryPolicyDefinition()
				.maximumRedeliveries(MAX_REDELIVERY_ATTEMPTS)
				.redeliveryDelay(0L); // go fast

		errorHandlingPolicy = new RecoverableRetryErrorHandlingPolicy();
		errorHandlingPolicy.setDeadLetterQueueEndpoint(deadLetterEndpoint);
		errorHandlingPolicy.setRedeliveryPolicy(redeliveryPolicy);
		errorHandlingPolicy.setRecoverableExceptions(EpServiceException.class);

		changeSetPublishEventRoute.setRouteId("changeSetPublishEventRoute");
		changeSetPublishEventRoute.setSourceEndpoint(incomingEndpoint);
		changeSetPublishEventRoute.setErrorHandlingPolicy(errorHandlingPolicy);
		changeSetPublishEventRoute.setDataSyncEventMessageService(dataSyncEventMessageService);

		changeSetCreator = ImmutableMap.of("guid", ChangeSetUserConstants.CHANGE_SET_CREATOR_GUID,
										   "username", ChangeSetUserConstants.CHANGE_SET_CREATOR_USERNAME,
										   "firstName", ChangeSetUserConstants.CHANGE_SET_CREATOR_FIRST_NAME,
										   "lastName", ChangeSetUserConstants.CHANGE_SET_CREATOR_LAST_NAME,
										   "emailAddress", ChangeSetUserConstants.CHANGE_SET_CREATOR_EMAIL_ADDRESS);

		changeSetPublishInitiator = ImmutableMap.of("guid", ChangeSetUserConstants.CHANGE_SET_PUBLISH_INITIATOR_GUID,
													"username", ChangeSetUserConstants.CHANGE_SET_PUBLISH_INITIATOR_USERNAME,
													"firstName", ChangeSetUserConstants.CHANGE_SET_PUBLISH_INITIATOR_FIRST_NAME,
													"lastName", ChangeSetUserConstants.CHANGE_SET_PUBLISH_INITIATOR_LAST_NAME,
													"emailAddress", ChangeSetUserConstants.CHANGE_SET_PUBLISH_INITIATOR_EMAIL_ADDRESS);

		when(changeSetPublisher.publish(eq(ChangeSetUserConstants.CHANGE_SET_GUID))).thenReturn(changeSetSummaryMessage);

		final EventMessageObjectMapper objectMapper = new EventMessageObjectMapper();
		objectMapper.setEventMessageFactory(new EventMessageFactoryImpl());
		objectMapper.init();

		objectMapper.registerEventType(new EventTypeProviderImpl<>(ChangeSetEventType.class,
																   new ChangeSetEventType.ChangeSetEventTypeLookup()));

		final JacksonDataFormat jacksonDataFormat = new JacksonDataFormat(objectMapper, EventMessage.class);
		changeSetPublishEventRoute.setEventMessageDataFormat(jacksonDataFormat);

		when(eventMessagePredicateFilter.apply(any(EventMessage.class)))
				.thenAnswer(invocation -> {
					final EventMessage eventMessage = (EventMessage) invocation.getArguments()[0];
					return eventMessage.getEventType() == ChangeSetEventType.CHANGE_SET_READY_FOR_PUBLISH;
				});

		context().addRoutes(changeSetPublishEventRoute);
	}

	@Override
	protected JndiRegistry createRegistry() throws Exception {
		final JndiRegistry reg = super.createRegistry();

		database = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build();
		reg.bind("testdb", database);

		final DataSourceTransactionManager txMgr = new DataSourceTransactionManager();
		txMgr.setDataSource(database);
		reg.bind("txManager", txMgr);

		final SpringTransactionPolicy txPolicy = new SpringTransactionPolicy();
		txPolicy.setTransactionManager(txMgr);
		txPolicy.setPropagationBehaviorName("PROPAGATION_REQUIRED");
		reg.bind("required", txPolicy);

		return reg;
	}

	@Test
	public void verifyNonReadyForPublishMessagesAreDiscarded() throws Exception {
		final EventMessage inputEventMessage =
				createEventMessage(ChangeSetEventType.CHANGE_SET_PUBLISHED, ChangeSetUserConstants.CHANGE_SET_GUID, createDefaultDataBuilder(
						ChangeSetUserConstants.CHANGE_SET_NAME, changeSetCreator, changeSetPublishInitiator).build());

		sendMessage(inputEventMessage);

		verifyZeroInteractions(dataSyncEventMessageService);
	}

	@Test
	public void testSuccessfulPublishSendsEventMessage() throws Exception {
		givenPublishSummaryHas(changeSetSummaryMessage,
							   ChangeSetUserConstants.CHANGE_SET_GUID,
							   nonEmptySuccessResults,
							   Collections.emptySet(),
							   PUBLISH_SUMMARY);
		given(changeSetSummaryMessage.isSuccess()).willReturn(true);

		final ImmutableMap.Builder<String, Object> defaultDataBuilder =
				createDefaultDataBuilder(ChangeSetUserConstants.CHANGE_SET_NAME, changeSetCreator, changeSetPublishInitiator);

		final ImmutableMap<String, Object> inputData = defaultDataBuilder.build();

		final EventMessage inputEventMessage =
				createEventMessage(ChangeSetEventType.CHANGE_SET_READY_FOR_PUBLISH, ChangeSetUserConstants.CHANGE_SET_GUID, inputData);

		sendMessage(inputEventMessage);

		verify(dataSyncEventMessageBuilder).withChangeSetGuid(eq(ChangeSetUserConstants.CHANGE_SET_GUID));
		verify(dataSyncEventMessageBuilder).withSuccess(eq(true));
		verify(dataSyncEventMessageBuilder).withChangeSetName(eq(ChangeSetUserConstants.CHANGE_SET_NAME));
		verify(dataSyncEventMessageBuilder).withChangeSetCreatorData(eq(changeSetCreator));
		verify(dataSyncEventMessageBuilder).withChangeSetPublishInitiator(eq(changeSetPublishInitiator));
		verify(dataSyncEventMessageBuilder).build();
	}

	@Test
	public void verifyUnsuccessfulPublishSendsEventMessage() throws Exception {
		givenPublishSummaryHas(changeSetSummaryMessage,
							   ChangeSetUserConstants.CHANGE_SET_GUID,
							   nonEmptySuccessResults,
							   nonEmptyFailureResults,
							   PUBLISH_SUMMARY);
		given(changeSetSummaryMessage.isSuccess()).willReturn(false);

		final ImmutableMap.Builder<String, Object> defaultDataBuilder =
				createDefaultDataBuilder(ChangeSetUserConstants.CHANGE_SET_NAME, changeSetCreator, changeSetPublishInitiator);

		final ImmutableMap<String, Object> inputData = defaultDataBuilder.build();

		final EventMessage inputEventMessage =
				createEventMessage(ChangeSetEventType.CHANGE_SET_READY_FOR_PUBLISH, ChangeSetUserConstants.CHANGE_SET_GUID, inputData);

		sendMessage(inputEventMessage);

		verify(dataSyncEventMessageBuilder).withChangeSetGuid(eq(ChangeSetUserConstants.CHANGE_SET_GUID));
		verify(dataSyncEventMessageBuilder).withSuccess(eq(false));
		verify(dataSyncEventMessageBuilder).withChangeSetName(eq(ChangeSetUserConstants.CHANGE_SET_NAME));
		verify(dataSyncEventMessageBuilder).withChangeSetCreatorData(eq(changeSetCreator));
		verify(dataSyncEventMessageBuilder).withChangeSetPublishInitiator(eq(changeSetPublishInitiator));
		verify(dataSyncEventMessageBuilder).build();
	}

	private void givenPublishSummaryHas(final ChangeSetSummaryMessage changeSetSummaryMessage,
										final String changeSetGuid,
										final Collection<SyncResultItem> successResults,
										final Collection<SyncErrorResultItem> failureResults,
										final String publishSummaryMessage) {
		given(changeSetSummaryMessage.getChangeSetGuid()).willReturn(changeSetGuid);
		given(changeSetSummaryMessage.getSyncSuccessResults()).willReturn(successResults);
		given(changeSetSummaryMessage.getSyncErrorResults()).willReturn(failureResults);
		given(changeSetSummaryMessage.getPublishSummary()).willReturn(publishSummaryMessage);
	}

	private void sendMessage(final EventMessage eventMessage) throws Exception {
		final NotifyBuilder notifyBuilder = new NotifyBuilder(context())
				.from(incomingEndpoint.getEndpointUri())
				.whenDone(1)
				.create();

		template().sendBodyAndProperty(incomingEndpoint, getJson(eventMessage), DST_PUBLISH_SUCCESS_PROPERTY, true);

		assertTrue("Did not receive message(s) on endpoint within " + TIMEOUT_SECONDS + " seconds", notifyBuilder.matches(TIMEOUT_SECONDS,
																														  TimeUnit.SECONDS));
	}

	private ImmutableMap.Builder<String, Object> createDefaultDataBuilder(final String changeSetName,
																		  final Map<String, String> changeSetCreator,
																		  final Map<String, String> changeSetPublishInitiator) {
		return ImmutableMap.<String, Object>builder()
				.put("changeSetName", changeSetName)
				.put("changeSetCreator", changeSetCreator)
				.put("changeSetPublishInitiator", changeSetPublishInitiator);

	}

	private EventMessage createEventMessage(final ChangeSetEventType changeSetEventType,
											final String guid,
											final Map<String, Object> data) {
		return new EventMessageImpl(changeSetEventType, guid, data);
	}

	private String getJson(final EventMessage eventMessage) throws JsonProcessingException {
		final EventMessageObjectMapper eventMessageObjectMapper = new EventMessageObjectMapper();
		eventMessageObjectMapper.init();
		return eventMessageObjectMapper.writeValueAsString(eventMessage);
	}

}