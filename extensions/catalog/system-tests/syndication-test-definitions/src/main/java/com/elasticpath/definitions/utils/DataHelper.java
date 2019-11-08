/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.definitions.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * Helper class for handling dates formats in the tests.
 */
public final class DataHelper {

	/**
	 * Pattern which corresponds to date format used in CM UI.
	 */
	public static final Pattern CM_UI_DATE_PATTERN = Pattern.compile("[A-Za-z]{3}\\s[0-9]{1,2},\\s[0-9]{4}\\s[0-9]+:[0-9]+\\s[A-Z]{2}");
	private static final Logger LOG = Logger.getLogger(DataHelper.class);
	private static final String MESSAGE = "Could not parse date ";
	/**
	 * Date format used in CM UI.
	 */
	public static final String CM_UI_DATE_FORMAT = "MMM d, y h:mm a";
	private static final String GMT_ZONE = "GMT";

	/**
	 * Date format which is used in DB tables for the dates.
	 */
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("YYYY-M-d HH:mm:ss", Locale.ENGLISH);

	/**
	 * Date format which is used in DB tables for the dates.
	 */
	public static final String INPUT_DATE_FORMAT = "MMM d, y h:mm a";

	/**
	 * Date format which is used in Syndication API URLs for the dates.
	 */
	public static final SimpleDateFormat URL_DATE_FORMAT = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH);
	private static final long ONE_MINUTE_IN_MILLIS = 60000;

	private DataHelper() {
	}

	/**
	 * @param minutes amount of minutes which should be deducted or added from\to current date time.
	 * @return current date time with provided offset in minutes.
	 */
	public static Date getDateWithOffset(final String minutes) {
		Date date;
		if (minutes.startsWith("-")) {
			int min = Integer.parseInt(minutes.substring(1));
			date = new Date(Calendar.getInstance().getTimeInMillis() - (min * ONE_MINUTE_IN_MILLIS));
		} else {
			int min = Integer.parseInt(minutes);
			date = new Date(Calendar.getInstance().getTimeInMillis() + (min * ONE_MINUTE_IN_MILLIS));
		}
		return date;
	}

	/**
	 * Convert Date to ZonedDateTime object.
	 *
	 * @param date date.
	 * @return converted ZonedDateTime object.
	 */
	public static ZonedDateTime convertToZonedDateTime(final Date date) {
		final ZoneId defaultZoneId = ZoneId.of(GMT_ZONE);
		final Instant instant = date.toInstant();
		return instant.atZone(defaultZoneId);
	}

	/**
	 * Convert String to ZonedDateTime object.
	 *
	 * @param date date.
	 * @return ZonedDateTime object.
	 */
	public static ZonedDateTime convertStringToZonedDateTime(final String date) {
		return ZonedDateTime.parse(
				date,
				DateTimeFormatter.ofPattern(INPUT_DATE_FORMAT, Locale.ENGLISH).withZone(
						ZoneId.of(GMT_ZONE)
				));
	}

	/**
	 * Convert date from given format to "yyyy-MM-dd" format.
	 *
	 * @param inputDate   date in "MMM d, y h:mm a" format.
	 * @param datePattern pattern by which date will converts.
	 * @return date in "yyyy-MM-dd" format.
	 */
	public static String getFormatDate(final String inputDate, final String datePattern) {
		Date outputDate = null;
		DateFormat originalFormat = new SimpleDateFormat(datePattern, Locale.ENGLISH);
		DateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
		targetFormat.setTimeZone(TimeZone.getTimeZone(GMT_ZONE));
		try {
			outputDate = originalFormat.parse(inputDate);
		} catch (ParseException e) {
			LOG.error(MESSAGE, e);
		}
		return targetFormat.format(outputDate);
	}

	/**
	 * Converts provided date to projection date format.
	 *
	 * @param date date which should be converted.
	 * @return date in projection date format.
	 * @throws ParseException when trying to parse provided date.
	 */
	public static String getProjectionDate(final String date) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat(CM_UI_DATE_FORMAT, Locale.ENGLISH);
		dateFormat.setTimeZone(TimeZone.getTimeZone(GMT_ZONE));
		Date disableCategoryDate = dateFormat.parse(date);
		dateFormat.applyPattern(CM_UI_DATE_FORMAT);
		return disableCategoryDate == null ? null : getFormatDate(dateFormat.format(disableCategoryDate), CM_UI_DATE_FORMAT);
	}

	/**
	 * Returns CM Ui date pattern with ; as delimiter.
	 *
	 * @return CM Ui date pattern with ; as delimiter.
	 */
	public static Pattern getMultiCmUiDatePattern() {
		return Pattern.compile("(" + CM_UI_DATE_PATTERN.toString() + ";*)+");
	}
}
