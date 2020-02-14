/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.domain.transaction.cancel;

import java.util.List;
import java.util.Map;

import com.elasticpath.plugin.payment.provider.dto.OrderContext;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIRequest;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;

/**
 * The cancellation of all reservations request for Payment API.
 */
public class CancelAllReservationsRequest implements PaymentAPIRequest {

	private List<PaymentEvent> ledger;
	private Map<String, String> customRequestData;
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
	public Map<String, String> getCustomRequestData() {
		return customRequestData;
	}

	@Override
	public void setCustomRequestData(final Map<String, String> customRequestData) {
		this.customRequestData = customRequestData;
	}

	@Override
	public OrderContext getOrderContext() {
		return orderContext;
	}

	@Override
	public void setOrderContext(final OrderContext orderContext) {
		this.orderContext = orderContext;
	}
}
