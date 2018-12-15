/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.searches.offer.prototypes;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.searches.FacetIdentifier;
import com.elasticpath.rest.definition.searches.FacetsIdentifier;
import com.elasticpath.rest.definition.searches.FacetsResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Read list of facet.
 */
public class ReadFacetsPrototype implements FacetsResource.Read {

	private final FacetsIdentifier facetsIdentifier;
	private final LinksRepository<FacetsIdentifier, FacetIdentifier> repository;

	/**
	 * Constructor.
	 * @param facetsIdentifier identifier
	 * @param repository repository
	 */
	@Inject
	public ReadFacetsPrototype(@RequestIdentifier final FacetsIdentifier facetsIdentifier,
							   @ResourceRepository final LinksRepository<FacetsIdentifier, FacetIdentifier> repository) {
		this.facetsIdentifier = facetsIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<FacetIdentifier> onRead() {
		return repository.getElements(facetsIdentifier);
	}
}
