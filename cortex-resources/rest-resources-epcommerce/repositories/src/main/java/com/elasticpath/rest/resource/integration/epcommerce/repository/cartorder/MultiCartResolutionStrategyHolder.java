/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder;

import java.util.List;

/**
 * Holder for multi cart resolution strategies.
 */
public interface MultiCartResolutionStrategyHolder {


	/**
	 * Gets the strategies.
	 * @return the strategies.
	 */
	List<MultiCartResolutionStrategy> getStrategies();

	/**
	 * Sets the strategies.
	 * @param strategies the strategies.
	 */
	void setStrategies(List<MultiCartResolutionStrategy> strategies);
}
