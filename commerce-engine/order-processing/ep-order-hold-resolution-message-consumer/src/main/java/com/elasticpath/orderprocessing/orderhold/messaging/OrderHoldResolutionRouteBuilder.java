/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.orderprocessing.orderhold.messaging;

import org.apache.camel.Endpoint;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.DataFormat;

import com.elasticpath.messaging.EventMessagePredicate;

/**
 * Route to the hold event to be resolved or marked as unresolvable.
 */
public class OrderHoldResolutionRouteBuilder extends RouteBuilder {

	private static final int MAXIMUM_REDELIVERIES = 5;
	private static final String ERROR_ENDPOINT_ID = "ERROR_ENDPOINT_ID";

	private Endpoint incomingEndpoint;

	private String routeId;

	private EventMessagePredicate holdResolutionEventPredicate;

	private DataFormat eventMessageDataFormat;

	private HoldResolutionMessageProcessor holdResolutionMessageProcessor;

	private Endpoint errorEndpoint;

	private HoldResolutionMessageTransformer holdResolutionMessageTransformer;

	private String transactionPropagationRef;

	/**
	 * Fetches hold resolution messages from message broker.
	 */
	@Override
	public void configure() {

		// Captures the UnableToLockOrderException and retries delivery.
		onException(UnableToLockOrderException.class).maximumRedeliveries(MAXIMUM_REDELIVERIES)
				.useOriginalMessage()
				.handled(true)
				.logStackTrace(true)
				.setHeader("exceptionMessage").simple("${exception.message}")
				.setBody().simple("${body}")
				.to(getErrorEndpoint()).id(ERROR_ENDPOINT_ID);

		from(getIncomingEndpoint())
				.routeId(getRouteId())
				.unmarshal(getEventMessageDataFormat())
				.filter().method(getHoldResolutionEventPredicate())
				.bean(getHoldResolutionMessageTransformer(), "transform")
				.bean(getHoldResolutionMessageProcessor(), "process").id("holdResolutionMessageProcessor");

	}

	private Endpoint getIncomingEndpoint() {
		return incomingEndpoint;
	}

	public void setIncomingEndpoint(final Endpoint incomingEndpoint) {
		this.incomingEndpoint = incomingEndpoint;
	}

	private String getRouteId() {
		return routeId;
	}

	public void setRouteId(final String routeId) {
		this.routeId = routeId;
	}

	protected EventMessagePredicate getHoldResolutionEventPredicate() {
		return holdResolutionEventPredicate;
	}

	public void setHoldResolutionEventPredicate(final EventMessagePredicate holdResolutionEventPredicate) {
		this.holdResolutionEventPredicate = holdResolutionEventPredicate;
	}

	private DataFormat getEventMessageDataFormat() {
		return eventMessageDataFormat;
	}

	public void setEventMessageDataFormat(final DataFormat eventMessageDataFormat) {
		this.eventMessageDataFormat = eventMessageDataFormat;
	}

	public void setErrorEndpoint(final Endpoint errorEndpoint) {
		this.errorEndpoint = errorEndpoint;
	}

	private Endpoint getErrorEndpoint() {
		return errorEndpoint;
	}

	public void setHoldResolutionMessageProcessor(final HoldResolutionMessageProcessor holdResolutionMessageProcessor) {
		this.holdResolutionMessageProcessor = holdResolutionMessageProcessor;
	}

	private HoldResolutionMessageProcessor getHoldResolutionMessageProcessor() {
		return this.holdResolutionMessageProcessor;
	}

	public void setHoldResolutionMessageTransformer(final HoldResolutionMessageTransformer holdResolutionMessageTransformer) {
		this.holdResolutionMessageTransformer = holdResolutionMessageTransformer;
	}

	public HoldResolutionMessageTransformer getHoldResolutionMessageTransformer() {
		return this.holdResolutionMessageTransformer;
	}

	protected String getTransactionPropagationRef() {
		return transactionPropagationRef;
	}

	public void setTransactionPropagationRef(final String transactionPropagationRef) {
		this.transactionPropagationRef = transactionPropagationRef;
	}
}
