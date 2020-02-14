/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;

/**
 * The facade for operations with default payment instrument identifiers.
 */
public interface CustomerDefaultPaymentInstrumentRepository {

	/**
	 * Saves {@link CustomerPaymentInstrument} as default.
	 *
	 * @param customerPaymentInstrument a {@link CustomerPaymentInstrument} entity
	 * @return Completable
	 */
	Completable saveAsDefault(CustomerPaymentInstrument customerPaymentInstrument);

	/**
	 * Get default {@link CustomerPaymentInstrument} associated with {@link Customer} entity.
	 *
	 * @param customer the {@link Customer} entity
	 * @return default {@link CustomerPaymentInstrument} if one exists
	 */
	Maybe<CustomerPaymentInstrument> getDefaultForCustomer(Customer customer);

	/**
	 * Checks if this {@link Customer} has associated default {@link CustomerPaymentInstrument}.
	 *
	 * @param customer the {@link Customer} entity
	 * @return true if customer has a default payment instrument
	 */
	Single<Boolean> hasDefaultPaymentInstrument(Customer customer);

	/**
	 * Checks if this {@link CustomerPaymentInstrument} is a default one for a customer.
	 *
	 * @param customerPaymentInstrument the {@link CustomerPaymentInstrument} candidate
	 * @return true if instrument is a default one
	 */
	Single<Boolean> isDefault(CustomerPaymentInstrument customerPaymentInstrument);
}
