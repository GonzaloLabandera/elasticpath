/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.commons.util.impl;

import static java.util.Locale.CANADA;
import static java.util.Locale.CANADA_FRENCH;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.FRENCH;
import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;

/**
 * Tests the {@link LocaleUtils} class.
 */
public class LocaleUtilsTest {

	/**
	 * Tests the broadenLocale method.
	 */
	@Test
	public void testBroadenLocale() {
		assertEquals("Trivial Case", ENGLISH, LocaleUtils.broadenLocale(ENGLISH));
		assertEquals("Remove Country", ENGLISH, LocaleUtils.broadenLocale(CANADA));
		assertEquals("Remove Country", FRENCH, LocaleUtils.broadenLocale(CANADA_FRENCH));
		assertEquals("Remove Variant", new Locale("es", "ES"), LocaleUtils.broadenLocale(new Locale("es", "ES", "Tradition_WIN")));
	}

	/**
	 * Tests that broadenLocale hates nulls.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testBroadenLocaleNullCase() {
		LocaleUtils.broadenLocale(null);
	}

	/**
	 * Tests the getLanguageCode method.
	 */
	@Test
	public void testGetLanguageCode() {
		assertEquals("en", LocaleUtils.getCommerceLocalCode(ENGLISH));
		assertEquals("en_CA", LocaleUtils.getCommerceLocalCode(CANADA));
		assertEquals("fr_CA", LocaleUtils.getCommerceLocalCode(CANADA_FRENCH));
	}

	/**
	 * Tests that getLanguageCode hates nulls.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testGetLanguageCodeNullCase() {
		LocaleUtils.getCommerceLocalCode(null);
	}

}
