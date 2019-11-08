/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.category;

import static com.elasticpath.catalog.entity.constants.ProjectionSchemaPath.CATEGORY_SCHEMA_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import com.elasticpath.catalog.entity.AvailabilityRules;
import com.elasticpath.catalog.entity.BaseSetUp;
import com.elasticpath.catalog.entity.ProjectionProperties;
import com.elasticpath.catalog.entity.translation.CategoryTranslation;
import com.elasticpath.catalog.entity.translation.DetailsTranslation;
import com.elasticpath.catalog.entity.translation.Translation;
import com.elasticpath.catalog.validator.impl.ProjectionValidator;

/**
 * Tests {@link Category}.
 */
public class CategoryTest extends BaseSetUp {

	private static final String PROJECTION_CODE = "code";
	public static final String STORE_CODE = "store";

	/**
	 * Test for ensure that Category json corresponds to schema.
	 *
	 * @throws JsonProcessingException when Category cannot serialize to JSON.
	 */
	@Test
	public void testThatCategoryProjectionJsonCorrespondsToSchema() throws JsonProcessingException {
		final ProjectionProperties projectionProperties = new ProjectionProperties("code", "store", ZonedDateTime.now(), false);
		final List<String> childList = new ArrayList<>();
		final List<CategoryTranslation> categoryTranslations = new ArrayList<>();
		final List<DetailsTranslation> categoryDetailsTranslations = new ArrayList<>();
		final List<String> displayValues = new ArrayList<>();
		displayValues.add("<image1PathDisplayValues>");
		displayValues.add("<image2PathDisplayValues>");
		final List<Object> values = new ArrayList<>();
		values.add("<image1PathValues>");
		values.add("<image2PathValues>");
		categoryDetailsTranslations.add(new DetailsTranslation("Image Gallery", "CATEGORY_IMAGES", displayValues, values));
		categoryTranslations.add(new CategoryTranslation(new Translation("en", "<categoryName>"), categoryDetailsTranslations));
		childList.add("<child1Code>");
		childList.add("<child2Code>");
		final Category category = new Category(new CategoryProperties(projectionProperties, Collections.emptyList()), new Object(),
				categoryTranslations, childList, new AvailabilityRules(null, null), Collections.emptyList(), null);

		final String categoryJson = getObjectMapper().writeValueAsString(category);

		assertThatCode(() -> new ProjectionValidator(CATEGORY_SCHEMA_JSON)
				.validate(categoryJson)).doesNotThrowAnyException();
	}

	@Test
	public void ensureThatCategoryJsonContainsEmptyPath() throws JsonProcessingException {
		final Category category = new Category(new CategoryProperties(new ProjectionProperties(PROJECTION_CODE, STORE_CODE, null, false), null),
				new Object(), null, Collections.emptyList(), null, Collections.emptyList(), null);

		final String categoryJson = getObjectMapper().writeValueAsString(category);

		assertThat(categoryJson).contains("path");
	}

	@Test
	public void ensureThatCategoryJsonDoesNotContainEmptyParent() throws JsonProcessingException {
		final Category category = new Category(new CategoryProperties(
				new ProjectionProperties(PROJECTION_CODE, STORE_CODE, null, false), null), new Object(), null, Collections.emptyList(), null,
				Collections.emptyList(), null);

		final String categoryJson = getObjectMapper().writeValueAsString(category);

		assertThat(categoryJson).doesNotContain("parent");
	}

	@Test
	public void ensureThatCategoryJsonHasProperty() throws JsonProcessingException {
		final Category category = new Category(new CategoryProperties(
				new ProjectionProperties(PROJECTION_CODE, STORE_CODE, null, false), Collections.emptyList()), new Object(), null,
				Collections.emptyList(), null, Collections.emptyList(), null);

		final String categoryJson = getObjectMapper().writeValueAsString(category);

		assertThat(categoryJson).contains("properties");
	}

	@Test
	public void ensureThatCategoryJsonHasAvailabilityRules() throws JsonProcessingException {
		final AvailabilityRules availabilityRules = new AvailabilityRules(ZonedDateTime.now(), ZonedDateTime.now());
		final Category category = new Category(new CategoryProperties(
				new ProjectionProperties(PROJECTION_CODE, STORE_CODE, null, false), Collections.emptyList()), new Object(), null,
				Collections.emptyList(), availabilityRules, Collections.emptyList(), null);

		final String categoryJson = getObjectMapper().writeValueAsString(category);

		assertThat(categoryJson).contains("availabilityRules");
	}

	@Test
	public void ensureThatCategoryJsonHasEmptyAvailabilityRules() throws JsonProcessingException {
		final AvailabilityRules availabilityRules = new AvailabilityRules(null, null);
		final Category category = new Category(new CategoryProperties(
				new ProjectionProperties(PROJECTION_CODE, STORE_CODE, null, false), Collections.emptyList()), new Object(), null,
				Collections.emptyList(),
				availabilityRules, Collections.emptyList(), null);

		final String categoryJson = getObjectMapper().writeValueAsString(category);

		assertThat(categoryJson).contains("\"availabilityRules\":{}");
	}
}
