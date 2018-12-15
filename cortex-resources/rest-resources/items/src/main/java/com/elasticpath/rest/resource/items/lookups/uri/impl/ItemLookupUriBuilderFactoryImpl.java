/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.items.lookups.uri.impl;

import javax.inject.Named;
import javax.inject.Singleton;

import com.elasticpath.rest.schema.uri.ItemLookupUriBuilder;
import com.elasticpath.rest.schema.uri.ItemLookupUriBuilderFactory;

/**
 * A factory for creating ItemLookupUriBuilder objects.
 *
 * @deprecated remove once dependent resources are converted to Helix.
 */
@Deprecated
@Singleton
@Named("itemLookupUriBuilderFactory")
public final class ItemLookupUriBuilderFactoryImpl implements ItemLookupUriBuilderFactory {

	@Override
	public ItemLookupUriBuilder get() {
		return new ItemLookupUriBuilderImpl();
	}

}
