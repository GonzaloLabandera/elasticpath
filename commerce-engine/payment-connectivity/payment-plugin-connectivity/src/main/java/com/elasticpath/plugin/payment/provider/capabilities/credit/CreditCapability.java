/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider.capabilities.credit;

import com.elasticpath.plugin.payment.provider.capabilities.Capability;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequestFailedException;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityResponse;

/**
 * <p>Provides the ability to perform a credit, also known as a <em>refund</em>.</p>
 * <p>Used to reverse the effects of a {@link CreditCapability credit}.</p>
 *
 * @see CreditCapability
 */
public interface CreditCapability extends Capability {


	/**
	 * Credit credit response.
	 *
	 * @param request the request
	 * @return the credit response
	 * @throws PaymentCapabilityRequestFailedException if the process failed
	 */
	PaymentCapabilityResponse credit(CreditCapabilityRequest request) throws PaymentCapabilityRequestFailedException;
}
