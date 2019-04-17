/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.offers.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.lookups.LookupsIdentifier;
import com.elasticpath.rest.definition.offers.LookupsToOfferLookupFormRelationship;
import com.elasticpath.rest.definition.offers.OfferLookupFormIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Link from lookups to offerlookupform.
 */
public class LookupsToOfferLookupFormRelationshipImpl implements LookupsToOfferLookupFormRelationship.LinkTo {

	private final LookupsIdentifier lookupsIdentifier;

	/**
	 * Constructor.
	 * @param lookupsIdentifier identifier
	 */
	@Inject
	public LookupsToOfferLookupFormRelationshipImpl(@RequestIdentifier final LookupsIdentifier lookupsIdentifier) {
		this.lookupsIdentifier = lookupsIdentifier;
	}

	@Override
	public Observable<OfferLookupFormIdentifier> onLinkTo() {
		return Observable.just(OfferLookupFormIdentifier.builder()
				.withScope(lookupsIdentifier.getScope())
				.build());
	}
}
