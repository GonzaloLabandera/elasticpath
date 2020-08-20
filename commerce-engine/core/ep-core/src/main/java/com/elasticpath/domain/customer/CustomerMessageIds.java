/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.domain.customer;

/**
 * Collection of StructuredErrorMessage message ids for the customer profile domain.
 * These message ids should be used for localization of error messages on client side.
 */
public final class CustomerMessageIds {
	/**
	 * User id already exists error where shared id is already defined.
	 */
	public static final String SHAREDID_ALREADY_EXISTS = "profile.sharedId.already.exists";

	private CustomerMessageIds() {
		// private constructor to ensure class can't be instantiated
	}
}
