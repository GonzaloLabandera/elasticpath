/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.customer.impl;

import static com.elasticpath.commons.constants.ContextIdNames.ACCOUNT_CRITERION;
import static com.elasticpath.commons.constants.ContextIdNames.CUSTOMER_CRITERION;
import static com.elasticpath.commons.constants.ContextIdNames.FETCH_GROUP_LOAD_TUNER;
import static com.elasticpath.persistence.api.PersistenceConstants.LIST_PARAMETER_NAME;
import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.openjpa.persistence.jdbc.FetchMode;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.base.exception.structured.EpValidationException;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.exception.SharedIdNonExistException;
import com.elasticpath.commons.exception.UserStatusInactiveException;
import com.elasticpath.core.messaging.customer.CustomerEventType;
import com.elasticpath.domain.EpDomainException;
import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.CustomerGroup;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.domain.store.Store;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.EventType;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.persistence.api.FetchGroupLoadTuner;
import com.elasticpath.persistence.api.FlushMode;
import com.elasticpath.persistence.api.LoadTuner;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.persistence.support.AccountCriterion;
import com.elasticpath.persistence.support.CustomerCriterion;
import com.elasticpath.persistence.support.FetchGroupConstants;
import com.elasticpath.persistence.support.impl.CriteriaQuery;
import com.elasticpath.service.customer.AccountTreeService;
import com.elasticpath.service.customer.AddressService;
import com.elasticpath.service.customer.CustomerGroupService;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.customer.dao.CustomerAddressDao;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.orderpaymentapi.CustomerPaymentInstrumentService;
import com.elasticpath.service.search.query.AccountSearchCriteria;
import com.elasticpath.service.search.query.CustomerSearchCriteria;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.settings.SettingsReader;
import com.elasticpath.validation.ConstraintViolationTransformer;
import com.elasticpath.validation.service.CustomerConstraintValidationService;

/**
 * The default implementation of <code>CustomerService</code>.
 */
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.TooManyMethods", "PMD.GodClass",
		"PMD.ExcessiveClassLength", "PMD.NPathComplexity", "PMD.ExcessiveImports"})
public class CustomerServiceImpl extends AbstractEpPersistenceServiceImpl implements CustomerService {

	private static final String USER_ACCOUNT_NOT_ACTIVE = "purchase.user.account.not.active";

	private CustomerGroupService customerGroupService;

	private TimeService timeService;

	private StoreService storeService;

	private AddressService addressService;

	private CustomerAddressDao customerAddressDao;

	private SettingsReader settingsReader;

	private EventMessageFactory eventMessageFactory;

	private EventMessagePublisher eventMessagePublisher;

	private static final String NEW_PASSWORD_KEY = "newPassword";

	private Validator validator;

	private CustomerConstraintValidationService customerConstraintValidationService;

	private CustomerPaymentInstrumentService customerPaymentInstrumentService;

	private OrderService orderService;

	// Turn off line too long PMD warning
	private ConstraintViolationTransformer constraintViolationTransformer; //NOPMD


	public void setValidator(final Validator validator) {
		this.validator = validator;
	}

	public void setCustomerConstraintValidationService(final CustomerConstraintValidationService customerConstraintValidationService) {
		this.customerConstraintValidationService = customerConstraintValidationService;
	}

	protected CustomerConstraintValidationService getCustomerConstraintValidationService() {
		return customerConstraintValidationService;
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
	 */
	@Override
	public Customer add(final Customer customer) {
		return addByAuthenticate(customer, false);
	}

	/**
	 * Adds the given customer, special treatment for already authenticated user.
	 *
	 * @param customer          the customer to add
	 * @param shouldSetPassword true if password needs to be set
	 * @return the persisted instance of customer
	 * @throws EpValidationException - if there is a validation error.
	 */
	@Override
	public Customer addByAuthenticate(final Customer customer, final boolean shouldSetPassword) {
		sanityCheck();

		validateStore(customer);

		final Set<ConstraintViolation<Customer>> customerViolations = validatePasswordAndUsername(customer, shouldSetPassword);
		checkConstraintViolations(customerViolations);

		defaultCustomerGroupCheck(customer);

		final Date now = timeService.getCurrentTime();
		// update customer object
		customer.setCreationDate(now);

		saveCustomerWithAddresses(customer);
		// persist the customer
		getPersistenceEngine().save(customer);

		final AccountTreeService accountTreeService = getAccountTreeService();

		// persist a record for the closure table
		if (customer.getParentGuid() != null) {
			accountTreeService.insertClosures(customer.getGuid(), customer.getParentGuid());
		}

		// send confirmation email and update index
		sendCustomerEventAndUpdateIndex(customer);

		return customer;
	}

	/**
	 * Send appropriate customer event message and add search index notification record.
	 * @param customer the customer
	 */
	protected void sendCustomerEventAndUpdateIndex(final Customer customer) {
		if (customer.getCustomerType() == CustomerType.REGISTERED_USER) {
			sendCustomerEvent(CustomerEventType.CUSTOMER_REGISTERED, customer.getGuid(), null);
		}
		if (customer.getCustomerType() == CustomerType.ACCOUNT) {
			sendCustomerEvent(CustomerEventType.ACCOUNT_CREATED, customer.getGuid(), null);
		}
	}

	private void validateStore(final Customer customer) {
		if (customer.getCustomerType() != CustomerType.ACCOUNT) {
			final String storeCode = customer.getStoreCode();
			if (StringUtils.isBlank(storeCode)) {
				throw new EpServiceException("Customer has no store attached.");
			}
			final String validStoreCode = storeService.findValidStoreCode(storeCode);
			customer.setStoreCode(validStoreCode);
		}
	}

	@Override
	public Customer update(final Customer customer) {
		return update(customer, false);
	}

	@Override
	public Customer update(final Customer customer, final boolean shouldSetPassword) {
		sanityCheck();

		final Set<ConstraintViolation<Customer>> customerViolations = validatePasswordAndUsername(customer, shouldSetPassword);

		customerViolations.addAll(customerConstraintValidationService.validate(customer));

		checkConstraintViolations(customerViolations);

		defaultCustomerGroupCheck(customer);

		return saveCustomerWithAddresses(customer);
	}

	/*
		Customer addresses are no longer maintained by OpenJPA and must be saved using AddressService.
		CM and import/export still requires Customer.addresses field as a transfer medium.
	 */
	private Customer saveCustomerWithAddresses(final Customer customer) {
		Set<CustomerAddress> addressesToSave = new HashSet<>(customer.getTransientAddresses());

		CustomerAddress preferredBillingAddress = resetPreferredAddressIfRequired(customer, addressesToSave, true);
		CustomerAddress preferredShippingAddress = resetPreferredAddressIfRequired(customer, addressesToSave, false);

		Customer persistedCustomer = getPersistenceEngine().saveOrUpdate(customer);
		//customer must be saved to db before saving addresses
		getPersistenceEngine().flush();

		if (!addressesToSave.isEmpty()) {
			CustomerAddress[] arrayOfAddresses = new CustomerAddress[addressesToSave.size()];
			addressesToSave.forEach(address -> address.setCustomerUidPk(persistedCustomer.getUidPk()));
			addressService.save(addressesToSave.toArray(arrayOfAddresses));
		}

		persistedCustomer.setPreferredShippingAddress(preferredShippingAddress);
		persistedCustomer.setPreferredBillingAddress(preferredBillingAddress);

		return getPersistenceEngine().saveOrUpdate(persistedCustomer);
	}

	private CustomerAddress resetPreferredAddressIfRequired(final Customer customer, final Set<CustomerAddress> addressesToSave,
															final boolean isBillingAddress) {
		CustomerAddress address;

		if (isBillingAddress) {
			address = customer.getPreferredBillingAddress();
			if (address != null && !address.isPersisted()) {
				customer.setPreferredBillingAddress(null);
				addressesToSave.add(address);
			}
		} else {
			address = customer.getPreferredShippingAddress();
			if (address != null && !address.isPersisted()) {
				customer.setPreferredShippingAddress(null);
				addressesToSave.add(address);
			}
		}

		return address;
	}

	private Set<ConstraintViolation<Customer>> validatePasswordAndUsername(final Customer customer, final boolean shouldSetPassword) {
		if (CustomerType.REGISTERED_USER == customer.getCustomerType()) {
			return shouldSetPassword
					? customerConstraintValidationService.validateUserRegistrationConstraints(customer)
					: new HashSet<>();
		}
		return new HashSet<>();
	}

	private void checkConstraintViolations(final Set<ConstraintViolation<Customer>> customerViolations) {
		if (CollectionUtils.isNotEmpty(customerViolations)) {
			final List<StructuredErrorMessage> structuredErrorMessageList = constraintViolationTransformer.transform(customerViolations);
			throw new EpValidationException("Customer validation failure.", structuredErrorMessageList);
		}
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
			customer = getPrototypeBean(ContextIdNames.CUSTOMER, Customer.class);
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
			customer = getPrototypeBean(ContextIdNames.CUSTOMER, Customer.class);
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
	public Customer findCustomerByUserName(final String username, final String storeCode) {
		sanityCheck();
		if (username == null || storeCode == null) {
			throw new EpServiceException("Cannot retrieve customer without username or store");
		}

		Store store = storeService.findStoreWithCode(storeCode);
		if (store == null) {
			throw new EpServiceException("Store with code " + storeCode + " not found.");
		}

		Set<Long> storeUids = getAssociatedStoreUids(store);
		final List<Customer> customers =
				getPersistenceEngine().retrieveByNamedQueryWithList("CUSTOMER_FIND_BY_USERNAME_IN_STORES", LIST_PARAMETER_NAME, storeUids, username);

		return getCustomerForStoreOrNewest(customers, store);
	}

	@Override
	public boolean isCustomerByUserNameExists(final Customer customer) {
		sanityCheck();
		if (customer.getUsername() == null || customer.getStoreCode() == null) {
			throw new EpServiceException("Cannot retrieve customer without username or store");
		}

		Store store = storeService.findStoreWithCode(customer.getStoreCode());
		if (store == null) {
			throw new EpServiceException("Store with code " + customer.getStoreCode() + " not found.");
		}

		Set<Long> storeUids = getAssociatedStoreUids(store);

		return getPersistenceEngine().
				retrieveByNamedQueryWithList("CUSTOMER_GUID_FIND_BY_USERNAME_IN_STORES",
						LIST_PARAMETER_NAME,
						storeUids,
						customer.getUsername(),
						customer.getGuid()).size() > 0;
	}

	@Override
	public boolean isCustomerGuidExists(final String guid) {
		sanityCheck();
		return !getPersistenceEngine().retrieveByNamedQuery("CUSTOMER_EXISTS_BY_GUID", guid).isEmpty();
	}

	@Override
	public CustomerType getCustomerTypeByGuid(final String guid) {
		List<CustomerType> customerTypes = getPersistenceEngine()
				.retrieveByNamedQuery("CUSTOMER_TYPE_SELECT_BY_GUID", FlushMode.COMMIT, true, new Object[]{guid});
		if (customerTypes.isEmpty()) {
			return null;
		}
		if (customerTypes.size() > 1) {
			throw new EpServiceException("Inconsistent data -- duplicate guid:" + guid);
		}
		return customerTypes.get(0);
	}

	@Override
	public boolean isRegisteredCustomerExistsBySharedIdAndCustomerType(final Customer customer) {
		return getPersistenceEngine().retrieveByNamedQuery("CUSTOMER_GUID_FIND_BY_SHAREDID_AND_CUSTOMER_TYPE",
				customer.getSharedId(),
				customer.getCustomerType(),
				customer.getGuid()).size() > 0;
	}

	@Override
	public boolean isCustomerExistsBySharedId(final String sharedId, final CustomerType customerType) {
		return (findBySharedId(sharedId, customerType) != null);
	}

	@Override
	public Long getCustomerCountByProfileAttributeKeyAndValue(final String profileAttributeKey, final String profileAttributeValue) {
		sanityCheck();
		List<Long> results = getPersistenceEngine().
				retrieveByNamedQuery("CUSTOMER_COUNT_BY_PROFILEVALUE", profileAttributeKey, profileAttributeValue);
		return results.get(0);
	}

	@Override
	public String findCustomerGuidBySharedId(final String sharedId, final CustomerType customerType) {
		Customer customer = findBySharedId(sharedId, customerType);
		return (customer == null ? null : customer.getGuid());
	}

	@Override
	public String findCustomerGuidByProfileAttributeKeyAndValue(final String profileAttributeKey, final String profileAttributeValue) {
		sanityCheck();
		List<String> results = getPersistenceEngine().
				retrieveByNamedQuery("CUSTOMER_GUID_BY_PROFILEVALUE", profileAttributeKey, profileAttributeValue);

		if (results.isEmpty()) {
			return null;
		}

		return results.get(0);
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

	@Override
	public Customer findBySharedId(final String sharedId, final CustomerType customerType) throws EpServiceException {
		sanityCheck();
		if (sharedId == null || customerType == null) {
			throw new EpServiceException("Cannot retrieve customer without sharedId and customerType");
		}

		final List<Customer> customers = getPersistenceEngine().retrieveByNamedQuery("CUSTOMER_FIND_BY_SHAREDID_AND_TYPE", sharedId, customerType);
		return customers.isEmpty() ? null : customers.get(0);
	}

	@Override
	public List<Customer> findCustomersByProfileAttributeKeyAndValue(final String profileAttributeKey, final String profileAttributeValue) {
		sanityCheck();
		if (profileAttributeKey == null || profileAttributeValue == null) {
			throw new EpServiceException("Cannot retrieve customer without profile value key or profile value text");
		}

		return getPersistenceEngine().retrieveByNamedQuery("CUSTOMER_FIND_BY_PROFILEVALUE", profileAttributeKey, profileAttributeValue);
	}

	/**
	 * Retrieves associated storeUids for a given store.
	 *
	 * @param store store
	 * @return associated store uids.
	 */
	private Set<Long> getAssociatedStoreUids(final Store store) {
		Set<Long> storeUids = new HashSet<>();
		storeUids.add(store.getUidPk());
		storeUids.addAll(store.getAssociatedStoreUids());
		return storeUids;
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
	public void resetPassword(final String sharedId, final String storeCode) throws SharedIdNonExistException {
		Customer customer = findBySharedId(sharedId, CustomerType.REGISTERED_USER);

		if (customer == null) {
			throw new SharedIdNonExistException("The given shared id doesn't exist: " + sharedId + " In store: " + storeCode);
		}

		// Needed in order to get Spring to create another transactional context
		getCustomerService().auditableResetPassword(customer);
	}

	@Override
	public Customer auditableResetPassword(final Customer customer) {
		final String newPassword = customer.resetPassword();

		final Customer updatedCustomer = update(customer);

		final Map<String, Object> data = new HashMap<>();
		data.put(NEW_PASSWORD_KEY, newPassword);
		sendCustomerEvent(CustomerEventType.PASSWORD_FORGOTTEN, updatedCustomer.getGuid(), data);

		return updatedCustomer;
	}

	@Override
	public Customer setPassword(final Customer customer, final String newPassword) {
		customer.setClearTextPassword(newPassword);

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

		return getPersistenceEngine().retrieveByNamedQueryWithList("CUSTOMER_FIND_BY_UIDS", LIST_PARAMETER_NAME, customerUids);
	}

	/**
	 * Retrieves a paginated list of searchable <code>Customer</code> uids where the last modified date is later than the specified date.
	 * A customer is searchable if they are an 'Account', a 'Registered User' or an 'Single Session User' with at least one Order.
	 *
	 * @param lastModifiedDate date to compare with the last modified date
	 * @param firstResult the first result of the customer list to retrieve
	 * @param maxResult the maximum number of customers to retrieve
	 * @return a paginated list of indexable <code>Customer</code> uids whose last modified date is later than the specified date
	 */
	@Override
	public List<Long> findIndexableUidsPaginated(final Date lastModifiedDate, final int firstResult, final int maxResult) {
		sanityCheck();
		return getPersistenceEngine().retrieveByNamedQuery("FIND_UIDS_OF_INDEXABLE_CUSTOMERS",
				new Object[] {lastModifiedDate}, firstResult, maxResult);
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
				String errorMessage = "Customer account " + customer.getSharedId() + " is not active.";
				throw new UserStatusInactiveException(
						errorMessage,
						asList(
								new StructuredErrorMessage(
										USER_ACCOUNT_NOT_ACTIVE,
										errorMessage,
										ImmutableMap.of("shared-id", String.valueOf(customer.getSharedId()))
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
		addOrUpdateCustomerAddress(customer, address);
		return update(customer);
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
		final CustomerAddress updatedAddress = addOrUpdateCustomerAddress(customer, address);
		customer.setPreferredBillingAddress(updatedAddress);
		return update(customer);
	}

	@Override
	public Customer addOrUpdateCustomerShippingAddress(final Customer customer, final CustomerAddress address) {
		final CustomerAddress updatedAddress = addOrUpdateCustomerAddress(customer, address);
		customer.setPreferredShippingAddress(updatedAddress);
		return update(customer);
	}

	private CustomerAddress addOrUpdateCustomerAddress(final Customer customer, final CustomerAddress address) {
		if (!address.isPersisted()) {
			address.setCustomerUidPk(customer.getUidPk());
			addressService.save(address);
			return address;
		}

		CustomerAddress customerAddress = addressService.findByCustomerAndAddressUid(customer.getUidPk(), address.getUidPk());
		if (customerAddress == null) {
			throw new EpDomainException("Address with uid " + address.getUidPk() + " was not found in the customer's address list");
		}
		customerAddress.copyFrom(address);
		addressService.save(customerAddress);

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
		return getSingletonBean(ContextIdNames.CUSTOMER_SERVICE, CustomerService.class);
	}

	/**
	 * @param customerAddressDao The CustomerAddressDao to set and use.
	 */
	public void setCustomerAddressDao(final CustomerAddressDao customerAddressDao) {
		this.customerAddressDao = customerAddressDao;
	}

	@Override
	public Date getCustomerLastModifiedDate(final String customerGuid) {
		final List<Date> dates = getPersistenceEngine().retrieveByNamedQuery("CUSTOMER_LAST_MODIFIED_DATE", customerGuid);
		if (dates.isEmpty()) {
			return null;
		}
		return dates.get(0);
	}

	@Override
	public Customer removeAllAddresses(final Customer customer) {
		return addressService.removeAllByCustomer(customer);
	}

	@Override
	public long countAssociatedOrders(final Collection<String> customerGuids) {
		return customerGuids.stream().map(guid -> orderService.findOrderByAccountGuid(guid, true)).mapToLong(List::size).sum();
	}

	@Override
	public void remove(final Customer customer) {
		sanityCheck();
		if (customer.getCustomerType().equals(CustomerType.ACCOUNT)) {
			final AccountTreeService accountTreeService = getAccountTreeService();

			List<String> descendantGuids = accountTreeService.findDescendantGuids(customer.getGuid());
			Collections.reverse(descendantGuids);
			descendantGuids.add(customer.getGuid());

			if (countAssociatedOrders(descendantGuids) > 0) {
				throw new EpServiceException("Account cannot be deleted - the account or its children has associated orders");
			}

			for (String accountGuid : descendantGuids) {
				final Customer account = findByGuid(accountGuid);

				customerPaymentInstrumentService.findByCustomer(account)
						.forEach(customerPaymentInstrument -> customerPaymentInstrumentService.remove(customerPaymentInstrument));

				getPersistenceEngine().delete(account);
			}
		} else {
			getPersistenceEngine().delete(customer);
		}
	}

	protected AccountTreeService getAccountTreeService() {
		return getSingletonBean(ContextIdNames.ACCOUNT_TREE_SERVICE, AccountTreeService.class);
	}

	@Override
	public long getCustomerCountBySearchCriteria(final CustomerSearchCriteria searchCriteria) {
		sanityCheck();
		final CustomerCriterion customerCriterion = getPrototypeBean(CUSTOMER_CRITERION, CustomerCriterion.class);
		Collection<String> storeCodes = new LinkedList<>();
		CriteriaQuery query = customerCriterion.getCustomerSearchCriteria(searchCriteria, storeCodes, CustomerCriterion.ResultType.COUNT);

		FetchGroupLoadTuner loadTuner = getPrototypeBean(FETCH_GROUP_LOAD_TUNER, FetchGroupLoadTuner.class);
		loadTuner.addFetchGroup(FetchGroupConstants.DEFAULT);
		getFetchPlanHelper().setFetchMode(FetchMode.JOIN);

		return getCustomerCountByQueryAndStoreCodes(query, storeCodes, loadTuner);
	}

	@Override
	public long getAccountCountBySearchCriteria(final AccountSearchCriteria searchCriteria) {
		sanityCheck();
		final AccountCriterion customerCriterion = getPrototypeBean(ACCOUNT_CRITERION, AccountCriterion.class);
		CriteriaQuery query = customerCriterion.getAccountSearchCriteria(searchCriteria, AccountCriterion.ResultType.COUNT);

		FetchGroupLoadTuner loadTuner = getPrototypeBean(FETCH_GROUP_LOAD_TUNER, FetchGroupLoadTuner.class);
		loadTuner.addFetchGroup(FetchGroupConstants.DEFAULT);
		getFetchPlanHelper().setFetchMode(FetchMode.JOIN);

		return getCustomerCountByQueryAndStoreCodes(query, new LinkedList<>(), loadTuner);
	}

	@Override
	public List<Customer> findCustomersBySearchCriteria(final CustomerSearchCriteria searchCriteria, final int start,
			final int pagination) {
		sanityCheck();

		FetchGroupLoadTuner loadTuner = getPrototypeBean(FETCH_GROUP_LOAD_TUNER, FetchGroupLoadTuner.class);
		loadTuner.addFetchGroup(FetchGroupConstants.DEFAULT);

		getFetchPlanHelper().setFetchMode(FetchMode.JOIN);

		final CustomerCriterion customerCriterion = getPrototypeBean(CUSTOMER_CRITERION, CustomerCriterion.class);
		Collection<String> storeCodes = new LinkedList<>();
		CriteriaQuery query = customerCriterion.getCustomerSearchCriteria(searchCriteria, storeCodes, CustomerCriterion.ResultType.ENTITY);

		return getCustomersByQuery(query, storeCodes, loadTuner, start, pagination);
	}

	@Override
	public List<Customer> findAccountsBySearchCriteria(final AccountSearchCriteria searchCriteria, final int start,
														final int pagination) {
		sanityCheck();

		FetchGroupLoadTuner loadTuner = getPrototypeBean(FETCH_GROUP_LOAD_TUNER, FetchGroupLoadTuner.class);
		loadTuner.addFetchGroup(FetchGroupConstants.DEFAULT);

		getFetchPlanHelper().setFetchMode(FetchMode.JOIN);

		final AccountCriterion accountCriterion = getPrototypeBean(ACCOUNT_CRITERION, AccountCriterion.class);
		CriteriaQuery query = accountCriterion.getAccountSearchCriteria(searchCriteria, AccountCriterion.ResultType.ENTITY);

		return getCustomersByQuery(query, new LinkedList<>(), loadTuner, start, pagination);
	}

	@Override
	public void updateCustomers(final List<Customer> customers) {
		customers.forEach(getPersistenceEngine()::saveOrUpdate);
	}

	@Override
	public List<Customer> findByGuids(final Collection<String> customerGuids) {
		if (CollectionUtils.isEmpty(customerGuids)) {
			return Collections.emptyList();
		}

		return getPersistenceEngine().retrieveByNamedQueryWithList("CUSTOMER_FIND_BY_GUIDS", LIST_PARAMETER_NAME, customerGuids,
				null, 0, customerGuids.size());
	}

	/**
	 * Get customer count by search query.
	 *
	 * @param query the customer search query.
	 * @param storeCodes the storecodes
	 * @param loadTuner the load tuner
	 * @return the list of customer matching the given query.
	 */
	protected long getCustomerCountByQueryAndStoreCodes(final CriteriaQuery query, final Collection<String> storeCodes, final LoadTuner loadTuner) {
		List<Long> customerCount;

		if (query.getParameters().isEmpty() && storeCodes.isEmpty()) {
			customerCount = getPersistenceEngineWithLoadTuner(loadTuner).retrieve(query.getQuery());
		} else if (storeCodes.isEmpty()) {
			customerCount = getPersistenceEngineWithLoadTuner(loadTuner)
					.retrieve(query.getQuery(), query.getParameters().toArray());
		} else {
			customerCount = getPersistenceEngineWithLoadTuner(loadTuner)
					.retrieveWithList(query.getQuery(), "storeList", storeCodes,
							query.getParameters().toArray(), 0, Integer.MAX_VALUE);
		}

		return customerCount.get(0);
	}

	/**
	 * Find customers by search query using the given load tuner.
	 *
	 * @param query      the customer search query.
	 * @param storeCodes the storecodes
	 * @param loadTuner  the load tuner
	 * @param start      the starting record to search
	 * @param maxResults the max results to be returned
	 * @return the list of customer matching the given query.
	 */
	protected List<Customer> getCustomersByQuery(final CriteriaQuery query, final Collection<String> storeCodes, final LoadTuner loadTuner,
												 final int start, final int maxResults) {
		List<Customer> customerList;
		if (query.getParameters().isEmpty() && storeCodes.isEmpty()) {
			customerList = getPersistenceEngineWithLoadTuner(loadTuner)
					.retrieve(query.getQuery(), start, maxResults);
		} else if (storeCodes.isEmpty()) {
			customerList = getPersistenceEngineWithLoadTuner(loadTuner)
					.retrieve(query.getQuery(), query.getParameters().toArray(), start, maxResults);
		} else {
			customerList = getPersistenceEngineWithLoadTuner(loadTuner)
					.retrieveWithList(query.getQuery(), "storeList", storeCodes, query.getParameters().toArray(), start, maxResults);
		}
		return customerList;
	}


	private PersistenceEngine getPersistenceEngineWithLoadTuner(final LoadTuner loadTuner) {
		return getPersistenceEngine().withLoadTuners(loadTuner);
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

	protected CustomerPaymentInstrumentService getCustomerPaymentInstrumentService() {
		return customerPaymentInstrumentService;
	}

	public void setCustomerPaymentInstrumentService(final CustomerPaymentInstrumentService customerPaymentInstrumentService) {
		this.customerPaymentInstrumentService = customerPaymentInstrumentService;
	}

	protected OrderService getOrderService() {
		return orderService;
	}

	public void setOrderService(final OrderService orderService) {
		this.orderService = orderService;
	}

	public void setAddressService(final AddressService addressService) {
		this.addressService = addressService;
	}
}
