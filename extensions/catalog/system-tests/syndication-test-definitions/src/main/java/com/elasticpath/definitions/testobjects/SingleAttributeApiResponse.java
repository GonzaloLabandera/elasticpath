/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.definitions.testobjects;

/**
 * Attribute API test object.
 */
public final class SingleAttributeApiResponse {

	/**
	 * get attribute projection API URL.
	 */
	public static final String ATTRIBUTE_URL = "%s/attributes/%s";

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
	 * JSON path to a flag which identifies if attribute is marked as deleted.
	 */
	public static final String DELETED = "deleted";

	/**
	 * JSON path to translations block.
	 */
	public static final String TRANSLATIONS = "translations";
	private static final String DISPLAY_NAME = "displayName";
	private static final String DATA_TYPE = "dataType";
	private static final String MULTI_VALUE = "multiValue";
	private static final String LANGUAGE = "language";

	private SingleAttributeApiResponse() {
	}

	/**
	 * Returns a path to display name in single attribute projection API response JSON.
	 *
	 * @param language locale code.
	 * @return a path to display name in single attribute projection API response JSON.
	 */
	public static String getDisplayNamePath(final String language) {
		return getTranslationPath(language) + DISPLAY_NAME;
	}

	/**
	 * Returns a path to data type in single attribute projection API response JSON.
	 *
	 * @param language locale code.
	 * @return a path to data type in single attribute projection API response JSON.
	 */
	public static String getDataTypePath(final String language) {
		return getTranslationPath(language) + DATA_TYPE;
	}

	/**
	 * Returns a path to multi value in single attribute projection API response JSON.
	 *
	 * @param language locale code.
	 * @return a path to multi value in single attribute projection API response JSON.
	 */
	public static String getMultiValuePath(final String language) {
		return getTranslationPath(language) + MULTI_VALUE;
	}

	/**
	 * Returns a path to multi language in single attribute projection API response JSON.
	 *
	 * @param language locale code.
	 * @return a path to multi language in single attribute projection API response JSON.
	 */
	public static String getMultiLanguagePath(final String language) {
		return getTranslationPath(language) + MULTI_VALUE;
	}

	/**
	 * Returns a path to language in single attribute projection API response JSON.
	 *
	 * @return a path to display name in single attribute projection API response JSON.
	 */
	public static String getTranslationsLanguagePath() {
		return TRANSLATIONS + "." + LANGUAGE;
	}

	/**
	 * Returns a path to translation block in single attribute projection API response JSON.
	 *
	 * @param language locale code.
	 * @return Returns a path to translation block in attribute brand projection API response JSON.
	 */
	private static String getTranslationPath(final String language) {
		return TRANSLATIONS + ".find{it.language == \"" + language + "\"}.";
	}
}
