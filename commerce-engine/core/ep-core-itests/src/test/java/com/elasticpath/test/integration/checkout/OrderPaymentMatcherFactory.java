/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.test.integration.checkout;

import static com.elasticpath.plugin.payment.provider.dto.TransactionType.CANCEL_RESERVE;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.CHARGE;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.CREDIT;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.RESERVE;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.REVERSE_CHARGE;

import com.elasticpath.domain.order.OrderPaymentStatus;

/**
 * Factory for creating {@link OrderPaymentMatcher}s for integration tests.
 */
public class OrderPaymentMatcherFactory {
	private OrderPaymentMatcherFactory() {
		// Prohibit instances of this class being created.
	}

	/**
	 * Gets a successful credit.
	 *
	 * @return the successful credit
	 */
	public static OrderPaymentMatcher createSuccessfulCredit() {
		return new OrderPaymentMatcher(OrderPaymentStatus.APPROVED, CREDIT);
	}

	/**
	 * Gets a successful charge.
	 *
	 * @return the successful charge
	 */
	public static OrderPaymentMatcher createSuccessfulCharge() {
		return new OrderPaymentMatcher(OrderPaymentStatus.APPROVED, CHARGE);
	}

	/**
	 * Gets a successful cancel.
	 *
	 * @return the successful cancel
	 */
	public static OrderPaymentMatcher createSuccessfulCancel() {
		return new OrderPaymentMatcher(OrderPaymentStatus.APPROVED, CANCEL_RESERVE);
	}

	/**
	 * Gets a successful reverse charge.
	 *
	 * @return the successful reverse charge
	 */
	public static OrderPaymentMatcher createSuccessfulReverseCharge() {
		return new OrderPaymentMatcher(OrderPaymentStatus.APPROVED, REVERSE_CHARGE);
	}

	/**
	 * Gets a successful treserve.
	 *
	 * @return the successful reserve
	 */
	public static OrderPaymentMatcher createSuccessfulReserve() {
		return new OrderPaymentMatcher(OrderPaymentStatus.APPROVED, RESERVE);
	}


	/**
	 * Gets a failed reserve.
	 *
	 * @return the failed reserve
	 */
	public static OrderPaymentMatcher createFailedReserve() {
		return new OrderPaymentMatcher(OrderPaymentStatus.FAILED, RESERVE);
	}

	/**
	 * Gets a failed charge.
	 *
	 * @return the failed charge
	 */
	public static OrderPaymentMatcher createFailedCharge() {
		return new OrderPaymentMatcher(OrderPaymentStatus.FAILED, CHARGE);
	}
}
