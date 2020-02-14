/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstruments.prototypes.purchase;

import io.reactivex.Single;

import com.elasticpath.rest.ResourceInfo;
import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentInstrumentsResource;
import com.elasticpath.rest.resource.purchases.constants.PurchasesResourceConstants;

/**
 * Purchase Payment Instruments prototype for Info operation.
 */
public class InfoPurchasePaymentInstrumentsPrototype implements PurchasePaymentInstrumentsResource.Info {

	private static final Single<ResourceInfo> INFO_SINGLE = Single.just(ResourceInfo.builder()
			.withMaxAge(PurchasesResourceConstants.MAX_AGE)
			.build());

	@Override
	public Single<ResourceInfo> onInfo() {
		return INFO_SINGLE;
	}
}
