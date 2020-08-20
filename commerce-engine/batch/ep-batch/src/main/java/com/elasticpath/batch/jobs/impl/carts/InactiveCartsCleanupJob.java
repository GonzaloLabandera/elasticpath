/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.batch.jobs.impl.carts;

import com.elasticpath.batch.jobs.AbstractBatchJob;

/**
 * Job to purge inactive shopping carts.
 */
public class InactiveCartsCleanupJob extends AbstractBatchJob<Long> {

	@Override
	protected String getBatchJPQLQuery() {
		return "FIND_INACTIVE_SHOPPING_CART_UIDS";
	}

	@Override
	protected String getJobName() {
		return "Inactive Carts Cleanup";
	}
}
