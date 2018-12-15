/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.searches.offer.prototypes;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.OffersResourceConstants.SELECTION_RULE;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.OffersResourceConstants.SELECTOR_NAME;

import io.reactivex.Single;

import com.elasticpath.rest.definition.searches.FacetSelectorResource;
import com.elasticpath.rest.selector.SelectorEntity;

/**
 * Multi selection facet values.
 */
public class ReadFacetSelectorPrototype implements FacetSelectorResource.Select {

	@Override
	public Single<SelectorEntity> onRead() {
		return Single.just(SelectorEntity.builder()
				.withName(SELECTOR_NAME)
				.withSelectionRule(SELECTION_RULE)
				.build());
	}
}
