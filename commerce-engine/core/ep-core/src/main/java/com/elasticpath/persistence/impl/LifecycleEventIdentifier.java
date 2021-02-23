/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.persistence.impl;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.openjpa.enhance.PersistenceCapable;

/**
 * Identifies a lifecycle event to ensure that repeat events are skipped.
 */
public class LifecycleEventIdentifier {
	private final EventActionGroupEnum eventActionGroup;
	private final Class<PersistenceCapable> entityClass;
	private final String entityGuid;

	/**
	 * Constructor.
	 *
	 * @param eventActionGroup the event action group
	 * @param entityClass the entity class
	 * @param entityGuid the entity object GUID
	 */
	public LifecycleEventIdentifier(final EventActionGroupEnum eventActionGroup, final Class<PersistenceCapable> entityClass,
									final String entityGuid) {
		this.eventActionGroup = eventActionGroup;
		this.entityClass = entityClass;
		this.entityGuid = entityGuid;
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}

		if (other == null || getClass() != other.getClass()) {
			return false;
		}

		LifecycleEventIdentifier that = (LifecycleEventIdentifier) other;

		return new EqualsBuilder()
				.append(eventActionGroup, that.eventActionGroup)
				.append(entityClass, that.entityClass)
				.append(entityGuid, that.entityGuid)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(eventActionGroup)
				.append(entityClass)
				.append(entityGuid)
				.toHashCode();
	}
}
