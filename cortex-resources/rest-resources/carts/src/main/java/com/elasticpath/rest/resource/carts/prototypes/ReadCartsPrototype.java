/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.carts.prototypes;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.carts.CartEntity;
import com.elasticpath.rest.definition.carts.CartIdentifier;
import com.elasticpath.rest.definition.carts.CartsIdentifier;
import com.elasticpath.rest.definition.carts.CartsResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;


/**
 * Read Carts resource prototype.
 */
public class ReadCartsPrototype implements CartsResource.Read {


	private final CartsIdentifier listIdentifier;

	private final Repository<CartEntity, CartIdentifier> repository;
	/**
	 * Constructor.
	 *
	 * @param listIdentifier carIdentifier
	 * @param repository     repository
	 */
	@Inject
	public ReadCartsPrototype(@RequestIdentifier final CartsIdentifier listIdentifier,
							  @ResourceRepository final Repository<CartEntity, CartIdentifier> repository) {
		this.listIdentifier = listIdentifier;
		this.repository = repository;
	}


	@Override
	public Observable<CartIdentifier> onRead() {
		return repository.findAll(listIdentifier.getScope());
	}
}
