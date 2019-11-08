/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.customer.impl;

import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.apache.openjpa.persistence.DataCache;
import org.apache.openjpa.persistence.Externalizer;
import org.apache.openjpa.persistence.Factory;
import org.apache.openjpa.persistence.Persistent;

import com.elasticpath.domain.customer.PolicyKey;
import com.elasticpath.domain.customer.StoreCustomerAttribute;
import com.elasticpath.persistence.api.AbstractEntityImpl;

/**
 * StoreCustomerAttributeImpl entity.
 */
@Entity
@Table(name = StoreCustomerAttributeImpl.TABLE_NAME)
@DataCache(enabled = true)
public class StoreCustomerAttributeImpl extends AbstractEntityImpl implements StoreCustomerAttribute {

	private static final long serialVersionUID = -7691745121162193459L;

	/**
	 * The name of the table & generator to use for persistence.
	 */
	public static final String TABLE_NAME = "TSTORECUSTOMERATTRIBUTE";

	private long uidPk;
	private String guid;
	private String storeCode;
	private String attributeKey;
	private PolicyKey policyKey;

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
	@Basic
	@Column(name = "GUID", nullable = false, unique = true)
	public String getGuid() {
		return guid;
	}

	@Override
	public void setGuid(final String guid) {
		this.guid = guid;
	}

	@Basic
	@Column(name = "STORECODE", nullable = false)
	public String getStoreCode() {
		return storeCode;
	}

	@Override
	public void setStoreCode(final String storeCode) {
		this.storeCode = storeCode;
	}

	@Basic
	@Override
	@Column(name = "ATTRIBUTE_KEY")
	public String getAttributeKey() {
		return attributeKey;
	}

	@Override
	public void setAttributeKey(final String attributeKey) {
		this.attributeKey = attributeKey;
	}

	@Override
	@Persistent
	@Column(name = "POLICY_KEY")
	@Externalizer("getName")
	@Factory("valueOf")
	public PolicyKey getPolicyKey() {
		return policyKey;
	}

	@Override
	public void setPolicyKey(final PolicyKey policyKey) {
		this.policyKey = policyKey;
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}
		if (other == null) {
			return false;
		}

		if (other instanceof StoreCustomerAttribute) {
			StoreCustomerAttribute otherStoreCustomerAttribute = (StoreCustomerAttribute) other;
			return getGuid().equals(otherStoreCustomerAttribute.getGuid());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getGuid());
	}
}
