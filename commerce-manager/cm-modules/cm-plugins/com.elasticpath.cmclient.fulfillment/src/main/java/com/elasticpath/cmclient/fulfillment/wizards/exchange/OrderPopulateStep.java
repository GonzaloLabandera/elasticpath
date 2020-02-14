/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.cmclient.fulfillment.wizards.exchange;

/**
 * OrderPopulateStep defines step of modifications that should be performed.
 */
enum OrderPopulateStep {
	/**
	 * Shipping method modified.
	 */
	SHIPPING_METHOD_MODIFIED,
	/**
	 * Shipping cost modified.
	 */
	SHIPPING_COST_MODIFIED,
	/**
	 * Shipment discount modified.
	 */
	SHIPMENT_DISCOUNT_MODIFIED,
	/**
	 * Items were added/removed from/to shopping cart.
	 */
	SHOPPING_CART_MODIFIED
}
