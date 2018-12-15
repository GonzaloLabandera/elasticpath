/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.options.uri.impl;

import org.osgi.service.component.annotations.Component;

import com.elasticpath.rest.schema.uri.ItemDefinitionsOptionUriBuilder;
import com.elasticpath.rest.schema.uri.ItemDefinitionsOptionUriBuilderFactory;

/**
 * Factory for {@link ItemDefinitionsOptionUriBuilder}.
 *
 * @deprecated remove once dependent resources are converted to Helix.
 */
@Deprecated
@Component(service = ItemDefinitionsOptionUriBuilderFactory.class)
public final class ItemDefinitionsOptionUriBuilderFactoryImpl implements ItemDefinitionsOptionUriBuilderFactory {

	@Override
	public ItemDefinitionsOptionUriBuilder get() {
		return new ItemDefinitionsOptionUriBuilderImpl();
	}
}
