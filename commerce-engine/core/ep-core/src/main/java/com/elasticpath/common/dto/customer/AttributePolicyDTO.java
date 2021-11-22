/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.common.dto.customer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.elasticpath.common.dto.Dto;

/**
 * JAXB DTO of a AttributePolicy.
 */
@XmlRootElement(name = AttributePolicyDTO.ROOT_ELEMENT)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {})
public class AttributePolicyDTO implements Dto {

	private static final long serialVersionUID = 1L;

	/**
	 * Root XML element name.
	 */
	public static final String ROOT_ELEMENT = "attribute_policy";

	@XmlElement(required = true)
	private String guid;

	@XmlElement(name = "policy_key", required = true)
	private String policyKey;

	@XmlElement(name = "policy_permission", required = true)
	private String policyPermission;

	public String getGuid() {
		return guid;
	}

	public void setGuid(final String guid) {
		this.guid = guid;
	}

	public String getPolicyKey() {
		return policyKey;
	}

	public void setPolicyKey(final String policyKey) {
		this.policyKey = policyKey;
	}

	public String getPolicyPermission() {
		return policyPermission;
	}

	public void setPolicyPermission(final String policyPermission) {
		this.policyPermission = policyPermission;
	}

	@Override
	public boolean equals(final Object other) {
		return EqualsBuilder.reflectionEquals(this, other);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}
}
