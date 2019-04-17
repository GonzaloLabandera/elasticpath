/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.test.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import com.elasticpath.base.GloballyIdentifiable;
import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.exception.structured.EpValidationException;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.exception.UserIdExistException;
import com.elasticpath.domain.builder.customer.CustomerGroupBuilder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.CustomerGroup;
import com.elasticpath.domain.customer.CustomerMessageIds;
import com.elasticpath.domain.customer.CustomerPaymentMethods;
import com.elasticpath.domain.customer.PaymentToken;
import com.elasticpath.domain.customer.impl.PaymentTokenImpl;
import com.elasticpath.domain.misc.impl.RandomGuidImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.plugin.payment.dto.PaymentMethod;
import com.elasticpath.service.customer.CustomerGroupService;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;
import com.elasticpath.test.util.Utils;

/**
 * An integration test for the CustomerServiceImpl, we are testing from a client's point of view with Spring and the Database up and running.
 */
public class CustomerServiceImplTest extends BasicSpringContextTest {

	private static final String TEST_USERID_1 = "7fc7f73c-688b-471b-886d-9898dbeb5bef";
	private static final String TEST_USERID_2 = "ba628c3b-3067-453f-96a6-e0c648e8aa9c";

	private static final String TEST_EMAIL_1 = "test@elasticpath.com";
	private static final String TEST_EMAIL_2 = "test2@elasticpath.com";

	private static final String PASSWORD = "password";
	private static final String TOKEN_DISPLAY_VALUE = "**** **** **** 1234";
	private static final String TOKEN_GATEWAY_GUID = "abc";
	private static final String TEST_TOKEN_VALUE = "testTokenValue";

	@Autowired
	@Qualifier("customerService")
	private CustomerService service;

	@Autowired
	private CustomerGroupService customerGroupService;

	@Autowired
	private CustomerGroupBuilder customerGroupBuilder;

	private SimpleStoreScenario scenario;

	@Autowired
	private PersistenceEngine persistenceEngine;

	@Autowired
	private BeanFactory beanFactory;

	/**
	 * Get a reference to TestApplicationContext for use within the test. Setup scenarios.
	 */
	@Before
	public void setUp() throws Exception {
		scenario = getTac().useScenario(SimpleStoreScenario.class);
	}

	/**
	 * Test creating a new customer with the same userId as an existing anonymous customer.
	 */
	@DirtiesDatabase
	@Test
	public void testCreateCustomerWithSameUserIdAsAnonymousCustomer() {
		Customer anonymousCustomer = createPersistedAnonymousCustomer(scenario.getStore());

		UserIdExistException uidee = catchThrowableOfType(
				() -> createPersistedCustomer(anonymousCustomer.getUserId(), TEST_EMAIL_1, scenario.getStore()),
				UserIdExistException.class);

		assertThat(uidee.getStructuredErrorMessages())
				.extracting(StructuredErrorMessage::getMessageId)
				.containsOnly(CustomerMessageIds.USERID_ALREADY_EXISTS);
		assertThat(uidee.getStructuredErrorMessages())
				.extracting(structuredErrorMessage -> structuredErrorMessage.getData().get("user-id"))
				.containsOnly(anonymousCustomer.getUserId());
	}

	/**
	 * Test creating a new customer with the same userId as an existing non-anonymous customer.
	 */
	@DirtiesDatabase
	@Test
	public void testCreateCustomerWithSameUserIdAsExistingCustomer() {
		Customer customer = createPersistedCustomer(TEST_USERID_1, TEST_EMAIL_1, scenario.getStore());

		UserIdExistException uidee = catchThrowableOfType(
				() -> createPersistedCustomer(customer.getUserId(), TEST_EMAIL_2, scenario.getStore()),
				UserIdExistException.class);

		assertThat(uidee.getStructuredErrorMessages())
				.extracting(StructuredErrorMessage::getMessageId)
				.containsOnly(CustomerMessageIds.USERID_ALREADY_EXISTS);
		assertThat(uidee.getStructuredErrorMessages())
				.extracting(structuredErrorMessage -> structuredErrorMessage.getData().get("user-id"))
				.containsOnly(TEST_USERID_1);
	}

	/**
	 * Test creating a new customer with the same email as another existing customer.
	 */
	@DirtiesDatabase
	@Test
	public void testCreateCustomerWithSameEmailAsExistingCustomer() {
		createPersistedCustomer(TEST_USERID_1, TEST_EMAIL_1, scenario.getStore());

		assertThatCode(() -> createPersistedCustomer(TEST_USERID_2, TEST_EMAIL_1, scenario.getStore()))
				.as("Should be able to add a new customer with the same email as another existing customer.")
				.doesNotThrowAnyException();
	}

	/**
	 * Test updating a customer to have the same userId as an existing anonymous customer.
	 */
	@DirtiesDatabase
	@Test
	public void testUpdateCustomerWithSameUserIdAsAnonymousCustomer() {
		Customer anonymousCustomer = createPersistedAnonymousCustomer(scenario.getStore());

		Customer customer2 = createPersistedCustomer(TEST_USERID_2, TEST_EMAIL_2, scenario.getStore());
		UserIdExistException uidee = catchThrowableOfType(
				() -> {
					customer2.setUserId(anonymousCustomer.getUserId());
					service.update(customer2);
				},
				UserIdExistException.class);

		assertThat(uidee.getStructuredErrorMessages())
				.extracting(StructuredErrorMessage::getMessageId)
				.containsOnly(CustomerMessageIds.USERID_ALREADY_EXISTS);
		assertThat(uidee.getStructuredErrorMessages())
				.extracting(structuredErrorMessage -> structuredErrorMessage.getData().get("user-id"))
				.containsOnly(anonymousCustomer.getUserId());
	}

	/**
	 * Test updating a customer to have the same userId as an existing non-anonymous customer.
	 */
	@DirtiesDatabase
	@Test
	public void testUpdateCustomerWithSameUserIdAsExistingCustomer() {
		Customer customer1 = createPersistedCustomer(TEST_USERID_1, TEST_EMAIL_1, scenario.getStore());

		Customer customer2 = createPersistedCustomer(TEST_USERID_2, TEST_EMAIL_2, scenario.getStore());
		UserIdExistException uidee = catchThrowableOfType(
				() -> {
					customer2.setUserId(customer1.getUserId());
					service.update(customer2);
				},
				UserIdExistException.class);

		assertThat(uidee.getStructuredErrorMessages())
				.extracting(StructuredErrorMessage::getMessageId)
				.containsOnly(CustomerMessageIds.USERID_ALREADY_EXISTS);
		assertThat(uidee.getStructuredErrorMessages())
				.extracting(structuredErrorMessage -> structuredErrorMessage.getData().get("user-id"))
				.containsOnly(TEST_USERID_1);
	}

	/**
	 * Test updating a customer to have the same email as another existing customer.
	 */
	@DirtiesDatabase
	@Test
	public void testUpdateCustomerWithSameEmailAsExistingCustomer() {
		Customer customer1 = createPersistedCustomer(TEST_USERID_1, TEST_EMAIL_1, scenario.getStore());

		Customer customer2 = createPersistedCustomer(TEST_USERID_2, TEST_EMAIL_2, scenario.getStore());
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
			customer.setUserId(TEST_EMAIL_1);
			customer.setEmail(TEST_EMAIL_1);
			customer.setFirstName("Tester");
			customer.setLastName("Testerson");
			customer.setClearTextPassword(PASSWORD);
			customer.setAnonymous(false);
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
		customer.addAddress(customerAddress);
		assertThat(customer.getAddresses()).containsOnly(customerAddress);

		String guid = customer.getAddresses().get(0).getGuid();
		assertThat(guid).isNotNull();

		// Update the customer
		Customer updatedCustomer = service.update(customer);
		assertThat(updatedCustomer.getAddresses()).hasSize(1);
		CustomerAddress updatedAddress = updatedCustomer.getAddresses().get(0);
		assertThat(updatedAddress.isPersisted())
			.as("The customer's address should now be persistent")
			.isTrue();

		// Load the address back from the customer object
		CustomerAddress retrievedAddress = updatedCustomer.getAddressByUid(updatedAddress.getUidPk());
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
		customer.addAddress(createAddress());
		Customer updatedCustomer = service.update(customer);
		assertThat(updatedCustomer.getCustomerGroups())
			.as("The customer should still have a group")
			.hasSize(1);

		// Make a change to the address and save to the DB
		CustomerAddress address = updatedCustomer.getAddresses().get(0);
		long addressUid = address.getUidPk();
		String uniqueName = Utils.uniqueCode("name");
		address.setFirstName(uniqueName);
		Customer updatedTwiceCustomer = service.update(updatedCustomer);
		CustomerAddress updatedAddress = updatedTwiceCustomer.getAddressByUid(addressUid);
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
		Customer updatedThriceCustomer = service.update(updatedTwiceCustomer);
		CustomerAddress updatedTwiceAddress = updatedThriceCustomer.getAddressByUid(addressUid);
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
		assertThat(customer.getAddresses()).hasSize(1);
		assertThat(customer.getPreferredBillingAddress())
			.as("The customer should not have a preferred billing address.")
			.isNull();
		assertThat(customer.getPreferredShippingAddress())
			.as("The customer should not have a preferred shipping address.")
			.isNull();

		// Assert that we can update an address and not make it the default.
		address = customer.getAddresses().get(0);
		address.setFirstName("a");
		customer = service.addOrUpdateAddress(customer, address);
		assertThat(customer.getAddresses()).hasSize(1);
		assertThat(customer.getPreferredBillingAddress())
			.as("The customer should not have a preferred billing address.")
			.isNull();
		assertThat(customer.getPreferredShippingAddress())
			.as("The customer should not have a preferred shipping address.")
			.isNull();
		assertThat(customer.getAddresses().get(0))
			.as("The persisted billing address should equal the in-memory billing address.")
			.isEqualTo(address);

		// Assert that we can make an existing address the default billing address.
		address = customer.getAddresses().get(0);
		customer = service.addOrUpdateCustomerBillingAddress(customer, address);
		assertThat(customer.getAddresses().size()).isEqualTo(1);
		assertThat(customer.getPreferredBillingAddress())
			.as("The customer should have a preferred billing address.")
			.isEqualTo(address);
		assertThat(customer.getPreferredShippingAddress())
			.as("The customer should not have a preferred shipping address.")
			.isNull();
		assertThat(customer.getAddresses().get(0))
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
	 * Test that a salt is persisted when a customer password is set.
	 */
	@DirtiesDatabase
	@Test
	public void testPasswordSalt() {
		Customer customer = createCustomer();
		customer.setClearTextPassword(PASSWORD);
		customer = service.update(customer);

		Customer loadedCustomer = service.load(customer.getUidPk());

		assertThat(loadedCustomer.getCustomerAuthentication().getSalt()).isNotEmpty();
	}

	/**
	 * Test that the same clear text password generates a unique encoded password each time (due to random salting).
	 */
	@DirtiesDatabase
	@Test
	public void testEncodedPasswordUniqueness() {
		Customer customer = createCustomer();
		customer.setClearTextPassword(PASSWORD);
		customer = service.update(customer);

		String password = customer.getPassword();
		assertThat(password).isNotEmpty();

		customer.setClearTextPassword(PASSWORD);
		customer = service.update(customer);

		assertThat(customer.getPassword())
			.as("The encoded password should be different")
			.isNotEqualTo(password);
	}

	@Test
	@DirtiesDatabase
	public void ensureCascadeDeletionOfTokensOnCustomerDelete() {
		PaymentTokenImpl.TokenBuilder tokenBuilder = new PaymentTokenImpl.TokenBuilder()
			.withDisplayValue(TOKEN_DISPLAY_VALUE)
			.withGatewayGuid(TOKEN_GATEWAY_GUID);

		PaymentToken firstToken = tokenBuilder
			.withValue("first-token-value").build();
		PaymentToken secondToken = tokenBuilder
			.withValue("second-token-value").build();
		PaymentToken thirdToken = tokenBuilder
			.withValue("third-token-value").build();

		final Customer customer = createCustomer();
		List<PaymentMethod> customerPaymentMethods = ImmutableList.of(firstToken, secondToken);
		customer.getPaymentMethods().addAll(customerPaymentMethods);
		customer.getPaymentMethods().setDefault(thirdToken);
		service.add(customer);

		service.remove(customer);
		for (PaymentMethod token : customer.getPaymentMethods().all()) {
			long uidPk = ((PaymentToken) token).getUidPk();
			assertThat(persistenceEngine.get(PaymentTokenImpl.class, uidPk))
				.as("Token with uidpk {%s} should have been cascade deleted", uidPk)
				.isNull();
		}
		assertThat(persistenceEngine.get(PaymentTokenImpl.class, thirdToken.getUidPk()))
			.as("Token with uidpk {%s} should have been cascade deleted", thirdToken.getUidPk())
			.isNull();
	}

	@Test
	@DirtiesDatabase
	public void ensureCustomerSavesWithPopulatedTokenFields() {
		PaymentTokenImpl.TokenBuilder tokenBuilder = new PaymentTokenImpl.TokenBuilder()
			.withDisplayValue(TOKEN_DISPLAY_VALUE)
			.withGatewayGuid(TOKEN_GATEWAY_GUID);

		PaymentToken firstToken = tokenBuilder
			.withValue("first-token-value").build();
		PaymentToken secondToken = tokenBuilder
			.withValue("second-token-value").build();


		final Customer customer = createCustomer();
		List<PaymentMethod> customerPaymentMethods = ImmutableList.of(firstToken, secondToken);
		customer.getPaymentMethods().addAll(customerPaymentMethods);
		customer.getPaymentMethods().setDefault(secondToken);
		service.add(customer);

		Customer retrievedCustomer = service.get(customer.getUidPk());
		CustomerPaymentMethods retrievedPaymentTokens = retrievedCustomer.getPaymentMethods();

		assertThat(retrievedPaymentTokens.all().size()).isEqualTo(2);
		for (PaymentMethod token : retrievedPaymentTokens.all()) {
			assertThat(persistenceEngine.get(PaymentTokenImpl.class, ((PaymentToken) token).getUidPk()))
				.as("Tokens referenced by the customer should exist in the database")
				.isNotNull();
		}
		assertThat(customer.getPaymentMethods().getDefault()).isNotNull();
	}

	@Test
	@DirtiesDatabase
	public void ensureCustomerSavesWithEmptyTokenFields() {
		final Customer customer = createCustomer();
		service.add(customer);

		Customer retrievedCustomer = service.get(customer.getUidPk());
		CustomerPaymentMethods retrievedCustomerCustomerPaymentMethods = retrievedCustomer.getPaymentMethods();
		assertThat(retrievedCustomerCustomerPaymentMethods.all()).isEmpty();
		assertThat(customer.getPaymentMethods().getDefault())
			.as("No default payment token should have been saved on the customer")
			.isNull();
	}

	@Test(expected = InvalidDataAccessApiUsageException.class)
	@DirtiesDatabase
	public void ensureCustomerSavedWithTokenWithNullDisplayValueThrowsAnException() {
		PaymentToken tokenWithNoDisplayValue = new PaymentTokenImpl.TokenBuilder()
				.withGatewayGuid(TOKEN_GATEWAY_GUID)
				.withValue("token-value")
				.build();

		Customer customer = createCustomer();
		customer.getPaymentMethods().addAll(Collections.singletonList(tokenWithNoDisplayValue));

		service.add(customer);
	}

	@Test(expected = InvalidDataAccessApiUsageException.class)
	@DirtiesDatabase
	public void ensureCustomerSavedWithTokenWithNullTokenValueThrowsAnException() {
		PaymentToken tokenWithNoValue = new PaymentTokenImpl.TokenBuilder()
				.withGatewayGuid(TOKEN_GATEWAY_GUID)
				.withDisplayValue(TOKEN_DISPLAY_VALUE)
				.build();

		Customer customer = createCustomer();
		customer.getPaymentMethods().addAll(Collections.singletonList(tokenWithNoValue));

		service.add(customer);
	}

	@Test
	@DirtiesDatabase
	public void ensureFilterSearchableFiltersOutAnonymousCustomers() {
		Customer signedInCustomer = createPersistedCustomer(TEST_USERID_1, TEST_EMAIL_1, scenario.getStore());
		Customer anonymousCustomer = createPersistedAnonymousCustomer(scenario.getStore());

		Collection<Long> filteredUids = service.filterSearchable(ImmutableList.of(signedInCustomer.getUidPk(), anonymousCustomer.getUidPk()));
		Set<Long> filteredUidSet = new HashSet<>(filteredUids);

		assertThat(filteredUidSet)
			.as("The filtered set should not include the anonymous customer uid")
			.containsOnly(signedInCustomer.getUidPk());
	}

	@Test
	@DirtiesDatabase
	public void ensureCustomerDefaultPaymentMethodIsUpdatedAndPreviousDefaultIsRetained() {
		Customer customer = createCustomer();

		PaymentMethod tokenWithValue = new PaymentTokenImpl.TokenBuilder()
			.withGatewayGuid(TOKEN_GATEWAY_GUID)
			.withDisplayValue(TOKEN_DISPLAY_VALUE)
			.withValue(TEST_TOKEN_VALUE + "1")
			.build();
		customer.getPaymentMethods().add(tokenWithValue);

		PaymentMethod tokenWithValue2 = new PaymentTokenImpl.TokenBuilder()
			.withGatewayGuid(TOKEN_GATEWAY_GUID)
			.withDisplayValue(TOKEN_DISPLAY_VALUE)
			.withValue(TEST_TOKEN_VALUE + "2")
			.build();
		customer.getPaymentMethods().setDefault(tokenWithValue2);
		Customer updatedCustomer = service.update(customer);

		//Set a new default and re-update customer
		updatedCustomer.getPaymentMethods().setDefault(tokenWithValue);
		updatedCustomer = service.update(updatedCustomer);

		assertThat(updatedCustomer.getPaymentMethods().all())
			.containsExactlyInAnyOrder(tokenWithValue, tokenWithValue2)
			.allSatisfy(paymentMethod -> assertThat(retrieveToken(paymentMethod)).isNotNull());
	}

	private PaymentTokenImpl retrieveToken(final PaymentMethod paymentMethod) {
		return persistenceEngine.get(PaymentTokenImpl.class, ((PaymentTokenImpl) paymentMethod).getUidPk());
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
		final Customer anonymousCustomer = beanFactory.getBean(ContextIdNames.CUSTOMER);
		anonymousCustomer.setStoreCode(store.getCode());
		anonymousCustomer.setAnonymous(true);

		return service.add(anonymousCustomer);
	}

	private Customer createPersistedCustomer(final String userId, final String email, final Store store) {
		final Customer customer = beanFactory.getBean(ContextIdNames.CUSTOMER);
		customer.setUserId(userId);
		customer.setEmail(email);
		customer.setFirstName("Test");
		customer.setLastName("Test");
		customer.setStoreCode(store.getCode());
		customer.setAnonymous(false);

		return service.add(customer);
	}

	private CustomerAddress createAddress() {
		final CustomerAddress customerAddress = beanFactory.getBean("customerAddress");
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
		final Customer customer = beanFactory.getBean("customer");
		customer.setUserId(Utils.uniqueCode("id"));
		customer.setFirstName("Test");
		customer.setLastName("Test");
		customer.setCreationDate(new Date());
		customer.setLastEditDate(new Date());
		customer.setStatus(Customer.STATUS_ACTIVE);
		customer.setAnonymous(false);
		customer.setGuid(new RandomGuidImpl().toString());
		customer.setEmail("test" + Math.round(Math.random() * 1000) + "@elasticpath.com");
		customer.setStoreCode(scenario.getStore().getCode());
		return customer;
	}
}
