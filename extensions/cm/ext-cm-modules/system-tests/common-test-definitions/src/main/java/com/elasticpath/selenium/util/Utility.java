package com.elasticpath.selenium.util;

import java.util.UUID;

import org.apache.commons.text.RandomStringGenerator;

/**
 * Utility class for commonly used method.
 */
public final class Utility {

	/**
	 * Constructor.
	 */
	private Utility() {
	}

	/**
	 * Returns a random UUID of length UUID_END_INDEX.
	 *
	 * @return a random UUID
	 */
	public static String getRandomUUID() {
		return UUID.randomUUID().toString().substring(0, Constants.UUID_END_INDEX);
	}

	/**
	 * Generates a random string of specified length consisting of only lower case english alphabets a-z.
	 *
	 * @param length the length of random string to be generated
	 * @return a random UUID
	 */
	public static String generateRandomAlphabets(final int length) {
		final RandomStringGenerator generator = new RandomStringGenerator.Builder()
				.withinRange('a', 'z').build();
		return generator.generate(length);
	}
}
