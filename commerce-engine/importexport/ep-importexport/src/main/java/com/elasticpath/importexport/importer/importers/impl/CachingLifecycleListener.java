/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.importer.importers.impl;

import com.elasticpath.caching.core.MutableCachingService;
import com.elasticpath.persistence.api.Persistable;

/**
 * Caching implementation of {@link LifecycleListener}.
 */
public class CachingLifecycleListener extends DefaultLifecycleListener {
	private MutableCachingService<Persistable> cachingService;

	@Override
	public void afterSave(final Persistable persistable) {
		invalidate(persistable);
	}

	/**
	 * Invalidates the persistable.
	 *
	 * @param persistable the persistable
	 */
	protected void invalidate(final Persistable persistable) {
		cachingService.invalidate(persistable);
	}

	protected MutableCachingService<Persistable> getCachingService() {
		return cachingService;
	}

	public void setCachingService(final MutableCachingService<Persistable> cachingService) {
		this.cachingService = cachingService;
	}
}