/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.search;

import com.elasticpath.commons.util.extenum.AbstractExtensibleEnum;

/**
 * Sort attribute type.
 */
public class SortAttributeGroup extends AbstractExtensibleEnum<SortAttributeGroup> {

	private static final long serialVersionUID = 5000000001L;

	/**
	 * Ordinal for field type.
	 */
	public static final int FIELD_ORDINAL = 0;

	/**
	 * Field type.
	 */
	public static final SortAttributeGroup FIELD_TYPE = new SortAttributeGroup(FIELD_ORDINAL, "FIELD", SortAttributeGroup.class);

	/**
	 * Ordinal for attribute type.
	 */
	public static final int ATTRIBUTE_ORDINAL = 1;

	/**
	 * Attribute type.
	 */
	public static final SortAttributeGroup ATTRIBUTE_TYPE = new SortAttributeGroup(ATTRIBUTE_ORDINAL, "ATTRIBUTE", SortAttributeGroup.class);

	/**
	 * Constructor.
	 * @param ordinal the ordinal value
	 * @param name the named value
	 * @param klass the Class Type
	 */
	public SortAttributeGroup(final int ordinal, final String name, final Class<SortAttributeGroup> klass) {
		super(ordinal, name, klass);
	}

	/**
	 * Find the enum value with the specified name.
	 * @param name the name
	 * @return the enum value
	 */
	public static SortAttributeGroup valueOf(final String name) {
		return valueOf(name, SortAttributeGroup.class);
	}


	@Override
	protected Class<SortAttributeGroup> getEnumType() {
		return SortAttributeGroup.class;
	}
}
