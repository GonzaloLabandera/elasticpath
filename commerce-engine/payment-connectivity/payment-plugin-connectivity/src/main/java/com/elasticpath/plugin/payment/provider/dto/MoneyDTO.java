/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider.dto;

import java.math.BigDecimal;

/**
 * Represents a specific monetary amount, with currency.
 */
public class MoneyDTO {

	private BigDecimal amount;
	private String currencyCode;

	/**
	 * Get the amount of this payment.
	 *
	 * @return the amount as a BigDecimal
	 */
	public BigDecimal getAmount() {
		return amount;
	}

	/**
	 * Set the amount of this payment.
	 *
	 * @param amount the amount as a BigDecimal
	 */
	public void setAmount(final BigDecimal amount) {
		this.amount = amount;
	}

	/**
	 * Get the currency code (e.g. CAD or USD).
	 *
	 * @return the currency code
	 */
	public String getCurrencyCode() {
		return currencyCode;
	}

	/**
	 * Set the currency code.
	 *
	 * @param currencyCode the currency code code
	 */
	public void setCurrencyCode(final String currencyCode) {
		this.currencyCode = currencyCode;
	}

	@Override
	public String toString() {
		return "MoneyDTO(" + amount + " " + currencyCode + ")";
	}
}
