/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */

package com.elasticpath.caching.core;

import java.util.Objects;

/**
 * Composite Ehcache key, consisting of entity code, uid and given date.
 */
public class CodeUidCacheKey {

	private final String entityCode;
	private final Integer entityUid;

	/**
	 * Default constructor.
	 *
	 * @param entityCode the entity code (store, catalog, etc)
	 * @param entityUid the entity id
	 */
	public CodeUidCacheKey(final String entityCode, final Integer entityUid) {
		this.entityCode = entityCode;
		this.entityUid = entityUid;
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}

		if (!(other instanceof CodeUidCacheKey)) {
			return false;
		}

		CodeUidCacheKey otherKey = (CodeUidCacheKey) other;
		return Objects.equals(entityCode, otherKey.entityCode)
			&& Objects.equals(entityUid, otherKey.entityUid);
	}

	@Override
	public int hashCode() {
		return Objects.hash(entityCode, entityUid);
	}
}
