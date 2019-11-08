/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.persistence.api;

/**
 * Persistence constants.
 */
public final class PersistenceConstants {

	/** the name of the placeholder used in JPA queries with IN clause e.g IN (:list). */
	public static final String LIST_PARAMETER_NAME = "list";

	private PersistenceConstants() {
		//empty constructor
	}
}
