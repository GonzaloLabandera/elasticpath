package com.elasticpath.definitions.testobjects;


/**
 * Sku option API test object.
 */
public class SingleOptionApiResponse {

	/**
	 * get option projection API URL.
	 */
	public static final String OPTION_URL = "%s/options/%s";

	/**
	 * JSON path to type.
	 */
	public static final String TYPE = "identity.type";

	/**
	 * JSON path to code.
	 */
	public static final String CODE = "identity.code";

	/**
	 * JSON path to store.
	 */
	public static final String STORE = "identity.store";

	/**
	 * JSON path to modified date.
	 */
	public static final String MODIFIED_DATE_TIME = "modifiedDateTime";

	/**
	 * JSON path to a flag which identifies if sku option is marked as deleted.
	 */
	public static final String DELETED = "deleted";

	/**
	 * JSON path to translations block.
	 */
	public static final String TRANSLATIONS = "translations";
	private static final String LANGUAGE = "language";
	private static final String DISPLAY_NAME = "displayName";
	private static final String OPTION_VALUES = "optionValues";
	private static final String VALUE = "value";
	private static final String DISPLAY_VALUE = "displayValue";

	/**
	 * Returns a path to display name in single option projection API response JSON.
	 *
	 * @param language locale code
	 * @return a path to display name in single option projection API response JSON
	 */
	public String getDisplayNamePath(final String language) {
		return getTranslationPath(language) + DISPLAY_NAME;
	}

	/**
	 * Returns a path to language in single option projection API response JSON.
	 *
	 * @return a path to display name in single option projection API response JSON
	 */
	public String getTranslationsLanguagePath() {
		return TRANSLATIONS + "." + LANGUAGE;
	}

	/**
	 * Returns a path to the first sku option value code in single option projection API response JSON.
	 *
	 * @param language locale code
	 * @return Returns a path to sku option value code in single option projection API response JSON
	 */
	public String getFirstOptionValuePath(final String language) {
		return getOptionValues(language) + ".first()." + VALUE;
	}

	/**
	 * Returns a path to a specified sku option value code in single option projection API response JSON.
	 *
	 * @param language locale code
	 * @return Returns a path to sku option value code in single option projection API response JSON.
	 */
	public String getOptionValuePath(final String language) {
		return getOptionValues(language) + "." + VALUE;
	}

	/**
	 * Returns a path to a specified sku option value code in single option projection API response JSON.
	 *
	 * @param language locale code
	 * @param valueCode sku option value code
	 * @return Returns a path to sku option value code in single option projection API response JSON.
	 */
	public String getOptionValuePath(final String language, final String valueCode) {
		return getOptionValues(language) + ".find{it.value == \"" + valueCode + "\"}.";
	}

	/**
	 * Returns a path to sku option values array in single option projection API response JSON.
	 *
	 * @param language locale code
	 * @return Returns a path to sku option value code in single option projection API response JSON
	 */
	public String getOptionValues(final String language) {
		return getTranslationPath(language) + OPTION_VALUES;
	}

	/**
	 * Returns a path to the first sku option value display name in single option projection API response JSON.
	 *
	 * @param language locale code
	 * @return Returns a path to sku option value display name in single option projection API response JSON
	 */
	public String getFirstOptionValueNamePath(final String language) {
		return getTranslationPath(language) + OPTION_VALUES + ".first()." + DISPLAY_VALUE;
	}

	/**
	 * Returns a path to sku option value display name in single option projection API response JSON.
	 *
	 * @param language      locale code
	 * @param skuOptionValueCode sku option value code
	 * @return Returns a path to sku option value display name in single option projection API response JSON
	 */
	public String getOptionValueNamePath(final String language, final String skuOptionValueCode) {
		return getOptionValuePath(language, skuOptionValueCode) + DISPLAY_VALUE;
	}

	/**
	 * Returns a path to translation block in single option projection API response JSON.
	 *
	 * @param language locale code
	 * @return Returns a path to translation block in single option projection API response JSON
	 */
	private String getTranslationPath(final String language) {
		return TRANSLATIONS + ".find{it.language == \"" + language + "\"}.";
	}

}
