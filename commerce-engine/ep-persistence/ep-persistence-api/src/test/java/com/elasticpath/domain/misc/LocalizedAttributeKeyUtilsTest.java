/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.domain.misc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.LocaleUtils;
import org.junit.Test;

/**
 * Test class for {@link LocalizedAttributeKeyUtils}.
 */
public class LocalizedAttributeKeyUtilsTest {

	/**
	 * This is a sample set of locales supported by Java 7+ that covers the bases of language tag complexity.
	 */
	private static final String[] SAMPLE_SUPPORTED_LOCALE_LANGUAGE_TAGS = new String[]{
		// Easy examples
		"en",
		"fr",
		"es",
		"ar-AE",
		"be-BY",
		"ca-ES",
		"da-DK",
		"el-CY",
		"fi-FI",
		"ga-IE",
		"hi-IN",
		"it-IT",
		"ja-JP",
		"ko-KR",
		"lt-LT",
		"mk-MK",
		"nl-BE",
		"pl-PL",
		"ro-RO",
		"sk-SK",
		"th-TH",
		"uk-UA",
		"vi-VN",
		"zh-CN",

		// Cunning counterexamples
		"agq",
		"agq-CM",
		"ar-001",
		"en-029",
		"es-419",
		"mn-Mong-CN",
		"sr-Latn-BA",
		"sr-Latn-ME",
		"sr-Latn-RS",

//		 Downright devilish
		"en-US-WIN",
		"en-US-x-lvariant-POSIX",
		"sr-x-lvariant-BA",
		"ja-JP-u-ca-japanese",
		"th-TH-u-ca-buddhist",
		"th-TH-u-ca-buddhist-nu-thai",
	};

	private static final Map<String, Locale> SAMPLE_SUPPORTED_LOCALE_TOSTRING_VALUES = ImmutableMap.of(
			// Locate Locale.toString Strings too

			"sr-x-lvariant-BA",
			LocaleUtils.toLocale("sr__BA"),

			Locale.forLanguageTag("en-CA").toString(), // evaluates to "en_CA"
			Locale.forLanguageTag("en-CA"),

			Locale.forLanguageTag("sr-Latn-BA").toString(), // evaluates to sr_BA#Latn
			Locale.forLanguageTag("sr-Latn-BA"),

			Locale.forLanguageTag("jp-JP-u-some-extension").toString(), // evaluates to "jp_JP_#u-some-extension"
			Locale.forLanguageTag("jp-JP-u-some-extension"),

			Locale.forLanguageTag("th-TH-TH-u-some-extension").toString(), // evaluates to "th_TH_TH_#u-some-extension"
			Locale.forLanguageTag("th-TH-TH-u-some-extension")
	);

	private static final String[] UNLOCALISED_ATTRIBUTE_KEYS = new String[]{
			"I'm not a locale",
			"Neither am I.",
			"foooo-baaar",
			"attribute-name",
			"attributename",
			"attributenameen",
			"attributenameen-CA",
			"attribute-en-CA-name"
	};

	@Test
	public void verifyLanguageTagsCanBeFoundInLocalisedAttributeKeys() {
		final String attributeName = "attribute_name";

		// Converts language-tag strings (e.g. from Locale.toLanguageTag) to Java locale-string strings (e.g. from Locale.toString) and appends
		// them to the end of a sample attribute name.
		final Map<String, String> languageTagToAttributeNames = Arrays.stream(SAMPLE_SUPPORTED_LOCALE_LANGUAGE_TAGS)
				.map(Locale::forLanguageTag)
				.collect(
						Collectors.toMap(Locale::toLanguageTag, locale -> attributeName + "_" + locale));

		for (final Map.Entry<String, String> entry : languageTagToAttributeNames.entrySet()) {
			final String actualLanguageTag = LocalizedAttributeKeyUtils.getLanguageTagFromLocalizedKeyName(entry.getValue());
			assertThat(actualLanguageTag)
					.as("Unexpected language tag produced by translator")
					.isEqualTo(entry.getKey());
		}
	}

	@Test
	public void verifyLocaleToStringOutputCanBeFoundInLocalisedAttributeKeys() {
		SAMPLE_SUPPORTED_LOCALE_TOSTRING_VALUES.forEach((localeStr, expectedLocale) -> {
			final String attributeName = "attribute_name_" + localeStr;

			final Locale actualLocale = LocalizedAttributeKeyUtils.getLocaleFromLocalizedKeyName(attributeName);

			assertThat(actualLocale)
					.isEqualTo(expectedLocale);
		});
	}

	@Test
	public void verifyLocalesCanBeFoundInLocalisedAttributeKeys() {
		final String attributeName = "attribute_name";

		// Converts language-tag strings (e.g. from Locale.toLanguageTag) to Java locale-string strings (e.g. from Locale.toString) and appends
		// them to the end of a sample attribute name.
		final Map<Locale, String> languageTagToAttributeNames = Arrays.stream(SAMPLE_SUPPORTED_LOCALE_LANGUAGE_TAGS)
				.map(Locale::forLanguageTag)
				.collect(
						Collectors.toMap(locale -> locale, locale -> attributeName + "_" + locale));

		for (final Map.Entry<Locale, String> entry : languageTagToAttributeNames.entrySet()) {
			final Locale actualLocale = LocalizedAttributeKeyUtils.getLocaleFromLocalizedKeyName(entry.getValue());
			assertThat(actualLocale)
					.as("Unexpected locale produced by translator")
					.isEqualTo(entry.getKey());
		}
	}

	@Test
	public void verifyAttributePropertyNameCanBeFoundInLocalizedAttributeKeys() {
		final Locale locale = Locale.CANADA;
		final Collection<String> attributePropertyNames = ImmutableList.of(
				"attributename",
				"attribute_name",
				"a_b_c",
				"_attribute",

				// These are resolved as property names, because no remainder part can be a valid Locale
				"en",
				"en_CA",
				"en_CA_CA"
		);

		attributePropertyNames.forEach(attributeProperty -> {
			final String localisedAttributeKey = attributeProperty + "_" + locale;

			final String actualAttributeProperty = LocalizedAttributeKeyUtils.getAttributePropertyFromLocalizedKeyName(localisedAttributeKey);

			assertThat(actualAttributeProperty)
					.isEqualTo(attributeProperty);
		});
	}

	@Test
	public void verifyNullLocaleLanguageTagReturnedWhenNoLocalePartOfKey() {
		for (final String unlocalisedAttributeKey : UNLOCALISED_ATTRIBUTE_KEYS) {
			final String languageTag = LocalizedAttributeKeyUtils.getLanguageTagFromLocalizedKeyName(unlocalisedAttributeKey);
			assertThat(languageTag)
					.as("Expected no language tag to be returned when a key does not contain a locale value")
					.isNull();
		}
	}

	@Test
	public void verifyNullLocaleReturnedWhenNoLocalePartOfKey() {
		for (final String unlocalisedAttributeKey : UNLOCALISED_ATTRIBUTE_KEYS) {
			final Locale locale = LocalizedAttributeKeyUtils.getLocaleFromLocalizedKeyName(unlocalisedAttributeKey);
			assertThat(locale)
					.as("Expected no Locale to be returned when a key does not contain a locale value")
					.isNull();
		}
	}

}