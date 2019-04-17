/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.search;

/**
 * Field group type of a facet.
 */
public enum FacetGroup {
	/**
	 * Product attribute.
	 */
	PRODUCT_ATTRIBUTE("Product Attributes", 0),
	/**
	 * Sku attribute.
	 */
	SKU_ATTRIBUTE("SKU Attributes", 1),
	/**
	 * Sku option.
	 */
	SKU_OPTION("SKU Options", 2),
	/**
	 * Others.
	 */
	OTHERS("Others", 3);

	private final String name;
	private final int ordinal;

	/**
	 * Constructor.
	 * @param name name
	 * @param ordinal ordinal
	 */
	FacetGroup(final String name, final int ordinal) {
		this.name = name;
		this.ordinal = ordinal;
	}

	public String getName() {
		return name;
	}

	public int getOrdinal() {
		return ordinal;
	}

	/**
	 * Get the field group with the ordinal.
	 * @param ordinal ordinal
	 * @return field group
	 */
	public static FacetGroup valueOfOrdinal(final int ordinal) {
		for (FacetGroup fieldGroup : values()) {
			if (fieldGroup.getOrdinal() == ordinal) {
				return fieldGroup;
			}
		}
		throw new IllegalArgumentException("Invalid field group ordinal: " + ordinal);
	}
}