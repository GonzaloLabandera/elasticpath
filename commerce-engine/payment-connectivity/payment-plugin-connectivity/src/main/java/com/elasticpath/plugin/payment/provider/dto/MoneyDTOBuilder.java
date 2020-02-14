/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.plugin.payment.provider.dto;

import java.math.BigDecimal;

/**
 * MoneyDTO builder.
 */
public final class MoneyDTOBuilder {
	private BigDecimal amount;
	private String currencyCode;

	private MoneyDTOBuilder() {
	}

	/**
	 * A money dto builder.
	 *
	 * @return builder
	 */
	public static MoneyDTOBuilder builder() {
		return new MoneyDTOBuilder();
	}

	/**
	 * Configures builder to build with amount.
	 *
	 * @param amount the amount
	 * @return the builder
	 */
	public MoneyDTOBuilder withAmount(final BigDecimal amount) {
		this.amount = amount;
		return this;
	}

	/**
	 * Configures builder to build with currency code.
	 *
	 * @param currencyCode the currency code
	 * @return the builder
	 */
	public MoneyDTOBuilder withCurrencyCode(final String currencyCode) {
		this.currencyCode = currencyCode;
		return this;
	}

	/**
	 * Build MoneyDTO.
	 *
	 * @param prototype bean prototype
	 * @return populated object
	 */
	public MoneyDTO build(final MoneyDTO prototype) {
		if (amount == null) {
			throw new IllegalStateException("Builder is not fully initialized, amount is missing");
		}
		prototype.setAmount(amount);
		prototype.setCurrencyCode(currencyCode);
		return prototype;
	}
}