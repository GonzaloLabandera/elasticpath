/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.plugin.payment.provider.capabilities;

import java.util.Map;

/**
 * Charge capability followup request (reverse, refund, etc.).
 */
public interface ChargeCapabilityFollowupRequest extends PaymentCapabilityRequest {
	/**
	 * Get charge data.
	 *
	 * @return charge data
	 */
	Map<String, String> getChargeData();

	/**
	 * Set charge data.
	 *
	 * @param chargeData the charge data
	 */
	void setChargeData(Map<String, String> chargeData);
}
