/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider.capabilities.reservation;

import java.util.Map;

import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.OrderContext;

/**
 * Modify capability request builder.
 */
public final class ModifyCapabilityRequestBuilder {

	private MoneyDTO amount;
	private Map<String, String> paymentInstrumentData;
	private Map<String, String> customRequestData;
	private OrderContext orderContext;
	private Map<String, String> reservationData;

	private ModifyCapabilityRequestBuilder() {
	}

	/**
	 * Creates modify capability request builder.
	 *
	 * @return the builder
	 */
	public static ModifyCapabilityRequestBuilder builder() {
		return new ModifyCapabilityRequestBuilder();
	}

	/**
	 * Configures builder to build with amount of money.
	 *
	 * @param amount the amount
	 * @return the builder
	 */
	public ModifyCapabilityRequestBuilder withAmount(final MoneyDTO amount) {
		this.amount = amount;
		return this;
	}

	/**
	 * Configures builder to build with payment instrument data.
	 *
	 * @param paymentInstrumentData the payment instrument data
	 * @return the builder
	 */
	public ModifyCapabilityRequestBuilder withPaymentInstrumentData(final Map<String, String> paymentInstrumentData) {
		this.paymentInstrumentData = paymentInstrumentData;
		return this;
	}

	/**
	 * Configures builder to build with custom request data.
	 *
	 * @param customRequestData the custom request data
	 * @return the builder
	 */
	public ModifyCapabilityRequestBuilder withCustomRequestData(final Map<String, String> customRequestData) {
		this.customRequestData = customRequestData;
		return this;
	}

	/**
	 * Configures builder to build with order context.
	 *
	 * @param orderContext the order context
	 * @return the builder
	 */
	public ModifyCapabilityRequestBuilder withOrderContext(final OrderContext orderContext) {
		this.orderContext = orderContext;
		return this;
	}

	/**
	 * Configures builder to build with reservation data.
	 *
	 * @param reservationData the reservation data
	 * @return the builder
	 */
	public ModifyCapabilityRequestBuilder withReservationData(final Map<String, String> reservationData) {
		this.reservationData = reservationData;
		return this;
	}

	/**
	 * Build payment capability request.
	 *
	 * @param request request prototype
	 * @return populated request
	 */
	public ModifyCapabilityRequest build(final ModifyCapabilityRequest request) {
		if (amount == null) {
			throw new IllegalStateException("Builder is not fully initialized, amount is missing");
		}
		if (paymentInstrumentData == null) {
			throw new IllegalStateException("Builder is not fully initialized, paymentInstrumentData is missing");
		}
		if (customRequestData == null) {
			throw new IllegalStateException("Builder is not fully initialized, customRequestData is missing");
		}
		if (orderContext == null) {
			throw new IllegalStateException("Builder is not fully initialized, orderContext is missing");
		}
		if (reservationData == null) {
			throw new IllegalStateException("Builder is not fully initialized, reservationData is missing");
		}
		request.setAmount(amount);
		request.setPaymentInstrumentData(paymentInstrumentData);
		request.setCustomRequestData(customRequestData);
		request.setOrderContext(orderContext);
		request.setReservationData(reservationData);
		return request;
	}

}
