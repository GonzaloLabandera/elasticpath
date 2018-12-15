/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.TimeUnit;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.awaitility.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test that SimpleTimeoutCache works as intended.
 */
public class SimpleTimeoutCacheTest {

	private static final String FRED = "fred";
	private static final String FREDS_INFORMATION = "fred's information";

	private final CacheManager cacheManager = CacheManager.getInstance();
	private final SimpleTimeoutCache<String, String> fixture = new SimpleTimeoutCache<>();

	private Cache ehCache;

	@Before
	public void before() {
		ehCache = createEhcacheInstance("simple_timeout", 2, 1L, 1L);

		fixture.setCache(ehCache);
	}

	@After
	public void after() {
		ehCache.dispose();
	}
	
	/**
	 * Simple test for putting and getting from the SimpleTimeoutCache.
	 */
	@Test
	public void testSimpleCaching() {
		final Duration sleepTime = new Duration(1050L, TimeUnit.MILLISECONDS);
		final long timeout = 1000L;

		fixture.changeTimeout(timeout);
		fixture.put(FRED, FREDS_INFORMATION);

		assertThat(fixture.get(FRED)).isEqualTo(FREDS_INFORMATION);
		
		// Make sure it's available after the first call
		assertThat(fixture.get(FRED)).isEqualTo(FREDS_INFORMATION);
		assertThat(fixture.get(FRED)).isEqualTo(FREDS_INFORMATION);
		assertThat(fixture.get(FRED)).isEqualTo(FREDS_INFORMATION);

		await().between(Duration.ONE_SECOND, sleepTime).until(() ->
			assertThat(fixture.get(FRED)).isNull());

	}
	
	/** 
	 * Test that with a zero timeout values are cached indefinitely (eternal cache).
	 */
	@Test
	public void testZeroTimeout() {
		final long timeout = 0L;

		fixture.changeTimeout(timeout);
		fixture.put(FRED, FREDS_INFORMATION);

		await().atMost(Duration.ONE_SECOND).until(() ->
			assertThat(fixture.get(FRED)).isEqualTo(FREDS_INFORMATION));
	}
	
	/** 
	 * Test that with a negative timeout values nothing is cached (timeout must be in negative seconds).
	 */
	@Test
	public void testNegativeTimeout() {
		final long timeout = -1000L;

		fixture.changeTimeout(timeout);
		fixture.put(FRED, FREDS_INFORMATION);

		await().atMost(Duration.ONE_SECOND).until(() ->
			assertThat(fixture.get(FRED)).isNull());
	}
	
	
	/**
	 * Test the timeout works as expected.
	 */
	@Test
	public void testNormalCacheTimeout() {
		final long timeout = 3000L;

		fixture.changeTimeout(timeout);
		fixture.put(FRED, FREDS_INFORMATION);  // put in at time=0 - should expire after 3s

		assertThat(fixture.get(FRED)).isEqualTo(FREDS_INFORMATION);

		await().atLeast(timeout, TimeUnit.MILLISECONDS).until(() ->
			assertThat(fixture.get(FRED)).as("Cached entry should have expired").isNull());
	}

	/**
	 * Test storage and retrieval of null values.
	 */
	@Test
	public void testNullKeysAndValues() {
		fixture.put(null, null);
		assertThat(fixture.get(null)).as("Returned cached entry must be null").isNull();

		fixture.put(null, FRED);
		assertThat(fixture.get(null)).as("Returned cached entry must be null if key is null, regardless of value").isNull();
	}

	/**
	 * Test creation of multiple instances of SimpleTimeoutCache with Ehcache instances with the same name.
	 */
	@Test
	public void testMultipleInstanceOfSimpleTimeoutCacheWithMultipleEhcacheInstancesWithSameName() {
		final SimpleTimeoutCache<String, String> simpleCache1 = new SimpleTimeoutCache<>();
		final Cache ehCache1 = createEhcacheInstance("simple_timeout", 2, 1L, 1L);

		simpleCache1.setCache(ehCache1);

		final int maxElementsInHeap = 20;
		final long timeout = 10L;
		final SimpleTimeoutCache<String, String> simpleCache2 = new SimpleTimeoutCache<>();
		final Cache ehCache2 = createEhcacheInstance("simple_timeout", maxElementsInHeap, timeout, timeout);

		simpleCache2.setCache(ehCache2);

		//assert ehCache1 params
		assertThat(simpleCache1.getCache().getCacheConfiguration().getMaxEntriesLocalHeap()).isEqualTo(2);
		assertThat(simpleCache1.getCache().getCacheConfiguration().getTimeToIdleSeconds()).isEqualTo(1L);
		assertThat(simpleCache1.getCache().getCacheConfiguration().getTimeToLiveSeconds()).isEqualTo(1L);

		//assert ehCache2 params
		assertThat(simpleCache2.getCache().getCacheConfiguration().getMaxEntriesLocalHeap()).isEqualTo(maxElementsInHeap);
		assertThat(simpleCache2.getCache().getCacheConfiguration().getTimeToIdleSeconds()).isEqualTo(timeout);
		assertThat(simpleCache2.getCache().getCacheConfiguration().getTimeToLiveSeconds()).isEqualTo(timeout);

	}

	private Cache createEhcacheInstance(final String name, final int maxElementsInHeap, final long timeToIdleSeconds, final long timeToLiveSeconds) {
		final Cache ehCache = new Cache(name, maxElementsInHeap, false, false, timeToIdleSeconds, timeToLiveSeconds);
		ehCache.setCacheManager(cacheManager);
		ehCache.initialise();

		return ehCache;
	}

	//TODO tests for cache event listener and timeout; test ordering
	
}
