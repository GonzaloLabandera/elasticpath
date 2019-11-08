/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.purchases.lineitems.prototypes;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.purchases.DependentPurchaseLineItemsIdentifier;
import com.elasticpath.rest.definition.purchases.DependentPurchaseLineItemsResource;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.lineitems.DependentPurchaseLineItemRepository;

/**
 * Read dependent line items prototype.
 */
public class ReadDependentLineItemsPrototype implements DependentPurchaseLineItemsResource.Read {

	private final DependentPurchaseLineItemsIdentifier dependentPurchaseLineItemsIdentifier;
	private final DependentPurchaseLineItemRepository repository;

	/**
	 * Constructor.
	 *
	 * @param dependentPurchaseLineItemsIdentifier the dependent purchase line items.
	 * @param repository                           the repository to get the dependent purchase line items.
	 */
	@Inject
	public ReadDependentLineItemsPrototype(
			@RequestIdentifier final DependentPurchaseLineItemsIdentifier dependentPurchaseLineItemsIdentifier,
			@ResourceRepository final DependentPurchaseLineItemRepository repository) {

		this.dependentPurchaseLineItemsIdentifier = dependentPurchaseLineItemsIdentifier;
		this.repository = repository;

	}

	@Override
	public Observable<PurchaseLineItemIdentifier> onRead() {

		return repository.findDependentPurchaseLineItems(dependentPurchaseLineItemsIdentifier.getPurchaseLineItem());

	}
}
