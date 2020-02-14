/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.workflow;

import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;

/**
 * Payment instrument workflow facade for Order Payment API (ep-core) interaction.
 */
public interface PaymentInstrumentWorkflow {

	/**
	 * Retrieve the {@link PaymentInstrumentDTO} with the given guid.
	 *
	 * @param guid the guid of the payment instrument
	 * @return the payment instrument with the given guid
	 */
	PaymentInstrumentDTO findByGuid(String guid);

}
