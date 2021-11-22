/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.orderpaymentapi;

import java.util.Collection;

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
	 * Find all {@link CartOrderPaymentInstrument} entities by the cart order.
	 *
	 * @param cartOrderGuid the cart order guid
	 * @return collection of {@link CartOrderPaymentInstrument} entities
	 */
	Collection<CartOrderPaymentInstrument> findByCartOrderGuid(String cartOrderGuid);

	/**
	 * Checks if cart order has any associated {@link CartOrderPaymentInstrument} entities.
	 *
	 * @param cartOrderGuid the cart order guid
	 * @return true if payment instruments attached to the cart order
	 */
	boolean hasPaymentInstruments(String cartOrderGuid);
}
