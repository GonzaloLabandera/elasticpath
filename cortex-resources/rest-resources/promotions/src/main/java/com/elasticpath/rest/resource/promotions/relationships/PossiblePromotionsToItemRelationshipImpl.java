/*
 *  Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.promotions.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.items.ItemIdentifier;
import com.elasticpath.rest.definition.promotions.PossiblePromotionsForItemIdentifier;
import com.elasticpath.rest.definition.promotions.PossiblePromotionsForItemRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Adds a item link in promotions.
 */
public class PossiblePromotionsToItemRelationshipImpl implements PossiblePromotionsForItemRelationship.LinkFrom {

	private final PossiblePromotionsForItemIdentifier possiblePromotionsForItemIdentifier;

	/**
	 * Constructor.
	 *
	 * @param possiblePromotionsForItemIdentifier	identifier
	 */
	@Inject
	public PossiblePromotionsToItemRelationshipImpl(@RequestIdentifier final PossiblePromotionsForItemIdentifier
																possiblePromotionsForItemIdentifier) {
		this.possiblePromotionsForItemIdentifier = possiblePromotionsForItemIdentifier;
	}

	@Override
	public Observable<ItemIdentifier> onLinkFrom() {
		return Observable.just(possiblePromotionsForItemIdentifier.getItem());
	}
}
