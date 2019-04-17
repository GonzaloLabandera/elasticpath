/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.email.routes;

import org.apache.camel.Endpoint;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.spring.SpringRouteBuilder;

import com.elasticpath.core.messaging.changeset.ChangeSetEventType;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.error.handling.policy.ErrorHandlingPolicy;
import com.elasticpath.tools.sync.dstmessagelistener.email.converters.EmailDtoConverter;

/**
 * Route responsible for reading a ChangeSet Event Message and send an EMailDTO to be consumed by the core e-mail route.
 */
public class DatasyncEmailRoute extends SpringRouteBuilder {

	private String routeId;

	private Endpoint sourceEndpoint;

	private Endpoint coreEmailEndpoint;

	private DataFormat eventMessageDataFormat;

	private DataFormat emailDtoDataFormat;

	private EmailDtoConverter emailDtoConverter;

	private ErrorHandlingPolicy errorHandlingPolicy;

	@Override
	public void configure() throws Exception {
		getErrorHandlingPolicy().configureErrorHandlingPolicy(this);

		from(getSourceEndpoint())
				.routeId(getRouteId())
				.unmarshal(getEventMessageDataFormat())
				.filter(exchange ->
						(exchange.getIn().getBody(EventMessage.class).getEventType().equals(ChangeSetEventType.CHANGE_SET_PUBLISH_FAILED)
								|| exchange.getIn().getBody(EventMessage.class).getEventType().equals(ChangeSetEventType.CHANGE_SET_PUBLISHED)))
				.bean(emailDtoConverter)
				.marshal(getEmailDtoDataFormat())
				.to(getCoreEmailEndpoint())
				.end();

	}

	public void setSourceEndpoint(final Endpoint sourceEndpoint) {
		this.sourceEndpoint = sourceEndpoint;
	}

	protected Endpoint getSourceEndpoint() {
		return sourceEndpoint;
	}

	public void setCoreEmailEndpoint(final Endpoint coreEmailEndpoint) {
		this.coreEmailEndpoint = coreEmailEndpoint;
	}

	protected Endpoint getCoreEmailEndpoint() {
		return coreEmailEndpoint;
	}

	protected String getRouteId() {
		return routeId;
	}

	public void setRouteId(final String routeId) {
		this.routeId = routeId;
	}

	protected DataFormat getEventMessageDataFormat() {
		return eventMessageDataFormat;
	}

	public void setEventMessageDataFormat(final DataFormat eventMessageDataFormat) {
		this.eventMessageDataFormat = eventMessageDataFormat;
	}

	protected DataFormat getEmailDtoDataFormat() {
		return emailDtoDataFormat;
	}

	public void setEmailDtoDataFormat(final DataFormat emailDtoDataFormat) {
		this.emailDtoDataFormat = emailDtoDataFormat;
	}

	protected EmailDtoConverter getEmailDtoConverter() {
		return emailDtoConverter;
	}

	public void setEmailDtoConverter(final EmailDtoConverter emailDtoConverter) {
		this.emailDtoConverter = emailDtoConverter;
	}

	protected ErrorHandlingPolicy getErrorHandlingPolicy() {
		return errorHandlingPolicy;
	}

	public void setErrorHandlingPolicy(final ErrorHandlingPolicy errorHandlingPolicy) {
		this.errorHandlingPolicy = errorHandlingPolicy;
	}

}
