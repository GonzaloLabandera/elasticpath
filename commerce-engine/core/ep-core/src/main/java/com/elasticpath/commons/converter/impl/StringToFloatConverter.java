/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.commons.converter.impl;

import com.elasticpath.commons.converter.ConversionMalformedValueException;
import com.elasticpath.commons.converter.StringToTypeConverter;

/**
 * Converts a string to a float.
 */
public class StringToFloatConverter implements StringToTypeConverter<Float> {

	@Override
	public Float convert(final String stringValue) {
		try {
			return Float.valueOf(stringValue);
		} catch (NumberFormatException e) {
			throw new ConversionMalformedValueException("Cannot convert [" + stringValue + "] to Float", e);
		}
	}

}
