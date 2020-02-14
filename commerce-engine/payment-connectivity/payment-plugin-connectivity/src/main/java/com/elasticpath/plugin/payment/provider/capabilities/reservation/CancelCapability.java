/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.plugin.payment.provider.capabilities.reservation;

import com.elasticpath.plugin.payment.provider.capabilities.Capability;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequestFailedException;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityResponse;

/**
 * <p>Provides the ability to cancel a reservation.</p>
 * <p>While reservations do naturally cancel themselves once the expiration window has been exceeded, it is generally considered polite to cancel a
 * reservation as soon as the vendor knows that they will not charge (i.e. capture) the reserved funds (e.g. in the case of an in-progress order
 * being cancelled).</p>
 * <p>This operation is particularly important when modifying an order, as the reservation cancellation may free up funds on the payment instrument
 * that could then be immediately re-reserved, for example when splitting an in-progress order into multiple shipments. Failing to cancel the
 * reservation may result in hitting a credit limit for the follow-up reservation, which would then fail.</p>
 *
 * @see ReserveCapability
 */
public interface CancelCapability extends Capability {

	/**
	 * Cancel cancel reservation response.
	 *
	 * @param request the request
	 * @return the cancel reservation response
	 * @throws PaymentCapabilityRequestFailedException if the process failed
	 */
	PaymentCapabilityResponse cancel(CancelCapabilityRequest request) throws PaymentCapabilityRequestFailedException;
}
