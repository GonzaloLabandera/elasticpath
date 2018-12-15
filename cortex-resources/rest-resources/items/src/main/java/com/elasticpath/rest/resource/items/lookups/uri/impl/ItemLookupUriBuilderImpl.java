/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.items.lookups.uri.impl;

import com.elasticpath.rest.resource.dispatch.operator.annotation.Form;
import com.elasticpath.rest.resource.items.lookups.Items;
import com.elasticpath.rest.schema.uri.ItemLookupUriBuilder;
import com.elasticpath.rest.uri.URIUtil;

/**
 * Implementation of the {@link ItemLookupUriBuilder}.
 *
 * @deprecated remove once dependent resources are converted to Helix.
 */
@Deprecated
public final class ItemLookupUriBuilderImpl implements ItemLookupUriBuilder {

	private static final String RESOURCE_SERVER_NAME = "items";

	private String sourceUri;
	private String formPart;
	private String itemsPart;
	private String scope;

	@Override
	public ItemLookupUriBuilder setFormPart() {
		this.sourceUri = null;
		this.itemsPart = Items.PATH_PART;
		this.formPart = Form.PATH_PART;
		return this;
	}

	@Override
	public ItemLookupUriBuilder setItemsPart() {
		this.sourceUri = null;
		this.itemsPart = Items.PATH_PART;
		return this;
	}

	@Override
	public ItemLookupUriBuilder setScope(final String scope) {
		this.sourceUri = null;
		this.scope = scope;
		return this;

	}

	@Override
	public ItemLookupUriBuilder setSourceUri(final String sourceUri) {
		this.formPart = null;
		this.itemsPart = null;
		this.scope = null;
		this.sourceUri = sourceUri;
		return this;
	}


	@Override
	public String build() {
		assert !(scope == null && (formPart != null || itemsPart != null)) : "scope must be set if formPart or itemPart set.";
		return URIUtil.format(RESOURCE_SERVER_NAME, scope, itemsPart, formPart, sourceUri);
	}
}
