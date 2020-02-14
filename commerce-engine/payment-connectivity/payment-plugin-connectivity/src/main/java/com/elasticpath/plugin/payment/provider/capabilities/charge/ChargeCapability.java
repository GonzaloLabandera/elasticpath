/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.plugin.payment.provider.capabilities.charge;

import com.elasticpath.plugin.payment.provider.capabilities.Capability;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequestFailedException;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityResponse;


/**
 * The interface for charge capability.
 */
public interface ChargeCapability extends Capability {

	/**
	 * Charge response.
	 *
	 * @param request the request
	 * @return the charge response
	 * @throws PaymentCapabilityRequestFailedException if the process failed
	 */
	PaymentCapabilityResponse charge(ChargeCapabilityRequest request) throws PaymentCapabilityRequestFailedException;
}
