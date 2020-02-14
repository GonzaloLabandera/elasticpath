/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.orderpaymentapi;

import java.util.Collection;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.orderpaymentapi.CartOrderPaymentInstrument;
import com.elasticpath.service.EpPersistenceService;

/**
 * Service for saving, deleting and retrieving {@link CartOrderPaymentInstrument}.
 */
public interface CartOrderPaymentInstrumentService extends EpPersistenceService {

	/**
	 * Save or update an {@link CartOrderPaymentInstrument}.
	 *
	 * @param cartOrderPaymentInstrument {@link CartOrderPaymentInstrument} to save or update
	 * @return persisted {@link CartOrderPaymentInstrument}
	 */
	CartOrderPaymentInstrument saveOrUpdate(CartOrderPaymentInstrument cartOrderPaymentInstrument);

	/**
	 * Delete a persisted {@link CartOrderPaymentInstrument}.
	 *
	 * @param cartOrderPaymentInstrument persisted {@link CartOrderPaymentInstrument}
	 */
	void remove(CartOrderPaymentInstrument cartOrderPaymentInstrument);

	/**
	 * Retrieve {@link CartOrderPaymentInstrument} with the given guid.
	 *
	 * @param guid the guid of the {@link CartOrderPaymentInstrument}
	 * @return the {@link CartOrderPaymentInstrument} with the given guid.
	 */
	CartOrderPaymentInstrument findByGuid(String guid);

	/**
	 * Find all {@link CartOrderPaymentInstrument} entities by the {@link CartOrder}.
	 *
	 * @param cartOrder the {@link CartOrder} entity
	 * @return {@link CartOrderPaymentInstrument} entity
	 */
	Collection<CartOrderPaymentInstrument> findByCartOrder(CartOrder cartOrder);

	/**
	 * Checks if {@link CartOrder} has any associated {@link CartOrderPaymentInstrument} entities.
	 *
	 * @param cartOrder the {@link CartOrder} entity
	 * @return true if payment instruments attached to the cart order
	 */
	boolean hasPaymentInstruments(CartOrder cartOrder);
}
