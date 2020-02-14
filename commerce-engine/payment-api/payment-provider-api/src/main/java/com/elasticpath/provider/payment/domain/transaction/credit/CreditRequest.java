/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.domain.transaction.credit;

import java.util.List;
import java.util.Map;

import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.OrderContext;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIRequest;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;

/**
 * The credit payment request for Payment API.
 */
public class CreditRequest implements PaymentAPIRequest {
	private Map<String, String> customRequestData;
	private List<PaymentEvent> ledger;
	private List<OrderPaymentInstrumentDTO> orderPaymentInstruments;
	private List<OrderPaymentInstrumentDTO> selectedOrderPaymentInstruments;
	private MoneyDTO amount;
	private OrderContext orderContext;

	/**
	 * Gets selected instruments.
	 *
	 * @return selected instruments
	 */
	public List<OrderPaymentInstrumentDTO> getSelectedOrderPaymentInstruments() {
		return selectedOrderPaymentInstruments;
	}

	/**
	 * Sets selected instruments.
	 *
	 * @param selectedOrderPaymentInstruments selected instruments
	 */
	public void setSelectedOrderPaymentInstruments(final List<OrderPaymentInstrumentDTO> selectedOrderPaymentInstruments) {
		this.selectedOrderPaymentInstruments = selectedOrderPaymentInstruments;
	}

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
	 * Gets amount.
	 *
	 * @return the amount
	 */
	public MoneyDTO getAmount() {
		return amount;
	}

	/**
	 * Sets amount.
	 *
	 * @param amount the amount
	 */
	public void setAmount(final MoneyDTO amount) {
		this.amount = amount;
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
