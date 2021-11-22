/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.batch.jobs.impl.carts;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.batch.jobs.AbstractBatchJob;
import com.elasticpath.domain.shoppingcart.impl.ShoppingCartStatus;

public class AbandonedCartsCleanupJobTest extends AbstractCleanupCartsJobTest<Long> {

	private static final int MAX_DAYS_HISTORY = 61;

	@Autowired
	private AbandonedCartsCleanupJob abandonedCartsCleanupJob;

	@Override
	protected AbstractBatchJob<Long> getBatchJob() {
		return abandonedCartsCleanupJob;
	}

	@Override
	protected ShoppingCartStatus getDesiredInvalidCartStatus() {
		return ShoppingCartStatus.ACTIVE;
	}

	@Override
	protected Date getDesiredInvalidLastModifiedDate() {
		return DateUtils.addDays(new Date(), -MAX_DAYS_HISTORY);
	}

	@Override
	protected ShoppingCartStatus getDesiredValidCartStatus() {
		return ShoppingCartStatus.ACTIVE;
	}

	@Override
	protected Date getDesiredValidLastModifiedDate() {
		return new Date();
	}
}
