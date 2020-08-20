/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.rest.resource.integration.epcommerce.personalization.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.tags.Tag;
import com.elasticpath.tags.service.TagFactory;

/**
 * Test for {@link CustomerSharedIdTagStrategy}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomerSharedIdTagStrategyTest {

	private static final String SHARED_ID = "sharedId";

	@Mock
	private TagFactory tagFactory;

	@InjectMocks
	private CustomerSharedIdTagStrategy classUnderTest;

	@Mock
	private Customer customer;

	@Test
	public void testCustomerSharedIdTagIsReturned() {
		when(customer.getSharedId()).thenReturn(SHARED_ID);
		when(tagFactory.createTagFromTagName(CustomerSharedIdTagStrategy.CUSTOMER_SHARED_ID, SHARED_ID))
			.thenReturn(new Tag(SHARED_ID));

		final Optional<Tag> result = classUnderTest.createTag(customer);

		assertTrue(CustomerSharedIdTagStrategy.CUSTOMER_SHARED_ID + " should be returned", result.isPresent());
		final Tag actual = result.get();
		assertEquals(CustomerSharedIdTagStrategy.CUSTOMER_SHARED_ID + " should be sharedId", SHARED_ID, actual.getValue());
	}
}
