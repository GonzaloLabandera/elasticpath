/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.carts.prototypes;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.carts.AddToCartFormsIdentifier;
import com.elasticpath.rest.definition.carts.AddToCartFormsResource;
import com.elasticpath.rest.definition.carts.AddToSpecificCartFormIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Read add to cart selector Select prototype.
 */
public class ReadAddToCartFormsImpl implements AddToCartFormsResource.Read {


	private final AddToCartFormsIdentifier identifier;
	private final LinksRepository<AddToCartFormsIdentifier, AddToSpecificCartFormIdentifier> repository;


	/**
	 *  Constructor.
	 * @param identifier the identifier.
	 * @param repository the repository.
	 */
	@Inject
	public ReadAddToCartFormsImpl(@RequestIdentifier final AddToCartFormsIdentifier identifier,
								 @ResourceRepository final LinksRepository<AddToCartFormsIdentifier, AddToSpecificCartFormIdentifier> repository) {
		this.identifier = identifier;
		this.repository = repository;
	}

	/**
	 * @Override
	 *        public Observable<CartDescriptorIdentifier> onRead() {
	 * 		return repository.getElements(identifier);
	 *    }
	 * @return
	 */
	@Override
	public Observable<AddToSpecificCartFormIdentifier> onRead() {
		return repository.getElements(identifier);
	}
}
