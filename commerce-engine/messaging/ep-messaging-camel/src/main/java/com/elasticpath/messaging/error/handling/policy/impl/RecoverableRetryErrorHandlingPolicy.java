/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.messaging.error.handling.policy.impl;

import org.apache.camel.Endpoint;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RedeliveryPolicyDefinition;
import org.apache.camel.spring.spi.TransactionErrorHandlerBuilder;

import com.elasticpath.messaging.error.handling.policy.ErrorHandlingPolicy;

/**
 * <p>An error handling policy that performs retries for applicable exceptions.</p>
 * <p>Routes configured through this class are wrapped in a transaction.</p>
 * <p>If the exception is identified as a recoverable error, a number of
 * redelivery attempts may be performed depending on the configured redeliveryPolicy.
 * When all redelivery attempts are exhausted, the transaction is rolled back, and
 * the exception is propagated back to activemq to be handled.</p>
 * <p>For all other exceptions, the original message is directly moved to an Invalid
 * Message Channel with no redelivery attempted. In this case, the transaction is not rolled
 * back and the exception is marked as handled.</p>
 */
public class RecoverableRetryErrorHandlingPolicy implements ErrorHandlingPolicy {

	/**
	 * Dead letter queue message definition.
	 */
	private static final String DLQ_ERROR_MESSAGE = " headers: ${headers}, body: ${body},"
			+ " exception: ${exception}, exception.stacktrace: ${exception.stacktrace}";

	/**
	 * Grep the logs for this string to find the non-recoverable errors.
	 */
	private static final String DLQ_ERROR_TEXT = "Routing to DeadLetterQueue.";

	private Endpoint deadLetterQueueEndpoint;

	private RedeliveryPolicyDefinition redeliveryPolicy;

	@SuppressWarnings({"unchecked", "rawtypes"})
	private Class<? extends Throwable>[] recoverableExceptions = new Class[0];

	@Override
	public void configureErrorHandlingPolicy(final RouteBuilder routeBuilder) {
		routeBuilder.errorHandler(new TransactionErrorHandlerBuilder());

		// By default, all exceptions will cause the message to be routed to the Invalid Message Channel.
		routeBuilder.onException(Exception.class)
				.handled(true)
				.useOriginalMessage()
				.log(LoggingLevel.ERROR, DLQ_ERROR_TEXT + DLQ_ERROR_MESSAGE)
				.to(getDeadLetterQueueEndpoint());

		// Overrides the default behaviour for recoverable exceptions only.
		if (getRecoverableExceptions().length > 0) {
			routeBuilder.onException(getRecoverableExceptions())
					.setRedeliveryPolicy(getRedeliveryPolicy());
		}
	}

	protected Endpoint getDeadLetterQueueEndpoint() {
		return deadLetterQueueEndpoint;
	}

	public void setDeadLetterQueueEndpoint(final Endpoint deadLetterQueueEndpoint) {
		this.deadLetterQueueEndpoint = deadLetterQueueEndpoint;
	}

	protected RedeliveryPolicyDefinition getRedeliveryPolicy() {
		return redeliveryPolicy;
	}

	public void setRedeliveryPolicy(final RedeliveryPolicyDefinition redeliveryPolicy) {
		this.redeliveryPolicy = redeliveryPolicy;
	}

	protected Class<? extends Throwable>[] getRecoverableExceptions() {
		return recoverableExceptions.clone();
	}

	/**
	 * Sets recoverable exceptions.
	 *
	 * @param recoverableExceptionsClass recoverableExceptionsClass.
	 */
	public final void setRecoverableExceptions(final Class<?>... recoverableExceptionsClass) {
		@SuppressWarnings("unchecked")
		final Class<Throwable>[] recoverableExceptionsClassArray = (Class<Throwable>[]) recoverableExceptionsClass;
		this.recoverableExceptions = recoverableExceptionsClassArray.clone();
	}

}
