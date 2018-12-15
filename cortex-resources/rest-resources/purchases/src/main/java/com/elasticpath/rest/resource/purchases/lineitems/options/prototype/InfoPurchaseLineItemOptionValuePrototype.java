/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.purchases.lineitems.options.prototype;

import io.reactivex.Single;

import com.elasticpath.rest.ResourceInfo;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemOptionValueResource;
import com.elasticpath.rest.resource.purchases.constants.PurchasesResourceConstants;

/**
 * Purchase Line Item Option Value prototype for Info operation.
 */
public class InfoPurchaseLineItemOptionValuePrototype implements PurchaseLineItemOptionValueResource.Info {

	private static final Single<ResourceInfo> INFO_SINGLE = Single.just(ResourceInfo.builder()
			.withMaxAge(PurchasesResourceConstants.MAX_AGE)
			.build());

	@Override
	public Single<ResourceInfo> onInfo() {
		return INFO_SINGLE;
	}
}
