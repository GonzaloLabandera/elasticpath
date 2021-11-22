/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.offer.recommendations.impl;

import java.util.Dictionary;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.service.cm.ConfigurationException;

import com.elasticpath.rest.util.collection.CollectionUtil;

/**
 * Test class for {@link RecommendedOfferPageSizeResolver}.
 */
@RunWith(MockitoJUnitRunner.class)
public class RecommendedOffersPageSizeResolverTest {

	private static final Integer DEFAULT_PAGE_SIZE = 5;
	private static final Integer VALID_PAGE_SIZE = 21;

	private final RecommendedOfferPageSizeResolver classUnderTest = new RecommendedOfferPageSizeResolver();

	@Test
	public void testGetPageSizeSuccessful() throws ConfigurationException {

		updatePropertiesWithValue(VALID_PAGE_SIZE);

		Integer actual = classUnderTest.getPageSize();

		Assert.assertEquals(VALID_PAGE_SIZE, actual);
	}

	@Test(expected = ConfigurationException.class)
	public void testGetPageSizeFailureWhenNumberIsLargerThanRange() throws ConfigurationException {
		updatePropertiesWithValue(Integer.MAX_VALUE);
	}

	@Test(expected = ConfigurationException.class)
	public void testUpdatedFailureWhenNumberIsNegative() throws ConfigurationException {
		updatePropertiesWithValue(-1);
	}

	@Test
	public void testUpdatedFailureWhenNullProperties() throws ConfigurationException {
		classUnderTest.updated(null);
		Integer actual = classUnderTest.getPageSize();

		Assert.assertEquals(DEFAULT_PAGE_SIZE, actual);
	}

	private void updatePropertiesWithValue(final Integer value) throws ConfigurationException {
		Dictionary<String, ?> properties = CollectionUtil.dictionaryOf(
				RecommendedOfferPageSizeResolver.PAGE_SIZE_PROPERTY, value);
		classUnderTest.updated(properties);
	}
}
