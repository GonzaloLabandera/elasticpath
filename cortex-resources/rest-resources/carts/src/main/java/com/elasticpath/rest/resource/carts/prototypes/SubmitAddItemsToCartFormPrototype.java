/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.carts.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.carts.AddItemsToCartFormEntity;
import com.elasticpath.rest.definition.carts.AddItemsToCartFormIdentifier;
import com.elasticpath.rest.definition.carts.AddItemsToCartFormResource;
import com.elasticpath.rest.definition.carts.CartIdentifier;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.helix.data.annotation.RequestForm;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Add Items to Cart Form prototype for Submit operation.
 */
public class SubmitAddItemsToCartFormPrototype implements AddItemsToCartFormResource.SubmitWithResult {

	private final AddItemsToCartFormEntity addItemsToCartFormEntity;

	private final AddItemsToCartFormIdentifier addItemsToCartFormIdentifier;

	private final Repository<AddItemsToCartFormEntity, CartIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param addItemsToCartFormEntity     addItemsToCartFormEntity
	 * @param addItemsToCartFormIdentifier addItemsToCartFormIdentifier
	 * @param repository                   repository
	 */
	@Inject
	public SubmitAddItemsToCartFormPrototype(@RequestForm final AddItemsToCartFormEntity addItemsToCartFormEntity,
											 @RequestIdentifier final AddItemsToCartFormIdentifier addItemsToCartFormIdentifier,
											 @ResourceRepository final Repository<AddItemsToCartFormEntity, CartIdentifier> repository) {
		this.addItemsToCartFormEntity = addItemsToCartFormEntity;
		this.addItemsToCartFormIdentifier = addItemsToCartFormIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<SubmitResult<CartIdentifier>> onSubmitWithResult() {
		return repository.submit(addItemsToCartFormEntity, addItemsToCartFormIdentifier.getCart().getScope());
	}
}
