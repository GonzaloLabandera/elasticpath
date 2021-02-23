/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.batch.jobs.impl.orders;

import static com.elasticpath.persistence.api.PersistenceConstants.LIST_PARAMETER_NAME;

import java.util.List;

import com.elasticpath.batch.jobs.AbstractBatchProcessor;

/**
 * The batch job processor for cleaning expired anonymous customers without orders.
 */
public class PurgeExpiredFailedOrdersBatchProcessor extends AbstractBatchProcessor<Long> {

	@Override
	protected void executeBulkOperations(final List<Long> batch) {
		/*
		 	Bundle orderSKUs (roots/constituents) do not have ORDER_SHIPMENT_UID set, which renders db cascade deletion inoperable.
		 	This field was intentionally left empty for bundles and shouldn't be set ever.

		 	To make cascade deletion possible, the ORDER_UID field is added to TORDERSKU table.
		 	However, because of bundle (tree) structure and constituents' dependency on order shipment, the dependency must be disabled first.
		 	The records with disabled (null) dependency are dead anyway, no harm is done.
		 */
		getPersistenceEngine().executeNamedQueryWithList("DISABLE_DEPENDENCY_ORDER_SKU_ON_SHIPMENT", LIST_PARAMETER_NAME, batch);
		getPersistenceEngine().executeNamedQueryWithList("DELETE_ORDERS_BY_UIDS", LIST_PARAMETER_NAME, batch);
	}
}
