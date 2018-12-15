/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.items.lookups.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.items.BatchItemsFormFromLookupsRelationship;
import com.elasticpath.rest.definition.items.BatchItemsFormIdentifier;
import com.elasticpath.rest.definition.lookups.LookupsIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Lookups resource to BatchItemLookupForm resource relationship implementation.
 */
public class LookupsToBatchItemLookupFormRelationshipImpl implements BatchItemsFormFromLookupsRelationship.LinkTo {

	private final LookupsIdentifier lookupsIdentifier;

	/**
	 * Constructor.
	 *
	 * @param lookupsIdentifier lookupsIdentifier
	 */
	@Inject
	public LookupsToBatchItemLookupFormRelationshipImpl(@RequestIdentifier final LookupsIdentifier lookupsIdentifier) {
		this.lookupsIdentifier = lookupsIdentifier;
	}

	@Override
	public Observable<BatchItemsFormIdentifier> onLinkTo() {
		return Observable.just(BatchItemsFormIdentifier.builder()
				.withScope(lookupsIdentifier.getScope())
				.build());
	}
}
