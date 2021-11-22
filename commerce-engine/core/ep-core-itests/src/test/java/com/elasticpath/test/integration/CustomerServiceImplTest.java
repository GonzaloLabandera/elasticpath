/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.test.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.elasticpath.base.GloballyIdentifiable;
import com.elasticpath.base.exception.structured.EpValidationException;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.builder.customer.CustomerGroupBuilder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.CustomerGroup;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.domain.misc.impl.RandomGuidImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.service.customer.AddressService;
import com.elasticpath.service.customer.CustomerGroupService;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.orderpaymentapi.CustomerPaymentInstrumentService;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;
import com.elasticpath.test.util.Utils;

/**
 * An integration test for the CustomerServiceImpl, we are testing from a client's point of view with Spring and the Database up and running.
 */
public class CustomerServiceImplTest extends BasicSpringContextTest {

	private static final String TEST_SHAREDID_1 = "7fc7f73c-688b-471b-886d-9898dbeb5bef";
	private static final String TEST_SHAREDID_2 = "ba628c3b-3067-453f-96a6-e0c648e8aa9c";

	private static final String TEST_EMAIL_1 = "test@elasticpath.com";
	private static final String TEST_EMAIL_2 = "test2@elasticpath.com";

	private static final String PASSWORD = "password";
	private static final String TOKEN_DISPLAY_VALUE = "**** **** **** 1234";
	private static final String TOKEN_GATEWAY_GUID = "abc";
	private static final String TEST_TOKEN_VALUE = "testTokenValue";
	public static final String SHARED_ID_NOT_UNIQUE_MSG = "sharedId.not.unique";

	@Autowired
	@Qualifier("customerService")
	private CustomerService service;

	@Autowired
	private CustomerGroupService customerGroupService;

	@Autowired
	private AddressService addressService;

	@Autowired
	private CustomerGroupBuilder customerGroupBuilder;

	private SimpleStoreScenario scenario;

	@Autowired
	private PersistenceEngine persistenceEngine;

	@Autowired
	private BeanFactory beanFactory;

	@Autowired
	private CustomerPaymentInstrumentService customerPaymentInstrumentService;

	/**
	 * Get a reference to TestApplicationContext for use within the test. Setup scenarios.
	 */
	@Before
	public void setUp() throws Exception {
		scenario = getTac().useScenario(SimpleStoreScenario.class);
	}

	/**
	 * Test creating a new customer with the same email as another existing customer.
	 */
	@DirtiesDatabase
	@Test
	public void testCreateCustomerWithSameEmailAsExistingCustomer() {
		createPersistedCustomer(TEST_SHAREDID_1, TEST_EMAIL_1, scenario.getStore());

		assertThatCode(() -> createPersistedCustomer(TEST_SHAREDID_2, TEST_EMAIL_1, scenario.getStore()))
				.as("Should be able to add a new customer with the same email as another existing customer.")
				.doesNotThrowAnyException();
	}

	/**
	 * Test updating a customer to have the same email as another existing customer.
	 */
	@DirtiesDatabase
	@Test
	public void testUpdateCustomerWithSameEmailAsExistingCustomer() {
		Customer customer1 = createPersistedCustomer(TEST_SHAREDID_1, TEST_EMAIL_1, scenario.getStore());

		Customer customer2 = createPersistedCustomer(TEST_SHAREDID_2, TEST_EMAIL_2, scenario.getStore());
		assertThatCode(() -> {
			customer2.setEmail(customer1.getEmail());
			service.update(customer2);
		})
				.as("Should be able to update a customer's email "
						+ "to be the same as another existing customer's email.")
				.doesNotThrowAnyException();
	}

	/**
	 * Test updating an anonymous customer into a non-anonymous customer using an email address as userId.
	 */
	@DirtiesDatabase
	@Test
	public void testUpdateAnonymousCustomerIntoNonAnonymousCustomer() {
		Customer customer = createPersistedAnonymousCustomer(scenario.getStore());

		assertThatCode(() -> {
			customer.setSharedId(TEST_EMAIL_1);
			customer.setEmail(TEST_EMAIL_1);
			customer.setUsername(TEST_EMAIL_1);
			customer.setFirstName("Tester");
			customer.setLastName("Testerson");
			customer.setClearTextPassword(PASSWORD);
			customer.setCustomerType(CustomerType.REGISTERED_USER);
			service.update(customer);
		})
				.as("Should be able to update an anonymous customer "
						+ "into a non-anonymous customer using an email as the userId.")
				.doesNotThrowAnyException();
	}

	/**
	 * Test persisting a customer and then loading it back from the DB
	 */
	@DirtiesDatabase
	@Test
	public void testAddAndGetCustomer() {
		Customer customer = createCustomer();
		service.add(customer);
		assertThat(customer.getUidPk()).isNotEqualTo(0);
		assertThat(customer.isPersisted()).isTrue();
		Customer retrievedCustomer = service.get(customer.getUidPk());
		assertThat(retrievedCustomer.getUidPk()).isEqualTo(customer.getUidPk());
	}

	/**
	 * Tests finding customers by group name.
	 */
	@DirtiesDatabase
	@Test
	public void testFindCustomerByGroup() {
		CustomerGroup newCustomerGroup = customerGroupBuilder.newInstance()
			.withGuid("guid_newCustomerGroup")
			.withName("newCustomerGroup")
			.build();
		newCustomerGroup = customerGroupService.add(newCustomerGroup);

		final Customer customer1 = createCustomer();
		final String customer1Guid = "guid_customer1";
		customer1.setGuid(customer1Guid);
		customer1.setCustomerGroups(Lists.newArrayList(newCustomerGroup));
		service.add(customer1);

		final Customer customer2 = createCustomer();
		final String customer2Guid = "guid_customer2";
		customer2.setGuid(customer2Guid);
		customer2.setCustomerGroups(Lists.newArrayList(newCustomerGroup));
		service.add(customer2);

		final List<Customer> foundCustomers = service.findCustomersByCustomerGroup(newCustomerGroup.getName());

		final Collection<String> expectedCustomerGuids = ImmutableList.of(customer1Guid, customer2Guid);
		final Collection<String> foundCustomerGuids =
			Collections2.transform(foundCustomers, GloballyIdentifiable::getGuid);

		Joiner joiner = Joiner.on(", ");
		assertThat(CollectionUtils.isEqualCollection(expectedCustomerGuids, foundCustomerGuids))
			.as("Expected customer guids [%s] but got [%s]",
				joiner.join(expectedCustomerGuids), joiner.join(foundCustomerGuids))
			.isTrue();
	}

	/**
	 * Test persisting a customer and then updating the firstname (tests value saved to customerprofile map).
	 */
	@DirtiesDatabase
	@Test
	public void testAddThenUpdateCustomerProfileValue() {
		final String firstName = "First Name";

		Customer customer = createCustomer();
		service.add(customer);
		assertThat(customer.getUidPk()).isNotEqualTo(0);
		assertThat(customer.isPersisted()).isTrue();

		customer.setFirstName(firstName);
		service.update(customer);

		Customer retrievedCustomer = service.get(customer.getUidPk());
		assertThat(retrievedCustomer.getUidPk()).isEqualTo(customer.getUidPk());
		assertThat(retrievedCustomer.getFirstName()).isEqualTo(firstName);
	}

	/**
	 * Test adding an address to a customer.
	 */
	@DirtiesDatabase
	@Test
	public void testAddCustomerAddress() {
		// Persist a customer
		Customer customer = createCustomer();
		service.add(customer);

		// Add an address to the customer
		final CustomerAddress customerAddress = createAddress();

		assertThat(customerAddress.getGuid()).isNotNull();

		// Update the customer
		Customer updatedCustomer = service.addOrUpdateAddress(customer, customerAddress);
		List<CustomerAddress> customerAddresses = addressService.findByCustomer(updatedCustomer.getUidPk());

		assertThat(customerAddresses).hasSize(1);
		CustomerAddress updatedAddress = customerAddresses.get(0);
		assertThat(updatedAddress.isPersisted())
			.as("The customer's address should now be persistent")
			.isTrue();

		// Load the address back from the customer object
		CustomerAddress retrievedAddress = addressService.findByCustomerAndAddressUid(updatedCustomer.getUidPk(), updatedAddress.getUidPk());
		assertThat(retrievedAddress)
			.as("The address should be found in the database and match the updated address")
			.isEqualTo(updatedAddress);
		assertThat(retrievedAddress.getGuid())
			.as("The GUID of the retrieved address should match that of the updated object")
			.isEqualTo(updatedAddress.getGuid());

		// Also try using the service to get the updated customer address.
		assertThat(service.getCustomerAddress(updatedAddress.getUidPk())).isNotNull();
	}

	/**
	 * Test that editing a saved address and updating works correctly.
	 */
	@DirtiesDatabase
	@Test
	public void testEditCustomerAddress() {
		// Persist a customer
		Customer customer = createCustomer();
		service.add(customer);
		assertThat(customer.getCustomerGroups())
			.as("The customer should have a group")
			.hasSize(1);

		// Add an address to the customer and save to the DB
		CustomerAddress customerAddress = createAddress();
		Customer updatedCustomer = service.addOrUpdateAddress(customer, customerAddress);
		assertThat(updatedCustomer.getCustomerGroups())
			.as("The customer should still have a group")
			.hasSize(1);

		// Make a change to the address and save to the DB
		CustomerAddress address = customerAddress;
		long addressUid = address.getUidPk();
		String uniqueName = Utils.uniqueCode("name");
		address.setFirstName(uniqueName);
		Customer updatedTwiceCustomer = service.addOrUpdateAddress(updatedCustomer, address);
		CustomerAddress updatedAddress = addressService.findByCustomerAndAddressUid(updatedTwiceCustomer.getUidPk(), addressUid);
		assertThat(updatedAddress.getUidPk())
			.as("The updated address should have the same ID")
			.isEqualTo(addressUid);
		assertThat(updatedAddress.getFirstName())
			.as("The updated address should have the updated first name")
			.isEqualTo(uniqueName);

		// Change a field to the same value and save again
		String lastName = updatedAddress.getLastName();
		updatedAddress.setLastName(lastName);
		assertThat(updatedAddress.getLastName())
			.as("Name should not have changed")
			.isEqualTo(lastName);
		assertThat(updatedAddress).isEqualTo(address);
		Customer updatedThriceCustomer = service.addOrUpdateAddress(updatedTwiceCustomer, updatedAddress);
		CustomerAddress updatedTwiceAddress = addressService.findByCustomerAndAddressUid(updatedThriceCustomer.getUidPk(), addressUid);
		assertThat(updatedTwiceAddress.getUidPk())
			.as("The address of the updated customer should still have the same ID")
			.isEqualTo(addressUid);
		assertThat(updatedTwiceAddress.getFirstName())
			.as("The address of the updated customer should still have the updated first name")
			.isEqualTo(uniqueName);

		// Now reload customer and resave
		Customer reloadedCustomer = service.get(customer.getUidPk());
		Customer updatedReloadedCustomer = service.update(reloadedCustomer);
		assertThat(updatedReloadedCustomer).isNotNull();
	}

	/**
	 * Test that a Customer's address can be created or updated while at the same time
	 * having the option to change the default billing or shipping address.
	 */
	@DirtiesDatabase
	@Test
	public void testAddOrUpdateCustomerAddress() {
		Customer customer;
		CustomerAddress address;

		// Assert that we can create an address and not make it the default.
		customer = service.add(createCustomer());
		customer = service.addOrUpdateAddress(customer, createAddress());

		List<CustomerAddress> customerAddresses = addressService.findByCustomer(customer.getUidPk());
		assertThat(customerAddresses).hasSize(1);
		assertThat(customer.getPreferredBillingAddress())
			.as("The customer should not have a preferred billing address.")
			.isNull();
		assertThat(customer.getPreferredShippingAddress())
			.as("The customer should not have a preferred shipping address.")
			.isNull();

		// Assert that we can update an address and not make it the default.
		address = customerAddresses.get(0);
		address.setFirstName("a");
		customer = service.addOrUpdateAddress(customer, address);
		customerAddresses = addressService.findByCustomer(customer.getUidPk());

		assertThat(customerAddresses).hasSize(1);
		assertThat(customer.getPreferredBillingAddress())
			.as("The customer should not have a preferred billing address.")
			.isNull();
		assertThat(customer.getPreferredShippingAddress())
			.as("The customer should not have a preferred shipping address.")
			.isNull();
		assertThat(customerAddresses.get(0))
			.as("The persisted billing address should equal the in-memory billing address.")
			.isEqualTo(address);

		// Assert that we can make an existing address the default billing address.
		address = customerAddresses.get(0);
		customer = service.addOrUpdateCustomerBillingAddress(customer, address);
		customerAddresses = addressService.findByCustomer(customer.getUidPk());

		assertThat(customerAddresses.size()).isEqualTo(1);
		assertThat(customer.getPreferredBillingAddress())
			.as("The customer should have a preferred billing address.")
			.isEqualTo(address);
		assertThat(customer.getPreferredShippingAddress())
			.as("The customer should not have a preferred shipping address.")
			.isNull();
		assertThat(customerAddresses.get(0))
			.as("The persisted billing address should equal the in-memory billing address.")
			.isEqualTo(address);

	}

	/**
	 * Verifies that {@link CustomerService#getCustomerLastModifiedDate(String) will return <code>null</code> if the GUID is invalid.
	 */
	@DirtiesDatabase
	@Test
	public void testGetLastModifiedDateInvalidGuid() {
		Date dateFromService = service.getCustomerLastModifiedDate("INVALID_GUID");
		assertThat(dateFromService)
			.as("The method should return null when the GUID is invalid.")
			.isNull();
	}




	/**
	 * Tests {@link CustomerService#getCustomerLastModifiedDate(String).
	 */
	@DirtiesDatabase
	@Test
	public void testGetLastModifiedDate() {
		Customer customer = createPersistedAnonymousCustomer(scenario.getStore());
		Date dateFromCustomer = customer.getLastModifiedDate();
		Date dateFromService = service.getCustomerLastModifiedDate(customer.getGuid());
		assertThat(dateFromService)
			.as("The date loaded from the object should be equal to the date retrieved by the service.")
			.isEqualTo(dateFromCustomer);
	}

	/**
	 * Test {@link CustomerService#findIndexableUidsPaginated(Date, int, int)'.
	 */
	@Test
	public void testFindIndexableUidsPaginated() {
		Date lastModifiedDate = new Date();

		// Persist a customer
		Customer customer = createCustomer();
		customer.setLastEditDate(lastModifiedDate);
		service.add(customer);

		final ArrayList<Long> uidList = new ArrayList<>();
		uidList.add(customer.getUidPk());

		assertThat(service.findIndexableUidsPaginated(lastModifiedDate, 0, Integer.MAX_VALUE)).isEqualTo(uidList);
	}

	/**
	 * Test that the same clear text password generates a unique encoded password each time (due to random BCrypt not generating the same value).
	 */
	@DirtiesDatabase
	@Test
	public void testEncodedPasswordUniqueness() {
		Customer customer = createCustomer();
		customer.setClearTextPassword(PASSWORD);
		customer.setUsername(customer.getSharedId());
		customer = service.update(customer);

		String password = customer.getPassword();
		assertThat(password).isNotEmpty();

		customer.setClearTextPassword(PASSWORD);
		customer = service.update(customer);

		assertThat(customer.getPassword())
			.as("The encoded password should be different")
			.isNotEqualTo(password);
	}

	@Test(expected= EpValidationException.class)
	@DirtiesDatabase
	public void verifyAddingInvalidEmailToAnonymousCustomerResultsInStructuredErrorMessage() {
		Customer anonymousCustomer = createPersistedAnonymousCustomer(scenario.getStore());
		anonymousCustomer.setEmail("invalid email string");
		service.update(anonymousCustomer);
	}

	//====================================================================================================================
	// Setup methods
	//====================================================================================================================

	private Customer createPersistedAnonymousCustomer(final Store store) {
		final Customer anonymousCustomer = beanFactory.getPrototypeBean(ContextIdNames.CUSTOMER, Customer.class);
		anonymousCustomer.setStoreCode(store.getCode());
		anonymousCustomer.setCustomerType(CustomerType.SINGLE_SESSION_USER);

		return service.add(anonymousCustomer);
	}

	private Customer createPersistedCustomer(final String sharedId, final String email, final Store store) {
		final Customer customer = beanFactory.getPrototypeBean(ContextIdNames.CUSTOMER, Customer.class);
		customer.setSharedId(sharedId);
		customer.setCustomerType(CustomerType.REGISTERED_USER);
		customer.setEmail(email);
		customer.setFirstName("Test");
		customer.setLastName("Test");
		customer.setStoreCode(store.getCode());

		return service.add(customer);
	}

	private CustomerAddress createAddress() {
		final CustomerAddress customerAddress = beanFactory.getPrototypeBean(ContextIdNames.CUSTOMER_ADDRESS, CustomerAddress.class);
		customerAddress.setFirstName("Test");
		customerAddress.setLastName("Test");
		customerAddress.setSubCountry("BC");
		customerAddress.setCountry("CA");
		customerAddress.setStreet1("123 Testing St");
		customerAddress.setCity("Vancouver");
		customerAddress.setZipOrPostalCode("V5Y1N3");
		return customerAddress;
	}

	private Customer createCustomer() {
		final Customer customer = beanFactory.getPrototypeBean(ContextIdNames.CUSTOMER, Customer.class);
		customer.setSharedId(Utils.uniqueCode("id"));
		customer.setCustomerType(CustomerType.REGISTERED_USER);
		customer.setFirstName("Test");
		customer.setLastName("Test");
		customer.setCreationDate(new Date());
		customer.setLastEditDate(new Date());
		customer.setStatus(Customer.STATUS_ACTIVE);
		customer.setGuid(new RandomGuidImpl().toString());
		customer.setEmail("test" + Math.round(Math.random() * 1000) + "@elasticpath.com");
		customer.setStoreCode(scenario.getStore().getCode());
		return customer;
	}
}
