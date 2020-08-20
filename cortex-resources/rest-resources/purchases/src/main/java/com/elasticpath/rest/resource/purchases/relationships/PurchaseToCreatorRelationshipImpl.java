/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.purchases.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.purchases.PurchaseCreatorIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseToCreatorRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Purchase to creator link.
 */
public class PurchaseToCreatorRelationshipImpl implements PurchaseToCreatorRelationship.LinkTo {

	private final PurchaseIdentifier purchaseIdentifier;

	/**
	 * Constructor.
	 *
	 * @param purchaseIdentifier purchaseIdentifier
	 */
	@Inject
	public PurchaseToCreatorRelationshipImpl(@RequestIdentifier final PurchaseIdentifier purchaseIdentifier) {
		this.purchaseIdentifier = purchaseIdentifier;
	}

	@Override
	public Observable<PurchaseCreatorIdentifier> onLinkTo() {
		return Observable.just(PurchaseCreatorIdentifier.builder().withPurchase(purchaseIdentifier).build());
	}
}
