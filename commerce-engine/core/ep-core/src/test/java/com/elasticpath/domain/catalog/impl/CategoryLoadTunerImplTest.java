/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.domain.catalog.impl;

import static com.elasticpath.persistence.support.FetchFieldConstants.ATTRIBUTE_VALUE_MAP;
import static com.elasticpath.persistence.support.FetchFieldConstants.CATEGORY_TYPE;
import static com.elasticpath.persistence.support.FetchFieldConstants.LOCALE_DEPENDANT_FIELDS;
import static com.elasticpath.persistence.support.FetchFieldConstants.MASTER_CATEGORY;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.openjpa.persistence.FetchPlan;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.CategoryLoadTuner;
import com.elasticpath.domain.catalog.CategoryTypeLoadTuner;
import com.elasticpath.domain.impl.ElasticPathImpl;

/**
 * Test class for {@link CategoryLoadTunerImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CategoryLoadTunerImplTest {

	@Mock
	private FetchPlan mockFetchPlan;

	/**
	 * Test method for {@link CategoryLoadTunerImpl#contains(CategoryLoadTuner)}.
	 */
	@Test
	public void testContains() {
		final CategoryLoadTuner loadTuner1 = new CategoryLoadTunerImpl();
		final CategoryLoadTuner loadTuner2 = new CategoryLoadTunerImpl();

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
		loadTuner1.setLoadingCategoryType(true);
		loadTuner1.setLoadingLocaleDependantFields(true);
		loadTuner1.setLoadingMaster(true);

		loadTuner2.setLoadingCategoryType(true);
		loadTuner2.setLoadingLocaleDependantFields(true);
		loadTuner2.setLoadingMaster(true);

		assertTrue(loadTuner1.contains(loadTuner2));
		assertFalse(loadTuner2.contains(loadTuner1));

		// Load tuner 2 has a product type load tuner
		final CategoryTypeLoadTuner categoryTypeLoadTuner = setupCategoryTypeLoadTuner();
		loadTuner2.setCategoryTypeLoadTuner(categoryTypeLoadTuner);
		assertFalse(loadTuner1.contains(loadTuner2));
		assertFalse(loadTuner2.contains(loadTuner1));

		// Load tuner 1 and 2 both have a product type load tuner
		loadTuner1.setCategoryTypeLoadTuner(categoryTypeLoadTuner);
		assertTrue(loadTuner1.contains(loadTuner2));
		assertFalse(loadTuner2.contains(loadTuner1));
	}

	private CategoryTypeLoadTuner setupCategoryTypeLoadTuner() {
		final CategoryTypeLoadTuner loadTuner = new CategoryTypeLoadTunerImpl();
		loadTuner.setLoadingAttributes(true);
		return loadTuner;
	}

	/**
	 * Test method for {@link CategoryLoadTunerImpl#merge(CategoryLoadTuner)}.
	 */
	@Test
	public void testMerge() {
		mockBeanFactory();

		final CategoryLoadTuner loadTuner1 = new CategoryLoadTunerImpl();

		final CategoryLoadTuner loadTuner2 = new CategoryLoadTunerImpl();

		// Merge null doesn't change anything
		loadTuner1.merge(null);
		assertTrue(loadTuner1.contains(loadTuner2));
		assertTrue(loadTuner2.contains(loadTuner1));

		// Load tuner 1 contains 2, we will just return load tuner 1
		loadTuner1.setLoadingAttributeValue(true);
		CategoryLoadTuner loadTuner3 = loadTuner2.merge(loadTuner1);
		assertSame(loadTuner3, loadTuner1);

		// Load tuner 1 and 2 have different flags set
		loadTuner2.setLoadingMaster(true);

		// Merge tuner 1 to tuner 2
		loadTuner3 = loadTuner2.merge(loadTuner1);
		assertTrue(loadTuner3.contains(loadTuner1));
		assertTrue(loadTuner3.contains(loadTuner2));

		// Load tuner 2 has a product type load tuner
		final CategoryTypeLoadTuner categoryTypeLoadTuner = setupCategoryTypeLoadTuner();
		loadTuner2.setCategoryTypeLoadTuner(categoryTypeLoadTuner);

		// Merge load tuner 2 into 1
		loadTuner3 = loadTuner1.merge(loadTuner2);
		assertTrue(loadTuner3.contains(loadTuner1));
		assertTrue(loadTuner3.contains(loadTuner2));
	}

	@Test
	public void shouldConfigureWithLazyFields() {
		final CategoryLoadTuner loadTuner = new CategoryLoadTunerImpl();
		loadTuner.setLoadingAttributeValue(true);
		loadTuner.setLoadingMaster(true);
		loadTuner.setLoadingCategoryType(true);
		loadTuner.setLoadingLocaleDependantFields(true);

		loadTuner.configure(mockFetchPlan);

		verify(mockFetchPlan).addField(CategoryImpl.class, ATTRIBUTE_VALUE_MAP);
		verify(mockFetchPlan).addField(CategoryImpl.class, LOCALE_DEPENDANT_FIELDS);
		verify(mockFetchPlan).addField(LinkedCategoryImpl.class, MASTER_CATEGORY);
		verify(mockFetchPlan).addField(CategoryImpl.class, CATEGORY_TYPE);

	}

	@Test
	public void shouldConfigureWithCategoryTypeLoadTuner() {
		final CategoryLoadTuner loadTuner = new CategoryLoadTunerImpl();
		CategoryTypeLoadTuner mockCategoryTypeLoadTuner = mock(CategoryTypeLoadTuner.class);

		loadTuner.setCategoryTypeLoadTuner(mockCategoryTypeLoadTuner);

		loadTuner.configure(mockFetchPlan);

		verify(mockCategoryTypeLoadTuner).configure(mockFetchPlan);
		verifyZeroInteractions(mockFetchPlan);
	}

	//CategoryTunerLoadTunerImpl#merge calls ElasticPathImpl to obtain a bean. When that one is fixed then @SuppressWarnings can be removed
	@SuppressWarnings("PMD.DontUseElasticPathImplGetInstance")
	private void mockBeanFactory() {
		BeanFactory beanFactory = mock(BeanFactory.class);
		((ElasticPathImpl) ElasticPathImpl.getInstance()).setBeanFactory(beanFactory);

		when(beanFactory.getPrototypeBean(ContextIdNames.CATEGORY_TYPE_LOAD_TUNER, CategoryTypeLoadTuner.class))
				.thenAnswer(invocation -> new CategoryTypeLoadTunerImpl());
	}
}
