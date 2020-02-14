/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.plugin.payment.provider.capabilities.reservation;

import com.elasticpath.plugin.payment.provider.capabilities.Capability;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequestFailedException;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityResponse;

/**
 * <p>Provides the ability to perform a reservation operation for a payment instrument.</p>
 * <p>A reservation is sometimes referred to as an <em>authorisation</em>,
 * <em>pre-authorisation</em>, or <em>hold</em>.</p>
 * <p>Generally a payment operation that includes a reservation will also include a <em>capture</em> or <em>settlement</em>. In particular, credit
 * card issuers typically insist on reserving funds at time of purchase, but deferring the capture until the order has been fulfilled (e.g. in an
 * order of physical items, when the goods have shipped).</p>
 * <p>A reservation will usually expire after a particular duration, generally a few days.
 * <p>Most payment gateways allow reservations to be {@link CancelCapability cancelled} prior to capture.</p>
 */
public interface ReserveCapability extends Capability {

	/**
	 * Reserve payment reservation response.
	 *
	 * @param request the payment reservation request
	 * @return the payment reservation response
	 * @throws PaymentCapabilityRequestFailedException if the process failed
	 */
	PaymentCapabilityResponse reserve(ReserveCapabilityRequest request) throws PaymentCapabilityRequestFailedException;
}
