/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.rest.command.ExecutionResult;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.dto.CustomerDTO;

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
	 * Find single session user based on shared ID, and assigns the passed store code.
	 * If store code is null, searches for account.
	 *
	 * @param storeCode store code.
	 * @param sharedId  shared Id.
	 * @return Customer.
	 */
	ExecutionResult<Customer> findCustomerBySharedId(String storeCode, String sharedId);

	/**
	 * Find user by shared id from customerDTO or if it does not exist, will create new one by values from customerDTO
	 * and associate with account.
	 *
	 * @param customerDTO     {@link CustomerDTO}
	 * @param scope           scope
	 * @param sharedId        user shared ID
	 * @param accountSharedId account shared ID
	 * @return {@link ExecutionResult<Customer>}
	 */
	ExecutionResult<Customer> findOrCreateUser(CustomerDTO customerDTO, String scope, String sharedId, String accountSharedId);

	/**
	 * Finds customers by profile attribute key value pair.
	 *
	 * @param profileAttributeKey   profile attribute key
	 * @param profileAttributeValue profile attribute value
	 * @return list of customers matching given criteria
	 */
	ExecutionResult<List<Customer>> findCustomersByProfileAttributeKeyAndValue(String profileAttributeKey, String profileAttributeValue);

	/**
	 * Checks whether a customer exists with the given guid.
	 *
	 * @param guid the guid
	 * @return success if customer exists
	 */
	ExecutionResult<Boolean> isCustomerGuidExists(String guid);

	/**
	 * Returns the number of customers with given profile attribute key value pair.
	 *
	 * @param profileAttributeKey   profile attribute key
	 * @param profileAttributeValue profile attribute value
	 * @return count of customers matching given criteria
	 */
	ExecutionResult<Long> getCustomerCountByProfileAttributeKeyAndValue(String profileAttributeKey, String profileAttributeValue);

	/**
	 * Checks whether a customer exists with given shared ID and store code.
	 *
	 * @param customerType the customer type
	 * @param sharedId     the customer shared ID
	 * @return success if customer exists.
	 */
	ExecutionResult<Boolean> isCustomerExistsBySharedIdAndStoreCode(CustomerType customerType, String sharedId);

	/**
	 * Finds customer's guid by shared id and store code.
	 *
	 * @param customerType          the customer type
	 * @param sharedId              the customer shared ID
	 * @param customerIdentifierKey customerIdentifierKey added to fix CacheResult annotation conflict with
	 *                              findCustomerGuidByProfileAttributeKeyAndValue
	 * @return Customer's guid.
	 */
	ExecutionResult<String> findCustomerGuidBySharedId(CustomerType customerType, String sharedId, String customerIdentifierKey);

	/**
	 * Finds customer's guid by profile attribute key value pair.
	 *
	 * @param profileAttributeKey   profile attribute key
	 * @param profileAttributeValue profile attribute value
	 * @return customer's guid
	 */
	ExecutionResult<String> findCustomerGuidByProfileAttributeKeyAndValue(String profileAttributeKey, String profileAttributeValue);

	/**
	 * Finds customer by username and store code.
	 *
	 * @param username  the username
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
	 * Gets the customer. Returns account guid if account shared id presents in subject, user guid in other case.
	 *
	 * @param userGuid user guid
	 * @param subject  request subject
	 * @return customer guid
	 */
	String getCustomerGuid(String userGuid, Subject subject);

	/**
	 * Gets account guid if account shared id presents in subject, null other case.
	 *
	 * @param subject request subject
	 * @return account guid
	 */
	String getAccountGuid(Subject subject);

	/**
	 * Updates the customer.
	 *
	 * @param customer the customer.
	 * @return Completable with the deferred execution of the update operation.
	 */
	Completable updateCustomer(Customer customer);

	/**
	 * Merge single session to registered customer.
	 *
	 * @param singleSessionShopper the single session shopper
	 * @param registeredCustomer the registered customer
	 * @param validatedStoreCode the validated store code
	 * @return an execution result based on whether the merge was successful
	 */
	ExecutionResult<Object> mergeCustomer(Shopper singleSessionShopper, Customer registeredCustomer,
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

	/**
	 * Find customer by guid and store code.
	 *
	 * @param customerGuid the customer guid
	 * @param storeCode    the store code
	 * @return a customer
	 */
	ExecutionResult<Customer> findCustomerByGuidAndStoreCode(String customerGuid, String storeCode);


	/**
	 * Find all child accounts by parent account guid.
	 *
	 * @param accountGuid the parent account guid
	 * @return a list of child accounts guids
	 */
	List<String> findDescendants(String accountGuid);

	/**
	 * Find paginated direct child accounts by parent account guid.
	 *
	 * @param accountGuid    the parent account guid
	 * @param pageStartIndex the page start index
	 * @param pageSize       the number of results per page
	 * @return a paginated list of child accounts guids
	 */
	List<String> findPaginatedChildren(String accountGuid, int pageStartIndex, int pageSize);
}
