/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.plugin.payment.provider.capabilities.reservation;

import java.util.Map;

import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.OrderContext;

/**
 * Reserve capability request builder.
 */
public final class ReserveCapabilityRequestBuilder {

	private MoneyDTO amount;
	private Map<String, String> paymentInstrumentData;
	private Map<String, String> customRequestData;
	private OrderContext orderContext;
	private int reserveCount;

	private ReserveCapabilityRequestBuilder() {
	}

	/**
	 * Creates a reserve capability request builder.
	 *
	 * @return the builder
	 */
	public static ReserveCapabilityRequestBuilder builder() {
		return new ReserveCapabilityRequestBuilder();
	}

	/**
	 * Configures builder to build with amount of money.
	 *
	 * @param amount the amount
	 * @return the builder
	 */
	public ReserveCapabilityRequestBuilder withAmount(final MoneyDTO amount) {
		this.amount = amount;
		return this;
	}

	/**
	 * Configures builder to build with payment instrument data.
	 *
	 * @param paymentInstrumentData the payment instrument data
	 * @return the builder
	 */
	public ReserveCapabilityRequestBuilder withPaymentInstrumentData(final Map<String, String> paymentInstrumentData) {
		this.paymentInstrumentData = paymentInstrumentData;
		return this;
	}

	/**
	 * Configures builder to build with custom request data.
	 *
	 * @param customRequestData the custom request data
	 * @return the builder
	 */
	public ReserveCapabilityRequestBuilder withCustomRequestData(final Map<String, String> customRequestData) {
		this.customRequestData = customRequestData;
		return this;
	}

	/**
	 * Configures builder to build with order context.
	 *
	 * @param orderContext the order context
	 * @return the builder
	 */
	public ReserveCapabilityRequestBuilder withOrderContext(final OrderContext orderContext) {
		this.orderContext = orderContext;
		return this;
	}

	/**
	 * Configures builder to build with rereserve count.
	 *
	 * @param reserveCount the rereserve count
	 * @return the builder
	 */
	public ReserveCapabilityRequestBuilder withRereserveCount(final int reserveCount) {
		this.reserveCount = reserveCount;
		return this;
	}

	/**
	 * Build payment capability request.
	 *
	 * @param request request prototype
	 * @return populated request
	 */
	public ReserveCapabilityRequest build(final ReserveCapabilityRequest request) {
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
		request.setAmount(amount);
		request.setPaymentInstrumentData(paymentInstrumentData);
		request.setCustomRequestData(customRequestData);
		request.setOrderContext(orderContext);
		request.setRereserveCount(reserveCount);
		return request;
	}
}
