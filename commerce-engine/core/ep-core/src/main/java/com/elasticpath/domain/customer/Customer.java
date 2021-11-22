/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.domain.customer;

import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

import org.springframework.security.core.userdetails.UserDetails;

import com.elasticpath.commons.constants.GlobalConstants;
import com.elasticpath.domain.DatabaseCreationDate;
import com.elasticpath.domain.DatabaseLastModifiedDate;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.CustomerProfileValue;
import com.elasticpath.domain.customer.impl.CustomerRoleMapper;
import com.elasticpath.persistence.api.Entity;
import com.elasticpath.validation.constraints.AttributeRequired;
import com.elasticpath.validation.constraints.EpEmail;
import com.elasticpath.validation.constraints.NotBlank;
import com.elasticpath.validation.constraints.RegisteredCustomerAllOrNoneUsernamePasswordSaltFieldsExist;
import com.elasticpath.validation.constraints.RegisteredCustomerPasswordNotBlankWithSize;
import com.elasticpath.validation.constraints.RegisteredCustomerUsernameUniqueByStore;
import com.elasticpath.validation.groups.AccountValidation;
import com.elasticpath.validation.groups.PasswordCheck;
import com.elasticpath.validation.groups.UserValidation;
import com.elasticpath.validation.groups.UsernameUniqueCheck;

/**
 * A <code>Customer</code> is someone with an account in the system for making orders through the Store Front.
 */
@AttributeRequired
@RegisteredCustomerPasswordNotBlankWithSize(
		min = Customer.MINIMUM_PASSWORD_LENGTH, max = GlobalConstants.SHORT_TEXT_MAX_LENGTH, groups = PasswordCheck.class)
@RegisteredCustomerUsernameUniqueByStore(groups = UsernameUniqueCheck.class)
@RegisteredCustomerAllOrNoneUsernamePasswordSaltFieldsExist
@SuppressWarnings("PMD.GodClass")
public interface Customer extends Entity, UserDetails, DatabaseLastModifiedDate, DatabaseCreationDate {

	/** The minimum length of a password. */
	int MINIMUM_PASSWORD_LENGTH = 8;

	/** The status - Active. */
	int STATUS_ACTIVE = 1;

	/** The status - Disabled. */
	int STATUS_DISABLED = 2;

	/** The status - Pending Approval. */
	int STATUS_PENDING_APPROVAL = 3;

	/** The status - Suspended. */
	int STATUS_SUSPENDED = 4;

	/**
	 * Gets the user identifier for this <code>Customer</code>.
	 *
	 * @return the user identifier.
	 * @deprecated use getSharedId instead
	 */
	@Deprecated
	String getUserId();

	/**
	 * Sets the user identifier for this <code>Customer</code>.
	 *
	 * @param userId the new user identifier.
	 * @deprecated use setSharedId instead
	 */
	@Deprecated
	void setUserId(String userId);

	/**
	 * Gets the shared user identifier for this <code>Customer</code>.
	 *
	 * @return the shared user identifier.
	 */
	String getSharedId();

	/**
	 * Sets the shared user identifier for this <code>Customer</code>.
	 *
	 * @param sharedId the new shared user identifier.
	 */
	void setSharedId(String sharedId);

	/**
	 * Sets the username for this <code>Customer</code>.
	 *
	 * @param username the username.
	 */
	void setUsername(String username);

	/**
	 * Gets the email address of this <code>Customer</code>.
	 *
	 * @return the email address.
	 */
	@EpEmail
	String getEmail();

	/**
	 * Sets the email address of this <code>Customer</code>.
	 *
	 * @param email the new email address.
	 */
	void setEmail(String email);

	/**
	 * Gets the <code>Customer</code>'s full name.
	 *
	 * @return the full name.
	 */
	String getFullName();

	/**
	 * Gets the <code>Customer</code>'s first name.
	 *
	 * @return the first name.
	 */
	String getFirstName();

	/**
	 * Sets the <code>Customer</code>'s first name.
	 *
	 * @param firstName the new first name.
	 */
	void setFirstName(String firstName);

	/**
	 * Gets the <code>Customer</code>'s last name.
	 *
	 * @return the last name.
	 */
	String getLastName();

	/**
	 * Sets the <code>Customer</code>'s last name.
	 *
	 * @param lastName the new last name.
	 */
	void setLastName(String lastName);

	/**
	 * Get the username.
	 *
	 * @return the username
	 */
	@Override
	@Size(max = GlobalConstants.SHORT_TEXT_MAX_LENGTH, groups = UserValidation.class)
	String getUsername();

	/**
	 * Gets the encrypted password.
	 *
	 * @return the encrypted password.
	 */
	@Override
	String getPassword();

	/**
	 * Sets the encrypted password. <br>
	 * By default, the clear-text user input password will be encrypted using the SHA1 secure hash algorithm
	 *
	 * @param password the encrypted password.
	 * @param salt the password salt
	 */
	void setPassword(String password, String salt);

	/**
	 * Gets the password salt.
	 *
	 * @return the password salt.
	 */
	String getPasswordSalt();

	/**
	 * Sets the clear-text password. <br>
	 * The password will be encrypted using a secure hash like MD5 or SHA1 and saved as password.
	 *
	 * @param clearTextPassword the clear-text password.
	 */
	void setClearTextPassword(String clearTextPassword);

	/**
	 * Gets the clear-text password (only available at creation time).
	 *
	 * @return the clear-text password.
	 */
	String getClearTextPassword();

	/**
	 * Reset the customer's password.
	 *
	 * @return the reseted password
	 */
	String resetPassword();

	/**
	 * Gets the <code>CustomerAddress</code>es associated with this <code>Customer</code>.
	 * This method is intended for adding addresses different than preferred billing and shipping ones, only during the import process.
	 *
	 * To set preferred billing and shipping addresses use {@link #setPreferredBillingAddress(CustomerAddress)} and
	 * {@link #setPreferredShippingAddress(CustomerAddress)} methods.
	 *
	 * @return the list of addresses.
	 */
	List<CustomerAddress> getTransientAddresses();

	/**
	 * Adds an <code>CustomerAddress</code> to the list of addresses.
	 *
	 * @param address the address to add.
	 */
	void addAddress(CustomerAddress address);

	/**
	 * Removes an <code>CustomerAddress</code> from the list of addresses.
	 *
	 * @param address the address to remove.
	 */
	void removeAddress(CustomerAddress address);

	/**
	 * Set the preferred billing address.
	 *
	 * @param address the <code>CustomerAddress</code>
	 */
	void setPreferredBillingAddress(CustomerAddress address);

	/**
	 * Get the preferred billing address .This {@link Address} will be added to the list of known addresses for the
	 * customer, i.e. it will be added to {@link #getTransientAddresses()}.
	 *
	 * @return the preferred shipping address
	 */
	@Valid
	CustomerAddress getPreferredBillingAddress();

	/**
	 * Set the preferred shipping address. This {@link Address} will be added to the list of known addresses for the
	 * customer, i.e. it will be added to {@link #getTransientAddresses()}.
	 *
	 * @param address the <code>CustomerAddress</code>
	 */
	void setPreferredShippingAddress(CustomerAddress address);

	/**
	 * Get the preferred shipping address.
	 *
	 * @return the preferred shipping address
	 */
	@Valid
	CustomerAddress getPreferredShippingAddress();

	/**
	 * Get the preferred locale of the customer corresponding to the shopping cart.
	 *
	 * @return the <code>Locale</code>
	 */
	Locale getPreferredLocale();

	/**
	 * Set the preferred locale of the customer corresponding to the shopping cart.
	 *
	 * @param preferredLocale the <code>Locale</code>
	 */
	void setPreferredLocale(Locale preferredLocale);

	/**
	 * Get the preferred currency of the customer corresponding to the shopping cart.
	 *
	 * @return the <code>Currency</code>
	 */
	Currency getPreferredCurrency();

	/**
	 * Set the preferred currency of the customer corresponding to the shopping cart.
	 *
	 * @param preferredCurrency the <code>Currency</code>
	 */
	void setPreferredCurrency(Currency preferredCurrency);

	/**
	 * Gets the flag indicating whether this customer is anonymous or not. <br>
	 * Anonymous customers do not have a password and their email address does not need to be unique.
	 *
	 * @return true if the customer is anonymous; otherwise false.
	 * @deprecated use getCustomerType instead
	 */
	@Deprecated
	boolean isAnonymous();

	/**
	 * Gets the flag indicating whether this customer is a 'real' registered customer or not. <br>
	 * Registered customers are not anonymous and have a persisted record in the database.
	 *
	 * @return true if the customer is registered; otherwise false.
	 * @deprecated use getCustomerType instead
	 */
	@Deprecated
	boolean isRegistered();

	/**
	 * Sets the anonymous status of the customer. <br>
	 * Anonymous customers do not have a password and their email address does not need to be unique.
	 *
	 * @param anonymous - true if the customer is anonymous; otherwise false.
	 * @deprecated use setCustomerType instead
	 */
	@Deprecated
	void setAnonymous(boolean anonymous);

	/**
	 * Gets the customer last edit date.
	 *
	 * @return customer last edit date.
	 */
	Date getLastEditDate();

	/**
	 * Sets the customer last edit date.
	 *
	 * @param lastEditDate customer last edit date.
	 */
	void setLastEditDate(Date lastEditDate);

	/**
	 * Gets the customer's date of birth.
	 *
	 * @return customer's date of birth.
	 */
	Date getDateOfBirth();

	/**
	 * Sets the customer's date of birth.
	 *
	 * @param dateOfBirth customer's date of birth.
	 */
	void setDateOfBirth(Date dateOfBirth);

	/**
	 * Get the <code>CustomerGroup</code>s associated with this customer.
	 *
	 * @return list of customerGroups.
	 */
	List<CustomerGroup> getCustomerGroups();

	/**
	 * Sets the <code>CustomerGroup</code>s associated with this <code>Customer</code>.
	 *
	 * @param customerGroups - the list of customerGroups that the current user is in.
	 */
	void setCustomerGroups(List<CustomerGroup> customerGroups);

	/**
	 * Return a boolean that indicates whether the customer belongs to customerGroup with the given customerGroupID.
	 *
	 * @param customerGroupID - customerGroup ID.
	 * @return true if the customer belongs to a customerGroup with the given customerGroupID; otherwise, false.
	 */
	boolean belongsToCustomerGroup(long customerGroupID);

	/**
	 * Return a boolean that indicates whether the customer belongs to customerGroup with the given name.
	 *
	 * @param groupName - customerGroup name.
	 * @return true if the customer belongs to a customerGroup with the given customerGroup name; otherwise, false.
	 */
	boolean belongsToCustomerGroup(String groupName);

	/**
	 * Adds a <code>CustomerGroup</code> to the list of customertGroups.
	 *
	 * @param customerGroup the customerGroup to add.
	 */
	void addCustomerGroup(CustomerGroup customerGroup);

	/**
	 * Removes an <code>CustomerGroup</code> from the list of customerGroups.
	 *
	 * @param customerGroup the customerGroup to remove.
	 */
	void removeCustomerGroup(CustomerGroup customerGroup);

	/**
	 * Gets the phone number associated with this <code>Customer</code>.
	 *
	 * @return the phone number.
	 */
	String getPhoneNumber();

	/**
	 * Sets the phone number associated with this <code>Customer</code>.
	 *
	 * @param phoneNumber the new phone number.
	 */
	void setPhoneNumber(String phoneNumber);

	/**
	 * Gets the company associated with this <code>Customer</code>.
	 *
	 * @return the company.
	 */
	String getCompany();

	/**
	 * Sets the company associated with this <code>Customer</code>.
	 *
	 * @param company the new company.
	 */
	void setCompany(String company);

	/**
	 * Gets the status of this <code>Customer</code>.
	 *
	 * @return the status.
	 */
	int getStatus();

	/**
	 * Sets the status of this <code>Customer</code>.
	 *
	 * @param status the custome's status.
	 */
	void setStatus(int status);

	/**
	 * Returns the customer's address with the matching GUID. <br>
	 * If no matching address is found then null is returned.
	 *
	 * @param addressGuid the guid of the address to be retrieved
	 * @return a <code>CustomerAddress</code> or <code>null</code> if no matching address is found
	 */
	CustomerAddress getAddressByGuid(String addressGuid);

	/**
	 * Get the customer profile.
	 *
	 * @return the domain model's <code>CustomerProfile</code>
	 */
	@Valid
	CustomerProfile getCustomerProfile();

	/**
	 * Set the customer profile.
	 *
	 * @param customerProfile the <code>CustomerProfile</code>
	 */
	void setCustomerProfile(CustomerProfile customerProfile);

	/**
	 * Sets the customer profile attribute metadata.  This metadata is required to set profile attributes,
	 * like email, name, etc.  For new customer objects, the metadata is set by the bean factory (see prototypes.xml).
	 * For existing customers, the metadata is set by OpenJPA via the {@link com.elasticpath.service.customer.impl.CustomerPostLoadStrategy}.
	 *
	 * @param attributes the attribute metadata
	 */
	void setCustomerProfileAttributes(Map<String, Attribute> attributes);

	/**
	 * Get the customer authentication.
	 *
	 * @return the domain model's <code>CustomerAuthentication</code>
	 */
	CustomerAuthentication getCustomerAuthentication();

	/**
	 * Set the customer authentication.
	 *
	 * @param customerAuthentication the <code>CustomerAuthentication</code>
	 */
	void setCustomerAuthentication(CustomerAuthentication customerAuthentication);

	/**
	 * Indicates whether the user wishes to be notified of news.
	 *
	 * @return true if need to be notified, false otherwise
	 */
	boolean isToBeNotified();

	/**
	 * Set whether the user wishes to be notified of news.
	 *
	 * @param toBeNotified set to true to indicate that need to be notified of news
	 */
	void setToBeNotified(boolean toBeNotified);

	/**
	 * Indicates whether the user wishes to receive emails in HTML format.
	 *
	 * @return true if HTML email format is preferred
	 */
	boolean isHtmlEmailPreferred();

	/**
	 * Set whether the user wishes to receive emails in HTML format.
	 *
	 * @param isHtmlEmail set to true to indicate HTML email preference
	 */
	void setHtmlEmailPreferred(boolean isHtmlEmail);

	/**
	 * Get the profile value map.
	 *
	 * @return the map
	 */
	Map<String, CustomerProfileValue> getProfileValueMap();

	/**
	 * Set the profile value map.
	 *
	 * @param profileValueMap the map
	 */
	void setProfileValueMap(Map<String, CustomerProfileValue> profileValueMap);

	/**
	 * @return store code for which the customer is associated
	 */
	@NotNull(groups = UserValidation.class)
	@NotBlank(groups = UserValidation.class)
	@Null(groups = AccountValidation.class)
	String getStoreCode();

	/**
	 * Sets the code for which the customer is associated.
	 *
	 * @param code store to associate with
	 */
	void setStoreCode(String code);

	/**
	 * Gets the fax number associated with this <code>Customer</code>.
	 *
	 * @return the fax number.
	 */
	String getFaxNumber();

	/**
	 * Sets the fax number associated with this <code>Customer</code>.
	 *
	 * @param faxNumber the new fax number.
	 */
	void setFaxNumber(String faxNumber);

	/**
	 * Gets the <code>Customer</code>'s email address required-ness flag.
	 *
	 * @return true if email address is required, false if not required.
	 */
	boolean isEmailRequired();

	/**
	 * Gets the <code>Customer</code>'s preferred locale required-ness flag.
	 *
	 * @return true if preferred locale is required, false if not required.
	 */
	boolean isPreferredLocaleRequired();

	/**
	 * Gets the <code>Customer</code>'s preferred currency required-ness flag.
	 *
	 * @return true if preferred currency is required, false if not required.
	 */
	boolean isPreferredCurrencyRequired();

	/**
	 * Gets the <code>Customer</code>'s first name required-ness flag.
	 *
	 * @return true if first name is required, false if not required.
	 */
	boolean isFirstNameRequired();

	/**
	 * Gets the <code>Customer</code>'s last name required-ness flag.
	 *
	 * @return true if last name is required, false if not required.
	 */
	boolean isLastNameRequired();

	/**
	 * Gets the <code>Customer</code>'s phone number required-ness flag.
	 *
	 * @return true if phone number is required, false if not required.
	 */
	boolean isPhoneNumberRequired();

	/**
	 * Gets the <code>Customer</code>'s company required-ness flag.
	 *
	 * @return true if company is required, false if not required.
	 */
	boolean isCompanyRequired();

	/**
	 * Gets the <code>Customer</code>'s fax number required-ness flag.
	 *
	 * @return true if fax number is required, false if not required.
	 */
	boolean isFaxNumberRequired();

	/**
	 * Gets a {@link CustomerRoleMapper} associated with this Customer.
	 * @return the associated {@link CustomerRoleMapper}
	 */
	CustomerRoleMapper getCustomerRoleMapper();

	/**
	 * Get this customer's business number.
	 *
	 * @return the customer's business number
	 */
	String getBusinessNumber();

	/**
	 * Set the business number belonging to this customer.
	 *
	 * @param businessNumber the customer's business number
	 */
	void setBusinessNumber(String businessNumber);

	/**
	 * Get this customer's tax exemption id.
	 *
	 * @return this customer's tax exemption id
	 */
	String getTaxExemptionId();

	/**
	 * Set the tax exeption id associated with this customer.
	 *
	 * @param taxExemptionId the customer's tax exemption id
	 */
	void setTaxExemptionId(String taxExemptionId);

	/**
	 * Set a flag whether customer is a first time buyer.
	 * @param flag the flag
	 */
	void setFirstTimeBuyer(boolean flag);

	/**
	 * Check if customer is first time buyer.
	 * @return true if customer is a first time buyer
	 */
	boolean isFirstTimeBuyer();

	/**
	 * Get the customer type.
	 * @return the customer type.
	 */
	CustomerType getCustomerType();

	/**
	 * Set customer type.
	 * @param customerType the customer type.
	 */
	void setCustomerType(CustomerType customerType);

	/**
	 * Gets the business name.
	 *
	 * @return the business name.
	 */
	String getBusinessName();

	/**
	 * Set the business name.
	 *
	 * @param businessName the the business name.
	 */
	void setBusinessName(String businessName);

	/**
	 * Gets the business number of account user.
	 *
	 * @return the business number.
	 */
	String getAccountBusinessNumber();

	/**
	 * Set the business number of account user.
	 *
	 * @param businessNumber the the business number.
	 */
	void setAccountBusinessNumber(String businessNumber);

	/**
	 * Gets the phone number of account user.
	 *
	 * @return the phone number.
	 */
	String getAccountPhoneNumber();

	/**
	 * Set the phone number of account user.
	 *
	 * @param phoneNumber the the phone number.
	 */
	void setAccountPhoneNumber(String phoneNumber);

	/**
	 * Gets the fax number of account user.
	 *
	 * @return the fax number.
	 */
	String getAccountFaxNumber();

	/**
	 * Set the fax number of account user.
	 *
	 * @param faxNumber the the fax number.
	 */
	void setAccountFaxNumber(String faxNumber);

	/**
	 * Gets the tax exemption id of account user.
	 *
	 * @return the tax exemption id.
	 */
	String getAccountTaxExemptionId();

	/**
	 * Set the tax exemption id of account user.
	 *
	 * @param taxExemptionId the tax exemption id.
	 */
	void setAccountTaxExemptionId(String taxExemptionId);

	/**
	 * Gets the parent customer guid for this <code>Customer</code>.
	 *
	 * @return the parent customer guid.
	 */
	String getParentGuid();

	/**
	 * Sets the parent customer guid for this <code>Customer</code>.
	 *
	 * @param parentGuid the parent customer guid.
	 */
	void setParentGuid(String parentGuid);
}
