/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.billing.prototypes;

import io.reactivex.Single;

import com.elasticpath.rest.definition.addresses.BillingAddressSelectorResource;
import com.elasticpath.rest.selector.SelectorEntity;

/**
 * Implement a Selector for the Billing-Address.
 * Implements {@link BillingAddressSelectorResource.Select}.
 */
public class BillingAddressSelectorReadPrototype implements BillingAddressSelectorResource.Select {

	private static final String SELECTOR_NAME = "billingaddress";
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
