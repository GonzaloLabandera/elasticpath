/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.caching.core;

import net.sf.ehcache.Cache;

/**
 * Since net.sf.ehcache.CacheManager is a concrete class without an interface,
 * we need to create an interface wrapper so that it can be exported through OSGi.
 */
public interface CacheManagerWrapper {
	/**
	 * Delegates to net.sf.ehcache.CacheManager#getCache(java.lang.String).
	 * @param name the cache name
	 * @return the cache
	 * @throws IllegalStateException if the cache is not alive
	 * @throws ClassCastException if a class cast exception occurs
	 */
	Cache getCache(String name) throws IllegalStateException, ClassCastException;
}
