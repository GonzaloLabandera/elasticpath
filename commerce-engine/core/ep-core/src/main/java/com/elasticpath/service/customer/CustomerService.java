/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.service.customer;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.exception.SharedIdNonExistException;
import com.elasticpath.commons.exception.UserStatusInactiveException;
import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.persistence.api.FetchGroupLoadTuner;
import com.elasticpath.service.EpPersistenceService;

/**
 * Provide customer-related business service.
 *
 * @ws.attribute service="ContextIdNames.CUSTOMER_SERVICE"
 */
@SuppressWarnings("PMD.TooManyMethods")
public interface CustomerService extends EpPersistenceService {

	/**
	 * Adds the given customer.
	 *
	 * @param customer the customer to add
	 * @return the persisted instance of customer
	 *
	 * @ws.property
	 * @ws.param ori-type="com.elasticpath.domain.customer.Customer"
	 * 	ori-type-bean="ContextIdNames.CUSTOMER"
	 *  ws-type="com.elasticpath.connect.domain.customer.CustomerWsImpl"
	 * @ws.return ori-type="com.elasticpath.domain.customer.Customer"
	 * 	ws-type="com.elasticpath.connect.domain.customer.CustomerWsImpl"
	 */
	Customer add(Customer customer);

	/**
	 * Updates the given customer.
	 *
	 * @param customer the customer to update
	 * @return the new persisted instance of customer object
	 */
	Customer update(Customer customer);

	/**
	 * Updates the given customer.
	 *
	 * @param customer the customer to update
	 * @param shouldSetPassword true if the update should also set the password
	 * @return the new customer object from the persistence layer
	 */
	Customer update(Customer customer, boolean shouldSetPassword);


	/**
	 * List all customers stored in the database.
	 *
	 * @return a list of customers
	 * @throws EpServiceException - in case of any errors
	 */
	List<Customer> list() throws EpServiceException;

	/**
	 * Load the customer with the given UID. Throw an unrecoverable exception if there is no matching database row.
	 *
	 * @param customerUid the customer UID
	 * @return the customer if UID exists, otherwise null
	 * @throws EpServiceException - in case of any errors
	 */
	Customer load(long customerUid) throws EpServiceException;

	/**
	 * Get the customer with the given UID. Return null if no matching record exists.
	 *
	 * @param customerUid the customer UID
	 * @return the customer if UID exists, otherwise null
	 * @throws EpServiceException - in case of any errors
	 */
	Customer get(long customerUid) throws EpServiceException;
	
	/**
	 * Get the customer with the given UID. Return <code>null</code>l if no matching record
	 * exists.
	 * 
	 * @param customerUid the customer UID
	 * @param loadTuner the load tuner to tune the results, or <code>null</code> for the default
	 *            tuner
	 * @return the customer if UID exists, otherwise <code>null</code>
	 * @throws EpServiceException - in case of any errors
	 */
	Customer get(long customerUid, FetchGroupLoadTuner loadTuner) throws EpServiceException;

	/**
	 * Retrieve the customer with the given guid.
	 *
 	 * @param guid the guid of the customer
	 * @return the customer with the given guid
	 * @throws EpServiceException in case of any error
	 */
	Customer findByGuid(String guid) throws EpServiceException;

	/**
	 * Retrieve the customer with the given guid, with an optional load tuner.
	 * 
	 * @param guid the guid of the customer
	 * @param loadTuner the load tuner to tune the results, or <code>null</code> for the default tuner
	 * @return the customer with the given guid
	 * @throws EpServiceException in case of any error
	 */
	Customer findByGuid(String guid, FetchGroupLoadTuner loadTuner) throws EpServiceException;

	/**
	 * Retrieve the customers with the given profileAttributeKey and profileAttributeValue combination.
	 *
	 * @param profileAttributeKey profile attribute key
	 * @param profileAttributeValue profile attribute value
	 * @return customers with the given profile attribute key value pair
	 */
	List<Customer> findCustomersByProfileAttributeKeyAndValue(String profileAttributeKey, String profileAttributeValue);

	/**
	 * Retrieve customer by username and storeCode.
	 * @param username username of the Customer
	 * @param storeCode of the store for this customer
	 * @return the customer with the given username and storeCode
	 */
	Customer findCustomerByUserName(String username, String storeCode);

	/**
	 * Checks whether a customer exists with the given username and storeCode.
	 *
	 * @param customer the Customer
	 * @return true if customer exists
	 */
	boolean isCustomerByUserNameExists(Customer customer);

	/**
	 * Checks whether a customer exists with the given guid.
	 *
	 * @param guid the guid
	 * @return true if customer exists
	 */
	boolean isCustomerGuidExists(String guid);

	/**
	 * Retrieve the customer type by customer guid.
	 *
	 * @param guid the customer guid
	 * @return the customer type
	 */
	CustomerType getCustomerTypeByGuid(String guid);

	/**
	 * Checks whether a registered customer exists with the given sharedId and store code.
	 *
	 * @param customer the customer
	 * @return true if customer exists
	 */
	boolean isRegisteredCustomerExistsBySharedIdAndCustomerType(Customer customer);

	/**
	 * Checks whether a customer exists with the given sharedId and store code.
	 *
	 * @param sharedId the sharedId
	 * @param storeCode the store code
	 * @return true if customer exists
	 */
	boolean isCustomerExistsBySharedIdAndStoreCode(String sharedId, String storeCode);

	/**
	 * Checks whether customer exists with profile attribute key value pair.
	 *
	 * @param profileAttributeKey profile attribute key
	 * @param profileAttributeValue profile attribute value
	 *
	 * @return number of customers matching given criteria
	 */
	Long getCustomerCountByProfileAttributeKeyAndValue(String profileAttributeKey, String profileAttributeValue);

	/**
	 * Finds customer's guid by shared id and store code.
	 *
	 * @param sharedId customer shared ID
	 * @param storeCode the store Code
	 * @return Customer's guid.
	 */
	String findCustomerGuidBySharedId(String sharedId, String storeCode);

	/**
	 * Finds customer's guid based on attribute value for the passed attribute key.
	 *
	 * @param profileAttributeKey profile attribute key
	 * @param profileAttributeValue profile attribute value
	 * @return Customer's guid.
	 */
	String findCustomerGuidByProfileAttributeKeyAndValue(String profileAttributeKey, String profileAttributeValue);

	/**
	 * Resets the customer's password for the specified email.
	 *
	 * @param sharedId The shared Id of the customer whose password is to be reset
	 * @param storeCode of the store for this customer
	 * @throws SharedIdNonExistException if the shared Id isn't found
	 */
	void resetPassword(String sharedId, String storeCode) throws SharedIdNonExistException;

	/**
	 * Resets the given customer's password.
	 *
	 * @param customer The given customer.
	 * @return the updated instance of the customer
	 * @see Customer
	 */
	Customer auditableResetPassword(Customer customer);

	/**
	 * Changes the password for the specified customer, and sends the confirmation email.
	 * 
	 * @param customer the customer whose password is to be changed
	 * @param newPassword the new password (clear text)
	 * @return the updated instance of the customer
	 * @see Customer
	 */
	Customer changePasswordAndSendEmail(Customer customer, String newPassword);
	
	/**
	 * Changes the password for the specified customer.
	 * 
	 * @param customer the customer whose password is to be changed
	 * @param newPassword the new password (clear text)
	 * @return the updated instance of the customer
	 * @see Customer
	 */
	Customer setPassword(Customer customer, String newPassword);

	/**
	 * Retrieve the list of customers, whose specified property contain the given criteria value.
	 *
	 * @param propertyName customer property to search on.
	 * @param criteriaValue criteria value to be used for searching.
	 * @return list of customers matching the given criteria.
	 */
	List<Customer> findCustomerLike(String propertyName, String criteriaValue);

	/**
	 * Returns a list of <code>Customer</code> based on the given uids. The returned customers will be populated based on the given load tuner.
	 *
	 * @param customerUids a collection of customer uids
	 * @return a list of <code>Customer</code>s
	 */
	List<Customer> findByUids(Collection<Long> customerUids);

	/**
	 * Returns all customer uids as a list.
	 *
	 * @return all customer uids as a list
	 */
	List<Long> findAllUids();

	/**
	 * Removes uids which correspond to customers that are not searchable.
	 * @param uids the uids to filter
	 * @return the uids of searchable customers
	 */
	Collection<Long> filterSearchable(Collection<Long> uids);

	/**
	 * Retrieves list of searchable <code>Customer</code> uids where the last modified date is later than the specified date.
	 * A customer is searchable if they have defined their first name or last name.
	 *
	 * @param date date to compare with the last modified date
	 * @return list of <code>Customer</code> uid
	 */
	List<Long> findSearchableUidsByModifiedDate(Date date);

	/**
	 * Adds a customer to the default customer group (ensuring that they have the default role).
	 * @param customer the customer upon which to set the default group
	 * @throws EpServiceException in case of any errors.
	 */
	void setCustomerDefaultGroup(Customer customer);

	/**
	 * Find the account with the given shared ID.
	 *
	 * @param sharedId the account shared ID
	 * @return the account with the given shared ID
	 * @throws EpServiceException - in case of any errors
	 */
	Customer findBySharedId(String sharedId) throws EpServiceException;

	/**
	 * Find the customer with the given shared ID.
	 * If it cannot find the customer in the given store, also look within the store's associated stores.
	 *
	 * @param sharedId the customer shared ID
	 * @param storeCode the store to search in
	 * @return the customers with the given shared ID
	 * @throws EpServiceException - in case of any errors
	 */
	Customer findBySharedId(String sharedId, String storeCode) throws EpServiceException;

	/**
	 * Adds the given customer.
	 *
	 * @param customer the customer to add
	 * @param isAuthenticated true if the Customer is already authenticated via external source
	 * @return the persisted instance of customer
	 */
	Customer addByAuthenticate(Customer customer, boolean isAuthenticated);

	/**
	 * See CustomerAddressDao for details.
	 * 
	 * @param addressUid See above.
	 * @return See above.
	 * @see com.elasticpath.service.customer.dao.CustomerAddressDao
	 */
	CustomerAddress getCustomerAddress(long addressUid);

	/**
	 * Verifies the customer status.
	 *
	 * @param customer the customer need to be verified.
	 * @throws UserStatusInactiveException if the customer is inactive
	 */
	void verifyCustomer(Customer customer) throws UserStatusInactiveException;
	
	/**
	 * Retrieves customer status by customer uidpk.
	 *
	 * @param customerUid long
	 * @return customer status
	 */
	int findCustomerStatusByUid(long customerUid);

	/**
	 * Retrieves a collection of Customer accounts associated with the specified CustomerGroup.
	 *
	 * @param groupName the customer group
	 * @return collection of Customer accounts associated with the group
	 */
	List<Customer> findCustomersByCustomerGroup(String groupName);

	/**
	 * Adds or updates the given address.
	 * If the address passed in has a uidPk of 0 then it will be added to the customer's address list,
	 * otherwise the address will be retrieved from the customer's address list and the information copied over from the given address.
	 * If the address already exists then its preferred status is not changed (for example, preferred billing or preferred shipping status).
	 * 
	 * @param customer The Customer to whom the address belongs.
	 * @param address The address to add or update.
	 * @return The updated instance of the given Customer.
	 */
	Customer addOrUpdateAddress(Customer customer, CustomerAddress address);
	
	/**
	 * Adds or updates the customer's billing address. If the address passed in has a uidPk of 0 then it will be added to the customer's address
	 * list, otherwise the corresponding address will be retrieved from the customer's address list and the information copied over from the passed
	 * in address. Finally, this address will be set as the customer's preferred billing address, the changes saved, and the updated customer
	 * instance returned.
	 * 
	 * @param customer the customer to add or update the address on
	 * @param address the address being updated or added
	 * @return the updated instance of the customer
	 */
	Customer addOrUpdateCustomerBillingAddress(Customer customer, CustomerAddress address);

	/**
	 * Adds or updates the customer's shipping address. If the address passed in has a uidPk of 0 then it will be added to the customer's address
	 * list, otherwise the corresponding address will be retrieved from the customer's address list and the information copied over from the passed
	 * in address. Finally, this address will be set as the customer's preferred shipping address, the changes saved, and the updated customer
	 * instance returned.
	 * 
	 * @param customer the customer to add or update the address on
	 * @param address the address being updated or added
	 * @return the updated instance of the customer
	 */
	Customer addOrUpdateCustomerShippingAddress(Customer customer, CustomerAddress address);

	/**
	 * Updates the customer's profile using information in an address.
	 *
	 * @param customer the customer to update
	 * @param address the address
	 * @return the updated instance of the customer
	 */
	Customer updateCustomerFromAddress(Customer customer, Address address);

	/**
	 * Gets the customer last modified date given the customer GUID.
	 *
	 * @param customerGuid the customer GUID
	 * @return the customer last modified date
	 */
	Date getCustomerLastModifiedDate(String customerGuid);

	/**
	 * Remove all addresses from the customer.
	 *
	 * @param customer the customer
	 * @return customer without addresses.
	 */
	Customer removeAllAddresses(Customer customer);
}
