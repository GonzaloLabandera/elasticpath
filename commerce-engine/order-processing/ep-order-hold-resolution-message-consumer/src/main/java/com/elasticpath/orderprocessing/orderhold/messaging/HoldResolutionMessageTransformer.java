/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.orderprocessing.orderhold.messaging;

import java.util.function.Function;

import com.elasticpath.messaging.EventMessage;

/**
 * Defining a transformation interface.
 */
@FunctionalInterface
public interface HoldResolutionMessageTransformer extends Function<EventMessage, HoldResolutionContext> {

	/**
	 * Transform from {@link EventMessage} to {@link HoldResolutionContextImpl}.
	 *
	 * @param eventMessage the event message contains order reference.
	 * @return the hold resolution context.
	 */
	HoldResolutionContext transform(EventMessage eventMessage);

	@Override
	default HoldResolutionContext apply(EventMessage eventMessage) {
		return transform(eventMessage);
	}
}
