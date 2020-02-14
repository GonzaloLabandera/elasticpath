/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.orderpaymentapi;

import java.math.BigDecimal;
import java.util.Currency;

import com.elasticpath.persistence.api.Entity;

/**
 * Payment Instrument related to Order and CartOrder.
 */
public interface CorePaymentInstrument extends Entity {

	/**
	 * Sets the payment instrument guid.
	 *
	 * @param paymentInstrumentGuid payment instrument guid
	 */
	void setPaymentInstrumentGuid(String paymentInstrumentGuid);

	/**
	 * Get the payment instrument guid.
	 *
	 * @return guid
	 */
	String getPaymentInstrumentGuid();

	/**
	 * Sets the limit amount.
	 *
	 * @param limitAmount limit amount
	 */
	void setLimitAmount(BigDecimal limitAmount);

	/**
	 * Get the limit amount.
	 *
	 * @return limit amount
	 */
	BigDecimal getLimitAmount();

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

}
