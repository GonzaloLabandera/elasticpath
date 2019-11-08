/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity.fieldmetadata;

import static com.elasticpath.catalog.entity.constants.ProjectionSchemaPath.FIELD_METADATA_SCHEMA_JSON;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;
import com.elasticpath.catalog.entity.Field;
import com.elasticpath.catalog.entity.translation.FieldMetadataTranslation;
import com.elasticpath.catalog.validator.impl.ProjectionValidator;


/**
 * Tests {@link FieldMetadata}.
 */
public class FieldMetadataTest extends BaseSetUp {

	public static final String NAME = "NAME";
	public static final String STORE_CODE_ONE = "123";
	private static final int MAX_SIZE = 20;
	private static final Field FIELD_1 = new Field("NAME",
			"Nom",
			"ShortText",
			true,
			MAX_SIZE,
			Collections.emptyList());

	private static final Field FIELD_2 = new Field("NUMBER",
			"Numero",
			"Integer",
			true,
			MAX_SIZE,
			Collections.emptyList());

	private static final List<Field> FIELDS = Stream.of(FIELD_1, FIELD_2).collect(Collectors.toList());

	private static final List<FieldMetadataTranslation> TRANSLATIONS =
			Collections.singletonList(new FieldMetadataTranslation("fr", "Maillot d'Equipe", FIELDS));

	/**
	 * Test for ensure that FieldMetadata json corresponds to schema.
	 *
	 * @throws JsonProcessingException if wrong json format is a directory.
	 */
	@Test
	public void testThatFieldMetadataProjectionJsonCorrespondsToSchema() throws JsonProcessingException {
		FieldMetadata fieldMetadataProjection = new FieldMetadata(NAME, STORE_CODE_ONE, TRANSLATIONS, ZonedDateTime.now(), false);

		String json = getObjectMapper().writeValueAsString(fieldMetadataProjection);

		assertThatCode(() -> new ProjectionValidator(FIELD_METADATA_SCHEMA_JSON)
				.validate(json)).doesNotThrowAnyException();
	}
}
