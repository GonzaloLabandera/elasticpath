/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.domain.catalog.impl;

import static com.elasticpath.persistence.support.FetchFieldConstants.CATEGORY_ATTRIBUTE_GROUP_ATTRIBUTES;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.openjpa.persistence.FetchPlan;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.CategoryTypeLoadTuner;

/**
 * Test case for {@link CategoryTypeLoadTunerImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CategoryTypeLoadTunerImplTest {

	/**
	 * Test method for {@link CategoryTypeLoadTunerImpl#contains(CategoryTypeLoadTuner)}.
	 */
	@Test
	public void testContains() {
		final CategoryTypeLoadTuner loadTuner1 = new CategoryTypeLoadTunerImpl();
		assertTrue(loadTuner1.contains(null));

		final CategoryTypeLoadTuner loadTuner2 = new CategoryTypeLoadTunerImpl();
		assertTrue(loadTuner1.contains(loadTuner2));
		assertTrue(loadTuner2.contains(loadTuner1));

		loadTuner1.setLoadingAttributes(true);
		assertTrue(loadTuner1.contains(loadTuner2));
		assertFalse(loadTuner2.contains(loadTuner1));
	}

	/**
	 * Test method for {@link CategoryTypeLoadTunerImpl#merge(CategoryTypeLoadTuner)}.
	 */
	@Test
	public void testMerge() {
		final CategoryTypeLoadTuner loadTuner1 = new CategoryTypeLoadTunerImpl();
		final CategoryTypeLoadTuner loadTuner2 = new CategoryTypeLoadTunerImpl();
		assertTrue(loadTuner1.contains(loadTuner2));
		assertTrue(loadTuner2.contains(loadTuner1));

		loadTuner1.setLoadingAttributes(true);

		final CategoryTypeLoadTuner loadTuner3 = loadTuner2.merge(loadTuner1);
		assertTrue(loadTuner3.contains(loadTuner2));
		assertTrue(loadTuner3.contains(loadTuner1));
	}

	@Test
	public void shouldConfigureWithLazyField() {

		FetchPlan mockFetchPlan = mock(FetchPlan.class);

		final CategoryTypeLoadTuner loadTuner = new CategoryTypeLoadTunerImpl();
		loadTuner.setLoadingAttributes(true);

		loadTuner.configure(mockFetchPlan);

		verify(mockFetchPlan).addField(CategoryTypeImpl.class, CATEGORY_ATTRIBUTE_GROUP_ATTRIBUTES);
	}
}
