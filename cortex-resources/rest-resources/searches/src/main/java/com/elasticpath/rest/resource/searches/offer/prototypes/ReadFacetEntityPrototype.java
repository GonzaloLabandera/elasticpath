/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.searches.offer.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.searches.FacetEntity;
import com.elasticpath.rest.definition.searches.FacetIdentifier;
import com.elasticpath.rest.definition.searches.FacetResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Read a facet field.
 */
public class ReadFacetEntityPrototype implements FacetResource.Read {

	private final FacetIdentifier facetIdentifier;
	private final Repository<FacetEntity, FacetIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param facetIdentifier facet identifier
	 * @param repository      resource repository
	 */
	@Inject
	public ReadFacetEntityPrototype(@RequestIdentifier final FacetIdentifier facetIdentifier,
									@ResourceRepository final Repository<FacetEntity, FacetIdentifier> repository) {
		this.facetIdentifier = facetIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<FacetEntity> onRead() {
		return repository.findOne(facetIdentifier);
	}
}
