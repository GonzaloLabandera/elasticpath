/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.plugin.payment.provider.capabilities.charge;

import com.elasticpath.plugin.payment.provider.capabilities.Capability;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequestFailedException;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityResponse;


/**
 * The interface for Reverse Commit capability.
 */
public interface ReverseChargeCapability extends Capability {

	/**
	 * Reverse charge response.
	 *
	 * @param request the request
	 * @return the charge response
	 * @throws PaymentCapabilityRequestFailedException if the process failed
	 */
	PaymentCapabilityResponse reverseCharge(ReverseChargeCapabilityRequest request) throws PaymentCapabilityRequestFailedException;
}
