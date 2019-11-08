/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.option;

import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Collections;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link Option}.
 */
public class OptionSerializationTest extends BaseSetUp {

	@Test
	public void testOptionProjectionShouldBeEqualsJsonStringWithCorrectOrderOfFields() throws IOException {
		final ZonedDateTime dateTime = ZonedDateTime.parse("2019-03-15T09:57:11.234+03:00", ISO_ZONED_DATE_TIME);
		final String jsonString = "{\"identity\":{\"type\":\"option\",\"code\":\"code1\",\"store\":\"store1\"},"
				+ "\"modifiedDateTime\":\"2019-03-15T09:57:11.234+03:00\",\"deleted\":false,\"translations\":[]}";
		final Option option = new Option("code1", "store1", Collections.emptyList(), dateTime, false);
		final String newJson = getObjectMapper().writeValueAsString(option);
		assertThat(newJson).isEqualTo(jsonString);
	}
}
