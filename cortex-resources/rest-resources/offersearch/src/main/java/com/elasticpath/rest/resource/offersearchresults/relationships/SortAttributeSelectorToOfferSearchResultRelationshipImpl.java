/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.offersearchresults.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.offersearches.OfferSearchResultIdentifier;
import com.elasticpath.rest.definition.offersearches.OfferSearchResultToSortAttributeSelectorRelationship;
import com.elasticpath.rest.definition.offersearches.SortAttributeSelectorIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Link from sort attribute selector to offer search result.
 */
public class SortAttributeSelectorToOfferSearchResultRelationshipImpl implements OfferSearchResultToSortAttributeSelectorRelationship.LinkFrom {

	private final SortAttributeSelectorIdentifier identifier;

	/**
	 * Constructor.
	 * @param identifier identifier
	 */
	@Inject
	public SortAttributeSelectorToOfferSearchResultRelationshipImpl(@RequestIdentifier final SortAttributeSelectorIdentifier identifier) {
		this.identifier = identifier;
	}

	@Override
	public Observable<OfferSearchResultIdentifier> onLinkFrom() {
		return Observable.just(identifier.getOfferSearchResult());
	}
}
