/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.reader;

import java.util.Date;

/**
 * Represents additional restriction of all projections request.
 */
public interface ModifiedSince {
	/**
	 * Return date threshold for projections which have been created or modified.
	 *
	 * @return value for modifiedSince.
	 */
	Date getModifiedSince();

	/**
	 * Return date offset for modifiedSince.
	 *
	 * @return value for modified offset.
	 */
	Long getModifiedSinceOffset();
}
