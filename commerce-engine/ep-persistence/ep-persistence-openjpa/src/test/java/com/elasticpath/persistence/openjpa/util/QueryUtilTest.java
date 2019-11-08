/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.persistence.openjpa.util;

import static com.elasticpath.persistence.api.PersistenceConstants.LIST_PARAMETER_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;

import org.junit.Test;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAQuery;

/**
 * Unit test for the {@code QueryUtil} class.
 */
public class QueryUtilTest {

	private final QueryUtil fixture = new QueryUtil();

	/**
	 * Test execution of a dynamic query with list.
	 */
	@Test
	public final void testInsertListIntoQuery() {
		final String queryString = "SELECT so FROM SomeObjectImpl so WHERE so.uidPk in (:list) AND so.field = :field";
		final String listParameterValues = "1001, 1002, 1003";

		final OpenJPAQuery<?> mockQuery = mock(OpenJPAQuery.class);
		final OpenJPAEntityManager mockEntityManager = mock(OpenJPAEntityManager.class);

		when(mockQuery.getQueryString()).thenReturn(queryString);
		when(mockQuery.getEntityManager()).thenReturn(mockEntityManager);

		fixture.insertListIntoQuery(mockQuery, LIST_PARAMETER_NAME, listParameterValues);

		verify(mockQuery).getQueryString();
		verify(mockEntityManager).createQuery("SELECT so FROM SomeObjectImpl so WHERE so.uidPk in (1001, 1002, 1003) AND so.field = :field");
	}

	/**
	 * Test that splitting a large collection (i.e. > 900 parameters) creates the correct number of
	 * resultant collection strings.
	 */
	@Test
	public final void testSplitCollectionWithLargeCollection() {
		final int parameterCount = 1000;
		final Set<Long> values = new HashSet<>();

		for (long l = 0; l < parameterCount; l++) {
			values.add(l);
		}

		List<String> result = fixture.splitCollection(values, parameterCount);

		assertThat(result)
			.as("The method should have split the collection into 2 strings")
			.hasSize(2);
	}

	/**
	 * Test that splitting a small collection doesn't actually split the collection but just returns
	 * a single collection string.
	 */
	@Test
	public final void testSplitCollectionWithSmallCollection() {
		final int parameterCount = 3;
		final List<Long> values = new ArrayList<>();

		for (long l = 1; l <= parameterCount; l++) {
			values.add(l);
		}

		List<String> result = fixture.splitCollection(values, parameterCount);

		assertThat(result).containsOnly("1,2,3");
	}

	/**
	 * Test that splitting a small collection doesn't actually split the collection but just returns
	 * a single collection string. A single quote ' should be escaped to '' according to SQL escaping rules.
	 */
	@Test
	public final void testSplitCollectionWithEscaping() {
		final int parameterCount = 3;
		final List<String> values = new ArrayList<>();
		values.add("SNAP' IT UP");
		values.add("SNAP IT UP UK");
		values.add("SLR' WORLD");

		List<String> result = fixture.splitCollection(values, parameterCount);

		assertThat(result).containsOnly("'SNAP'' IT UP','SNAP IT UP UK','SLR'' WORLD'");
	}

	/**
	 * Tests that if a parameter is a string and it contains single quotes they will be escaped according to SQL rules.
	 */
	@Test
	public final void testEscapeParameterWithStrings() {
		final String noSingleQuotes = "some text";
		final String hasSingleQuotes = "some' text'";

		String result = fixture.escapeParameter(noSingleQuotes);

		assertThat(result)
			.as("The string should be the same as before")
			.isEqualTo(noSingleQuotes);

		result = fixture.escapeParameter(hasSingleQuotes);

		assertThat(result)
			.as("Single quotes should be escaped")
			.isEqualTo("some'' text''");
	}

	/**
	 * Currently only string parameters are escaped. Parameters of other types should not be processed.
	 */
	@Test
	public final void testEscapeParameterWithNonStrings() {
		Integer one = 1;
		Integer result = fixture.escapeParameter(one);
		assertThat(result).isSameAs(one);
	}

	/**
	 * Test that class name is obtained from the Entity#name() method.
	 */
	@Test
	public void shouldReturnEntityClassNameByCallingEntityNameMethod() {
		assertThat(fixture.getEntityClassName(OrdinaryManEntityImpl.class))
			.isEqualTo("SupermanEntityImpl");
	}

	/**
	 * Test that class name is obtained from the Class#getSimpleName() method.
	 */
	@Test
	public void shouldReturnEntityClassNameByCallingGetSimpleNameMethod() {
		assertThat(fixture.getEntityClassName(EntityWithoutNameImpl.class))
			.isEqualTo("EntityWithoutNameImpl");
	}

	@Entity(name = "SupermanEntityImpl")
	private class OrdinaryManEntityImpl {
		//empty class
	}

	@Entity
	private class EntityWithoutNameImpl {
		//empty class
	}

}
