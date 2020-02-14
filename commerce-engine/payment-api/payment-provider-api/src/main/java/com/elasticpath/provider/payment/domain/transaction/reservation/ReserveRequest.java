/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.domain.transaction.reservation;

import java.util.List;
import java.util.Map;

import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.OrderContext;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIRequest;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;

/**
 * The Reserve request for Payment API.
 */
public class ReserveRequest implements PaymentAPIRequest {
	private List<OrderPaymentInstrumentDTO> selectedOrderPaymentInstruments;
	private MoneyDTO amount;
	private Map<String, String> customRequestData;
	private OrderContext orderContext;

	/**
	 * Gets selected instruments list.
	 *
	 * @return the selected instruments list
	 */
	public List<OrderPaymentInstrumentDTO> getSelectedOrderPaymentInstruments() {
		return selectedOrderPaymentInstruments;
	}

	/**
	 * Sets selected instruments list.
	 *
	 * @param target the selected instruments list
	 */
	public void setSelectedOrderPaymentInstruments(final List<OrderPaymentInstrumentDTO> target) {
		this.selectedOrderPaymentInstruments = target;
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
