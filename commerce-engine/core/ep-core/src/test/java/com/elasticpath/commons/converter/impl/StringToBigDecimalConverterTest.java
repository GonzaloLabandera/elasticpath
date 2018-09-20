/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.commons.converter.impl;

import static groovy.util.GroovyTestCase.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

import com.elasticpath.commons.converter.ConversionMalformedValueException;

/**
 * Test class for {@link StringToBigDecimalConverter}.
 */
public class StringToBigDecimalConverterTest {

	private final StringToBigDecimalConverter converter = new StringToBigDecimalConverter();

	@Test
	public void testConvert() {
		final String value = "12";
		BigDecimal result = converter.convert(value);

		assertEquals("Should convert to big decimal with expected value.", new BigDecimal(value), result);
	}

	@Test(expected = ConversionMalformedValueException.class)
	public void testConvertThrowsExceptionOnMalformedValue() {
		converter.convert("abc");
	}

}
