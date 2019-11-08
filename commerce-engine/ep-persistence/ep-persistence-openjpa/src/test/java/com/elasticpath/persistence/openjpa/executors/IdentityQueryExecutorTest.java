/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.persistence.openjpa.executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.persistence.Entity;
import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.persistence.openjpa.util.QueryUtil;

/**
 * Unit test for the {@code IdentityQueryExecutor} class.
 */
@SuppressWarnings({"unchecked", "rawtypes", "serial"})
@RunWith(MockitoJUnitRunner.class)
public class IdentityQueryExecutorTest {

	private static final long UID_PK = 1L;

	@InjectMocks private IdentityQueryExecutor identityQueryExecutor;

	@Mock private EntityManager entityManager;

	@Before
	public void init() {
		identityQueryExecutor.setQueryUtil(new QueryUtil());
	}
	/**
	 * Test whether a query is executed for a given entity class and UID PK.
	 */
	@Test
	public void shouldExecuteSingleResultQuery() {
		BikeImpl bike = new BikeImpl();

		identityQueryExecutor
			.withClass(BikeImpl.class)
			.withUidPk(UID_PK);

		when(entityManager.find(BikeImpl.class, UID_PK)).thenReturn(bike);

		BikeImpl actualResult = (BikeImpl) identityQueryExecutor.executeSingleResultQuery(entityManager);

		assertThat(actualResult)
			.isSameAs(bike);

		verify(entityManager).find(BikeImpl.class, UID_PK);
	}

	/**
	 * Test whether returned string representation of a query is correct.
	 */
	@Test
	public void shouldReturnDynamicQueryUsingClassName() {
		identityQueryExecutor
			.withClass(BikeImpl.class)
			.withUidPk(UID_PK);

		String actualResult = identityQueryExecutor.getQuery();

		assertThat(actualResult)
			.isEqualTo("SELECT c FROM BikeImpl c WHERE c.uidPk = ?1");
	}

	@Test
	public void shouldReturnDynamicQueryUsingEntityName() {
		identityQueryExecutor
			.withClass(BarImpl.class)
			.withUidPk(UID_PK);

		String actualResult = identityQueryExecutor.getQuery();

		assertThat(actualResult)
			.isEqualTo("SELECT c FROM FooImpl c WHERE c.uidPk = ?1");
	}

	@Entity(name = "FooImpl")
	private class BarImpl {
		//empty class
	}

	@Entity
	private class BikeImpl implements Persistable {
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
