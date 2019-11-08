/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.carts.prototypes;

import java.util.Collections;

import io.reactivex.Single;
import org.apache.commons.lang3.StringUtils;

import com.elasticpath.rest.definition.carts.AddItemsToCartFormEntity;
import com.elasticpath.rest.definition.carts.AddItemsToCartFormResource;
import com.elasticpath.rest.definition.carts.ItemEntity;

/**
 * Add Items to Cart Form prototype for Read operation.
 */
public class ReadAddItemsToCartFormPrototype implements AddItemsToCartFormResource.Read {

	@Override
	public Single<AddItemsToCartFormEntity> onRead() {
		return Single.just(AddItemsToCartFormEntity.builder()
				.withItems(Collections.singletonList(ItemEntity.builder()
						.withCode(StringUtils.EMPTY)
						.withQuantity(1)
						.build()))
				.build());
	}
}
