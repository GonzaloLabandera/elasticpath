/**
 * Copyright (c) Elastic Path Software Inc., 2012
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.common.dto.StructuredErrorMessage;
import com.elasticpath.commons.exception.InvalidBusinessStateException;

/**
 * {@link EpServiceException} thrown when an invalid {@link com.elasticpath.domain.shipping.ShippingServiceLevel}
 * is set on {@link com.elasticpath.domain.shoppingcart.ShoppingCart}.
 */
public class InvalidShippingServiceLevelException extends EpServiceException implements InvalidBusinessStateException {

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	private final List<StructuredErrorMessage> structuredErrorMessages;

	/**
	 * The constructor.
	 *
	 * @param message the message
	 * @deprecated use {@link #InvalidShippingServiceLevelException(String, Collection <StructuredErrorMessage>)} instead.
	 */
	@Deprecated
	public InvalidShippingServiceLevelException(final String message) {
		super(message);
		structuredErrorMessages = asList(new StructuredErrorMessage(null, message, null));
	}

	/**
	 * The constructor.
	 *
	 * @param message                 the reason for this <code>InvalidShippingServiceLevelException</code>.
	 * @param structuredErrorMessages the detailed reason for this <code>InvalidShippingServiceLevelException</code>.
	 */
	public InvalidShippingServiceLevelException(final String message, final Collection<StructuredErrorMessage> structuredErrorMessages) {
		super(message);
		this.structuredErrorMessages = structuredErrorMessages == null ? emptyList() : ImmutableList.copyOf(structuredErrorMessages);
	}

	/**
	 * Constructor with a throwable.
	 *
	 * @param message   the message
	 * @param throwable the cause
	 * @deprecated use {@link #InvalidShippingServiceLevelException(String, Collection<StructuredErrorMessage>, Throwable)} instead.
	 */
	@Deprecated
	public InvalidShippingServiceLevelException(final String message, final Throwable throwable) {
		super(message, throwable);
		structuredErrorMessages = asList(new StructuredErrorMessage(null, message, null));
	}

	/**
	 * Constructor with a throwable.
	 *
	 * @param message                 the reason for this <code>InvalidShippingServiceLevelException</code>.
	 * @param structuredErrorMessages the detailed reason for this <code>InvalidShippingServiceLevelException</code>.
	 * @param cause                   the <code>Throwable</code> that caused this <code>InvalidShippingServiceLevelException</code>.
	 */
	public InvalidShippingServiceLevelException(
			final String message,
			final Collection<StructuredErrorMessage> structuredErrorMessages,
			final Throwable cause) {
		super(message, cause);
		this.structuredErrorMessages = structuredErrorMessages == null ? emptyList() : ImmutableList.copyOf(structuredErrorMessages);
	}

	@Override
	public List<StructuredErrorMessage> getStructuredErrorMessages() {
		return structuredErrorMessages;
	}

}
