/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.shipping.prototypes;

import io.reactivex.Single;

import com.elasticpath.rest.definition.addresses.ShippingAddressSelectorResource;
import com.elasticpath.rest.selector.SelectorEntity;

/**
 * Implement a Selector for the Shipping-Address default selector.
 * Implements {@link ShippingAddressSelectorResource.Select}.
 */
public class ShippingAddressSelectorReadPrototype implements ShippingAddressSelectorResource.Select {

	private static final String SELECTOR_NAME = "shippingaddress";
	private static final String SELECTION_RULE = "1";

	@Override
	public Single<SelectorEntity> onRead() {
		return Single.just(
				SelectorEntity
						.builder()
						.withName(SELECTOR_NAME)
						.withSelectionRule(SELECTION_RULE)
						.build());
	}
}
