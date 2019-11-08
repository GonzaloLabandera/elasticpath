/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.service.rules.impl;

/**
 * Coupon usage validation enum.
 */
public enum CouponUsageValidationResultEnum {

	/**
	 * Success.
	 */
	SUCCESS(true),
	/**
	 * Coupon use count exceeded error.
	 */
	ERROR_USE_COUNT_EXCEEDED(false),
	/**
	 * Coupon expired error.
	 */
	ERROR_EXPIRED(false),
	/**
	 * Coupon suspended error.
	 */
	ERROR_SUSPENDED(false),
	/**
	 * Email required error.
	 */
	ERROR_EMAIL_REQUIRED(false),
	/**
	 * Unspecified error.
	 */
	ERROR_UNSPECIFIED(false);

	private boolean success;

	/**
	 * Default private constructor.
	 * @param success whether or not this value is a success
	 */
	CouponUsageValidationResultEnum(final boolean success) {
		this.success = success;
	}

	/**
	 * Determine if the value represents a success.
	 * @return is success flag.
	 */
	public boolean isSuccess() {
		return success;
	}
}
