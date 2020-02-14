/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider.capabilities.credit;

import java.util.Map;

import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.OrderContext;

/**
 * Credit capability request builder.
 */
public final class CreditCapabilityRequestBuilder {

	private MoneyDTO amount;
	private Map<String, String> paymentInstrumentData;
	private Map<String, String> customRequestData;
	private OrderContext orderContext;
	private Map<String, String> chargeData;

	private CreditCapabilityRequestBuilder() {
	}

	/**
	 * Creates credit capability request builder.
	 *
	 * @return the builder
	 */
	public static CreditCapabilityRequestBuilder builder() {
		return new CreditCapabilityRequestBuilder();
	}

	/**
	 * Configures builder to build with amount of money.
	 *
	 * @param amount the amount
	 * @return the builder
	 */
	public CreditCapabilityRequestBuilder withAmount(final MoneyDTO amount) {
		this.amount = amount;
		return this;
	}

	/**
	 * Configures builder to build with payment instrument data.
	 *
	 * @param paymentInstrumentData the payment instrument data
	 * @return the builder
	 */
	public CreditCapabilityRequestBuilder withPaymentInstrumentData(final Map<String, String> paymentInstrumentData) {
		this.paymentInstrumentData = paymentInstrumentData;
		return this;
	}

	/**
	 * Configures builder to build with custom request data.
	 *
	 * @param customRequestData the custom request data
	 * @return the builder
	 */
	public CreditCapabilityRequestBuilder withCustomRequestData(final Map<String, String> customRequestData) {
		this.customRequestData = customRequestData;
		return this;
	}

	/**
	 * Configures builder to build with order context.
	 *
	 * @param orderContext the order context
	 * @return the builder
	 */
	public CreditCapabilityRequestBuilder withOrderContext(final OrderContext orderContext) {
		this.orderContext = orderContext;
		return this;
	}

	/**
	 * Configures builder to build with charge data.
	 *
	 * @param chargeData the charge data
	 * @return the builder
	 */
	public CreditCapabilityRequestBuilder withChargeData(final Map<String, String> chargeData) {
		this.chargeData = chargeData;
		return this;
	}

	/**
	 * Build payment capability request.
	 *
	 * @param request request prototype
	 * @return populated request
	 */
	public CreditCapabilityRequest build(final CreditCapabilityRequest request) {
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
		if (chargeData == null) {
			throw new IllegalStateException("Builder is not fully initialized, chargeData is missing");
		}
		request.setAmount(amount);
		request.setPaymentInstrumentData(paymentInstrumentData);
		request.setCustomRequestData(customRequestData);
		request.setOrderContext(orderContext);
		request.setChargeData(chargeData);
		return request;
	}
}
