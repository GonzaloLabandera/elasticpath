/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.items.uri.impl;

import com.elasticpath.rest.schema.uri.ItemsUriBuilder;
import com.elasticpath.rest.uri.URIUtil;


/**
 * Creates items Uri.
 *
 * @deprecated remove once dependent resources are converted to Helix.
 */
@Deprecated
public class ItemsUriBuilderImpl implements ItemsUriBuilder {

	private static final String RESOURCE_SERVER_NAME = "items";

	private String scope;
	private String itemId;



	@Override
	public ItemsUriBuilder setScope(final String scope) {
		this.scope = scope;
		return this;
	}

	@Override
	public ItemsUriBuilder setItemId(final String itemId) {
		this.itemId = itemId;
		return this;
	}

	@Override
	public String build() {
		assert scope != null : "scope required.";
		assert itemId != null : "itemId required.";
		return URIUtil.format(RESOURCE_SERVER_NAME, scope, itemId);
	}
}
