/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemselections.prototypes;

import io.reactivex.Single;

import com.elasticpath.rest.definition.itemselections.ItemOptionSelectorResource;
import com.elasticpath.rest.selector.SelectorEntity;

/**
 * READ operation on the {@link ItemOptionSelectorResource} for selecting a choice and promoting it to be chosen.
 */
public class ReadItemOptionSelectorPrototype implements ItemOptionSelectorResource.Select {

	private static final String SELECTOR_NAME = "option-value-selector";

	private static final String SELECTION_RULE = "1";

	@Override
	public Single<SelectorEntity> onRead() {
		return Single.just(
				SelectorEntity
						.builder()
						.withName(SELECTOR_NAME)
						.withSelectionRule(SELECTION_RULE)
						.build());
	}
}
