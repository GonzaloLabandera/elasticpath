/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.cmclient.fulfillment.wizards.refund;

import java.util.Collection;
import java.util.List;

import com.elasticpath.domain.order.Order;
import com.elasticpath.money.Money;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.service.orderpaymentapi.management.PaymentStatistic;

/**
 * Utility container for information related to refund process.
 */
public class RefundItem {
	/**
	 * The order for which refund should be made.
	 */
	private Order order;

	/**
	 * Amount of money to be refunded.
	 */
	private Money refundedAmount;

	/**
	 * The payment instruments used for the order. May be empty if order is free.
	 */
	private List<PaymentInstrumentDTO> originalInstruments;

	/**
	 * Reauthorization transactions (instruments mapped to their authorised amounts).
	 */
	private Collection<PaymentStatistic> paymentTransactions;

	/**
	 * Flag of manual refund (no instruments were used).
	 */
	private boolean manual;

	/**
	 * @return the order
	 */
	public Order getOrder() {
		return order;
	}

	/**
	 * @param order the order to set
	 */
	public void setOrder(final Order order) {
		this.order = order;
	}

	/**
	 * @return the refundedAmount
	 */
	public Money getRefundedAmount() {
		return refundedAmount;
	}

	/**
	 * @param refundedAmount the amount to set
	 */
	public void setRefundedAmount(final Money refundedAmount) {
		this.refundedAmount = refundedAmount;
	}

	/**
	 * @return the originalInstruments
	 */
	public List<PaymentInstrumentDTO> getOriginalInstruments() {
		return originalInstruments;
	}

	/**
	 * @param originalInstruments the originalInstruments to set
	 */
	public void setOriginalInstruments(final List<PaymentInstrumentDTO> originalInstruments) {
		this.originalInstruments = originalInstruments;
	}

	/**
	 * @return the paymentTransactions
	 */
	public Collection<PaymentStatistic> getPaymentTransactions() {
		return paymentTransactions;
	}

	/**
	 * @param paymentTransactions the paymentTransactions to set
	 */
	public void setPaymentTransactions(final Collection<PaymentStatistic> paymentTransactions) {
		this.paymentTransactions = paymentTransactions;
	}

	/**
	 * @return the manual refund flag
	 */
	public boolean isManual() {
		return manual;
	}

	/**
	 * @param manual the manual refund flag to set
	 */
	public void setManual(final boolean manual) {
		this.manual = manual;
	}
}
