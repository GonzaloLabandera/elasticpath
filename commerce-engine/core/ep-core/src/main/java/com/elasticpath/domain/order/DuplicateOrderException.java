/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.order;

import static java.util.Collections.emptyList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.base.exception.structured.InvalidBusinessStateException;

/**
 * Thrown when an previously submitted order is submitted again.
 */
public class DuplicateOrderException extends EpServiceException implements InvalidBusinessStateException {
	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	private final List<StructuredErrorMessage> structuredErrorMessages;

	/**
	 * Creates a new <code>DuplicateOrderException</code> object with the given message.
	 *
	 * @param message the reason for this <code>DuplicateOrderException</code>.
	 */
	public DuplicateOrderException(final String message) {
		super(message);
		structuredErrorMessages = Collections.singletonList(new StructuredErrorMessage(null, message, null));
	}

	/**
	 * Creates a new <code>DuplicateOrderException</code> object with the given message.
	 *
	 * @param message                 the reason for this <code>DuplicateOrderException</code>.
	 * @param structuredErrorMessages the detailed reason for this <code>DuplicateOrderException</code>.
	 */
	public DuplicateOrderException(final String message, final Collection<StructuredErrorMessage> structuredErrorMessages) {
		super(message);
		this.structuredErrorMessages = structuredErrorMessages == null ? emptyList() : ImmutableList.copyOf(structuredErrorMessages);
	}

	/**
	 * Creates a new <code>DuplicateOrderException</code> object using the given message and cause exception.
	 *
	 * @param message the reason for this <code>DuplicateOrderException</code>.
	 * @param cause   the <code>Throwable</code> that caused this <code>DuplicateOrderException</code>.
	 */
	public DuplicateOrderException(final String message, final Throwable cause) {
		super(message, cause);
		structuredErrorMessages = Collections.singletonList(new StructuredErrorMessage(null, message, null));
	}

	/**
	 * Creates a new <code>DuplicateOrderException</code> object using the given message and cause exception.
	 *
	 * @param message                 the reason for this <code>DuplicateOrderException</code>.
	 * @param structuredErrorMessages the detailed reason for this <code>DuplicateOrderException</code>.
	 * @param cause                   the <code>Throwable</code> that caused this <code>DuplicateOrderException</code>.
	 */
	public DuplicateOrderException(
			final String message,
			final Collection<StructuredErrorMessage> structuredErrorMessages,
			final Throwable cause) {
		super(message, cause);
		this.structuredErrorMessages = structuredErrorMessages == null ? emptyList() : ImmutableList.copyOf(structuredErrorMessages);
	}

	@Override
	public Collection<StructuredErrorMessage> getStructuredErrorMessages() {
		return structuredErrorMessages;
	}
}
