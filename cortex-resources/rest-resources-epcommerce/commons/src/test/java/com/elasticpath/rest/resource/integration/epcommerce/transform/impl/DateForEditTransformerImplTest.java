/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.integration.epcommerce.transform.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.format.DateTimeParseException;
import java.util.Date;

import org.junit.Test;

import com.elasticpath.domain.attribute.AttributeType;
import com.elasticpath.rest.resource.integration.epcommerce.transform.DateForEditTransformer;

/**
 * Test class for {@link DateForEditTransformerImpl}.
 */
public class DateForEditTransformerImplTest {

	private static final String DATE = "1993-02-12";
	private static final String DATE_WITH_OFFSET = "1993-02-12T12:00:00+02:00";
	private static final String UTC_DATE = "1993-02-12T12:00:00Z";
	private final DateForEditTransformer dateTransformer = new DateForEditTransformerImpl();

	/**
	 * Test invalid transform to domain.
	 */
	@Test(expected = DateTimeParseException.class)
	public void testTransformToDomainInvalidDateTime() {

		dateTransformer.transformToDomain(AttributeType.DATETIME, DATE);
	}

	/**
	 * Test invalid transform to domain.
	 */
	@Test(expected = DateTimeParseException.class)
	public void testTransformToDomainInvalidDate() {

		dateTransformer.transformToDomain(AttributeType.DATE, DATE_WITH_OFFSET);
	}

	/**
	 * Test transform to domain.
	 */
	@Test
	public void testTransformToDomain() {
		dateTransformer.transformToDomain(AttributeType.DATETIME, DATE_WITH_OFFSET);
		dateTransformer.transformToDomain(AttributeType.DATETIME, UTC_DATE);
		dateTransformer.transformToDomain(AttributeType.DATE, DATE);
	}

	/**
	 * Test transform to entity.
	 */
	@Test
	public void testTransformToString() {
		Date date = dateTransformer.transformToDomain(AttributeType.DATETIME, UTC_DATE).get();

		String dateString = dateTransformer.transformToString(AttributeType.DATETIME, date).get();

		assertThat(dateString.equals(UTC_DATE))
				.as("Display value does not match expected formatted date.")
				.isTrue();

		date = dateTransformer.transformToDomain(AttributeType.DATE, DATE).get();

		dateString = dateTransformer.transformToString(AttributeType.DATE, date).get();

		assertThat(dateString.equals(DATE))
				.as("Display value does not match expected formatted date.")
				.isTrue();
	}

}
