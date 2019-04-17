/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.offers.prototypes;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.offers.OfferComponentsIdentifier;
import com.elasticpath.rest.definition.offers.OfferComponentsResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.id.ResourceIdentifier;

/**
 * Read components for an offer.
 */
public class OfferComponentsPrototype implements OfferComponentsResource.Read {

	private final OfferComponentsIdentifier offerComponentsIdentifier;
	private final LinksRepository<OfferComponentsIdentifier, ResourceIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param offerComponentsIdentifier identifier
	 * @param repository                repository
	 */
	@Inject
	public OfferComponentsPrototype(@RequestIdentifier final OfferComponentsIdentifier offerComponentsIdentifier,
									@ResourceRepository final LinksRepository<OfferComponentsIdentifier, ResourceIdentifier> repository) {
		this.offerComponentsIdentifier = offerComponentsIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<ResourceIdentifier> onRead() {
		return repository.getElements(offerComponentsIdentifier);
	}
}
