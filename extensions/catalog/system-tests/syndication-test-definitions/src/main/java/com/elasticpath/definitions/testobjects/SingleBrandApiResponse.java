/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.definitions.testobjects;

/**
 * Brand API test object.
 */
public class SingleBrandApiResponse {

	/**
	 * get brand projection API URL.
	 */
	public static final String BRAND_URL = "%s/brands/%s";

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
	 * JSON path to a flag which identifies if brand is marked as deleted.
	 */
	public static final String DELETED = "deleted";

	/**
	 * JSON path to type.
	 */
	public static final String TRANSLATIONS = "translations";
	private static final String LANGUAGE = "language";
	private static final String DISPLAY_NAME = "displayName";

	/**
	 * Returns a path to display name in single brand projection API response JSON.
	 *
	 * @param language locale code.
	 * @return a path to display name in single brand projection API response JSON.
	 */
	public String getDisplayNamePath(final String language) {
		return getTranslationPath(language) + DISPLAY_NAME;
	}

	/**
	 * Returns a path to language in single brand projection API response JSON.
	 *
	 * @return a path to display name in single brand projection API response JSON.
	 */
	public String getTranslationsLanguagePath() {
		return TRANSLATIONS + "." + LANGUAGE;
	}

	/**
	 * Returns a path to translation block in single brand projection API response JSON.
	 *
	 * @param language locale code.
	 * @return Returns a path to translation block in single brand projection API response JSON.
	 */
	private String getTranslationPath(final String language) {
		return TRANSLATIONS + ".find{it.language == \"" + language + "\"}.";
	}
}
