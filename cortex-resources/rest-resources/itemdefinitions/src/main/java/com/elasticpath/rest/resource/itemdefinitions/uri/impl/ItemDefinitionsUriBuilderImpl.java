/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.uri.impl;

import com.elasticpath.rest.schema.uri.ItemDefinitionsUriBuilder;
import com.elasticpath.rest.uri.URIUtil;

/**
 * Builds a URI for an item definition.
 *
 * @deprecated remove once dependent resources are converted to Helix.
 */
@Deprecated
public final class ItemDefinitionsUriBuilderImpl implements ItemDefinitionsUriBuilder {

	private static final String RESOURCE_SERVER_NAME = "itemdefinitions";

	private String scope;
	private String itemId;

	@Override
	public ItemDefinitionsUriBuilder setScope(final String scope) {
		this.scope = scope;
		return this;
	}

	@Override
	public ItemDefinitionsUriBuilder setItemId(final String itemId) {
		this.itemId = itemId;
		return this;
	}

	@Override
	public String build() {
		return URIUtil.format(RESOURCE_SERVER_NAME, scope, itemId);
	}
}
