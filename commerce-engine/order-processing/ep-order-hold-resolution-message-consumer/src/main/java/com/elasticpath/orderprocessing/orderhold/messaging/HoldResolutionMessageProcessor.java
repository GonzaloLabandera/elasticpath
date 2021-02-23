/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.orderprocessing.orderhold.messaging;

import java.util.function.Consumer;

/**
 * Defining interface to consume {@link HoldResolutionContext}.
 */
public interface HoldResolutionMessageProcessor extends Consumer<HoldResolutionContext> {

	/**
	 * Determine an order to be accepted, rejected or ignored.
	 * Regarding info from {@link HoldResolutionContextImpl}.
	 *
	 * @param context the context of hold resolution.
	 */
	void process(HoldResolutionContext context);

	@Override
	default void accept(HoldResolutionContext context) {
		process(context);
	}
}
