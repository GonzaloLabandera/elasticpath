/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.impl;

/**
 * Enum, that represent boolean display value of Offer details translation.
 */
public enum BooleanEnum {
	/**
	 * False display value.
	 */
	FALSE("false"),
	/**
	 * True display value.
	 */
	TRUE("true");
	private final String value;

	/**
	 * Constructor.
	 *
	 * @param value string boolean value.
	 */
	BooleanEnum(final String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "true".equals(value) ? "True" : "False";
	}
}
