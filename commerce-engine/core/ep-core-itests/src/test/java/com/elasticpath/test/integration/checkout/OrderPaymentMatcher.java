/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.test.integration.checkout;

import com.elasticpath.domain.order.OrderPaymentStatus;
import com.elasticpath.domain.orderpaymentapi.OrderPayment;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;

public class OrderPaymentMatcher {
	private OrderPaymentStatus status;
	private TransactionType transaction;

	OrderPaymentMatcher(OrderPaymentStatus status, TransactionType transaction) {
		this.status = status;
		this.transaction = transaction;
	}

	public boolean matches(final OrderPayment orderPayment) {
		if (!status.equals(orderPayment.getOrderPaymentStatus())) {
			return false;
		}

		if (!transaction.equals(orderPayment.getTransactionType())) {
			return false;
		}

		return true;
	}
}
