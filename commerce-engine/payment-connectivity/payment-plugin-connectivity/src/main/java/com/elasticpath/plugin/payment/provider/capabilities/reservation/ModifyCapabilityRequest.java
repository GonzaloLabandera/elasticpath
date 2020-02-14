/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider.capabilities.reservation;

import java.util.Map;

import com.elasticpath.plugin.payment.provider.capabilities.ReserveCapabilityFollowupRequest;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.OrderContext;

/**
 * Reservation request.
 */
public class ModifyCapabilityRequest implements ReserveCapabilityFollowupRequest {
	private MoneyDTO amount;
	private Map<String, String> paymentInstrumentData;
	private Map<String, String> customRequestData;
	private Map<String, String> pluginConfigData;
	private Map<String, String> reservationData;
	private OrderContext orderContext;

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
	public Map<String, String> getReservationData() {
		return this.reservationData;
	}

	@Override
	public void setReservationData(final Map<String, String> reservationData) {
		this.reservationData = reservationData;
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
	public Map<String, String> getPaymentInstrumentData() {
		return paymentInstrumentData;
	}

	@Override
	public void setPaymentInstrumentData(final Map<String, String> paymentInstrumentData) {
		this.paymentInstrumentData = paymentInstrumentData;
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
	public Map<String, String> getPluginConfigData() {
		return pluginConfigData;
	}

	@Override
	public void setPluginConfigData(final Map<String, String> pluginConfigData) {
		this.pluginConfigData = pluginConfigData;
	}

}
