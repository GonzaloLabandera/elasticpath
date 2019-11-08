/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/***
 * Test class for {@link MultiCartResolutionStrategyHolderImpl}.
 */
public class MultiCartResolutionStrategyHolderImplTest {


	private MultiCartResolutionStrategyHolderImpl holder
			= new MultiCartResolutionStrategyHolderImpl();


	@Test
	public void testCanRetreiveStrategy() {
		List<MultiCartResolutionStrategy> strategies = new ArrayList<>();
		holder.setStrategies(strategies);

		assertThat(holder.getStrategies()).isEqualTo(strategies);
	}

	@Test
	public void testCanRetreiveStrategyFromConstructor() {
		List<MultiCartResolutionStrategy> strategies = new ArrayList<>();
		holder = new MultiCartResolutionStrategyHolderImpl(strategies);

		assertThat(holder.getStrategies()).isEqualTo(strategies);
	}
}
