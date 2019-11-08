/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.domain.catalog.impl;

import static com.elasticpath.persistence.support.FetchFieldConstants.ATTRIBUTE_VALUE_MAP;
import static com.elasticpath.persistence.support.FetchFieldConstants.LOCALE_DEPENDANT_FIELDS;
import static com.elasticpath.persistence.support.FetchFieldConstants.LOCALIZED_PROPERTIES_MAP;
import static com.elasticpath.persistence.support.FetchFieldConstants.PRODUCT_CATEGORIES;
import static com.elasticpath.persistence.support.FetchFieldConstants.PRODUCT_SKUS_INTERNAL;
import static com.elasticpath.persistence.support.FetchFieldConstants.PRODUCT_TYPE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.openjpa.persistence.FetchPlan;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.domain.catalog.CategoryLoadTuner;
import com.elasticpath.domain.catalog.ProductLoadTuner;
import com.elasticpath.domain.catalog.ProductSkuLoadTuner;
import com.elasticpath.domain.catalog.ProductTypeLoadTuner;
import com.elasticpath.domain.impl.ElasticPathImpl;
import com.elasticpath.persistence.support.FetchGroupConstants;

/**
 * Test <code>ProductLoadTunerImpl</code>.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProductLoadTunerImplTest {

	@Mock
	private FetchPlan mockFetchPlan;

	/**
	 * Test method for 'com.elasticpath.domain.catalog.impl.ProductLoadTunerImpl.contains(ProductLoadTuner)'.
	 */
	@Test
	public void testContains() {
		final ProductLoadTuner loadTuner1 = new ProductLoadTunerImpl();
		final ProductLoadTuner loadTuner2 = new ProductLoadTunerImpl();

		// Always contains a <code>null<code> tuner.
		assertTrue(loadTuner1.contains(null));

		// Empty load tuner contains each other.
		assertTrue(loadTuner1.contains(loadTuner2));
		assertTrue(loadTuner2.contains(loadTuner1));

		// Load tuner 1 has more flags set than load tuner 2
		loadTuner1.setLoadingAttributeValue(true);
		assertTrue(loadTuner1.contains(loadTuner2));
		assertFalse(loadTuner2.contains(loadTuner1));

		// Load tuner 1 has more flags set than load tuner 2
		loadTuner1.setLoadingCategories(true);
		loadTuner1.setLoadingDefaultCategory(true);
		loadTuner1.setLoadingDefaultSku(true);
		loadTuner1.setLoadingProductType(true);
		loadTuner1.setLoadingSkus(true);

		loadTuner2.setLoadingCategories(true);
		loadTuner2.setLoadingDefaultCategory(true);
		loadTuner2.setLoadingDefaultSku(true);
		loadTuner2.setLoadingProductType(true);
		loadTuner2.setLoadingSkus(true);

		assertTrue(loadTuner1.contains(loadTuner2));
		assertFalse(loadTuner2.contains(loadTuner1));

		// Load tuner 2 has a product type load tuner
		final ProductTypeLoadTuner productTypeLoadTuner = setupProductTypeLoadTuner();
		loadTuner2.setProductTypeLoadTuner(productTypeLoadTuner);
		assertFalse(loadTuner1.contains(loadTuner2));
		assertFalse(loadTuner2.contains(loadTuner1));

		// Load tuner 1 and 2 both have a product type load tuner
		loadTuner1.setProductTypeLoadTuner(productTypeLoadTuner);
		assertTrue(loadTuner1.contains(loadTuner2));
		assertFalse(loadTuner2.contains(loadTuner1));
	}

	private ProductTypeLoadTuner setupProductTypeLoadTuner() {
		final ProductTypeLoadTuner loadTuner = new ProductTypeLoadTunerImpl();
		loadTuner.setLoadingAttributes(true);
		loadTuner.setLoadingSkuOptions(true);
		return loadTuner;
	}

	/**
	 * Test method for 'com.elasticpath.domain.catalog.impl.ProductLoadTunerImpl.merge(ProductLoadTuner)'.
	 */
	@Test
	public void testMerge() {

		mockBeanFactory();

		final ProductLoadTuner loadTuner1 = new ProductLoadTunerImpl();
		final ProductLoadTuner loadTuner2 = new ProductLoadTunerImpl();

		// Merge null doesn't change anything
		loadTuner1.merge(null);
		assertTrue(loadTuner1.contains(loadTuner2));
		assertTrue(loadTuner2.contains(loadTuner1));

		// Load tuner 1 contains 2, we will just return load tuner 1
		loadTuner1.setLoadingAttributeValue(true);
		ProductLoadTuner loadTuner3 = loadTuner2.merge(loadTuner1);
		assertSame(loadTuner3, loadTuner1);

		// Load tuner 1 and 2 have different flags set
		loadTuner2.setLoadingCategories(true);

		// Merge tuner 1 to tuner 2
		loadTuner3 = loadTuner2.merge(loadTuner1);
		assertTrue(loadTuner3.contains(loadTuner1));
		assertTrue(loadTuner3.contains(loadTuner2));

		// Load tuner 2 has a product type load tuner
		final ProductTypeLoadTuner productTypeLoadTuner = setupProductTypeLoadTuner();
		loadTuner2.setProductTypeLoadTuner(productTypeLoadTuner);

		// Merge load tuner 2 into 1
		loadTuner3 = loadTuner1.merge(loadTuner2);
		assertTrue(loadTuner3.contains(loadTuner1));
		assertTrue(loadTuner3.contains(loadTuner2));
	}

	@Test
	public void shouldConfigureWithLazyFieldsOnly() {
		final ProductLoadTuner loadTuner = new ProductLoadTunerImpl();

		loadTuner.setLoadingAttributeValue(true);
		loadTuner.setLoadingCategories(true);
		loadTuner.setLoadingProductType(true);
		loadTuner.setLoadingSkus(true);

		loadTuner.configure(mockFetchPlan);

		verify(mockFetchPlan).addField(ProductImpl.class, ATTRIBUTE_VALUE_MAP);
		verify(mockFetchPlan).addField(ProductImpl.class, LOCALE_DEPENDANT_FIELDS);
		verify(mockFetchPlan).addField(BrandImpl.class, LOCALIZED_PROPERTIES_MAP);
		verify(mockFetchPlan).addField(ProductImpl.class, PRODUCT_CATEGORIES);
		verify(mockFetchPlan).addField(ProductImpl.class, PRODUCT_TYPE);
		verify(mockFetchPlan).addField(ProductImpl.class, PRODUCT_SKUS_INTERNAL);
		verify(mockFetchPlan).addFetchGroup(FetchGroupConstants.BUNDLE_CONSTITUENTS);
		verify(mockFetchPlan).addFetchGroup(FetchGroupConstants.PRODUCT_INDEX);
		verify(mockFetchPlan).addFetchGroup(FetchGroupConstants.PRODUCT_SKU_INDEX);
		verify(mockFetchPlan).setMaxFetchDepth(FetchPlan.DEPTH_INFINITE);
	}

	@Test
	public void shouldConfigureWithLazyFieldsAndProvidedLoadTuners() {
		final ProductLoadTuner loadTuner = new ProductLoadTunerImpl();

		CategoryLoadTuner mockCategoryLoadTuner = mock(CategoryLoadTuner.class);
		ProductSkuLoadTuner mockProductSkuLoadTuner = mock(ProductSkuLoadTuner.class);
		ProductTypeLoadTuner mockProductTypeLoadTuner = mock(ProductTypeLoadTuner.class);

		loadTuner.setCategoryLoadTuner(mockCategoryLoadTuner);
		loadTuner.setProductTypeLoadTuner(mockProductTypeLoadTuner);
		loadTuner.setProductSkuLoadTuner(mockProductSkuLoadTuner);
		loadTuner.setLoadingDefaultSku(true);
		loadTuner.setLoadingCategories(true);
		loadTuner.setLoadingProductType(true);
		loadTuner.setLoadingSkus(true);

		loadTuner.configure(mockFetchPlan);

		verify(mockFetchPlan).addField(ProductImpl.class, LOCALE_DEPENDANT_FIELDS);
		verify(mockFetchPlan).addField(BrandImpl.class, LOCALIZED_PROPERTIES_MAP);
		verify(mockFetchPlan).addField(ProductImpl.class, PRODUCT_CATEGORIES);
		verify(mockFetchPlan).addField(ProductImpl.class, PRODUCT_TYPE);
		verify(mockFetchPlan).addField(ProductImpl.class, PRODUCT_SKUS_INTERNAL);
		verify(mockFetchPlan).addFetchGroup(FetchGroupConstants.BUNDLE_CONSTITUENTS);
		verify(mockFetchPlan).addFetchGroup(FetchGroupConstants.PRODUCT_INDEX);
		verify(mockFetchPlan).addFetchGroup(FetchGroupConstants.PRODUCT_SKU_INDEX);
		verify(mockFetchPlan).setMaxFetchDepth(FetchPlan.DEPTH_INFINITE);
		verify(mockCategoryLoadTuner).configure(mockFetchPlan);
		verify(mockProductSkuLoadTuner, times(2)).configure(mockFetchPlan);
		verify(mockProductTypeLoadTuner).configure(mockFetchPlan);
	}

	//CategoryTunerLoadTunerImpl#merge calls ElasticPathImpl to obtain a bean. When that one is fixed then @SuppressWarnings can be removed
	@SuppressWarnings("PMD.DontUseElasticPathImplGetInstance")
	private void mockBeanFactory() {
		BeanFactory beanFactory = mock(BeanFactory.class);
		((ElasticPathImpl) ElasticPathImpl.getInstance()).setBeanFactory(beanFactory);

		when(beanFactory.getBean("productSkuLoadTuner")).thenAnswer(invocation -> new ProductSkuLoadTunerImpl());
		when(beanFactory.getBean("categoryLoadTuner")).thenAnswer(invocation -> new CategoryLoadTunerImpl());
		when(beanFactory.getBean("productTypeLoadTuner")).thenAnswer(invocation -> new ProductTypeLoadTunerImpl());
	}
}
