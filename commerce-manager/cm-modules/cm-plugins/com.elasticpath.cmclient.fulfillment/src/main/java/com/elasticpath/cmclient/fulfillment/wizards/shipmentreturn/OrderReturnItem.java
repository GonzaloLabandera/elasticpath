/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.cmclient.fulfillment.wizards.shipmentreturn;

import java.util.Collection;
import java.util.List;

import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderReturn;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.service.orderpaymentapi.management.PaymentStatistic;

/**
 * Utility container for information related to return process.
 */
public class OrderReturnItem {
	private OrderReturn orderReturn;
	private final List<PaymentInstrumentDTO> orderInstruments;

	private Collection<PaymentStatistic> paymentStatistics;

	/**
	 * Constructor.
	 *
	 * @param orderReturn      order return
	 * @param orderInstruments order purchase instruments
	 */
	public OrderReturnItem(final OrderReturn orderReturn, final List<PaymentInstrumentDTO> orderInstruments) {
		this.orderReturn = orderReturn;
		this.orderInstruments = orderInstruments;
	}

	public OrderReturn getOrderReturn() {
		return orderReturn;
	}

	public Order getOrder() {
		return orderReturn.getOrder();
	}

	/**
	 * Updates order return in this container.
	 *
	 * @param orderReturn updated order return
	 */
	public void updateOrderReturn(final OrderReturn orderReturn) {
		this.orderReturn = orderReturn;
	}

	public List<PaymentInstrumentDTO> getOrderInstruments() {
		return orderInstruments;
	}

	public Collection<PaymentStatistic> getPaymentStatistics() {
		return paymentStatistics;
	}

	public void setPaymentStatistics(final Collection<PaymentStatistic> paymentStatistics) {
		this.paymentStatistics = paymentStatistics;
	}
}
