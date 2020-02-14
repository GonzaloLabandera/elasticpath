/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.plugin.payment.provider.dto;

import java.util.List;

/**
 * OrderContext builder.
 */
public final class OrderContextBuilder {
	private List<OrderSkuDTO> orderSkus;
	private MoneyDTO orderTotal;
	private String orderNumber;
	private String customerEmail;
	private AddressDTO billingAddress;

	private OrderContextBuilder() {
	}

	/**
	 * An order context builder.
	 *
	 * @return builder
	 */
	public static OrderContextBuilder builder() {
		return new OrderContextBuilder();
	}

	/**
	 * Configures builder to build with order skus.
	 *
	 * @param orderSkus the order skus
	 * @return the builder
	 */
	public OrderContextBuilder withOrderSkus(final List<OrderSkuDTO> orderSkus) {
		this.orderSkus = orderSkus;
		return this;
	}

	/**
	 * Configures builder to build with orderTotal.
	 *
	 * @param orderTotal the orderTotal
	 * @return the builder
	 */
	public OrderContextBuilder withOrderTotal(final MoneyDTO orderTotal) {
		this.orderTotal = orderTotal;
		return this;
	}

	/**
	 * Configures builder to build with orderNumber.
	 *
	 * @param orderNumber the orderNumber
	 * @return the builder
	 */
	public OrderContextBuilder withOrderNumber(final String orderNumber) {
		this.orderNumber = orderNumber;
		return this;
	}

	/**
	 * Configures builder to build with customerEmail.
	 *
	 * @param customerEmail the customerEmail
	 * @return the builder
	 */
	public OrderContextBuilder withCustomerEmail(final String customerEmail) {
		this.customerEmail = customerEmail;
		return this;
	}

	/**
	 * Configures builder to build with billingAddress.
	 *
	 * @param billingAddress the billingAddress
	 * @return the builder
	 */
	public OrderContextBuilder withBillingAddress(final AddressDTO billingAddress) {
		this.billingAddress = billingAddress;
		return this;
	}

	/**
	 * Build OrderContext.
	 *
	 * @param prototype bean prototype
	 * @return populated object
	 */
	public OrderContext build(final OrderContext prototype) {
		if (orderSkus == null) {
			throw new IllegalStateException("Builder is not fully initialized, orderSkus are missing");
		}
		if (orderTotal == null) {
			throw new IllegalStateException("Builder is not fully initialized, orderTotal is missing");
		}
		if (orderNumber == null) {
			throw new IllegalStateException("Builder is not fully initialized, orderNumber is missing");
		}
		prototype.setOrderSkus(orderSkus);
		prototype.setOrderTotal(orderTotal);
		prototype.setOrderNumber(orderNumber);
		prototype.setCustomerEmail(customerEmail);
		prototype.setBillingAddress(billingAddress);
		return prototype;
	}
}