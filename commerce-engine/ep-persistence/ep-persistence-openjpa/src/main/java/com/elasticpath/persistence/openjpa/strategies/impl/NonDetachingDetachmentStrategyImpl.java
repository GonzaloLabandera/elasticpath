/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.persistence.openjpa.strategies.impl;

import com.elasticpath.persistence.api.CachedInstanceDetachmentStrategy;


/**
 * This strategy simply returns the object as it is and it's suitable for single-threaded applications (e.g. import-export tool).
 * The instance will be cached with current state and the state manager.
 *
 */
public class NonDetachingDetachmentStrategyImpl implements CachedInstanceDetachmentStrategy {
	@Override
	public <T> T detach(final T object) {
		return object;
	}
}
