/**
 * Copyright (c) Elastic Path Software Inc., 2011
 */
package com.elasticpath.commons.util;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

/**
 * Test that the Pair class works as expected.
 */
public class PairTest {

	/**
	 * Test that equals and hashCode works as expected - based on the equality of
	 * first and second.
	 */
	@Test
	public void testEqualsHashcode() {
		final Pair<Integer, String> pair1 = new Pair<>(1, "fred");
		final Pair<Integer, String> pair2 = new Pair<>(1, "fred");
		final Pair<Integer, String> pairDifferentFirst = new Pair<>(1, "geoff");
		final Pair<Integer, String> pairDifferentSecond = new Pair<>(2, "fred");

		final Pair<Integer, char[]> pairDifferentType = new Pair<>(1, new char[]{'f', 'r', 'e', 'd'});

		new EqualsTester()
			.addEqualityGroup(pair1, pair2)
			.addEqualityGroup(pairDifferentFirst)
			.addEqualityGroup(pairDifferentSecond)
			.addEqualityGroup(pairDifferentType)
			.testEquals();

	}

}
