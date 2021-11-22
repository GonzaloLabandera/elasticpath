package com.elasticpath.cache.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.cache.CacheResult;
import com.elasticpath.base.cache.SynchronizedCacheSupport;
import com.elasticpath.base.cache.impl.SynchronizedCacheSupportImpl;

/**
 * Tests for SynchronizedCacheSupportImpl.
 */
@RunWith(MockitoJUnitRunner.class)
public class SynchronizedCacheSupportImplTest {
	private static final int NUMBER_OF_THREADS = 100;
	private static final int NUMBER_OF_ATTEMPTS_PER_THREAD = 10;
	private static final int FALLBACK_LOADER_DELAY_MS = 5000;
	private static final int MAX_TEST_DURATION_SECONDS = 30;

	private final BiFunction<Object, Boolean, Boolean> populateCacheFunctionDoNothing = (key, value) -> value;
	private final Function<Object, CacheResult<Boolean>> checkCacheFunctionAlwaysHit = key -> CacheResult.create(true);
	private final Function<Object, CacheResult<Boolean>> checkCacheFunctionAlwaysMiss = key -> CacheResult.notPresent();

	@Mock
	private Object cacheKey;

	private final SynchronizedCacheSupport<Object, Boolean> synchronizedCacheSupport = new SynchronizedCacheSupportImpl<>();

	/**
	 * Ensure that the correct result is returned when the cache hits.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testCacheHit() {
		Function<Object, Boolean> fallbackLoaderFunction = mock(Function.class);
		assertThat(synchronizedCacheSupport.get(cacheKey, fallbackLoaderFunction, checkCacheFunctionAlwaysHit, populateCacheFunctionDoNothing))
				.isEqualTo(true);
		verify(fallbackLoaderFunction, never()).apply(Mockito.any());
	}

	/**
	 * Ensure that the correct result is returned when the cache misses.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testCacheMiss() {
		Function<Object, Boolean> fallbackLoaderFunction = mock(Function.class);
		when(fallbackLoaderFunction.apply(Mockito.any())).thenReturn(true);
		assertThat(synchronizedCacheSupport.get(cacheKey, fallbackLoaderFunction, checkCacheFunctionAlwaysMiss, populateCacheFunctionDoNothing))
				.isEqualTo(true);
		verify(fallbackLoaderFunction).apply(Mockito.any());
	}

	@SuppressWarnings("PMD.PrematureDeclaration")
	@Test
	public void testMultipleThreadsOnlyTriggerOneCacheLoad() throws InterruptedException {
		AtomicInteger timesFallbackLoaderFunctionInvoked = new AtomicInteger(0);
		AtomicInteger timesSynchronizedCacheSupportInvoked = new AtomicInteger(0);
		AtomicReference<Boolean> cacheContents = new AtomicReference<>();
		Function<Object, Boolean> fallbackLoaderFunctionSlow = pair -> {
			timesFallbackLoaderFunctionInvoked.getAndIncrement();
			try {
				Thread.sleep(FALLBACK_LOADER_DELAY_MS);
			} catch (InterruptedException e) {
				// Do nothing
			}
			return true;
		};
		BiFunction<Object, Boolean, Boolean> populateCacheFunction = (key, value) -> {
			cacheContents.set(value);
			return value;
		};
		Function<Object, CacheResult<Boolean>> checkCacheFunction = key -> {
			if (cacheContents.get() == null) {
				return CacheResult.notPresent();
			}
			return CacheResult.create(cacheContents.get());
		};
		Runnable runnable = () -> {
			for (int i = 0; i < NUMBER_OF_ATTEMPTS_PER_THREAD; i++) {
				synchronizedCacheSupport.get(cacheKey, fallbackLoaderFunctionSlow, checkCacheFunction, populateCacheFunction);
				timesSynchronizedCacheSupportInvoked.incrementAndGet();
			}
		};
		ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
		for (int i = 0; i < NUMBER_OF_THREADS; i++) {
			executor.execute(runnable);
		}
		executor.shutdown();
		executor.awaitTermination(MAX_TEST_DURATION_SECONDS, TimeUnit.SECONDS);
		assertThat(timesSynchronizedCacheSupportInvoked.get()).isEqualTo(NUMBER_OF_ATTEMPTS_PER_THREAD * NUMBER_OF_THREADS);
		assertThat(timesFallbackLoaderFunctionInvoked.get()).isEqualTo(1);
	}
}