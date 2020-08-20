/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.importer.importers.impl;

import com.elasticpath.persistence.api.Persistable;

/**
 * Product caching implementation of {@link LifecycleListener}.
 */
public class ProductCachingLifecycleListener extends CachingLifecycleListener {
	@Override
	public void beforePopulate(final Persistable persistable) {
		super.beforePopulate(persistable);
		invalidate(persistable);
	}

	@Override
	public void afterSave(final Persistable persistable) {
		super.afterSave(persistable);
		invalidate(persistable);
	}
}