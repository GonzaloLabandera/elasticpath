/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.persistence.openjpa.strategies.impl;

import org.apache.openjpa.enhance.PersistenceCapable;
import com.elasticpath.persistence.api.CachedInstanceDetachmentStrategy;


/**
 * This strategy handles only {@link PersistenceCapable} objects and nulls both the state manager and detached state fields.
 * This completely bypasses the additional code, weaved by the OpenJPA enhancer, for accessing fields under high load.
 *
 * The intended usage is for applications using cached instances in read-only scenarios.
 */
public class StrictDetachmentStrategyImpl implements CachedInstanceDetachmentStrategy {

	@Override
	public <T> T detach(final T object) {
		if (object instanceof PersistenceCapable) {
			PersistenceCapable persistenceCapable = (PersistenceCapable) object;

			persistenceCapable.pcReplaceStateManager(null);
			persistenceCapable.pcSetDetachedState(null);
		}

		return object;
	}
}
