/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.billing.prototypes;

import io.reactivex.Single;

import com.elasticpath.rest.definition.addresses.AccountBillingAddressSelectorResource;
import com.elasticpath.rest.selector.SelectorEntity;

/**
 * Implement a Selector for the Account Billing Address.
 * Implements {@link AccountBillingAddressSelectorResource.Select}.
 */
public class AccountBillingAddressSelectorReadPrototype implements AccountBillingAddressSelectorResource.Select {

	private static final String SELECTOR_NAME = "accountbillingaddress";
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
