/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.commons.converter.impl;

import java.math.BigDecimal;

import com.elasticpath.commons.converter.ConversionMalformedValueException;
import com.elasticpath.commons.converter.StringToTypeConverter;

/**
 * Converts a string to a BigDecimal.
 */
public class StringToBigDecimalConverter implements StringToTypeConverter<BigDecimal> {

	@Override
	public BigDecimal convert(final String stringValue) {
		try {
			return BigDecimal.valueOf(Double.valueOf(stringValue));
		} catch (NumberFormatException e) {
			throw new ConversionMalformedValueException("Cannot convert [" + stringValue + "] to BigDecimal", e);
		}
	}

}
