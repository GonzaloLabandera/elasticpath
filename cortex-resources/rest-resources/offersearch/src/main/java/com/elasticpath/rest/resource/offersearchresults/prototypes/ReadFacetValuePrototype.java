/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.offersearchresults.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.rest.definition.offersearches.FacetValueEntity;
import com.elasticpath.rest.definition.offersearches.FacetValueIdentifier;
import com.elasticpath.rest.definition.offersearches.FacetValueResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.service.search.solr.FacetConstants;

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
				.withCount(facetValueIdentifier.getFacetValueId().getValue().get(FacetConstants.FACET_VALUE_COUNT))
				.withValue(facetValueIdentifier.getFacetValueId().getValue().get(FacetConstants.FACET_VALUE_DISPLAY_NAME))
				.build());
	}
}
