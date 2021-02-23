/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.cache.impl;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

public class CacheUtilTest {

	public static final int KEY_1 = 1;
	public static final int KEY_2 = 2;
	public static final int KEY_3 = 3;
	public static final String VALUE_ONE = "one";
	public static final String VALUE_TWO = "two";
	public static final String VALUE_THREE = "three";

	@Test
	public void testMergeResultsCanCombineCachedAndUncachedResults() {
		// Given
		Map<Integer, String> cachedResults = ImmutableMap.of(
				KEY_1, VALUE_ONE,
				KEY_3, VALUE_THREE
		);
		Map<Integer, String> uncachedResults = ImmutableMap.of(
				KEY_2, VALUE_TWO
		);

		// When
		Map<Integer, String> combinedResults = CacheUtil.mergeResults(
				Arrays.asList(KEY_1, KEY_2, KEY_3), cachedResults, uncachedResults);

		// Then
		assertEquals("Results Should have been combined in the order that the keys were presented",
				ImmutableMap.of(
						KEY_1, VALUE_ONE,
						KEY_2, VALUE_TWO,
						KEY_3, VALUE_THREE
				), combinedResults);
	}

	@Test
	public void testMergeResultsCanHandleMissingResults() {
		// Given
		Map<Integer, String> cachedResults = ImmutableMap.of(
				KEY_1, VALUE_ONE
		);
		Map<Integer, String> uncachedResults = ImmutableMap.of(
				KEY_3, VALUE_THREE
		);

		Map<Integer, String> combinedResults = CacheUtil.mergeResults(
				Arrays.asList(KEY_1, KEY_2, KEY_3), cachedResults, uncachedResults);

		assertEquals("Results Should have been combined in the order that the keys were presented",
				ImmutableMap.of(
						KEY_1, VALUE_ONE,
						KEY_3, VALUE_THREE
				), combinedResults);
	}

	@Test
	public void testMergeResultsCanHandleNullValues() {
		final String nullValue = null;
		// Given
		Map<Integer, String> cachedResults = Collections.singletonMap(
				KEY_1, nullValue
		);
		Map<Integer, String> uncachedResults = Collections.singletonMap(
				KEY_3, nullValue
		);

		Map<Integer, String> combinedResults = CacheUtil.mergeResults(
				Arrays.asList(KEY_1, KEY_2, KEY_3), cachedResults, uncachedResults);

		Map<Integer, String> expectedMap = new LinkedHashMap<>();
		expectedMap.put(KEY_1, nullValue);
		expectedMap.put(KEY_3, nullValue);


		assertEquals("Results Should have been combined in the order that the keys were presented", expectedMap, combinedResults);
	}
}
