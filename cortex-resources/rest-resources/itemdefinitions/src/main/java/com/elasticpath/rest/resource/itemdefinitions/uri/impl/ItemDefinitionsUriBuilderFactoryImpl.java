/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.uri.impl;

import org.osgi.service.component.annotations.Component;

import com.elasticpath.rest.schema.uri.ItemDefinitionsUriBuilder;
import com.elasticpath.rest.schema.uri.ItemDefinitionsUriBuilderFactory;

/**
 * Factory for {@link com.elasticpath.rest.schema.uri.ItemDefinitionsUriBuilder}s.
 *
 * @deprecated remove once dependent resources are converted to Helix.
 */
@Deprecated
@Component(service = ItemDefinitionsUriBuilderFactory.class)
public final class ItemDefinitionsUriBuilderFactoryImpl implements ItemDefinitionsUriBuilderFactory {

	@Override
	public ItemDefinitionsUriBuilder get() {
		return new ItemDefinitionsUriBuilderImpl();
	}
}
