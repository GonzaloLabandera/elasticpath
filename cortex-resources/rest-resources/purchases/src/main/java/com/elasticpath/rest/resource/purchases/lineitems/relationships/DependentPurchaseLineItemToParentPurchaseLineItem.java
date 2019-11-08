/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.purchases.lineitems.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.purchases.DependentPurchaseLineItemToParentPurchaseLineItemRelationship;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.lineitems.DependentPurchaseLineItemRepository;

/**
 * Add link from dependent purchase line item to its parent purchase line item.
 */
public class DependentPurchaseLineItemToParentPurchaseLineItem implements DependentPurchaseLineItemToParentPurchaseLineItemRelationship.LinkTo {

	private final PurchaseLineItemIdentifier dependentPurchaseLineItemIdentifier;
	private final DependentPurchaseLineItemRepository repository;

	/**
	 * Constructor.
	 *
	 * @param dependentPurchaseLineItemIdentifier the dependent purchase line item.
	 * @param repository                          the repository to find the link from dependent purchase line item to its parent.
	 */
	@Inject
	public DependentPurchaseLineItemToParentPurchaseLineItem(
			@RequestIdentifier final PurchaseLineItemIdentifier dependentPurchaseLineItemIdentifier,
			@ResourceRepository final DependentPurchaseLineItemRepository repository) {
		this.dependentPurchaseLineItemIdentifier = dependentPurchaseLineItemIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<PurchaseLineItemIdentifier> onLinkTo() {

		return repository.findParentPurchaseLineItem(dependentPurchaseLineItemIdentifier).toObservable();

	}

}
