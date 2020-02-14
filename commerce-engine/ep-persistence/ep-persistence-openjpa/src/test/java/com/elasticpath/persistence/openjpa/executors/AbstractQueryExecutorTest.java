/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.persistence.openjpa.executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Lists;

import org.apache.openjpa.persistence.FetchPlan;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.persistence.openjpa.routing.QueryRouter;
import com.elasticpath.persistence.openjpa.util.FetchPlanHelper;

/**
 * Unit test for the {@code AbstractQueryExecutor} class.
 */
@SuppressWarnings({"serial", "rawtypes"})
@RunWith(MockitoJUnitRunner.class)
public class AbstractQueryExecutorTest {

	private static final String QUERY = "SELECT 1";
	private static final long CUSTOM_ENTITY_UID_PK = 1L;

	private AbstractQueryExecutor abstractQueryExecutor;

	@Mock private QueryRouter queryRouter;
	@Mock private FetchPlanHelper fetchPlanHelper;
	@Mock private EntityManager rwEntityManager;
	@Mock private EntityManager roEntityManager;
	@Mock private FetchPlan roFetchPlan;
	@Mock private FetchPlan rwFetchPlan;
	@Mock private CustomEntityImpl persistableResult;
	@Mock private Query query;

	@Before
	public void init() {
		abstractQueryExecutor = createAbstractQueryExecutor();

	}

	/**
	 * Verify that query, returning a single result, is executed on replica, without retrying on master.
	 */
	@Test
	public void shouldExecuteQueryOnReplicaAndReturnSingleResultWithoutRetryingOnMaster() {
		when(queryRouter.getEntityManagerForQuery(QUERY)).thenReturn(roEntityManager);
		when(fetchPlanHelper.configureFetchPlan(roEntityManager)).thenReturn(roFetchPlan);
		when(queryRouter.shouldRetry(roEntityManager, QUERY, persistableResult)).thenReturn(false);
		when(roEntityManager.find(CustomEntityImpl.class, CUSTOM_ENTITY_UID_PK)).thenReturn(persistableResult);

		Persistable result = abstractQueryExecutor.executeAndReturnSingleResult();

		assertThat(result)
			.isSameAs(persistableResult);

		verifyZeroInteractions(rwEntityManager);
		verify(roEntityManager).find(CustomEntityImpl.class, CUSTOM_ENTITY_UID_PK);
		verify(fetchPlanHelper).clearFetchPlan(null);
		verify(fetchPlanHelper).clearFetchPlan(roFetchPlan);
	}

	/**
	 * Verify that query is retried on master if reading from replica yields null.
	 */
	@Test
	public void shouldExecuteQueryOnReplicaAndReturnSingleResultWithRetryOnMaster() {
		when(queryRouter.getEntityManagerForQuery(QUERY)).thenReturn(roEntityManager);
		when(queryRouter.getReadWriteEntityManager()).thenReturn(rwEntityManager);
		when(fetchPlanHelper.configureFetchPlan(roEntityManager)).thenReturn(roFetchPlan);
		when(fetchPlanHelper.configureFetchPlan(rwEntityManager)).thenReturn(rwFetchPlan);
		when(queryRouter.shouldRetry(roEntityManager, QUERY, null)).thenReturn(true);
		when(roEntityManager.find(CustomEntityImpl.class, CUSTOM_ENTITY_UID_PK)).thenReturn(null);
		when(rwEntityManager.find(CustomEntityImpl.class, CUSTOM_ENTITY_UID_PK)).thenReturn(persistableResult);

		Persistable result = abstractQueryExecutor.executeAndReturnSingleResult();

		assertThat(result)
			.isSameAs(persistableResult);

		verify(roEntityManager).find(CustomEntityImpl.class, CUSTOM_ENTITY_UID_PK);
		verify(rwEntityManager).find(CustomEntityImpl.class, CUSTOM_ENTITY_UID_PK);
		verify(fetchPlanHelper).clearFetchPlan(rwFetchPlan);
		verify(fetchPlanHelper).clearFetchPlan(roFetchPlan);
	}

	/**
	 * Verify that query, returning a list of results, is executed on replica, without retrying on master.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void shouldExecuteQueryOnReplicaAndReturnAListOfResultsWithoutRetryingOnMaster() {

		List<Persistable> expectedResultList = Lists.newArrayList(persistableResult);

		when(queryRouter.getEntityManagerForQuery(QUERY)).thenReturn(roEntityManager);
		when(fetchPlanHelper.configureFetchPlan(roEntityManager)).thenReturn(roFetchPlan);
		when(roEntityManager.createQuery(QUERY)).thenReturn(query);
		when(query.getResultList()).thenReturn(expectedResultList);

		List<Persistable> result = abstractQueryExecutor.executeAndReturnResultList();

		assertThat(result)
			.isSameAs(expectedResultList);

		verifyZeroInteractions(rwEntityManager);
		verify(query).getResultList();
		verify(fetchPlanHelper).clearFetchPlan(null);
		verify(fetchPlanHelper).clearFetchPlan(roFetchPlan);
	}

	/**
	 * Verify that query, returning a list of results, is executed on replica, without retrying on master.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void shouldExecuteQueryOnReplicaAndReturnAListOfResultsWithRetryOnMaster() {

		List<Persistable> expectedResultList = Lists.newArrayList(persistableResult);

		when(queryRouter.getEntityManagerForQuery(QUERY)).thenReturn(roEntityManager);
		when(queryRouter.getReadWriteEntityManager()).thenReturn(rwEntityManager);
		when(fetchPlanHelper.configureFetchPlan(roEntityManager)).thenReturn(roFetchPlan);
		when(queryRouter.shouldRetry(roEntityManager, QUERY, null)).thenReturn(true);
		when(roEntityManager.createQuery(QUERY)).thenReturn(query);
		when(rwEntityManager.createQuery(QUERY)).thenReturn(query);
		when(query.getResultList()).thenReturn(null).thenReturn(expectedResultList);

		List<Persistable> result = abstractQueryExecutor.executeAndReturnResultList();

		assertThat(result)
			.isSameAs(expectedResultList);

		verify(query, times(2)).getResultList();
		verify(fetchPlanHelper).clearFetchPlan(null);
		verify(fetchPlanHelper).clearFetchPlan(roFetchPlan);
	}

	private AbstractQueryExecutor createAbstractQueryExecutor() {

		AbstractQueryExecutor abstractQueryExecutor = new AbstractQueryExecutor() {
			@Override
			protected String getQuery() {
				return QUERY;
			}

			@Override
			protected Persistable executeSingleResultQuery(final EntityManager entityManager) {
				return entityManager.find(CustomEntityImpl.class, CUSTOM_ENTITY_UID_PK);
			}

			@SuppressWarnings("unchecked")
			@Override
			protected List<Persistable> executeMultiResultQuery(final EntityManager entityManager) {
				Query query = entityManager.createQuery(QUERY);
				return query.getResultList();
			}
		};

		abstractQueryExecutor.setFetchPlanHelper(fetchPlanHelper);
		abstractQueryExecutor.setQueryRouter(queryRouter);

		return abstractQueryExecutor;
	}

	private class CustomEntityImpl implements Persistable {
		@Override
		public long getUidPk() {
			return 0;
		}

		@Override
		public void setUidPk(final long uidPk) {
			//do nothing
		}

		@Override
		public boolean isPersisted() {
			return false;
		}
	}
}
