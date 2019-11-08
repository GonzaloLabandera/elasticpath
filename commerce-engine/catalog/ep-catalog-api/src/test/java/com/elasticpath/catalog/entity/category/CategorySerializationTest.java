/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.category;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Collections;

import org.junit.Test;

import com.elasticpath.catalog.entity.AvailabilityRules;
import com.elasticpath.catalog.entity.BaseSetUp;
import com.elasticpath.catalog.entity.ProjectionProperties;

/**
 * Tests {@link Category}.
 */
public class CategorySerializationTest extends BaseSetUp {

	@Test
	public void testCategoryProjectionShouldBeEqualsJsonStringWithCorrectOrderOfFields() throws IOException {
		final String jsonString = "{\"identity\":{\"type\":\"category\",\"code\":\"code\",\"store\":\"store\"},\"deleted\":false,\"properties\":[],"
				+ "\"availabilityRules\":{},\"path\":[],\"parent\":\"\",\"children\":[],\"translations\":[]}";
		final Category category = new Category(new CategoryProperties(
				new ProjectionProperties("code", "store", null, false),
				Collections.emptyList()), null, Collections.emptyList(), Collections.emptyList(),
				new AvailabilityRules(null, null), Collections.emptyList(), "");
		final String newJson = getObjectMapper().writeValueAsString(category);
		assertThat(newJson).isEqualTo(jsonString);
	}
}
