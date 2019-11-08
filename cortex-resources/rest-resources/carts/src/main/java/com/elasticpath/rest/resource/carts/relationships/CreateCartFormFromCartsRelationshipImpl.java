/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.carts.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.carts.CartsIdentifier;
import com.elasticpath.rest.definition.carts.CreateCartFormFromCartsRelationship;
import com.elasticpath.rest.definition.carts.CreateCartFormIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Relationship to create cart form from list of carts.
 */
public class CreateCartFormFromCartsRelationshipImpl implements CreateCartFormFromCartsRelationship.LinkTo {


	private final CartsIdentifier identifier;

	private final LinksRepository<CartsIdentifier, CreateCartFormIdentifier> repository;


	/**
	 *  Constructor.
	 * @param identifier the identifier.
	 * @param repository the repository
	 */
	@Inject
	public CreateCartFormFromCartsRelationshipImpl(@RequestIdentifier final CartsIdentifier identifier,
												   @ResourceRepository final LinksRepository<CartsIdentifier,
															CreateCartFormIdentifier> repository) {

		this.identifier = identifier;
		this.repository = repository;

	}
	@Override
	public Observable<CreateCartFormIdentifier> onLinkTo() {
		return  repository.getElements(identifier);
	}
}
