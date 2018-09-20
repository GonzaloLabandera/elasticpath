/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.cmclient.core.formatting;

import com.elasticpath.cmclient.core.util.DateTimeUtilFactory;
import org.apache.commons.lang.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.Locale;

/**
 * Provides an uniform way to display date & date time all across cm client.
 */
public class UIDateTimeUtilImpl implements UIDateTimeUtil {

	private DateFormat dateTimeFormatter;

	private DateFormat dateFormatter;

	@Override
	public String formatAsDate(final Date date) {
		return formatAsDate(date, StringUtils.EMPTY);
	}

	@Override
	public String formatAsDate(final Date date, final String nullDateRepresentation) {
		return formatDateTime(date, getDefaultDateFormatter(), nullDateRepresentation);
	}

	@Override
	public String formatAsDateTime(final Date date) {
		return formatAsDateTime(date, StringUtils.EMPTY);
	}

	@Override
	public String formatAsDateTime(final Date date, final String nullDateRepresentation) {
		return formatDateTime(date, getDefaultDateTimeFormatter(), nullDateRepresentation);
	}

	@Override
	public Date getDateWithFormat(final Date date) {
		return new FormattedDate(date);
	}

	@Override
	public Date parseDate(final String dateString) throws ParseException {
		return getDefaultDateFormatter().parse(dateString);
	}

	@Override
	public Date parseDate(final String dateString, final ParsePosition parsePosition) {
		return getDefaultDateFormatter().parse(dateString, parsePosition);
	}

	@Override
	public Date parseDateTime(final String dateString, final ParsePosition parsePosition) {
		return getDefaultDateTimeFormatter().parse(dateString, parsePosition);
	}

	@Override
	public Date parseDateTime(final String dateString) throws ParseException {
		return getDefaultDateTimeFormatter().parse(dateString);
	}

	@Override
	public DateFormat[] getFormatters() {
		return new DateFormat[]{getDefaultDateFormatter(), getDefaultDateTimeFormatter()};
	}

	/**
	 * Formats a date to the given format and returns nullDateRepresentation in case of null date.
	 *
	 * @param object                 the object to be formatted
	 * @param dateFormat             the date format
	 * @param nullDateRepresentation the null date representation
	 * @return the string formatted date
	 */
	protected static String formatDateTime(final Object object, final DateFormat dateFormat, final String nullDateRepresentation) {
		if (object == null) {
			return nullDateRepresentation;
		}
		return dateFormat.format(object);
	}

	/**
	 * Returns the singleton instance of a Date & Time formatter.
	 *
	 * @return DateFormat
	 */
	protected DateFormat getDefaultDateTimeFormatter() {
		if (dateTimeFormatter == null) {
			dateTimeFormatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, getDefaultLocale());
		}
		return dateTimeFormatter;
	}

	/**
	 * Returns the session instance of a Date formatter.
	 *
	 * @return DateFormat
	 */
	protected DateFormat getDefaultDateFormatter() {
		if (dateFormatter == null) {
			dateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM, getDefaultLocale());
		}
		return dateFormatter;
	}

	/**
	 * Retrieves the current value of the default locale for this instance of the JVM.
	 *
	 * @return the current value of the default locale for this instance of the JVM.
	 */
	protected Locale getDefaultLocale() {
		return Locale.getDefault();
	}

	/**
	 * Helper class for maintaining a Date object to be passed to the table viewer, but use our own formatting to display.
	 */
	class FormattedDate extends Date {
		/**
		 * Constructor.
		 *
		 * @param date the date
		 */
		FormattedDate(final Date date) {
			super(date.getTime());
		}

		@Override
		public String toString() {
			return DateTimeUtilFactory.getDateUtil().formatAsDate(this);
		}
	}

}
