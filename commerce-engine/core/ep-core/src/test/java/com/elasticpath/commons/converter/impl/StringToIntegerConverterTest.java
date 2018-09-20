/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.commons.converter.impl;

import static groovy.util.GroovyTestCase.assertEquals;

import org.junit.Test;

import com.elasticpath.commons.converter.ConversionMalformedValueException;

/**
 * Test class for {@link StringToIntegerConverter}.
 */
public class StringToIntegerConverterTest {

	private final StringToIntegerConverter converter = new StringToIntegerConverter();

	@Test
	public void testConvert() {
		final String value = "12";
		Integer result = converter.convert(value);

		assertEquals("Should convert to integer with expected value.", Integer.valueOf(value), result);
	}

	@Test(expected = ConversionMalformedValueException.class)
	public void testConvertThrowsExceptionOnMalformedValue() {
		converter.convert("abc");
	}
}
