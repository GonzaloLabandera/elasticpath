/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.commons.util.impl;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.commons.util.CacheInvalidationStrategy;
import com.elasticpath.commons.util.InvalidatableCache;

/**
 * Basic implementation of CacheInvalidationStrategy for any invalidatable resources.
 *
 */
public class TriggeredCacheInvalidationStrategyImpl implements CacheInvalidationStrategy {

	private Set<InvalidatableCache> caches;
	private static final Logger LOG = LogManager.getLogger(TriggeredCacheInvalidationStrategyImpl.class);

	@Override
	public void invalidateCaches() {
		for (InvalidatableCache cache : getCaches()) {
			cache.invalidate();
		}
		LOG.info("Cache invalidated");
	}


	@Override
	public void invalidateCachesForObject(final Object objectUid) {
		for (InvalidatableCache cache : getCaches()) {
			cache.invalidate(objectUid);
		}
		LOG.info("Cache invalidated for objects:" + objectUid);

	}


	@Override
	public void setInvalidatableCaches(final Set<InvalidatableCache> caches) {
		this.caches = caches;
	}

	/**
	 * @return set of invalidatable caches
	 */
	protected Set<InvalidatableCache> getCaches() {
		return caches;
	}
}
