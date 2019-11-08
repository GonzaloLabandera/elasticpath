/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity;

import java.time.ZonedDateTime;

/**
 * Represents an interface for Projection entity.
 */
public interface Projection {
	/**
	 * Return NameIdentity object for projection.
	 *
	 * @return {@link NameIdentity} object for projection.
	 */
	NameIdentity getIdentity();

	/**
	 * Returns the modified date time.
	 *
	 * @return the modified date time.
	 */
	ZonedDateTime getModifiedDateTime();

	/**
	 * Field has value "true",if projection deleted.
	 *
	 * @return boolean value.
	 */
	boolean isDeleted();

	/**
	 * Returns the disable date time.
	 *
	 * @return the disable date time.
	 */
	ZonedDateTime getDisableDateTime();

}