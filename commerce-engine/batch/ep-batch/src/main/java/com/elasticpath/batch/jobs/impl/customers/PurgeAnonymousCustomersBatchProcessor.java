/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.batch.jobs.impl.customers;

import static com.elasticpath.persistence.api.PersistenceConstants.LIST_PARAMETER_NAME;

import java.util.List;

import com.elasticpath.batch.jobs.AbstractBatchProcessor;

/**
 * The batch job processor for cleaning expired anonymous customers without orders.
 */
public class PurgeAnonymousCustomersBatchProcessor extends AbstractBatchProcessor<Long> {

	@Override
	protected void executeBulkOperations(final List<Long> batch) {
		List<Long> cartUids =  getPersistenceEngine().retrieveByNamedQueryWithList("SHOPPING_CART_UIDS_BY_CUSTOMER_UIDS",
				LIST_PARAMETER_NAME, batch);

		//must disable parent-child relation before deleting cart items
		getPersistenceEngine().executeNamedQueryWithList("DISABLE_PARENT_CHILD_CART_ITEM_RELATION_BY_SHOPPING_CART_UIDS",
				LIST_PARAMETER_NAME, cartUids);
		//all anonymous customers and relevant entities will be deleted using db cascading deletion
		getPersistenceEngine().executeNamedQueryWithList("DELETE_CUSTOMERS_BY_UIDS", LIST_PARAMETER_NAME, batch);
	}
}
