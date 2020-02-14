/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.orderpaymentapi;

import java.util.Collection;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.orderpaymentapi.CartOrderPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;

/**
 * Service to provide filtered payment instruments.
 */
public interface FilteredPaymentInstrumentService {

	/**
	 * Finds collection of CustomerPaymentInstrument for defined customer and store.
	 *
	 * @param customer  customer.
	 * @param storeCode store code.
	 * @return collection of CustomerPaymentInstrument for defined customer and store.
	 */
	Collection<CustomerPaymentInstrument> findCustomerPaymentInstrumentsForCustomerAndStore(Customer customer, String storeCode);

	/**
	 * Gets default payment instrument for defined customer and store.
	 *
	 * @param customer  customer.
	 * @param storeCode store code.
	 * @return default payment instrument for defined customer and store.
	 */
	CustomerPaymentInstrument findDefaultPaymentInstrumentForCustomerAndStore(Customer customer, String storeCode);

	/**
	 * Finds collection of CartOrderPaymentInstrument for defined cartOrder and store.
	 *
	 * @param cartOrder cart order
	 * @param storeCode store code.
	 * @return collection of CartOrderPaymentInstrument for defined cartOrder and store.
	 */
	Collection<CartOrderPaymentInstrument> findCartOrderPaymentInstrumentsForCartOrderAndStore(CartOrder cartOrder, String storeCode);
}
