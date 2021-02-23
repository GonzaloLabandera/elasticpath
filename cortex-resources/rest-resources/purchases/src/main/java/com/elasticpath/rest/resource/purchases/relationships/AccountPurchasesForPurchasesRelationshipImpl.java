/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.purchases.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.purchases.AccountPurchasesForPurchasesRelationship;
import com.elasticpath.rest.definition.purchases.PaginatedAccountPurchasesIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Account purchase to account purchases link.
 */
public class AccountPurchasesForPurchasesRelationshipImpl implements AccountPurchasesForPurchasesRelationship.LinkTo {

	private final PurchaseIdentifier purchaseIdentifier;
	private final LinksRepository<PurchaseIdentifier, PaginatedAccountPurchasesIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param purchaseIdentifier the purchase identifier
	 * @param repository         the paginated repository
	 */
	@Inject
	public AccountPurchasesForPurchasesRelationshipImpl(@RequestIdentifier final PurchaseIdentifier purchaseIdentifier,
														@ResourceRepository final LinksRepository<PurchaseIdentifier,
																PaginatedAccountPurchasesIdentifier> repository) {
		this.purchaseIdentifier = purchaseIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<PaginatedAccountPurchasesIdentifier> onLinkTo() {
		return repository.getElements(purchaseIdentifier);
	}
}
