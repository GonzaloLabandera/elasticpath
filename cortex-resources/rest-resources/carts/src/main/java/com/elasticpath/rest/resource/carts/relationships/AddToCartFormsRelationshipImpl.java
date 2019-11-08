/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.carts.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.carts.AddToCartFormsIdentifier;
import com.elasticpath.rest.definition.carts.AddToCartFormsRelationship;
import com.elasticpath.rest.definition.items.ItemIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * link from item to add-to-cart list.
 */
public class AddToCartFormsRelationshipImpl implements AddToCartFormsRelationship.LinkTo {

	private final ItemIdentifier itemIdentifier;
	private final LinksRepository<ItemIdentifier, AddToCartFormsIdentifier> repository;

	/**
	 * Constructor.
	 * @param itemIdentifier the item identifier.
	 * @param repository the repository.
	 */
	@Inject
	public AddToCartFormsRelationshipImpl(@RequestIdentifier final ItemIdentifier itemIdentifier,
										 @ResourceRepository final LinksRepository<ItemIdentifier, AddToCartFormsIdentifier> repository) {

		this.itemIdentifier = itemIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<AddToCartFormsIdentifier> onLinkTo() {
		return repository.getElements(itemIdentifier);

	}
}
