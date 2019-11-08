/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.offersearchresults.prototypes;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.OffersResourceConstants.SELECTION_RULE_ONE;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.OffersResourceConstants.SORT_SELECTOR_NAME;

import io.reactivex.Single;

import com.elasticpath.rest.definition.offersearches.SortAttributeSelectorResource;
import com.elasticpath.rest.selector.SelectorEntity;

/**
 * Read attribute selector.
 */
public class ReadSortAttributeSelectorPrototype implements SortAttributeSelectorResource.Select {

	@Override
	public Single<SelectorEntity> onRead() {
		return Single.just(SelectorEntity.builder()
				.withName(SORT_SELECTOR_NAME)
				.withSelectionRule(SELECTION_RULE_ONE)
				.build());
	}
}
