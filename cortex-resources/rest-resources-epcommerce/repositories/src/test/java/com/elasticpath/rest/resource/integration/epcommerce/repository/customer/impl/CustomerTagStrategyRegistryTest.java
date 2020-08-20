/**
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer.impl;

import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.junit.Test;

import org.hamcrest.Matchers;
import org.mockito.Mockito;

import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.AccountTagStrategy;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerTagStrategy;

public class CustomerTagStrategyRegistryTest {

	final CustomerTagStrategyRegistry classUnderTest = new CustomerTagStrategyRegistry();

	@Test
	public void testEmptyRegistry() {
		final Collection<CustomerTagStrategy> strategies = classUnderTest.getStrategies();

		assertThat(strategies, Matchers.empty());
	}

	@Test
	public void testEmptyAccountRegistry() {
		final Collection<AccountTagStrategy> strategies = classUnderTest.getAccountStrategies();

		assertThat(strategies, Matchers.empty());
	}

	@Test
	public void testRegisterandUnregisterStrategy() {

		CustomerTagStrategy strategy = Mockito.mock(CustomerTagStrategy.class);

		classUnderTest.loadStrategy(strategy);

		Collection<CustomerTagStrategy> strategies = classUnderTest.getStrategies();

		assertThat(strategies, Matchers.contains(strategy));

		classUnderTest.unloadStrategy(strategy);

		strategies = classUnderTest.getStrategies();

		assertThat(strategies, Matchers.empty());
	}

	@Test
	public void testLoadAndUnloadAccountStrategy() {
		final AccountTagStrategy strategy = Mockito.mock(AccountTagStrategy.class);
		classUnderTest.loadAccountStrategy(strategy);

		final Collection<AccountTagStrategy> strategies = classUnderTest.getAccountStrategies();
		assertThat(strategies, Matchers.contains(strategy));

		classUnderTest.unloadAccountStrategy(strategy);

		final Collection<AccountTagStrategy> strategiesAfterUnloading = classUnderTest.getAccountStrategies();
		assertThat(strategiesAfterUnloading, Matchers.empty());
	}
}