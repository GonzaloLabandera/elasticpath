/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.service.shoppingcart.impl;

import com.elasticpath.service.shoppingcart.MulticartItemListTypeLocationProvider;
import com.elasticpath.settings.provider.SettingValueProvider;

/**
 * Multicart Item List Type Location Provider Implementation.
 */
public class MulticartItemListTypeLocationProviderImpl implements MulticartItemListTypeLocationProvider {

	private SettingValueProvider<String> shoppingCartItemListTypeProvider;

	@Override
	public String getMulticartItemListTypeForStore(final String storeCode) {
		return getShoppingCartItemListTypeProvider().get(storeCode);
	}

	private SettingValueProvider<String> getShoppingCartItemListTypeProvider() {
		return shoppingCartItemListTypeProvider;
	}

	public void setShoppingCartItemListTypeProvider(final SettingValueProvider<String> shoppingCartItemListTypeProvider) {
		this.shoppingCartItemListTypeProvider = shoppingCartItemListTypeProvider;
	}
}
