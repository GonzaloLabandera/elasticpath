/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.persistence.openjpa.routing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Lists;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.persistence.openjpa.util.QueryRouterMetaInfoHolder;

/**
 * Unit test for the {@code QueryRouter} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class QueryRouterTest {

	private static final String NAMED_QUERY = "NAMED_QUERY";

	@InjectMocks private QueryRouter queryRouter;

	@Mock private EntityManager readWriteEntityManager;
	@Mock private EntityManager readOnlyEntityManager;
	@Mock private HDSSupportBean hdsSupportBean;
	@Mock private QueryRouterMetaInfoHolder queryRouterMetaInfoHolder;


	/**
	 * Test that required data are prepared for query routing when HDS feature is ON.
	 */
	@Test
	public void shouldInitMetaInfoStructureWhenHDSIsOn() {
		when(hdsSupportBean.isHdsSupportEnabled()).thenReturn(true);

		queryRouter.init();

		verify(queryRouterMetaInfoHolder).initFromRWEntityManager(readWriteEntityManager);
		verifyZeroInteractions(readOnlyEntityManager);
		verifyNoMoreInteractions(queryRouterMetaInfoHolder, readWriteEntityManager);
	}

	/**
	 * Test that nothing happens when HDS feature is OFF.
	 */
	@Test
	public void shouldNotInitMetaInfoStructureWhenHDSIsOff() {
		when(hdsSupportBean.isHdsSupportEnabled()).thenReturn(false);

		queryRouter.init();

		verifyZeroInteractions(queryRouterMetaInfoHolder, readWriteEntityManager, readOnlyEntityManager);
	}

	/**
	 * Test that read-only entity manager is returned when HDS feature is ON, no active transaction and
	 * query is safe for replica.
	 */
	@Test
	public void shouldReturnReadOnlyEntityManagerWhenHDSIsONAndNoActiveTransactionAndQueryIsSafeForReplica() {
		when(hdsSupportBean.isHdsSupportEnabled()).thenReturn(true);
		when(queryRouterMetaInfoHolder.isQuerySafeForReadingFromReplica(any())).thenReturn(true);

		EntityManager actualEntityManager = queryRouter.getEntityManagerForQuery(NAMED_QUERY);

		assertThat(actualEntityManager)
			.isSameAs(readOnlyEntityManager);

		verify(hdsSupportBean).isHdsSupportEnabled();
		verify(queryRouterMetaInfoHolder).isQuerySafeForReadingFromReplica(NAMED_QUERY);
	}

	/**
	 * Test that read-write entity manager is returned when HDS feature is ON, no active transaction and
	 * query is not safe for replica.
	 */
	@Test
	public void shouldReturnReadWriteEntityManagerWhenHDSIsONAndNoActiveTransactionAndQueryIsNotSafeForReplica() {
		when(hdsSupportBean.isHdsSupportEnabled()).thenReturn(true);
		when(queryRouterMetaInfoHolder.isQuerySafeForReadingFromReplica(any())).thenReturn(false);

		EntityManager actualEntityManager = queryRouter.getEntityManagerForQuery(NAMED_QUERY);

		assertThat(actualEntityManager)
			.isSameAs(readWriteEntityManager);

		verify(hdsSupportBean).isHdsSupportEnabled();
		verify(queryRouterMetaInfoHolder).isQuerySafeForReadingFromReplica(NAMED_QUERY);
	}

	/**
	 * Test that query shouldn't be retried on master if HDS is ON, entity manager is read-only one, query is not safe for replica, query is
	 * retriable and result is not null (when a single entity is returned).
	 */
	@Test
	public void shouldNotRetryOnMasterWhenHDSIsONAndEntityManagerIsReadOnlyAndQueryIsNotSafeForReplicaAndQueryIsRetriableAndResultIsNotNull() {
		Persistable result = mock(Persistable.class);

		when(hdsSupportBean.isHdsSupportEnabled()).thenReturn(true);
		when(hdsSupportBean.isQuerySafeForReplica()).thenReturn(false);
		when(queryRouterMetaInfoHolder.isQueryRetriable(any())).thenReturn(true);

		boolean shouldRetry = queryRouter.shouldRetry(readOnlyEntityManager, NAMED_QUERY, result);

		assertThat(shouldRetry)
			.isFalse();

		verify(hdsSupportBean).isHdsSupportEnabled();
		verify(hdsSupportBean).isQuerySafeForReplica();
		verify(queryRouterMetaInfoHolder).isQueryRetriable(NAMED_QUERY);
	}

	/**
	 * Test that query shouldn't be retried on master if HDS is ON, entity manager is read-only one, query is not safe for replica, query is
	 * retriable and result is not empty (when a collection of results is returned).
	 */
	@Test
	public void shouldNotRetryOnMasterWhenHDSIsONAndEntityManagerIsReadOnlyAndQueryIsNotSafeForReplicaAndQueryIsRetriableAndResultIsNotEmpty() {
		List<Persistable> results = Lists.newArrayList(mock(Persistable.class));

		when(hdsSupportBean.isHdsSupportEnabled()).thenReturn(true);
		when(hdsSupportBean.isQuerySafeForReplica()).thenReturn(false);
		when(queryRouterMetaInfoHolder.isQueryRetriable(any())).thenReturn(true);

		boolean shouldRetry = queryRouter.shouldRetry(readOnlyEntityManager, NAMED_QUERY, results);

		assertThat(shouldRetry)
			.isFalse();

		verify(hdsSupportBean).isHdsSupportEnabled();
		verify(hdsSupportBean).isQuerySafeForReplica();
		verify(queryRouterMetaInfoHolder).isQueryRetriable(NAMED_QUERY);
	}

	/**
	 * Test that query shouldn't be retried on master if HDS is ON, entity manager is read-only one, query is not safe for replica, query is not
	 * retriable.
	 */
	@Test
	public void shouldNotRetryOnMasterWhenHDSIsONAndEntityManagerIsReadOnlyAndQueryIsNotSafeForReplicaAndQueryIsNotRetriable() {
		Persistable result = mock(Persistable.class);

		when(hdsSupportBean.isHdsSupportEnabled()).thenReturn(true);
		when(hdsSupportBean.isQuerySafeForReplica()).thenReturn(false);
		when(queryRouterMetaInfoHolder.isQueryRetriable(any())).thenReturn(false);

		boolean shouldRetry = queryRouter.shouldRetry(readOnlyEntityManager, NAMED_QUERY, result);

		assertThat(shouldRetry)
			.isFalse();

		verify(hdsSupportBean).isHdsSupportEnabled();
		verify(hdsSupportBean).isQuerySafeForReplica();
		verify(queryRouterMetaInfoHolder).isQueryRetriable(NAMED_QUERY);
	}

	/**
	 * Test that query shouldn't be retried on master if HDS is ON, entity manager is read-only one, query is safe for replica.
	 */
	@Test
	public void shouldNotRetryOnMasterWhenHDSIsONAndEntityManagerIsReadOnlyAndQueryIsSafeForReplica() {
		Persistable result = mock(Persistable.class);

		when(hdsSupportBean.isHdsSupportEnabled()).thenReturn(true);
		when(hdsSupportBean.isQuerySafeForReplica()).thenReturn(true);

		boolean shouldRetry = queryRouter.shouldRetry(readOnlyEntityManager, NAMED_QUERY, result);

		assertThat(shouldRetry)
			.isFalse();

		verify(hdsSupportBean).isHdsSupportEnabled();
		verify(hdsSupportBean).isQuerySafeForReplica();
		verifyZeroInteractions(queryRouterMetaInfoHolder);
	}

	/**
	 * Test that query shouldn't be retried on master if HDS is ON, entity manager is read-write one.
	 */
	@Test
	public void shouldNotRetryOnMasterWhenHDSIsONAndEntityManagerIsReadWrite() {
		Persistable result = mock(Persistable.class);

		when(hdsSupportBean.isHdsSupportEnabled()).thenReturn(true);

		boolean shouldRetry = queryRouter.shouldRetry(readWriteEntityManager, NAMED_QUERY, result);

		assertThat(shouldRetry)
			.isFalse();

		verify(hdsSupportBean).isHdsSupportEnabled();
		verifyNoMoreInteractions(hdsSupportBean);
		verifyZeroInteractions(queryRouterMetaInfoHolder);
	}

	/**
	 * Test that query shouldn't be retried on master if HDS is OFF.
	 */
	@Test
	public void shouldNotRetryOnMasterWhenHDSIsOFF() {
		Persistable result = mock(Persistable.class);

		when(hdsSupportBean.isHdsSupportEnabled()).thenReturn(false);

		boolean shouldRetry = queryRouter.shouldRetry(readWriteEntityManager, NAMED_QUERY, result);

		assertThat(shouldRetry)
			.isFalse();

		verify(hdsSupportBean).isHdsSupportEnabled();
		verifyNoMoreInteractions(hdsSupportBean);
		verifyZeroInteractions(readWriteEntityManager, readOnlyEntityManager, queryRouterMetaInfoHolder);
	}

	/**
	 * Test that query shouldn't be retried on master if HDS is ON, entity manager is read-only one, query is not safe for replica, query is
	 * retriable and result is null (when a single entity is returned).
	 */
	@Test
	public void shouldRetryOnMasterWhenHDSIsONAndEntityManagerIsReadOnlyAndQueryIsNotSafeForReplicaAndQueryIsRetriableAndResultIsNull() {
		when(hdsSupportBean.isHdsSupportEnabled()).thenReturn(true);
		when(hdsSupportBean.isQuerySafeForReplica()).thenReturn(false);
		when(queryRouterMetaInfoHolder.isQueryRetriable(any())).thenReturn(true);

		boolean shouldRetry = queryRouter.shouldRetry(readOnlyEntityManager, NAMED_QUERY, null);

		assertThat(shouldRetry)
			.isTrue();

		verify(hdsSupportBean).isHdsSupportEnabled();
		verify(hdsSupportBean).isQuerySafeForReplica();
		verify(queryRouterMetaInfoHolder).isQueryRetriable(NAMED_QUERY);
	}

	/**
	 * Test that query shouldn't be retried on master if HDS is ON, entity manager is read-only one, query is not safe for replica, query is
	 * retriable and a list of results is empty .
	 */
	@Test
	public void shouldRetryOnMasterWhenHDSIsONAndEntityManagerIsReadOnlyAndQueryIsNotSafeForReplicaAndQueryIsRetriableAndResultsAreEmpty() {
		when(hdsSupportBean.isHdsSupportEnabled()).thenReturn(true);
		when(hdsSupportBean.isQuerySafeForReplica()).thenReturn(false);
		when(queryRouterMetaInfoHolder.isQueryRetriable(any())).thenReturn(true);

		boolean shouldRetry = queryRouter.shouldRetry(readOnlyEntityManager, NAMED_QUERY, new ArrayList<>());

		assertThat(shouldRetry)
			.isTrue();

		verify(hdsSupportBean).isHdsSupportEnabled();
		verify(hdsSupportBean).isQuerySafeForReplica();
		verify(queryRouterMetaInfoHolder).isQueryRetriable(NAMED_QUERY);
	}

}
