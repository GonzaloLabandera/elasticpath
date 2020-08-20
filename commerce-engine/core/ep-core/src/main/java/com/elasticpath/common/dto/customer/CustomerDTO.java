/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.common.dto.customer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.elasticpath.common.dto.AddressDTO;
import com.elasticpath.common.dto.Dto;

/**
 * JAXB DTO of a Customer.
 */
@XmlRootElement(name = CustomerDTO.ROOT_ELEMENT)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = { })
public class CustomerDTO implements Dto {

	private static final long serialVersionUID = 1L;

	/** Root XML element name. */
	public static final String ROOT_ELEMENT = "customer";

	@XmlAttribute(required = true)
	private String guid;

	@XmlAttribute(name = "shared_id", required = true)
	private String sharedId;

	@XmlAttribute(name = "store_code")
	private String storeCode;

	@XmlAttribute(name = "parent_guid")
	private String parentGuid;

	@XmlElement(name = "customer_type", required = true)
	private String customerType;

	@XmlElement(required = true)
	private int status;

	@XmlElement(required = true)
	private Date creationDate;

	@XmlElement(name = "last_edited", required = true)
	private Date lastEditDate;

	@XmlElement(name = "username")
	private String username;

	@XmlElement(name = "password")
	private String password;

	@XmlElement(name = "salt")
	private String salt;

	@XmlElement(name = "first_time_buyer")
	private boolean firstTimeBuyer;

	@XmlElementWrapper(name = "addresses")
	@XmlElement(name = "address")
	private List<AddressDTO> addresses = new ArrayList<>();

	@XmlElementWrapper(name = "groups")
	@XmlElement(name = "group")
	private List<String> groups = new ArrayList<>();

	@XmlElement(name = "preferred_shipping_address")
	private String preferredShippingAddressGuid;

	@XmlElement(name = "preferred_billing_address")
	private String preferredBillingAddressGuid;

	@XmlElementWrapper(name = "credit_cards")
	@XmlElement(name = "card")
	private List<LegacyCreditCardDTO> creditCards;

	@XmlElementWrapper(name = "customer_profile")
	@XmlElement(name = "profile_value")
	private Set<AttributeValueDTO> profileValues = new HashSet<>();

	public String getGuid() {
		return guid;
	}

	public void setGuid(final String guid) {
		this.guid = guid;
	}

	public String getSharedId() {
		return sharedId;
	}

	public void setSharedId(final String sharedId) {
		this.sharedId = sharedId;
	}

	public String getStoreCode() {
		return storeCode;
	}

	public void setStoreCode(final String storeCode) {
		this.storeCode = storeCode;
	}

	public String getParentGuid() {
		return parentGuid;
	}

	public void setParentGuid(final String parentGuid) {
		this.parentGuid = parentGuid;
	}

	public String getCustomerType() {
		return this.customerType;
	}

	public void setCustomerType(final String customerType) {
		this.customerType = customerType;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(final int status) {
		this.status = status;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(final Date created) {
		creationDate = created;
	}

	public Date getLastEditDate() {
		return lastEditDate;
	}

	public void setLastEditDate(final Date lastEditDate) {
		this.lastEditDate = lastEditDate;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(final String salt) {
		this.salt = salt;
	}

	public List<AddressDTO> getAddresses() {
		return addresses;
	}

	public void setAddresses(final List<AddressDTO> addresses) {
		this.addresses = addresses;
	}

	public List<String> getGroups() {
		return groups;
	}

	public void setGroups(final List<String> groups) {
		this.groups = groups;
	}

	public String getPreferredShippingAddressGuid() {
		return preferredShippingAddressGuid;
	}

	public void setPreferredShippingAddressGuid(final String preferredShippingAddressGuid) {
		this.preferredShippingAddressGuid = preferredShippingAddressGuid;
	}

	public String getPreferredBillingAddressGuid() {
		return preferredBillingAddressGuid;
	}

	public void setPreferredBillingAddressGuid(final String preferredBillingAddressGuid) {
		this.preferredBillingAddressGuid = preferredBillingAddressGuid;
	}

	public List<LegacyCreditCardDTO> getCreditCards() {
		return creditCards;
	}

	public void setCreditCards(final List<LegacyCreditCardDTO> creditCards) {
		this.creditCards = creditCards;
	}

	public Set<AttributeValueDTO> getProfileValues() {
		return profileValues;
	}

	public void setProfileValues(final Set<AttributeValueDTO> profileValues) {
		this.profileValues = profileValues;
	}

	public boolean isFirstTimeBuyer() {
		return firstTimeBuyer;
	}

	public void setFirstTimeBuyer(final boolean firstTimeBuyer) {
		this.firstTimeBuyer = firstTimeBuyer;
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
