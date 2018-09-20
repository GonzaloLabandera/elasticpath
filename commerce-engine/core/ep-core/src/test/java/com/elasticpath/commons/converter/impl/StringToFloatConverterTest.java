/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.commons.converter.impl;

import static groovy.util.GroovyTestCase.assertEquals;

import org.junit.Test;

import com.elasticpath.commons.converter.ConversionMalformedValueException;

/**
 * Test class for {@link StringToFloatConverter}.
 */
public class StringToFloatConverterTest {

	private final StringToFloatConverter converter = new StringToFloatConverter();

	@Test
	public void testConvert() {
		final String value = "12.0";
		Float result = converter.convert(value);

		assertEquals("Should convert to float with expected value.", new Float(value), result);
	}

	@Test(expected = ConversionMalformedValueException.class)
	public void testConvertThrowsExceptionOnMalformedValue() {
		converter.convert("abc");
	}

}
