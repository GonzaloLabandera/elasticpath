/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.orderpaymentapi;

import com.elasticpath.money.Money;

/**
 * <p>Class encapsulating information amount the calculated amounts for an order based on the order total and order payments.
 * Some of the amounts are: amounts due and amount paid</p>
 * <p>Extend this class to enrich such requests, i.e. in cases where a client application requires
 * additional information beyond what is currently provided here.</p>
 */
public class OrderPaymentAmounts {
	private Money amountDue;
	private Money amountPaid;
	private Money amountRefunded;
	private Money amountRefundable;

	public Money getAmountDue() {
		return amountDue;
	}

	public void setAmountDue(final Money amountDue) {
		this.amountDue = amountDue;
	}

	public Money getAmountPaid() {
		return amountPaid;
	}

	public void setAmountPaid(final Money amountPaid) {
		this.amountPaid = amountPaid;
	}

	public Money getAmountRefunded() {
		return amountRefunded;
	}

	public void setAmountRefunded(final Money amountRefunded) {
		this.amountRefunded = amountRefunded;
	}

	public Money getAmountRefundable() {
		return amountRefundable;
	}

	public void setAmountRefundable(final Money amountRefundable) {
		this.amountRefundable = amountRefundable;
	}
}
