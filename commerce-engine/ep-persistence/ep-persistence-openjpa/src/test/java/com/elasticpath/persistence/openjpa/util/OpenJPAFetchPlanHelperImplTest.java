/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.persistence.openjpa.util;

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Lists;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.jdbc.FetchMode;
import org.apache.openjpa.persistence.jdbc.JDBCFetchPlan;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.persistence.api.LoadTuner;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.persistence.openjpa.routing.HDSSupportBeanImpl;

/**
 * Unit test for the {@code OpenJPAFetchPlanHelperImpl} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class OpenJPAFetchPlanHelperImplTest {

	@InjectMocks private OpenJPAFetchPlanHelperImpl openJPAFetchPlanHelper;

	@Mock private LoadTuner loadTuner;
	@Mock private OpenJPAEntityManager entityManager;
	@Mock private JDBCFetchPlan fetchPlan;

	@Before
	public void init() {
		openJPAFetchPlanHelper.setHdsSupportBean(new HDSSupportBeanImpl());
	}

	/**
	 * Test whether a FetchPlan is configured with load tuners, FetchMode and lazy fields (single class, collection of lazy fields).
	 */
	@Test
	public final void shouldConfigureFetchPlanWithLoadTunersAndFetchModeAndSingleClassWithLazyFields() {
		Collection<String> lazyFields = Lists.newArrayList("spoon", "fork");
		FetchMode fetchMode = FetchMode.JOIN;

		openJPAFetchPlanHelper.setLoadTuners(loadTuner);
		openJPAFetchPlanHelper.setLazyFields(Persistable.class, lazyFields);
		openJPAFetchPlanHelper.setFetchMode(fetchMode);

		when(entityManager.getFetchPlan()).thenReturn(fetchPlan);

		openJPAFetchPlanHelper.configureFetchPlan(entityManager);

		verify(loadTuner).configure(fetchPlan);
		verify(fetchPlan).setEagerFetchMode(fetchMode);
		verify(fetchPlan).setSubclassFetchMode(fetchMode);
		verify(fetchPlan).addFields(Persistable.class, lazyFields);
	}

	/**
	 * Test whether a FetchPlan is configured with lazy fields provided as a map with class <=> field entries.
	 */
	@Test
	public final void shouldConfigureFetchPlanWithMapOfSingleClassAndSingleLazyField() {
		Map<Class<?>, String> lazyFields = new HashMap<>();
		lazyFields.put(Persistable.class, "spoon");

		openJPAFetchPlanHelper.setLazyFields(lazyFields);

		when(entityManager.getFetchPlan()).thenReturn(fetchPlan);

		openJPAFetchPlanHelper.configureFetchPlan(entityManager);

		verify(fetchPlan).addFields(eq(Persistable.class), anyCollection());
	}

	/**
	 * Test whether a FetchPlan is configured with lazy fields provided as a map with class <=> collection of fields entries.
	 */
	@Test
	public final void shouldConfigureFetchPlanWithACollectionOfLazyFieldsAsAMap() {
		Map<Class<?>, Collection<String>> lazyFields = new HashMap<>();
		lazyFields.put(Persistable.class, Lists.newArrayList("spoon", "fork"));

		openJPAFetchPlanHelper.setCollectionOfLazyFields(lazyFields);

		when(entityManager.getFetchPlan()).thenReturn(fetchPlan);

		openJPAFetchPlanHelper.configureFetchPlan(entityManager);

		verify(fetchPlan).addFields(eq(Persistable.class), anyCollection());
	}

	/**
	 * Test whether a FetchPlan is cleared assuming that FETCH_MODE_SETTINGS TL is empty.
	 */
	@Test
	public final void shouldClearFetchPlanWithoutTouchingFetchModeSettings() {

		openJPAFetchPlanHelper.clearFetchPlan(fetchPlan);

		verify(fetchPlan).clearFields();
		verify(fetchPlan).resetFetchGroups();
		verifyNoMoreInteractions(fetchPlan);
	}

	/**
	 * Test whether a FetchPlan is cleared as well as fetch mode settings.
	 */
	@Test
	public final void shouldClearFetchPlanAndResetFetchModeSettings() {
		FetchMode parallelFetchMode = FetchMode.PARALLEL;
		FetchMode fetchMode = FetchMode.JOIN;
		openJPAFetchPlanHelper.setFetchMode(fetchMode);

		when(entityManager.getFetchPlan()).thenReturn(fetchPlan);
		when(fetchPlan.getEagerFetchMode()).thenReturn(parallelFetchMode);
		when(fetchPlan.getSubclassFetchMode()).thenReturn(parallelFetchMode);

		openJPAFetchPlanHelper.configureFetchPlan(entityManager);
		openJPAFetchPlanHelper.clearFetchPlan(fetchPlan);

		verify(fetchPlan).clearFields();
		verify(fetchPlan).resetFetchGroups();
		verify(fetchPlan).setEagerFetchMode(parallelFetchMode);
		verify(fetchPlan).setSubclassFetchMode(parallelFetchMode);
	}

}
