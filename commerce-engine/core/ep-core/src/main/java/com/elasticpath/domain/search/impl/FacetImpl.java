/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.domain.search.impl;

import java.util.Map;
import java.util.SortedSet;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import org.apache.openjpa.persistence.DataCache;

import com.elasticpath.common.dto.search.RangeFacet;
import com.elasticpath.commons.constants.GlobalConstants;
import com.elasticpath.domain.search.Facet;
import com.elasticpath.persistence.api.AbstractPersistableImpl;

/**
 * Default implementation of {@link Facet}.
 */
@Entity
@Table(name = FacetImpl.TABLE_NAME)
@DataCache(enabled = false)
public class FacetImpl extends AbstractPersistableImpl implements Facet {

	private static final long serialVersionUID = 5000000001L;

	/**
	 * The name of the table & generator to use for persistence.
	 */
	public static final String TABLE_NAME = "TFACET";

	private long uidPk;
	private String facetGuid;
	private String businessObjectId;
	private String facetName;
	private Integer fieldKeyType;
	private String storeCode;
	private String displayName;
	private Integer facetType;
	private Boolean searchableOption;
	private String rangeFacetValues;
	private Integer facetGroup;
	private SortedSet<RangeFacet> sortedRangeFacet;
	private Map<String, String> displayNameMap;

	/**
	 * Gets the unique identifier for this domain model object.
	 *
	 * @return the unique identifier.
	 */
	@Override
	@Id
	@Column(name = "UIDPK")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = TABLE_NAME)
	@TableGenerator(name = TABLE_NAME, table = "JPA_GENERATED_KEYS", pkColumnName = "ID", valueColumnName = "LAST_VALUE", pkColumnValue = TABLE_NAME)
	public long getUidPk() {
		return uidPk;
	}

	/**
	 * Sets the unique identifier for this domain model object.
	 *
	 * @param uidPk the new unique identifier.
	 */
	@Override
	public void setUidPk(final long uidPk) {
		this.uidPk = uidPk;
	}

	@Override
	@Basic(optional = false)
	@Column(name = "FACET_NAME", unique = true)
	public String getFacetName() {
		return facetName;
	}

	@Override
	public void setFacetName(final String facetName) {
		this.facetName = facetName;
	}

	@Override
	@Basic(optional = false)
	@Column(name = "FIELD_KEY_TYPE")
	public Integer getFieldKeyType() {
		return fieldKeyType;
	}

	@Override
	public void setFieldKeyType(final Integer fieldKeyType) {
		this.fieldKeyType = fieldKeyType;
	}

	@Override
	@Basic(optional = false)
	@Column(name = "DISPLAY_NAME", length = GlobalConstants.MEDIUM_TEXT_MAX_LENGTH)
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public void setDisplayName(final String displayName) {
		this.displayName = displayName;
	}

	@Override
	@Basic(optional = false)
	@Column(name = "FACET_TYPE")
	public Integer getFacetType() {
		return facetType;
	}

	public void setFacetType(final Integer facetType) {
		this.facetType = facetType;
	}

	@Override
	@Basic(optional = false)
	@Column(name = "SEARCHABLE_OPTION")
	public Boolean getSearchableOption() {
		return searchableOption;
	}

	@Override
	public void setSearchableOption(final Boolean searchableOption) {
		this.searchableOption = searchableOption;
	}

	@Override
	@Column(name = "RANGE_FACET_VALUES", length = GlobalConstants.MEDIUM_LONG_TEXT_MAX_LENGTH)
	public String getRangeFacetValues() {
		return rangeFacetValues;
	}

	@Override
	public void setRangeFacetValues(final String rangeFacetValues) {
		this.rangeFacetValues = rangeFacetValues;
	}

	@Override
	@Transient
	public SortedSet<RangeFacet> getSortedRangeFacet() {
		return sortedRangeFacet;
	}

	@Override
	public void setSortedRangeFacet(final SortedSet<RangeFacet> sortedRangeFacet) {
		this.sortedRangeFacet = sortedRangeFacet;
	}

	@Override
	@Basic(optional = false)
	@Column(name = "STORECODE", unique = true)
	public String getStoreCode() {
		return storeCode;
	}

	@Override
	public void setStoreCode(final String storeCode) {
		this.storeCode = storeCode;
	}

	@Override
	@Basic(optional = false)
	@Column(name = "FACET_GROUP")
	public Integer getFacetGroup() {
		return facetGroup;
	}

	@Override
	public void setFacetGroup(final Integer facetGroup) {
		this.facetGroup = facetGroup;
	}

	@Override
	@Transient
	public Map<String, String> getDisplayNameMap() {
		return displayNameMap;
	}

	@Override
	public void setDisplayNameMap(final Map<String, String> displayNameMap) {
		this.displayNameMap = displayNameMap;
	}

	@Override
	@Basic(optional = false)
	@Column(name = "FACET_GUID", unique = true)
	public String getFacetGuid() {
		return facetGuid;
	}

	@Override
	public void setFacetGuid(final String facetGuid) {
		this.facetGuid = facetGuid;
	}

	@Override
	@Basic(optional = false)
	@Column(name = "BUSINESS_OBJECT_ID")
	public String getBusinessObjectId() {
		return businessObjectId;
	}

	@Override
	public void setBusinessObjectId(final String businessObjectId) {
		this.businessObjectId = businessObjectId;
	}
}