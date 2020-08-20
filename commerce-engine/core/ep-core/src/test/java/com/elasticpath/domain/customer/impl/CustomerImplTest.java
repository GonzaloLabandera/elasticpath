/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.domain.customer.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.encoding.PasswordEncoder;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.CustomerAuthentication;
import com.elasticpath.domain.customer.CustomerGroup;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.service.security.SaltFactory;
import com.elasticpath.test.factory.CustomerBuilder;

/**
 * Test of the public API of <code>CustomerImpl</code>.
 */
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.TooManyStaticImports", "PMD.GodClass" })
@RunWith(MockitoJUnitRunner.class)
public class CustomerImplTest {
	private static final int INVALID_STATUS = 5;
	private static final String PUBLIC = "PUBLIC";
	private static final String REGISTERED = "REGISTERED";

	private CustomerImpl customerImpl;

	@Mock
	private BeanFactory beanFactory;

	@Mock
	private PasswordEncoder passwordEncoder;

	/**
	 * Prepares for the next test.
	 */
	@Before
	public void setUp()  {

		final CustomerAuthentication customerAuthentication = new CustomerAuthenticationImpl() {
			private static final long serialVersionUID = 740L;

			@Override
			public <T> T getSingletonBean(final String name, final Class<T> clazz) {
				return beanFactory.getSingletonBean(name, clazz);
			}
		};

		when(beanFactory.getPrototypeBean(ContextIdNames.CUSTOMER_AUTHENTICATION, CustomerAuthentication.class)).thenReturn(customerAuthentication);
		when(beanFactory.getSingletonBean(ContextIdNames.PASSWORDENCODER, PasswordEncoder.class)).thenReturn(passwordEncoder);

		customerImpl = CustomerBuilder.newCustomer(beanFactory).build();

		CustomerGroupImpl customerGroupImpl = new CustomerGroupImpl();
		customerGroupImpl.initialize();
		final long uidPk = 100000L;
		customerGroupImpl.setUidPk(uidPk);

		List<CustomerGroup> customerGroupList = new ArrayList<>();
		customerGroupList.add(customerGroupImpl);
		customerImpl.setCustomerGroups(customerGroupList);
		customerImpl.setCustomerType(CustomerType.REGISTERED_USER);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.getSharedId()'.
	 */
	@Test
	public void testGetSharedId() {
		assertThat(customerImpl.getSharedId()).isNotNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.getSharedId(String)'.
	 */
	@Test
	public void testSetSharedId() {
		final String sharedId = "aaaa";
		customerImpl.setSharedId(sharedId);
		assertThat(customerImpl.getSharedId()).isEqualTo(sharedId);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.getUserId()'.
	 */
	@Test
	public void testGetUserId() {
		assertThat(customerImpl.getUserId()).isNotNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.setUserId(String)'.
	 */
	@Test
	public void testSetUserId() {
		final String userId = "aaaa";
		customerImpl.setUserId(userId);
		assertThat(customerImpl.getUserId()).isEqualTo(userId);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.getUsername()'.
	 */
	@Test
	public void testGetUsername() {
		assertThat(customerImpl.getUsername()).isNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.getUsername(String)'.
	 */
	@Test
	public void testSetUsername() {
		final String username = "aaaa";
		customerImpl.setUsername(username);
		assertThat(customerImpl.getUsername()).isEqualTo(username);
	}

	/**
	 * Test method for 'getLocale()'.
	 */
	@Test
	public void testSetGetPreferrredLocale() {
		final Locale locale = Locale.US;
		customerImpl.setPreferredLocale(locale);
		assertThat(customerImpl.getPreferredLocale()).isEqualTo(locale);
	}

	/**
	 * Test method for 'getCurrency()'.
	 */
	@Test
	public void testSetGetPreferredCurrency() {
		final Currency currency = Currency.getInstance(Locale.US);
		customerImpl.setPreferredCurrency(currency);
		assertThat(customerImpl.getPreferredCurrency()).isEqualTo(currency);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.getEmail()'.
	 */
	@Test
	public void testGetEmail() {
		assertThat(customerImpl.getEmail()).as("Check get email").isNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.setEmail(String)'.
	 */
	@Test
	public void testSetEmail() {
		final String[] testData = new String[]{"aaaa@aaa.aaa", "", null};
		for (final String email : testData) {
			customerImpl.setEmail(email);
			assertThat(customerImpl.getEmail()).isEqualTo(email);
		}
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.getAddresses()'.
	 */
	@Test
	public void testGetAddresses() {
		final List<CustomerAddress> addresses = customerImpl.getAddresses();
		assertThat(addresses).isEmpty();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.setAddresss(List)'.
	 */
	@Test
	public void testSetAddresses() {
		final List<CustomerAddress> addressList = new ArrayList<>();
		customerImpl.setAddresses(addressList);
		final List<CustomerAddress> savedAddresses = customerImpl.getAddresses();
		assertThat(savedAddresses).isEqualTo(addressList);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.addAddress(Address)'.
	 */
	@Test
	public void testAddAddress() {
		final CustomerAddress address = new CustomerAddressImpl();
		customerImpl.addAddress(address);
		final List<CustomerAddress> savedAddresses = customerImpl.getAddresses();
		assertThat(savedAddresses).contains(address);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.addAddress(Address)'.
	 */
	@Test(expected = NullPointerException.class)
	public void testAddAddressNull() {
		customerImpl.addAddress(null);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.removeAddress(Address)'.
	 */
	@Test
	public void testRemoveAddress() {
		final CustomerAddress address = new CustomerAddressImpl();
		customerImpl.addAddress(address);
		customerImpl.removeAddress(address);
		final List<CustomerAddress> savedAddresses = customerImpl.getAddresses();
		assertThat(savedAddresses).doesNotContain(address);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.removeAddress(Address)'.
	 */
	@Test(expected = NullPointerException.class)
	public void testRemoveAddressNull() {
		customerImpl.removeAddress(null);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.getFirstName()'.
	 */
	@Test
	public void testGetFirstName() {
		assertThat(customerImpl.getFirstName()).isNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.setFirstName(String)'.
	 */
	@Test
	public void testSetFirstName() {
		final String[] testData = new String[] { "aaaaa", "", null };
		for (final String firstName : testData) {
			customerImpl.setFirstName(firstName);
			assertThat(customerImpl.getFirstName()).isEqualTo(firstName);
		}
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.getLastName()'.
	 */
	@Test
	public void testGetLastName() {
		assertThat(customerImpl.getLastName()).isNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.setLastName(String)'.
	 */
	@Test
	public void testSetLastName() {
		final String[] testData = new String[] { "aaaaaa", "", null };
		for (final String lastName : testData) {
			customerImpl.setLastName(lastName);
			assertThat(customerImpl.getLastName()).isEqualTo(lastName);
		}
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.getPassword()'.
	 */
	@Test
	public void testGetPassword() {
		assertThat(customerImpl.getPassword()).isNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.getPassword()'.
	 */
	@Test
	public void testGetEncryptedPassword() {
		assertThat(customerImpl.getPassword()).isNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.setClearTextPassword(String)'.
	 */
	@Test
	public void testSetClearTextPassword() {
		@SuppressWarnings("unchecked") final SaltFactory<String> saltFactory = mock(SaltFactory.class);
		when(beanFactory.getSingletonBean(ContextIdNames.SALT_FACTORY, SaltFactory.class)).thenReturn(saltFactory);

		final String[] passwords = new String[] { "AbCdEfGhI", "AbCdEfGhIjKlMnOpQrS", "aA123_$@#^&", "", null };
		final String[] hashedPasswords = new String[] { "d60c7aaba158d8270ec509390438152ca931ec6a", "32a6ea3419c4d9653cf51c6500f3accef2012ab0",
				"e9d1d12fbb45ca95c496f3a33a40956c1a4da1ef", null, null };

		final String salt = "SALT";
		for (int i = 0; i < passwords.length; i++) {
			final String password = passwords[i];
			final String hashedPassword = hashedPasswords[i];

			if (!StringUtils.isBlank(password)) {
				when(saltFactory.createSalt()).thenReturn(salt);
				when(passwordEncoder.encodePassword(password, salt)).thenReturn(hashedPassword);
			}

			customerImpl.setClearTextPassword(password);
			assertThat(customerImpl.getPassword()).isEqualTo(hashedPassword);
		}
	}


	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.isAnonymous()'.
	 */
	@Test
	public void testIsAnonymous() {
		assertThat(customerImpl.isAnonymous()).isFalse();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.setAnonymous()'.
	 */
	@Test
	public void testSetAnonymous() {
		customerImpl.setAnonymous(true);
		assertThat(customerImpl.isAnonymous()).isTrue();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.getCreationDate()'.
	 */
	@Test
	public void testGetCreationDate() {
		final Date creationDate = customerImpl.getCreationDate();
		assertThat(creationDate).isNotNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.setCreationDate(Date)'.
	 */
	@Test
	public void testSetCreationDate() {
		final Date creationDate = new Date();
		this.customerImpl.setCreationDate(creationDate);
		assertThat(this.customerImpl.getCreationDate()).isEqualTo(creationDate);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.getLastEditDate()'.
	 */
	@Test
	public void testGetLastEditDate() {
		final Date lastEditDate = customerImpl.getLastEditDate();
		assertThat(lastEditDate).isNotNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.setLastEditDate(Date)'.
	 */
	@Test
	public void testSetLastEditDate() {
		final Date lastEditDate = new Date();
		this.customerImpl.setLastEditDate(lastEditDate);
		assertThat(this.customerImpl.getLastEditDate()).isEqualTo(lastEditDate);
	}

	/**
	 * Test method for setting addresses.
	 */
	@Test
	public void testGetSetAddresses() {
		assertThat(customerImpl.getPreferredBillingAddress()).isNull();
		assertThat(customerImpl.getPreferredShippingAddress()).isNull();

		CustomerAddress address = new CustomerAddressImpl();
		address.setFirstName("Test");

		customerImpl.setPreferredBillingAddress(address);
		assertThat(customerImpl.getPreferredBillingAddress()).isEqualTo(address);
		assertThat(customerImpl.getPreferredShippingAddress()).isNull();

		customerImpl.setPreferredBillingAddress(null);
		customerImpl.setPreferredShippingAddress(address);
		assertThat(customerImpl.getPreferredShippingAddress()).isEqualTo(address);
	}

	/**
	 * Test method for setting customer status.
	 */
	@Test
	public void testSetStatus() {
		this.customerImpl.setStatus(Customer.STATUS_ACTIVE);
		assertThat(this.customerImpl.getStatus()).isEqualTo(Customer.STATUS_ACTIVE);
	}

	/**
	 * Test method for setting customer status.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testSetStatusInvalid() {
		customerImpl.setStatus(INVALID_STATUS);
	}

	/**
	 * Test method for setting customer gender.
	 */
	@Test
	public void testSetGender() {
		this.customerImpl.setGender(Customer.GENDER_FEMALE);
		assertThat(this.customerImpl.getGender()).isEqualTo(Customer.GENDER_FEMALE);
	}

	/**
	 * Test method for setting customer's company.
	 */
	@Test
	public void testSetCompany() {
		this.customerImpl.setCompany(null);
		assertThat(this.customerImpl.getCompany()).isNull();
	}

	/** When setting the preferred billing address, it should be in the list of addresses. */
	@Test
	public void testSetPreferredBillingIsInAddresses() {
		CustomerAddress address = new CustomerAddressImpl();
		assertThat(customerImpl.getAddresses()).doesNotContain(address);

		customerImpl.setPreferredBillingAddress(address);
		assertThat(customerImpl.getAddresses())
			.as("Preferred address should a part of customer addresses")
			.contains(address);
	}

	/** When setting the preferred shipping address, it should be in the list of addresses. */
	@Test
	public void testSetPreferredShippingIsInAddresses() {
		CustomerAddress address = new CustomerAddressImpl();
		assertThat(customerImpl.getAddresses()).doesNotContain(address);

		customerImpl.setPreferredShippingAddress(address);
		assertThat(customerImpl.getAddresses())
			.as("Preferred address should a part of customer addresses")
			.contains(address);
	}

	/** Test get address by Id method. */
	@Test
	public void testGetAddressById() {
		CustomerAddress address1 = new CustomerAddressImpl();
		address1.setUidPk(1);
		address1.setCity("Burnaby");
		CustomerAddress address2 = new CustomerAddressImpl();
		address2.setUidPk(2);
		address2.setCity("Vancouver");
		customerImpl.addAddress(address1);
		customerImpl.addAddress(address2);
		assertThat(customerImpl.getAddressByUid(1)).isEqualTo(address1);
		assertThat(customerImpl.getAddressByUid(2)).isEqualTo(address2);
	}

	/**
	 * Tests setting and retrieving html email preference flag.
	 */
	@Test
	public void testSetHtmlEmailPreferred() {
		assertThat(customerImpl.isHtmlEmailPreferred()).isFalse();
		customerImpl.setHtmlEmailPreferred(true);
		assertThat(customerImpl.isHtmlEmailPreferred()).isTrue();

		customerImpl.setHtmlEmailPreferred(false);
		assertThat(customerImpl.isHtmlEmailPreferred()).isFalse();

	}

	/**
	 * Tests setting and retrieving notification email preference flag.
	 */
	@Test
	public void testSetToBeNotified() {
		assertThat(customerImpl.isToBeNotified()).isFalse();
		customerImpl.setToBeNotified(true);
		assertThat(customerImpl.isToBeNotified()).isTrue();

		customerImpl.setToBeNotified(false);
		assertThat(customerImpl.isToBeNotified()).isFalse();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.setFaxNumber()'.
	 */
	@Test
	public void testSetFaxNumber() {
		String faxNumber = "+1 111 111 888";
		assertThat(customerImpl.getFaxNumber()).isNull();
		customerImpl.setFaxNumber(faxNumber);
		assertThat(customerImpl.getFaxNumber()).isEqualTo(faxNumber);

	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.setPhoneNumber()'.
	 */
	@Test
	public void testSetPhoneNumber() {
		String phoneNumber = "+1 111 111 888";
		assertThat(customerImpl.getPhoneNumber()).isNull();
		customerImpl.setPhoneNumber(phoneNumber);
		assertThat(customerImpl.getPhoneNumber()).isEqualTo(phoneNumber);

	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.belongsToCustomerGroup()'.
	 */
	@Test
	public void testBelongsToCustomerGroup() {
		assertThat(customerImpl.belongsToCustomerGroup(customerImpl.getCustomerGroups().get(0).getUidPk())).isTrue();
	}

	/**
	 * Verify role mapper returns correct roles on customer.
	 */
	@Test
	public void verifyRoleMapperReturnsCorrectRolesOnCustomer() {
		customerImpl.setCustomerType(CustomerType.SINGLE_SESSION_USER);
		CustomerRoleMapper roleMapper = customerImpl.getCustomerRoleMapper();
		assertThat(roleMapper).isNotNull();
		assertThat(roleMapper.hasRole(PUBLIC))
			.as("Customer should have role %s", PUBLIC)
			.isTrue();

		customerImpl.setCustomerType(CustomerType.REGISTERED_USER);
		assertThat(roleMapper.hasRole(REGISTERED))
			.as("Customer should have role %s", REGISTERED)
			.isTrue();
	}

	/**
	 * Test method for setCustomerType.
	 */
	@Test
	public void testSetCustomerType() {
		final CustomerType registered = CustomerType.REGISTERED_USER;
		this.customerImpl.setCustomerType(registered);
		assertThat(this.customerImpl.getCustomerType()).isEqualTo(registered);
	}
}
