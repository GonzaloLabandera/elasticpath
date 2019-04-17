/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.cache.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Lists;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.cache.CacheLoader;

@RunWith(MockitoJUnitRunner.class)
public class EhcacheCacheAdapterTest {
	private static final String KEY_1 = "key1";
	private static final String KEY_2 = "key2";
	private static final String VAL_1 = "value1";
	private static final String VAL_2 = "value2";

	private static final String CACHE_NAME = "test-cache";
	private final CacheManager cacheManager = CacheManager.create();
	@Mock
	private CacheLoader<String, String> fallbackCacheLoader;

	private Cache cache;
	private EhcacheCacheAdapter<String, String> adapter;

	@Before
	public void setUp() throws Exception {
		cache = new Cache(CACHE_NAME, 0, false, true, 0, 0);
		cacheManager.addCache(cache);

		adapter = new EhcacheCacheAdapter<>(cache);
	}

	@After
	public void tearDown() {
		cacheManager.removeCache(CACHE_NAME);
	}

	@Test
	public void verifyThatGetOnCacheHitReturnsACachedObject() {
		cache.put(new Element("foo", "bar"));

		assertEquals("bar", adapter.get("foo"));
	}

	@Test
	public void verifyThatGetOnCacheMissReturnsNull() {
		assertNull(adapter.get("foo"));
	}

	@Test
	public void verifyThatCacheContainsKey()  {

		cache.put(new Element(KEY_1, VAL_1));

		assertTrue(adapter.containsKey(KEY_1));
	}

	@Test
	public void shouldGetFromCacheOnCacheHitNotFromDb()  {
		cache.put(new Element(KEY_1, VAL_1));

		assertEquals(VAL_1, adapter.get(KEY_1, fallbackCacheLoader));

		verifyZeroInteractions(fallbackCacheLoader);
	}

	@Test
	public void shouldGetFromDbOnCacheMissUsingFallbackLoader()  {
		when(fallbackCacheLoader.load(KEY_1)).thenReturn(VAL_1);

		assertEquals(VAL_1, adapter.get(KEY_1, fallbackCacheLoader));
		//verify that value is stored in cache
		assertEquals(VAL_1, adapter.get(KEY_1));

		verify(fallbackCacheLoader).load(KEY_1);
		verifyNoMoreInteractions(fallbackCacheLoader);
	}

	@Test
	public void shouldReturnNullFromDbOnCacheMissUsingFallbackLoader()  {
		when(fallbackCacheLoader.load(KEY_1)).thenReturn(null);

		assertNull(adapter.get(KEY_1, fallbackCacheLoader));
		assertNull(VAL_1, adapter.get(KEY_1));

		verify(fallbackCacheLoader).load(KEY_1);
		verifyNoMoreInteractions(fallbackCacheLoader);
	}

	@Test
	public void shouldReturnCompleteMapWithCachedValuesOnAllCacheHits()  {
		List<String> keys = Lists.newArrayList(KEY_1, KEY_2);
		List<String> values = Lists.newArrayList(VAL_1, VAL_2);

		cache.put(new Element(KEY_1, VAL_1));
		cache.put(new Element(KEY_2, VAL_2));

		Map<String, String> result = adapter.getAll(keys);

		assertEquals(2, result.size());
		assertTrue(result.keySet().containsAll(keys));
		assertTrue(result.values().containsAll(values));
	}

	@Test
	public void shouldReturnPartialMapWithCachedValuesOnPartialCacheHits()  {
		List<String> keys = Lists.newArrayList(KEY_1, KEY_2);

		cache.put(new Element(KEY_1, VAL_1));
		cache.put(new Element("willBeMiss", VAL_2));

		Map<String, String> result = adapter.getAll(keys);

		assertEquals(1, result.size());

		assertTrue(result.containsKey(KEY_1));
		assertFalse(result.containsKey(KEY_2));

		assertTrue(result.containsValue(VAL_1));
		assertFalse(result.containsValue(VAL_2));
	}

	@Test
	public void shouldReturnEmptyMapForOnCacheMisses()  {
		List<String> keys = Lists.newArrayList(KEY_1, KEY_2);

		cache.put(new Element("miss1", VAL_1));
		cache.put(new Element("miss2", VAL_2));

		Map<String, String> result = adapter.getAll(keys);

		assertTrue(result.isEmpty());
	}

	@Test
	public void shouldReturnCompleteMapWithAllCachedValuesOnAllCacheHitsWithoutFallbackLoader()  {
		List<String> keys = Lists.newArrayList(KEY_1, KEY_2);
		List<String> values = Lists.newArrayList(VAL_1, VAL_2);

		cache.put(new Element(KEY_1, VAL_1));
		cache.put(new Element(KEY_2, VAL_2));

		Map<String, String> result = adapter.getAll(keys, fallbackCacheLoader);

		assertEquals(2, result.size());
		assertTrue(result.keySet().containsAll(keys));
		assertTrue(result.values().containsAll(values));

		verifyZeroInteractions(fallbackCacheLoader);
	}

	@Test
	public void shouldLoadFromDbOnPartialHits()  {
		List<String> keys = Lists.newArrayList(KEY_1, KEY_2);
		List<String> values = Lists.newArrayList(VAL_1, VAL_2);
		List<String> missingKey = Lists.newArrayList(KEY_2);

		cache.put(new Element(KEY_1, VAL_1));

		Map<String, String> expectedMapWithMissingKeyVal = new HashMap<>(1);
		expectedMapWithMissingKeyVal.put(KEY_2, VAL_2);

		when(fallbackCacheLoader.loadAll(missingKey)).thenReturn(expectedMapWithMissingKeyVal);

		Map<String, String> result = adapter.getAll(keys, fallbackCacheLoader);

		assertEquals(2, result.size());
		assertTrue(result.keySet().containsAll(keys));
		assertTrue(result.values().containsAll(values));
		assertEquals(VAL_1, adapter.get(KEY_1));
		assertEquals(VAL_2, adapter.get(KEY_2));

		verify(fallbackCacheLoader).loadAll(missingKey);
		verifyZeroInteractions(fallbackCacheLoader);
	}

	@Test
	public void shouldLoadFromDbOnAllMisses()  {
		List<String> keys = Lists.newArrayList(KEY_1, KEY_2);
		List<String> values = Lists.newArrayList(VAL_1, VAL_2);

		Map<String, String> expectedMapWithMissingKeyVal = new HashMap<>(1);
		expectedMapWithMissingKeyVal.put(KEY_1, VAL_1);
		expectedMapWithMissingKeyVal.put(KEY_2, VAL_2);

		when(fallbackCacheLoader.loadAll(keys)).thenReturn(expectedMapWithMissingKeyVal);

		Map<String, String> result = adapter.getAll(keys, fallbackCacheLoader);

		assertEquals(2, result.size());
		assertTrue(result.keySet().containsAll(keys));
		assertTrue(result.values().containsAll(values));
		assertEquals(VAL_1, adapter.get(KEY_1));
		assertEquals(VAL_2, adapter.get(KEY_2));

		verify(fallbackCacheLoader).loadAll(keys);
		verifyZeroInteractions(fallbackCacheLoader);
	}

}
