/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.insights.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Security utility for masking sensitive values.
 */
public final class SecurityMaskingUtil {

	private static final String MASKED_TEXT = "*MASKED*";
	private static final int KEYWORD_MATCHING_VALUE_GROUP = 1;

	/**
	 * Private constructor to prevent instantiation.
	 */
	private SecurityMaskingUtil() {
		// No op
	}

	/**
	 * Utility method for masking jvm argument values.
	 *
	 *
	 * @param keywords list of sensitive keywords in jvm argument
	 * @param propertyStrings the jvm property strings to be masked
	 *
	 * @return a new list of property Strings, with sensitive values masked
	 */
	public static List<String> maskValuesWithMatchingKeys(final List<String> keywords, final List<String> propertyStrings) {
		String patternRegex = "(?:" + String.join("|", keywords) + ").*=(.*)";
		Pattern pattern = Pattern.compile(patternRegex, Pattern.CASE_INSENSITIVE);
		List<String> maskedJvmArguments = new ArrayList<>(propertyStrings.size());
		propertyStrings.forEach(jvmArgument -> {
			Matcher matcher = pattern.matcher(jvmArgument);
			if (matcher.find()) {
				maskedJvmArguments.add(new StringBuilder(jvmArgument)
						.replace(
								matcher.start(KEYWORD_MATCHING_VALUE_GROUP),
								matcher.end(KEYWORD_MATCHING_VALUE_GROUP),
								MASKED_TEXT)
						.toString());
			} else {
				maskedJvmArguments.add(jvmArgument);
			}
		});

		return maskedJvmArguments;
	}
}
