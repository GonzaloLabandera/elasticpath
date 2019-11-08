/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.definitions.testobjects;

/**
 * Brand API test object.
 */
public final class SingleCategoryApiResponse {
	/**
	 * Get category projection API URL.
	 */
	public static final String CATEGORY_URL = "%s/categories/%s";
	/**
	 * JSON path to modifiedDateTime field in Category projection.
	 */
	public static final String MODIFIED_DATE_TIME = "modifiedDateTime";
	/**
	 * JSON path to deleted field in Category projection.
	 */
	public static final String DELETED = "deleted";
	/**
	 * JSON path to identity.type field in Category projection.
	 */
	public static final String TYPE = "identity.type";
	/**
	 * JSON path to identity.code field in Category projection.
	 */
	public static final String CODE = "identity.code";
	/**
	 * JSON path to identity.store field in Category projection.
	 */
	public static final String STORE = "identity.store";
	/**
	 * JSON path to translations field in Category projection.
	 */
	private static final String TRANSLATIONS = "translations";
	/**
	 * JSON path to availabilityRules field in Category projection.
	 */
	private static final String AVAILABILITY_RULES = "availabilityRules";
	/**
	 * JSON path to properties field in Category projection.
	 */
	private static final String PROPERTIES = "properties";
	/**
	 * JSON path to displayName field in Category projection.
	 */
	private static final String DISPLAY_NAME = "displayName";
	/**
	 * JSON path to displayValues field in Category projection.
	 */
	private static final String DISPLAY_VALUES = "displayValues";
	/**
	 * JSON path to name field in Category projection.
	 */
	private static final String NAME = "name";
	/**
	 * JSON path to children field in Category projection.
	 */
	private static final String CHIDLREN = "children";
	/**
	 * JSON path to details field in Category projection.
	 */
	private static final String DETAILS = "details";
	/**
	 * JSON path to disableDateTime field in Category projection.
	 */
	private static final String DISABLE_DATE_TIME = "disableDateTime";
	/**
	 * JSON path to enableDateTime field in Category projection.
	 */
	private static final String ENABLE_DATE_TIME = "enableDateTime";
	/**
	 * JSON path to path field in Category projection.
	 */
	private static final String PATH = "path";
	/**
	 * JSON path to value field in Category projection.
	 */
	private static final String VALUE = "value";
	/**
	 * JSON path to values field in Category projection.
	 */
	private static final String VALUES = "values";
	/**
	 * JSON path to parent field in Category projection.
	 */
	private static final String PARENT = "parent";
	/**
	 * Closure for find() method.
	 */
	private static final String FIND_CLOSURE = "\"}.";
	/**
	 * JSON path to extensions field in Category projection.
	 */
	private static final String EXTENSIONS = "extensions";
	private static final String LANGUAGE = "language";

	private SingleCategoryApiResponse() {
	}

	/**
	 * Get displayName JSON path by language.
	 *
	 * @param language displayName language.
	 * @return JSON path for displayName.
	 */
	public static String getDisplayNamePath(final String language) {
		return getTranslationPath(language) + DISPLAY_NAME;
	}

	/**
	 * Get detailDisplayName JSON path by language and detailName.
	 *
	 * @param language   detail displayName language.
	 * @param detailName detail name.
	 * @return JSON path for detailDisplayName.
	 */
	public static String getDetailDisplayName(final String language, final String detailName) {
		return getDetail(language, detailName) + DISPLAY_NAME;
	}

	/**
	 * Get detailName JSON path by language and detailName.
	 *
	 * @param language   detailName language.
	 * @param detailName detail name.
	 * @return JSON path for detailName.
	 */
	public static String getDetailName(final String language, final String detailName) {
		return getDetail(language, detailName) + NAME;
	}

	/**
	 * Get detailValues JSON path by language and detailName.
	 *
	 * @param language   detailValues language.
	 * @param detailName detail name.
	 * @return JSON path for detailName.
	 */
	public static Object getDetailValues(final String language, final String detailName) {
		return getDetail(language, detailName) + VALUES;
	}

	/**
	 * Get detailDisplayValues JSON path by language and detailName.
	 *
	 * @param language   detailDisplayValues language.
	 * @param detailName detail name.
	 * @return JSON path for detailDisplayValues.
	 */
	public static Object getDetailDisplayValues(final String language, final String detailName) {
		return getDetail(language, detailName) + DISPLAY_VALUES;
	}

	/**
	 * Get propertiesValue JSON path by categoryName.
	 *
	 * @param categoryName category name.
	 * @return JSON path for properties values.
	 */
	public static String getPropertiesValue(final String categoryName) {
		return getProperties(categoryName) + VALUE;
	}

	/**
	 * Get properties name JSON path by categoryName.
	 *
	 * @param categoryName category name.
	 * @return JSON path for properties name.
	 */
	public static String getPropertiesName(final String categoryName) {
		return getProperties(categoryName) + NAME;
	}

	/**
	 * Get enableDateTime JSON path.
	 *
	 * @return JSON path for enableDateTime.
	 */
	public static String getEnableDateTime() {
		return getAvailabilityRules() + ENABLE_DATE_TIME;
	}

	/**
	 * Get enableDateTime JSON path.
	 *
	 * @return JSON path for enableDateTime.
	 */
	public static String getDisableDateTime() {
		return getAvailabilityRules() + DISABLE_DATE_TIME;
	}

	/**
	 * Get children JSON path.
	 *
	 * @return JSON path for children.
	 */
	public static String getChildren() {
		return CHIDLREN;
	}

	/**
	 * Get parent JSON path.
	 *
	 * @return JSON path for parent.
	 */
	public static String getParent() {
		return PARENT;
	}

	/**
	 * Get path JSON path.
	 *
	 * @return JSON path for path.
	 */
	public static String getPath() {
		return PATH;
	}

	/**
	 * Get extension JSON path.
	 *
	 * @return JSON path for extension.
	 */
	public static String getExtension() {
		return EXTENSIONS;
	}

	/**
	 * Get detail JSON path by language and detail.
	 *
	 * @param language   detailName language.
	 * @param detailName detail name.
	 * @return JSON path for detail.
	 */
	private static String getDetail(final String language, final String detailName) {
		return getDetails(language) + ".find{it.name == \"" + detailName + FIND_CLOSURE;
	}

	/**
	 * Get availabilityRules JSON path.
	 *
	 * @return JSON path for availabilityRules.
	 */
	private static String getAvailabilityRules() {
		return AVAILABILITY_RULES + ".";
	}

	/**
	 * Get properties JSON path by categoryName.
	 *
	 * @param categoryName category name.
	 * @return JSON path for properties name.
	 */
	private static String getProperties(final String categoryName) {
		return PROPERTIES + ".find{it.name == \"" + categoryName + FIND_CLOSURE;
	}

	/**
	 * Get details JSON path by language.
	 *
	 * @param language details language.
	 * @return JSON path for details.
	 */
	private static String getDetails(final String language) {
		return getTranslationPath(language) + DETAILS;
	}

	/**
	 * Get properties name JSON path by language.
	 *
	 * @param language language for translationPath.
	 * @return JSON path for translationPath.
	 */
	private static String getTranslationPath(final String language) {
		return TRANSLATIONS + ".find{it.language == \"" + language + "\"}.";
	}

	/**
	 * Returns a path to language in single brand projection API response JSON.
	 *
	 * @return a path to language in single category projection API response JSON.
	 */
	public static String getTranslationsLanguagePath() {
		return TRANSLATIONS + "." + LANGUAGE;
	}

}