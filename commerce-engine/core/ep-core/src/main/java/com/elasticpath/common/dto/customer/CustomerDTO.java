/**
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
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlMixed;
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

	@XmlAttribute(name = "user_id", required = true)
	private String userId;

	@XmlAttribute(name = "store_code", required = true)
	private String storeCode;

	@XmlElement(required = true)
	private int status;

	@XmlElement(required = true)
	private Date creationDate;

	@XmlElement(name = "last_edited", required = true)
	private Date lastEditDate;

	@XmlElement(name = "password", required = true)
	private String password;

	@XmlElement(name = "salt")
	private String salt;

	@XmlElement(name = "first_time_buyer")
	private boolean firstTimeBuyer;

	@XmlElementWrapper(name = "addresses")
	@XmlElement(name = "address")
	private List<AddressDTO> addresses = new ArrayList<>();

	@XmlMixed
	@XmlElementWrapper(name = "payment_methods")
	@XmlElementRefs({
			@XmlElementRef(name = "payment_token", type = PaymentTokenDto.class),
			@XmlElementRef(name = "credit_card", type = CreditCardDTO.class)
	})
	private List<PaymentMethodDto> paymentMethods = new ArrayList<>();

	@XmlElement(name = "default_payment_method")
	private DefaultPaymentMethodDTO defaultPaymentMethod;

	/**
	 * The customer sessions.
	 * @deprecated No replacement as customer sessions are not maintained through the customer domain object. Marked for removal.
	 */
	@XmlElementWrapper(name = "sessions")
	@XmlElement(name = "session")
	@Deprecated
	private List<CustomerSessionDTO> customerSessions;

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

	public String getUserId() {
		return userId;
	}

	public void setUserId(final String userid) {
		userId = userid;
	}

	public String getStoreCode() {
		return storeCode;
	}

	public void setStoreCode(final String storeCode) {
		this.storeCode = storeCode;
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

	/**
	 * Gets the customer sessions.
	 *
	 * @return the customer sessions
	 * @deprecated No replacement as customer sessions are not maintained through the customer domain object. Marked for removal.
	 */
	@Deprecated
	public List<CustomerSessionDTO> getCustomerSessions() {
		return customerSessions;
	}

	/**
	 * Sets the customer sessions.
	 *
	 * @param customerSessions the new customer sessions
	 * @deprecated No replacement as customer sessions are not maintained through the customer domain object. Marked for removal.
	 */
	@Deprecated
	public void setCustomerSessions(final List<CustomerSessionDTO> customerSessions) {
		this.customerSessions = customerSessions;
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

	public List<PaymentMethodDto> getPaymentMethods() {
		return paymentMethods;
	}

	public void setPaymentMethods(final List<PaymentMethodDto> paymentMethods) {
		this.paymentMethods = paymentMethods;
	}

	public DefaultPaymentMethodDTO getDefaultPaymentMethod() {
		return this.defaultPaymentMethod;
	}

	public void setDefaultPaymentMethod(final DefaultPaymentMethodDTO defaultPaymentMethod) {
		this.defaultPaymentMethod = defaultPaymentMethod;
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
