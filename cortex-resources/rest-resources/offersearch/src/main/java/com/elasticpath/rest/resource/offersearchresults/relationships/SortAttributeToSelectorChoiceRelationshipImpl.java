/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.offersearchresults.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.offersearches.SortAttributeIdentifier;
import com.elasticpath.rest.definition.offersearches.SortAttributeSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.offersearches.SortAttributeToSelectorChoiceRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Link from sort attribute to selector choice.
 */
public class SortAttributeToSelectorChoiceRelationshipImpl implements SortAttributeToSelectorChoiceRelationship.LinkTo {

	private final SortAttributeIdentifier sortAttributeIdentifier;

	/**
	 * Constructor.
	 * @param sortAttributeIdentifier identifier
	 */
	@Inject
	public SortAttributeToSelectorChoiceRelationshipImpl(@RequestIdentifier final SortAttributeIdentifier sortAttributeIdentifier) {
		this.sortAttributeIdentifier = sortAttributeIdentifier;
	}

	@Override
	public Observable<SortAttributeSelectorChoiceIdentifier> onLinkTo() {
		return Observable.just(sortAttributeIdentifier.getSortAttributeSelectorChoice());
	}
}
