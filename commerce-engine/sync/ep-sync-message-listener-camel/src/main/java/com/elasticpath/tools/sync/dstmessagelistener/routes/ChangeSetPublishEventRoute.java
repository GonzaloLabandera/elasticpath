/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.routes;

import static com.elasticpath.tools.sync.dstmessagelistener.commons.constants.DataTransferExchangeConstants.CHANGE_SET_CREATOR_KEY;
import static com.elasticpath.tools.sync.dstmessagelistener.commons.constants.DataTransferExchangeConstants.CHANGE_SET_GUID_KEY;
import static com.elasticpath.tools.sync.dstmessagelistener.commons.constants.DataTransferExchangeConstants.CHANGE_SET_NAME_KEY;
import static com.elasticpath.tools.sync.dstmessagelistener.commons.constants.DataTransferExchangeConstants.CHANGE_SET_PUBLISH_INITIATOR_KEY;

import java.util.Map;

import org.apache.camel.Body;
import org.apache.camel.Endpoint;
import org.apache.camel.ExchangeProperty;
import org.apache.camel.Handler;
import org.apache.camel.language.Simple;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.spring.SpringRouteBuilder;

import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePredicate;
import com.elasticpath.messaging.error.handling.policy.ErrorHandlingPolicy;
import com.elasticpath.tools.sync.dstmessagelistener.messages.ChangeSetSummaryMessage;
import com.elasticpath.tools.sync.dstmessagelistener.messages.DataSyncEventMessageService;
import com.elasticpath.tools.sync.dstmessagelistener.publishing.ChangeSetPublisher;

/**
 * Route for Publishing Change Sets.
 */
public class ChangeSetPublishEventRoute extends SpringRouteBuilder {

	private DataSyncEventMessageService dataSyncEventMessageService;

	private Endpoint sourceEndpoint;

	private String targetEndPointUri;

	private DataFormat eventMessageDataFormat;

	private ErrorHandlingPolicy errorHandlingPolicy;

	private ChangeSetPublisher changeSetPublisher;

	private String transactionPolicyBeanId;

	private String routeId;

	private EventMessagePredicate eventMessagePredicateFilter;

	@Override
	public void configure() throws Exception {
		getErrorHandlingPolicy().configureErrorHandlingPolicy(this);
		from(getSourceEndpoint())
				.routeId(getRouteId())
				.transacted(getTransactionPolicyBeanId())
				.unmarshal(getEventMessageDataFormat())
				.filter().method(getEventMessagePredicateFilter())
				.setProperty(CHANGE_SET_GUID_KEY, simple("${body.guid}"))
				.setProperty(CHANGE_SET_NAME_KEY, simple("${body.data[" + CHANGE_SET_NAME_KEY + "]}"))
				.setProperty(CHANGE_SET_CREATOR_KEY, simple("${body.data[" + CHANGE_SET_CREATOR_KEY + "]}"))
				.setProperty(CHANGE_SET_PUBLISH_INITIATOR_KEY, simple("${body.data[" + CHANGE_SET_PUBLISH_INITIATOR_KEY + "]}"))
				.bean(this, "processChangeSet")
				.bean(this, "createSyncCompletedEventMessage")
				.marshal(getEventMessageDataFormat())
				.to(getTargetEndPointUri());
	}

	/**
	 * Publishes a Change Set.
	 *
	 * @param changeSetGuid the GUID of the change set to publish
	 * @return a summary of the publish attempt
	 */
	@SuppressWarnings("unchecked")
	@Handler
	public ChangeSetSummaryMessage processChangeSet(@Simple("${body.guid}") final String changeSetGuid) {
		return getChangeSetPublisher().publish(changeSetGuid);
	}

	/**
	 * Publishes an Event Message announcing the successful or unsuccessful change set synchronisation attempt.
	 *
	 * @param changeSetSummaryMessage       the change set summary message
	 * @param changeSetName                 the name of the Change Set
	 * @param changeSetCreatorData          the user that created the Change Set
	 * @param changeSetPublishInitiatorData the user that initiated the publish of the Change Set
	 * @return EventMessage the message
	 */
	@Handler
	public EventMessage createSyncCompletedEventMessage(@Body final ChangeSetSummaryMessage changeSetSummaryMessage,
			@ExchangeProperty(CHANGE_SET_NAME_KEY) final String changeSetName,
			@ExchangeProperty(CHANGE_SET_CREATOR_KEY) final Map<String, String> changeSetCreatorData,
			@ExchangeProperty(CHANGE_SET_PUBLISH_INITIATOR_KEY) final Map<String, String> changeSetPublishInitiatorData) {

		return getDataSyncEventMessageService().prepareMessage()
				.withChangeSetGuid(changeSetSummaryMessage.getChangeSetGuid())
				.withChangeSetName(changeSetName)
				.withChangeSetCreatorData(changeSetCreatorData)
				.withChangeSetPublishInitiator(changeSetPublishInitiatorData)
				.withSuccess(changeSetSummaryMessage.isSuccess())
				.withPublishSummary(changeSetSummaryMessage.getPublishSummary())
				.withSyncSuccessResults(changeSetSummaryMessage.getSyncSuccessResults())
				.withSyncErrorResults(changeSetSummaryMessage.getSyncErrorResults())
				.build();
	}

	protected String getTransactionPolicyBeanId() {
		return transactionPolicyBeanId;
	}

	public void setTransactionPolicyBeanId(final String transactionPolicyBeanId) {
		this.transactionPolicyBeanId = transactionPolicyBeanId;
	}

	protected DataFormat getEventMessageDataFormat() {
		return eventMessageDataFormat;
	}

	public void setEventMessageDataFormat(final DataFormat eventMessageDataFormat) {
		this.eventMessageDataFormat = eventMessageDataFormat;
	}

	protected ChangeSetPublisher getChangeSetPublisher() {
		return changeSetPublisher;
	}

	public void setChangeSetPublisher(final ChangeSetPublisher changeSetPublisher) {
		this.changeSetPublisher = changeSetPublisher;
	}

	protected Endpoint getSourceEndpoint() {
		return sourceEndpoint;
	}

	public void setSourceEndpoint(final Endpoint sourceEndpoint) {
		this.sourceEndpoint = sourceEndpoint;
	}

	protected String getTargetEndPointUri() {
		return targetEndPointUri == null ? "jms:queue:ep.dst" : targetEndPointUri;
	}

	public void setTargetEndPointUri(final String targetEndPointUri) {
		this.targetEndPointUri = targetEndPointUri;
	}

	protected ErrorHandlingPolicy getErrorHandlingPolicy() {
		return errorHandlingPolicy;
	}

	public void setErrorHandlingPolicy(final ErrorHandlingPolicy errorHandlingPolicy) {
		this.errorHandlingPolicy = errorHandlingPolicy;
	}

	public void setRouteId(final String routeId) {
		this.routeId = routeId;
	}

	protected String getRouteId() {
		return routeId;
	}

	public void setDataSyncEventMessageService(final DataSyncEventMessageService dataSyncEventMessageService) {
		this.dataSyncEventMessageService = dataSyncEventMessageService;
	}

	protected DataSyncEventMessageService getDataSyncEventMessageService() {
		return dataSyncEventMessageService;
	}

	protected EventMessagePredicate getEventMessagePredicateFilter() {
		return eventMessagePredicateFilter;
	}

	public void setEventMessagePredicateFilter(final EventMessagePredicate eventMessagePredicateFilter) {
		this.eventMessagePredicateFilter = eventMessagePredicateFilter;
	}

}
