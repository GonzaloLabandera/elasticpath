/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.searches.offer.prototypes;

import static com.elasticpath.service.search.solr.FacetConstants.FACET_VALUE_COUNT;
import static com.elasticpath.service.search.solr.FacetConstants.FACET_VALUE_DISPLAY_NAME;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.rest.definition.searches.FacetValueEntity;
import com.elasticpath.rest.definition.searches.FacetValueIdentifier;
import com.elasticpath.rest.definition.searches.FacetValueResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Displays the display name of the facet value and count.
 */
public class ReadFacetValuePrototype implements FacetValueResource.Read {

	private final FacetValueIdentifier facetValueIdentifier;

	/**
	 * Constructor.
	 * @param facetValueIdentifier facet value identifier
	 */
	@Inject
	public ReadFacetValuePrototype(@RequestIdentifier final FacetValueIdentifier facetValueIdentifier) {
		this.facetValueIdentifier = facetValueIdentifier;
	}

	@Override
	public Single<FacetValueEntity> onRead() {
		return Single.just(FacetValueEntity.builder()
				.withCount(facetValueIdentifier.getFacetValueId().getValue().get(FACET_VALUE_COUNT))
				.withValue(facetValueIdentifier.getFacetValueId().getValue().get(FACET_VALUE_DISPLAY_NAME))
				.build());
	}
}
