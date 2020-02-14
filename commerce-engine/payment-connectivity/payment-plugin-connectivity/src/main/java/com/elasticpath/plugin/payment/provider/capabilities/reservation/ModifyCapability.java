/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider.capabilities.reservation;

import com.elasticpath.plugin.payment.provider.capabilities.Capability;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequestFailedException;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityResponse;

/**
 * <p>Provides the ability to modify the amount of a reservation.</p>.
 * <p>Some payment providers support increasing or decreasing the amount of reserved funds for an existing reservation. This is useful in cases in
 * which an in-progress order is modified, or when prices are estimated at time of purchase and may vary when the order is processed.</p>
 * <p>Most payment providers that support this functionality allow increasing a reservation amount only by a certain percentage of its existing
 * value.
 * <p>Payment providers that do not support modifying an existing reservation will have to {@link ReserveCapability create a new reservation},
 * optionally {@link CancelCapability first cancelling the existing reservation}.</p>
 *
 * @see ReserveCapability
 * @see CancelCapability
 */
public interface ModifyCapability extends Capability {


	/**
	 * Modify modify reservation response.
	 *
	 * @param request the request
	 * @return the modify reservation response
	 * @throws PaymentCapabilityRequestFailedException if the process failed
	 */
	PaymentCapabilityResponse modify(ModifyCapabilityRequest request) throws PaymentCapabilityRequestFailedException;
}
