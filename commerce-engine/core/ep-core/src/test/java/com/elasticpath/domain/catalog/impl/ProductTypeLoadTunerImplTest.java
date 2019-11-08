/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.domain.catalog.impl;

import static com.elasticpath.persistence.support.FetchFieldConstants.CART_ITEM_MODIFIER_GROUPS;
import static com.elasticpath.persistence.support.FetchFieldConstants.OPTION_VALUE_MAP;
import static com.elasticpath.persistence.support.FetchFieldConstants.PRODUCT_ATTRIBUTE_GROUP_ATTRIBUTES;
import static com.elasticpath.persistence.support.FetchFieldConstants.SKU_ATTRIBUTE_GROUP_ATTRIBUTES;
import static com.elasticpath.persistence.support.FetchFieldConstants.SKU_OPTIONS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import org.apache.openjpa.persistence.FetchPlan;

import com.elasticpath.domain.catalog.ProductTypeLoadTuner;

/**
 * Test <code>ProductTypeLoadTunerImpl</code>.
 */
public class ProductTypeLoadTunerImplTest {

	
	/**
	 * Test method for 'com.elasticpath.domain.catalog.impl.ProductTypeLoadTunerImpl.contains(ProductTypeLoadTuner)'.
	 */
	@Test
	public void testContains() {
		final ProductTypeLoadTuner loadTuner1 = new ProductTypeLoadTunerImpl();
		assertTrue(loadTuner1.contains(null));

		final ProductTypeLoadTuner loadTuner2 = new ProductTypeLoadTunerImpl();
		assertTrue(loadTuner1.contains(loadTuner2));
		assertTrue(loadTuner2.contains(loadTuner1));

		loadTuner1.setLoadingAttributes(true);
		assertTrue(loadTuner1.contains(loadTuner2));
		assertFalse(loadTuner2.contains(loadTuner1));

		loadTuner1.setLoadingSkuOptions(true);

		loadTuner2.setLoadingSkuOptions(true);
		assertTrue(loadTuner1.contains(loadTuner2));
		assertFalse(loadTuner2.contains(loadTuner1));
	}

	/**
	 * Test method for 'com.elasticpath.domain.catalog.impl.ProductTypeLoadTunerImpl.merge(ProductTypeLoadTuner)'.
	 */
	@Test
	public void testMerge() {
		final ProductTypeLoadTuner loadTuner1 = new ProductTypeLoadTunerImpl();
		final ProductTypeLoadTuner loadTuner2 = new ProductTypeLoadTunerImpl();
		assertTrue(loadTuner1.contains(loadTuner2));
		assertTrue(loadTuner2.contains(loadTuner1));

		loadTuner1.setLoadingAttributes(true);
		loadTuner1.setLoadingSkuOptions(true);

		final ProductTypeLoadTuner loadTuner3 = loadTuner2.merge(loadTuner1);
		assertTrue(loadTuner3.contains(loadTuner2));
		assertTrue(loadTuner3.contains(loadTuner1));
	}

	@Test
	public void shouldConfigureWithLazyFields() {
		FetchPlan mockFetchPlan = mock(FetchPlan.class);

		final ProductTypeLoadTuner loadTuner = new ProductTypeLoadTunerImpl();
		loadTuner.setLoadingAttributes(true);
		loadTuner.setLoadingSkuOptions(true);
		loadTuner.setLoadingModifierGroups(true);

		loadTuner.configure(mockFetchPlan);

		verify(mockFetchPlan).addField(ProductTypeImpl.class, PRODUCT_ATTRIBUTE_GROUP_ATTRIBUTES);
		verify(mockFetchPlan).addField(ProductTypeImpl.class, SKU_ATTRIBUTE_GROUP_ATTRIBUTES);
		verify(mockFetchPlan).addField(ProductTypeImpl.class, SKU_OPTIONS);
		verify(mockFetchPlan).addField(ProductSkuImpl.class, OPTION_VALUE_MAP);
		verify(mockFetchPlan).addFields(ProductTypeImpl.class, CART_ITEM_MODIFIER_GROUPS);
	}
}
