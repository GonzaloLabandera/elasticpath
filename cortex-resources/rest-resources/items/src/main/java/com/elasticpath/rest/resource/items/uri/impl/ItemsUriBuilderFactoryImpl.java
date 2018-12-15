/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.items.uri.impl;

import org.osgi.service.component.annotations.Component;

import com.elasticpath.rest.schema.uri.ItemsUriBuilder;
import com.elasticpath.rest.schema.uri.ItemsUriBuilderFactory;

/**
 * Factory for {@link ItemsUriBuilder}.
 *
 * @deprecated remove once dependent resources are converted to Helix.
 */
@Deprecated
@Component(service = ItemsUriBuilderFactory.class)
public final class ItemsUriBuilderFactoryImpl implements ItemsUriBuilderFactory {

	@Override
	public ItemsUriBuilder get() {
		return new ItemsUriBuilderImpl();
	}
}
