/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.commons.converter.impl;

import static groovy.util.GroovyTestCase.assertEquals;

import org.junit.Test;

import com.elasticpath.commons.converter.ConversionMalformedValueException;

/**
 * Test class for {@link StringToLongConverter}.
 */
public class StringToLongConverterTest {

	private final StringToLongConverter converter = new StringToLongConverter();

	@Test
	public void testConvert() {
		final String value = "12";
		Long result = converter.convert(value);

		assertEquals("Should convert to long with expected value.", new Long(value), result);
	}

	@Test(expected = ConversionMalformedValueException.class)
	public void testConvertThrowsExceptionOnMalformedValue() {
		converter.convert("abc");
	}
}
