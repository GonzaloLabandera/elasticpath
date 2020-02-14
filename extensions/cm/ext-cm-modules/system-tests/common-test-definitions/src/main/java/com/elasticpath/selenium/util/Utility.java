package com.elasticpath.selenium.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import org.apache.commons.text.RandomStringGenerator;

import com.elasticpath.selenium.framework.util.PropertyManager;

/**
 * Utility class for commonly used method.
 */
public final class Utility {
	private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(Utility.class);
	private static final String DATE_FORMAT = "MMM d, yyyy h:mm a";
	private static final String SIMPLE_DATE_FORMAT = "MMM d, yyyy";

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

	/**
	 * Converts string to Date with the ep dateTime format with hh:mm:ss included.
	 *
	 * @param dateTimeString String
	 * @return Date
	 */
	public static Date convertToDateTime(final String dateTimeString) {
		String dateFormat = PropertyManager.getInstance().getProperty("ep.dateTimeFormat");
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.ENGLISH);

		Date actualDate = null;
		try {
			actualDate = simpleDateFormat.parse(dateTimeString);
		} catch (ParseException e) {
			LOGGER.debug(e.getMessage());
		}
		return actualDate;
	}

	/**
	 * Converts string to Date.
	 *
	 * @param dateString String
	 * @return Date
	 */
	public static Date convertToDate(final String dateString) {
		String dateFormat = PropertyManager.getInstance().getProperty("ep.dateFormat");
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.ENGLISH);

		Date actualDate = null;
		try {
			actualDate = simpleDateFormat.parse(dateString);
		} catch (ParseException e) {
			LOGGER.debug(e.getMessage());
		}
		return actualDate;
	}

	/**
	 * Adds provided amount of days to current date and returns date as a string.
	 *
	 * @param datePlus the number of days to add.
	 * @return current date plus provided amount of days.
	 */
	public static String getDateTimeWithPlus(final Integer datePlus) {
		final Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, datePlus);
		final Date currentDate = calendar.getTime();
		final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, y h:mm a", Locale.ENGLISH);
		return dateFormat.format(currentDate);
	}

	/**
	 * Get current date plus some days.
	 *
	 * @param days count days.
	 */
	public static Date getDateTimePlusDays(final Integer days) {
		final Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, days);
		return calendar.getTime();
	}

	/**
	 * Convert date to format.
	 *
	 * @param date date.
	 */
	public static String convertToSimpleDateFormat(final Date date) {
		final SimpleDateFormat dbDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT, Locale.ENGLISH);
		return dbDateFormat.format(date);
	}

	/**
	 * Convert date to format.
	 *
	 * @param date date.
	 */
	public static String convertToDateFormat(final Date date) {
		final SimpleDateFormat dbDateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
		return dbDateFormat.format(date);
	}
}
