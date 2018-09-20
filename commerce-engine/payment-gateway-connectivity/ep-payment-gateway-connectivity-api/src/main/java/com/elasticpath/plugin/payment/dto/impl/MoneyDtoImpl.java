/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.plugin.payment.dto.impl;

import java.math.BigDecimal;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.elasticpath.plugin.payment.dto.MoneyDto;

/**
 * Simple POJO implementation of @{link Money}.
 */
public class MoneyDtoImpl implements MoneyDto {
	private BigDecimal amount;
	private String currencyCode;
	
	/**
	 * Default constructor.
	 */
	public MoneyDtoImpl() {
		// empty constructor
	}
	
	/**
	 * Constructor.
	 *
	 * @param amount the amount of money
	 * @param currencyCode the currency code
	 */
	public MoneyDtoImpl(final BigDecimal amount, final String currencyCode) {
		this.amount = amount;
		this.currencyCode = currencyCode;
	}
	
	@Override
	public BigDecimal getAmount() {
		return amount;
	}

	@Override
	public void setAmount(final BigDecimal amount) {
		this.amount = amount;
	}

	@Override
	public String getCurrencyCode() {
		return currencyCode;
	}

	@Override
	public void setCurrencyCode(final String currencyCode) {
		this.currencyCode = currencyCode;
	}
	
	@Override
	public boolean equals(final Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}
	
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}
}
