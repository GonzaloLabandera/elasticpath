/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.domain.customer.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.google.common.base.Objects;
import org.apache.openjpa.persistence.DataCache;
import org.apache.openjpa.persistence.ElementDependent;
import org.apache.openjpa.persistence.FetchAttribute;
import org.apache.openjpa.persistence.FetchGroup;
import org.apache.openjpa.persistence.FetchGroups;
import org.apache.openjpa.persistence.jdbc.ElementForeignKey;
import org.apache.openjpa.persistence.jdbc.ElementJoinColumn;
import org.apache.openjpa.persistence.jdbc.ForeignKey;
import org.springframework.security.core.GrantedAuthority;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.util.PasswordGenerator;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.CustomerProfileValue;
import com.elasticpath.domain.attribute.impl.CustomerProfileValueImpl;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.CustomerAuthentication;
import com.elasticpath.domain.customer.CustomerGroup;
import com.elasticpath.domain.customer.CustomerProfile;
import com.elasticpath.domain.impl.AbstractLegacyEntityImpl;
import com.elasticpath.persistence.support.FetchGroupConstants;

/**
 * The default implementation of <code>Customer</code>.
 */
@Entity
@Table(name = CustomerImpl.TABLE_NAME)
@FetchGroups({
		@FetchGroup(name = FetchGroupConstants.CUSTOMER,
				attributes = { @FetchAttribute(name = "profileValueMap"),
						@FetchAttribute(name = "userId") }),
		@FetchGroup(name = FetchGroupConstants.ORDER_SEARCH,
				attributes = { @FetchAttribute(name = "userId") })
})
@DataCache(enabled = false)
@SuppressWarnings({
		"PMD.UselessOverridingMethod", "PMD.ExcessiveClassLength", "PMD.TooManyMethods", "PMD.ExcessivePublicCount", "PMD.ExcessiveImports",
		"PMD.GodClass" })
public class CustomerImpl extends AbstractLegacyEntityImpl implements Customer {

	private static final int CURRENCY_LENGTH_ISO4217 = 3;

	private static final long serialVersionUID = 5000000001L;

	/**
	 * The name of the table & generator to use for persistence.
	 */
	public static final String TABLE_NAME = "TCUSTOMER";

	/**
	 * System attribute key.
	 */
	public static final String ATT_KEY_CP_FIRST_NAME = "CP_FIRST_NAME";

	/**
	 * System attribute key.
	 */
	public static final String ATT_KEY_CP_LAST_NAME = "CP_LAST_NAME";

	/**
	 * System attribute key.
	 */
	public static final String ATT_KEY_CP_EMAIL = "CP_EMAIL";

	/**
	 * System attribute key.
	 */
	public static final String ATT_KEY_CP_PREF_LOCALE = "CP_PREF_LOCALE";

	/**
	 * System attribute key.
	 */
	public static final String ATT_KEY_CP_PREF_CURR = "CP_PREF_CURR";

	/**
	 * System attribute key.
	 */
	public static final String ATT_KEY_CP_ANONYMOUS_CUST = "CP_ANONYMOUS_CUST";

	/**
	 * System attribute key.
	 */
	public static final String ATT_KEY_CP_PHONE = "CP_PHONE";

	/**
	 * System attribute key.
	 */
	public static final String ATT_KEY_CP_FAX = "CP_FAX";

	/**
	 * System attribute key.
	 */
	public static final String ATT_KEY_CP_GENDER = "CP_GENDER";

	/**
	 * System attribute key.
	 */
	public static final String ATT_KEY_CP_COMPANY = "CP_COMPANY";

	/**
	 * System attribute key.
	 */
	public static final String ATT_KEY_CP_DOB = "CP_DOB";

	/**
	 * System attribute key.
	 */
	public static final String ATT_KEY_CP_HTML_EMAIL = "CP_HTML_EMAIL";

	/**
	 * System attribute key.
	 */
	public static final String ATT_KEY_CP_BE_NOTIFIED = "CP_BE_NOTIFIED";

	/**
	 * System attribute key.
	 */
	public static final String ATT_KEY_CP_BUSINESS_NUMBER = "CP_BUSINESS_NUMBER";

	/**
	 * System attribute key.
	 */
	public static final String ATT_KEY_CP_TAX_EXEMPTION_ID = "CP_TAX_EXEMPTION_ID";

	private static final String CUSTOMER_UID = "CUSTOMER_UID";

	private List<CustomerAddress> addresses = new ArrayList<>();

	private String userId;

	private Date creationDate;

	private Date lastEditDate;

	private List<CustomerGroup> customerGroups = new ArrayList<>();

	private CustomerAddress preferredShippingAddress;

	private CustomerAddress preferredBillingAddress;

	private int status = Customer.STATUS_ACTIVE;

	private CustomerProfile customerProfile;

	private CustomerAuthentication customerAuthentication;

	private String storeCode;

	private long uidPk;

	private String guid;

	private boolean firstTimeBuyer = true;

	@Override
	public void initialize() {
		super.initialize();
		if (getUserId() == null) {
			setUserId(UUID.randomUUID().toString());
		}
		if (getCreationDate() == null) {
			setCreationDate(new Date());
		}
		if (getLastEditDate() == null) {
			setLastEditDate(new Date());
		}
	}

	@Override
	@Basic(optional = false)
	@Column(name = "USER_ID", nullable = false)
	public String getUserId() {
		return userId;
	}

	@Override
	public void setUserId(final String userId) {
		this.userId = userId;
	}

	@Override
	@Transient
	public String getEmail() {
		return getCustomerProfile().getStringProfileValue(ATT_KEY_CP_EMAIL);
	}

	@Override
	@Transient
	public boolean isEmailRequired() {
		return getCustomerProfile().isProfileValueRequired(ATT_KEY_CP_EMAIL);
	}

	/**
	 * {@inheritDoc} <br>
	 * Sets the userId as the email if userId is null.
	 */
	@Override
	public void setEmail(final String email) {
		getCustomerProfile().setStringProfileValue(ATT_KEY_CP_EMAIL, email);
	}

	@Override
	@OneToMany(targetEntity = CustomerAddressImpl.class, cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@ElementJoinColumn(name = CUSTOMER_UID, nullable = false)
	@ElementForeignKey(name = "taddress_ibfk_1")
	@ElementDependent
	public List<CustomerAddress> getAddresses() {
		return addresses;
	}

	@Override
	public void setAddresses(final List<CustomerAddress> addresses) {
		this.addresses = addresses;
	}

	@Override
	public void addAddress(final CustomerAddress address) {
		checkNotNull(address, "address to add must not be null");
		if (!getAddresses().contains(address)) {
			getAddresses().add(address);
		}
	}

	@Override
	public void removeAddress(final CustomerAddress address) {
		checkNotNull(address, "address to remove must not be null");
		getAddresses().remove(address);
		if (Objects.equal(address, getPreferredShippingAddress())) {
			setPreferredShippingAddress(null);
		}
		if (Objects.equal(address, getPreferredBillingAddress())) {
			setPreferredBillingAddress(null);
		}
	}

	@ManyToOne(targetEntity = CustomerAddressImpl.class, fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.REFRESH,
			CascadeType.MERGE })
	@JoinColumn(name = "PREF_SHIP_ADDRESS_UID")
	@ForeignKey(name = "FK_C_SHIPADDRESS", enabled = true)
	protected CustomerAddress getPreferredShippingAddressInternal() {
		return preferredShippingAddress;
	}

	protected void setPreferredShippingAddressInternal(final CustomerAddress preferredShippingAddress) {
		this.preferredShippingAddress = preferredShippingAddress;
	}

	@Override
	public void setPreferredShippingAddress(final CustomerAddress address) {
		if (address != null) {
			addAddress(address);
		}
		setPreferredShippingAddressInternal(address);
	}

	@Override
	@Transient
	public CustomerAddress getPreferredShippingAddress() {
		return getPreferredShippingAddressInternal();
	}

	@ManyToOne(targetEntity = CustomerAddressImpl.class, fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.REFRESH,
			CascadeType.MERGE })
	@JoinColumn(name = "PREF_BILL_ADDRESS_UID")
	@ForeignKey(name = "FK_C_BILLADDRESS", enabled = true)
	protected CustomerAddress getPreferredBillingAddressInternal() {
		return preferredBillingAddress;
	}

	protected void setPreferredBillingAddressInternal(final CustomerAddress preferredBillingAddress) {
		this.preferredBillingAddress = preferredBillingAddress;
	}

	@Override
	public void setPreferredBillingAddress(final CustomerAddress address) {
		if (address != null) {
			addAddress(address);
		}
		setPreferredBillingAddressInternal(address);
	}

	@Override
	@Transient
	public CustomerAddress getPreferredBillingAddress() {
		return getPreferredBillingAddressInternal();
	}

	@Override
	@Transient
	public Locale getPreferredLocale() {
		String localeStr = getCustomerProfile().getStringProfileValue(ATT_KEY_CP_PREF_LOCALE);
		if (localeStr == null || localeStr.length() == 0) {
			return null;
		}
		final StringTokenizer stringTokenizer = new StringTokenizer(localeStr.trim(), "_");
		int size = stringTokenizer.countTokens();
		if (size == 2) { // eg. en_US, or en_
			String language = stringTokenizer.nextToken();
			String country = stringTokenizer.nextToken();
			if (country != null && country.length() > 0) {
				return new Locale(language, country);
			}
		}
		return new Locale(localeStr.trim());
	}

	@Override
	@Transient
	public boolean isPreferredLocaleRequired() {
		return getCustomerProfile().isProfileValueRequired(ATT_KEY_CP_PREF_LOCALE);
	}

	@Override
	public void setPreferredLocale(final Locale preferredLocale) {
		getCustomerProfile().setStringProfileValue(ATT_KEY_CP_PREF_LOCALE, preferredLocale.toString());
	}

	@Override
	@Transient
	public Currency getPreferredCurrency() {
		Currency currency = null;
		String currencyStr = getCustomerProfile().getStringProfileValue(ATT_KEY_CP_PREF_CURR);
		if (currencyStr != null && currencyStr.length() == CURRENCY_LENGTH_ISO4217) {
			currency = Currency.getInstance(currencyStr.trim());
		}
		return currency;
	}

	@Override
	@Transient
	public boolean isPreferredCurrencyRequired() {
		return getCustomerProfile().isProfileValueRequired(ATT_KEY_CP_PREF_CURR);
	}

	@Override
	public void setPreferredCurrency(final Currency preferredCurrency) {
		getCustomerProfile().setStringProfileValue(ATT_KEY_CP_PREF_CURR, preferredCurrency.getCurrencyCode());
	}

	@Override
	@Transient
	public String getFullName() {
		StringBuilder fullName = new StringBuilder();
		if (isAnonymous() && getPreferredBillingAddress() != null) {
			fullName.append(getPreferredBillingAddress().getFirstName());
			fullName.append(' ');
			fullName.append(getPreferredBillingAddress().getLastName());
		} else {
			if (getFirstName() == null) {
				if (getLastName() != null) {
					fullName.append(getLastName());
				}
			} else {
				fullName.append(getFirstName());
				if (getLastName() != null) {
					fullName.append(' ');
					fullName.append(getLastName());
				}
			}
		}
		return fullName.toString();
	}

	@Override
	@Transient
	public String getFirstName() {
		return getCustomerProfile().getStringProfileValue(ATT_KEY_CP_FIRST_NAME);
	}

	@Override
	@Transient
	public boolean isFirstNameRequired() {
		return getCustomerProfile().isProfileValueRequired(ATT_KEY_CP_FIRST_NAME);
	}

	@Override
	public void setFirstName(final String firstName) {
		getCustomerProfile().setStringProfileValue(ATT_KEY_CP_FIRST_NAME, firstName);
	}

	@Override
	@Transient
	public String getLastName() {
		return getCustomerProfile().getStringProfileValue(ATT_KEY_CP_LAST_NAME);
	}

	@Override
	@Transient
	public boolean isLastNameRequired() {
		return getCustomerProfile().isProfileValueRequired(ATT_KEY_CP_LAST_NAME);
	}

	@Override
	public void setLastName(final String lastName) {
		getCustomerProfile().setStringProfileValue(ATT_KEY_CP_LAST_NAME, lastName);
	}

	@Override
	@Transient
	public String getPassword() {
		if (getCustomerAuthentication() == null) {
			return null;
		}
		return getCustomerAuthentication().getPassword();
	}

	@Override
	public void setPassword(final String password, final String salt) {
		initCustomerAuthentication();
		getCustomerAuthentication().setPassword(password);
		getCustomerAuthentication().setSalt(salt);
	}

	@Override
	public void setClearTextPassword(final String clearTextPassword) {
		initCustomerAuthentication();
		getCustomerAuthentication().setClearTextPassword(clearTextPassword);
	}

	@Override
	@Transient
	public String getClearTextPassword() {
		if (getCustomerAuthentication() == null) {
			return null;
		}
		return getCustomerAuthentication().getClearTextPassword();
	}

	@Override
	public String resetPassword() {
		final PasswordGenerator passwordGenerator = getSingletonBean(ContextIdNames.PASSWORD_GENERATOR, PasswordGenerator.class);
		final String newPassword = passwordGenerator.getPassword();
		setClearTextPassword(newPassword);
		return newPassword;
	}

	@Override
	@Transient
	public boolean isAnonymous() {
		Boolean anonymous = (Boolean) getCustomerProfile().getProfileValue(ATT_KEY_CP_ANONYMOUS_CUST);

		return Optional.ofNullable(anonymous).orElse(false);
	}

	@Override
	@Transient
	public boolean isRegistered() {
		return isPersisted() && !isAnonymous();
	}

	@Override
	public void setAnonymous(final boolean anonymous) {
		getCustomerProfile().setProfileValue(ATT_KEY_CP_ANONYMOUS_CUST, anonymous);
	}

	@Override
	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATION_DATE", nullable = false)
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public void setCreationDate(final Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_EDIT_DATE", nullable = false)
	public Date getLastEditDate() {
		return lastEditDate;
	}

	@Override
	public void setLastEditDate(final Date lastEditDate) {
		this.lastEditDate = lastEditDate;
	}

	@Override
	@Transient
	public Date getDateOfBirth() {
		return (Date) getCustomerProfile().getProfileValue(ATT_KEY_CP_DOB);
	}

	@Override
	public void setDateOfBirth(final Date dateOfBirth) {
		getCustomerProfile().setProfileValue(ATT_KEY_CP_DOB, dateOfBirth);
	}

	@Override
	@ManyToMany(targetEntity = CustomerGroupImpl.class, cascade = { CascadeType.REFRESH, CascadeType.MERGE },
			fetch = FetchType.EAGER)
	@JoinTable(name = "TCUSTOMERGROUPX",
			joinColumns = @JoinColumn(name = CUSTOMER_UID, nullable = false),
			inverseJoinColumns = @JoinColumn(name = "CUSTOMERGROUP_UID", nullable = false)
	)
	public List<CustomerGroup> getCustomerGroups() {
		return customerGroups;
	}

	@Override
	public void setCustomerGroups(final List<CustomerGroup> customerGroups) {
		this.customerGroups = customerGroups;
	}

	@Override
	public boolean belongsToCustomerGroup(final long customerGroupID) {
		boolean status = false;
		if (getCustomerGroups() != null && !getCustomerGroups().isEmpty()) {
			for (int i = 0; i < getCustomerGroups().size(); i++) {
				if (customerGroupID == getCustomerGroups().get(i).getUidPk()) {
					status = true;
					break;
				}
			}

		}
		return status;
	}

	@Override
	public boolean belongsToCustomerGroup(final String groupName) {
		boolean status = false;
		if (groupName != null && getCustomerGroups() != null && !getCustomerGroups().isEmpty()) {
			for (int i = 0; i < getCustomerGroups().size(); i++) {
				if (groupName.equals(getCustomerGroups().get(i).getName())) {
					status = true;
					break;
				}
			}

		}
		return status;
	}

	@Override
	public void addCustomerGroup(final CustomerGroup customerGroup) {
		checkNotNull(customerGroup, "customerGroup argument was null");

		if (!getCustomerGroups().contains(customerGroup)) {
			getCustomerGroups().add(customerGroup);
		}
	}

	@Override
	public void removeCustomerGroup(final CustomerGroup customerGroup) {
		checkNotNull(customerGroup, "customerGroup argument was null");

		getCustomerGroups().remove(customerGroup);
	}

	@Override
	@Transient
	public String getPhoneNumber() {
		return getCustomerProfile().getStringProfileValue(ATT_KEY_CP_PHONE);
	}

	@Override
	@Transient
	public boolean isPhoneNumberRequired() {
		return getCustomerProfile().isProfileValueRequired(ATT_KEY_CP_PHONE);
	}

	@Override
	public void setPhoneNumber(final String phoneNumber) {
		getCustomerProfile().setStringProfileValue(ATT_KEY_CP_PHONE, phoneNumber);
	}

	@Override
	@Transient
	public char getGender() {
		char gender = GENDER_NOT_SELECTED;
		String genderString = getCustomerProfile().getStringProfileValue(ATT_KEY_CP_GENDER);
		if (genderString != null && genderString.length() == 1) { // there should be exactly one character
			gender = genderString.charAt(0);
		}
		return gender;
	}

	@Override
	@Transient
	public boolean isGenderRequired() {
		return getCustomerProfile().isProfileValueRequired(ATT_KEY_CP_GENDER);
	}

	@Override
	public void setGender(final char gender) {
		getCustomerProfile().setStringProfileValue(ATT_KEY_CP_GENDER, String.valueOf(gender));
	}

	@Override
	@Transient
	public String getCompany() {
		return getCustomerProfile().getStringProfileValue(ATT_KEY_CP_COMPANY);
	}

	@Override
	@Transient
	public boolean isCompanyRequired() {
		return getCustomerProfile().isProfileValueRequired(ATT_KEY_CP_COMPANY);
	}

	@Override
	public void setCompany(final String company) {
		getCustomerProfile().setStringProfileValue(ATT_KEY_CP_COMPANY, company);
	}

	@Override
	@Basic
	@Column(name = "STATUS")
	public int getStatus() {
		return status;
	}

	@Override
	public void setStatus(final int status) {
		// for JPA setter
		if (status == 0) {
			return;
		}
		checkArgument(
				status == Customer.STATUS_ACTIVE || status == Customer.STATUS_DISABLED || status == Customer.STATUS_PENDING_APPROVAL,
				"status must be %s, %s, or %s, but was %s",
				Customer.STATUS_ACTIVE, Customer.STATUS_DISABLED, Customer.STATUS_PENDING_APPROVAL, status);
		this.status = status;
	}

	@Override
	@Transient
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	@Transient
	public boolean isAccountNonLocked() {
		return getStatus() == Customer.STATUS_ACTIVE;
	}

	@Override
	@Transient
	public Collection<GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> grantedAuthority = new ArrayList<>();
		for (final CustomerGroup customerGroup : getCustomerGroups()) {
			grantedAuthority.addAll(customerGroup.getCustomerRoles());
		}
		return grantedAuthority;
	}

	@Override
	@Transient
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	@Transient
	public boolean isEnabled() {
		return getStatus() == Customer.STATUS_ACTIVE;
	}

	/**
	 * Returns the username used to authenticate the user. Cannot return <code>null</code>.
	 *
	 * @return the username (never <code>null</code>)
	 */
	@Override
	@Transient
	public String getUsername() {
		return getUserId();
	}

	@Override
	@Transient
	public CustomerAddress getAddressByUid(final long addressUid) {
		for (CustomerAddress currAddress : getAddresses()) {
			if (currAddress.getUidPk() == addressUid) {
				return currAddress;
			}
		}
		return null;
	}

	@Override
	@Transient
	public CustomerAddress getAddressByGuid(final String addressGuid) {
		for (Object element : getAddresses()) {
			CustomerAddress currAddress = (CustomerAddress) element;
			if (currAddress.getGuid() != null && currAddress.getGuid().equals(addressGuid)) {
				return currAddress;
			}
		}
		return null;
	}

	@Override
	@Transient
	public CustomerProfile getCustomerProfile() {
		if (customerProfile == null) {
			initializeCustomerProfile();
		}
		return customerProfile;
	}

	/**
	 * Initializes the customer profile bean.  Extend CustomerImpl and override this method if you need to
	 * change the type of either CustomerProfileImpl or CustomerProfileValueImpl.
	 */
	protected void initializeCustomerProfile() {
		customerProfile = new CustomerProfileImpl();
		customerProfile.setProfileValueMap(new HashMap<>());
	}

	@Override
	public void setCustomerProfile(final CustomerProfile customerProfile) {
		this.customerProfile = customerProfile;
	}

	/**
	 * Sets the customer profile attribute metadata.  This metadata is required to set profile attributes,
	 * like email, name, etc.  For new customer objects, the metadata is set by the bean factory (see prototypes.xml).
	 * For existing customers, the metadata is set by OpenJPA via the {@link com.elasticpath.service.customer.impl.CustomerPostLoadStrategy}.
	 *
	 * @param attributes the attribute metadata
	 */
	public void setCustomerProfileAttributes(final Map<String, Attribute> attributes) {
		((CustomerProfileImpl) getCustomerProfile()).setCustomerProfileAttributeMap(attributes);
	}

	@Override
	@OneToOne(targetEntity = CustomerAuthenticationImpl.class, cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@JoinColumn(name = "AUTHENTICATION_UID")
	public CustomerAuthentication getCustomerAuthentication() {
		return customerAuthentication;
	}

	@Override
	public void setCustomerAuthentication(final CustomerAuthentication customerAuthentication) {
		this.customerAuthentication = customerAuthentication;
	}

	private void initCustomerAuthentication() {
		if (getCustomerAuthentication() == null) {
			setCustomerAuthentication(getPrototypeBean(ContextIdNames.CUSTOMER_AUTHENTICATION, CustomerAuthentication.class));
		}
	}

	@Override
	@Transient
	public boolean isToBeNotified() {
		Boolean toBeNotified = (Boolean) getCustomerProfile().getProfileValue(ATT_KEY_CP_BE_NOTIFIED);
		return Optional.ofNullable(toBeNotified).orElse(false);
	}

	@Override
	public void setToBeNotified(final boolean toBeNotified) {
		getCustomerProfile().setProfileValue(ATT_KEY_CP_BE_NOTIFIED, toBeNotified);
	}

	@Override
	@OneToMany(targetEntity = CustomerProfileValueImpl.class, cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@MapKey(name = "localizedAttributeKey")
	@ElementDependent
	@ElementJoinColumn(name = CUSTOMER_UID)
	@ElementForeignKey(name = "tcustomerprofilevalue_ibfk_2")
	public Map<String, CustomerProfileValue> getProfileValueMap() {
		return getCustomerProfile().getProfileValueMap();
	}

	@Override
	public void setProfileValueMap(final Map<String, CustomerProfileValue> profileValueMap) {
		getCustomerProfile().setProfileValueMap(profileValueMap);
	}

	@Override
	@Basic
	@Column(name = "GUID")
	public String getGuid() {
		return guid;
	}

	@Override
	public void setGuid(final String guid) {
		this.guid = guid;
	}

	@Override
	@Transient
	public boolean isHtmlEmailPreferred() {
		Boolean isHtmlEmailPreferred = (Boolean) getCustomerProfile().getProfileValue(ATT_KEY_CP_HTML_EMAIL);

		return Optional.ofNullable(isHtmlEmailPreferred).orElse(false);
	}

	@Override
	public void setHtmlEmailPreferred(final boolean isHtmlEmail) {
		getCustomerProfile().setProfileValue(ATT_KEY_CP_HTML_EMAIL, isHtmlEmail);
	}

	@Override
	@Basic
	@Column(name = "STORECODE", nullable = false)
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
	@TableGenerator(
			name = TABLE_NAME,
			table = "JPA_GENERATED_KEYS",
			pkColumnName = "ID",
			valueColumnName = "LAST_VALUE",
			pkColumnValue = TABLE_NAME,
			allocationSize = HIGH_CONCURRENCY_ALLOCATION_SIZE)
	public long getUidPk() {
		return uidPk;
	}

	@Override
	public void setUidPk(final long uidPk) {
		this.uidPk = uidPk;
	}

	@Override
	@Transient
	public String getFaxNumber() {
		return getCustomerProfile().getStringProfileValue(ATT_KEY_CP_FAX);
	}

	@Override
	@Transient
	public boolean isFaxNumberRequired() {
		return getCustomerProfile().isProfileValueRequired(ATT_KEY_CP_FAX);
	}

	@Override
	public void setFaxNumber(final String faxNumber) {
		getCustomerProfile().setStringProfileValue(ATT_KEY_CP_FAX, faxNumber);
	}

	@Override
	@Transient
	public Date getLastModifiedDate() {
		return getLastEditDate();
	}

	@Override
	public void setLastModifiedDate(final Date lastModifiedDate) {
		setLastEditDate(lastModifiedDate);
	}

	@Override
	public CustomerRoleMapper getCustomerRoleMapper() {
		return new CustomerRoleMapper(this);
	}

	@Override
	@Transient
	public String getBusinessNumber() {
		return getCustomerProfile().getStringProfileValue(ATT_KEY_CP_BUSINESS_NUMBER);
	}

	@Override
	public void setBusinessNumber(final String businessNumber) {
		getCustomerProfile().setStringProfileValue(ATT_KEY_CP_BUSINESS_NUMBER, businessNumber);
	}

	@Override
	@Transient
	public String getTaxExemptionId() {
		return getCustomerProfile().getStringProfileValue(ATT_KEY_CP_TAX_EXEMPTION_ID);
	}

	@Override
	public void setTaxExemptionId(final String taxExemptionId) {
		getCustomerProfile().setStringProfileValue(ATT_KEY_CP_TAX_EXEMPTION_ID, taxExemptionId);
	}

	@Override
	@Column(name = "IS_FIRST_TIME_BUYER")
	public boolean isFirstTimeBuyer() {
		return this.firstTimeBuyer;
	}

	@Override
	public void setFirstTimeBuyer(final boolean firstTimeBuyer) {
		this.firstTimeBuyer = firstTimeBuyer;
	}


}
