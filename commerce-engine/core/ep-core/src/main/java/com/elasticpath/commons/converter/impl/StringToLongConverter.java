/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.commons.converter.impl;

import com.elasticpath.commons.converter.ConversionMalformedValueException;
import com.elasticpath.commons.converter.StringToTypeConverter;

/**
 * Converts a string to a long.
 */
public class StringToLongConverter implements StringToTypeConverter<Long> {

	@Override
	public Long convert(final String stringValue) {
		try {
			return Long.valueOf(stringValue);
		} catch (NumberFormatException e) {
			throw new ConversionMalformedValueException("Cannot convert [" + stringValue + "] to Long", e);
		}
	}

}
