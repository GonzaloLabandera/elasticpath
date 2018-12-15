/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.catalogview;

/**
 * Types of size filters.
 */
public enum SizeType {
	/**
	 * Length.
	 */
	LENGTH("Length"),
	/**
	 * Width.
	 */
	WIDTH ("Width"),
	/**
	 * Height.
	 */
	HEIGHT("Height"),
	/**
	 * Weight.
	 */
	WEIGHT("Weight");

	private String label;

	/**
	 * Constructor.
	 * @param label label
	 */
	SizeType(final String label) {
		this.label = label;
	}

	/**
	 * Returns SizeType given a label.
	 * @param label label
	 * @return SizeType
	 */
	public static SizeType valueOfLabel(final String label) {
		for (SizeType sizeType : values()) {
			if (sizeType.getLabel().equals(label)) {
				return sizeType;
			}
		}
		throw new IllegalArgumentException("Invalid label: " + label);
	}

	public String getLabel() {
		return label;
	}
}