/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.batch.jobs.impl.carts;

import static com.elasticpath.persistence.api.PersistenceConstants.LIST_PARAMETER_NAME;

import java.util.List;

import com.elasticpath.batch.jobs.AbstractBatchProcessor;

/**
 * The batch job processor for abandoned/inactive carts.
 */
public class PurgeCartsBatchProcessor extends AbstractBatchProcessor<Long> {

	@Override
	protected void executeBulkOperations(final List<Long> batch) {
		//must disable parent-child relation before deleting cart items - no harm is done - these are dead entries anyway
		getPersistenceEngine().executeNamedQueryWithList("DISABLE_PARENT_CHILD_CART_ITEM_RELATION_BY_SHOPPING_CART_UIDS",
				LIST_PARAMETER_NAME, batch);
		//all shopping carts and cart-relevant entities will be deleted using db cascading delete
		getPersistenceEngine().executeNamedQueryWithList("DELETE_SHOPPING_CART_BY_UIDS",
				LIST_PARAMETER_NAME, batch);
	}
}
