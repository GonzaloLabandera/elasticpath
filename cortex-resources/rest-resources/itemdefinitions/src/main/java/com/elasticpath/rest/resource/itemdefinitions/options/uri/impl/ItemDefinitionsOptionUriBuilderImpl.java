/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.options.uri.impl;

import com.elasticpath.rest.resource.dispatch.operator.annotation.Options;
import com.elasticpath.rest.schema.uri.ItemDefinitionsOptionUriBuilder;
import com.elasticpath.rest.uri.URIUtil;

/**
 * Builds an URI for item definitions option.
 *
 * @deprecated remove once dependent resources are converted to Helix.
 */
@Deprecated
public final class ItemDefinitionsOptionUriBuilderImpl implements ItemDefinitionsOptionUriBuilder {

	private static final String RESOURCE_SERVER_NAME = "itemdefinitions";

	private String scope;
	private String itemId;
	private String optionId;

	@Override
	public ItemDefinitionsOptionUriBuilder setScope(final String scope) {
		this.scope = scope;
		return this;
	}

	@Override
	public ItemDefinitionsOptionUriBuilder setItemId(final String itemId) {
		this.itemId = itemId;
		return this;
	}

	@Override
	public ItemDefinitionsOptionUriBuilder setOptionId(final String optionId) {
		this.optionId = optionId;
		return this;
	}

	@Override
	public String build() {
		return URIUtil.format(RESOURCE_SERVER_NAME, scope, itemId, Options.URI_PART, optionId);
	}
}
