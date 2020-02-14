/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;

/**
 * The facade for operations with customer payment instruments.
 */
public interface CustomerPaymentInstrumentRepository {

	/**
	 * Save or update a {@link CustomerPaymentInstrument}.
	 *
	 * @param customerPaymentInstrument {@link CustomerPaymentInstrument} entity
	 * @return persisted {@link CustomerPaymentInstrument}
	 */
	Single<CustomerPaymentInstrument> saveOrUpdate(CustomerPaymentInstrument customerPaymentInstrument);

	/**
	 * Retrieve the {@link CustomerPaymentInstrument} with the given guid.
	 *
	 * @param guid the guid of the {@link CustomerPaymentInstrument}
	 * @return the {@link CustomerPaymentInstrument} with the given guid
	 */
	Single<CustomerPaymentInstrument> findByGuid(String guid);

	/**
	 * Delete a persisted {@link CustomerPaymentInstrument}.
	 *
	 * @param customerPaymentInstrument persisted {@link CustomerPaymentInstrument}
	 * @return Completable
	 */
	Completable remove(CustomerPaymentInstrument customerPaymentInstrument);

	/**
	 * Find all {@link CustomerPaymentInstrument} entities by {@link Customer} entity.
	 *
	 * @param customer {@link Customer} entity
	 * @return {@link CustomerPaymentInstrument} collection
	 */
	Observable<CustomerPaymentInstrument> findByCustomer(Customer customer);

}
