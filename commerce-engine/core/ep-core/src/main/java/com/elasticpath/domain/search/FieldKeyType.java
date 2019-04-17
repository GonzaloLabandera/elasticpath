/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.search;

/**
 * Field key types.
 */
public enum FieldKeyType {
	/**
	 * Type for short text and long text.
	 */
	STRING("String", 0),
	/**
	 * Integer.
	 */
	INTEGER("Integer", 1),
	/**
	 * Boolean.
	 */
	BOOLEAN("Boolean", 2),
	/**
	 * Decimal.
	 */
	DECIMAL("Decimal", 3);

	private final String name;
	private final int ordinal;

	/**
	 * Constructor.
	 *
	 * @param name name of the field key type
	 * @param ordinal ordinal value of the field key type
	 */
	FieldKeyType(final String name, final int ordinal) {
		this.name = name;
		this.ordinal = ordinal;
	}

	public String getName() {
		return name;
	}

	public int getOrdinal() {
		return ordinal;
	}
}
