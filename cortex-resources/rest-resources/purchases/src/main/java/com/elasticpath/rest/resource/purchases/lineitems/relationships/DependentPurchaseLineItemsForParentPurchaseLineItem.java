/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.purchases.lineitems.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.purchases.DependentPurchaseLineItemsForParentPurchaseLineItemRelationship;
import com.elasticpath.rest.definition.purchases.DependentPurchaseLineItemsIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Add dependent purchase line items for its parent purchase line item.
 */
public class DependentPurchaseLineItemsForParentPurchaseLineItem implements DependentPurchaseLineItemsForParentPurchaseLineItemRelationship.LinkTo {

	private final PurchaseLineItemIdentifier parentPurchaseLineItemIdentifier;

	/**
	 * Constructor.
	 *
	 * @param parentPurchaseLineItemIdentifier the parent purchase line item.
	 */
	@Inject
	public DependentPurchaseLineItemsForParentPurchaseLineItem(
			@RequestIdentifier final PurchaseLineItemIdentifier parentPurchaseLineItemIdentifier) {
		this.parentPurchaseLineItemIdentifier = parentPurchaseLineItemIdentifier;
	}

	@Override
	public Observable<DependentPurchaseLineItemsIdentifier> onLinkTo() {

		return Observable.just(DependentPurchaseLineItemsIdentifier.builder()
				.withPurchaseLineItem(parentPurchaseLineItemIdentifier)
				.build());

	}

}
