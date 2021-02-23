/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.addresses;

import java.util.Collection;
import java.util.Optional;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.rest.definition.addresses.AccountAddressesIdentifier;
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.resource.ResourceOperationContext;

/**
 * Repository for shared address functionality.
 */
public interface AddressRepository {

	/**
	 * Update a customer address.
	 *
	 * @param customerGuid  the customer guid
	 * @param addressGuid   the address guid
	 * @param addressEntity the address entity
	 * @return completable
	 */
	Completable update(String customerGuid, String addressGuid, AddressEntity addressEntity);

	/**
	 * Get the customer's address by guid.
	 *
	 * @param addressGuid addressGuid
	 * @param customer    customer
	 * @return customer address
	 */
	Single<CustomerAddress> getExistingAddressByGuid(String addressGuid, Customer customer);

	/**
	 * Get Address Entity by address guid and customer guid.
	 *
	 * @param addressGuid  the address guid
	 * @param customerGuid the customer guid
	 * @return the AddressEntity single
	 */
	Single<AddressEntity> getAddressEntity(String addressGuid, String customerGuid);

	/**
	 * Find all addresses by customer guid.
	 *
	 * @param customerGuid the customer guid
	 * @return Observable of CustomerAddress
	 */
	Observable<CustomerAddress> findAllAddresses(String customerGuid);

	/**
	 * Add a new address.
	 *
	 * @param customerAddress the customer address
	 * @param customer        the customer
	 * @param scope           the scope
	 * @return new customer address single
	 */
	Single<CustomerAddress> addAddress(CustomerAddress customerAddress, Customer customer, String scope);

	/**
	 * Add a new account address.
	 *
	 * @param customerAddress the customer address
	 * @param account         the account
	 * @param scope           the scope
	 * @return new customer address single
	 */
	Single<CustomerAddress> addAccountAddress(CustomerAddress customerAddress, Customer account, String scope);

	/**
	 * Validate an AddressEntity.
	 *
	 * @param addressEntity the AddressEntity
	 * @return Completable
	 */
	Completable validateAddressEntity(AddressEntity addressEntity);

	/**
	 * Get existing address by CustomerAddress and Customer.
	 *
	 * @param customerAddress the customer address
	 * @param customer        the customer
	 * @return Optional of customer address if existing address exists
	 */
	Optional<CustomerAddress> getExistingAddressMatchingAddress(CustomerAddress customerAddress, Customer customer);

	/**
	 * Converts AddressEntity to CustomerAddress.
	 *
	 * @param addressEntity addressEntity
	 * @return customer address
	 */
	CustomerAddress convertAddressEntityToCustomerAddress(AddressEntity addressEntity);

	/**
	 * Add the customer's preferred billing and shipping address if the customer doesn't already have existing ones.
	 *
	 * @param scope    scope
	 * @param customer newAddressCustomer
	 * @param address  address
	 * @return the customer
	 */
	Single<Customer> setCustomerPreferredAddress(String scope, Customer customer, CustomerAddress address);

	/**
	 * Add the account's preferred billing and shipping address if the customer doesn't already have existing ones.
	 *
	 * @param scope   scope
	 * @param account newAddressAccount
	 * @param address address
	 * @return the customer
	 */
	Single<Customer> setAccountPreferredAddress(String scope, Customer account, CustomerAddress address);

	/**
	 * Update all cart order addresses of the customer if needed.
	 *
	 * @param scope                          scope
	 * @param customer                       newAddressCustomer
	 * @param address                        address
	 * @param updatePreferredBillingAddress  updatePreferredBillingAddress
	 * @param updatePreferredShippingAddress updatePreferredShippingAddress
	 * @return Completable
	 */
	Completable updateCartOrdersAddresses(String scope, Customer customer, CustomerAddress address,
										  boolean updatePreferredBillingAddress, boolean updatePreferredShippingAddress);

	/**
	 * Update all cart order addresses of the account if needed.
	 *
	 * @param scope                          scope
	 * @param account                        newAddressAccount
	 * @param address                        address
	 * @param updatePreferredBillingAddress  updatePreferredBillingAddress
	 * @param updatePreferredShippingAddress updatePreferredShippingAddress
	 * @return Completable
	 */
	Completable updateAccountCartOrdersAddresses(String scope, Customer account, CustomerAddress address,
												 boolean updatePreferredBillingAddress, boolean updatePreferredShippingAddress);

	/**
	 * Converts Address to AddressEntity.
	 *
	 * @param address address
	 * @return address entity
	 */
	AddressEntity convertCustomerAddressToAddressEntity(Address address);

	/**
	 * Delete an address by address guid and customer guid.
	 *
	 * @param addressGuid the address guid
	 * @param customerId  the customer id
	 * @return completable
	 */
	Completable deleteAddress(String addressGuid, String customerId);

	/**
	 * Get the account address identifier from the resource identifier.
	 *
	 * @param context the resource operation context
	 * @return AccountAddressesIdentifier
	 */
	AccountAddressesIdentifier getAccountAddressesIdentifier(ResourceOperationContext context);

	/**
	 * Get the account's addresses by account id.
	 *
	 * @param accountId the account id
	 * @return the account's customer address
	 */
	Collection<CustomerAddress> getAccountAddresses(String accountId);

}