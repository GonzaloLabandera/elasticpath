/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.history;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import com.elasticpath.provider.payment.service.event.PaymentEvent;

/**
 * Represents a chain of payment events.
 */
public class PaymentEventChain {

	private final Deque<PaymentEvent> chain = new LinkedList<>();

	/**
	 * Constructor.
	 */
	public PaymentEventChain() {
		// Default constructor to create Spring prototype bean
	}

	/**
	 * Constructor.
	 *
	 * @param paymentEvent root payment event.
	 */
	public PaymentEventChain(final PaymentEvent paymentEvent) {
		this.chain.add(paymentEvent);
	}

	/**
	 * Constructor.
	 *
	 * @param paymentEvents source deque of payment events.
	 */
	public PaymentEventChain(final Deque<PaymentEvent> paymentEvents) {
		this.chain.addAll(paymentEvents);
	}

	/**
	 * Add payment event to payment event chain.
	 *
	 * @param paymentEvent source payment event.
	 */
	public void addPaymentEvent(final PaymentEvent paymentEvent) {
		chain.add(paymentEvent);
	}

	/**
	 * Gets last payment event in payment event chain.
	 *
	 * @return last payment event in payment event chain.
	 */
	public PaymentEvent getLast() {
		return chain.getLast();
	}

	/**
	 * Gets list of payment events in chain.
	 *
	 * @return list of payment events in chain.
	 */
	public List<PaymentEvent> getPaymentEvents() {
		return new ArrayList<>(chain);
	}

	/**
	 * Create downstream payment event chain with new payment event.
	 *
	 * @param paymentEvent source payment event.
	 * @return downstream payment event chain with new payment event.
	 */
	public PaymentEventChain createDownstreamPaymentEventChain(final PaymentEvent paymentEvent) {
		final PaymentEventChain paymentEventChain = new PaymentEventChain(chain);
		paymentEventChain.addPaymentEvent(paymentEvent);

		return paymentEventChain;
	}

}
