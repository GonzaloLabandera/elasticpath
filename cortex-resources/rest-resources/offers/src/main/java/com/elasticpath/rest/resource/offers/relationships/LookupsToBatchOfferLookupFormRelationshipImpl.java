/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.offers.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.lookups.LookupsIdentifier;
import com.elasticpath.rest.definition.offers.BatchOffersLookupFormIdentifier;
import com.elasticpath.rest.definition.offers.LookupsToBatchOffersLookupFormRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Link from lookups to batch offer lookup.
 */
public class LookupsToBatchOfferLookupFormRelationshipImpl implements LookupsToBatchOffersLookupFormRelationship.LinkTo {

	private final LookupsIdentifier identifier;

	/**
	 * Constructor.
	 * @param identifier identifier
	 */
	@Inject
	public LookupsToBatchOfferLookupFormRelationshipImpl(@RequestIdentifier final LookupsIdentifier identifier) {
		this.identifier = identifier;
	}

	@Override
	public Observable<BatchOffersLookupFormIdentifier> onLinkTo() {
		return Observable.just(BatchOffersLookupFormIdentifier.builder()
				.withScope(identifier.getScope())
				.build());
	}
}
