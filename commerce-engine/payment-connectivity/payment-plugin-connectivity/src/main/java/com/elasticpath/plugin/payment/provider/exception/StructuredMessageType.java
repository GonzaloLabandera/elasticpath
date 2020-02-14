/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider.exception;

/**
 * Represents data type of the plugin exceptions.
 */
public enum StructuredMessageType {
	/**
	 * Error type id.
	 */
	ERROR("ERROR"),
	/**
	 * Warning type id.
	 */
	WARNING("WARNING"),
	/**
	 * Information type id.
	 */
	INFORMATION("INFORMATION"),
	/**
	 * Promotion type id.
	 */
	PROMOTION("PROMOTION"),
	/**
	 * Needinfo type id.
	 */
	NEEDINFO("NEEDINFO");

	private String errorType;

	/**
	 * Constructor.
	 *
	 * @param errorType is type of error.
	 */
	StructuredMessageType(final String errorType) {
		this.errorType = errorType;
	}

	/**
	 * Returns type of error.
	 *
	 * @return type of error.
	 */
	public String errorType() {
		return errorType;
	}
}
