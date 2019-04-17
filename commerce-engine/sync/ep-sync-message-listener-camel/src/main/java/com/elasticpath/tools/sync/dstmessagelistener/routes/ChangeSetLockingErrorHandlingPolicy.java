/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.routes;

import static com.elasticpath.tools.sync.dstmessagelistener.commons.constants.DataTransferExchangeConstants.CHANGE_SET_CREATOR_KEY;
import static com.elasticpath.tools.sync.dstmessagelistener.commons.constants.DataTransferExchangeConstants.CHANGE_SET_GUID_KEY;
import static com.elasticpath.tools.sync.dstmessagelistener.commons.constants.DataTransferExchangeConstants.CHANGE_SET_NAME_KEY;
import static com.elasticpath.tools.sync.dstmessagelistener.commons.constants.DataTransferExchangeConstants.CHANGE_SET_PUBLISH_INITIATOR_KEY;
import static java.lang.String.format;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangeProperty;
import org.apache.camel.Handler;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.jsonpath.JsonPath;
import org.apache.camel.spi.DataFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.domain.changeset.ChangeSet;
import com.elasticpath.domain.changeset.ChangeSetStateCode;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.error.handling.policy.impl.RecoverableRetryErrorHandlingPolicy;
import com.elasticpath.tools.sync.dstmessagelistener.changesets.ChangeSetLoader;
import com.elasticpath.tools.sync.dstmessagelistener.changesets.ChangeSetStateUpdater;
import com.elasticpath.tools.sync.dstmessagelistener.messages.DataSyncEventMessageService;

/**
 * An error handling policy that locks the relevant Change Set in the event of a non-recoverable failure.
 *
 * @see RecoverableRetryErrorHandlingPolicy
 */
public class ChangeSetLockingErrorHandlingPolicy extends RecoverableRetryErrorHandlingPolicy {

	private static final Logger LOG = LoggerFactory.getLogger(ChangeSetLockingErrorHandlingPolicy.class);

	private String targetEndPointUri;

	private ChangeSetLoader changeSetLoader;

	private ChangeSetStateUpdater changeSetStateUpdater;

	private DataFormat eventMessageDataFormat;

	private DataSyncEventMessageService dataSyncEventMessageService;

	@Override
	public void configureErrorHandlingPolicy(final RouteBuilder routeBuilder) {
		super.configureErrorHandlingPolicy(routeBuilder);

		routeBuilder.interceptSendToEndpoint(getDeadLetterQueueEndpoint().getEndpointUri())
				.doTry()
					.bean(this, "lockChangeSet")
					.bean(this, "sendErrorEventMessage")
					.marshal(getEventMessageDataFormat())
					.to(getTargetEndPointUri())
				.doCatch(Exception.class)
					.log(LoggingLevel.ERROR, "================== Error encountered while handling original error. ==================")
				.end();

		routeBuilder.getRouteCollection().onCompletion().onFailureOnly()
				.bean(this);
	}

	/**
	 * Locks a Change Set.
	 *
	 * @param changeSetGuid the GUID of the change set to lock
	 */
	@Handler
	public void lockChangeSet(@JsonPath("$.guid") final String changeSetGuid) {
		final ChangeSet changeSet = getChangeSetLoader().load(changeSetGuid);

		if (changeSet.getStateCode() != ChangeSetStateCode.FINALIZED && changeSet.getGuid() != null) {
			if (LOG.isErrorEnabled()) {
				LOG.error(format(
						"Locking Change Set [%s] so that further changes cannot be made until the issue has been resolved.", changeSet.getGuid()));
			}
			getChangeSetStateUpdater().updateState(changeSet, ChangeSetStateCode.LOCKED);
		}
	}

	/**
	 * Publishes an Event Message announcing an error occurring while attempting to publish.
	 * @param exception the exception that triggered the error message
	 * @param changeSetGuid the GUID of the Change Set
	 * @param changeSetName the name of the Change Set
	 * @param changeSetCreatorData the user that created the Change Set
	 * @param changeSetPublishInitiatorData the user that initiated the publish of the Change Set
	 *
	 * @return the result Event Message
	 */
	@Handler
	public EventMessage sendErrorEventMessage(@ExchangeProperty(Exchange.EXCEPTION_CAUGHT)
									  final Exception exception,
									  @ExchangeProperty(CHANGE_SET_GUID_KEY)
									  final String changeSetGuid,
									  @ExchangeProperty(CHANGE_SET_NAME_KEY)
									  final String changeSetName,
									  @ExchangeProperty(CHANGE_SET_CREATOR_KEY)
									  final Map<String, String> changeSetCreatorData,
									  @ExchangeProperty(CHANGE_SET_PUBLISH_INITIATOR_KEY)
									  final Map<String, String> changeSetPublishInitiatorData) {
		return getDataSyncEventMessageService().prepareMessage()
				.withChangeSetGuid(changeSetGuid)
				.withChangeSetName(changeSetName)
				.withChangeSetCreatorData(changeSetCreatorData)
				.withChangeSetPublishInitiator(changeSetPublishInitiatorData)
				.withSuccess(false)
				.build();
	}

	protected ChangeSetLoader getChangeSetLoader() {
		return changeSetLoader;
	}

	public void setChangeSetLoader(final ChangeSetLoader changeSetLoader) {
		this.changeSetLoader = changeSetLoader;
	}

	protected ChangeSetStateUpdater getChangeSetStateUpdater() {
		return changeSetStateUpdater;
	}

	public void setChangeSetStateUpdater(final ChangeSetStateUpdater changeSetStateUpdater) {
		this.changeSetStateUpdater = changeSetStateUpdater;
	}

	protected DataFormat getEventMessageDataFormat() {
		return eventMessageDataFormat;
	}

	public void setEventMessageDataFormat(final DataFormat eventMessageDataFormat) {
		this.eventMessageDataFormat = eventMessageDataFormat;
	}

	protected DataSyncEventMessageService getDataSyncEventMessageService() {
		return dataSyncEventMessageService;
	}

	public void setDataSyncEventMessageService(final DataSyncEventMessageService dataSyncEventMessageService) {
		this.dataSyncEventMessageService = dataSyncEventMessageService;
	}

	protected String getTargetEndPointUri() {
		return targetEndPointUri == null ? "jms:queue:ep.dst" : targetEndPointUri;
	}

	public void setTargetEndPointUri(final String targetEndPointUri) {
		this.targetEndPointUri = targetEndPointUri;
	}
}
