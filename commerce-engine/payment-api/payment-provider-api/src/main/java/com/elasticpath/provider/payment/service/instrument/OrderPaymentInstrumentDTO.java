/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.instrument;

import java.math.BigDecimal;
import java.util.Map;

import com.elasticpath.plugin.payment.provider.dto.AddressDTO;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;

/**
 * A DTO object of an order payment instrument.
 */
public class OrderPaymentInstrumentDTO {

	private String guid;
	private PaymentInstrumentDTO paymentInstrument;
	private MoneyDTO limit;
	private String orderNumber;
	private Map<String, String> orderPaymentInstrumentData;
	private String customerEmail;
	private AddressDTO billingAddress;

	/**
	 * Gets guid.
	 *
	 * @return the guid
	 */
	public String getGUID() {
		return guid;
	}

	/**
	 * Sets guid.
	 *
	 * @param guid the guid
	 */
	public void setGUID(final String guid) {
		this.guid = guid;
	}

	/**
	 * Gets payment instrument.
	 *
	 * @return the payment instrument DTO
	 */
	public PaymentInstrumentDTO getPaymentInstrument() {
		return paymentInstrument;
	}

	/**
	 * Sets payment instrument.
	 *
	 * @param paymentInstrument the payment instrument DTO
	 */
	public void setPaymentInstrument(final PaymentInstrumentDTO paymentInstrument) {
		this.paymentInstrument = paymentInstrument;
	}

	/**
	 * Gets limit.
	 *
	 * @return the limit
	 */
	public MoneyDTO getLimit() {
		return limit;
	}

	/**
	 * Set limit.
	 *
	 * @param limit the limit
	 */
	public void setLimit(final MoneyDTO limit) {
		this.limit = limit;
	}

	/**
	 * Gets if there is a limit set for the order payment instrument.
	 *
	 * @return if the limit was set
	 */
	public boolean hasLimit() {
		return limit != null && BigDecimal.ZERO.compareTo(limit.getAmount()) != 0;
	}

	/**
	 * Gets order number.
	 *
	 * @return the order number
	 */
	public String getOrderNumber() {
		return orderNumber;
	}

	/**
	 * Set order number.
	 *
	 * @param orderNumber the order number
	 */
	public void setOrderNumber(final String orderNumber) {
		this.orderNumber = orderNumber;
	}

	/**
	 * Gets custom request data for plugin.
	 *
	 * @return the data
	 */
	public Map<String, String> getOrderPaymentInstrumentData() {
		return orderPaymentInstrumentData;
	}

	/**
	 * Sets custom request data for plugin.
	 *
	 * @param orderPaymentInstrumentData the data
	 */
	public void setOrderPaymentInstrumentData(final Map<String, String> orderPaymentInstrumentData) {
		this.orderPaymentInstrumentData = orderPaymentInstrumentData;
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
	 * Sets customer email.
	 *
	 * @param customerEmail the customer email
	 */
	public void setCustomerEmail(final String customerEmail) {
		this.customerEmail = customerEmail;
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
	 * Sets billing address.
	 *
	 * @param billingAddress the billing address
	 */
	public void setBillingAddress(final AddressDTO billingAddress) {
		this.billingAddress = billingAddress;
	}

}
