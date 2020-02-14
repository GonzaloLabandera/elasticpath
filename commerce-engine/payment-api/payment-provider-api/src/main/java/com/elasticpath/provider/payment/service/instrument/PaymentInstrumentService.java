/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.instrument;

import com.elasticpath.provider.payment.domain.PaymentInstrument;

/**
 * Interface to perform CRUD operations with {@link PaymentInstrument} entity.
 * <br/>
 * This entity is internal to Payment API module, so don't use it externally. Use corresponding workflow service instead.
 *
 * @see com.elasticpath.provider.payment.workflow.PaymentInstrumentWorkflow
 */
public interface PaymentInstrumentService {

	/**
	 * Load the {@link PaymentInstrument} with the given UID.
	 *
	 * @param uid the {@link PaymentInstrument} UID.
	 * @return the {@link PaymentInstrument} entity if UID exists, otherwise null.
	 */
	PaymentInstrument get(long uid);

	/**
	 * Retrieve the {@link PaymentInstrument} with the given guid.
	 *
	 * @param guid the guid of the {@link PaymentInstrument}
	 * @return the {@link PaymentInstrument} with the given guid
	 */
	PaymentInstrument findByGuid(String guid);

	/**
	 * Deleting a {@link PaymentInstrument}.
	 *
	 * @param paymentInstrument The {@link PaymentInstrument} to remove
	 */
	void remove(PaymentInstrument paymentInstrument);

	/**
	 * Saves or updates the given {@link PaymentInstrument}.
	 *
	 * @param paymentInstrument The {@link PaymentInstrument} to save or update
	 * @return The updated {@link PaymentInstrument}
	 */
	PaymentInstrument saveOrUpdate(PaymentInstrument paymentInstrument);

}
