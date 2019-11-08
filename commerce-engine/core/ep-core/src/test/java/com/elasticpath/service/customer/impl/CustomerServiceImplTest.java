/**
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.service.customer.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableMap;

import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.base.exception.structured.EpValidationException;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.exception.UserIdExistException;
import com.elasticpath.commons.exception.UserIdNonExistException;
import com.elasticpath.commons.exception.UserStatusInactiveException;
import com.elasticpath.domain.ElasticPath;
import com.elasticpath.domain.EpDomainException;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.CustomerDeleted;
import com.elasticpath.domain.customer.CustomerGroup;
import com.elasticpath.domain.customer.CustomerMessageIds;
import com.elasticpath.domain.order.OrderAddress;
import com.elasticpath.domain.store.Store;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.EventType;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.persistence.api.FetchGroupLoadTuner;
import com.elasticpath.persistence.api.LoadTuner;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.persistence.openjpa.util.FetchPlanHelper;
import com.elasticpath.service.auth.UserIdentityService;
import com.elasticpath.service.customer.CustomerGroupService;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.search.IndexNotificationService;
import com.elasticpath.service.search.IndexType;
import com.elasticpath.service.shopper.ShopperCleanupService;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.validation.ConstraintViolationTransformer;

/**
 * Test <code>CustomerServiceImpl</code>.
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports"})
@RunWith(MockitoJUnitRunner.class)
public class CustomerServiceImplTest {

	private static final String NEW_PASSWORD = "newPassword";
	private static final String USER_ID = "User Id";
	private static final String USER_GUID = "User Guid";
	private static final String ANOTHER_USER_GUID = "Another User Guid";
	private static final String USER_FIRST_NAME = "UserFirstname";
	private static final String USER_LAST_NAME = "UserLastName";
	private static final String USER_PHONE_NUMBER = "12345";
	private static final String TEST_STORE_CODE = "SAMPLE_STORECODE";
	private static final String STRUCTURED_ERROR_MESSAGES = "structuredErrorMessages";
	private static final String USER_ID_FIELD = "user-id";
	private static final String ID_EXISTS_ERROR_MESSAGE = "Customer with the given user Id already exists";
	private static final long USER_UIDPK = 1L;
	private static final String LIST = "list";
	private static final String CANNOT_RETRIEVE_CUSTOMER_WITHOUT_USER_ID_OR_STORE = "Cannot retrieve customer without userId or store";
	private static final String ADDRESS_FIRST_NAME = "AddressFirstname";
	private static final String ADDRESS_LAST_NAME = "AddressLastName";
	private static final String ADDRESS_PHONE_NUMBER = "OneTwoThreeFourFive";

	@Spy
	@InjectMocks
	private CustomerServiceImpl customerServiceImpl;

	@Mock
	private StoreService storeService;
	@Mock
	private CustomerGroupService customerGroupService;
	@Mock
	private UserIdentityService userIdentityService;
	@Mock
	private TimeService timeService;
	@Mock
	private IndexNotificationService indexNotificationService;
	@Mock
	private ShopperCleanupService shopperCleanupService;
	@Mock
	private PersistenceEngine persistenceEngine;
	@Mock
	private FetchPlanHelper fetchPlanHelper;
	@Mock
	private Store store;
	@Mock
	private EventMessageFactory eventMessageFactory;
	@Mock
	private EventMessagePublisher eventMessagePublisher;
	@Mock
	private Validator validator;
	@Mock
	private ConstraintViolationTransformer constraintViolationTransformer;
	@Mock
	private Customer customer;
	@Mock
	private OrderAddress billingAddress;
	@Mock
	private ElasticPath elasticPath;

	@Before
	public void setUp() {
		when(customerGroupService.getDefaultGroup()).thenReturn(mock(CustomerGroup.class));
		when(storeService.findStoreWithCode(TEST_STORE_CODE)).thenReturn(store);
		when(timeService.getCurrentTime()).thenReturn(new Date());

		when(store.getCode()).thenReturn(TEST_STORE_CODE);
		when(store.getUidPk()).thenReturn(1L);

		when(customer.getGuid()).thenReturn(USER_GUID);
		when(customer.getUserId()).thenReturn(USER_ID);
		when(customer.getStoreCode()).thenReturn(TEST_STORE_CODE);
		when(customer.getUidPk()).thenReturn(USER_UIDPK);

		Mockito.<Class<Customer>>when(elasticPath.getBeanImplClass(ContextIdNames.CUSTOMER)).thenReturn(Customer.class);
		when(elasticPath.getBean(ContextIdNames.CUSTOMER)).thenReturn(customer);
	}

	/**
	 * Adding a non-anonymous customer adds the new customer and identity to the system via delegation and returns the new customer.
	 */
	@Test
	public void addNonAnonymousCustomerDelegatesAndReturnsNewCustomer() {
		when(storeService.findValidStoreCode(TEST_STORE_CODE)).thenReturn(TEST_STORE_CODE);
		when(eventMessageFactory.createEventMessage(any(EventType.class), any(String.class), eq(null))).thenReturn(mock(EventMessage.class));

		assertThat(customerServiceImpl.add(customer)).isEqualTo(customer);

		verify(persistenceEngine).save(customer);
		verify(userIdentityService).add(customer.getUserId(), customer.getClearTextPassword());
		verify(indexNotificationService).addNotificationForEntityIndexUpdate(IndexType.CUSTOMER, customer.getUidPk());
		verify(storeService).findValidStoreCode(TEST_STORE_CODE);
		verify(eventMessagePublisher).publish(any(EventMessage.class));
		verify(customer).setCreationDate(any(Date.class));
	}

	/**
	 * Adding an anonymous customer only saves the new anonymous customer to the persistence engine and returns the customer.
	 */
	@Test
	public void addAnonymousCustomerDelegatesAndReturnsCustomer() {
		when(customer.isAnonymous()).thenReturn(true);

		assertThat(customerServiceImpl.add(customer)).isEqualTo(customer);

		verify(persistenceEngine).save(customer);
		verifyZeroInteractions(userIdentityService, eventMessagePublisher, indexNotificationService);
		verify(storeService).findValidStoreCode(TEST_STORE_CODE);
	}

	/**
	 * Adding a customer without a store attached throws an EpServiceException.
	 */
	@Test
	public void addCustomerWithNoStoreThrowsException() {
		when(customer.getStoreCode()).thenReturn(null);

		assertThatThrownBy(() -> customerServiceImpl.add(customer))
				.isInstanceOf(EpServiceException.class)
				.hasMessage("Customer has no store attached.");
	}

	/**
	 * Adding a customer with a user id that already exists throws a UserIdExistException with attached message and structured error message.
	 */
	@Test
	public void verifyErrorMessageReceivedWhenUserIdExists() {
		doReturn(Collections.singleton(ANOTHER_USER_GUID))
				.when(customerServiceImpl).findCustomerGuidsByUserIdInStoreOrAssociatedStores(USER_ID, TEST_STORE_CODE);

		assertThatThrownBy(() -> customerServiceImpl.add(customer))
				.isInstanceOf(UserIdExistException.class)
				.hasMessage(ID_EXISTS_ERROR_MESSAGE)
				.hasFieldOrPropertyWithValue(STRUCTURED_ERROR_MESSAGES, Collections.singletonList(createStructuredErrorMessageForId()));
	}

	/**
	 * Updating an anonymous customer with valid properties updates and returns the customer.
	 */
	@Test
	public void updateAnonymousCustomerUpdatesAndReturnsCustomer() {
		when(customer.isAnonymous()).thenReturn(true);
		doReturn(Collections.singleton(USER_GUID))
				.when(customerServiceImpl).findCustomerGuidsByUserIdInStoreOrAssociatedStores(USER_ID, TEST_STORE_CODE);
		when(persistenceEngine.update(customer)).thenReturn(customer);

		assertThat(customerServiceImpl.update(customer)).isEqualTo(customer);

		verify(customer).setPreferredBillingAddress(null);
		verify(customer).setPreferredShippingAddress(null);
		verify(validator).validateProperty(customer, "username", Customer.class);
		verify(validator).validateProperty(customer, "email", Customer.class);
		verifyZeroInteractions(userIdentityService, eventMessagePublisher, indexNotificationService);
		verify(persistenceEngine).update(customer);
	}

	/**
	 * Updating a non-anonymous customer with valid properties updates and returns the customer.
	 */
	@Test
	public void updateNonAnonymousCustomerUpdatesAndReturnsCustomer() {
		when(customer.isAnonymous()).thenReturn(false);
		doReturn(Collections.singleton(USER_GUID))
				.when(customerServiceImpl).findCustomerGuidsByUserIdInStoreOrAssociatedStores(USER_ID, TEST_STORE_CODE);
		when(persistenceEngine.update(customer)).thenReturn(customer);

		assertThat(customerServiceImpl.update(customer)).isEqualTo(customer);

		verify(validator).validate(customer);
		verify(persistenceEngine).update(customer);
		verify(indexNotificationService).addNotificationForEntityIndexUpdate(IndexType.CUSTOMER, customer.getUidPk());
	}

	/**
	 * Updating a customer with a user id that already exists in the customer's store throws a
	 * UserIdExistException with a corresponding message and error message.
	 */
	@Test
	public void updatingWithExistingUserIdInStoreThrowsException() {
		doReturn(new HashSet<>(Arrays.asList(USER_GUID, ANOTHER_USER_GUID)))
				.when(customerServiceImpl).findCustomerGuidsByUserIdInStoreOrAssociatedStores(USER_ID, TEST_STORE_CODE);

		assertThatThrownBy(() -> customerServiceImpl.update(customer))
				.isInstanceOf(UserIdExistException.class)
				.hasMessage(ID_EXISTS_ERROR_MESSAGE)
				.hasFieldOrPropertyWithValue(STRUCTURED_ERROR_MESSAGES, Collections.singletonList(createStructuredErrorMessageForId()));
	}

	/**
	 * Updating a customer with invalid properties elicits an EpValidationException with message and list of structured error messages.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void updateCustomerWithValidationViolationsThrowsException() {
		final ConstraintViolation<Customer> constraintViolation = (ConstraintViolation<Customer>) mock(ConstraintViolation.class);

		final Set<ConstraintViolation<Customer>> constraintViolations = new HashSet<>();
		constraintViolations.add(constraintViolation);

		when(customer.isAnonymous()).thenReturn(false);
		doReturn(Collections.singleton(USER_GUID))
				.when(customerServiceImpl).findCustomerGuidsByUserIdInStoreOrAssociatedStores(USER_ID, TEST_STORE_CODE);
		when(validator.validate(customer)).thenReturn((constraintViolations));
		when(constraintViolationTransformer.transform(constraintViolations))
				.thenReturn(Collections.singletonList(createStructuredErrorMessageForId()));

		assertThatThrownBy(() -> customerServiceImpl.update(customer))
				.isInstanceOf(EpValidationException.class)
				.hasMessageContaining("Customer validation failure.")
				.hasFieldOrPropertyWithValue(STRUCTURED_ERROR_MESSAGES, Collections.singletonList(createStructuredErrorMessageForId()));
	}

	/**
	 * Removing a customer removes the customer and the customer's corresponding identity.
	 */
	@Test
	public void verifyRemove() {
		CustomerDeleted customerDeleted = mock(CustomerDeleted.class);
		when(elasticPath.getBean(ContextIdNames.CUSTOMER_DELETED)).thenReturn(customerDeleted);

		customerServiceImpl.remove(customer);

		verify(userIdentityService).remove(USER_ID);
		verify(shopperCleanupService).removeShoppersByCustomer(customer);
		verify(persistenceEngine).delete(customer);
		verify(customerDeleted).setCustomerUid(USER_UIDPK);
		verify(customerDeleted).setDeletedDate(any(Date.class));
		verify(persistenceEngine).save(customerDeleted);
	}

	/**
	 * Verify list() delegates to persistence engine query and returns a list of the corresponding customers.
	 */
	@Test
	public void listDelegatesQueryAndReturnsListOfResultingCustomers() {
		when(persistenceEngine.retrieveByNamedQuery("CUSTOMER_SELECT_ALL")).thenReturn(Collections.singletonList(customer));

		assertThat(customerServiceImpl.list()).isEqualTo(Collections.singletonList(customer));
	}

	/**
	 * Loading a customer with a UIDPK of 0 creates a new customer.
	 */
	@Test
	public void loadWithUidPkOfZeroReturnsNewCustomerFromBean() {
		assertThat(customerServiceImpl.load(0)).isEqualTo(customer);
		verify(elasticPath).getBean(ContextIdNames.CUSTOMER);
	}

	/**
	 * Loading a customer with a UIDPK greater than 0 returns the corresponding customer from the persistence engine.
	 */
	@Test
	public void loadWithUidPkGreaterThanZeroReturnsTheCorrespondingCustomerFromThePersistenceEngine() {
		when(persistenceEngine.load(Customer.class, USER_UIDPK)).thenReturn(customer);

		MockCustomerServiceImpl customerService = new MockCustomerServiceImpl();

		customerService.setPersistenceEngine(persistenceEngine);
		customerService.setElasticPath(elasticPath);

		assertThat(customerService.load(USER_UIDPK)).isEqualTo(customer);
	}

	/**
	 * Getting a customer with a valid input UIDPK configures the fetch plan and returns the corresponding customer.
	 */
	@Test
	public void verifyGetDelegationAndProcessing() {
		when(persistenceEngine.get(Customer.class, USER_UIDPK)).thenReturn(customer);

		MockCustomerServiceImpl customerService = new MockCustomerServiceImpl();

		customerService.setPersistenceEngine(persistenceEngine);
		customerService.setElasticPath(elasticPath);
		customerService.setFetchPlanHelper(fetchPlanHelper);

		assertThat(customerService.get(USER_UIDPK)).isEqualTo(customer);
		verify(fetchPlanHelper).setLoadTuners((LoadTuner[]) null);
	}

	/**
	 * Getting a customer with a UIDPK of 0 creates a new customer.
	 */
	@Test
	public void gettingCustomerWithUidPkZeroCreatesNewCustomer() {
		assertThat(customerServiceImpl.get(0)).isEqualTo(customer);
	}

	/**
	 * Null returned when getting a UIDPK that does not correspond to an existing customer.
	 */
	@Test
	public void nullReturnedWhenCustomerDoesNotExistForGivenUidPk() {
		MockCustomerServiceImpl customerService = new MockCustomerServiceImpl();

		customerService.setPersistenceEngine(persistenceEngine);
		customerService.setElasticPath(elasticPath);
		customerService.setFetchPlanHelper(fetchPlanHelper);

		assertThat(customerService.get(USER_UIDPK)).isEqualTo(null);

		verify(fetchPlanHelper).setLoadTuners((LoadTuner[]) null);
	}

	/**
	 * Getting with a FetchGroupLoadTuner should delegate the given FetchGroupLoadTuner.
	 */
	@Test
	public void gettingWithFetchGroupLoadTunerDelegatesToConfigureFetchPlanUtil() {
		FetchGroupLoadTuner fetchGroupLoadTuner = mock(FetchGroupLoadTuner.class);

		when(persistenceEngine.get(Customer.class, USER_UIDPK)).thenReturn(customer);

		MockCustomerServiceImpl customerService = new MockCustomerServiceImpl();

		customerService.setPersistenceEngine(persistenceEngine);
		customerService.setElasticPath(elasticPath);
		customerService.setFetchPlanHelper(fetchPlanHelper);

		assertThat(customerService.get(USER_UIDPK, fetchGroupLoadTuner)).isEqualTo(customer);
		verify(fetchPlanHelper).setLoadTuners(fetchGroupLoadTuner);
	}

	/**
	 * Setting password sets the password for the customer and customer's identity, updates the customer, and returns the updated customer.
	 */
	@Test
	public void setPasswordSetsThePasswordAndReturnsTheCustomer() {
		doReturn(customer).when(customerServiceImpl).update(customer);

		assertThat(customerServiceImpl.setPassword(customer, NEW_PASSWORD)).isEqualTo(customer);

		verify(customer).setClearTextPassword(NEW_PASSWORD);
		verify(userIdentityService).setPassword(USER_ID, NEW_PASSWORD);
		verify(customerServiceImpl).update(customer);
	}

	/**
	 * Changing password and sending email delegates to set the password and publish the corresponding event.
	 */
	@Test
	public void changePasswordAndSendEmailSetsPasswordAndPublishsEvent() {
		when(elasticPath.getBean(ContextIdNames.CUSTOMER_SERVICE)).thenReturn(customerServiceImpl);
		doReturn(customer).when(customerServiceImpl).update(customer);

		customerServiceImpl.setElasticPath(elasticPath);

		assertThat(customerServiceImpl.changePasswordAndSendEmail(customer, NEW_PASSWORD)).isEqualTo(customer);
		verify(customerServiceImpl).setPassword(customer, NEW_PASSWORD);
		verify(eventMessagePublisher).publish(any());
	}

	/**
	 * Reset password throws UserIdNonExistException with appropriate message when customer does not exist for the input USERID in the given store.
	 */
	@Test
	public void resetPasswordThrowsExceptionWhenCustomerDoesNotExist() {
		assertThatThrownBy(() -> customerServiceImpl.resetPassword(USER_ID, TEST_STORE_CODE))
				.isInstanceOf(UserIdNonExistException.class)
				.hasMessage("The given user id doesn't exist: " + USER_ID + " In store: " + TEST_STORE_CODE);
		verify(customerServiceImpl).findByUserId(USER_ID, TEST_STORE_CODE);
	}

	/**
	 * Reset password finds the customer and resets the password when a valid customer is found.
	 */
	@Test
	public void resetPasswordFindsCustomerAndResetsPassword() {
		when(elasticPath.getBean(ContextIdNames.CUSTOMER_SERVICE)).thenReturn(customerServiceImpl);
		doReturn(customer).when(customerServiceImpl).findByUserId(USER_ID, TEST_STORE_CODE);
		doReturn(customer).when(customerServiceImpl).auditableResetPassword(customer);

		customerServiceImpl.resetPassword(USER_ID, TEST_STORE_CODE);
		verify(customerServiceImpl).findByUserId(USER_ID, TEST_STORE_CODE);
		verify(customerServiceImpl).auditableResetPassword(customer);
	}

	/**
	 * AuditableResetPassword(...) sets the password on the customer's identity, updates the customer, publishes
	 * the corresponding event, and returns the updated customer.
	 */
	@Test
	public void auditableResetPasswordUpdatesAndReturnsCustomer() {
		final String password = "Password";

		when(customer.resetPassword()).thenReturn(password);
		when(customer.getClearTextPassword()).thenReturn(password);
		doReturn(customer).when(customerServiceImpl).update(customer);

		assertThat(customerServiceImpl.auditableResetPassword(customer)).isEqualTo(customer);

		verify(userIdentityService).setPassword(USER_ID, password);
		verify(customerServiceImpl).update(customer);
		verify(eventMessagePublisher).publish(any());
	}

	/**
	 * Finding customers with a given list of UIDPKs delegates a query to the persistence engine and returns the resulting list of customers.
	 */
	@Test
	public void findByUidsDelegatesToPersistenceEngineAndReturnsCustomers() {
		when(persistenceEngine.retrieveByNamedQueryWithList("CUSTOMER_FIND_BY_UIDS", LIST, Collections.singletonList(USER_UIDPK)))
				.thenReturn(Collections.singletonList(customer));

		assertThat(customerServiceImpl.findByUids(Collections.singletonList(USER_UIDPK))).isEqualTo(Collections.singletonList(customer));
	}

	/**
	 * Finding customers with a given list of UIDPKs returns an empty list if the list of UIDPKs is empty or null.
	 */
	@Test
	public void findByUidsReturnsEmptyListWithNoInputUids() {
		assertThat(customerServiceImpl.findByUids(Collections.emptyList())).isEqualTo(Collections.emptyList());
		assertThat(customerServiceImpl.findByUids(null)).isEqualTo(Collections.emptyList());
	}

	/**
	 * Finding customers with a given list of UIDPKs returns an empty list if no customers are found for the input UIDPKs.
	 */
	@Test
	public void findByUidsReturnsEmptyListWhenNoCustomersFound() {
		when(persistenceEngine.retrieveByNamedQueryWithList("CUSTOMER_FIND_BY_UIDS", LIST, Collections.singletonList(USER_UIDPK)))
				.thenReturn(Collections.emptyList());
		assertThat(customerServiceImpl.findByUids(Collections.singletonList(USER_UIDPK))).isEqualTo(Collections.emptyList());
	}

	/**
	 * Validating with an invalid customer address elicits an EpValidationException.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void validateInvalidCustomerAddressThrowsException() {
		final ConstraintViolation<CustomerAddress> constraintViolation = (ConstraintViolation<CustomerAddress>) mock(ConstraintViolation.class);
		CustomerAddress customerAddress = mock(CustomerAddress.class);

		final Set<ConstraintViolation<CustomerAddress>> constraintViolations = new HashSet<>();
		constraintViolations.add(constraintViolation);
		when(validator.validate(customerAddress)).thenReturn(constraintViolations);
		when(constraintViolationTransformer.transform(constraintViolations))
				.thenReturn(Collections.singletonList(createStructuredErrorMessageForId()));

		assertThatThrownBy(() -> customerServiceImpl.validateCustomerAddress(customerAddress))
				.isInstanceOf(EpValidationException.class)
				.hasMessageContaining("Address validation failure.")
				.hasFieldOrPropertyWithValue(STRUCTURED_ERROR_MESSAGES, Collections.singletonList(createStructuredErrorMessageForId()));
	}

	/**
	 * Adding or updating address with a valid input address adds the address to the given customer, updates the customer, and returns the customer.
	 */
	@Test
	public void addOrUpdateAddressWithValidAddressUpdatesAndReturnsCustomer() {
		CustomerAddress customerAddress = mock(CustomerAddress.class);

		doNothing().when(customerServiceImpl).validateCustomerAddress(customerAddress);
		doReturn(customer).when(customerServiceImpl).get(USER_UIDPK);
		doReturn(customer).when(customerServiceImpl).update(customer);
		when(customerAddress.getUidPk()).thenReturn(0L);

		assertThat(customerServiceImpl.addOrUpdateAddress(customer, customerAddress)).isEqualTo(customer);

		verify(customerServiceImpl).validateCustomerAddress(customerAddress);
		verify(customer).addAddress(customerAddress);
		verify(customerServiceImpl).update(customer);
	}

	/**
	 * Adding or updating an address with an invalid address throws an EpValidationException.
	 */
	@Test
	public void addOrUpdateAddressWithInvalidAddressThrowsException() {
		CustomerAddress customerAddress = mock(CustomerAddress.class);

		Throwable exception = new EpValidationException("Address validation failure.", Collections.emptyList());

		doThrow(exception).when(customerServiceImpl).validateCustomerAddress(customerAddress);

		assertThatThrownBy(() -> customerServiceImpl.addOrUpdateAddress(customer, customerAddress))
				.isEqualTo(exception);
	}

	/**
	 * Adding or updating a customer with a new address creates a new address, sets it as the customer's preferred address,
	 * updates the customer, and returns the updated customer.
	 */
	@Test
	public void addOrUpdateCustomerShippingAddressWithNewAddressCreatesAndSetsAddressAndReturnsCustomer() {
		CustomerAddress customerAddress = mock(CustomerAddress.class);
		when(customerAddress.getUidPk()).thenReturn(0L);
		doReturn(customer).when(customerServiceImpl).get(USER_UIDPK);
		doReturn(customer).when(customerServiceImpl).update(customer);

		assertThat(customerServiceImpl.addOrUpdateCustomerShippingAddress(customer, customerAddress)).isEqualTo(customer);
		verify(customer).addAddress(customerAddress);
		verify(customer).setPreferredShippingAddress(customerAddress);
		verify(customerServiceImpl).update(customer);
	}

	/**
	 * Adding or updating a customer with an existing address sets it as the preferred address, updates the customer, and returns the customer.
	 */
	@Test
	public void addOrUpdateCustomerShippingAddressWithExistingAddressUpdatesAndReturnsCustomer() {
		final long addressUid = 1L;
		CustomerAddress customerAddress = mock(CustomerAddress.class);
		when(customerAddress.getUidPk()).thenReturn(addressUid);
		when(customer.getAddressByUid(addressUid)).thenReturn(customerAddress);
		doReturn(customer).when(customerServiceImpl).get(USER_UIDPK);
		doReturn(customer).when(customerServiceImpl).update(customer);

		assertThat(customerServiceImpl.addOrUpdateCustomerShippingAddress(customer, customerAddress)).isEqualTo(customer);
		verify(customer).setPreferredShippingAddress(customerAddress);
		verify(customerAddress).copyFrom(customerAddress);
		verify(customerServiceImpl).update(customer);
	}

	/**
	 * Attempting to add or update a customer with an address UID that is not found in the customer's address list throws an EpDomainException.
	 */
	@Test
	public void addOrUpdateCustomerShippingAddressWithNonExistentAddressUidThrowsException() {
		final long addressUid = 1L;
		CustomerAddress customerAddress = mock(CustomerAddress.class);
		when(customerAddress.getUidPk()).thenReturn(addressUid);
		when(customer.getAddressByUid(addressUid)).thenReturn(null);
		doReturn(customer).when(customerServiceImpl).get(USER_UIDPK);

		assertThatThrownBy(() -> customerServiceImpl.addOrUpdateCustomerShippingAddress(customer, customerAddress))
				.isInstanceOf(EpDomainException.class)
				.hasMessage("Address with uid " + addressUid + " was not found in the customer's address list");
	}

	/**
	 * Adding or updating a customer with a new billing address creates a new address, sets it as the preferred address,
	 * updates the customer, and returns the updated customer.
	 */
	@Test
	public void addOrUpdateCustomerBillingAddressWithNewAddressUpdatesAndReturnsCustomer() {
		CustomerAddress customerAddress = mock(CustomerAddress.class);
		when(customerAddress.getUidPk()).thenReturn(0L);
		doReturn(customer).when(customerServiceImpl).get(USER_UIDPK);
		doReturn(customer).when(customerServiceImpl).update(customer);

		assertThat(customerServiceImpl.addOrUpdateCustomerBillingAddress(customer, customerAddress)).isEqualTo(customer);
		verify(customer).addAddress(customerAddress);
		verify(customer).setPreferredBillingAddress(customerAddress);
		verify(customerServiceImpl).update(customer);
	}

	/**
	 * Adding or updating a customer with an existing billing address sets it as the preferred address,
	 * updates the customer, and returns the updated customer.
	 */
	@Test
	public void addOrUpdateCustomerBillingAddressWithExistingAddressUpdatesAndReturnsCustomer() {
		final long addressUid = 1L;
		CustomerAddress customerAddress = mock(CustomerAddress.class);
		when(customerAddress.getUidPk()).thenReturn(addressUid);
		when(customer.getAddressByUid(addressUid)).thenReturn(customerAddress);
		doReturn(customer).when(customerServiceImpl).get(USER_UIDPK);
		doReturn(customer).when(customerServiceImpl).update(customer);

		assertThat(customerServiceImpl.addOrUpdateCustomerBillingAddress(customer, customerAddress)).isEqualTo(customer);
		verify(customer).setPreferredBillingAddress(customerAddress);
		verify(customerAddress).copyFrom(customerAddress);
		verify(customerServiceImpl).update(customer);
	}

	/**
	 * Attempting to add or update a customer billing address with an address UID
	 * that is not found in the customer's address list throws an EpDomainException.
	 */
	@Test
	public void addOrUpdateCustomerBillingAddressWithNonExistentAddressUidThrowsException() {
		final long addressUid = 1L;
		CustomerAddress customerAddress = mock(CustomerAddress.class);
		when(customerAddress.getUidPk()).thenReturn(addressUid);
		when(customer.getAddressByUid(addressUid)).thenReturn(null);
		doReturn(customer).when(customerServiceImpl).get(USER_UIDPK);

		assertThatThrownBy(() -> customerServiceImpl.addOrUpdateCustomerBillingAddress(customer, customerAddress))
				.isInstanceOf(EpDomainException.class)
				.hasMessage("Address with uid " + addressUid + " was not found in the customer's address list");
	}

	/**
	 * Finding by user id with a null user id throws an EpServiceException with an appropriate message.
	 */
	@Test
	public void findByUserIdWithNullUserIdThrowsException() {
		assertThatThrownBy(() -> customerServiceImpl.findByUserId(null, TEST_STORE_CODE))
				.isInstanceOf(EpServiceException.class)
				.hasMessage(CANNOT_RETRIEVE_CUSTOMER_WITHOUT_USER_ID_OR_STORE);
	}

	/**
	 * Finding by user id with a null store code throws an EpServiceException with an appropriate message.
	 */
	@Test
	public void findByUserIdWithNullStoreCodeThrowsException() {
		assertThatThrownBy(() -> customerServiceImpl.findByUserId(USER_ID, null))
				.isInstanceOf(EpServiceException.class)
				.hasMessage(CANNOT_RETRIEVE_CUSTOMER_WITHOUT_USER_ID_OR_STORE);
	}

	/**
	 * Finding by user id with an invalid store code throws an EpServiceException with an appropriate message.
	 */
	@Test
	public void findByUserIdWithInvalidStoreCodeThrowsException() {
		when(storeService.findStoreWithCode(TEST_STORE_CODE)).thenReturn(null);
		assertThatThrownBy(() -> customerServiceImpl.findByUserId(USER_ID, TEST_STORE_CODE))
				.isInstanceOf(EpServiceException.class)
				.hasMessage("Store with code " + TEST_STORE_CODE + " not found.");
	}

	/**
	 * Finding by user id returns the corresponding customer when the input user id corresponds to a customer in the given store.
	 */
	@Test
	public void findByUserIdWithValidUserIdReturnsCorrespondingCustomer() {
		when(persistenceEngine.retrieveByNamedQueryWithList(any(String.class), any(String.class),
				Matchers.<Collection<Long>>any(), any(Object.class))).thenReturn(Collections.singletonList(customer));

		assertThat(customerServiceImpl.findByUserId(USER_ID, TEST_STORE_CODE)).isEqualTo(customer);
		verify(storeService).findStoreWithCode(TEST_STORE_CODE);
	}

	/**
	 * Finding by user id returns null when the input user id does not correspond to a customer in the given store.
	 */
	@Test
	public void findByUserIdReturnsNullWhenNoStoreCorrespondsToTheGivenStoreCode() {
		when(persistenceEngine.retrieveByNamedQueryWithList(any(String.class), any(String.class),
				Matchers.<Collection<Long>>any(), any(Object.class))).thenReturn(Collections.emptyList());

		assertThat(customerServiceImpl.findByUserId(USER_ID, TEST_STORE_CODE)).isEqualTo(null);
		verify(storeService).findStoreWithCode(TEST_STORE_CODE);
	}

	/**
	 * Verifying a customer with disabled status throws a UserStatusInactiveException.
	 */
	@Test
	public void verifyStructuredErrorMessageWhenCustomerIsNotPersisted() {
		when(customer.isPersisted()).thenReturn(true);
		doReturn(Customer.STATUS_DISABLED).when(customerServiceImpl).findCustomerStatusByUid(USER_UIDPK);

		String errorMessage = "Customer account " + USER_ID + " is not active.";

		StructuredErrorMessage structuredErrorMessage = new StructuredErrorMessage(
				"purchase.user.account.not.active",
				errorMessage,
				ImmutableMap.of(USER_ID_FIELD, USER_ID));

		assertThatThrownBy(() -> customerServiceImpl.verifyCustomer(customer))
				.isInstanceOf(UserStatusInactiveException.class)
				.hasMessage(errorMessage)
				.hasFieldOrPropertyWithValue(STRUCTURED_ERROR_MESSAGES, Collections.singletonList(structuredErrorMessage));
	}

	/**
	 * Verify updateCustomerFromAddress sets customer profile values from address.
	 */
	@Test
	public void testUpdateCustomerFromAddress() {
		when(customer.isPersisted()).thenReturn(true);
		doReturn(customer).when(customerServiceImpl).get(USER_UIDPK);
		doReturn(customer).when(customerServiceImpl).update(customer);

		lenient().when(customer.getFirstName()).thenReturn(null);
		lenient().when(customer.getLastName()).thenReturn(null);
		lenient().when(customer.getPhoneNumber()).thenReturn(null);

		lenient().when(billingAddress.getFirstName()).thenReturn(ADDRESS_FIRST_NAME);
		lenient().when(billingAddress.getLastName()).thenReturn(ADDRESS_LAST_NAME);
		lenient().when(billingAddress.getPhoneNumber()).thenReturn(ADDRESS_PHONE_NUMBER);

		customerServiceImpl.updateCustomerFromAddress(customer, billingAddress);

		verify(customer).setFirstName(ADDRESS_FIRST_NAME);
		verify(customer).setLastName(ADDRESS_LAST_NAME);
		verify(customer).setPhoneNumber(ADDRESS_PHONE_NUMBER);
		verify(customerServiceImpl).update(customer);
	}

	/**
	 * Verify updateCustomerFromAddress does not overwrite existing customer profile values.
	 */
	@Test
	public void testUpdateCustomerFromAddressWithExistingProfile() {
		when(customer.isPersisted()).thenReturn(true);
		doReturn(customer).when(customerServiceImpl).get(USER_UIDPK);

		lenient().when(customer.getFirstName()).thenReturn(USER_FIRST_NAME);
		lenient().when(customer.getLastName()).thenReturn(USER_LAST_NAME);
		lenient().when(customer.getPhoneNumber()).thenReturn(USER_PHONE_NUMBER);

		lenient().when(billingAddress.getFirstName()).thenReturn(ADDRESS_FIRST_NAME);
		lenient().when(billingAddress.getLastName()).thenReturn(ADDRESS_LAST_NAME);
		lenient().when(billingAddress.getPhoneNumber()).thenReturn(ADDRESS_PHONE_NUMBER);

		customerServiceImpl.updateCustomerFromAddress(customer, billingAddress);

		verify(customer, never()).setFirstName(ADDRESS_FIRST_NAME);
		verify(customer, never()).setLastName(ADDRESS_LAST_NAME);
		verify(customer, never()).setPhoneNumber(ADDRESS_PHONE_NUMBER);
		verify(customerServiceImpl, never()).update(customer);
	}

	/**
	 * Verify updateCustomerFromAddress does not set name partially.
	 */
	@Test
	public void testUpdateCustomerFromAddressDoesNotSetPartialName() {
		when(customer.isPersisted()).thenReturn(true);
		doReturn(customer).when(customerServiceImpl).get(USER_UIDPK);
		doReturn(customer).when(customerServiceImpl).update(customer);

		lenient().when(customer.getFirstName()).thenReturn(USER_FIRST_NAME);
		lenient().when(customer.getLastName()).thenReturn(null);

		lenient().when(billingAddress.getFirstName()).thenReturn(ADDRESS_FIRST_NAME);
		lenient().when(billingAddress.getLastName()).thenReturn(ADDRESS_LAST_NAME);

		customerServiceImpl.updateCustomerFromAddress(customer, billingAddress);

		verify(customer, never()).setFirstName(ADDRESS_FIRST_NAME);
		verify(customer, never()).setLastName(ADDRESS_LAST_NAME);
	}

	/**
	 * Helper method returning a new <code>StructuredErrorMessage</code> indicating the user Id already exists.
	 */
	private StructuredErrorMessage createStructuredErrorMessageForId() {
		return new StructuredErrorMessage(
				CustomerMessageIds.USERID_ALREADY_EXISTS,
				ID_EXISTS_ERROR_MESSAGE,
				ImmutableMap.of(USER_ID_FIELD, USER_ID));
	}

	/**
	 * Convenience testing class to control these delegating methods.
	 */
	private class MockCustomerServiceImpl extends CustomerServiceImpl {
		@Override
		public PersistentBeanFinder getPersistentBeanFinder() {
			return super.getPersistentBeanFinder();
		}

		@Override
		public void setPersistenceEngine(final PersistenceEngine persistenceEngine) {
			super.setPersistenceEngine(persistenceEngine);
		}
	}

}
