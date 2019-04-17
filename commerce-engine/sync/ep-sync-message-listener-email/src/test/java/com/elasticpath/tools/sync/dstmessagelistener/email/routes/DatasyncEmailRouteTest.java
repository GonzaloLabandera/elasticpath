/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.email.routes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.apache.camel.Endpoint;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.model.RedeliveryPolicyDefinition;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import com.elasticpath.core.messaging.changeset.ChangeSetEventType;
import com.elasticpath.email.EmailDto;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.camel.jackson.EventMessageObjectMapper;
import com.elasticpath.messaging.impl.EventMessageImpl;
import com.elasticpath.tools.sync.dstmessagelistener.email.converters.EmailDtoConverter;
import com.elasticpath.messaging.error.handling.policy.impl.RecoverableRetryErrorHandlingPolicy;

/**
 * Unit test for <code>{@link DatasyncEmailRoute}</code> route builder.
 */
@RunWith(MockitoJUnitRunner.class)
public class DatasyncEmailRouteTest extends CamelTestSupport {

	private static final String GUID = "CS_GUID_1234";
	private static final int MAX_REDELIVERY_ATTEMPTS = 2;

	private Endpoint sourceEndpoint;

	private MockEndpoint coreEmailEndpoint;
	private MockEndpoint deadLetterEndpoint;

	@Mock
	private DataFormat eventMessageDataFormat;

	@Mock
	private DataFormat emailDtoDataFormat;

	@Mock
	private EmailDtoConverter emailDtoConverter;

	private EmbeddedDatabase database;
	private RecoverableRetryErrorHandlingPolicy errorHandlingPolicy;

	@InjectMocks
	private DatasyncEmailRoute datasyncEmailRoute;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		sourceEndpoint = getMandatoryEndpoint("direct:source");
		coreEmailEndpoint = getMockEndpoint("mock:coreEmail");
		deadLetterEndpoint = getMockEndpoint("mock:dlq");

		final RedeliveryPolicyDefinition redeliveryPolicy = new RedeliveryPolicyDefinition()
				.maximumRedeliveries(MAX_REDELIVERY_ATTEMPTS)
				.redeliveryDelay(0L); // go fast

		errorHandlingPolicy = new RecoverableRetryErrorHandlingPolicy();
		errorHandlingPolicy.setDeadLetterQueueEndpoint(deadLetterEndpoint);
		errorHandlingPolicy.setRedeliveryPolicy(redeliveryPolicy);

		datasyncEmailRoute.setRouteId("datasyncEmailRoute");
		datasyncEmailRoute.setSourceEndpoint(sourceEndpoint);
		datasyncEmailRoute.setEventMessageDataFormat(eventMessageDataFormat);
		datasyncEmailRoute.setEmailDtoDataFormat(emailDtoDataFormat);
		datasyncEmailRoute.setCoreEmailEndpoint(coreEmailEndpoint);
		datasyncEmailRoute.setEmailDtoConverter(emailDtoConverter);
		datasyncEmailRoute.setErrorHandlingPolicy(errorHandlingPolicy);

		given(eventMessageDataFormat.unmarshal(any(), any())).willReturn(createEventMessage(ChangeSetEventType.CHANGE_SET_PUBLISHED));
		given(emailDtoConverter.fromEventMessage(any(EventMessage.class))).willReturn(new EmailDto());

		context().setTracing(true);
		context().addRoutes(datasyncEmailRoute);
	}

	@Override
	protected JndiRegistry createRegistry() throws Exception {
		JndiRegistry reg = super.createRegistry();

		database = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build();
		reg.bind("testdb", database);

		DataSourceTransactionManager txMgr = new DataSourceTransactionManager();
		txMgr.setDataSource(database);
		reg.bind("txManager", txMgr);

		SpringTransactionPolicy txPolicy = new SpringTransactionPolicy();
		txPolicy.setTransactionManager(txMgr);
		txPolicy.setPropagationBehaviorName("PROPAGATION_REQUIRED");
		reg.bind("required", txPolicy);

		return reg;
	}

	@Test
	public void testSimpleMessageGoesThrough() throws InterruptedException, JsonProcessingException {
		// Given
		String eventMessage = getJson(createEventMessage(ChangeSetEventType.CHANGE_SET_PUBLISHED));

		// When
		template().sendBody(sourceEndpoint, eventMessage);

		// Then
		coreEmailEndpoint.expectedMessageCount(1);
		coreEmailEndpoint.assertIsSatisfied();
	}

	@Test
	public void testEventMessageProcessing() throws Exception {
		// Given
		String eventMessage = getJson(createEventMessage(ChangeSetEventType.CHANGE_SET_READY_FOR_PUBLISH));

		// When
		template().sendBody(sourceEndpoint, eventMessage);

		// Then
		verify(eventMessageDataFormat).unmarshal(any(), any());
		verify(emailDtoConverter).fromEventMessage(any(EventMessage.class));
		verify(emailDtoDataFormat).marshal(any(), any(), any());
	}

	@Test
	public void testFilterUnwantedEvents() throws Exception {
		// Given
		String eventMessage = getJson(createEventMessage(ChangeSetEventType.CHANGE_SET_READY_FOR_PUBLISH));
		given(eventMessageDataFormat.unmarshal(any(), any())).willReturn(createEventMessage(ChangeSetEventType.CHANGE_SET_READY_FOR_PUBLISH));

		// When
		template().sendBody(sourceEndpoint, eventMessage);

		// Then
		coreEmailEndpoint.expectedMessageCount(0);
		coreEmailEndpoint.assertIsSatisfied();
	}

	@Test
	public void testErrorsAreSentToDLQ() throws Exception {
		// Given
		String eventMessage = getJson(createEventMessage(ChangeSetEventType.CHANGE_SET_READY_FOR_PUBLISH));
		given(eventMessageDataFormat.unmarshal(any(), any())).willThrow(new Exception("Error"));

		// When
		template().sendBody(sourceEndpoint, eventMessage);

		// Then
		coreEmailEndpoint.expectedMessageCount(0);
		deadLetterEndpoint.expectedMessageCount(1);
		coreEmailEndpoint.assertIsSatisfied();
		deadLetterEndpoint.assertIsSatisfied();
	}

	private EventMessage createEventMessage(final ChangeSetEventType changeSetEventType) {
		return new EventMessageImpl(changeSetEventType, GUID);
	}

	private String getJson(final EventMessage eventMessage) throws JsonProcessingException {
		final EventMessageObjectMapper eventMessageObjectMapper = new EventMessageObjectMapper();
		eventMessageObjectMapper.init();
		return eventMessageObjectMapper.writeValueAsString(eventMessage);
	}

}