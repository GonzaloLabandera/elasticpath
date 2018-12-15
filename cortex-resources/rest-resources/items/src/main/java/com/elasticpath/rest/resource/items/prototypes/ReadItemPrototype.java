/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.items.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.items.ItemEntity;
import com.elasticpath.rest.definition.items.ItemIdentifier;
import com.elasticpath.rest.definition.items.ItemResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Item prototype for Read operation.
 */
public class ReadItemPrototype implements ItemResource.Read {

	private final ItemIdentifier itemIdentifier;
	private final Repository<ItemEntity, ItemIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param itemIdentifier itemIdentifier
	 * @param repository     repository
	 */
	@Inject
	public ReadItemPrototype(@RequestIdentifier final ItemIdentifier itemIdentifier,
							 @ResourceRepository final Repository<ItemEntity, ItemIdentifier> repository) {
		this.itemIdentifier = itemIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<ItemEntity> onRead() {
		return repository.findOne(itemIdentifier);
	}
}
