/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.domain.catalog.impl;

import static com.elasticpath.persistence.support.FetchFieldConstants.ATTRIBUTE_VALUE_MAP;
import static com.elasticpath.persistence.support.FetchFieldConstants.DIGITAL_ASSET_INTERNAL;
import static com.elasticpath.persistence.support.FetchFieldConstants.OPTION_VALUE_MAP;
import static com.elasticpath.persistence.support.FetchFieldConstants.PRODUCT_INTERNAL;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import org.apache.openjpa.persistence.FetchPlan;

import com.elasticpath.domain.catalog.ProductSkuLoadTuner;

/**
 * Test <code>ProductSkuLoadTunerImpl</code>.
 */
public class ProductSkuLoadTunerImplTest {

	
	/**
	 * Test method for 'com.elasticpath.domain.catalog.impl.ProductSkuLoadTunerImpl.contains(ProductSkuLoadTuner)'.
	 */
	@Test
	public void testContains() {
		final ProductSkuLoadTuner loadTuner1 = new ProductSkuLoadTunerImpl();
		assertTrue(loadTuner1.contains(null));

		final ProductSkuLoadTuner loadTuner2 = new ProductSkuLoadTunerImpl();
		assertTrue(loadTuner1.contains(loadTuner2));
		assertTrue(loadTuner2.contains(loadTuner1));

		loadTuner1.setLoadingAttributeValue(true);
		assertTrue(loadTuner1.contains(loadTuner2));
		assertFalse(loadTuner2.contains(loadTuner1));

		loadTuner1.setLoadingDigitalAsset(true);
		loadTuner1.setLoadingOptionValue(true);
		loadTuner1.setLoadingProduct(true);

		loadTuner2.setLoadingDigitalAsset(true);
		loadTuner2.setLoadingOptionValue(true);
		loadTuner2.setLoadingProduct(true);
		assertTrue(loadTuner1.contains(loadTuner2));
		assertFalse(loadTuner2.contains(loadTuner1));
	}

	/**
	 * Test method for 'com.elasticpath.domain.catalog.impl.ProductSkuLoadTunerImpl.merge(ProductSkuLoadTuner)'.
	 */
	@Test
	public void testMerge() {
		final ProductSkuLoadTuner loadTuner1 = new ProductSkuLoadTunerImpl();
		final ProductSkuLoadTuner loadTuner2 = new ProductSkuLoadTunerImpl();
		assertTrue(loadTuner1.contains(loadTuner2));
		assertTrue(loadTuner2.contains(loadTuner1));

		loadTuner1.setLoadingAttributeValue(true);
		loadTuner1.setLoadingDigitalAsset(true);
		loadTuner1.setLoadingOptionValue(true);
		loadTuner1.setLoadingProduct(true);

		final ProductSkuLoadTuner loadTuner3 = loadTuner2.merge(loadTuner1);
		assertTrue(loadTuner3.contains(loadTuner2));
		assertTrue(loadTuner3.contains(loadTuner1));
	}

	@Test
	public void shouldConfigureWithLazyFields() {
		FetchPlan mockFetchPlan = mock(FetchPlan.class);

		final ProductSkuLoadTuner loadTuner = new ProductSkuLoadTunerImpl();
		loadTuner.setLoadingAttributeValue(true);
		loadTuner.setLoadingOptionValue(true);
		loadTuner.setLoadingProduct(true);
		loadTuner.setLoadingDigitalAsset(true);

		loadTuner.configure(mockFetchPlan);

		verify(mockFetchPlan).addField(ProductSkuImpl.class, ATTRIBUTE_VALUE_MAP);
		verify(mockFetchPlan).addField(ProductSkuImpl.class, OPTION_VALUE_MAP);
		verify(mockFetchPlan).addField(ProductSkuImpl.class, PRODUCT_INTERNAL);
		verify(mockFetchPlan).addField(ProductSkuImpl.class, DIGITAL_ASSET_INTERNAL);
	}
}
