/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.domain.transaction.credit;

import java.util.List;
import java.util.Map;

import com.elasticpath.plugin.payment.provider.dto.OrderContext;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIRequest;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;

/**
 * The reverse charge request.
 */
public class ReverseChargeRequest implements PaymentAPIRequest {
	private Map<String, String> customRequestData;
	private List<PaymentEvent> ledger;
	private List<PaymentEvent> selectedPaymentEvents;
	private OrderContext orderContext;
	private List<OrderPaymentInstrumentDTO> orderPaymentInstruments;

	/**
	 * Gets instruments list.
	 *
	 * @return the instruments list
	 */
	public List<OrderPaymentInstrumentDTO> getOrderPaymentInstruments() {
		return orderPaymentInstruments;
	}

	/**
	 * Sets instruments list.
	 *
	 * @param orderPaymentInstruments the instruments list
	 */
	public void setOrderPaymentInstruments(final List<OrderPaymentInstrumentDTO> orderPaymentInstruments) {
		this.orderPaymentInstruments = orderPaymentInstruments;
	}

	/**
	 * Gets paymentEvents.
	 *
	 * @return the paymentEvents
	 */
	public List<PaymentEvent> getSelectedPaymentEvents() {
		return selectedPaymentEvents;
	}

	/**
	 * Sets paymentEvents to reverse charge.
	 *
	 * @param selectedPaymentEvents the payment events
	 */
	public void setSelectedPaymentEvents(final List<PaymentEvent> selectedPaymentEvents) {
		this.selectedPaymentEvents = selectedPaymentEvents;
	}

	/**
	 * Gets ledger.
	 *
	 * @return the ledger
	 */
	public List<PaymentEvent> getLedger() {
		return ledger;
	}

	/**
	 * Sets ledger.
	 *
	 * @param ledger the ledger
	 */
	public void setLedger(final List<PaymentEvent> ledger) {
		this.ledger = ledger;
	}

	@Override
	public OrderContext getOrderContext() {
		return orderContext;
	}

	@Override
	public void setOrderContext(final OrderContext orderContext) {
		this.orderContext = orderContext;
	}

	@Override
	public Map<String, String> getCustomRequestData() {
		return customRequestData;
	}

	@Override
	public void setCustomRequestData(final Map<String, String> data) {
		this.customRequestData = data;
	}
}