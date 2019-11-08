/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.search;

import com.elasticpath.commons.util.extenum.AbstractExtensibleEnum;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.AttributeType;

/**
 * Sort attribute types.
 */
public class SortAttributeType extends AbstractExtensibleEnum<SortAttributeType> {

	private static final long serialVersionUID = 5000000001L;

	/**
	 * Ordinal for string type.
	 */
	public static final int STRING_ORDINAL = 0;

	/**
	 * String type.
	 */
	public static final SortAttributeType STRING_TYPE = new SortAttributeType(STRING_ORDINAL, "STRING", SortAttributeType.class);

	/**
	 * Ordinal for number type.
	 */
	public static final int NUMBER_ORDINAL = 1;

	/**
	 * Number type.
	 */
	public static final SortAttributeType NUMBER_TYPE = new SortAttributeType(NUMBER_ORDINAL, "NUMBER", SortAttributeType.class);

	/**
	 * Ordinal for boolean type.
	 */
	public static final int BOOLEAN_ORDINAL = 2;

	/**
	 * Boolean type.
	 */
	public static final SortAttributeType BOOLEAN_TYPE = new SortAttributeType(BOOLEAN_ORDINAL, "BOOLEAN", SortAttributeType.class);

	/**
	 * Constructor.
	 * @param ordinal ordinal
	 * @param name name
	 * @param klass class
	 */
	public SortAttributeType(final int ordinal, final String name, final Class<SortAttributeType> klass) {
		super(ordinal, name, klass);
	}

	@Override
	protected Class<SortAttributeType> getEnumType() {
		return SortAttributeType.class;
	}

	/**
	 * Find the enum value with the specified name.
	 * @param name the name
	 * @return the enum value
	 */
	public static SortAttributeType valueOf(final String name) {
		return valueOf(name, SortAttributeType.class);
	}

	/**
	 * Get the sort attribute type of an attribute.
	 * @param attribute attribute
	 * @return type
	 */
	public static SortAttributeType getTypeOfAttribute(final Attribute attribute) {
		int ordinal = attribute.getAttributeType().getOrdinal();

		if (ordinal <= AttributeType.LONG_TEXT.getOrdinal()) {
			return STRING_TYPE;
		} else if (ordinal <= AttributeType.DECIMAL.getOrdinal()) {
			return NUMBER_TYPE;
		} else if (ordinal == AttributeType.BOOLEAN.getOrdinal()) {
			return BOOLEAN_TYPE;
		}

		throw new IllegalArgumentException("Sort attribute type is only valid for strings, numbers, and boolean.");
	}
}
