/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.persistence.openjpa.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import org.junit.Test;

/**
 * Unit test for the {@code QueryUtil} class.
 */
public class QueryUtilTest {

	private static final Long ONE_LONG = 1L;
	private static final Long TWO_LONG = 2L;
	private static final Long THREE_LONG = 3L;

	/**
	 * Test that splitting a large collection (i.e. > 900 parameters) creates the correct number of
	 * resultant collection strings.
	 */
	@Test
	public final void testSplitCollectionWithLargeCollection() {
		final int parameterCount = 1000;
		final List<Long> values = new ArrayList<>();

		for (long l = 0; l < parameterCount; l++) {
			values.add(l);
		}

		List<List<Long>> result = QueryUtil.splitCollection(values);

		assertThat(result)
			.as("The method should have split the collection into 2 lists")
			.hasSize(2);
	}

	/**
	 * Test that splitting a small collection doesn't actually split the collection but just returns
	 * a single collection string.
	 */
	@Test
	public final void testSplitCollectionWithSmallCollection() {
		final int parameterCount = 3;
		List<Long> values = new ArrayList<>();

		for (long l = 1; l <= parameterCount; l++) {
			values.add(l);
		}

		List<List<Long>> result = QueryUtil.splitCollection(values);

		assertThat(result).hasSize(1);
		assertThat(result.get(0)).contains(ONE_LONG, TWO_LONG, THREE_LONG);
	}

	/**
	 * Test that splitting a small collection doesn't actually split the collection but just returns
	 * a single collection string. A single quote ' should be escaped to '' according to SQL escaping rules.
	 */
	@Test
	public final void testSplitCollectionWithEscaping() {
		final List<String> values = new ArrayList<>();
		values.add("SNAP' IT UP");
		values.add("SNAP IT UP UK");
		values.add("SLR' WORLD");

		List<List<String>> result = QueryUtil.splitCollection(values);

		assertThat(result).hasSize(1);
		assertThat(result.get(0)).containsOnly("SNAP' IT UP", "SNAP IT UP UK", "SLR' WORLD");
	}

	/**
	 * Test that class name is obtained from the Entity#name() method.
	 */
	@Test
	public void shouldReturnEntityClassNameByCallingEntityNameMethod() {
		assertThat(QueryUtil.getEntityClassName(OrdinaryManEntityImpl.class))
			.isEqualTo("SupermanEntityImpl");
	}

	/**
	 * Test that class name is obtained from the Class#getSimpleName() method.
	 */
	@Test
	public void shouldReturnEntityClassNameByCallingGetSimpleNameMethod() {
		assertThat(QueryUtil.getEntityClassName(EntityWithoutNameImpl.class))
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
