/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.commons.converter.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test class for {@link StringToBooleanConverter}.
 */
public class StringToBooleanConverterTest {

	private final StringToBooleanConverter stringToBooleanConverter = new StringToBooleanConverter();

	@Test
	public void testConvertTrueString() {
		Boolean result = stringToBooleanConverter.convert("true");
		assertTrue("Result should be true", result);
	}

	@Test
	public void testConvertFalseString() {
		Boolean result = stringToBooleanConverter.convert("false");
		assertFalse("Result should be false", result);
	}

	@Test
	public void testConvertAnyString() {
		Boolean result = stringToBooleanConverter.convert("abc");
		assertFalse("Result should be false", result);
	}
}
