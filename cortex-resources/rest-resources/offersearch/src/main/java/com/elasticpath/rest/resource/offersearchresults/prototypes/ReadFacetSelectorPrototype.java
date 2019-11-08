/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.offersearchresults.prototypes;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.OffersResourceConstants.FACET_SELECTOR_NAME;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.OffersResourceConstants.SELECTION_RULE_MANY;

import io.reactivex.Single;

import com.elasticpath.rest.definition.offersearches.FacetSelectorResource;
import com.elasticpath.rest.selector.SelectorEntity;

/**
 * Multi selection facet values.
 */
public class ReadFacetSelectorPrototype implements FacetSelectorResource.Select {

	@Override
	public Single<SelectorEntity> onRead() {
		return Single.just(SelectorEntity.builder()
				.withName(FACET_SELECTOR_NAME)
				.withSelectionRule(SELECTION_RULE_MANY)
				.build());
	}
}
