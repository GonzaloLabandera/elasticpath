/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.common.dto.sort;

import static com.elasticpath.common.dto.sort.SortAttributeDTOConstants.BUSINESS_OBJECT_ID;
import static com.elasticpath.common.dto.sort.SortAttributeDTOConstants.DEFAULT_ATTRIBUTE;
import static com.elasticpath.common.dto.sort.SortAttributeDTOConstants.DESCENDING;
import static com.elasticpath.common.dto.sort.SortAttributeDTOConstants.DISPLAY_VALUE;
import static com.elasticpath.common.dto.sort.SortAttributeDTOConstants.SORT_ATTRIBUTE_GROUP;
import static com.elasticpath.common.dto.sort.SortAttributeDTOConstants.SORT_ATTRIBUTE_GUID;
import static com.elasticpath.common.dto.sort.SortAttributeDTOConstants.SORT_ATTRIBUTE_TYPE;
import static com.elasticpath.common.dto.sort.SortAttributeDTOConstants.STORE_CODE;
import static com.elasticpath.common.dto.sort.SortAttributeDTOConstants.VALUE;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.elasticpath.common.dto.DisplayValue;
import com.elasticpath.common.dto.Dto;

/**
 * DTO for Sort Attribute.
 */
@XmlRootElement(name = SortAttributeDTO.ROOT_ELEMENT)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = { })
public class SortAttributeDTO implements Dto {

	private static final long serialVersionUID = 20090928L;

	/** The name of root element in XML representation. */
	public static final String ROOT_ELEMENT = "sort_attribute";

	@XmlElement(name = SORT_ATTRIBUTE_GUID, required = true)
	private String sortAttributeGuid;

	@XmlElement(name = BUSINESS_OBJECT_ID, required = true)
	private String businessObjectId;

	@XmlElement(name = STORE_CODE, required = true)
	private String storeCode;

	@XmlElement(name = DESCENDING, required = true)
	private boolean descending;

	@XmlElement(name = SORT_ATTRIBUTE_GROUP, required = true)
	private String sortAttributeGroup;

	@XmlElement(name = DEFAULT_ATTRIBUTE, required = true)
	private boolean defaultAttribute;

	@XmlElement(name = SORT_ATTRIBUTE_TYPE, required = true)
	private String sortAttributeType;

	@XmlElementWrapper(name = DISPLAY_VALUE)
	@XmlElement(name = VALUE, required = true)
	private List<DisplayValue> displayValues;

	public String getSortAttributeGuid() {
		return sortAttributeGuid;
	}

	public void setSortAttributeGuid(final String sortAttributeGuid) {
		this.sortAttributeGuid = sortAttributeGuid;
	}

	public String getBusinessObjectId() {
		return businessObjectId;
	}

	public void setBusinessObjectId(final String businessObjectId) {
		this.businessObjectId = businessObjectId;
	}

	public String getStoreCode() {
		return storeCode;
	}

	public void setStoreCode(final String storeCode) {
		this.storeCode = storeCode;
	}

	public boolean isDescending() {
		return descending;
	}

	public void setDescending(final boolean descending) {
		this.descending = descending;
	}

	public String getSortAttributeGroup() {
		return sortAttributeGroup;
	}

	public void setSortAttributeGroup(final String sortAttributeGroup) {
		this.sortAttributeGroup = sortAttributeGroup;
	}

	public List<DisplayValue> getDisplayValues() {
		return displayValues;
	}

	public void setDisplayValues(final List<DisplayValue> displayValues) {
		this.displayValues = displayValues;
	}

	public boolean isDefaultAttribute() {
		return defaultAttribute;
	}

	public void setDefaultAttribute(final boolean defaultAttribute) {
		this.defaultAttribute = defaultAttribute;
	}

	public String getSortAttributeType() {
		return sortAttributeType;
	}

	public void setSortAttributeType(final String sortAttributeType) {
		this.sortAttributeType = sortAttributeType;
	}
}
