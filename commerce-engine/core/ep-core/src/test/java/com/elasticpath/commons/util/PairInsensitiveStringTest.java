/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.commons.util;

import java.util.Locale;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

/**
 * Test that the {@link PairInsensitiveString} class works as expected.
 */
public class PairInsensitiveStringTest {
	
	/**
	 * Test that equals and hashCode works in case insensitive mode when pairs keep 
	 * string in upper and lower cases.
	 */
	@Test
	public void testEqualsHashcode() {
		final Locale locale = new Locale("en");
		
		final PairInsensitiveString firstPair = new PairInsensitiveString("elastic", "path", locale);
		final PairInsensitiveString secondPair = new PairInsensitiveString("Elastic", "Path", locale);
		final PairInsensitiveString thrirdPair = new PairInsensitiveString("Flexible", "Path", locale);

		new EqualsTester()
			.addEqualityGroup(firstPair, secondPair)
			.addEqualityGroup(thrirdPair)
			.testEquals();
	}
	
	/**
	 * Test that equals and hashCode works as expected with null values.
	 */
	@Test
	public void testEqualsHashcodeWithNullValues() {
		final Locale locale = new Locale("en");
		
		final PairInsensitiveString thrirdPair = new PairInsensitiveString(null, null, locale);
		
		PairInsensitiveString firstPair = new PairInsensitiveString("elastic", null, locale);
		PairInsensitiveString secondPair = new PairInsensitiveString("Elastic", null, locale);

		new EqualsTester()
			.addEqualityGroup(firstPair, secondPair)
			.addEqualityGroup(thrirdPair)
			.testEquals();

		firstPair = new PairInsensitiveString(null, "path", locale);
		secondPair = new PairInsensitiveString(null, "PatH", locale);

		new EqualsTester()
			.addEqualityGroup(firstPair, secondPair)
			.testEquals();
	}
	

}
