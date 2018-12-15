/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.domain.misc;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.LocaleUtils;

/**
 * Utility that identifies attribute names and locale parts from localized attribute keys.
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class LocalizedAttributeKeyUtils {

	private static final char SEPARATOR = '_';

	private static final String LOCALE_MATCH_GROUP_NAME = "locale";
	private static final String LANGUAGE_TAG_MATCH_GROUP_NAME = "languagetag";

	private static final String ATTRIBUTE_PROPERTY_MATCH_GROUP_NAME = "property";

	/**
	 * <p>This regex will match a portion of a localized attribute key that comprises a well-formed IETF BCP 47 Language Tag. For example, it will
	 * match "en-CA" in the localised attribute key "my_attribute_name_en-CA".</p>
	 * <p>The match can be found in the 'languagetag' named group.</p>
	 */
	private static final String LANGUAGE_TAG_MATCHING_REGEX =
			// attribute property match group
			"(?<" + ATTRIBUTE_PROPERTY_MATCH_GROUP_NAME + ">.*)"

					// leading underscore separator
					+ "(?:" + SEPARATOR + ")"

					// big outer match group (contains the language tag string result)
					+ "(?<" + LANGUAGE_TAG_MATCH_GROUP_NAME + ">"

					// Support for explicitly-listed legacy language tags.
					+ "(?<grandfathered>(en-GB-oed|i-ami|i-bnn|i-default|i-enochian|i-hak|"
					+ "i-klingon|i-lux|i-mingo|i-navajo|i-pwn|i-tao|i-tay|i-tsu|sgn-BE-FR"
					+ "|sgn-BE-NL|sgn-CH-DE)|(art-lojban|cel-gaulish|no-bok|no-nyn|zh-guoyu|zh-hakka|zh-min|zh-min-nan|zh-xiang)\\b)"

					// language e.g. the "en" part of "en-CA" (includes extlang)
					+ "|((?<language>([a-z]{2,3}\\b"

					// extlang is actually not used by Java. However, it will successfully parse and consume language tags including extlang parts,
					// and  strip them out when converting to a Locale instance.
					+ "(-(?<extlang>[A-Za-z]{3}(-[A-Za-z]{3}){0,2}))?))"

					// script e.g. the "Latn" part of "sr-Latn-BA"
					+ "(-(?<script>[A-Za-z]{4})\\b)?"

					// region/country e.g. the "CA" part of "en-CA"
					+ "(-(?<region>[A-Za-z]{2}|[0-9]{3})\\b)?"

					// variant e.g. the "POSIX" part of "en-US-POSIX"
					+ "(-(?<variant>[A-Za-z0-9]{5,8}|[0-9][A-Za-z0-9]{3})\\b)*"

					// extensions e.g. the complicated parts of "th-TH-u-ca-buddhist-nu-thai"
					+ "(-(?<extension>[0-9A-WY-Za-wy-z](-[A-Za-z0-9]{2,8})+)\\b)*"
					+ "(-(?<privateUse>x(-[A-Za-z0-9]{1,8})+)\\b)?)|(?<privateUse2>x(-[A-Za-z0-9]{1,8}\\b)+)"

					// end of outer match group
					+ ")$";
	/**
	 * <p>Matches simple Locale Strings, in other words the output of {@code Locale.toString}.</p>
	 * <p>This regex does not match the full gamut of Locale options; specifically it will match against a language, country, and variant, with the
	 * latter two fragments being optional.</p>
	 * <p>This provides support for String representations of Locales used prior to Java 1.6. For full Locale-matching support, refer to
	 * {@link #LANGUAGE_TAG_MATCHING_REGEX} for a regex matching the Language Tag scheme, supported since Java 1.7.</p>
	 */
	private static final String LOCALE_STRING_MATCHING_REGEX = ".*?" + SEPARATOR + "(?<" + LOCALE_MATCH_GROUP_NAME + ">[a-z]{2,3}(?:_[A-Z0-9]*(?:_"
			+ ".+)?)?)$";

	/**
	 * <p>Matches Extended Locale Strings that specify explicitly the script parameter, e.g. sr_BA_#Latn, but not jp_JP_#u-some-extension.</p>
	 * <p>This is important, because such Locales are produced by {@link Locale#toString()}, but can not be read by
	 * {@link LocaleUtils#toLocale(java.lang.String)}; they must be translated to a matching Language Tag manually. For example:
	 * <ul>
	 * <li>{@code Locale.forLanguageTag("sr-Latn-BA").toString()} => {@code "sr_BA_#Latn"}</li>
	 * <li>{@code LocaleUtils.toLocale(Locale.forLanguageTag("sr-Latn-BA").toString())} => throws {@code IllegalArgumentException}</li>
	 * </code>
	 * </p>
	 */
	private static final String EXTENDED_LOCALE_STRING_MATCHING_REGEX =
			".*?"
					+ SEPARATOR
					+ "(?<" + LOCALE_MATCH_GROUP_NAME + ">"
					+ "(?<language>[a-z]+)_(?<country>[A-Z0-9]+)?_#(?<script>[^-]{2,})"
					+ ")";

	private static final Pattern LANGUAGE_TAG_MATCHING_PATTERN = Pattern.compile(LANGUAGE_TAG_MATCHING_REGEX);

	private static final Pattern LOCALE_STRING_MATCHING_PATTERN = Pattern.compile(LOCALE_STRING_MATCHING_REGEX);

	private static final Pattern EXTENDED_LOCALE_STRING_MATCHING_PATTERN = Pattern.compile(EXTENDED_LOCALE_STRING_MATCHING_REGEX);

	/**
	 * Private constructor. This util class is not intended to be instantiated.
	 */
	private LocalizedAttributeKeyUtils() {
		// do not instantiate
	}

	/**
	 * Locates the locale part of the given Localized Attribute Key Name, and returns a matching {@link Locale} instance.
	 *
	 * @param attributeKeyName the localized attribute key name
	 * @return the corresponding {@link Locale} if one can be found; otherwise {@code null}
	 */
	public static Locale getLocaleFromLocalizedKeyName(final String attributeKeyName) {
		final String languageTag = getLanguageTagFromLocalizedKeyName(attributeKeyName);

		if (languageTag == null) {
			return null;
		}

		return Locale.forLanguageTag(languageTag);
	}

	/**
	 * Locates the locale part of the given Localized Attribute Key Name, and returns a matching language tag.
	 *
	 * @param attributeKeyName the localized attribute key name
	 * @return the corresponding language tag if one can be found; otherwise {@code null}
	 */
	public static String getLanguageTagFromLocalizedKeyName(final String attributeKeyName) {
		final Matcher matcher = getLanguageTagMatchingPattern().matcher(convertLocaleStringToLanguageTag(attributeKeyName));

		if (matcher.find()) {
			return matcher.group(LANGUAGE_TAG_MATCH_GROUP_NAME);
		}

		return null;
	}

	/**
	 * <p>Returns a Locale based on the input language string.</p>
	 * <p>Previous versions of Elastic Path Commerce Engine exported this attribute as a Locale string, e.g. "en_CA".  This attribute is now
	 * encoded as a Language Tag e.g. "en-CA", as per the approach recommended by Java following first-party support in versions 1.7 and above.</p>
	 * <p>In order to maintain backwards compatibility, we continue to support unmarshalling Locale strings.  This method will convert from the
	 * former format to the current if necessary.</p>
	 *
	 * @param language the language string to convert
	 * @return a Locale
	 */
	public static Locale convertLocaleStringToLocale(final String language) {
		if (language == null) {
			return null;
		}

		return Locale.forLanguageTag(convertLocaleStringToLanguageTag(language));
	}

	/**
	 * <p>Returns the Attribute Property component from a localized attribute key.</p>
	 * <p>Examples:</p>
	 * <table>
	 * <tr>
	 * <th>Input String</th><th>Output</th>
	 * </tr>
	 * <tr>
	 * <td><pre>attributename</pre></td><td><pre>attributename</pre></td>
	 * </tr>
	 * <tr>
	 * <td><pre>attributename_en-CA</pre></td><td><pre>attributename</pre></td>
	 * </tr>
	 * <tr>
	 * <td><pre>attributename_en_CA</pre></td><td><pre>attributename</pre></td>
	 * </tr>
	 * <tr>
	 * <td><pre>attribute_name</pre></td><td><pre>attribute_name</pre></td>
	 * </tr>
	 * <tr>
	 * <td><pre>attribute_name_en-CA</pre></td><td>attribute_name</pre></td>
	 * </tr>
	 * </table>
	 *
	 * @param localizedAttributeKey the localized attribute key
	 * @return the attribute property
	 */
	public static String getAttributePropertyFromLocalizedKeyName(final String localizedAttributeKey) {
		final Matcher matcher = getLanguageTagMatchingPattern().matcher(convertLocaleStringToLanguageTag(localizedAttributeKey));

		if (matcher.matches()) {
			return matcher.group(ATTRIBUTE_PROPERTY_MATCH_GROUP_NAME);
		}

		return localizedAttributeKey;
	}

	/**
	 * <p>Locates a Locale String within the given input, and translates it to a language tag.</p>
	 * <p>Previous versions of Elastic Path Commerce Engine exported this attribute as a Locale string, e.g. "en_CA".  This attribute is now
	 * encoded as a Language Tag e.g. "en-CA", as per the approach recommended by Java following first-party support in versions 1.7 and above.</p>
	 * <p>In order to maintain backwards compatibility, we continue to support unmarshalling Locale strings.  This method will convert from the
	 * former format to the current if necessary.</p>
	 *
	 * @param input the input string possibly containing a Locale String
	 * @return a string representing a valid locale language tag
	 */
	private static String convertLocaleStringToLanguageTag(final String input) {
		// Locale.toString example: sr_BA_#Latn
		// Locale.toLanguageTag example: sr-Latn-BA
		// but
		// Locale.toString example: jp_JP_#u-some-extension and th_TH_TH_#u-some-extension
		// Locale.toLanguageTag example: jp-JP-u-some-extension and th-TH-TH-u-some-extension
		final Matcher extendedLocaleMatcher = getExtendedLocaleStringMatchingPattern().matcher(input);

		final String propertyName;
		final String languageTag;

		if (extendedLocaleMatcher.matches()) {
			final String localeStr = extendedLocaleMatcher.group(LOCALE_MATCH_GROUP_NAME);

			propertyName = input.replaceAll(SEPARATOR + localeStr, "");
			languageTag = extendedLocaleMatcher.replaceAll("${language}-${script}-${country}");

			return propertyName + SEPARATOR + languageTag;
		} else {
			final Matcher simpleLocaleMatcher = getLocaleStringMatchingPattern().matcher(input);

			if (simpleLocaleMatcher.matches()) {
				final String localeStr = simpleLocaleMatcher.group(LOCALE_MATCH_GROUP_NAME);
				propertyName = input.replaceAll(SEPARATOR + localeStr, "");
				languageTag = toLanguageTag(localeStr);

				return propertyName + SEPARATOR + languageTag;
			}

			return input;
		}
	}

	/**
	 * Converts a Locale String to a Language Tag.
	 *
	 * @param localeStr the Locale String to convert
	 * @return a language tag String
	 */
	private static String toLanguageTag(final String localeStr) {
		try {
			return LocaleUtils.toLocale(localeStr).toLanguageTag();
		} catch (final Exception e) {
			return localeStr.replaceAll("_", "-")
					.replaceAll("#", "");
		}
	}

	/**
	 * <p>Returns a {@link Pattern} that will locate the language tag portion of a localised attribute key.  For example, will match "en-CA" in the
	 * localised attribute key "my_attribute_name_en-CA".</p>
	 * <p> The match can be found in the 'languagetag' named group.</p>
	 *
	 * @return a Pattern that locates language tags in localised attribute keys
	 */
	private static Pattern getLanguageTagMatchingPattern() {
		return LANGUAGE_TAG_MATCHING_PATTERN;
	}

	/**
	 * Returns a {@link Pattern} that will match a {@link Locale#toString() locale string}.
	 *
	 * @return a Pattern that matches locale strings
	 */
	private static Pattern getLocaleStringMatchingPattern() {
		return LOCALE_STRING_MATCHING_PATTERN;
	}

	/**
	 * Returns a {@link Pattern} that will match an extended {@link Locale#toString locale string} with a script, and group by its constituent
	 * elements.
	 *
	 * @return a Pattern that matches extended locale strings with scripts
	 */
	private static Pattern getExtendedLocaleStringMatchingPattern() {
		return EXTENDED_LOCALE_STRING_MATCHING_PATTERN;
	}

}