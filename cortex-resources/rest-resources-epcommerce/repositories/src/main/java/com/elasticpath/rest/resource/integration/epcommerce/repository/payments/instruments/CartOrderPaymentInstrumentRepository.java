/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.orderpaymentapi.CartOrderPaymentInstrument;

/**
 * The facade for operations with cart order payment instruments.
 */
public interface CartOrderPaymentInstrumentRepository {

	/**
	 * Save or update an {@link CartOrderPaymentInstrument}.
	 *
	 * @param cartOrderPaymentInstrument {@link CartOrderPaymentInstrument} to save or update
	 * @return persisted {@link CartOrderPaymentInstrument}
	 */
	Single<CartOrderPaymentInstrument> saveOrUpdate(CartOrderPaymentInstrument cartOrderPaymentInstrument);

	/**
	 * Delete a persisted {@link CartOrderPaymentInstrument}.
	 *
	 * @param cartOrderPaymentInstrument persisted {@link CartOrderPaymentInstrument}
	 * @return Completable indicatin the object was removed
	 */
	Completable remove(CartOrderPaymentInstrument cartOrderPaymentInstrument);

	/**
	 * Find all {@link CartOrderPaymentInstrument} entities by the {@link CartOrder}.
	 *
	 * @param cartOrder the {@link CartOrder} entity
	 * @return {@link CartOrderPaymentInstrument} entity
	 */
	Observable<CartOrderPaymentInstrument> findByCartOrder(CartOrder cartOrder);

	/**
	 * Retrieve {@link CartOrderPaymentInstrument} with the given guid.
	 *
	 * @param guid the guid of the {@link CartOrderPaymentInstrument}
	 * @return the {@link CartOrderPaymentInstrument} with the given guid.
	 */
	Single<CartOrderPaymentInstrument> findByGuid(String guid);

}
