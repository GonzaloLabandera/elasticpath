/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.batch.jobs.impl.carts;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.batch.jobs.AbstractBatchJob;
import com.elasticpath.domain.shoppingcart.impl.ShoppingCartStatus;

public class InactiveCartsCleanupJobTest extends AbstractCleanupCartsJobTest<Long> {

	@Autowired
	private InactiveCartsCleanupJob inactiveCartsCleanupJob;

	@Override
	protected AbstractBatchJob<Long> getBatchJob() {
		return inactiveCartsCleanupJob;
	}

	@Override
	protected ShoppingCartStatus getDesiredInvalidCartStatus() {
		return ShoppingCartStatus.INACTIVE;
	}

	@Override
	protected Date getDesiredInvalidLastModifiedDate() {
		return new Date();
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
