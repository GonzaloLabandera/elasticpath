/*
 *
 *  * Copyright (c) Elastic Path Software Inc., 2021
 *  
 */
package com.elasticpath.service;

import java.util.List;

import com.google.common.collect.Multimap;

import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.service.customer.AddressService;

/**
 * FIT-specific address service.
 */
public interface TestAddressService extends AddressService {

	/**
	 * Return a map with customer Uids and all customer addresses.
	 *
	 * @param customerUids the list of customer uids.
	 * @return the map with customer uids and addresses
	 */
	Multimap<Long, CustomerAddress> findByCustomers(List<Long> customerUids);

	/**
	 * Find address by customer uid and "street1" field.
	 * @param customerUid the customer uid
	 * @param street the street1 value
	 * @return customer address or null
	 */
	CustomerAddress findByCustomerAndStreet1(long customerUid, String street);
}
