/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.offers.prototypes;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.offers.BatchOffersIdentifier;
import com.elasticpath.rest.definition.offers.BatchOffersResource;
import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Read prototype for batch offers resource.
 */
public class ReadBatchOffersPrototype implements BatchOffersResource.Read {

	private final BatchOffersIdentifier identifier;
	private final LinksRepository<BatchOffersIdentifier, OfferIdentifier> repository;

	/**
	 * Constructor.
	 * @param identifier identifier
	 * @param repository repository
	 */
	@Inject
	public ReadBatchOffersPrototype(@RequestIdentifier final BatchOffersIdentifier identifier,
									@ResourceRepository final LinksRepository<BatchOffersIdentifier, OfferIdentifier> repository) {
		this.identifier = identifier;
		this.repository = repository;
	}

	@Override
	public Observable<OfferIdentifier> onRead() {
		return repository.getElements(identifier);
	}
}
