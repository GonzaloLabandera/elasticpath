/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.persistence.openjpa.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Sets;

import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.meta.QueryMetaData;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.persistence.openjpa.routing.HDSSupportBean;

/**
 * Unit test for the {@code QueryRouterMetaInfoHolder} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class QueryRouterMetaInfoHolderTest {
	private static final String QUERY = "QUERY_OR_QUERY_STRING";
	private static final int FOUR_WANTED_TIMES_OF_INVOCATION = 4;

	@InjectMocks private QueryRouterMetaInfoHolder queryRouterMetaInfoHolder;

	@InjectMocks
	@Spy
	private QueryRouterMetaInfoHolder queryRouterMetaInfoHolderSpy;

	@Mock private HDSSupportBean hdsSupportBean;
	@Mock private JPAAnnotationParser jpaAnnotationParser;
	@Mock private NamedQueryParser namedQueryParser;

	/**
	 * Test that parsing of entity classes and named queries happened.
	 */
	@Test
	public final void shouldInitFromReadWriteEntityManager() {
		EntityManager readWriteEntityManager = mock(EntityManager.class);
		MetaDataRepository metaDataRepository = mock(MetaDataRepository.class);

		doReturn(metaDataRepository).when(queryRouterMetaInfoHolderSpy).getMetaDataRepositoryInstance(readWriteEntityManager);

		when(metaDataRepository.getMetaDatas()).thenReturn(new ClassMetaData[0]);
		when(metaDataRepository.getQueryMetaDatas()).thenReturn(new QueryMetaData[0]);

		queryRouterMetaInfoHolderSpy.initFromRWEntityManager(readWriteEntityManager);

		verify(jpaAnnotationParser).parse(any(ClassMetaData[].class));
		verify(namedQueryParser).parse(any(QueryMetaData[].class));
	}

	/**
	 * Test that named query is safe for running on replica if FROM entities are not modified given that set of modified entities exists.
	 */
	@Test
	public final void shouldReturnTrueIfQueryIsSafeForReadingFromReplicaWhenModifiedEntitiesExistAndNamedQueryFROMEntitiesAreNotModified() {
		Set<String> queryFromEntities = Sets.newHashSet("AvengersImpl", "ThorImpl");
		Set<String> modifiedEntities = Sets.newHashSet("BatmanImpl", "SupermanImpl");

		when(namedQueryParser.getQueriedEntitiesByQueryName(QUERY)).thenReturn(queryFromEntities);
		when(hdsSupportBean.getModifiedEntities()).thenReturn(modifiedEntities);
		when(jpaAnnotationParser.isQueriedEntityCoupledToModifiedEntity(anyString(), anyString())).thenReturn(false);


		assertThat(queryRouterMetaInfoHolder.isQuerySafeForReadingFromReplica(QUERY))
			.isTrue();

		verify(hdsSupportBean).getModifiedEntities();
		verify(namedQueryParser).getQueriedEntitiesByQueryName(QUERY);
		verify(jpaAnnotationParser, times(FOUR_WANTED_TIMES_OF_INVOCATION)).isQueriedEntityCoupledToModifiedEntity(anyString(), anyString());
		verify(hdsSupportBean).setQueryIsSafeForReplica(true);
	}

	/**
	 * Test that named query is safe for running on replica if FROM entities are not modified given that set of modified entities is empty.
	 */
	@Test
	public final void shouldReturnTrueIfQueryIsSafeForReadingFromReplicaWhenModifiedEntitiesAreEmptyAndNamedQueryFROMEntitiesAreNotModified() {
		Set<String> queryFromEntities = Sets.newHashSet("AvengersImpl", "ThorImpl");

		when(namedQueryParser.getQueriedEntitiesByQueryName(QUERY)).thenReturn(queryFromEntities);
		when(hdsSupportBean.getModifiedEntities()).thenReturn(new HashSet<>());


		assertThat(queryRouterMetaInfoHolder.isQuerySafeForReadingFromReplica(QUERY))
			.isTrue();

		verify(hdsSupportBean).getModifiedEntities();
		verify(namedQueryParser).getQueriedEntitiesByQueryName(QUERY);
		verify(hdsSupportBean).setQueryIsSafeForReplica(true);
		verifyZeroInteractions(jpaAnnotationParser);
	}

	/**
	 * Test that named query is NOT safe for running on replica if at least one FROM entity is modified given that set of modified entities
	 * exists.
	 */
	@Test
	public final void shouldReturnFalseIfQueryIsNotSafeForReadingFromReplicaWhenModifiedEntitiesExistAndAtLeastONeFROMEntityIsModified() {
		Set<String> queryFromEntities = Sets.newHashSet("AvengersImpl", "BatmanImpl");
		Set<String> modifiedEntities = Sets.newHashSet("BatmanImpl", "SupermanImpl");

		when(namedQueryParser.getQueriedEntitiesByQueryName(QUERY)).thenReturn(queryFromEntities);
		when(hdsSupportBean.getModifiedEntities()).thenReturn(modifiedEntities);


		assertThat(queryRouterMetaInfoHolder.isQuerySafeForReadingFromReplica(QUERY))
			.isFalse();

		verify(hdsSupportBean).getModifiedEntities();
		verify(namedQueryParser).getQueriedEntitiesByQueryName(QUERY);
		verify(hdsSupportBean).setQueryIsSafeForReplica(false);
		verifyZeroInteractions(jpaAnnotationParser);
	}
}
