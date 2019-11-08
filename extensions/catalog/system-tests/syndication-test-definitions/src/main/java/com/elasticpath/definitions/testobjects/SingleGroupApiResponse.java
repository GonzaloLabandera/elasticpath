package com.elasticpath.definitions.testobjects;

/**
 * Cart Item Modifier Group API test object.
 */
public final class SingleGroupApiResponse {

	/**
	 * get field metadata projection API URL.
	 */
	public static final String GROUP_URL = "%s/fieldmetadata/%s";

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
	 * JSON path to a flag which identifies if cart item modifier group is marked as deleted.
	 */
	public static final String DELETED = "deleted";

	/**
	 * JSON path to translations block.
	 */
	public static final String TRANSLATIONS = "translations";
	private static final String REQUIRED = "required";
	private static final String MAX_SIZE = "maxSize";
	private static final String DISPLAY_NAME = "displayName";
	private static final String DATA_TYPE = "dataType";
	private static final String NAME = "name";
	private static final String DISPLAY_VALUE = "displayValue";
	private static final String VALUE = "value";
	private static final String LANGUAGE = "language";
	private static final String FIELDS = "fields";
	private static final String FIELD_VALUES = "fieldValues";
	private static final String FIND_CLOSURE = "\"}.";

	private SingleGroupApiResponse() {
	}

	/**
	 * Returns a path to group value in single field metadata projection API response JSON.
	 *
	 * @param language  locale code
	 * @param fieldCode cart item modifier group code
	 * @return a path to field displayName in single option projection API response JSON
	 */
	public static String getGroupNamePath(final String language, final String fieldCode) {
		return getGroupValuePath(language, fieldCode) + DISPLAY_NAME;
	}

	/**
	 * Returns a path to display name in single field metadata projection API response JSON.
	 *
	 * @param language locale code
	 * @return a path to display name in single option projection API response JSON
	 */
	public static String getDisplayNamePath(final String language) {
		return getTranslationPath(language) + DISPLAY_NAME;
	}

	/**
	 * @param language locale code
	 * @return a path to name in single field metadata projection API response JSON
	 */
	public static String getGroupValuePath(final String language) {
		return SingleGroupApiResponse.getFields(language) + "." + NAME;
	}

	/**
	 * @param language               locale code
	 * @param groupFieldCode         cart item modifier group code
	 * @param valueOptionDisplayName cart item modifier group field option display name
	 * @return a path to field value data type in single option projection API response JSON
	 */
	public static Object getGroupFieldsValue(final String language, final String valueOptionDisplayName, final String groupFieldCode) {
		return getGroupFieldOptionValue(language, groupFieldCode, valueOptionDisplayName) + VALUE;
	}

	/**
	 * @param language        locale code
	 * @param groupFieldCode  cart item modifier group code
	 * @param valueOptionName cart item modifier group field option name
	 * @return a path to field name type in single field metadata projection API response JSON
	 */
	public static Object getGroupFieldsName(final String language, final String valueOptionName, final String groupFieldCode) {
		return getGroupFieldOptionName(language, groupFieldCode, valueOptionName) + DISPLAY_VALUE;
	}

	/**
	 * @param language       locale code
	 * @param groupFieldCode cart item modifier group code
	 * @return a path to field data type in single field metadata projection API response JSON
	 */
	public static Object getGroupFieldDataType(final String language, final String groupFieldCode) {
		return getGroupValuePath(language, groupFieldCode) + DATA_TYPE;
	}

	/**
	 * @param language       locale code
	 * @param groupFieldCode cart item modifier group code
	 * @return a path to field required status in single field metadata projection API response JSON
	 */
	public static Object groupFieldIsRequired(final String language, final String groupFieldCode) {
		return getGroupValuePath(language, groupFieldCode) + REQUIRED;
	}

	/**
	 * @param language       locale code
	 * @param groupFieldCode cart item modifier group code
	 * @return a path to field max size in single field metadata projection API response JSON
	 */
	public static String getGroupFieldMaxSize(final String language, final String groupFieldCode) {
		return getGroupValuePath(language, groupFieldCode) + MAX_SIZE;
	}

	/**
	 * @param language       locale code
	 * @param groupFieldCode cart item modifier group code
	 * @return a path to field values in single field metadata projection API response JSON
	 */
	public static Object getGroupFieldFields(final String language, final String groupFieldCode) {
		return getGroupValuePath(language, groupFieldCode) + FIELD_VALUES;
	}

	/**
	 * Returns a path to language in single field metadata projection API response JSON.
	 *
	 * @return a path to display name in single field metadata projection API response JSON
	 */
	public static String getTranslationsLanguagePath() {
		return TRANSLATIONS + "." + LANGUAGE;
	}

	/**
	 * @param language       locale code
	 * @param groupFieldCode cart item modifier group code
	 * @return a path to field option displayName in single field metadata projection API response JSON
	 */
	private static String getGroupFieldOptionName(final String language, final String groupFieldCode, final String valuesOptionName) {
		return getGroupFieldFields(language, groupFieldCode) + ".find{it.value == \"" + valuesOptionName + FIND_CLOSURE;
	}

	/**
	 * @param language       locale code
	 * @param groupFieldCode cart item modifier group code
	 * @return a path to field option value in single field metadata projection API response JSON
	 */
	private static String getGroupFieldOptionValue(final String language, final String groupFieldCode, final String valueOptionDisplayName) {
		return getGroupFieldFields(language, groupFieldCode) + ".find{it.displayValue == \"" + valueOptionDisplayName + FIND_CLOSURE;
	}

	/**
	 * @param language locale code
	 * @return a path to fields in single field metadata projection API response JSON
	 */
	private static String getFields(final String language) {
		return getTranslationPath(language) + FIELDS;
	}

	/**
	 * Returns a path to translation block in single option projection API response JSON.
	 *
	 * @param language locale code
	 * @return Returns a path to translation block in single field metadata projection API response JSON
	 */
	private static String getTranslationPath(final String language) {
		return TRANSLATIONS + ".find{it.language == \"" + language + FIND_CLOSURE;
	}

	/**
	 * @param language  locale code
	 * @param fieldCode cart item modifier group code
	 * @return a path to field displayName in single field metadata projection API response JSON
	 */
	private static String getGroupValuePath(final String language, final String fieldCode) {
		return SingleGroupApiResponse.getFields(language) + ".find{it.name == \"" + fieldCode + FIND_CLOSURE;
	}
}
