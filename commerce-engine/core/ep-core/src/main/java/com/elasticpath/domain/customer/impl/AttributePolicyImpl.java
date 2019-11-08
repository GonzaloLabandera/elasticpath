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

import org.apache.openjpa.persistence.Externalizer;
import org.apache.openjpa.persistence.Factory;
import org.apache.openjpa.persistence.Persistent;

import com.elasticpath.domain.customer.AttributePolicy;
import com.elasticpath.domain.customer.PolicyKey;
import com.elasticpath.domain.customer.PolicyPermission;
import com.elasticpath.persistence.api.AbstractEntityImpl;

/**
 * Implementation {@link AttributePolicy}.
 */
@Entity
@Table(name = AttributePolicyImpl.TABLE_NAME)
public class AttributePolicyImpl extends AbstractEntityImpl implements AttributePolicy {

	private static final long serialVersionUID = -142342001510552384L;

	/**
	 * The name of the table & generator to use for persistence.
	 */
	public static final String TABLE_NAME = "TATTRIBUTEPOLICY";

	private long uidPk;
	private String guid;
	private PolicyKey policyKey;
	private PolicyPermission policyPermission;

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

	public void setGuid(final String guid) {
		this.guid = guid;
	}

	@Override
	@Persistent
	@Column(name = "POLICY_KEY")
	@Externalizer("getName")
	@Factory("valueOf")
	public PolicyKey getPolicyKey() {
		return policyKey;
	}

	public void setPolicyKey(final PolicyKey policyKey) {
		this.policyKey = policyKey;
	}

	@Override
	@Persistent
	@Column(name = "POLICY_PERMISSION")
	@Externalizer("getName")
	@Factory("valueOf")
	public PolicyPermission getPolicyPermission() {
		return policyPermission;
	}

	@Override
	public void setPolicyPermission(final PolicyPermission policyPermission) {
		this.policyPermission = policyPermission;
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}
		if (other == null) {
			return false;
		}

		if (other instanceof AttributePolicy) {
			AttributePolicy otherAttributePolicy = (AttributePolicy) other;
			return getGuid().equals(otherAttributePolicy.getGuid());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getGuid());
	}
}
