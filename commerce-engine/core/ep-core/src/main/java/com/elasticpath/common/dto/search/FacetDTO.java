/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.common.dto.search;

import static com.elasticpath.common.dto.search.FacetDTOConstants.BUSINESS_OBJECT_ID;
import static com.elasticpath.common.dto.search.FacetDTOConstants.DISPLAY_VALUES;
import static com.elasticpath.common.dto.search.FacetDTOConstants.FACET_GROUP;
import static com.elasticpath.common.dto.search.FacetDTOConstants.FACET_GUID;
import static com.elasticpath.common.dto.search.FacetDTOConstants.FACET_NAME;
import static com.elasticpath.common.dto.search.FacetDTOConstants.FACET_TYPE;
import static com.elasticpath.common.dto.search.FacetDTOConstants.FIELD_KEY_TYPE;
import static com.elasticpath.common.dto.search.FacetDTOConstants.RANGE_FACET;
import static com.elasticpath.common.dto.search.FacetDTOConstants.RANGE_FACET_VALUES;
import static com.elasticpath.common.dto.search.FacetDTOConstants.SEARCHABLE_OPTION;
import static com.elasticpath.common.dto.search.FacetDTOConstants.STORE_CODE;
import static com.elasticpath.common.dto.search.FacetDTOConstants.VALUE;

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
 * DTO for Facet.
 */
@XmlRootElement(name = FacetDTO.ROOT_ELEMENT)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = { })
public class FacetDTO implements Dto {

	private static final long serialVersionUID = 20090928L;

	/** The name of root element in XML representation. */
	public static final String ROOT_ELEMENT = "facet";

	@XmlElement(name = FACET_GUID, required = true)
	private String facetGuid;

	@XmlElement(name = BUSINESS_OBJECT_ID, required = true)
	private String businessObjectId;

	@XmlElement(name = FACET_NAME, required = true)
	private String facetName;

	@XmlElement(name = FIELD_KEY_TYPE, required = true)
	private Integer fieldKeyType;

	@XmlElement(name = STORE_CODE, required = true)
	private String storeCode;

	@XmlElementWrapper(name = DISPLAY_VALUES)
	@XmlElement(name = VALUE, required = true)
	private List<DisplayValue> displayValues;

	@XmlElement(name = FACET_TYPE, required = true)
	private Integer facetType;

	@XmlElement(name = SEARCHABLE_OPTION, required = true)
	private Boolean searchableOption;

	@XmlElementWrapper(name = RANGE_FACET_VALUES, required = true)
	@XmlElement(name = RANGE_FACET)
	private List<RangeFacetDTO> rangeFacetValues;

	@XmlElement(name = FACET_GROUP, required = true)
	private Integer facetGroup;

	public String getFacetGuid() {
		return facetGuid;
	}

	public void setFacetGuid(final String facetGuid) {
		this.facetGuid = facetGuid;
	}

	public String getBusinessObjectId() {
		return businessObjectId;
	}

	public void setBusinessObjectId(final String businessObjectId) {
		this.businessObjectId = businessObjectId;
	}

	public String getFacetName() {
		return facetName;
	}

	public void setFacetName(final String facetName) {
		this.facetName = facetName;
	}

	public Integer getFieldKeyType() {
		return fieldKeyType;
	}

	public void setFieldKeyType(final Integer fieldKeyType) {
		this.fieldKeyType = fieldKeyType;
	}

	public String getStoreCode() {
		return storeCode;
	}

	public void setStoreCode(final String storeCode) {
		this.storeCode = storeCode;
	}

	public Integer getFacetType() {
		return facetType;
	}

	public void setFacetType(final Integer facetType) {
		this.facetType = facetType;
	}

	public Boolean getSearchableOption() {
		return searchableOption;
	}

	public void setSearchableOption(final Boolean searchableOption) {
		this.searchableOption = searchableOption;
	}

	public List<RangeFacetDTO> getRangeFacetValues() {
		return rangeFacetValues;
	}

	public void setRangeFacetValues(final List<RangeFacetDTO> rangeFacetValues) {
		this.rangeFacetValues = rangeFacetValues;
	}

	public Integer getFacetGroup() {
		return facetGroup;
	}

	public void setFacetGroup(final Integer facetGroup) {
		this.facetGroup = facetGroup;
	}

	public List<DisplayValue> getDisplayValues() {
		return displayValues;
	}

	public void setDisplayValues(final List<DisplayValue> displayValues) {
		this.displayValues = displayValues;
	}
}
