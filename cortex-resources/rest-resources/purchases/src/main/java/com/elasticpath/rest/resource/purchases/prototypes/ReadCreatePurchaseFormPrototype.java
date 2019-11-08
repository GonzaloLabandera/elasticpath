/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.purchases.prototypes;

import io.reactivex.Single;

import com.elasticpath.rest.definition.purchases.CreatePurchaseFormResource;
import com.elasticpath.rest.definition.purchases.PurchaseFormEntity;

/**
 * Create Purchase Form prototype for Read operation.
 */
public class ReadCreatePurchaseFormPrototype implements CreatePurchaseFormResource.Read {

	@Override
	public Single<PurchaseFormEntity> onRead() {
		return Single.just(PurchaseFormEntity.builder()
				.build());
	}
}
