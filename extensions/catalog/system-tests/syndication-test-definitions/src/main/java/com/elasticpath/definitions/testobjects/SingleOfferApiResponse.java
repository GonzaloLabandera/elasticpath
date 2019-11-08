/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.definitions.testobjects;

/**
 * Offer API test object.
 */
public final class SingleOfferApiResponse {
	/**
	 * Get offer projection API URL.
	 */
	public static final String OFFER_URL = "%s/offers/%s";
	/**
	 * JSON path to properties field in Offer projection.
	 */
	public static final String PROPERTIES = "properties";
	/**
	 * JSON path to availabilityRules field in Offer projection.
	 */
	public static final String AVAILABILITY_RULES = "availabilityRules";
	/**
	 * JSON path to selectionRules field in Offer projection.
	 */
	public static final String SELECTION_RULES = "selectionRules";
	/**
	 * JSON path to components field in Offer projection.
	 */
	public static final String COMPONENTS = "components";
	/**
	 * JSON path to extensions field in Offer projection.
	 */
	public static final String EXTENSIONS = "extensions";
	/**
	 * JSON path to categories field in Offer projection.
	 */
	public static final String CATEGORIES = "categories";
	/**
	 * JSON path to translations field in Offer projection.
	 */
	public static final String TRANSLATIONS = "translations";
	/**
	 * JSON path to deleted field in Offer projection.
	 */
	public static final String DELETED = "deleted";
	/**
	 * JSON path to formFields field in Offer projection.
	 */
	public static final String FORM_FIELDS = "formFields";
	/**
	 * JSON find first item from array in JSON response.
	 */
	public static final String FIRST = ".first()";
	/**
	 * JSON path to displayName field in Offer projection.
	 */
	private static final String DISPLAY_NAME = "displayName";
	/**
	 * JSON path to properties field in Offer projection.
	 */
	private static final String PROPERTY = "properties";
	/**
	 * JSON path to disableDateTime field in Offer projection.
	 */
	private static final String DISABLE_DATE_TIME = "disableDateTime";
	/**
	 * JSON path to enableDateTime field in Offer projection.
	 */
	private static final String ENABLE_DATE_TIME = "enableDateTime";
	/**
	 * JSON path to canDiscover field in Offer projection.
	 */
	private static final String CAN_DISCOVER = "canDiscover";
	/**
	 * JSON path to canView field in Offer projection.
	 */
	private static final String CAN_VIEW = "canView";
	/**
	 * JSON path to canAddToCart field in Offer projection.
	 */
	private static final String CAN_ADD_TO_CART = "canAddToCart";
	/**
	 * JSON path to selectionType field in Offer projection.
	 */
	private static final String SELECTION_TYPE = "selectionType";
	/**
	 * JSON path to quantity field in Offer projection.
	 */
	private static final String QUANTITY = "quantity";
	/**
	 * JSON path to components.list field in Offer projection.
	 */
	private static final String COMPONENTS_LIST = "components.list";
	/**
	 * JSON path to categories.code field in Offer projection.
	 */
	private static final String CATEGORIES_CODE = "categories.code";
	/**
	 * JSON path to path field in Offer projection.
	 */
	private static final String PATH = "path";
	/**
	 * JSON path to defaultt field in Offer projection.
	 */
	private static final String DEFAULT = "default";
	/**
	 * JSON path to featured field in Offer projection.
	 */
	private static final String FEATURED = "featured";
	/**
	 * JSON path to brand field in Offer projection.
	 */
	private static final String BRAND = "brand";
	/**
	 * JSON path to options field in Offer projection.
	 */
	private static final String OPTIONS = "options";
	/**
	 * JSON path to details field in Offer projection.
	 */
	private static final String DETAILS = "details";
	/**
	 * JSON path to items field in Offer projection.
	 */
	public static final String ITEMS = "items";
	/**
	 * JSON path to name field in Offer projection.
	 */
	private static final String NAME = "name";
	/**
	 * JSON path to shippingProperties field in Offer projection.
	 */
	private static final String SHIPPING_PROPERTIES = "shippingProperties";
	/**
	 * JSON path to value field in Offer projection.
	 */
	private static final String VALUE = "value";
	/**
	 * JSON path to itemCode field in Offer projection.
	 */
	private static final String ITEM_CODE = "itemCode";
	/**
	 * JSON path to associations field in Offer projection.
	 */
	private static final String ASSOCIATIONS = "associations";
	/**
	 * JSON path to list.offer field in Offer projection.
	 */
	private static final String LIST_OFFER = "list.offer";
	/**
	 * JSON path to categories.disableDateTime field in Offer projection.
	 */
	private static final String CATEGORIES_DISABLE_DATE_TIME = "categories.disableDateTime";
	/**
	 * JSON path to categories.enableDateTime field in Offer projection.
	 */
	private static final String CATEGORIES_ENABLE_DATE_TIME = "categories.enableDateTime";
	/**
	 * JSON path to categories.path field in Offer projection.
	 */
	public static final String CATEGORIES_PATH = "categories.path";
	/**
	 * JSON path to categories.default field in Offer projection.
	 */
	public static final String CATEGORIES_DEFAULT = "categories.default";
	/**
	 * JSON path to categories.featured field in Offer projection.
	 */
	public static final String CATEGORIES_FEATURED = "categories.featured";
	/**
	 * JSON path to releaseDateTime field in Offer projection.
	 */
	private static final String RELEASE_DATE_TIME = "releaseDateTime";

	/**
	 * POINT in JSON path in Offer projection.
	 */
	private static final String POINT = "\"}.";

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

	private SingleOfferApiResponse() {
	}

	/**
	 * Returns a path to display name in single offer projection API response JSON.
	 *
	 * @param language locale code.
	 * @return a path to display name in single offer projection API response JSON.
	 */
	public static String getDisplayNamePath(final String language) {
		return getTranslationPath(language) + DISPLAY_NAME;
	}

	/**
	 * Returns a path to brand name in single offer projection API response JSON.
	 *
	 * @param language locale code.
	 * @return a path to brand name in single offer projection API response JSON.
	 */
	public static String getBrandNamePath(final String language) {
		return getTranslationPath(language) + BRAND + "." + NAME;
	}

	/**
	 * Returns a path to brand display name in single offer projection API response JSON.
	 *
	 * @param language locale code.
	 * @return a path to brand display name in single offer projection API response JSON.
	 */
	public static String getBrandDisplayNamePath(final String language) {
		return getTranslationPath(language) + BRAND + "." + DISPLAY_NAME;
	}

	/**
	 * Returns a path to sku code in single offer projection API response JSON.
	 *
	 * @return a path to sku code in single offer projection API response JSON.
	 */
	public static String getSkuCodePath() {
		return ITEMS + "." + ITEM_CODE + FIRST;
	}

	/**
	 * Returns a path to sku property in single offer projection API response JSON.
	 *
	 * @param property property.
	 * @return a path to sku property in single offer projection API response JSON.
	 */
	public static String getSkuPropertiesPath(final String property) {
		return ITEMS + "." + getPropertyPath(property) + VALUE;
	}

	/**
	 * Returns a path to shipping properties and get first in single offer projection API response JSON.
	 *
	 * @param property property.
	 * @return a path to shipping properties and get first in single offer projection API response JSON.
	 */
	public static String getShippingPropertiesFirstPath(final String property) {
		return ITEMS + "." + SHIPPING_PROPERTIES + "." + property + FIRST;
	}

	/**
	 * Returns a path to items shipping properties in single offer projection API response JSON.
	 *
	 * @param property property.
	 * @return a path to items shipping properties in single offer projection API response JSON.
	 */
	public static String getItemsShippingPropertyPath(final String property) {
		return ITEMS + "." + getShippingPropertyPath(property);
	}

	/**
	 * Returns a path to shipping properties in single offer projection API response JSON.
	 *
	 * @param property property.
	 * @return a path to shipping properties in single offer projection API response JSON.
	 */
	public static String getShippingPropertyPath(final String property) {
		return SHIPPING_PROPERTIES + "." + property;
	}

	/**
	 * Returns a path to items options translation in single offer projection API response JSON.
	 *
	 * @param language locale code.
	 * @return a path to items options translation in single offer projection API response JSON.
	 */
	public static String getItemsOptionsTranslationPath(final String language) {
		return ITEMS + "." + getTranslationPath(language) + OPTIONS;
	}

	/**
	 * Returns a path to items details translation in single offer projection API response JSON.
	 *
	 * @param language locale code.
	 * @return a path to items details translation in single offer projection API response JSON.
	 */
	public static String getItemsDetailsTranslationPath(final String language) {
		return ITEMS + "." + getTranslationPath(language) + DETAILS;
	}

	/**
	 * Returns a path to items details translation in single offer projection API response JSON.
	 *
	 * @return a path to items details translation in single offer projection API response JSON.
	 */
	public static String getItemsCodesPath() {
		return ITEMS + "." + ITEM_CODE;
	}

	/**
	 * Returns a path to options translation in single offer projection API response JSON.
	 *
	 * @param language locale code.
	 * @return a path to options translation in single offer projection API response JSON.
	 */
	public static String getOptionsTranslationPath(final String language) {
		return getTranslationPath(language) + OPTIONS;
	}

	/**
	 * Returns a path to options translation in single offer projection API response JSON.
	 *
	 * @param language locale code.
	 * @return a path to options translation in single offer projection API response JSON.
	 */
	public static String getOptionsNamePath(final String language) {
		return getTranslationPath(language) + OPTIONS + "." + NAME;
	}

	/**
	 * Returns a path to options translation in single offer projection API response JSON.
	 *
	 * @param language locale code.
	 * @return a path to options translation in single offer projection API response JSON.
	 */
	public static String getOptionsTranslationDisplayNamePath(final String language) {
		return getTranslationPath(language) + OPTIONS + DISPLAY_NAME;
	}

	/**
	 * Returns a path to details translation in single offer projection API response JSON.
	 *
	 * @param language locale code.
	 * @return a path to details translation in single offer projection API response JSON.
	 */
	public static String getDetailsTranslationPath(final String language) {
		return getTranslationPath(language) + DETAILS;
	}

	/**
	 * Returns a path to details translation in single offer projection API response JSON.
	 *
	 * @param language locale code.
	 * @return a path to details translation in single offer projection API response JSON.
	 */
	public static String getDetailsNamePath(final String language) {
		return getTranslationPath(language) + DETAILS + "." + NAME;
	}

	/**
	 * Returns a path to details translation in single offer projection API response JSON.
	 *
	 * @param language locale code.
	 * @return a path to details translation in single offer projection API response JSON.
	 */
	public static String getDetailsDisplayNamePath(final String language) {
		return getTranslationPath(language) + DETAILS + "." + DISPLAY_NAME;
	}


	/**
	 * Returns a path to display name in single offer projection API response JSON.
	 *
	 * @param property property.
	 * @return a path to display name in single offer projection API response JSON.
	 */
	public static String getPropertyValuePath(final String property) {
		return getPropertyPath(property) + VALUE;
	}

	/**
	 * Returns a path to translation block in single offer projection API response JSON.
	 *
	 * @param language locale code.
	 * @return Returns a path to translation block in single offer projection API response JSON.
	 */
	private static String getTranslationPath(final String language) {
		return TRANSLATIONS + ".find{it.language == \"" + language + POINT;
	}

	/**
	 * Returns a path to property in single offer projection API response JSON.
	 *
	 * @param property property.
	 * @return a path to property in single offer projection API response JSON.
	 */
	private static String getPropertyPath(final String property) {
		return PROPERTY + ".find{it.name == \"" + property + POINT;
	}

	/**
	 * Returns a path to availability rules in single offer projection API response JSON.
	 *
	 * @return a path to availability rules in single offer projection API response JSON.
	 */
	public static String getCanDiscoverPath() {
		return AVAILABILITY_RULES + "." + CAN_DISCOVER;
	}

	/**
	 * Returns a path to enable date in single offer projection API response JSON.
	 *
	 * @return a path to enable date in single offer projection API response JSON.
	 */
	public static String getEnableDateTimePath() {
		return AVAILABILITY_RULES + "." + ENABLE_DATE_TIME;
	}

	/**
	 * Returns a path to items enable date in single offer projection API response JSON.
	 *
	 * @return a path to items enable date in single offer projection API response JSON.
	 */
	public static String getItemsEnableDateTimePath() {
		return ITEMS + "." + AVAILABILITY_RULES + "." + ENABLE_DATE_TIME + FIRST;
	}

	/**
	 * Returns a path to disable date in single offer projection API response JSON.
	 *
	 * @return a path to disable date in single offer projection API response JSON.
	 */
	public static String getDisableDateTimePath() {
		return AVAILABILITY_RULES + "." + DISABLE_DATE_TIME;
	}

	/**
	 * Returns a path to category enable date in single offer projection API response JSON.
	 *
	 * @return a path to category enable date in single offer projection API response JSON.
	 */
	public static String getCategoryEnableDateTimePath() {
		return CATEGORIES + "." + ENABLE_DATE_TIME + FIRST;
	}

	/**
	 * Returns a path to category disable date in single offer projection API response JSON.
	 *
	 * @return a path to category disable date in single offer projection API response JSON.
	 */
	public static String getCategoryDisableDateTimePath() {
		return CATEGORIES + "." + DISABLE_DATE_TIME + FIRST;
	}

	/**
	 * Returns a path to availability rules in single offer projection API response JSON.
	 *
	 * @return a path to availability rules in single offer projection API response JSON.
	 */
	public static String getCanViewPath() {
		return AVAILABILITY_RULES + "." + CAN_VIEW + FIRST;
	}

	/**
	 * Returns a path to availability rules in single offer projection API response JSON.
	 *
	 * @return a path to availability rules in single offer projection API response JSON.
	 */
	public static String getCanAddToCartPath() {
		return AVAILABILITY_RULES + "." + CAN_ADD_TO_CART;
	}

	/**
	 * Returns a path to selection type in single offer projection API response JSON.
	 *
	 * @return a path to selection type in single offer projection API response JSON.
	 */
	public static String getSelectionTypePath() {
		return SELECTION_RULES + "." + SELECTION_TYPE;
	}

	/**
	 * Returns a path to quantity in single offer projection API response JSON.
	 *
	 * @return a path to quantity in single offer projection API response JSON.
	 */
	public static String getQuantityPath() {
		return SELECTION_RULES + "." + QUANTITY;
	}

	/**
	 * Returns a path to category code in single offer projection API response JSON.
	 *
	 * @return a path to category code in single offer projection API response JSON.
	 */
	public static String getCategoryCodePath() {
		return CATEGORIES_CODE + FIRST;
	}

	/**
	 * Returns a path to components list in single offer projection API response JSON.
	 *
	 * @return a path to components list in single offer projection API response JSON.
	 */
	public static String getComponentsListPath() {
		return COMPONENTS_LIST;
	}

	/**
	 * Returns a path to category default value in single offer projection API response JSON.
	 *
	 * @return a path to category default value in single offer projection API response JSON.
	 */
	public static String getCategoryDefaultPath() {
		return CATEGORIES + "." + DEFAULT + FIRST;
	}

	/**
	 * Returns a path to category featured value in single offer projection API response JSON.
	 *
	 * @return a path to category featured value in single offer projection API response JSON.
	 */
	public static String getCategoryFeaturedPath() {
		return CATEGORIES + "." + FEATURED + FIRST;
	}

	/**
	 * Returns a path to form fields in single offer projection API response JSON.
	 *
	 * @return a path to form fields in single offer projection API response JSON.
	 */
	public static String getFormFieldsPath() {
		return FORM_FIELDS;
	}

	/**
	 * Returns a path to category in single offer projection API response JSON.
	 *
	 * @return a path to category in single offer projection API response JSON.
	 */
	public static String getCategoryPath() {
		return CATEGORIES + "." + PATH + FIRST;
	}

	/**
	 * Returns a path to translation details name in single offer projection API response JSON.
	 *
	 * @param language language of the translation details.
	 * @return a path to translation details name in single offer projection API response JSON.
	 */
	public static String getTranslationDetailsNamePath(final String language) {
		return getTranslationPath(language) + DETAILS + "." + NAME;
	}

	/**
	 * Returns a path to translation details display name in single offer projection API response JSON.
	 *
	 * @param language language of the translation details.
	 * @return a path to translation details display name in single offer projection API response JSON.
	 */
	public static String getTranslationDetailsDisplayNamePath(final String language) {
		return getTranslationPath(language) + DETAILS + "." + DISPLAY_NAME;
	}

	/**
	 * Returns a path to associations list offer in single offer projection API response JSON.
	 *
	 * @param nameList name of the association list.
	 * @return a path to associations list offer in single offer projection API response JSON.
	 */
	public static String getAssociationsListOfferPath(final String nameList) {
		return ASSOCIATIONS + ".find{it.type == \"" + nameList + POINT + LIST_OFFER;
	}

	/**
	 * Get a path to categories offer in single offer projection API response JSON.
	 *
	 * @return a path to categories offer in single offer projection API response JSON.
	 */
	public static String getCategoriesPath() {
		return CATEGORIES_PATH + ".sum()";
	}

	/**
	 * Get a path to categories default values in single offer projection API response JSON.
	 *
	 * @return a path to categories default values in single offer projection API response JSON.
	 */
	public static String getCategoriesDefault() {
		return CATEGORIES_DEFAULT;
	}

	/**
	 * Get a path to categories codes in single offer projection API response JSON.
	 *
	 * @return a path to categories codes in single offer projection API response JSON.
	 */
	public static String getCategoriesCode() {
		return CATEGORIES_CODE;
	}

	/**
	 * Get a path to categories disable dateTime in single offer projection API response JSON.
	 *
	 * @return a path to categories disable dateTime in single offer projection API response JSON.
	 */
	public static String getCategoriesDisableDateTime() {
		return CATEGORIES_DISABLE_DATE_TIME;
	}

	/**
	 * Get a path to categories enable dateTime in single offer projection API response JSON.
	 *
	 * @return a path to categories enable dateTime in single offer projection API response JSON.
	 */
	public static String getCategoriesEnableDateTime() {
		return CATEGORIES_ENABLE_DATE_TIME;
	}

	/**
	 * Get a path to categories featured values in single offer projection API response JSON.
	 *
	 * @return a path to categories featured values in single offer projection API response JSON.
	 */
	public static String getCategoriesFeatured() {
		return CATEGORIES_FEATURED;
	}

	/**
	 * Get a path to  release dateTime in single offer projection API response JSON.
	 *
	 * @return a path to release dateTime in single offer projection API response JSON.
	 */
	public static String getReleaseDateTimePath() {
		return AVAILABILITY_RULES + "." + RELEASE_DATE_TIME;
	}

	/**
	 * Get a path to translation option display name  in single offer projection API response JSON.
	 *
	 * @param language language of the translation options.
	 * @return a path to translation option display name  in single offer projection API response JSON.
	 */
	public static String getOptionsDisplayNamePath(final String language) {
		return getTranslationPath(language) + OPTIONS + "." + DISPLAY_NAME;
	}

	/**
	 * Get a path to translation option name in single offer projection API response JSON.
	 *
	 * @return a path to translation option name in single offer projection API response JSON.
	 */
	/*public static String getTranslationOptionsNamePath(final String language) {
		return getTranslationPath(language) + OPTIONS + "." + NAME;
	}*/

	/**
	 * Get a path to sku items code in single offer projection API response JSON.
	 *
	 * @return a path to sku items code  in single offer projection API response JSON.
	 */
	public static String getSkuItemCodesPath() {
		return ITEMS + "." + ITEM_CODE;
	}

	/**
	 * Get a path to item in single offer projection API response JSON.
	 *
	 * @param code code of the item.
	 * @return a path to item in single offer projection API response JSON.
	 */
	public static String getSkuItemByCodePath(final String code) {
		return "items.find{it.itemCode == \"" + code + POINT;
	}

	/**
	 * Get a path to translation options in single offer projection API response JSON.
	 *
	 * @param language language of the translation options.
	 * @return a path to translation options in single offer projection API response JSON.
	 */
	public static String getTranslationsOptionsPath(final String language) {
		return TRANSLATIONS + ".find{it.language == \"" + language + POINT + OPTIONS;
	}

	/**
	 * Get a path to translation details in single offer projection API response JSON.
	 *
	 * @param language language of the translation details.
	 * @return a path to translation details in single offer projection API response JSON.
	 */
	public static String getTranslationsDetailsPath(final String language) {
		return TRANSLATIONS + ".find{it.language == \"" + language + POINT + DETAILS;
	}
}
