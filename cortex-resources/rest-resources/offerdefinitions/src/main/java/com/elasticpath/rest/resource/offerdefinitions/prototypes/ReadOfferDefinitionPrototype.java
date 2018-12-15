/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.offerdefinitions.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.offerdefinitions.OfferDefinitionEntity;
import com.elasticpath.rest.definition.offerdefinitions.OfferDefinitionIdentifier;
import com.elasticpath.rest.definition.offerdefinitions.OfferDefinitionResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Offer Definition prototype for Read operation.
 */
public class ReadOfferDefinitionPrototype implements OfferDefinitionResource.Read {

	private final OfferDefinitionIdentifier offerDefinitionIdentifier;
	private final Repository<OfferDefinitionEntity, OfferDefinitionIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param offerDefinitionIdentifier offerDefinitionIdentifier
	 * @param repository                repository
	 */
	@Inject
	public ReadOfferDefinitionPrototype(@RequestIdentifier final OfferDefinitionIdentifier offerDefinitionIdentifier,
										@ResourceRepository final Repository<OfferDefinitionEntity, OfferDefinitionIdentifier> repository) {
		this.offerDefinitionIdentifier = offerDefinitionIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<OfferDefinitionEntity> onRead() {
		return repository.findOne(offerDefinitionIdentifier);
	}
}
