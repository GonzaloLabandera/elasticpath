/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.lineitems;

import io.reactivex.Maybe;
import io.reactivex.Observable;

import com.elasticpath.rest.definition.purchases.PurchaseLineItemIdentifier;

/**
 * Repository that provides access to dependent purchase Line Item entities.
 */
public interface DependentPurchaseLineItemRepository {

	/**
	 * Find parent purchase line item of given dependent purchase line item.
	 *
	 * @param dependent the dependent purchase line item identifier.
	 * @return parent purchase line item if exists.
	 */
	Maybe<PurchaseLineItemIdentifier> findParentPurchaseLineItem(PurchaseLineItemIdentifier dependent);

	/**
	 * Find dependent purchase line items of given parent purchase line item if exist.
	 *
	 * @param parent the parent purchase line item identifier.
	 * @return dependent purchase line items if exist.
	 */
	Observable<PurchaseLineItemIdentifier> findDependentPurchaseLineItems(PurchaseLineItemIdentifier parent);

}
