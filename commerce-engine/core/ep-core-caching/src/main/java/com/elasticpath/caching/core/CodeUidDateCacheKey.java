/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */

package com.elasticpath.caching.core;

import java.util.Date;
import java.util.Objects;

/**
 * Composite Ehcache key, consisting of entity code, uid and given date.
 */
public class CodeUidDateCacheKey {

	private final String entityCode;
	private final Integer entityUid;
	private final Date date;

	/**
	 * Default constructor.
	 *
	 * @param entityCode the entity code (store, catalog, etc)
	 * @param entityUid the entity id
	 * @param date the date
	 */
	public CodeUidDateCacheKey(final String entityCode, final Integer entityUid, final Date date) {
		this.entityCode = entityCode;
		this.entityUid = entityUid;
		this.date = date;
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}

		if (!(other instanceof CodeUidDateCacheKey)) {
			return false;
		}

		CodeUidDateCacheKey otherKey = (CodeUidDateCacheKey) other;
		return Objects.equals(entityCode, otherKey.entityCode)
			&& Objects.equals(entityUid, otherKey.entityUid)
			&& Objects.equals(date, otherKey.date);
	}

	@Override
	public int hashCode() {
		return Objects.hash(entityCode, entityUid, date);
	}
}
