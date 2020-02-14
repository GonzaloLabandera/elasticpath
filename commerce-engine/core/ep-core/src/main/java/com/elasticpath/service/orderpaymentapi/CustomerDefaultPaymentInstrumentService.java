/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.orderpaymentapi;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;

/**
 * Service for setting, removing and retrieving default {@link CustomerPaymentInstrument}.
 */
public interface CustomerDefaultPaymentInstrumentService {
	/**
	 * Saves {@link CustomerPaymentInstrument} as default.
	 *
	 * @param customerPaymentInstrument a {@link CustomerPaymentInstrument} entity
	 */
	void saveAsDefault(CustomerPaymentInstrument customerPaymentInstrument);

	/**
	 * Get default {@link CustomerPaymentInstrument} associated with {@link Customer} entity.
	 *
	 * @param customer the {@link Customer} entity
	 * @return default {@link CustomerPaymentInstrument}
	 */
	CustomerPaymentInstrument getDefaultForCustomer(Customer customer);

	/**
	 * Checks if this {@link Customer} has associated default {@link CustomerPaymentInstrument}.
	 *
	 * @param customer the {@link Customer} entity
	 * @return true if customer has a default payment instrument
	 */
	boolean hasDefaultPaymentInstrument(Customer customer);

	/**
	 * Checks if this {@link CustomerPaymentInstrument} is a default one for a customer.
	 *
	 * @param customerPaymentInstrument the {@link CustomerPaymentInstrument} candidate
	 * @return true if instrument is a default one
	 */
	boolean isDefault(CustomerPaymentInstrument customerPaymentInstrument);
}
