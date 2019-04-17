/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.offersearchresults.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.offersearches.OfferSearchFormIdentifier;
import com.elasticpath.rest.definition.offersearches.SearchesToOfferSearchFormRelationship;
import com.elasticpath.rest.definition.searches.SearchesIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Searches resource to OfferSearchForm resource relationship implementation.
 */
public class SearchesToOfferSearchFormRelationshipImpl implements SearchesToOfferSearchFormRelationship.LinkTo {


	private final SearchesIdentifier searchesIdentifier;

	/**
	 * Constructor.
	 *
	 * @param searchesIdentifier SearchesIdentifier
	 */
	@Inject
	public SearchesToOfferSearchFormRelationshipImpl(@RequestIdentifier final SearchesIdentifier searchesIdentifier) {
		this.searchesIdentifier = searchesIdentifier;
	}

	@Override
	public Observable<OfferSearchFormIdentifier> onLinkTo() {
		return Observable.just(OfferSearchFormIdentifier.builder()
				.withScope(searchesIdentifier.getScope())
				.build());
	}
}
