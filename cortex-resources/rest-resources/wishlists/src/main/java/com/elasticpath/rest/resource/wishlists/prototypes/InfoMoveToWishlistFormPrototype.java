/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.wishlists.prototypes;

import io.reactivex.Single;

import com.elasticpath.rest.ResourceInfo;
import com.elasticpath.rest.definition.wishlists.MoveToWishlistFormResource;
import com.elasticpath.rest.resource.wishlists.constants.WishlistsResourceFamilyConstants;

/**
 * Add Item to Wishlist Form prototype for Info operation.
 */
public class InfoMoveToWishlistFormPrototype implements MoveToWishlistFormResource.Info {

	private static final Single<ResourceInfo> INFO_SINGLE = Single.just(ResourceInfo.builder()
			.withMaxAge(WishlistsResourceFamilyConstants.MAX_AGE)
			.build());

	@Override
	public Single<ResourceInfo> onInfo() {
		return INFO_SINGLE;
	}
}
