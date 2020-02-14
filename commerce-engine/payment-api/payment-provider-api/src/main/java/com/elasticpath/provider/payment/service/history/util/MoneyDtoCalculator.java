/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.history.util;

import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.util.Objects;

import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTOBuilder;

/**
 * Represents calculator for mathematical operations with money dto.
 */
public class MoneyDtoCalculator {

	/**
	 * Creates money dto.
	 *
	 * @param amount       amount
	 * @param currencyCode currency code
	 * @return MoneyDTO
	 */
	public MoneyDTO create(final BigDecimal amount, final String currencyCode) {
		return MoneyDTOBuilder.builder()
				.withAmount(amount)
				.withCurrencyCode(currencyCode)
				.build(new MoneyDTO());
	}

	/**
	 * Sum up two money dto and return new value.
	 *
	 * @param operand1 first operand.
	 * @param operand2 second operand.
	 * @return result of the sum.
	 */
	public MoneyDTO plus(final MoneyDTO operand1, final MoneyDTO operand2) {
		final BigDecimal amount = operand1.getAmount().add(operand2.getAmount());
		return create(amount, getCurrencyCode(operand1, operand2));
	}

	/**
	 * Increase operand value.
	 *
	 * @param operand   operand to increase.
	 * @param increment value.
	 */
	public void increase(final MoneyDTO operand, final MoneyDTO increment) {
		final BigDecimal amount = operand.getAmount().add(increment.getAmount());
		operand.setCurrencyCode(getCurrencyCode(operand, increment));
		operand.setAmount(amount);
	}

	/**
	 * Subtract second money dto operand from the first.
	 *
	 * @param operand1 first operand.
	 * @param operand2 second operand.
	 * @return result of the subtract.
	 */
	public MoneyDTO minus(final MoneyDTO operand1, final MoneyDTO operand2) {
		final BigDecimal amount = operand1.getAmount().add(operand2.getAmount().negate());
		return create(amount, getCurrencyCode(operand1, operand2));
	}

	/**
	 * Decrease operand value.
	 *
	 * @param operand   operand to increase.
	 * @param decrement value.
	 */
	public void decrease(final MoneyDTO operand, final MoneyDTO decrement) {
		final BigDecimal amount = operand.getAmount().add(decrement.getAmount().negate());
		operand.setCurrencyCode(getCurrencyCode(operand, decrement));
		operand.setAmount(amount);
	}

	/**
	 * Convert operand value to absolute.
	 *
	 * @param operand operand to convert
	 * @return result of the operation
	 */
	public MoneyDTO abs(final MoneyDTO operand) {
		if (operand.getAmount().compareTo(ZERO) < 0) {
			final MoneyDTO clone = cloneMoneyDto(operand);
			clone.setAmount(operand.getAmount().negate());
			return clone;
		}
		return operand;
	}

	/**
	 * Set up amount of operand to zero.
	 *
	 * @param operand operand to set up zero amount.
	 */
	public void resetToZero(final MoneyDTO operand) {
		operand.setAmount(ZERO);
	}

	/**
	 * Returns money dto with zero amount.
	 *
	 * @return money dto with zero amount.
	 */
	public MoneyDTO zeroMoneyDto() {
		return MoneyDTOBuilder.builder()
				.withAmount(ZERO)
				.build(new MoneyDTO());
	}

	/**
	 * Compares money dto with zero.
	 *
	 * @param moneyDto money dto to compare.
	 * @return true if money dto is not zero, false if money dto is zero.
	 */
	public boolean hasBalance(final MoneyDTO moneyDto) {
		return moneyDto.getAmount().compareTo(ZERO) != 0;
	}

	/**
	 * Check is money dto negative.
	 *
	 * @param operand money dto operand.
	 * @return true if money dto is negative, false is money dto positive.
	 */
	public boolean isNegative(final MoneyDTO operand) {
		return operand.getAmount().compareTo(ZERO) < 0;
	}

	/**
	 * Check is money dto positive.
	 *
	 * @param operand money dto operand.
	 * @return true if money dto is positive, false is money dto negative.
	 */
	public boolean isPositive(final MoneyDTO operand) {
		return operand.getAmount().compareTo(ZERO) > 0;
	}

	/**
	 * Compares two money dto.
	 *
	 * @param operand1 first operand.
	 * @param operand2 second operand.
	 * @return -1, 0, or 1 as operand1 is numerically less than, equal to, or greater than operand2.
	 */
	public int compare(final MoneyDTO operand1, final MoneyDTO operand2) {
		return operand1.getAmount().compareTo(operand2.getAmount());
	}

	/**
	 * Creates money dto with same values of amount and currency code as in operand.
	 *
	 * @param operand money dto operand.
	 * @return copy of money dto.
	 */
	public MoneyDTO cloneMoneyDto(final MoneyDTO operand) {
		return MoneyDTOBuilder.builder()
				.withAmount(operand.getAmount())
				.withCurrencyCode(operand.getCurrencyCode())
				.build(new MoneyDTO());
	}

	private String getCurrencyCode(final MoneyDTO moneyDTO1, final MoneyDTO moneyDTO2) {
		return Objects.isNull(moneyDTO2.getCurrencyCode()) ? moneyDTO1.getCurrencyCode() : moneyDTO2.getCurrencyCode();
	}

}
