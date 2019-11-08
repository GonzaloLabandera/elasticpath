package com.elasticpath.service.caching.impl;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.cache.SimpleTimeoutCache;

/**
 * Tests for SynchronizedCacheSupportImpl.
 */
@RunWith(MockitoJUnitRunner.class)
public class SynchronizedCacheSupportImplTest {
	@Mock
	private SimpleTimeoutCache<Object, Boolean> cache;

	@Mock
	private Object cacheKey;

	@InjectMocks
	private SynchronizedCacheSupportImpl<Object, Boolean> synchronizedCacheSupport;

	/**
	 * Test setup.
	 */
	@Before
	public void setup() {
		synchronizedCacheSupport.setEvaluationCache(cache);
	}

	/**
	 * Ensure that the correct result is returned when the cache hits.
	 */
	@Test
	public void testCacheHit() {
		when(cache.get(cacheKey)).thenReturn(true);

		assertTrue(synchronizedCacheSupport.cacheResult(objects -> {
			fail("Cache population function should not be invoked");
			return false;
		}, cacheKey));
	}

	/**
	 * Ensure that the correct result is returned when the cache misses.
	 */
	@Test
	public void testCacheMiss() {
		when(cache.get(cacheKey)).thenReturn(null);

		assertTrue(synchronizedCacheSupport.cacheResult(objects -> true, cacheKey));
	}

	/**
	 * Ensure that the correct result is returned when the cache misses and then hits within the mutex.
	 */
	@Test
	public void testCacheMissThenHitWithinLock() {
		when(cache.get(cacheKey)).thenReturn(null, true);

		assertTrue(synchronizedCacheSupport.cacheResult(objects -> {
			fail("Cache population function should not be invoked");
			return false;
		}, cacheKey));

	}
}