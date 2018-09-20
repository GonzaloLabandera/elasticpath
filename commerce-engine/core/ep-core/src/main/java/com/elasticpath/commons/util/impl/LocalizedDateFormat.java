/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.commons.util.impl;

import java.util.Locale;
import java.util.Objects;

/**
 * Immutable POJO which contains a locale specific date formatting spec - Locale + Format String.
 */
public class LocalizedDateFormat {
	private final String dateFormat;
	private final Locale locale;

	/**
	 * Creates a localized date format with the given pattern and locale.
	 *
	 * @param dateFormat the SimpleDateFormat style format string
	 * @param locale the locale
	 */
	public LocalizedDateFormat(final String dateFormat, final Locale locale) {
		this.dateFormat = dateFormat;
		this.locale = locale;
	}

	public String getDateFormatPattern() {
		return dateFormat;
	}

	public Locale getLocale() {
		return locale;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof LocalizedDateFormat)) {
			return false;
		}

		LocalizedDateFormat that = (LocalizedDateFormat) obj;

		return Objects.equals(getDateFormatPattern(), that.getDateFormatPattern())
			&& Objects.equals(getLocale(), that.getLocale());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getDateFormatPattern(), getLocale());
	}

	@Override
	public String toString() {
		return "LocalizedDateFormat{"
				+ "dateFormat='" + dateFormat + '\''
				+ ", locale=" + locale + '}';
	}
}
