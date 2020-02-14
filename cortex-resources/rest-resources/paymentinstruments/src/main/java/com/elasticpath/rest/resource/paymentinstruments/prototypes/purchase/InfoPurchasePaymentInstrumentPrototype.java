/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstruments.prototypes.purchase;

import io.reactivex.Single;

import com.elasticpath.rest.ResourceInfo;
import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentInstrumentResource;
import com.elasticpath.rest.resource.purchases.constants.PurchasesResourceConstants;

/**
 * Purchase Payment Instrument prototype for Info operation.
 */
public class InfoPurchasePaymentInstrumentPrototype implements PurchasePaymentInstrumentResource.Info {

	private static final Single<ResourceInfo> INFO_SINGLE = Single.just(ResourceInfo.builder()
			.withMaxAge(PurchasesResourceConstants.MAX_AGE)
			.build());

	@Override
	public Single<ResourceInfo> onInfo() {
		return INFO_SINGLE;
	}
}
