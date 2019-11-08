/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.offersearchresults.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.offersearches.SortAttributeSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.offersearches.SortAttributeSelectorChoiceSortAttributeSelectorRelationship;
import com.elasticpath.rest.definition.offersearches.SortAttributeSelectorIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Link from choice to selector.
 */
public class SortAttributeSelectorChoiceToSelectorRelationshipImpl implements SortAttributeSelectorChoiceSortAttributeSelectorRelationship.LinkTo {

	private final SortAttributeSelectorChoiceIdentifier identifier;

	/**
	 * Constructor.
	 * @param identifier identifier
	 */
	@Inject
	public SortAttributeSelectorChoiceToSelectorRelationshipImpl(@RequestIdentifier final SortAttributeSelectorChoiceIdentifier identifier) {
		this.identifier = identifier;
	}

	@Override
	public Observable<SortAttributeSelectorIdentifier> onLinkTo() {
		return Observable.just(identifier.getSortAttributeSelector());
	}
}
