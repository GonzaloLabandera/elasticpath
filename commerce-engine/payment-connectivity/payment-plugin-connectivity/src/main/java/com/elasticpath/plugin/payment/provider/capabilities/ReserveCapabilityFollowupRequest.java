/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.plugin.payment.provider.capabilities;

import java.util.Map;

/**
 * Reserve capability followup request (cancel, modify, charge, etc.).
 */
public interface ReserveCapabilityFollowupRequest extends PaymentCapabilityRequest {
	/**
	 * Gets reservation data.
	 *
	 * @return reservation data
	 */
	Map<String, String> getReservationData();

	/**
	 * Sets reservation data.
	 *
	 * @param reservationData the reservation data to set
	 */
	void setReservationData(Map<String, String> reservationData);
}
