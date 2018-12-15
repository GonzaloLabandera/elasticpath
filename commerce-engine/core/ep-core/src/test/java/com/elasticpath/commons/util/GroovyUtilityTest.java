/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.commons.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.when;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import groovy.lang.Script;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.cache.SimpleTimeoutCache;
import com.elasticpath.commons.listeners.GroovyEhcacheEventListener;


/**
 * Test for {@link com.elasticpath.commons.util.GroovyUtility#removeExpiredCompiledScriptFromCache} method.
 */
@RunWith(MockitoJUnitRunner.class)
public class GroovyUtilityTest {

	private static final String GROOVY_SCRIPT = "a = 1";

	private final CacheManager cacheManager = CacheManager.getInstance();
	private final Cache ehCache = new Cache("simple_timeout", 2, false, false, 1L, 1L);

	private final SimpleTimeoutCache<String, FutureTask<Script>> groovyScriptTimeoutCache = new SimpleTimeoutCache<>();

	@Mock
	private FutureTask<Script> mockedCompiler;

	@Mock
	private Script mockedScript;

	@Before
	public void before() throws Exception {
		ehCache.setCacheManager(cacheManager);
		ehCache.initialise();

		groovyScriptTimeoutCache.setCache(ehCache);
		groovyScriptTimeoutCache.setCacheEventListener(new GroovyEhcacheEventListener());

		when(mockedCompiler.get()).thenReturn(mockedScript);
	}

	@After
	public void after() {
		ehCache.dispose();
	}

	@Test
	public void shouldRemoveCompiledScriptClassWhenScriptIsNotNull() {
		final long sleepTimeMillis = 1010L;

		groovyScriptTimeoutCache.put(GROOVY_SCRIPT, mockedCompiler);

		assertThat(groovyScriptTimeoutCache.get(GROOVY_SCRIPT)).isEqualTo(mockedCompiler);

		await().atLeast(sleepTimeMillis, TimeUnit.MILLISECONDS).until(() ->
			assertThat(groovyScriptTimeoutCache.get(GROOVY_SCRIPT)).isNull());
	}
}
