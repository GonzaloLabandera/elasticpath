/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.validators;

import io.reactivex.Completable;

import com.elasticpath.rest.definition.carts.AddItemsToCartFormEntity;

/**
 * Validator for an {@link AddItemsToCartFormEntity}.
 */
public interface AddItemsToCartValidator {

	/**
	 * Validate the AddItemsToCartFormEntity.
	 *
	 * @param addItemsToCartFormEntity addItemsToCartFormEntity
	 * @param scope                    scope
	 * @return Completable
	 */
	Completable validate(AddItemsToCartFormEntity addItemsToCartFormEntity, String scope);
}
