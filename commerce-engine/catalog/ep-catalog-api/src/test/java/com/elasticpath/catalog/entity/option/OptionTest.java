/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity.option;

import static com.elasticpath.catalog.entity.constants.ProjectionSchemaPath.OPTION_SCHEMA_JSON;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;
import com.elasticpath.catalog.entity.TranslatedName;
import com.elasticpath.catalog.entity.translation.OptionTranslation;
import com.elasticpath.catalog.validator.impl.ProjectionValidator;

/**
 * Tests {@link Option}.
 */
public class OptionTest extends BaseSetUp {

	private static final String COLOUR_EN = "Colour";
	private static final String COLOUR_FR = "Couleur";
	private static final TranslatedName RED_TRANSLATION_EN = new TranslatedName("RED", "Red");
	private static final TranslatedName RED_TRANSLATION_FR = new TranslatedName("RED", "Rouge");
	private static final TranslatedName BLUE_TRANSLATION_EN = new TranslatedName("BLUE", "Blue");
	private static final TranslatedName BLUE_TRANSLATION_FR = new TranslatedName("BLUE", "Bleu");
	private static final String LANGUAGE_EN = "en";
	private static final String LANGUAGE_FR = "fr";
	private static final String COLOUR_PROJECTION = "COLOUR";
	private static final String STORE_CODE = "Store 123";

	/**
	 * Test for ensure that Option json corresponds to schema.
	 *
	 * @throws JsonProcessingException when Option cannot serialize to JSON.
	 */
	@Test
	public void testThatSkuOptionProjectionJsonCorrespondsToSchema() throws JsonProcessingException {
		final Option skuOptionProjection = buildOption();

		String json = getObjectMapper().writeValueAsString(skuOptionProjection);

		assertThatCode(() -> new ProjectionValidator(OPTION_SCHEMA_JSON)
				.validate(json)).doesNotThrowAnyException();
	}

	private Option buildOption() {
		final List<TranslatedName> optionValuesRedEn = new ArrayList<>();
		optionValuesRedEn.add(RED_TRANSLATION_EN);
		optionValuesRedEn.add(BLUE_TRANSLATION_EN);
		final List<TranslatedName> optionValuesRedFr = new ArrayList<>();
		optionValuesRedFr.add(RED_TRANSLATION_FR);
		optionValuesRedFr.add(BLUE_TRANSLATION_FR);
		final List<OptionTranslation> translations = new ArrayList<>();
		translations.add(new OptionTranslation(LANGUAGE_EN, COLOUR_EN, optionValuesRedEn));
		translations.add(new OptionTranslation(LANGUAGE_FR, COLOUR_FR, optionValuesRedFr));

		return new Option(COLOUR_PROJECTION, STORE_CODE, translations, ZonedDateTime.now(), false);
	}

}
