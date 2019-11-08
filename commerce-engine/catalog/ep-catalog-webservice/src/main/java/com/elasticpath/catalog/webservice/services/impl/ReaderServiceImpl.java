/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.webservice.services.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.elasticpath.catalog.webservice.exception.InvalidRequestParameterException;
import com.elasticpath.service.misc.TimeService;

/**
 * An abstract service which contains common validation.
 */
public class ReaderServiceImpl {
	private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ssXXX";

	private final TimeService timeService;

	/**
	 * Constructor.
	 *
	 * @param timeService is time service.
	 */
	public ReaderServiceImpl(final TimeService timeService) {
		this.timeService = timeService;
	}

	/**
	 * Validates modified since value.
	 *
	 * @param modifiedSince modifiedSince value.
	 * @param modifiedSinceOffset modifiedSinceOffset value.
	 */
	protected void validateModifiedSince(final String modifiedSince, final String modifiedSinceOffset) {

		if (StringUtils.isEmpty(modifiedSince) && StringUtils.isNotEmpty(modifiedSinceOffset)) {
			throw new InvalidRequestParameterException("ModifiedSinceOffset parameter cannot be specified if modifiedSince not present");
		}

		if (StringUtils.isNotEmpty(modifiedSinceOffset)
				&& (!StringUtils.isNumeric(modifiedSinceOffset) || Integer.parseInt(modifiedSinceOffset) < 0)) {
			throw new InvalidRequestParameterException("ModifiedSinceOffset parameter must be >= zero");
		}
	}

	/**
	 * Converts string to date.
	 *
	 * @param modifiedSince source string.
	 * @return date.
	 */
	protected Date convertDate(final String modifiedSince) {
		if (StringUtils.isEmpty(modifiedSince)) {
			return null;
		}

		try {
			final SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN, Locale.ENGLISH);

			final Date date = format.parse(modifiedSince);

			if (date.after(timeService.getCurrentTime())) {
				throw new InvalidRequestParameterException("ModifiedSince date must be in the past");
			}

			return date;
		} catch (ParseException e) {
			throw new InvalidRequestParameterException("Invalid Date format", e);
		}
	}

	/**
	 * Converts string to long value.
	 *
	 * @param modifiedSinceOffset source string.
	 * @return long value.
	 */
	protected Long convertSinceOffset(final String modifiedSinceOffset) {
		if (StringUtils.isEmpty(modifiedSinceOffset)) {
			return null;
		}

		return Long.valueOf(modifiedSinceOffset);
	}

	/**
	 * Validates limit. If limit is invalid throws {@link InvalidRequestParameterException}.
	 *
	 * @param limit source limit.
	 */
	protected void validateLimit(final String limit) {
		if ((limit != null) && (!StringUtils.isNumeric(limit) || Integer.parseInt(limit) <= 0)) {
			throw new InvalidRequestParameterException();
		}
	}
}
