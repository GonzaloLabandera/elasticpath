/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart;

import static java.util.Collections.emptyList;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.exception.InvalidBusinessStateException;

/**
 * Exception thrown when a an attempt to delete an item which is auto selectable bundle item.
 */
public class CantDeleteAutoselectableBundleItemsException extends EpServiceException implements InvalidBusinessStateException {

	private static final long serialVersionUID = 11293344875592593L;

	private final List<StructuredErrorMessage> structuredErrorMessages;


	/**
	 * The constructor.
	 *
	 * @param message                 the reason for this <code>UserStatusInactiveException</code>.
	 * @param structuredErrorMessages the detailed reason for this <code>UserStatusInactiveException</code>.
	 */
	public CantDeleteAutoselectableBundleItemsException(final String message, final Collection<StructuredErrorMessage> structuredErrorMessages) {
		super(message);
		this.structuredErrorMessages = structuredErrorMessages == null ? emptyList() : ImmutableList.copyOf(structuredErrorMessages);
	}


	@Override
	public List<StructuredErrorMessage> getStructuredErrorMessages() {
		return structuredErrorMessages;
	}

}
