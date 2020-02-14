/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.service.processor;

import com.elasticpath.provider.payment.domain.transaction.PaymentAPIResponse;
import com.elasticpath.provider.payment.domain.transaction.modify.ModifyReservationRequest;

/**
 * Modify payment reservation processor.
 */
public interface ModifyReservationProcessor {
	/**
	 * Modify payment reservation processing.
	 *
	 * @param modifyRequest the modification of reservation request
	 * @return list of payment events
	 */
	PaymentAPIResponse modifyReservation(ModifyReservationRequest modifyRequest);
}
