/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.domain.search;

/**
 * Class that represents a sort value.
 */
public class SortValue {

	private final String businessObjectId;

	private final boolean descending;

	private final SortAttributeGroup attributeType;

	private final String name;

	/**
	 * Constructor.
	 * @param businessObjectId attribute key or the name of a product field
	 * @param descending true if descending
	 * @param attributeType type of attribute
	 * @param name display name
	 */
	public SortValue(final String businessObjectId, final boolean descending, final SortAttributeGroup attributeType, final String name) {
		this.businessObjectId = businessObjectId;
		this.descending = descending;
		this.attributeType = attributeType;
		this.name = name;
	}

	public String getBusinessObjectId() {
		return businessObjectId;
	}

	public boolean isDescending() {
		return descending;
	}

	public SortAttributeGroup getAttributeType() {
		return attributeType;
	}

	public String getName() {
		return name;
	}
}
