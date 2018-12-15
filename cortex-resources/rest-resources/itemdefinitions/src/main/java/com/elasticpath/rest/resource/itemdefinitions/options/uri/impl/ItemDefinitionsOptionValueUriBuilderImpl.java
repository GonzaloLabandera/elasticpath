/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.options.uri.impl;

import com.elasticpath.rest.resource.dispatch.operator.annotation.Options;
import com.elasticpath.rest.resource.dispatch.operator.annotation.Values;
import com.elasticpath.rest.schema.uri.ItemDefinitionsOptionValueUriBuilder;
import com.elasticpath.rest.uri.URIUtil;

/**
 * Builds an URI for item definitions option value.
 *
 * @deprecated remove once dependent resources are converted to Helix.
 */
@Deprecated
public final class ItemDefinitionsOptionValueUriBuilderImpl implements ItemDefinitionsOptionValueUriBuilder {

	private static final String RESOURCE_SERVER_NAME = "itemdefinitions";

	private String scope;
	private String itemId;
	private String optionId;
	private String valueId;

	@Override
	public ItemDefinitionsOptionValueUriBuilder setScope(final String scope) {
		this.scope = scope;
		return this;
	}

	@Override
	public ItemDefinitionsOptionValueUriBuilder setItemId(final String itemId) {
		this.itemId = itemId;
		return this;
	}

	@Override
	public ItemDefinitionsOptionValueUriBuilder setOptionId(final String optionId) {
		this.optionId = optionId;
		return this;
	}

	@Override
	public ItemDefinitionsOptionValueUriBuilder setValueId(final String valueId) {
		this.valueId = valueId;
		return this;
	}

	@Override
	public String build() {
		return URIUtil.format(RESOURCE_SERVER_NAME, scope, itemId, Options.URI_PART, optionId, Values.URI_PART, valueId);
	}
}
