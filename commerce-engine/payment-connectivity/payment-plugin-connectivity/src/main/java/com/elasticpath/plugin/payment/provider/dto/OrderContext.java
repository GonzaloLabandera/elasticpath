/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider.dto;

import java.util.List;

/**
 * Represents the order context object.
 */
public class OrderContext {

	private List<OrderSkuDTO> orderSkus;
	private MoneyDTO orderTotal;
	private String orderNumber;
	private String customerEmail;
	private AddressDTO billingAddress;

	/**
	 * Gets order SKUs.
	 *
	 * @return the list of OrderSkuDTO
	 */
	public List<OrderSkuDTO> getOrderSkus() {
		return orderSkus;
	}

	/**
	 * Sets order SKUs.
	 *
	 * @param orderSkus the list of OrderSkuDTO
	 */
	public void setOrderSkus(final List<OrderSkuDTO> orderSkus) {
		this.orderSkus = orderSkus;
	}

	/**
	 * Gets order total amount.
	 *
	 * @return total amount of money
	 */
	public MoneyDTO getOrderTotal() {
		return orderTotal;
	}

	/**
	 * Sets order total amount.
	 *
	 * @param orderTotal total amount of money
	 */
	public void setOrderTotal(final MoneyDTO orderTotal) {
		this.orderTotal = orderTotal;
	}

	/**
	 * Gets order number.
	 *
	 * @return order number
	 */
	public String getOrderNumber() {
		return orderNumber;
	}

	/**
	 * Sets order numeber.
	 *
	 * @param orderNumber order number
	 */
	public void setOrderNumber(final String orderNumber) {
		this.orderNumber = orderNumber;
	}

	/**
	 * Gets customer email.
	 *
	 * @return the customer email
	 */
	public String getCustomerEmail() {
		return customerEmail;
	}

	/**
	 * Gets billing address.
	 *
	 * @return the billing address
	 */
	public AddressDTO getBillingAddress() {
		return billingAddress;
	}

	/**
	 * Sets customer email.
	 *
	 * @param customerEmail the customer email
	 */
	public void setCustomerEmail(final String customerEmail) {
		this.customerEmail = customerEmail;
	}

	/**
	 * Sets billing address.
	 *
	 * @param billingAddress the billing address
	 */
	public void setBillingAddress(final AddressDTO billingAddress) {
		this.billingAddress = billingAddress;
	}
}
