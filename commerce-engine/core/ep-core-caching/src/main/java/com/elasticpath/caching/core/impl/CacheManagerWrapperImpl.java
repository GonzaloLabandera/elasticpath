/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.caching.core.impl;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import com.elasticpath.caching.core.CacheManagerWrapper;

/**
 * Since net.sf.ehcache.CacheManager is a concrete class without an interface,
 * we need to create a decorator that uses our interface so that it can be exported through OSGi.
 */
public class CacheManagerWrapperImpl implements CacheManagerWrapper {
	private final CacheManager cacheManager;

	/**
	 * Constructor.
	 * @param cacheManager the ehCache cache manager we will delegate to
	 */
	public CacheManagerWrapperImpl(final CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	@Override
	public Cache getCache(final String name) throws IllegalStateException, ClassCastException {
		return cacheManager.getCache(name);
	}
}
