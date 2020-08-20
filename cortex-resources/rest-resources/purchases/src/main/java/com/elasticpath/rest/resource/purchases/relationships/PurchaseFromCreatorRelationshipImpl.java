/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.purchases.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.purchases.PurchaseCreatorIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseFromCreatorRelationship;
import com.elasticpath.rest.definition.purchases.PurchaseIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Creator to purchases link.
 */
public class PurchaseFromCreatorRelationshipImpl implements PurchaseFromCreatorRelationship.LinkTo {

	private final PurchaseCreatorIdentifier purchaseIdentifier;

	/**
	 * Constructor.
	 *
	 * @param identifier identifier
	 */
	@Inject
	public PurchaseFromCreatorRelationshipImpl(@RequestIdentifier final PurchaseCreatorIdentifier identifier) {
		this.purchaseIdentifier = identifier;
	}

	@Override
	public Observable<PurchaseIdentifier> onLinkTo() {
		return Observable.just(purchaseIdentifier.getPurchase());
	}
}
