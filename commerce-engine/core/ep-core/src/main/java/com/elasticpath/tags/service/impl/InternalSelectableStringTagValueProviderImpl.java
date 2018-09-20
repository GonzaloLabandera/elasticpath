/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.tags.service.impl;

/**
 * 
 * Internal retrieve selectable gender values.
 *
 */
public class InternalSelectableStringTagValueProviderImpl extends AbstractInternalSelectableTagValueProvider<String> {

	@Override
	protected String adaptString(final String stringValue) {
		return stringValue;
	}

}
