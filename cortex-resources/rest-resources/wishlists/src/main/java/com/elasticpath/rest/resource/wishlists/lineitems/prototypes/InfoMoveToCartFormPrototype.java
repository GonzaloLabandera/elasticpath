/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.wishlists.lineitems.prototypes;

import io.reactivex.Single;

import com.elasticpath.rest.ResourceInfo;
import com.elasticpath.rest.definition.wishlists.MoveToCartFormResource;
import com.elasticpath.rest.resource.wishlists.constants.WishlistsResourceFamilyConstants;

/**
 * Move to Cart Form prototype for Info operation.
 */
public class InfoMoveToCartFormPrototype implements MoveToCartFormResource.Info {
	private static final Single<ResourceInfo> INFO_SINGLE = Single.just(ResourceInfo.builder()
			.withMaxAge(WishlistsResourceFamilyConstants.MAX_AGE)
			.build());

	@Override
	public Single<ResourceInfo> onInfo() {
		return INFO_SINGLE;
	}
}
