/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.carts.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.carts.AddToSpecificCartFormIdentifier;
import com.elasticpath.rest.definition.carts.AddToSpecificCartFormResource;
import com.elasticpath.rest.definition.carts.LineItemEntity;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Read add to specific cart form prototype.
 */
public class ReadAddToSpecificCartFormImpl implements AddToSpecificCartFormResource.Read {

	private final AddToSpecificCartFormIdentifier identifier;
	private final Repository<LineItemEntity,
			AddToSpecificCartFormIdentifier>  repository;

	/**
	 * Constructor.
	 *
	 * @param repository modifiersRepository.
	 * @param identifier the identifier.
	 */
	@Inject
	public ReadAddToSpecificCartFormImpl(
											@RequestIdentifier final AddToSpecificCartFormIdentifier identifier,
											 @ResourceRepository final Repository<LineItemEntity,
													 AddToSpecificCartFormIdentifier> repository) {
		this.identifier = identifier;
		this.repository = repository;
	}


	@Override
	public Single<LineItemEntity> onRead() {
			return repository.findOne(identifier);
	}
}
