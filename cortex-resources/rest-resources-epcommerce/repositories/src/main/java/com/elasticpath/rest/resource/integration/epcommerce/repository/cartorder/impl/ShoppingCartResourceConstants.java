/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl;

import com.google.common.annotations.VisibleForTesting;

/**
 * Constants for Shopping cart resource.
 */
public final class ShoppingCartResourceConstants {

	/**
	 * Error message used when default cart can't be found.
	 */
	@VisibleForTesting
	public static final String DEFAULT_CART_NOT_FOUND = "Default cart cannot be found.";
	/**
	 * Error message used when line item was not found.
	 */
	public static final String LINEITEM_WAS_NOT_FOUND = "No line item was found with GUID = %s.";

	/**
	 * Error message when creating a cart isn't supported.
	 */
	public static final String CREATE_CART_NOT_SUPPORTED = "Create cart not supported";

	/**
	 * Private constructor.
	 */
	private ShoppingCartResourceConstants() {
		//no-op private constructor.
	}
}
