/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.carts.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.carts.CartIdentifier;
import com.elasticpath.rest.definition.carts.CreateCartFormEntity;
import com.elasticpath.rest.definition.carts.CreateCartFormIdentifier;
import com.elasticpath.rest.definition.carts.CreateCartFormResource;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.helix.data.annotation.RequestForm;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Submit create cart form.
 */
public class SubmitCreateCartPrototypeImpl implements CreateCartFormResource.SubmitWithResult {


	private final CreateCartFormEntity cartEntity;
	private final CreateCartFormIdentifier cartIdentifier;
	private final Repository<CreateCartFormEntity, CartIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param entity         the cart form entity.
	 * @param identifier     the cart form identifier.
	 * @param repository     repository.
	 */
	@Inject
	public SubmitCreateCartPrototypeImpl(@RequestForm final CreateCartFormEntity entity,
										 @RequestIdentifier final CreateCartFormIdentifier identifier,
										 @ResourceRepository final Repository<CreateCartFormEntity, CartIdentifier> repository) {

		this.cartEntity = entity;
		this.cartIdentifier = identifier;
		this.repository = repository;
	}


	@Override
	public Single<SubmitResult<CartIdentifier>> onSubmitWithResult() {
		return repository.submit(cartEntity, cartIdentifier.getCarts().getScope());
	}
}