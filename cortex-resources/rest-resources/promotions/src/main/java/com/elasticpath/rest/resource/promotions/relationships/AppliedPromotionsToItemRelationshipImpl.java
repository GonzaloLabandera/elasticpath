/*
 *  Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.promotions.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.items.ItemIdentifier;
import com.elasticpath.rest.definition.promotions.AppliedPromotionsForItemIdentifier;
import com.elasticpath.rest.definition.promotions.AppliedPromotionsForItemRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Adds an item link in promotions.
 */
public class AppliedPromotionsToItemRelationshipImpl implements AppliedPromotionsForItemRelationship.LinkFrom {

	private final AppliedPromotionsForItemIdentifier appliedPromotionsForItemIdentifier;

	/**
	 * Constructor.
	 *
	 * @param appliedPromotionsForItemIdentifier	identifier
	 */
	@Inject
	public AppliedPromotionsToItemRelationshipImpl(@RequestIdentifier final AppliedPromotionsForItemIdentifier
															   appliedPromotionsForItemIdentifier) {
		this.appliedPromotionsForItemIdentifier = appliedPromotionsForItemIdentifier;
	}

	@Override
	public Observable<ItemIdentifier> onLinkFrom() {
		return Observable.just(appliedPromotionsForItemIdentifier.getItem());
	}
}
