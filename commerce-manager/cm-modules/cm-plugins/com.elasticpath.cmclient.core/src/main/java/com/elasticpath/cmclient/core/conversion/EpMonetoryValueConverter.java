/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.cmclient.core.conversion;

import java.math.BigDecimal;
import java.util.Currency;

import org.eclipse.core.databinding.conversion.Converter;

/**
 * Converter for converting a money representing BigDecimal field into String. It uses corresponding currency's fractional digits number as scale
 * when scale is less than default fractional digits of the currency.
 */
public class EpMonetoryValueConverter extends Converter {

	private final Currency currency;

	/**
	 * Creates a new converter for BigDecimal representing monetary values for a specified Currency.
	 *
	 * @param currency The currency whose fractional digits are used as scale to be set on the BigDecimal value as part of conversion.
	 */
	public EpMonetoryValueConverter(final Currency currency) {
		super(BigDecimal.class, String.class);
		this.currency = currency;
	}

	/**
	 * Converts from BigDecimal to String. Use currency's default fractional digits as scale, if the scale is less than
	 * default fractional digits.
	 *
	 * @param fromObject object to convert
	 * @return resulting object
	 */
	public Object convert(final Object fromObject) {
		if (currency == null) {
			throw new IllegalArgumentException("The currency argument cannot be null.");
		}

		BigDecimal scaleCorrectedValue = upscaleIfApplicable(currency, fromObject);
		return scaleCorrectedValue.toPlainString();
	}

	/**
	 * Modifes the scale of fromValue to expectedScale if its less than expectedScale.
	 * @param currency The currency whose fractional digits are used as scale to be set on the BigDecimal value as part of conversion.
	 * @param fromObject object to convert
	 * @return resulting BigDecimal
	 */
	public static BigDecimal upscaleIfApplicable(final Currency currency, final Object fromObject) {
		if (!(fromObject instanceof BigDecimal)) {
			throw new IllegalArgumentException("The argument to process is not BigDecimal"); //$NON-NLS-1$
		}

		if (currency == null) {
			throw new IllegalArgumentException("The currency argument cannot be null"); //$NON-NLS-1$
		}

		BigDecimal fromValue = (BigDecimal) fromObject;

		if (currency.getDefaultFractionDigits() > fromValue.scale()) {
			return fromValue.setScale(currency.getDefaultFractionDigits());
		}

		return fromValue;
	}

	/**
	 * Convenience method that casts the result of convert() to a string.
	 *
	 * @param fromObject object to convert
	 * @return resulting String
	 */
	public String asString(final Object fromObject) {
		return (String) convert(fromObject);
	}

	public Currency getCurrency() {
		return currency;
	}
}
