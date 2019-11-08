/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.order;

/**
 * Collection of StructuredErrorMessage message ids for the cart domain.
 * These message ids should be used for localization of error messages on client side.
 */
public final class OrderMessageIds {
	/**
	 * Order has already been submitted and can't be resubmitted.
	 */
	public static final String ORDER_ALREADY_SUBMITTED = "order.already.submitted";

	private OrderMessageIds() {
		// private constructor to ensure class can't be instantiated
	}
}
