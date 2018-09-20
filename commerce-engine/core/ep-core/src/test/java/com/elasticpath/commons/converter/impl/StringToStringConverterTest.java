/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.commons.converter.impl;

import static groovy.util.GroovyTestCase.assertEquals;

import org.junit.Test;

/**
 * Test class for {@link StringToStringConverter}.
 */
public class StringToStringConverterTest {

	private final StringToStringConverter converter = new StringToStringConverter();

	@Test
	public void testConvert() {
		final String value = "123";
		String result = converter.convert(value);

		assertEquals("Should convert to string with expected value.", value, result);
	}
}
