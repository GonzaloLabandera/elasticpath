/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.commons.util;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;

/**
 * Defines methods to handle email address in inline format or list.
 * Regarding the email addresses could be formed as either inline or list.
 */
public final class EmailAddressUtil {

	private static final char SEPARATOR = ',';
	private static final Splitter DEFAULT_EMAIL_ADDRESSES_SPLITTER = Splitter.on(SEPARATOR).trimResults().omitEmptyStrings();
	private static final Joiner DEFAULT_EMAIL_ADDRESSES_JOINER = Joiner.on(SEPARATOR).skipNulls();

	/**
	 * Default constructor.
	 */
	private EmailAddressUtil() {
		// do nothing.
	}

	/**
	 * Splits the inline email address into list of email addresses.
	 *
	 * @param inlineEmailAddresses the inline email addresses with comma separator.
	 * @return the list of email addresses.
	 */
	public static List<String> split(final String inlineEmailAddresses) {
		if (StringUtils.isEmpty(inlineEmailAddresses)) {
			return Collections.emptyList();
		}
		return DEFAULT_EMAIL_ADDRESSES_SPLITTER.splitToList(inlineEmailAddresses);
	}

	/**
	 * Joins the list of email addresses to inline format with separator.
	 *
	 * @param emailAddresses the list of email addresses.
	 * @return inline format of email addresses.
	 */
	public static String inline(final List<String> emailAddresses) {
		if (emailAddresses == null) {
			return StringUtils.EMPTY;
		}
		return DEFAULT_EMAIL_ADDRESSES_JOINER.join(emailAddresses);
	}

}
