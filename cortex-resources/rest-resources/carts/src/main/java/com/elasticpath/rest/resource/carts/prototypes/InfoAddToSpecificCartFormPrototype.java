/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.carts.prototypes;

import io.reactivex.Single;

import com.elasticpath.rest.ResourceInfo;
import com.elasticpath.rest.definition.carts.AddToSpecificCartFormResource;
import com.elasticpath.rest.resource.carts.constants.CartsResourceFamilyConstants;

/**
 * Add to Specific Cart Form prototype for Info operation.
 */
public class InfoAddToSpecificCartFormPrototype implements AddToSpecificCartFormResource.Info {

	private static final Single<ResourceInfo> INFO_SINGLE = Single.just(ResourceInfo.builder()
			.withMaxAge(CartsResourceFamilyConstants.MAX_AGE)
			.build());

	@Override
	public Single<ResourceInfo> onInfo() {
		return INFO_SINGLE;
	}
}
