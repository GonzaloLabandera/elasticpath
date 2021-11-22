/*
 *
 *  * Copyright (c) Elastic Path Software Inc., 2021
 *
 */
package com.elasticpath.service.customer;

import java.util.List;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.service.EpPersistenceService;

/**
 * Provide address-related business service.
 *
 * @ws.attribute service="ContextIdNames.ADDRESS_SERVICE"
 */
public interface AddressService extends EpPersistenceService {
	/*
		All List-returning methods may be a source of performance issues, especially in case of long lists.
		The pagination is strongly recommended.
	 */

	/**
	 * Find address by customer uid.
	 * @param customerUid the customer uid.
	 * @return the list of customer addresses or empty list
	 */
	List<CustomerAddress> findByCustomer(long customerUid);

	/**
	 * Find customer address by country and sub-country.
	 *
	 * @param customerUid the customer uid
	 * @param country the country
	 * @param subCountry the sub-country
	 * @return the list of customer addresses or empty list
	 */
	List<CustomerAddress> findByCustomerCountryAndSubCountry(long customerUid, String country, String subCountry);

	/**
	 * Find address by provided address instance.
	 *
	 * @param customerUid the customer uid.
	 * @param addressToMatch the address to match in db.
	 * @return customer address or null
	 */
	CustomerAddress findByAddress(long customerUid, CustomerAddress addressToMatch);

	/**
	 * Find address by customer uid and address guid.
	 *
	 * @param customerUid the customer uid
	 * @param addressGuid the address guid
	 * @return customer address or null
	 */
	CustomerAddress findByCustomerAndAddressGuid(long customerUid, String addressGuid);

	/**
	 * Find address by customer uid and address uid.
	 * @param customerUid the customer uid
	 * @param addressUid the address uid
	 * @return customer address or null
	 */
	CustomerAddress findByCustomerAndAddressUid(long customerUid, long addressUid);

	/**
	 * Remove all customer addresses.
	 * @param customer the customer
	 * @return updated customer
	 */
	Customer removeAllByCustomer(Customer customer);

	/**
	 * Remove selected address.
	 *
	 * @param customer the customer
	 * @param address the address to delete
	 *
	 * @return updated Customer without selected address
	 */
	Customer remove(Customer customer, CustomerAddress address);

	/**
	 * Save addresses.
	 * @param addresses the addresses to save
	 */
	void save(CustomerAddress... addresses);
}
