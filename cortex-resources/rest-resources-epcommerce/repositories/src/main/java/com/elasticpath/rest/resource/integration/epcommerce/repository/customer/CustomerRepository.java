/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.rest.command.ExecutionResult;

/**
 * Loads objects from core.
 */
public interface CustomerRepository {

	/**
	 * Create a new Customer Domain Object. This method does not perform any persistence.
	 *
	 * @return Customer.
	 */
	Customer createNewCustomerEntity();

	/**
	 * Finds customer by username and store code.
	 * If search is by accountSharedId storeCode is passed as null.
	 *
	 * @param storeCode store code.
	 * @param sharedId  shared Id.
	 * @return Customer.
	 */
	ExecutionResult<Customer> findCustomerBySharedId(String storeCode, String sharedId);

	/**
	 * Finds customer by user guid and store code.
	 *
	 * @param guid user guid.
	 * @return Customer.
	 */
	ExecutionResult<Customer> findCustomerByGuid(String guid);

	/**
	 * Finds customers by profile attribute key value pair.
	 *
	 * @param profileAttributeKey profile attribute key
	 * @param profileAttributeValue profile attribute value
	 *
	 * @return list of customers matching given criteria
	 */
	ExecutionResult<List<Customer>> findCustomersByProfileAttributeKeyAndValue(String profileAttributeKey, String profileAttributeValue);

	/**
	 * Checks whether a customer exists with the given guid.
	 *
	 * @param guid the guid
	 * @return success if customer exists
	 */
	ExecutionResult<Void> isCustomerGuidExists(String guid);

	/**
	 * Returns the number of customers with given profile attribute key value pair.
	 *
	 * @param profileAttributeKey profile attribute key
	 * @param profileAttributeValue profile attribute value
	 *
	 * @return count of customers matching given criteria
	 */
	ExecutionResult<Long> getCustomerCountByProfileAttributeKeyAndValue(String profileAttributeKey, String profileAttributeValue);

	/**
	 * Checks whether a customer exists with given shared ID and store code.
	 * @param storeCode the store code
	 * @param sharedId  the customer shared ID
	 * @return success if customer exists.
	 */
	ExecutionResult<Void> isCustomerExistsBySharedIdAndStoreCode(String storeCode, String sharedId);

	/**
	 * Finds customer's guid by shared id and store code.
	 *
	 * @param storeCode the store Code
	 * @param sharedId the customer shared ID
	 * @param customerIdentifierKey customerIdentifierKey added to fix CacheResult annotation conflict with
	 *                              findCustomerGuidByProfileAttributeKeyAndValue
	 *
	 * @return Customer's guid.
	 */
	ExecutionResult<String> findCustomerGuidBySharedId(String storeCode, String sharedId, String customerIdentifierKey);

	/**
	 * Finds customer's guid by profile attribute key value pair.
	 *
	 * @param profileAttributeKey profile attribute key
	 * @param profileAttributeValue profile attribute value
	 *
	 * @return customer's guid
	 */
	ExecutionResult<String> findCustomerGuidByProfileAttributeKeyAndValue(String profileAttributeKey, String profileAttributeValue);
	/**
	 * Finds customer by username and store code.
	 *
	 * @param username the username
	 * @param storeCode the store code
	 * @return Customer.
	 */
	ExecutionResult<Customer> findCustomerByUsername(String username, String storeCode);

	/**
	 * Gets the customer.
	 *
	 * @param guid customer guid.
	 * @return the Single customer
	 */
	Single<Customer> getCustomer(String guid);

	/**
	 * Updates the customer.
	 *
	 * @param customer the customer.
	 * @return Completable with the deferred execution of the update operation.
	 */
	Completable updateCustomer(Customer customer);

	/**
	 * Merge anonymous to registered customer.
	 *
	 * @param customerSession    the customer session
	 * @param recipientCustomer  the recipient customer guid
	 * @param validatedStoreCode the validated store code
	 * @return an execution result based on whether the merge was successful
	 */
	ExecutionResult<Object> mergeCustomer(CustomerSession customerSession, Customer recipientCustomer,
										  String validatedStoreCode);

	/**
	 * Adds the customer address.
	 *
	 * @param customer the customer
	 * @param address  the new address
	 * @return the customer
	 */
	Single<Customer> addAddress(Customer customer, CustomerAddress address);

	/**
	 * Updates the customer address.
	 *
	 * @param customer the customer
	 * @param address  the new address
	 * @return the customer
	 */
	Completable updateAddress(Customer customer, CustomerAddress address);

	/**
	 * Update the customer.
	 *
	 * @param updatedCustomer the updated customer
	 * @return the updated customer
	 */
	Single<Customer> update(Customer updatedCustomer);

	/**
	 * Authenticate an Anonymous User.
	 *
	 * @param customer the customer
	 * @return a customer
	 */
	ExecutionResult<Customer> addUnauthenticatedUser(Customer customer);

	/**
	 * Customer is first time buyer.
	 *
	 * @param customer the customer
	 * @return customer is first time buyer
	 */
	boolean isFirstTimeBuyer(Customer customer);

	/**
	 * Attaches an address to a customer if the customer does not already have a link to an equivalent address.
	 *
	 * @param customer        the customer
	 * @param customerAddress the address to check and/or attach to the customer.
	 * @return customer address
	 */
	Single<CustomerAddress> createAddressForCustomer(Customer customer, CustomerAddress customerAddress);

}
