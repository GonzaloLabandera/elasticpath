/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.orderpaymentapi;

import java.util.Collection;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.service.EpPersistenceService;

/**
 * Service for saving, deleting and retrieving {@link CustomerPaymentInstrument}.
 */
public interface CustomerPaymentInstrumentService extends EpPersistenceService {
	/**
	 * Save or update a {@link CustomerPaymentInstrument}.
	 *
	 * @param customerPaymentInstrument {@link CustomerPaymentInstrument} entity
	 * @return persisted {@link CustomerPaymentInstrument}
	 */
	CustomerPaymentInstrument saveOrUpdate(CustomerPaymentInstrument customerPaymentInstrument);

	/**
	 * Delete a persisted {@link CustomerPaymentInstrument}.
	 *
	 * @param customerPaymentInstrument persisted {@link CustomerPaymentInstrument}
	 */
	void remove(CustomerPaymentInstrument customerPaymentInstrument);

	/**
	 * Retrieve the {@link CustomerPaymentInstrument} with the given guid.
	 *
	 * @param guid the guid of the {@link CustomerPaymentInstrument}
	 * @return the {@link CustomerPaymentInstrument} with the given guid
	 */
	CustomerPaymentInstrument findByGuid(String guid);

	/**
	 * Find all {@link CustomerPaymentInstrument} entities by {@link Customer} entity.
	 *
	 * @param customer {@link Customer} entity
	 * @return {@link CustomerPaymentInstrument} collection
	 */
	Collection<CustomerPaymentInstrument> findByCustomer(Customer customer);
}
