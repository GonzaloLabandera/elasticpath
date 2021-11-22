/*
 *
 *  * Copyright (c) Elastic Path Software Inc., 2021
 *  
 */
package com.elasticpath.service.impl;

import static com.elasticpath.persistence.api.PersistenceConstants.LIST_PARAMETER_NAME;

import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.service.TestAddressService;
import com.elasticpath.service.customer.impl.AddressServiceImpl;

/**
 * Implementation of FitCustomerService.
 */
public class TestAddressServiceImpl extends AddressServiceImpl implements TestAddressService {

	@Override
	public Multimap<Long, CustomerAddress> findByCustomers(List<Long> customerUids) {
		Multimap<Long, CustomerAddress> addresses = HashMultimap.create();

		List<CustomerAddress> result = getPersistenceEngine()
				.retrieveByNamedQueryWithList("ADDRESSES_BY_CUSTOMER_UIDS", LIST_PARAMETER_NAME, customerUids);

		result.forEach(address -> addresses.put(address.getCustomerUidPk(), address));

		return addresses;
	}

	@Override
	public CustomerAddress findByCustomerAndStreet1(final long customerUid, final String street) {
		return Iterables
				.getFirst(
						getPersistenceEngine().retrieveByNamedQuery("ADDRESS_BY_CUSTOMER_UID_AND_STREET1", customerUid, street),
						null);
	}
}
