/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.search;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines the facet types available.
 */
public enum FacetType {
	/**
	 * With Facet.
	 */
	FACET("Facet", 0),
	/**
	 * Facet with ranged values.
	 */
	RANGE_FACET("Range Facet", 1),
	/**
	 * No Facet.
	 */
	NO_FACET("No Facet", 2);

	private final String name;
	private final int ordinal;

	/**
	 * Constructor.
	 *
	 * @param name name
	 * @param ordinal ordinal
	 */
	FacetType(final String name, final int ordinal) {
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
	 * Get Facet type for ordinal.
	 * @param ordinal ordinal
	 * @return the facet type
	 */
	public static FacetType valueOfOrdinal(final int ordinal) {
		for (FacetType facetType : values()) {
			if (facetType.getOrdinal() == ordinal) {
				return facetType;
			}
		}
		throw new IllegalArgumentException("Invalid facet ordinal: " + ordinal);
	}

	/**
	 * Get the FacetType by name.
	 * @param name name
	 * @return FacetType
	 */
	public static FacetType getFacetTypeForName(final String name) {
		for (FacetType facetType : values()) {
			if (facetType.getName().equals(name)) {
				return facetType;
			}
		}
		throw new IllegalArgumentException("Invalid facet name: " + name);
	}

	/**
	 * Get facet types available for the field key type.
	 * @param fieldKeyType field key type
	 * @return facet types
	 */
	public static List<FacetType> getFacetTypesForFieldKeyType(final FieldKeyType fieldKeyType) {
		List<FacetType> facetTypes = new ArrayList<>();
		facetTypes.add(NO_FACET);
		if (fieldKeyType == FieldKeyType.INTEGER || fieldKeyType == FieldKeyType.DECIMAL) {
			facetTypes.add(RANGE_FACET);
		} else {
			facetTypes.add(FACET);
		}
		return facetTypes;
	}
}