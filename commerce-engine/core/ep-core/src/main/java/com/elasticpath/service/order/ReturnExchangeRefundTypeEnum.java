/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.order;

/**
 * Enum that represents all available return/exchange types.
 */
public enum ReturnExchangeRefundTypeEnum {
	/**
	 * Physical return.
	 */
	PHYSICAL_RETURN_REQUIRED,
	/**
	 * Refund to original payment source.
	 */
	REFUND_TO_ORIGINAL,
	/**
	 * Manual refund.
	 */
	MANUAL_REFUND
}
