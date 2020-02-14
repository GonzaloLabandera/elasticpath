/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider.capabilities.charge;

import java.util.Map;

import com.elasticpath.plugin.payment.provider.dto.OrderContext;

/**
 * Reverse charge capability request builder.
 */
public final class ReverseChargeCapabilityRequestBuilder {

	private Map<String, String> paymentInstrumentData;
	private Map<String, String> customRequestData;
	private OrderContext orderContext;
	private Map<String, String> chargeData;

	private ReverseChargeCapabilityRequestBuilder() {
	}

	/**
	 * Creates reverse charge capability request builder.
	 *
	 * @return the builder
	 */
	public static ReverseChargeCapabilityRequestBuilder builder() {
		return new ReverseChargeCapabilityRequestBuilder();
	}

	/**
	 * Configures builder to build with payment instrument data.
	 *
	 * @param paymentInstrumentData the payment instrument data
	 * @return the builder
	 */
	public ReverseChargeCapabilityRequestBuilder withPaymentInstrumentData(final Map<String, String> paymentInstrumentData) {
		this.paymentInstrumentData = paymentInstrumentData;
		return this;
	}

	/**
	 * Configures builder to build with custom request data.
	 *
	 * @param customRequestData the custom request data
	 * @return the builder
	 */
	public ReverseChargeCapabilityRequestBuilder withCustomRequestData(final Map<String, String> customRequestData) {
		this.customRequestData = customRequestData;
		return this;
	}

	/**
	 * Configures builder to build with order context.
	 *
	 * @param orderContext the order context
	 * @return the builder
	 */
	public ReverseChargeCapabilityRequestBuilder withOrderContext(final OrderContext orderContext) {
		this.orderContext = orderContext;
		return this;
	}

	/**
	 * Configures builder to build with charge data.
	 *
	 * @param chargeData the charge data
	 * @return the builder
	 */
	public ReverseChargeCapabilityRequestBuilder withChargeData(final Map<String, String> chargeData) {
		this.chargeData = chargeData;
		return this;
	}

	/**
	 * Build payment capability request.
	 *
	 * @param request request prototype
	 * @return populated request
	 */
	public ReverseChargeCapabilityRequest build(final ReverseChargeCapabilityRequest request) {
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
		request.setPaymentInstrumentData(paymentInstrumentData);
		request.setCustomRequestData(customRequestData);
		request.setOrderContext(orderContext);
		request.setChargeData(chargeData);
		return request;
	}
}
