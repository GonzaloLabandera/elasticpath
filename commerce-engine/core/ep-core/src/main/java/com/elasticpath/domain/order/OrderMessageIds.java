/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.domain.order;

/**
 * Collection of StructuredErrorMessage message ids for the order domain.
 * These message ids should be used for localization of error messages on client side.
 */
public final class OrderMessageIds {
	/**
	 * Purchase failed - customer account is not in an active state.
	 */
	public static final String USER_ACCOUNT_NOT_ACTIVE = "purchase.user.account.not.active";
	/**
	 * Purchase failed - shipping address is not defined.
	 */
	public static final String SHIPPING_ADDRESS_MISSING = "purchase.missing.shipping.address";
	/**
	 * Purchase failed - shipping service level is invalid.
	 */
	public static final String SHIPPING_SERVICE_LEVEL_INVALID = "purchase.shipping.service.level.invalid";
	/**
	 * Purchase failed - shopping cart is empty.
	 */
	public static final String CART_IS_EMPTY = "purchase.cart.is.empty";
	/**
	 * Purchase failed - item is not purchasable.
	 */
	public static final String ITEM_NOT_AVAILABLE = "purchase.item.not.available";
	/**
	 * Purchase failed - insufficient inventory for item.
	 */
	public static final String INSUFFICIENT_INVENTORY = "purchase.item.insufficient.inventory";
	/**
	 * Purchase failed - item quantity less than required minimum quantity.
	 */
	public static final String MINIMUM_QUANTITY_REQUIRED = "purchase.item.minimum.quantity.required";

	private OrderMessageIds() {
		// private constructor to ensure class can't be instantiated
	}
}
