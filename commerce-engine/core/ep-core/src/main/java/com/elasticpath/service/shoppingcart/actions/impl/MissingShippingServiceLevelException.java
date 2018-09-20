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
 * {@link EpServiceException} thrown when {@link com.elasticpath.domain.shipping.ShippingServiceLevel} is not set
 * on {@link com.elasticpath.domain.shoppingcart.ShoppingCart}.
 */
public class MissingShippingServiceLevelException extends EpServiceException implements InvalidBusinessStateException {

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	private final List<StructuredErrorMessage> structuredErrorMessages;

	/**
	 * The constructor.
	 *
	 * @param message the message
	 * @deprecated use {@link #MissingShippingServiceLevelException(String, Collection <StructuredErrorMessage>)} instead.
	 */
	@Deprecated
	public MissingShippingServiceLevelException(final String message) {
		super(message);
		structuredErrorMessages = asList(new StructuredErrorMessage(null, message, null));
	}

	/**
	 * The constructor.
	 *
	 * @param message                 the reason for this <code>MissingShippingServiceLevelException</code>.
	 * @param structuredErrorMessages the detailed reason for this <code>MissingShippingServiceLevelException</code>.
	 */
	public MissingShippingServiceLevelException(final String message, final Collection<StructuredErrorMessage> structuredErrorMessages) {
		super(message);
		this.structuredErrorMessages = structuredErrorMessages == null ? emptyList() : ImmutableList.copyOf(structuredErrorMessages);
	}


	/**
	 * Constructor with a throwable.
	 *
	 * @param message   the message
	 * @param throwable the cause
	 * @deprecated use {@link #MissingShippingServiceLevelException(String, Collection<StructuredErrorMessage>, Throwable)} instead.
	 */
	@Deprecated
	public MissingShippingServiceLevelException(final String message, final Throwable throwable) {
		super(message, throwable);
		structuredErrorMessages = asList(new StructuredErrorMessage(null, message, null));
	}

	/**
	 * Constructor with a throwable.
	 *
	 * @param message                 the reason for this <code>MissingShippingServiceLevelException</code>.
	 * @param structuredErrorMessages the detailed reason for this <code>MissingShippingServiceLevelException</code>.
	 * @param cause                   the <code>Throwable</code> that caused this <code>MissingShippingServiceLevelException</code>.
	 */
	public MissingShippingServiceLevelException(
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
