/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.persistence.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.OpenJPAQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Test the methods used in integration tests.
 */
@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class TestPersistenceEngineImplTest {

	@InjectMocks
	private TestPersistenceEngineImpl persistenceEngine;

	@Mock
	private EntityManager entityManager;

	@Test
	public void shouldRetrieveNative() {
		long expectedCount = 1L;
		OpenJPAQuery<Long> nativeQueryInstance = mock(OpenJPAQuery.class);

		String nativeQuery = "select count(*) from TTABLE";
		when(entityManager.createNativeQuery(nativeQuery)).thenReturn(nativeQueryInstance);
		when(nativeQueryInstance.getResultList()).thenReturn(Collections.singletonList(expectedCount));

		List<Long> actualCount = persistenceEngine.retrieveNative(nativeQuery);

		assertThat(actualCount.get(0))
				.isEqualTo(expectedCount);
	}

	@Test
	public void shouldExecuteNative() {
		int one = 1;
		Object[] params = new Object[]{one};

		OpenJPAQuery<Long> nativeQueryInstance = mock(OpenJPAQuery.class);

		String nativeQuery = "update TTABLE set guid='something' where uidPk=?1";
		when(entityManager.createNativeQuery(nativeQuery)).thenReturn(nativeQueryInstance);
		when(nativeQueryInstance.executeUpdate()).thenReturn(one);

		int updatedRows = persistenceEngine.executeNativeQuery(nativeQuery, params);

		assertThat(updatedRows)
				.isEqualTo(one);
		verify(nativeQueryInstance).setParameters(params);
	}
}
