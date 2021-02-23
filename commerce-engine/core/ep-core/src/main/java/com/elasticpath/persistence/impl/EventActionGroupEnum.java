/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.persistence.impl;

/**
 * Represents domain event action grouping, when we want to treat created and updated the same.
 */
public enum EventActionGroupEnum {

	/**
	 * Entity created or updated.
	 */
	CREATED_OR_UPDATED,

	/**
	 * Entity deleted.
	 */
	DELETED
}
