/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.service.customer.impl;

import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.base.exception.structured.EpValidationException;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.exception.UserIdExistException;
import com.elasticpath.commons.exception.UserIdNonExistException;
import com.elasticpath.commons.exception.UserStatusInactiveException;
import com.elasticpath.core.messaging.customer.CustomerEventType;
import com.elasticpath.domain.EpDomainException;
import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.CustomerDeleted;
import com.elasticpath.domain.customer.CustomerGroup;
import com.elasticpath.domain.customer.CustomerMessageIds;
import com.elasticpath.domain.store.Store;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.EventType;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.persistence.api.FetchGroupLoadTuner;
import com.elasticpath.service.auth.UserIdentityService;
import com.elasticpath.service.customer.CustomerGroupService;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.customer.dao.CustomerAddressDao;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.search.IndexNotificationService;
import com.elasticpath.service.search.IndexType;
import com.elasticpath.service.shopper.ShopperCleanupService;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.settings.SettingsReader;
import com.elasticpath.validation.ConstraintViolationTransformer;

/**
 * The default implementation of <code>CustomerService</code>.
 */
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.TooManyMethods", "PMD.GodClass", "PMD.ExcessiveClassLength", "PMD.NPathComplexity"})
public class CustomerServiceImpl extends AbstractEpPersistenceServiceImpl implements CustomerService {

	private static final String STRUCTURED_ERROR_MESSAGE_DATA_FIELD_UNDEFINED = "undefined";

	private static final String USER_ACCOUNT_NOT_ACTIVE = "purchase.user.account.not.active";

	private CustomerGroupService customerGroupService;

	private UserIdentityService userIdentityService;

	private TimeService timeService;

	private StoreService storeService;

	private static final String LIST_PLACEHOLDER = "list";

	private IndexNotificationService indexNotificationService;

	private ShopperCleanupService shopperCleanupService;

	private CustomerAddressDao customerAddressDao;

	private SettingsReader settingsReader;

	private EventMessageFactory eventMessageFactory;

	private EventMessagePublisher eventMessagePublisher;

	private static final String NEW_PASSWORD_KEY = "newPassword";

	private Validator validator;

	// Turn off line too long PMD warning
	private ConstraintViolationTransformer constraintViolationTransformer; //NOPMD


	public void setValidator(final Validator validator) {
		this.validator = validator;
	}

	public void setConstraintViolationTransformer(
			final ConstraintViolationTransformer constraintViolationTransformer) {
		this.constraintViolationTransformer = constraintViolationTransformer;
	}

	/**
	 * Adds the given customer.
	 *
	 * @param customer the customer to add
	 * @return the persisted instance of customer
	 * @throws UserIdExistException - if trying to add an customer using an user Id.
	 */
	@Override
	public Customer add(final Customer customer) throws UserIdExistException {
		return addByAuthenticate(customer, false);
	}

	/**
	 * Adds the given customer, special treatment for already authenticated user.
	 *
	 * @param customer        the customer to add
	 * @param isAuthenticated true if the Customer is already authenticated via external source
	 * @return the persisted instance of customer
	 * @throws UserIdExistException - if trying to add an customer using an user Id.
	 */
	@Override
	public Customer addByAuthenticate(final Customer customer, final boolean isAuthenticated) throws UserIdExistException {
		sanityCheck();

		final String storeCode = customer.getStoreCode();
		if (StringUtils.isBlank(storeCode)) {
			throw new EpServiceException("Customer has no store attached.");
		}

		final boolean anyCustomerWithUserIdExists =
				findCustomerGuidsByUserIdInStoreOrAssociatedStores(customer.getUserId(), customer.getStoreCode())
						.stream().findAny().isPresent();

		if (anyCustomerWithUserIdExists) {
			String errorMessage = "Customer with the given user Id already exists";
			throw new UserIdExistException(
					errorMessage,
					asList(
							new StructuredErrorMessage(
									CustomerMessageIds.USERID_ALREADY_EXISTS,
									errorMessage,
									ImmutableMap.of("user-id", customer.getUserId() == null
											? STRUCTURED_ERROR_MESSAGE_DATA_FIELD_UNDEFINED : customer.getUserId())
							)
					)
			);
		}

		final String validStoreCode = storeService.findValidStoreCode(storeCode);
		customer.setStoreCode(validStoreCode);

		defaultCustomerGroupCheck(customer);

		if (!customer.isAnonymous() && !isAuthenticated) {
			userIdentityService.add(customer.getUserId(), customer.getClearTextPassword());
		}

		final Date now = timeService.getCurrentTime();
		// update customer object
		customer.setCreationDate(now);

		// persist the customer
		getPersistenceEngine().save(customer);

		// send confirmation email and update index
		if (!customer.isAnonymous()) {
			sendCustomerEvent(CustomerEventType.CUSTOMER_REGISTERED, customer.getGuid(), null);
			indexNotificationService.addNotificationForEntityIndexUpdate(IndexType.CUSTOMER, customer.getUidPk());
		}

		return customer;
	}

	/**
	 * Updates the given customer.
	 *
	 * @param customer the customer to update
	 * @return the new customer object from the persistence layer
	 * @throws UserIdExistException  - if the customer's new user Id is already in use by another existing customer.
	 * @throws EpValidationException - if there is a validation error.
	 */
	@Override
	public Customer update(final Customer customer) throws UserIdExistException {
		sanityCheck();

		final boolean anotherCustomerWithUserIdExists =
				findCustomerGuidsByUserIdInStoreOrAssociatedStores(customer.getUserId(), customer.getStoreCode())
						.stream().anyMatch(guid -> !guid.equals(customer.getGuid()));

		if (anotherCustomerWithUserIdExists) {
			String errorMessage = "Customer with the given user Id already exists";
			throw new UserIdExistException(
					errorMessage,
					asList(
							new StructuredErrorMessage(
									CustomerMessageIds.USERID_ALREADY_EXISTS,
									errorMessage,
									ImmutableMap.of("user-id", customer.getUserId() == null
											? STRUCTURED_ERROR_MESSAGE_DATA_FIELD_UNDEFINED : customer.getUserId())
							)
					)
			);
		}

		Set<ConstraintViolation<Customer>> customerViolations;

		// Validate and convert errors into commerce message list. For anonymous users, only validate username and email properties.
		if (customer.isAnonymous()) {
			customerViolations = Stream.concat(
					validator.validateProperty(customer, "username", Customer.class).stream(),
					validator.validateProperty(customer, "email", Customer.class).stream()).collect(Collectors.toSet());
		} else {
			customerViolations = validator.validate(customer);
		}

		if (CollectionUtils.isNotEmpty(customerViolations)) {
			List<StructuredErrorMessage> structuredErrorMessageList = constraintViolationTransformer.transform(customerViolations);
			throw new EpValidationException("Customer validation failure.", structuredErrorMessageList);
		}

		defaultCustomerAddressesCheck(customer);
		defaultCustomerGroupCheck(customer);

		Customer persistedCustomer = getPersistenceEngine().update(customer);

		if (isNotSearchable(customer)) {
			return persistedCustomer;
		}

		indexNotificationService.addNotificationForEntityIndexUpdate(IndexType.CUSTOMER, persistedCustomer.getUidPk());
		return persistedCustomer;
	}

	private boolean isNotSearchable(final Customer customer) {
		return customer.isAnonymous() && StringUtils.isEmpty(customer.getFirstName()) && StringUtils.isEmpty(customer.getLastName());
	}

	/**
	 * Check if the customer is in the default customerGroup. If not add him/her in.
	 *
	 * @param customer the customer to do the defaultCustomerGroupCheck on.
	 * @throws EpServiceException in case of any errors.
	 */
	private void defaultCustomerGroupCheck(final Customer customer) throws EpServiceException {
		if (!customer.belongsToCustomerGroup(CustomerGroup.DEFAULT_GROUP_NAME)) {
			if (customerGroupService == null) {
				throw new EpServiceException("CustomerGroupService not set");
			}
			final CustomerGroup defaultCG = customerGroupService.getDefaultGroup();
			if (defaultCG == null) {
				throw new EpServiceException("The default customer group \"PUBLIC\" not set");
			}
			customer.addCustomerGroup(defaultCG);
		}
	}

	/**
	 * Check the customer's preferred/default addresses to see if they've been removed. If they have, then remove the default address reference.
	 *
	 * @param customer the customer to check default addresses.
	 * @throws EpServiceException in case of any errors.
	 */
	private void defaultCustomerAddressesCheck(final Customer customer) throws EpServiceException {
		if (!customer.getAddresses().contains(customer.getPreferredBillingAddress())) {
			customer.setPreferredBillingAddress(null);
		}
		if (!customer.getAddresses().contains(customer.getPreferredShippingAddress())) {
			customer.setPreferredShippingAddress(null);
		}
	}

	/**
	 * Adds a customer to the default customer group (ensuring that they have the default role).
	 *
	 * @param customer the customer upon which to set the default group
	 * @throws EpServiceException in case of any errors.
	 */
	@Override
	public void setCustomerDefaultGroup(final Customer customer) throws EpServiceException {
		defaultCustomerGroupCheck(customer);
	}

	/**
	 * Deletes the customer.
	 *
	 * @param customer the customer to remove
	 * @throws EpServiceException - in case of any errors
	 */
	@Override
	public void remove(final Customer customer) throws EpServiceException {
		sanityCheck();

		userIdentityService.remove(customer.getUserId());
		shopperCleanupService.removeShoppersByCustomer(customer);
		getPersistenceEngine().delete(customer);
		addCustomerDeletedForAuditing(customer.getUidPk());
	}

	private void addCustomerDeletedForAuditing(final long uid) {
		final CustomerDeleted customerDeleted = getBean(ContextIdNames.CUSTOMER_DELETED);
		customerDeleted.setCustomerUid(uid);
		customerDeleted.setDeletedDate(new Date());
		getPersistenceEngine().save(customerDeleted);
	}

	/**
	 * Lists all customers stored in the database.
	 *
	 * @return a list of customers
	 * @throws EpServiceException - in case of any errors
	 */
	@Override
	public List<Customer> list() throws EpServiceException {
		sanityCheck();
		return getPersistenceEngine().retrieveByNamedQuery("CUSTOMER_SELECT_ALL");
	}

	/**
	 * Load the customer with the given UID. Throw an unrecoverable exception if there is no matching database row.
	 *
	 * @param customerUid the customer UID
	 * @return the customer if UID exists, otherwise null
	 * @throws EpServiceException - in case of any errors
	 */
	@Override
	public Customer load(final long customerUid) throws EpServiceException {
		sanityCheck();
		Customer customer = null;
		if (customerUid <= 0) {
			customer = getBean(ContextIdNames.CUSTOMER);
		} else {
			customer = getPersistentBeanFinder().load(ContextIdNames.CUSTOMER, customerUid);
		}
		return customer;
	}

	/**
	 * Get the customer with the given UID. Return null if no matching record exists.
	 *
	 * @param customerUid the customer UID
	 * @return the customer if UID exists, otherwise null
	 * @throws EpServiceException - in case of any errors
	 */
	@Override
	public Customer get(final long customerUid) throws EpServiceException {
		return get(customerUid, null);
	}

	/**
	 * Get the customer with the given UID. Return <code>null</code>l if no matching record exists.
	 *
	 * @param customerUid the customer UID
	 * @param loadTuner   the load tuner to tune the results, or <code>null</code> for the default tuner
	 * @return the customer if UID exists, otherwise <code>null</code>
	 * @throws EpServiceException - in case of any errors
	 */
	@Override
	public Customer get(final long customerUid, final FetchGroupLoadTuner loadTuner) throws EpServiceException {
		sanityCheck();
		Customer customer;
		if (customerUid <= 0) {
			customer = getBean(ContextIdNames.CUSTOMER);
		} else {
			customer = getPersistentBeanFinder()
				.withLoadTuners(loadTuner)
				.get(ContextIdNames.CUSTOMER, customerUid);
		}
		return customer;
	}

	@Override
	public Customer findByGuid(final String guid, final FetchGroupLoadTuner loadTuner) throws EpServiceException {
		getFetchPlanHelper().setLoadTuners(loadTuner);
		return findByGuid(guid);
	}

	/**
	 * Retrieve the customer with the given guid.
	 *
	 * @param guid the guid of the customer
	 * @return the customer with the given guid
	 * @throws EpServiceException in case of any error
	 */
	@Override
	public Customer findByGuid(final String guid) throws EpServiceException {
		final List<Customer> customers = getPersistenceEngine().retrieveByNamedQuery("CUSTOMER_FIND_BY_GUID", guid);
		if (customers.isEmpty()) {
			return null;
		}
		if (customers.size() > 1) {
			throw new EpServiceException("Inconsistent data -- duplicate guid:" + guid);
		}
		return customers.get(0);
	}

	@Override
	public boolean isCustomerGuidExists(final String guid) {
		return !getPersistenceEngine().retrieveByNamedQuery("CUSTOMER_EXISTS_BY_GUID", guid).isEmpty();
	}

	/**
	 * Generic load method for all persistable domain models.
	 *
	 * @param uid the persisted instance uid
	 * @return the persisted instance if exists, otherwise null
	 * @throws EpServiceException - in case of any errors
	 */
	@Override
	public Object getObject(final long uid) throws EpServiceException {
		return get(uid);
	}

	/**
	 * Find the customer with the given userId associated with the store. If it cannot find the customer in the given store, returns oldest record
	 * from the store's associated stores.
	 *
	 * @param userId    the customer userId
	 * @param storeCode the store to search in
	 * @return the customers with the given userId.
	 * @throws EpServiceException - in case of any errors
	 */
	@Override
	public Customer findByUserId(final String userId, final String storeCode) throws EpServiceException {
		sanityCheck();
		if (userId == null || storeCode == null) {
			throw new EpServiceException("Cannot retrieve customer without userId or store");
		}

		Store store = storeService.findStoreWithCode(storeCode);
		if (store == null) {
			throw new EpServiceException("Store with code " + storeCode + " not found.");
		}

		Set<Long> storeUids = new HashSet<>();
		storeUids.add(store.getUidPk());
		storeUids.addAll(store.getAssociatedStoreUids());

		final List<Customer> allResults = getPersistenceEngine().retrieveByNamedQueryWithList("CUSTOMER_FIND_BY_USERID_IN_STORES",
				LIST_PLACEHOLDER,
				storeUids,
				userId);

		return getCustomerForStoreOrNewest(allResults, store);
	}

	/**
	 * Returns either the customer matching the store, or if matching store not found, returns the most recently created customer.
	 *
	 * @param customers
	 * @param store
	 * @return customer
	 */
	private Customer getCustomerForStoreOrNewest(final List<Customer> customers, final Store store) {
		Customer customer = null;
		for (Customer candidateCustomer : customers) {
			if (customer == null || candidateCustomer.getCreationDate().after(customer.getCreationDate())) {
				customer = candidateCustomer;
			}
			if (candidateCustomer.getStoreCode().equals(store.getCode())) {
				break;
			}
		}
		if (customer != null) {
			// Field is updated for customers that are not native to the store
			customer.setStoreCode(store.getCode());
		}

		return customer;
	}

	@Override
	public void resetPassword(final String userId, final String storeCode) throws UserIdNonExistException {
		Customer customer = findByUserId(userId, storeCode);

		if (customer == null) {
			throw new UserIdNonExistException("The given user id doesn't exist: " + userId + " In store: " + storeCode);
		}

		// Needed in order to get Spring to create another transactional context
		getCustomerService().auditableResetPassword(customer);
	}

	@Override
	public Customer auditableResetPassword(final Customer customer) {
		final String newPassword = customer.resetPassword();

		userIdentityService.setPassword(customer.getUserId(), customer.getClearTextPassword());
		final Customer updatedCustomer = update(customer);

		final Map<String, Object> data = new HashMap<>();
		data.put(NEW_PASSWORD_KEY, newPassword);
		sendCustomerEvent(CustomerEventType.PASSWORD_FORGOTTEN, updatedCustomer.getGuid(), data);

		return updatedCustomer;
	}

	@Override
	public Customer setPassword(final Customer customer, final String newPassword) {
		customer.setClearTextPassword(newPassword);

		userIdentityService.setPassword(customer.getUserId(), newPassword);
		return update(customer);
	}

	/**
	 * {@inheritDoc} This method is not a transaction boundary. Instead, the two steps of setting the password, and sending the confirmation email
	 * are done in separate transactions to avoid deadlocks.
	 */
	@Override
	public Customer changePasswordAndSendEmail(final Customer customer, final String newPassword) {
		/*
		 * Need to get a new handle to CustomerService through the bean factory in order to trigger Spring's transaction management. Calling the
		 * setPassword method directly will not work.
		 */
		final Customer updatedCustomer = getCustomerService().setPassword(customer, newPassword);

		sendCustomerEvent(CustomerEventType.PASSWORD_CHANGED, updatedCustomer.getGuid(), null);
		return updatedCustomer;
	}

	/**
	 * Set the customerGroupService instance.
	 *
	 * @param customerGroupService the customerGroupService instance.
	 */
	public void setCustomerGroupService(final CustomerGroupService customerGroupService) {
		this.customerGroupService = customerGroupService;
	}

	/**
	 * Retrieve the list of customers, whose specified property contain the given criteria value.
	 *
	 * @param propertyName  customer property to search on.
	 * @param criteriaValue criteria value to be used for searching.
	 * @return list of customers matching the given criteria.
	 */
	@Override
	public List<Customer> findCustomerLike(final String propertyName, final String criteriaValue) {
		if (propertyName == null || propertyName.length() == 0) {
			throw new EpServiceException("propertyName not set");
		}
		if (criteriaValue == null || criteriaValue.trim().length() == 0) {
			return null;
		}
		sanityCheck();
		return getPersistenceEngine().retrieve("SELECT c FROM CustomerImpl WHERE c." + propertyName + " LIKE ?1", "%" + criteriaValue + "%");
	}

	/**
	 * Returns a list of <code>Customer</code> based on the given uids. The returned customers will be populated based on the given load tuner.
	 *
	 * @param customerUids a collection of customer uids
	 * @return a list of <code>Customer</code>s
	 */
	@Override
	public List<Customer> findByUids(final Collection<Long> customerUids) {
		sanityCheck();

		if (customerUids == null || customerUids.isEmpty()) {
			return Collections.emptyList();
		}

		return getPersistenceEngine().retrieveByNamedQueryWithList("CUSTOMER_FIND_BY_UIDS", LIST_PLACEHOLDER, customerUids);
	}

	/**
	 * Looks up customer guids by user id within a store and its associated stores.
	 *
	 * @param userId  the user id
	 * @param storeCode the store code
	 * @return a set of customer guids
	 * @throws EpServiceException - in case of any errors
	 */
	protected Set<String> findCustomerGuidsByUserIdInStoreOrAssociatedStores(
			final String userId, final String storeCode) throws EpServiceException {

		List<String> results = getPersistenceEngine().retrieveByNamedQuery(
				"CUSTOMER_GUIDS_BY_USERID_IN_STORE_AND_ASSOCIATED_STORES",
				userId,
				storeCode);

		return new HashSet<>(results);
	}

	/**
	 * Returns all customer uids as a list.
	 *
	 * @return all customer uids as a list
	 */
	@Override
	public List<Long> findAllUids() {
		sanityCheck();
		return getPersistenceEngine().retrieveByNamedQuery("CUSTOMER_UIDS_ALL");
	}

	@Override
	public Collection<Long> filterSearchable(final Collection<Long> uids) {
		sanityCheck();
		List<Long> anonymousCustomerUids = getPersistenceEngine().retrieveByNamedQueryWithList(
				"CUSTOMER_UIDS_FILTER_NON_ANONYMOUS", "list", uids);
		return new HashSet<>(anonymousCustomerUids);
	}

	/**
	 * Retrieves list of searchable <code>Customer</code> uids where the last modified date is later than the specified date.
	 * A customer is searchable if they have defined their first name or last name.
	 *
	 * @param date date to compare with the last modified date
	 * @return list of <code>Customer</code> whose last modified date is later than the specified date
	 */
	@Override
	public List<Long> findSearchableUidsByModifiedDate(final Date date) {
		sanityCheck();
		return getPersistenceEngine().retrieveByNamedQuery("SEARCHABLE_CUSTOMER_UIDS_SELECT_BY_MODIFIED_DATE", date);
	}

	/**
	 * Retrieves customer status by customer uidpk.
	 *
	 * @param customerUid long
	 * @return customer status
	 */
	@Override
	public int findCustomerStatusByUid(final long customerUid) {
		sanityCheck();
		return getPersistenceEngine().<Integer>retrieveByNamedQuery("CUSTOMER_STATUS_BY_UID", customerUid).get(0);
	}

	/**
	 * Retrieves list of customer uids where the deleted date is later than the specified date.
	 *
	 * @param date date to compare with the deleted date
	 * @return list of customer uids whose deleted date is later than the specified date
	 */
	@Override
	public List<Long> findUidsByDeletedDate(final Date date) {
		sanityCheck();
		return getPersistenceEngine().retrieveByNamedQuery("CUSTOMER_UIDS_SELECT_BY_DELETED_DATE", date);
	}

	/**
	 * Retrieves a collection of Customer accounts associated with the specified CustomerGroup.
	 *
	 * @param groupName the customer group
	 * @return collection of Customer accounts associated with the group
	 */
	@Override
	public List<Customer> findCustomersByCustomerGroup(final String groupName) {
		sanityCheck();
		return getPersistenceEngine().retrieveByNamedQuery("FIND_CUSTOMERS_BY_CUSTOMER_GROUP", groupName);
	}

	/**
	 * Set the userIdentityService instance.
	 *
	 * @param userIdentityService the userIdentityService instance.
	 */
	@Override
	public void setUserIdentityService(final UserIdentityService userIdentityService) {
		this.userIdentityService = userIdentityService;
	}

	/**
	 * Set the time service.
	 *
	 * @param timeService the <code>TimeService</code> instance.
	 */
	public void setTimeService(final TimeService timeService) {
		this.timeService = timeService;
	}

	@Override
	public CustomerAddress getCustomerAddress(final long addressUid) {
		return (CustomerAddress) customerAddressDao.get(addressUid);
	}

	/**
	 * Verifies the customer status.
	 *
	 * @param customer the customer need to be verified.
	 * @throws UserStatusInactiveException if the customer is inactive
	 */
	@Override
	public void verifyCustomer(final Customer customer) throws UserStatusInactiveException {
		if (customer.isPersisted()) {
			final int status = this.findCustomerStatusByUid(customer.getUidPk());
			if (status != Customer.STATUS_ACTIVE) {
				String errorMessage = "Customer account " + customer.getUserId() + " is not active.";
				throw new UserStatusInactiveException(
						errorMessage,
						asList(
								new StructuredErrorMessage(
										USER_ACCOUNT_NOT_ACTIVE,
										errorMessage,
										ImmutableMap.of("user-id", String.valueOf(customer.getUserId()))
								)
						)
				);
			}
		}
	}

	/**
	 * Sets the store service.
	 *
	 * @param storeService the store service
	 */
	public void setStoreService(final StoreService storeService) {
		this.storeService = storeService;
	}

	@Override
	public Customer addOrUpdateAddress(final Customer customer, final CustomerAddress address) {
		validateCustomerAddress(address);
		Customer freshCustomer = get(customer.getUidPk());
		addOrUpdateCustomerAddress(freshCustomer, address);
		return update(freshCustomer);
	}

	/**
	 * Validates the CustomerAddress data.
	 *
	 * @param address the customer address.
	 */
	protected void validateCustomerAddress(final CustomerAddress address) {
		Set<ConstraintViolation<CustomerAddress>> addressViolations = validator.validate(address);
		if (!addressViolations.isEmpty()) {
			List<StructuredErrorMessage> structuredErrorMessageList = constraintViolationTransformer.transform(addressViolations);
			throw new EpValidationException("Address validation failure.", structuredErrorMessageList);
		}
	}

	@Override
	public Customer addOrUpdateCustomerBillingAddress(final Customer customer, final CustomerAddress address) {
		Customer freshCustomer = get(customer.getUidPk());
		final CustomerAddress updatedAddress = addOrUpdateCustomerAddress(freshCustomer, address);
		freshCustomer.setPreferredBillingAddress(updatedAddress);
		return update(freshCustomer);
	}

	@Override
	public Customer addOrUpdateCustomerShippingAddress(final Customer customer, final CustomerAddress address) {
		Customer freshCustomer = get(customer.getUidPk());
		final CustomerAddress updatedAddress = addOrUpdateCustomerAddress(freshCustomer, address);
		freshCustomer.setPreferredShippingAddress(updatedAddress);
		return update(freshCustomer);
	}

	private CustomerAddress addOrUpdateCustomerAddress(final Customer customer, final CustomerAddress address) {
		if (address.getUidPk() == 0) {
			customer.addAddress(address);
			return address;
		}

		CustomerAddress customerAddress = customer.getAddressByUid(address.getUidPk());
		if (customerAddress == null) {
			throw new EpDomainException("Address with uid " + address.getUidPk() + " was not found in the customer's address list");
		}
		customerAddress.copyFrom(address);
		return customerAddress;
	}

	@Override
	public Customer updateCustomerFromAddress(final Customer customer, final Address address) {
		Customer customerToUpdate = customer;
		if (customer.isPersisted()) {
			customerToUpdate = get(customer.getUidPk());
		}

		boolean modified = setCustomerProfileFromAddress(customerToUpdate, address);
		if (modified) {
			return update(customerToUpdate);
		} else {
			return customerToUpdate;
		}
	}

	private boolean setCustomerProfileFromAddress(final Customer customer, final Address address) {
		boolean modified = false;

		// First & last names belong together
		if (customer.getFirstName() == null && customer.getLastName() == null) {
			customer.setFirstName(address.getFirstName());
			customer.setLastName(address.getLastName());
			modified = true;
		}

		if (customer.getPhoneNumber() == null) {
			customer.setPhoneNumber(address.getPhoneNumber());
			modified = true;
		}

		return modified;
	}

	/**
	 * @param indexNotificationService instance to set
	 */
	public void setIndexNotificationService(final IndexNotificationService indexNotificationService) {
		this.indexNotificationService = indexNotificationService;
	}

	/**
	 * Triggers a customer event.
	 *
	 * @param eventType    the type of Customer Event to trigger
	 * @param customerGuid the guid of the customer associated with the event
	 */
	private void sendCustomerEvent(final EventType eventType, final String customerGuid, final Map<String, Object> additionalData) {
		// Send notification via messaging system
		try {
			final EventMessage orderCreatedEventMessage = getEventMessageFactory().createEventMessage(eventType, customerGuid, additionalData);

			getEventMessagePublisher().publish(orderCreatedEventMessage);

		} catch (final Exception e) {
			throw new EpSystemException("Failed to publish Event Message", e);
		}
	}

	private CustomerService getCustomerService() {
		return getBean(ContextIdNames.CUSTOMER_SERVICE);
	}

	/**
	 * @param customerAddressDao The CustomerAddressDao to set and use.
	 */
	public void setCustomerAddressDao(final CustomerAddressDao customerAddressDao) {
		this.customerAddressDao = customerAddressDao;
	}

	/**
	 * @param shopperCleanupService the shopperCleanupService to set
	 */
	public void setShopperCleanupService(final ShopperCleanupService shopperCleanupService) {
		this.shopperCleanupService = shopperCleanupService;
	}

	@Override
	public Date getCustomerLastModifiedDate(final String customerGuid) {
		final List<Date> dates = getPersistenceEngine().retrieveByNamedQuery("CUSTOMER_LAST_MODIFIED_DATE", customerGuid);
		if (dates.isEmpty()) {
			return null;
		}
		return dates.get(0);
	}

	public void setSettingsReader(final SettingsReader settingsReader) {
		this.settingsReader = settingsReader;
	}

	protected SettingsReader getSettingsReader() {
		return settingsReader;
	}

	public void setEventMessageFactory(final EventMessageFactory eventMessageFactory) {
		this.eventMessageFactory = eventMessageFactory;
	}

	protected EventMessageFactory getEventMessageFactory() {
		return this.eventMessageFactory;
	}

	public void setEventMessagePublisher(final EventMessagePublisher eventMessagePublisher) {
		this.eventMessagePublisher = eventMessagePublisher;
	}

	protected EventMessagePublisher getEventMessagePublisher() {
		return eventMessagePublisher;
	}

}
