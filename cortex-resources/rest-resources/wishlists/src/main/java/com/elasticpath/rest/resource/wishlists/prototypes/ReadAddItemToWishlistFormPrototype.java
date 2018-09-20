/*
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.wishlists.prototypes;

import io.reactivex.Single;

import com.elasticpath.rest.definition.wishlists.AddItemToWishlistFormEntity;
import com.elasticpath.rest.definition.wishlists.AddItemToWishlistFormResource;

/**
 * Add item to wishlist form.
 */
public class ReadAddItemToWishlistFormPrototype implements AddItemToWishlistFormResource.Read {

	@Override
	public Single<AddItemToWishlistFormEntity> onRead() {
		return Single.just(AddItemToWishlistFormEntity.builder()
				.build());
	}
}
