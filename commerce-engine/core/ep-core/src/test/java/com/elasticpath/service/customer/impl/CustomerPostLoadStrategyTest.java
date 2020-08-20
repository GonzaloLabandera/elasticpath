/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.service.customer.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;

import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.domain.customer.impl.CustomerGroupImpl;
import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.service.attribute.AttributeService;
import com.elasticpath.test.factory.TestCustomerProfileFactory;

public class CustomerPostLoadStrategyTest {
	@Rule
	public JUnitRuleMockery context = new JUnitRuleMockery();

	private final AttributeService attributeService = context.mock(AttributeService.class);
	private CustomerPostLoadStrategy strategy;

	@Before
	public void setUp() {
		context.checking(new Expectations() {
			{
				allowing(attributeService).getCustomerProfileAttributesMapByCustomerType(CustomerType.REGISTERED_USER);
				will(returnValue(new TestCustomerProfileFactory().getProfile()));
			}
		});

		strategy = new CustomerPostLoadStrategy();
		strategy.setAttributeService(attributeService);
	}

	@Test
	public void testCanProcessWithCustomersReturnsTrue() {
		assertTrue(strategy.canProcess(new CustomerImpl()));
	}

	@Test
	public void testThatCanProcessWithNonCustomersReturnsFalse() {
		assertFalse(strategy.canProcess(new CustomerGroupImpl()));
	}

	@Test(expected = RuntimeException.class)
	public void sanityCheckThatNotLoadingCustomerProfileMetadataOntoCustomersIsBad() {
		CustomerImpl customer = new CustomerImpl();
		customer.setFirstName("Foo");

		fail("The setter should have thrown an exception because the customer profile attributes have not been initialized");
	}

	@Test
	public void testThatProcessLoadsCustomerProfileMetadataOntoCustomers() {
		CustomerImpl customer = new CustomerImpl();
		customer.setCustomerType(CustomerType.REGISTERED_USER);
		strategy.process(customer);

		customer.setFirstName("Foo");
		assertEquals("After processing, customer attribute values should be settable",
				"Foo", customer.getFirstName());
	}
}
