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
 * JAXB DTO of a StoreCustomerAttribute.
 */
@XmlRootElement(name = StoreCustomerAttributeDTO.ROOT_ELEMENT)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {})
public class StoreCustomerAttributeDTO implements Dto {

	private static final long serialVersionUID = 1L;

	/**
	 * Root XML element name.
	 */
	public static final String ROOT_ELEMENT = "store_customer_attribute";

	@XmlElement(required = true)
	private String guid;

	@XmlElement(name = "storecode", required = true)
	private String storeCode;

	@XmlElement(name = "attribute_key", required = true)
	private String attributeKey;

	@XmlElement(name = "policy_key", required = true)
	private String policyKey;

	public String getGuid() {
		return guid;
	}

	public void setGuid(final String guid) {
		this.guid = guid;
	}

	public String getStoreCode() {
		return storeCode;
	}

	public void setStoreCode(final String storeCode) {
		this.storeCode = storeCode;
	}

	public String getAttributeKey() {
		return attributeKey;
	}

	public void setAttributeKey(final String attributeKey) {
		this.attributeKey = attributeKey;
	}

	public String getPolicyKey() {
		return policyKey;
	}

	public void setPolicyKey(final String policyKey) {
		this.policyKey = policyKey;
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
