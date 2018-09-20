/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.domain.customer.impl;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.hamcrest.Matchers;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.security.authentication.encoding.PasswordEncoder;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.CustomerGroup;
import com.elasticpath.domain.misc.impl.RandomGuidImpl;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.security.SaltFactory;
import com.elasticpath.settings.impl.SettingsServiceImpl;
import com.elasticpath.test.BeanFactoryExpectationsFactory;
import com.elasticpath.test.factory.CustomerBuilder;

/**
 * Test of the public API of <code>CustomerImpl</code>.
 */
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.TooManyStaticImports", "PMD.GodClass" })
public class CustomerImplTest {
	private static final int INVALID_STATUS = 5;
	private static final String PUBLIC = "PUBLIC";
	private static final String REGISTERED = "REGISTERED";

	private CustomerImpl customerImpl;

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();
	private BeanFactoryExpectationsFactory beanExpectations;

	private PasswordEncoder passwordEncoder;

	/**
	 * Prepares for the next test.
	 */
	@Before
	public void setUp()  {
		BeanFactory beanFactory = context.mock(BeanFactory.class);
		beanExpectations = new BeanFactoryExpectationsFactory(context, beanFactory);
		beanExpectations.allowingBeanFactoryGetBean(ContextIdNames.CUSTOMER_AUTHENTICATION, CustomerAuthenticationImpl.class);
		beanExpectations.allowingBeanFactoryGetBean(ContextIdNames.RANDOM_GUID, RandomGuidImpl.class);

		passwordEncoder = context.mock(PasswordEncoder.class);
		beanExpectations.allowingBeanFactoryGetBean(ContextIdNames.PASSWORDENCODER, passwordEncoder);

		customerImpl = CustomerBuilder.newCustomer().build();

		CustomerGroupImpl customerGroupImpl = new CustomerGroupImpl();
		customerGroupImpl.initialize();
		final long uidPk = 100000L;
		customerGroupImpl.setUidPk(uidPk);

		List<CustomerGroup> customerGroupList = new ArrayList<>();
		customerGroupList.add(customerGroupImpl);
		customerImpl.setCustomerGroups(customerGroupList);
	}


	@After
	public void tearDown() {
		beanExpectations.close();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.getUserId()'.
	 */
	@Test
	public void testGetUserId() {
		assertEquals(customerImpl.getUserId(), null);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.setUserId(String)'.
	 */
	@Test
	public void testSetUserId() {
		final String userId = "aaaa";
		customerImpl.setUserId(userId);
		assertSame(customerImpl.getUserId(), userId);
	}


	/**
	 * Test method for 'getLocale()'.
	 */
	@Test
	public void testSetGetPreferrredLocale() {
		final Locale locale = Locale.US;
		customerImpl.setPreferredLocale(locale);
		assertEquals(locale, customerImpl.getPreferredLocale());
	}

	/**
	 * Test method for 'getCurrency()'.
	 */
	@Test
	public void testSetGetPreferredCurrency() {
		final Currency currency = Currency.getInstance(Locale.US);
		customerImpl.setPreferredCurrency(currency);
		assertEquals(currency, customerImpl.getPreferredCurrency());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.getEmail()'.
	 */
	@Test
	public void testGetEmail() {
		assertEquals("Check get email", null, customerImpl.getEmail());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.setEmail(String)'.
	 */
	@Test
	public void testSetEmail() {
		final CustomerService customerService = context.mock(CustomerService.class);
		context.checking(new Expectations() {
			{
				allowing(customerService).getUserIdMode(); will(returnValue(1));
			}
		});
		beanExpectations.allowingBeanFactoryGetBean(ContextIdNames.CUSTOMER_SERVICE, customerService);
		beanExpectations.allowingBeanFactoryGetBean("settingsService", SettingsServiceImpl.class);

		final String[] testData = new String[] { "aaaa@aaa.aaa", "", null };
		for (final String email : testData) {
			customerImpl.setEmail(email);
			assertSame("Check set email", email, customerImpl.getEmail());
		}
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.getAddresses()'.
	 */
	@Test
	public void testGetAddresses() {
		final List<CustomerAddress> addresses = customerImpl.getAddresses();
		assertTrue("Check get addresses", addresses.isEmpty());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.setAddresss(List)'.
	 */
	@Test
	public void testSetAddresses() {
		final List<CustomerAddress> addressList = new ArrayList<>();
		customerImpl.setAddresses(addressList);
		final List<CustomerAddress> savedAddresses = customerImpl.getAddresses();
		assertEquals(addressList, savedAddresses);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.addAddress(Address)'.
	 */
	@Test
	public void testAddAddress() {
		final CustomerAddress address = new CustomerAddressImpl();
		customerImpl.addAddress(address);
		final List<CustomerAddress> savedAddresses = customerImpl.getAddresses();
		assertTrue(savedAddresses.contains(address));
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
		assertFalse(savedAddresses.contains(address));
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
		assertEquals(null, customerImpl.getFirstName());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.setFirstName(String)'.
	 */
	@Test
	public void testSetFirstName() {
		final String[] testData = new String[] { "aaaaa", "", null };
		for (final String firstName : testData) {
			customerImpl.setFirstName(firstName);
			assertSame(firstName, customerImpl.getFirstName());
		}
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.getLastName()'.
	 */
	@Test
	public void testGetLastName() {
		assertEquals(null, customerImpl.getLastName());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.setLastName(String)'.
	 */
	@Test
	public void testSetLastName() {
		final String[] testData = new String[] { "aaaaaa", "", null };
		for (final String lastName : testData) {
			customerImpl.setLastName(lastName);
			assertSame(lastName, customerImpl.getLastName());
		}
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.getPassword()'.
	 */
	@Test
	public void testGetPassword() {
		assertEquals(null, customerImpl.getPassword());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.getPassword()'.
	 */
	@Test
	public void testGetEncryptedPassword() {
		assertEquals(customerImpl.getPassword(), null);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.setClearTextPassword(String)'.
	 */
	@Test
	public void testSetClearTextPassword() {
		@SuppressWarnings("unchecked")
		final SaltFactory<String> saltFactory = context.mock(SaltFactory.class);
		beanExpectations.allowingBeanFactoryGetBean(ContextIdNames.SALT_FACTORY, saltFactory);

		final String[] passwords = new String[] { "AbCdEfGhI", "AbCdEfGhIjKlMnOpQrS", "aA123_$@#^&", "", null };
		final String[] hashedPasswords = new String[] { "d60c7aaba158d8270ec509390438152ca931ec6a", "32a6ea3419c4d9653cf51c6500f3accef2012ab0",
				"e9d1d12fbb45ca95c496f3a33a40956c1a4da1ef", null, null };

		final String salt = "SALT";
		for (int i = 0; i < passwords.length; i++) {
			final String password = passwords[i];
			final String hashedPassword = hashedPasswords[i];

			context.checking(new Expectations() {
				{
					if (!StringUtils.isBlank(password)) {
						oneOf(saltFactory).createSalt(); will(returnValue(salt));
						oneOf(passwordEncoder).encodePassword(password, salt);
						will(returnValue(hashedPassword));
					}
				}
			});

			customerImpl.setClearTextPassword(password);
			assertEquals(hashedPassword, customerImpl.getPassword());
		}
	}


	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.isAnonymous()'.
	 */
	@Test
	public void testIsAnonymous() {
		assertFalse(customerImpl.isAnonymous());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.setAnonymous()'.
	 */
	@Test
	public void testSetAnonymous() {
		customerImpl.setAnonymous(true);
		assertTrue(customerImpl.isAnonymous());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.getCreationDate()'.
	 */
	@Test
	public void testGetCreationDate() {
		final Date creationDate = customerImpl.getCreationDate();
		assertNotNull("Check getCreationDate", creationDate);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.setCreationDate(Date)'.
	 */
	@Test
	public void testSetCreationDate() {
		final Date creationDate = new Date();
		this.customerImpl.setCreationDate(creationDate);
		assertEquals(creationDate, this.customerImpl.getCreationDate());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.getLastEditDate()'.
	 */
	@Test
	public void testGetLastEditDate() {
		final Date lastEditDate = customerImpl.getLastEditDate();
		assertNotNull("Check getCreationDate", lastEditDate);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.setLastEditDate(Date)'.
	 */
	@Test
	public void testSetLastEditDate() {
		final Date lastEditDate = new Date();
		this.customerImpl.setLastEditDate(lastEditDate);
		assertEquals(lastEditDate, this.customerImpl.getLastEditDate());
	}

	/**
	 * Test method for setting addresses.
	 */
	@Test
	public void testGetSetAddresses() {
		assertNull(customerImpl.getPreferredBillingAddress());
		assertNull(customerImpl.getPreferredShippingAddress());

		CustomerAddress address = new CustomerAddressImpl();
		address.setFirstName("Test");

		customerImpl.setPreferredBillingAddress(address);
		assertSame(address, customerImpl.getPreferredBillingAddress());

		assertNull(customerImpl.getPreferredShippingAddress());
		customerImpl.setPreferredBillingAddress(null);

		customerImpl.setPreferredShippingAddress(address);
		assertSame(address, customerImpl.getPreferredShippingAddress());
	}

	/**
	 * Test method for setting customer status.
	 */
	@Test
	public void testSetStatus() {
		this.customerImpl.setStatus(Customer.STATUS_ACTIVE);
		assertEquals(Customer.STATUS_ACTIVE, this.customerImpl.getStatus());
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
		assertEquals(Customer.GENDER_FEMALE, this.customerImpl.getGender());
	}

	/**
	 * Test method for setting customer's company.
	 */
	@Test
	public void testSetCompany() {
		this.customerImpl.setCompany(null);
		assertEquals(null, this.customerImpl.getCompany());
	}

	/** When setting the preferred billing address, it should be in the list of addresses. */
	@Test
	public void testSetPreferredBillingIsInAddresses() {
		CustomerAddress address = new CustomerAddressImpl();
		Assert.assertThat("Customer already has the address?", customerImpl.getAddresses(),
				Matchers.not(hasItem(address)));

		customerImpl.setPreferredBillingAddress(address);
		Assert.assertThat("Preferred address should a part of customer addresses", customerImpl.getAddresses(),
				hasItem(address));
	}

	/** When setting the preferred shipping address, it should be in the list of addresses. */
	@Test
	public void testSetPreferredShippingIsInAddresses() {
		CustomerAddress address = new CustomerAddressImpl();
		Assert.assertThat("Customer already has the address?", customerImpl.getAddresses(),
				Matchers.not(hasItem(address)));

		customerImpl.setPreferredShippingAddress(address);
		Assert.assertThat("Preferred address should a part of customer addresses", customerImpl.getAddresses(),
				hasItem(address));
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
		assertEquals(address1, customerImpl.getAddressByUid(1));
		assertEquals(address2, customerImpl.getAddressByUid(2));
	}

	/**
	 * Tests setting and retrieving html email preference flag.
	 */
	@Test
	public void testSetHtmlEmailPreferred() {
		assertFalse(customerImpl.isHtmlEmailPreferred());
		customerImpl.setHtmlEmailPreferred(true);
		assertTrue(customerImpl.isHtmlEmailPreferred());

		customerImpl.setHtmlEmailPreferred(false);
		assertFalse(customerImpl.isHtmlEmailPreferred());

	}

	/**
	 * Tests setting and retrieving notification email preference flag.
	 */
	@Test
	public void testSetToBeNotified() {
		assertFalse(customerImpl.isToBeNotified());
		customerImpl.setToBeNotified(true);
		assertTrue(customerImpl.isToBeNotified());

		customerImpl.setToBeNotified(false);
		assertFalse(customerImpl.isToBeNotified());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.setFaxNumber()'.
	 */
	@Test
	public void testSetFaxNumber() {
		String faxNumber = "+1 111 111 888";
		assertNull(customerImpl.getFaxNumber());
		customerImpl.setFaxNumber(faxNumber);
		assertEquals(faxNumber, customerImpl.getFaxNumber());

	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.setPhoneNumber()'.
	 */
	@Test
	public void testSetPhoneNumber() {
		String phoneNumber = "+1 111 111 888";
		assertNull(customerImpl.getPhoneNumber());
		customerImpl.setPhoneNumber(phoneNumber);
		assertEquals(phoneNumber, customerImpl.getPhoneNumber());

	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerImpl.belongsToCustomerGroup()'.
	 */
	@Test
	public void testBelongsToCustomerGroup() {
		assertTrue(customerImpl.belongsToCustomerGroup(customerImpl.getCustomerGroups().get(0).getUidPk()));
	}

	/**
	 * Verify role mapper returns correct roles on customer.
	 */
	@Test
	public void verifyRoleMapperReturnsCorrectRolesOnCustomer() {
		customerImpl.setAnonymous(true);
		CustomerRoleMapper roleMapper = customerImpl.getCustomerRoleMapper();
		assertNotNull(roleMapper);
		assertTrue("Customer should have role " + PUBLIC, roleMapper.hasRole(PUBLIC));

		customerImpl.setAnonymous(false);
		assertTrue("Customer should have role " + REGISTERED, roleMapper.hasRole(REGISTERED));
	}
}
