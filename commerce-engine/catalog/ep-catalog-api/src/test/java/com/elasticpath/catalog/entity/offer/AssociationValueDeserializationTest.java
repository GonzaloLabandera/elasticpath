/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.offer;

import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.ZonedDateTime;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link AssociationValue}.
 */
public class AssociationValueDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoAssociationValue() throws IOException {
		final String dateTime = "2019-03-15T09:57:11.234+03:00";
		final String jsonString =
				"{\"offer\":\"offer\","
						+ "\"enableDateTime\":\"2019-03-15T09:57:11.234+03:00\","
						+ "\"disableDateTime\":\"2019-03-15T09:57:11.234+03:00\"}]}";
		final AssociationValue associationValue = getObjectMapper().readValue(jsonString, AssociationValue.class);
		assertThat(associationValue.getOffer()).isEqualTo("offer");
		assertThat(associationValue.getEnableDateTime()).isEqualTo(ZonedDateTime.parse(dateTime, ISO_ZONED_DATE_TIME));
		assertThat(associationValue.getDisableDateTime()).isEqualTo(ZonedDateTime.parse(dateTime, ISO_ZONED_DATE_TIME));
	}
}
