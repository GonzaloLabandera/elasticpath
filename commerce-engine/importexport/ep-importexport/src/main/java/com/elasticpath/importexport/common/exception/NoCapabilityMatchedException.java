/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.common.exception;

/**
 * Exception indicating that a capability not match with
 * map values in {@link com.elasticpath.importexport.xa.datasource.ImportExportXaDataSourceCapability}.
 */
public class NoCapabilityMatchedException extends RuntimeException {

	/**
	 * Exception message.
	 */
	public static final String NO_CAPABILITY_FOUND_FOR_DB = "No capability found for database with driver class name = ";

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	/**
	 * Constructor with parameter.
	 *
	 * @param message the reason for this exception.
	 */
	public NoCapabilityMatchedException(final String message) {
		super(message);
	}
}
