/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder;

import java.util.List;

/**
 * Holder class for MultiCartResolutionStrategies.
 */
public class MultiCartResolutionStrategyHolderImpl implements MultiCartResolutionStrategyHolder {

	private List<MultiCartResolutionStrategy> strategies;

	/**
	 * Constructor.
	 */
	public MultiCartResolutionStrategyHolderImpl() {
		//no-op
	}

	/**
	 * Constructor.
	 *
	 * @param strategies the strategies.
	 */
	public MultiCartResolutionStrategyHolderImpl(final List<MultiCartResolutionStrategy> strategies) {
		this.strategies = strategies;
	}

	@Override
	public List<MultiCartResolutionStrategy> getStrategies() {
		return strategies;
	}

	@Override
	public void setStrategies(final List<MultiCartResolutionStrategy> strategies) {
		this.strategies = strategies;
	}
}
