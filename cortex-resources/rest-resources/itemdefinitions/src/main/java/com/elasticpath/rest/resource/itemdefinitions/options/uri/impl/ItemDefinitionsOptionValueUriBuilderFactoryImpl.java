/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.options.uri.impl;

import org.osgi.service.component.annotations.Component;

import com.elasticpath.rest.schema.uri.ItemDefinitionsOptionValueUriBuilder;
import com.elasticpath.rest.schema.uri.ItemDefinitionsOptionValueUriBuilderFactory;

/**
 * Factory for {@link ItemDefinitionsOptionValueUriBuilder}.
 *
 * @deprecated remove once dependent resources are converted to Helix.
 */
@Deprecated
@Component(service = ItemDefinitionsOptionValueUriBuilderFactory.class)
public final class ItemDefinitionsOptionValueUriBuilderFactoryImpl implements ItemDefinitionsOptionValueUriBuilderFactory {

	@Override
	public ItemDefinitionsOptionValueUriBuilder get() {
		return new ItemDefinitionsOptionValueUriBuilderImpl();
	}
}
