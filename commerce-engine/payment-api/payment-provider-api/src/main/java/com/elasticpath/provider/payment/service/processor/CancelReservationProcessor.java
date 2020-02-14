/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.service.processor;

import com.elasticpath.provider.payment.domain.transaction.PaymentAPIResponse;
import com.elasticpath.provider.payment.domain.transaction.cancel.CancelAllReservationsRequest;
import com.elasticpath.provider.payment.domain.transaction.cancel.CancelReservationRequest;

/**
 * Cancel payment reservation processor.
 */
public interface CancelReservationProcessor {
	/**
	 * Cancel payment reservation processing.
	 *
	 * @param cancelRequest the cancel request
	 * @return list of payment events
	 */
	PaymentAPIResponse cancelReservation(CancelReservationRequest cancelRequest);

	/**
	 * Cancel payment for all reservation processing.
	 *
	 * @param cancelAllReservationsRequest the cancel all reservations request
	 * @return list of payment events
	 */
	PaymentAPIResponse cancelAllReservations(CancelAllReservationsRequest cancelAllReservationsRequest);
}
