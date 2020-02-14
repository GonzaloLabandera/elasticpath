/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.domain.orderpaymentapi;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.Set;

import com.elasticpath.domain.order.OrderPaymentStatus;
import com.elasticpath.persistence.api.Entity;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;

/**
 * Placeholder for OrderPayment for now but will be deleted later.
 */
public interface OrderPayment extends Entity {

	/**
	 * Get the order number.
	 *
	 * @return order number
	 */
	String getOrderNumber();

	/**
	 * Sets the order number.
	 *
	 * @param orderNumber order number
	 */
	void setOrderNumber(String orderNumber);

	/**
	 * Set the created date.
	 *
	 * @param createdDate date
	 */
	void setCreatedDate(Date createdDate);

	/**
	 * Get the created date.
	 *
	 * @return created date
	 */
	Date getCreatedDate();

	/**
	 * Sets the transaction type.
	 *
	 * @param transactionType transaction type
	 */
	void setTransactionType(TransactionType transactionType);

	/**
	 * Gets the transaction type.
	 *
	 * @return transaction type
	 */
	TransactionType getTransactionType();

	/**
	 * Set the order status.
	 *
	 * @param orderPaymentStatus status
	 */
	void setOrderPaymentStatus(OrderPaymentStatus orderPaymentStatus);

	/**
	 * Get the order payment status.
	 *
	 * @return status
	 */
	OrderPaymentStatus getOrderPaymentStatus();

	/**
	 * Set the amount.
	 *
	 * @param amount amount
	 */
	void setAmount(BigDecimal amount);

	/**
	 * Get the amount.
	 *
	 * @return amount
	 */
	BigDecimal getAmount();

	/**
	 * Set the currency.
	 *
	 * @param currency currency
	 */
	void setCurrency(Currency currency);

	/**
	 * Get the currency.
	 *
	 * @return currency
	 */
	Currency getCurrency();

	/**
	 * Set the parent OrderPayment guid.
	 *
	 * @param parentOrderPaymentGuid parent OrderPayment guid
	 */
	void setParentOrderPaymentGuid(String parentOrderPaymentGuid);

	/**
	 * Parent OrderPayment guid.
	 *
	 * @return parent OrderPayment guid
	 */
	String getParentOrderPaymentGuid();

	/**
	 * Get all data keyed on the key in order payment data.
	 *
	 * @return all order payment data
	 */
	Set<OrderPaymentData> getOrderPaymentData();

	/**
	 * Set the order payment data.
	 *
	 * @param orderPaymentData order payment data
	 */
	void setOrderPaymentData(Set<OrderPaymentData> orderPaymentData);

	/**
	 * Get the payment instrument guid.
	 *
	 * @return payment instrument guid
	 */
	String getPaymentInstrumentGuid();

	/**
	 * Sets the payment instrument guid.
	 *
	 * @param paymentInstrumentGuid payment instrument guid
	 */
	void setPaymentInstrumentGuid(String paymentInstrumentGuid);

	/**
	 * @return true if the payment instrument associated with the order {@link OrderPayment#getOrderNumber()} was used, false otherwise.
	 */
	boolean isOriginalPI();

	/**
	 * @param isOriginalPI if the payment instrument associated with the order {@link OrderPayment#getOrderNumber()} was used.
	 */
	void setOriginalPI(boolean isOriginalPI);
}
