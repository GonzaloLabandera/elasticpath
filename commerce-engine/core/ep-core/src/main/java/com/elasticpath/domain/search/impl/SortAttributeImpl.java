/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.domain.search.impl;

import java.util.Map;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.apache.openjpa.persistence.Externalizer;
import org.apache.openjpa.persistence.Factory;
import org.apache.openjpa.persistence.Persistent;

import com.elasticpath.domain.search.SortAttribute;
import com.elasticpath.domain.search.SortAttributeGroup;
import com.elasticpath.domain.search.SortAttributeType;
import com.elasticpath.domain.search.SortLocalizedName;
import com.elasticpath.persistence.api.AbstractPersistableImpl;

/**
 * Default implementation of SortAttribute.
 */
@Entity
@Table(name = SortAttributeImpl.TABLE_NAME)
public class SortAttributeImpl extends AbstractPersistableImpl implements SortAttribute {

	private static final long serialVersionUID = 5000000001L;

	/**
	 * Table name for sort attribute.
	 */
	public static final String TABLE_NAME = "TSORTATTRIBUTE";

	private long uidPk;

	private String guid;

	private String businessObjectId;

	private String storeCode;

	private boolean descending;

	private boolean defaultAttribute;

	private SortAttributeType sortAttributeType;

	private Map<String, SortLocalizedName> localizedNames;

	private SortAttributeGroup sortAttributeGroup;

	@Override
	@Basic(optional = false)
	@Column(name = "GUID", unique = true)
	public String getGuid() {
		return guid;
	}

	@Override
	public void setGuid(final String guid) {
		this.guid = guid;
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

	@Override
	@Basic(optional = false)
	@Column(name = "STORECODE")
	public String getStoreCode() {
		return storeCode;
	}

	@Override
	public void setStoreCode(final String storeCode) {
		this.storeCode = storeCode;
	}

	@Override
	@Id
	@Column(name = "UIDPK")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = TABLE_NAME)
	@TableGenerator(name = TABLE_NAME, table = "JPA_GENERATED_KEYS", pkColumnName = "ID", valueColumnName = "LAST_VALUE", pkColumnValue = TABLE_NAME)
	public long getUidPk() {
		return uidPk;
	}

	@Override
	public void setUidPk(final long uidPk) {
		this.uidPk = uidPk;
	}

	@Override
	@Column(name = "IS_DESCENDING")
	public boolean isDescending() {
		return descending;
	}

	@Override
	public void setDescending(final boolean descending) {
		this.descending = descending;
	}

	@Override
	@OneToMany(targetEntity = SortLocalizedNameImpl.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@MapKey(name = "localeCode")
	@JoinColumn(name = "SORT_ATTRIBUTE_UID", nullable = false)
	public Map<String, SortLocalizedName> getLocalizedNames() {
		return localizedNames;
	}

	@Override
	public void setLocalizedNames(final Map<String, SortLocalizedName> localizedNames) {
		this.localizedNames = localizedNames;
	}

	@Override
	@Persistent(optional = false)
	@Column(name = "SORT_ATTRIBUTE_GROUP")
	@Externalizer("getName")
	@Factory("valueOf")
	public SortAttributeGroup getSortAttributeGroup() {
		return sortAttributeGroup;
	}

	@Override
	public void setSortAttributeGroup(final SortAttributeGroup sortAttributeGroup) {
		this.sortAttributeGroup = sortAttributeGroup;
	}

	@Override
	@Persistent(optional = false)
	@Column(name = "SORT_ATTRIBUTE_TYPE")
	@Externalizer("getName")
	@Factory("valueOf")
	public SortAttributeType getSortAttributeType() {
		return sortAttributeType;
	}

	public void setSortAttributeType(final SortAttributeType sortAttributeType) {
		this.sortAttributeType = sortAttributeType;
	}

	@Override
	@Column(name = "IS_DEFAULT_ATTRIBUTE")
	public boolean isDefaultAttribute() {
		return defaultAttribute;
	}

	@Override
	public void setDefaultAttribute(final boolean defaultAttribute) {
		this.defaultAttribute = defaultAttribute;
	}
}
