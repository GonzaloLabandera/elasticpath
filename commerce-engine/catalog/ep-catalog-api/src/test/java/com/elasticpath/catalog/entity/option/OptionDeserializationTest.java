/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.option;

import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.ZonedDateTime;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link Option}.
 */
public class OptionDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoOption() throws IOException {
		final String jsonString =
				"{\"code\":\"231\","
						+ "\"store\":\"storeName\","
						+ "\"translations\":[{\"optionValues\":[{\"value\":\"value\",\"displayValue\":\"displayValue\"}]}],"
						+ "\"modifiedDateTime\":\"2019-03-15T09:57:11.234+03:00\","
						+ "\"deleted\":false}";
		final Option option = getObjectMapper().readValue(jsonString, Option.class);
		assertThat(option.getIdentity().getCode()).isEqualTo("231");
		assertThat(option.getIdentity().getStore()).isEqualTo("storeName");
		assertThat(option.getTranslations().size()).isEqualTo(1);
		assertThat(option.getModifiedDateTime()).isEqualTo(ZonedDateTime.parse("2019-03-15T09:57:11.234+03:00", ISO_ZONED_DATE_TIME));
		assertThat(option.isDeleted()).isEqualTo(false);
	}
}
