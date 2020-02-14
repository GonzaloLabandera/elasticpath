/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.definitions.api.offer;

import static com.elasticpath.definitions.testobjects.SingleOfferApiResponse.FIRST;
import static com.elasticpath.definitions.utils.DataHelper.getFormatDate;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

import java.io.IOException;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.hamcrest.Matcher;

import com.elasticpath.definitions.api.helpers.Constants;
import com.elasticpath.definitions.api.helpers.StepsHelper;
import com.elasticpath.definitions.stateobjects.Context;
import com.elasticpath.definitions.testobjects.SingleOfferApiResponse;
import com.elasticpath.definitions.utils.DataHelper;
import com.elasticpath.selenium.domainobjects.Attribute;
import com.elasticpath.selenium.domainobjects.Brand;
import com.elasticpath.selenium.domainobjects.Category;
import com.elasticpath.selenium.domainobjects.Product;
import com.elasticpath.selenium.domainobjects.ProductSkuItem;
import com.elasticpath.selenium.domainobjects.ProductType;
import com.elasticpath.selenium.domainobjects.SkuOption;
import com.elasticpath.selenium.domainobjects.Store;
import com.elasticpath.selenium.domainobjects.containers.AttributeContainer;
import com.elasticpath.selenium.domainobjects.containers.CategoryContainer;
import com.elasticpath.selenium.domainobjects.containers.ProductContainer;
import com.elasticpath.selenium.domainobjects.containers.SkuOptionContainer;
import com.elasticpath.selenium.domainobjects.jsonobjects.Detail;
import com.elasticpath.selenium.domainobjects.jsonobjects.Option;
import com.elasticpath.selenium.util.Utility;

/**
 * Syndication API Offer steps.
 */
@SuppressWarnings({"PMD.GodClass", "PMD.TooManyMethods", "PMD.TooManyFields", "PMD.ExcessiveClassLength", "PMD.ExcessiveParameterList"})
public class OfferDefinition {
	private static final Logger LOG = Logger.getLogger(OfferDefinition.class);
	private static final String NOT_SOLD_SEPARATELY = "NOT_SOLD_SEPARATELY";
	private static final String MINIMUM_ORDER_QUANTITY = "MINIMUM_ORDER_QUANTITY";
	private static final String OFFER_TYPE = "OFFER_TYPE";
	private static final String ITEM_TYPE = "ITEM_TYPE";
	private static final String TAX_CODE = "TAX_CODE";
	private static final String EN_LANG = "en";
	private static final String FR_LANG = "fr";
	private static final String WEIGHT = "weight";
	private static final String WIDTH = "width";
	private static final String LENGTH = "length";
	private static final String HEIGHT = "height";
	private static final String UNITS_WEIGHT = "unitsWeight";
	private static final String UNITS_LENGTH = "unitsLength";
	private static final String ENGLISH = "English";
	private static final String FRENCH = "French";
	private static final String CAN_VIEW_STRING = "canView";
	private static final String CAN_DISCOVER_STRING = "canDiscover";
	private static final String CAN_ADD_TO_CART_STRING = "canAddToCart";
	private static final String QUANTITY = "quantity";
	private static final String SELECTION_TYPE = "selectionType";
	private static final String DISPLAY_NAME_FR_STRING = "displayNameFr";
	private static final String BRAND_NAME_FR = "brandNameFr";
	private static final String FIRST_DETAILS_NAME = "firstDetailsName";
	private static final int TEN_INT_NUMBER = 10;

	private final Store store;
	private final Context context;
	private Response response;
	private final Product product;
	private final Category category;
	private final ProductType productType;
	private final Brand brand;
	private final ProductContainer productContainer;
	private final AttributeContainer attributeContainer;
	private final CategoryContainer categoryContainer;
	private final SkuOptionContainer skuOptionContainer;
	private final ObjectMapper mapper = new ObjectMapper();

	/**
	 * Constructor.
	 *
	 * @param store              store state object.
	 * @param context            context state object.
	 * @param product            product state object.
	 * @param category           category state object.
	 * @param productType        productType state object.
	 * @param brand              brand state object.
	 * @param productContainer   ProductContainer object.
	 * @param attributeContainer AttributeContainer object.
	 * @param categoryContainer  CategoryContainer object.
	 * @param skuOptionContainer SkuOptionContainer object.
	 */
	@SuppressWarnings("checkstyle:parameternumber")
	public OfferDefinition(final Store store, final Context context, final Product product, final Category category, final ProductType productType,
						   final Brand brand, final ProductContainer productContainer, final AttributeContainer attributeContainer,
						   final CategoryContainer categoryContainer, final SkuOptionContainer skuOptionContainer) {
		this.context = context;
		this.response = this.context.getResponse();
		this.store = store;
		this.product = product;
		this.category = category;
		this.productType = productType;
		this.brand = brand;
		this.productContainer = productContainer;
		this.attributeContainer = attributeContainer;
		this.categoryContainer = categoryContainer;
		this.skuOptionContainer = skuOptionContainer;
	}

	/**
	 * Verifies that response contains complete or tombstone Offer projection information.
	 *
	 * @param offer offer projection.
	 */

	@Then("^Single offer API response contains complete information for (?:tombstone|) projection$")
	public void checkLatestOfferProjectionIfTombstonedOrNot(final Map<String, String> offer) {
		checkOfferProjectionMetadata(offer);

		response
				.then()
				.assertThat()
				.body(
						SingleOfferApiResponse.TYPE, equalTo(offer.get("type")),
						SingleOfferApiResponse.STORE, equalTo(offer.get("store")),
						SingleOfferApiResponse.DELETED, equalTo(Boolean.parseBoolean(offer.get("deleted"))),
						SingleOfferApiResponse.PROPERTIES, checkForNull(offer.get("properties")),
						SingleOfferApiResponse.AVAILABILITY_RULES, checkForNull(offer.get("availabilityRules")),
						SingleOfferApiResponse.SELECTION_RULES, checkForNull(offer.get("selectionRules")),
						SingleOfferApiResponse.COMPONENTS, checkForNull(offer.get("components")),
						SingleOfferApiResponse.EXTENSIONS, checkForNull(offer.get("extensions")),
						SingleOfferApiResponse.TRANSLATIONS, checkForNull(offer.get("translations")),
						SingleOfferApiResponse.CATEGORIES, checkForNull(offer.get("categories")),
						SingleOfferApiResponse.FORM_FIELDS, checkForNull(offer.get("formFields"))
				);
	}

	private Matcher<Object> checkForNull(final String value) {
		return "excluded".equalsIgnoreCase(value)
				? equalTo(null)
				: notNullValue();
	}

	/**
	 * Verifies that response contains complete offer projection information for a case.
	 * when JSON contains information about 2 languages.
	 *
	 * @param offer offer projection expected values.
	 */
	@Then("^Single offer API response contains complete information for product projection with 2 languages$")
	public void checkLatestOfferProjectionTwoLang(final Map<String, String> offer) {
		checkOfferProjectionMetadata(offer);
		checkOfferProjectionShippingProperty(offer);
		checkOfferProjectionProperty(offer);
		final String offerType = getProductTypeCodeByPartialCode(offer.get(OFFER_TYPE));
		final String selectionType = offer.get(SELECTION_TYPE);
		final int quantity = Integer.parseInt(offer.get(QUANTITY));
		final String canDiscover = offer.get(CAN_DISCOVER_STRING);
		final String canView = offer.get(CAN_VIEW_STRING);
		final String canAddToCart = offer.get(CAN_ADD_TO_CART_STRING);
		final String offerNameEn = offer.get("displayNameEn") + "-" + product.getProductCode();
		final String offerNameFr = offer.get(DISPLAY_NAME_FR_STRING) + "-" + product.getProductCode();
		final String brandNameEn = getBrandCodeByPartialCode(offer.get("brandNameEn"));
		final String brandNameFr = getBrandCodeByPartialCode(offer.get(BRAND_NAME_FR));
		final String itemCode = getSkuCodeByPartialCode(offer.get("itemCode"));
		final String itemType = getSkuCodeByPartialCode(offer.get(ITEM_TYPE));
		final String taxCode = getSkuCodeByPartialCode(offer.get(TAX_CODE));
		final ZonedDateTime enableDateTime = DataHelper.convertToZonedDateTime(product.getEnableDateTime());
		final String categoryEnableDate = Optional
				.ofNullable(offer.get("categoryEnableDateTime"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> category.getEnableDateTime());
		final ZonedDateTime categoryEnableDateTime = DataHelper.convertStringToZonedDateTime(categoryEnableDate);
		final String categoryDisableDate = Optional
				.ofNullable(offer.get("categoryDisableDateTime"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> category.getDisableDateTime());
		final ZonedDateTime categoryDisableDateTime = DataHelper.convertStringToZonedDateTime(categoryDisableDate);
		final List<String> cartItemModifierGroups = productType.getCartItemModifierGroup();

		response
				.then()
				.assertThat()
				.body(
						SingleOfferApiResponse.getEnableDateTimePath(), startsWith(getSimpleDate(enableDateTime)),
						SingleOfferApiResponse.getItemsEnableDateTimePath(),
						startsWith(getSimpleDate(DataHelper.convertToZonedDateTime(new Date()))),
						SingleOfferApiResponse.getDisableDateTimePath(), nullValue(),
						SingleOfferApiResponse.getPropertyValuePath(OFFER_TYPE), equalTo(offerType),
						SingleOfferApiResponse.getSelectionTypePath(), equalTo(selectionType),
						SingleOfferApiResponse.getQuantityPath(), equalTo(quantity),
						SingleOfferApiResponse.getCanDiscoverPath() + FIRST, equalTo(canDiscover),
						SingleOfferApiResponse.getCanViewPath(), equalTo(canView),
						SingleOfferApiResponse.getCanAddToCartPath() + FIRST, equalTo(canAddToCart),
						SingleOfferApiResponse.getDisplayNamePath(EN_LANG), equalTo(offerNameEn),
						SingleOfferApiResponse.getDisplayNamePath(FR_LANG), equalTo(offerNameFr),
						SingleOfferApiResponse.getBrandNamePath(EN_LANG), equalTo(brandNameEn),
						SingleOfferApiResponse.getBrandNamePath(FR_LANG), equalTo(brandNameFr),
						SingleOfferApiResponse.getBrandDisplayNamePath(FR_LANG), equalTo(brand.getName(FRENCH)),
						SingleOfferApiResponse.getBrandDisplayNamePath(EN_LANG), equalTo(brand.getName(ENGLISH)),
						SingleOfferApiResponse.getSkuCodePath(), equalTo(itemCode),
						SingleOfferApiResponse.getSkuPropertiesPath(ITEM_TYPE), equalTo(itemType),
						SingleOfferApiResponse.getSkuPropertiesPath(TAX_CODE), equalTo(taxCode),
						SingleOfferApiResponse.getItemsOptionsTranslationPath(EN_LANG), isEmptyOrNullString(),
						SingleOfferApiResponse.getItemsOptionsTranslationPath(FR_LANG), isEmptyOrNullString(),
						SingleOfferApiResponse.getItemsDetailsTranslationPath(EN_LANG), isEmptyOrNullString(),
						SingleOfferApiResponse.getItemsDetailsTranslationPath(FR_LANG), isEmptyOrNullString(),
						SingleOfferApiResponse.getCategoryCodePath(), equalTo(category.getCategoryCode()),
						SingleOfferApiResponse.getComponentsListPath(), empty(),
						SingleOfferApiResponse.getCategoryDefaultPath(), equalTo(Boolean.parseBoolean(offer.get("categoryDefault"))),
						SingleOfferApiResponse.getCategoryFeaturedPath(), isEmptyOrNullString(),
						SingleOfferApiResponse.getCategoryPath(), empty(),
						SingleOfferApiResponse.getCategoryEnableDateTimePath(), startsWith(getSimpleDate(categoryEnableDateTime)),
						SingleOfferApiResponse.getCategoryDisableDateTimePath(), startsWith(getSimpleDate(categoryDisableDateTime)),
						SingleOfferApiResponse.getOptionsTranslationPath(EN_LANG), empty(),
						SingleOfferApiResponse.getOptionsTranslationPath(FR_LANG), empty(),
						SingleOfferApiResponse.getDetailsTranslationPath(EN_LANG), empty(),
						SingleOfferApiResponse.getDetailsTranslationPath(FR_LANG), empty(),
						SingleOfferApiResponse.getFormFieldsPath(), containsInAnyOrder(cartItemModifierGroups.get(0), cartItemModifierGroups.get(1))
				);
	}

	/**
	 * Verifies that response contains complete offer projection information for a case.
	 * when JSON contains information about 2 languages.
	 *
	 * @param offer offer projection.
	 */
	@Then("^Single offer API response contains complete information for projection with 1 languages$")
	public void checkLatestOfferProjectionOneLang(final Map<String, String> offer) {
		checkOfferProjectionMetadata(offer);
		checkOfferProjectionShippingProperty(offer);
		checkOfferProjectionProperty(offer);
		final String offerType = getProductTypeCodeByPartialCode(offer.get(OFFER_TYPE));
		final String selectionType = offer.get(SELECTION_TYPE);
		final int quantity = Integer.parseInt(offer.get(QUANTITY));
		final String canDiscover = offer.get(CAN_DISCOVER_STRING);
		final String canView = offer.get(CAN_VIEW_STRING);
		final String canAddToCart = offer.get(CAN_ADD_TO_CART_STRING);
		final String offerNameFr = offer.get(DISPLAY_NAME_FR_STRING) + "-" + product.getProductCode();
		final String brandNameFr = getBrandCodeByPartialCode(offer.get(BRAND_NAME_FR));
		final String itemCode = getSkuCodeByPartialCode(offer.get("itemCode"));
		final String itemType = getSkuCodeByPartialCode(offer.get(ITEM_TYPE));
		final String taxCode = getSkuCodeByPartialCode(offer.get(TAX_CODE));
		checkOfferProjectionShippingProperty(offer);
		final ZonedDateTime enableDateTime = DataHelper.convertToZonedDateTime(product.getEnableDateTime());
		final String categoryEnableDate = Optional
				.ofNullable(offer.get("categoryEnableDateTime"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> category.getEnableDateTime());
		final ZonedDateTime categoryEnableDateTime = DataHelper.convertStringToZonedDateTime(categoryEnableDate);
		final String categoryDisableDate = Optional
				.ofNullable(offer.get("categoryDisableDateTime"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> category.getDisableDateTime());
		final ZonedDateTime categoryDisableDateTime = DataHelper.convertStringToZonedDateTime(categoryDisableDate);
		final List<String> cartItemModifierGroup = productType.getCartItemModifierGroup();
		checkOfferProjectionProperty(offer);

		response
				.then()
				.assertThat()
				.body(
						SingleOfferApiResponse.getEnableDateTimePath(), startsWith(getSimpleDate(enableDateTime)),
						SingleOfferApiResponse.getItemsEnableDateTimePath(),
						startsWith(getSimpleDate(DataHelper.convertToZonedDateTime(new Date()))),
						SingleOfferApiResponse.getDisableDateTimePath(), nullValue(),
						SingleOfferApiResponse.getPropertyValuePath(OFFER_TYPE), equalTo(offerType),
						SingleOfferApiResponse.getSelectionTypePath(), equalTo(selectionType),
						SingleOfferApiResponse.getQuantityPath(), equalTo(quantity),
						SingleOfferApiResponse.getCanDiscoverPath() + FIRST, equalTo(canDiscover),
						SingleOfferApiResponse.getCanViewPath(), equalTo(canView),
						SingleOfferApiResponse.getCanAddToCartPath() + FIRST, equalTo(canAddToCart),
						SingleOfferApiResponse.getDisplayNamePath(FR_LANG), equalTo(offerNameFr),
						SingleOfferApiResponse.getBrandNamePath(FR_LANG), equalTo(brandNameFr),
						SingleOfferApiResponse.getBrandDisplayNamePath(FR_LANG), equalTo(brand.getName(FRENCH)),
						SingleOfferApiResponse.getSkuCodePath(), equalTo(itemCode),
						SingleOfferApiResponse.getSkuPropertiesPath(ITEM_TYPE), equalTo(itemType),
						SingleOfferApiResponse.getSkuPropertiesPath(TAX_CODE), equalTo(taxCode),
						SingleOfferApiResponse.getItemsOptionsTranslationPath(FR_LANG), isEmptyOrNullString(),
						SingleOfferApiResponse.getItemsDetailsTranslationPath(FR_LANG), isEmptyOrNullString(),
						SingleOfferApiResponse.getCategoryCodePath(), equalTo(category.getCategoryCode()),
						SingleOfferApiResponse.getComponentsListPath(), empty(),
						SingleOfferApiResponse.getCategoryDefaultPath(), equalTo(Boolean.parseBoolean(offer.get("categoryDefault"))),
						SingleOfferApiResponse.getCategoryFeaturedPath(), isEmptyOrNullString(),
						SingleOfferApiResponse.getCategoryPath(), empty(),
						SingleOfferApiResponse.getCategoryEnableDateTimePath(), startsWith(getSimpleDate(categoryEnableDateTime)),
						SingleOfferApiResponse.getCategoryDisableDateTimePath(), startsWith(getSimpleDate(categoryDisableDateTime)),
						SingleOfferApiResponse.getOptionsTranslationPath(FR_LANG), empty(),
						SingleOfferApiResponse.getDetailsTranslationPath(FR_LANG), empty(),
						SingleOfferApiResponse.getFormFieldsPath(), containsInAnyOrder(cartItemModifierGroup.get(0), cartItemModifierGroup.get(1)));
	}

	/**
	 * Verifies that response contains complete offer projection information for a case.
	 * when JSON contains information about 2 languages.
	 *
	 * @param offer offer projection.
	 */
	@Then("^Single offer API response contains complete information for projection with 2 languages after update$")
	public void checkLatestOfferProjectionTwoLangAfterUpdate(final Map<String, String> offer) {
		final String canView = offer.get(CAN_VIEW_STRING);
		final String offerNameFr = offer.get(DISPLAY_NAME_FR_STRING);
		final String itemType = getSkuCodeByPartialCode(offer.get(ITEM_TYPE));
		final String taxCode = getSkuCodeByPartialCode(offer.get(TAX_CODE));
		final String detailsNameEn = getAttributeKeyByPartialCode(offer.get("detailsNameEn"));
		final String detailsNameFr = getAttributeKeyByPartialCode(offer.get("detailsNameFr"));
		final String detailsDisplayNameEn = Optional
				.ofNullable(offer.get("detailsDisplayNameEn"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> getAttributeNameByPartialCode(detailsNameEn, ENGLISH));
		final String detailsDisplayNameFr = Optional
				.ofNullable(offer.get("detailsDisplayNameFr"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> getAttributeNameByPartialCode(detailsNameFr, ENGLISH));
		final ZonedDateTime enableDateTime = DataHelper.convertToZonedDateTime(product.getEnableDateTime());
		final ZonedDateTime disableDateTime = DataHelper.convertToZonedDateTime(product.getDisableDateTime());
		checkOfferProjectionProperty(offer);
		checkOfferProjectionAssociations(offer);
		checkOfferProjectionEmptyShippingProperty();

		response
				.then()
				.assertThat()
				.body(
						SingleOfferApiResponse.getEnableDateTimePath(), startsWith(enableDateTime.toString().substring(0, TEN_INT_NUMBER)),
						SingleOfferApiResponse.getItemsEnableDateTimePath(),
						startsWith(getSimpleDate(DataHelper.convertToZonedDateTime(new Date()))),
						SingleOfferApiResponse.getDisableDateTimePath(), startsWith(disableDateTime.toString().substring(0, TEN_INT_NUMBER)),
						SingleOfferApiResponse.getCanDiscoverPath(), empty(),
						SingleOfferApiResponse.getCanViewPath(), equalTo(canView),
						SingleOfferApiResponse.getCanAddToCartPath(), empty(),
						SingleOfferApiResponse.getDisplayNamePath(FR_LANG), equalTo(offerNameFr),
						SingleOfferApiResponse.getSkuPropertiesPath(ITEM_TYPE), equalTo(itemType),
						SingleOfferApiResponse.getSkuPropertiesPath(TAX_CODE), equalTo(taxCode),
						SingleOfferApiResponse.getTranslationDetailsNamePath(EN_LANG) + FIRST, equalTo(detailsNameEn),
						SingleOfferApiResponse.getTranslationDetailsNamePath(FR_LANG) + FIRST, equalTo(detailsNameFr),
						SingleOfferApiResponse.getTranslationDetailsDisplayNamePath(EN_LANG) + FIRST, equalTo(detailsDisplayNameEn),
						SingleOfferApiResponse.getTranslationDetailsDisplayNamePath(FR_LANG) + FIRST, equalTo(detailsDisplayNameFr)
				);
	}

	/**
	 * Verifies that response contains complete offer projection information for a case.
	 * when JSON contains information about 2 languages.
	 *
	 * @param offer offer projection.
	 */
	@Then("^Single offer API response contains complete information for projection with 1 languages after update$")
	public void checkLatestOfferProjectionOneLangAfterUpdate(final Map<String, String> offer) {
		final String canView = offer.get(CAN_VIEW_STRING);
		final String offerNameFr = offer.get(DISPLAY_NAME_FR_STRING);
		final String itemType = getSkuCodeByPartialCode(offer.get(ITEM_TYPE));
		final String taxCode = getSkuCodeByPartialCode(offer.get(TAX_CODE));
		final String detailsNameFr = getAttributeKeyByPartialCode(offer.get("detailsNameFr"));
		final String detailsDisplayNameFr = Optional
				.ofNullable(offer.get("detailsDisplayNameFr"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> getAttributeNameByPartialCode(detailsNameFr, ENGLISH));
		final ZonedDateTime enableDateTime = DataHelper.convertToZonedDateTime(product.getEnableDateTime());
		final ZonedDateTime disableDateTime = DataHelper.convertToZonedDateTime(product.getDisableDateTime());
		checkOfferProjectionProperty(offer);
		checkOfferProjectionAssociations(offer);
		checkOfferProjectionEmptyShippingProperty();

		response
				.then()
				.assertThat()
				.body(
						SingleOfferApiResponse.getEnableDateTimePath(), startsWith(getSimpleDate(enableDateTime)),
						SingleOfferApiResponse.getItemsEnableDateTimePath(),
						startsWith(getSimpleDate(DataHelper.convertToZonedDateTime(new Date()))),
						SingleOfferApiResponse.getDisableDateTimePath(), startsWith(getSimpleDate(disableDateTime)),
						SingleOfferApiResponse.getCanDiscoverPath(), empty(),
						SingleOfferApiResponse.getCanViewPath(), equalTo(canView),
						SingleOfferApiResponse.getCanAddToCartPath(), empty(),
						SingleOfferApiResponse.getDisplayNamePath(FR_LANG), equalTo(offerNameFr),
						SingleOfferApiResponse.getSkuPropertiesPath(ITEM_TYPE), equalTo(itemType),
						SingleOfferApiResponse.getSkuPropertiesPath(TAX_CODE), equalTo(taxCode),
						SingleOfferApiResponse.getTranslationDetailsNamePath(FR_LANG) + FIRST, equalTo(detailsNameFr),
						SingleOfferApiResponse.getTranslationDetailsDisplayNamePath(FR_LANG) + FIRST, equalTo(detailsDisplayNameFr)
				);
	}

	/**
	 * Verifies that response contains complete offer projection information for a case.
	 * when JSON contains information about 2 languages.
	 *
	 * @param offer offer projection.
	 */
	@Then("^Single offer API response contains complete information for projection after update in merchandising association$")
	public void checkLatestOfferProjectionTwoLangAfterDelete(final Map<String, String> offer) {
		final String associationsCrossSellFirst = productContainer.getProductCodeByPartialCode(offer.get("associationsCrossSellFirst"));
		final String associationsCrossSellSecond = productContainer.getProductCodeByPartialCode(offer.get("associationsCrossSellSecond"));

		response
				.then()
				.assertThat()
				.body(
						SingleOfferApiResponse.getAssociationsListOfferPath("crosssell"), equalTo(Arrays.asList(associationsCrossSellSecond,
								associationsCrossSellFirst)),
						SingleOfferApiResponse.getAssociationsListOfferPath("associationsdependentItem"), isEmptyOrNullString(),
						SingleOfferApiResponse.getAssociationsListOfferPath("replacement"), isEmptyOrNullString()
				);
	}

	/**
	 * Verifies that response contains complete offer projection information about categories.
	 *
	 * @param offer data to comparing with.
	 * @throws ParseException while trying to convert date to projection date format.
	 */
	@And("^Single offer API response contains complete information about categories for projection with 2 languages$")
	public void checkCategoryBlockInOfferProjection(final Map<String, String> offer) throws ParseException {
		final List<Category> categoriesFromContainer = getCategoriesFromContainer(offer.get("categories.code"));
		final String[] categoryCodes = getCategoriesCodes(categoriesFromContainer);
		final String[] categoryPath = createCategoryPathByPartialCodes(offer.get("categories.path"));
		final String[] categoriesEnableDates = getCategoriesEnableDates(offer);
		final String[] categoriesDisableDates = getCategoriesDisableDates(offer);
		final Boolean[] categoryDefault = getCategoriesDefaultFromString(offer.get("categories.default"));
		final Integer[] featuredCount = getFeaturedCount(offer.get("categories.featured"));

		response
				.then()
				.assertThat()
				.body(
						SingleOfferApiResponse.getCategoriesCode(), hasItems(categoryCodes),
						SingleOfferApiResponse.getCategoriesPath(), hasItems(categoryPath),
						SingleOfferApiResponse.getCategoriesDefault(), hasItems(categoryDefault),
						SingleOfferApiResponse.getCategoriesFeatured(), hasItems(featuredCount)
				);

		final List<String> enableDateTimeList = response.then()
				.extract()
				.jsonPath()
				.getList(SingleOfferApiResponse.getCategoriesEnableDateTime(), String.class)
				.stream()
				.map(date -> getFormatDate(date, "yyyy-MM-dd'T'HH:mm:ss.SSS+00:00"))
				.collect(Collectors.toList());

		final List<String> disableDateTimeList = response.then()
				.extract()
				.jsonPath()
				.getList(SingleOfferApiResponse.getCategoriesDisableDateTime(), String.class)
				.stream()
				.map(date -> getFormatDate(date, "yyyy-MM-dd'T'HH:mm:ss.SSS+00:00"))
				.collect(Collectors.toList());

		assertThat(enableDateTimeList).contains(categoriesEnableDates);
		assertThat(disableDateTimeList).contains(categoriesDisableDates);
	}

	/**
	 * Create array of featured field values from given string. Comma is separator.
	 *
	 * @param stringWithFeaturedCount string with featured field values.
	 * @return array of featured field values.
	 */
	private Integer[] getFeaturedCount(final String stringWithFeaturedCount) {
		if (StringUtils.isEmpty(stringWithFeaturedCount)) {
			return ArrayUtils.toArray();
		}
		return Arrays.stream(stringWithFeaturedCount.split(";"))
				.filter(StringUtils::isNotEmpty)
				.map(Integer::valueOf)
				.toArray(Integer[]::new);
	}

	/**
	 * Get Categories from container. Comma is the separator.
	 *
	 * @param stringWithCodes array with Categories codes.
	 * @return list of the Categories.
	 */
	private List<Category> getCategoriesFromContainer(final String stringWithCodes) {
		return Arrays.stream(stringWithCodes.split(";"))
				.map(partialCode -> categoryContainer.getCategoryMap().get(categoryContainer.getFullCategoryNameByPartialName(partialCode)))
				.collect(Collectors.toList());
	}

	/**
	 * Get array of Categories disable dates.
	 *
	 * @param offer information about offer.
	 * @return array of Categories disable dates.
	 * @throws ParseException while trying to convert UI date to projection date.
	 */
	private String[] getCategoriesDisableDates(final Map<String, String> offer) throws ParseException {
		String disableDate = offer.get("categories.disableDateTime");
		if (DataHelper.getMultiCmUiDatePattern().matcher(disableDate).matches()) {
			List<String> dates = new ArrayList<>();
			for (String date : disableDate.split(";")) {
				dates.add(DataHelper.getProjectionDate(date));
			}
			String[] result = new String[dates.size()];
			return dates.toArray(result);
		} else {
			return Arrays.stream(disableDate.split(";"))
					.map(difference -> getFormatDate(Utility.getDateTimeWithPlus(Integer.valueOf(difference)), "MMM d, y h:mm a"))
					.toArray(String[]::new);
		}
	}

	/**
	 * Get array of Categories enable dates.
	 *
	 * @param offer information about offer.
	 * @return array of Categories enable dates.
	 * @throws ParseException if date format does not correspond to projection date format.
	 */
	private String[] getCategoriesEnableDates(final Map<String, String> offer) throws ParseException {
		String enableDate = offer.get("categories.enableDateTime");
		if (DataHelper.getMultiCmUiDatePattern().matcher(enableDate).matches()) {
			List<String> dates = new ArrayList<>();
			for (String date : enableDate.split(";")) {
				dates.add(DataHelper.getProjectionDate(date));
			}
			String[] result = new String[dates.size()];
			return dates.toArray(result);
		} else {
			return Arrays.stream(enableDate.split(";"))
					.map(difference -> getFormatDate(Utility.getDateTimeWithPlus(Integer.valueOf(difference)), "MMM d, y h:mm a"))
					.toArray(String[]::new);
		}
	}

	/**
	 * Get Categories default field from string. Comma is the separator.
	 *
	 * @param stringWithBooleanValues array with default field values.
	 * @return array with default field values.
	 */
	private Boolean[] getCategoriesDefaultFromString(final String stringWithBooleanValues) {
		return Arrays.stream(stringWithBooleanValues.split(";"))
				.map(Boolean::parseBoolean)
				.toArray(Boolean[]::new);
	}

	/**
	 * Get Categories codes array.
	 *
	 * @param categoryList list of the Categories.
	 * @return Categories codes array.
	 */
	private String[] getCategoriesCodes(final List<Category> categoryList) {
		return categoryList.stream()
				.map(Category::getCategoryCode)
				.toArray(String[]::new);
	}

	/**
	 * Get category code from container.
	 *
	 * @param categoryPartialName partial name of the category.
	 * @return code of the Category.
	 */
	private String getCategoryCodeFromCategoryContainerByPartialName(final String categoryPartialName) {
		return categoryContainer.getCategoryMap().get(categoryContainer.getFullCategoryNameByPartialName(categoryPartialName))
				.getCategoryCode();
	}

	/**
	 * Create categories array from given string. Comma is the separator.
	 *
	 * @param stringWithPartialNames string with categories partial names.
	 * @return array of Categories partial names.
	 */
	private String[] createCategoryPathByPartialCodes(final String stringWithPartialNames) {
		if (StringUtils.isEmpty(stringWithPartialNames)) {
			return ArrayUtils.toArray();
		}
		final String[] arrayWithPartialCodes = stringWithPartialNames.split(";");
		return Arrays.stream(arrayWithPartialCodes)
				.map(this::getCategoryCodeFromCategoryContainerByPartialName)
				.toArray(String[]::new);
	}

	/**
	 * Find product type code by partial code.
	 *
	 * @param partialCode product type partial code.
	 */
	private String getProductTypeCodeByPartialCode(final String partialCode) {
		return productType.getProductTypeName().startsWith(partialCode) ? productType.getProductTypeName() : null;
	}

	/**
	 * Find brand code by partial code.
	 *
	 * @param partialCode brand partial code.
	 */
	private String getBrandCodeByPartialCode(final String partialCode) {
		return brand.getCode().startsWith(partialCode) ? brand.getCode() : null;
	}

	/**
	 * Find sku code by partial code.
	 *
	 * @param partialCode sku partial code.
	 */
	private String getSkuCodeByPartialCode(final String partialCode) {
		return product.getSkuCode().startsWith(partialCode) ? product.getSkuCode() : null;
	}

	/**
	 * Find attribute name by partial code.
	 *
	 * @param partialCode attribute partial code.
	 * @param language    language.
	 */
	private String getAttributeNameByPartialCode(final String partialCode, final String language) {
		return attributeContainer.getAttributeNameByPartialCodeAndLanguage(partialCode, language);
	}

	/**
	 * Find attribute key by partial code.
	 *
	 * @param partialCode attribute partial code.
	 */
	private String getAttributeKeyByPartialCode(final String partialCode) {
		return attributeContainer.getAttributeKeyByPartialCode(partialCode);
	}

	/**
	 * Get simple date 'DD.MM.YYYY'.
	 *
	 * @param date date.
	 */
	private String getSimpleDate(final ZonedDateTime date) {
		return date.toString().substring(0, TEN_INT_NUMBER);
	}

	/**
	 * Verifies that response contains complete offer projection information except Translations block.
	 *
	 * @param offer offer projection.
	 */
	private void checkOfferProjectionMetadata(final Map<String, String> offer) {
		final String offerCode = Optional.ofNullable(offer.get("code"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(this.product::getProductCode);
		final String storeCode = Optional.ofNullable(offer.get("store"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(store::getCode);
		response
				.then()
				.assertThat()
				.body(
						SingleOfferApiResponse.TYPE, equalTo(offer.get("type")),
						SingleOfferApiResponse.CODE, equalTo(offerCode),
						SingleOfferApiResponse.STORE, equalTo(storeCode),
						SingleOfferApiResponse.MODIFIED_DATE_TIME, notNullValue(),
						SingleOfferApiResponse.DELETED, equalTo(Boolean.parseBoolean(offer.get("deleted")))
				);
	}

	/**
	 * Verifies that response contains complete offer projection associations.
	 *
	 * @param offer offer projection.
	 */
	private void checkOfferProjectionAssociations(final Map<String, String> offer) {
		final String associationsCrossSellFirst = productContainer.getProductCodeByPartialCode(offer.get("associationsCrossSellFirst"));
		final String associationsCrossSellSecond = productContainer.getProductCodeByPartialCode(offer.get("associationsCrossSellSecond"));
		final String associationsReplacementFirst = productContainer.getProductCodeByPartialCode(offer.get("associationsReplacementFirst"));
		final String associationsReplacementSecond = productContainer.getProductCodeByPartialCode(offer.get("associationsReplacementSecond"));
		response
				.then()
				.assertThat()
				.body(
						SingleOfferApiResponse.getAssociationsListOfferPath("crosssell"), equalTo(Arrays.asList(associationsCrossSellFirst,
								associationsCrossSellSecond)),
						SingleOfferApiResponse.getAssociationsListOfferPath("upsell"), isEmptyOrNullString(),
						SingleOfferApiResponse.getAssociationsListOfferPath("warranty"), isEmptyOrNullString(),
						SingleOfferApiResponse.getAssociationsListOfferPath("accessory"), isEmptyOrNullString(),
						SingleOfferApiResponse.getAssociationsListOfferPath("replacement"), equalTo(Arrays.asList(associationsReplacementFirst,
								associationsReplacementSecond))
				);
	}

	/**
	 * Verifies that response contains complete offer projection property.
	 *
	 * @param offer offer projection.
	 */
	private void checkOfferProjectionProperty(final Map<String, String> offer) {
		final String notSoldSeparatelyProperties = Optional.ofNullable(offer.get(NOT_SOLD_SEPARATELY))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(product::getNotSoldSeparately);
		final String minimumOrderQuantityProperties = Optional.ofNullable(offer.get(MINIMUM_ORDER_QUANTITY))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(product::getMinimumOrderQuantity);
		response
				.then()
				.assertThat()
				.body(
						SingleOfferApiResponse.getPropertyValuePath(NOT_SOLD_SEPARATELY), equalTo(notSoldSeparatelyProperties),
						SingleOfferApiResponse.getPropertyValuePath(MINIMUM_ORDER_QUANTITY), equalTo(minimumOrderQuantityProperties)
				);
	}

	/**
	 * Verifies that response contains complete offer projection shipping property.
	 *
	 * @param offer offer projection.
	 */
	private void checkOfferProjectionShippingProperty(final Map<String, String> offer) {
		final String weight = offer.get("propertiesWeight");
		final String width = offer.get("propertiesWidth");
		final String length = offer.get("propertiesLength");
		final String height = offer.get("propertiesHeight");
		final String unitsWeight = offer.get("propertiesUnitsWeight");
		final String unitsLength = offer.get("propertiesUnitsLength");
		response
				.then()
				.assertThat()
				.body(
						SingleOfferApiResponse.getShippingPropertiesFirstPath(WEIGHT), equalTo(Float.parseFloat(weight)),
						SingleOfferApiResponse.getShippingPropertiesFirstPath(WIDTH), equalTo(Float.parseFloat(width)),
						SingleOfferApiResponse.getShippingPropertiesFirstPath(LENGTH), equalTo(Float.parseFloat(length)),
						SingleOfferApiResponse.getShippingPropertiesFirstPath(HEIGHT), equalTo(Float.parseFloat(height)),
						SingleOfferApiResponse.getShippingPropertiesFirstPath(UNITS_WEIGHT), equalTo(unitsWeight),
						SingleOfferApiResponse.getShippingPropertiesFirstPath(UNITS_LENGTH), equalTo(unitsLength)
				);
	}

	/**
	 * Verifies that response contains complete offer projection empty shipping property.
	 */
	private void checkOfferProjectionEmptyShippingProperty() {
		response
				.then()
				.assertThat()
				.body(
						SingleOfferApiResponse.getItemsShippingPropertyPath(WEIGHT), empty(),
						SingleOfferApiResponse.getItemsShippingPropertyPath(WIDTH), empty(),
						SingleOfferApiResponse.getItemsShippingPropertyPath(LENGTH), empty(),
						SingleOfferApiResponse.getItemsShippingPropertyPath(HEIGHT), empty(),
						SingleOfferApiResponse.getItemsShippingPropertyPath(UNITS_WEIGHT), empty(),
						SingleOfferApiResponse.getItemsShippingPropertyPath(UNITS_LENGTH), empty()
				);
	}

	/**
	 * Calls API to retrieve latest version of single offer projection.
	 *
	 * @param storeCode store code which should be used to retrieve projection.
	 */
	@Then("^I retrieve latest version of created in CM offer projection for store (.+) via API$")
	public void getLatestOfferProjectionPopulatedViaCM(final String storeCode) {
		getLatestOfferProjection(storeCode, product.getProductCode());
	}

	/**
	 * Calls API to retrieve latest version of single brand projection.
	 *
	 * @param productPartialName part of Product name.
	 * @param storeCode          store code which should be used to retrieve projection.
	 */
	@When("^I retrieve latest version of created in CM offer projection for (.+) product in (.+) store via API$")
	public void getLatestBrandProjectionPopulatedViaCM(final String productPartialName, final String storeCode) {
		getLatestOfferProjection(storeCode, productContainer.getProductByPartialName(productPartialName).getProductCode());
	}

	private void getLatestOfferProjection(final String store, final String code) {
		StepsHelper.sleep(Constants.API_SLEEP_TIME);
		response = given()
				.when()
				.get(String.format(SingleOfferApiResponse.OFFER_URL, store, code));
		context.setResponse(response);
	}

	/**
	 * Verifies that response contains complete multiple sku offer projection information for a case.
	 * when JSON contains information about 2 languages.
	 *
	 * @param offer offer projection expected values.
	 */
	@Then("^Single offer API response contains complete information for projection multiple sku product with 2 languages$")
	public void checkLatestSkuOfferProjectionTwoLang(final Map<String, String> offer) {
		checkOfferProjectionMetadata(offer);
		checkOfferProjectionProperty(offer);
		final String offerType = getProductTypeCodeByPartialCode(offer.get(OFFER_TYPE));
		final String selectionType = offer.get(SELECTION_TYPE);
		final int quantity = Integer.parseInt(offer.get(QUANTITY));
		final String[] canDiscover = offer.get(CAN_DISCOVER_STRING).split(",");
		final String canView = offer.get(CAN_VIEW_STRING);
		final String[] canAddToCart = offer.get(CAN_ADD_TO_CART_STRING).split(",");
		final String offerNameEn = offer.get("displayNameEn") + "-" + product.getProductCode();
		final String offerNameFr = offer.get("displayNameFr") + "-" + product.getProductCode();
		final String brandNameEn = getBrandCodeByPartialCode(offer.get("brandNameEn"));
		final String brandNameFr = getBrandCodeByPartialCode(offer.get(BRAND_NAME_FR));
		final SkuOption firstSkuOption = skuOptionContainer.getSkuOptionByPartialCode(offer.get("firstOptionsName"));
		final SkuOption secondSkuOption = skuOptionContainer.getSkuOptionByPartialCode(offer.get("secondOptionsName"));
		final SkuOption thirdSkuOption = skuOptionContainer.getSkuOptionByPartialCode(offer.get("thirdOptionsName"));
		final String firstAttributeCode = attributeContainer.getAttributeKeyByPartialCode(offer.get(FIRST_DETAILS_NAME));
		final String secondAttributeCode = attributeContainer.getAttributeKeyByPartialCode(offer.get("secondDetailsName"));
		final ZonedDateTime enableDateTime = DataHelper.convertToZonedDateTime(product.getEnableDateTime());
		final ZonedDateTime releaseDateTime = DataHelper.convertToZonedDateTime(product.getReleaseDateTime());
		final List<String> cartItemModifierGroups = productType.getCartItemModifierGroup();
		final String firstProdSkuCode = product.getSkuOptionCodeByPartialCode(offer.get("firstProdSkuCode"));
		final String secondProdSkuCode = product.getSkuOptionCodeByPartialCode(offer.get("secondProdSkuCode"));

		response
				.then()
				.assertThat()
				.body(
						SingleOfferApiResponse.getEnableDateTimePath(), startsWith(getSimpleDate(enableDateTime)),
						SingleOfferApiResponse.getReleaseDateTimePath(), startsWith(getSimpleDate(releaseDateTime)),
						SingleOfferApiResponse.getDisableDateTimePath(), nullValue(),
						SingleOfferApiResponse.getPropertyValuePath(OFFER_TYPE), equalTo(offerType),
						SingleOfferApiResponse.getSelectionTypePath(), equalTo(selectionType),
						SingleOfferApiResponse.getQuantityPath(), equalTo(quantity),
						SingleOfferApiResponse.getCanViewPath(), equalTo(canView),
						SingleOfferApiResponse.getCanDiscoverPath(), containsInAnyOrder(canDiscover[0], canDiscover[1]),
						SingleOfferApiResponse.getCanAddToCartPath(), containsInAnyOrder(canAddToCart[0], canAddToCart[1]),
						SingleOfferApiResponse.getDisplayNamePath(EN_LANG), equalTo(offerNameEn),
						SingleOfferApiResponse.getDisplayNamePath(FR_LANG), equalTo(offerNameFr),
						SingleOfferApiResponse.getBrandNamePath(EN_LANG), equalTo(brandNameEn),
						SingleOfferApiResponse.getBrandNamePath(FR_LANG), equalTo(brandNameFr),
						SingleOfferApiResponse.getBrandDisplayNamePath(FR_LANG), equalTo(brand.getName(FRENCH)),
						SingleOfferApiResponse.getBrandDisplayNamePath(EN_LANG), equalTo(brand.getName(ENGLISH)),
						SingleOfferApiResponse.getComponentsListPath(), empty(),
						SingleOfferApiResponse.getOptionsNamePath(EN_LANG), containsInAnyOrder(firstSkuOption.getCode(), secondSkuOption.getCode(),
								thirdSkuOption.getCode()),
						SingleOfferApiResponse.getOptionsNamePath(FR_LANG), containsInAnyOrder(firstSkuOption.getCode(), secondSkuOption.getCode(),
								thirdSkuOption.getCode()),
						SingleOfferApiResponse.getOptionsDisplayNamePath(EN_LANG), containsInAnyOrder(firstSkuOption.getName(ENGLISH),
								secondSkuOption.getName(ENGLISH), thirdSkuOption.getName(ENGLISH)),
						SingleOfferApiResponse.getOptionsDisplayNamePath(FR_LANG), containsInAnyOrder(firstSkuOption.getName(FRENCH),
								secondSkuOption.getName(FRENCH), thirdSkuOption.getName(FRENCH)),
						SingleOfferApiResponse.getDetailsNamePath(EN_LANG), containsInAnyOrder(firstAttributeCode, secondAttributeCode),
						SingleOfferApiResponse.getDetailsNamePath(FR_LANG), containsInAnyOrder(firstAttributeCode, secondAttributeCode),
						SingleOfferApiResponse.getDetailsDisplayNamePath(EN_LANG),
						containsInAnyOrder(attributeContainer.getAttributeNameByPartialCodeAndLanguage(firstAttributeCode, ENGLISH),
								attributeContainer.getAttributeNameByPartialCodeAndLanguage(secondAttributeCode, ENGLISH)),
						SingleOfferApiResponse.getDetailsDisplayNamePath(FR_LANG),
						containsInAnyOrder(attributeContainer.getAttributeNameByPartialCodeAndLanguage(firstAttributeCode, FRENCH),
								attributeContainer.getAttributeNameByPartialCodeAndLanguage(secondAttributeCode, FRENCH)),
						SingleOfferApiResponse.getFormFieldsPath(), containsInAnyOrder(cartItemModifierGroups.get(0), cartItemModifierGroups.get(1)),
						SingleOfferApiResponse.getSkuItemCodesPath(), containsInAnyOrder(firstProdSkuCode, secondProdSkuCode)

				);
	}

	/**
	 * Verifies that response contains complete multiple sku offer projection information for a case.
	 * when JSON contains information about 1 languages.
	 *
	 * @param offer offer projection expected values.
	 */
	@Then("^Single offer API response contains complete information for projection multiple sku product with 1 languages$")
	public void checkLatestSkuOfferProjectionOneLang(final Map<String, String> offer) {
		checkOfferProjectionMetadata(offer);
		checkOfferProjectionProperty(offer);
		final String offerType = getProductTypeCodeByPartialCode(offer.get(OFFER_TYPE));
		final String selectionType = offer.get(SELECTION_TYPE);
		final int quantity = Integer.parseInt(offer.get(QUANTITY));
		final String[] canDiscover = offer.get(CAN_DISCOVER_STRING).split(",");
		final String canView = offer.get(CAN_VIEW_STRING);
		final String[] canAddToCart = offer.get(CAN_ADD_TO_CART_STRING).split(",");
		final String offerNameFr = offer.get("displayNameFr") + "-" + product.getProductCode();
		final String brandNameFr = getBrandCodeByPartialCode(offer.get(BRAND_NAME_FR));
		final SkuOption firstSkuOption = skuOptionContainer.getSkuOptionByPartialCode(offer.get("firstOptionsName"));
		final SkuOption secondSkuOption = skuOptionContainer.getSkuOptionByPartialCode(offer.get("secondOptionsName"));
		final SkuOption thirdSkuOption = skuOptionContainer.getSkuOptionByPartialCode(offer.get("thirdOptionsName"));
		final String firstAttributeCode = attributeContainer.getAttributeKeyByPartialCode(offer.get(FIRST_DETAILS_NAME));
		final String secondAttributeCode = attributeContainer.getAttributeKeyByPartialCode(offer.get("secondDetailsName"));
		final ZonedDateTime enableDateTime = DataHelper.convertToZonedDateTime(product.getEnableDateTime());
		final ZonedDateTime releaseDateTime = DataHelper.convertToZonedDateTime(product.getReleaseDateTime());
		final List<String> cartItemModifierGroups = productType.getCartItemModifierGroup();
		final String firstProdSkuCode = product.getSkuOptionCodeByPartialCode(offer.get("firstProdSkuCode"));
		final String secondProdSkuCode = product.getSkuOptionCodeByPartialCode(offer.get("secondProdSkuCode"));

		response
				.then()
				.assertThat()
				.body(
						SingleOfferApiResponse.getEnableDateTimePath(), startsWith(getSimpleDate(enableDateTime)),
						SingleOfferApiResponse.getReleaseDateTimePath(), startsWith(getSimpleDate(releaseDateTime)),
						SingleOfferApiResponse.getDisableDateTimePath(), nullValue(),
						SingleOfferApiResponse.getPropertyValuePath(OFFER_TYPE), equalTo(offerType),
						SingleOfferApiResponse.getSelectionTypePath(), equalTo(selectionType),
						SingleOfferApiResponse.getQuantityPath(), equalTo(quantity),
						SingleOfferApiResponse.getCanViewPath(), equalTo(canView),
						SingleOfferApiResponse.getCanDiscoverPath(), containsInAnyOrder(canDiscover[0], canDiscover[1]),
						SingleOfferApiResponse.getCanAddToCartPath(), containsInAnyOrder(canAddToCart[0], canAddToCart[1]),
						SingleOfferApiResponse.getDisplayNamePath(FR_LANG), equalTo(offerNameFr),
						SingleOfferApiResponse.getBrandNamePath(FR_LANG), equalTo(brandNameFr),
						SingleOfferApiResponse.getBrandDisplayNamePath(FR_LANG), equalTo(brand.getName(FRENCH)),
						SingleOfferApiResponse.getComponentsListPath(), empty(),
						SingleOfferApiResponse.getOptionsNamePath(FR_LANG), containsInAnyOrder(firstSkuOption.getCode(), secondSkuOption.getCode(),
								thirdSkuOption.getCode()),
						SingleOfferApiResponse.getOptionsDisplayNamePath(FR_LANG), containsInAnyOrder(firstSkuOption.getName(FRENCH),
								secondSkuOption.getName(FRENCH), thirdSkuOption.getName(FRENCH)),
						SingleOfferApiResponse.getDetailsNamePath(FR_LANG), containsInAnyOrder(firstAttributeCode, secondAttributeCode),
						SingleOfferApiResponse.getDetailsDisplayNamePath(FR_LANG),
						containsInAnyOrder(attributeContainer.getAttributeNameByPartialCodeAndLanguage(firstAttributeCode, FRENCH),
								attributeContainer.getAttributeNameByPartialCodeAndLanguage(secondAttributeCode, FRENCH)),
						SingleOfferApiResponse.getFormFieldsPath(), containsInAnyOrder(cartItemModifierGroups.get(0), cartItemModifierGroups.get(1)),
						SingleOfferApiResponse.getSkuItemCodesPath(), containsInAnyOrder(firstProdSkuCode, secondProdSkuCode)
				);
	}

	/**
	 * Verifies that response contains complete item information for a case.
	 * when JSON contains information about 2 languages.
	 *
	 * @param code productSkuCode.
	 * @param item item projection expected values.
	 */
	@Then("^Single offer API response contains complete information for item with code (.+) in product projection with 2 languages$")
	public void checkLatestSkuOfferProjectionItemsTwoLangAfterUpdate(final String code, final Map<String, String> item) {
		final String productSkuCode = product.getProductSkuOptionItemByPartialCode(code);
		final String itemPath = SingleOfferApiResponse.getSkuItemByCodePath(productSkuCode);
		checkOptions(item, ENGLISH, itemPath + SingleOfferApiResponse.getTranslationsOptionsPath("en"));
		checkOptions(item, FRENCH, itemPath + SingleOfferApiResponse.getTranslationsOptionsPath("fr"));
		checkDetails(item, ENGLISH, itemPath + SingleOfferApiResponse.getTranslationsDetailsPath("en"));
		checkDetails(item, FRENCH, itemPath + SingleOfferApiResponse.getTranslationsDetailsPath("fr"));
		checkOfferProjectionItemsShippingProperty(item, productSkuCode);
		checkOfferProjectionItemsProperty(item, productSkuCode);
		checkOfferProjectionItemsDateTimes(productSkuCode);
	}

	/**
	 * Verifies that response contains complete item information for a case.
	 * when JSON contains information about 1 languages.
	 *
	 * @param code productSkuCode.
	 * @param item item projection expected values.
	 */
	@Then("^Single offer API response contains complete information for item with code (.+) in product projection with 1 languages$")
	public void checkLatestSkuOfferProjectionItemsOneLang(final String code, final Map<String, String> item) {
		final String productSkuCode = product.getProductSkuOptionItemByPartialCode(code);
		final String itemPath = SingleOfferApiResponse.getSkuItemByCodePath(productSkuCode);
		checkOptions(item, FRENCH, itemPath + SingleOfferApiResponse.getTranslationsOptionsPath("fr"));
		checkDetails(item, FRENCH, itemPath + SingleOfferApiResponse.getTranslationsDetailsPath("fr"));
		checkOfferProjectionItemsShippingProperty(item, productSkuCode);
		checkOfferProjectionItemsProperty(item, productSkuCode);
		checkOfferProjectionItemsDateTimes(productSkuCode);
	}

	/**
	 * Verifies that response contains complete product sku codes information for a case.
	 * when JSON contains information about 2 languages.
	 *
	 * @param codes codes contained sku.
	 */
	@Then("^Single offer API response contains product sku codes for projection multiple sku product$")
	public void checkLatestSkuOfferProjectionProductSkuCodes(final Map<String, String> codes) {
		final String firstProdSkuCode = product.getSkuOptionCodeByPartialCode(codes.get("firstProdSkuCode"));
		final String thirdProdSkuCode = product.getSkuOptionCodeByPartialCode(codes.get("thirdProdSkuCode"));
		response
				.then()
				.assertThat()
				.body(

						SingleOfferApiResponse.getSkuItemCodesPath(), containsInAnyOrder(firstProdSkuCode, thirdProdSkuCode)
				);
	}


	/**
	 * Verifies that response contains complete item information for a case.
	 * when JSON contains information about 2 languages.
	 *
	 * @param code productSkuCode.
	 * @param item item projection expected values.
	 */
	@Then("^Single offer API response contains complete information for item with code (.+) with 2 languages after update sku options$")
	public void checkLatestSkuOfferProjectionItemsTwoLang(final String code, final Map<String, String> item) {
		final String productSkuCode = product.getProductSkuOptionItemByPartialCode(code);
		final String itemPath = SingleOfferApiResponse.getSkuItemByCodePath(productSkuCode);

		checkOneDetail(item, ENGLISH, itemPath + SingleOfferApiResponse.getTranslationsDetailsPath("en"));
		checkOneDetail(item, FRENCH, itemPath + SingleOfferApiResponse.getTranslationsDetailsPath("fr"));

		checkOfferProjectionItemsShippingProperty(item, productSkuCode);
		checkOfferProjectionItemsProperty(item, productSkuCode);
		checkOfferProjectionItemsDateTimes(productSkuCode);
	}

	/**
	 * Verifies that response contains complete item information for a case.
	 * when JSON contains information about 1 languages.
	 *
	 * @param code productSkuCode.
	 * @param item item projection expected values.
	 */
	@Then("^Single offer API response contains complete information for item with code (.+) with 1 languages after update sku options$")
	public void checkLatestSkuOfferProjectionItemsOneLangAfterUpdate(final String code, final Map<String, String> item) {
		final String productSkuCode = product.getProductSkuOptionItemByPartialCode(code);
		final String itemPath = SingleOfferApiResponse.getSkuItemByCodePath(productSkuCode);
		checkOneDetail(item, FRENCH, itemPath + SingleOfferApiResponse.getTranslationsDetailsPath("fr"));

		checkOfferProjectionItemsShippingProperty(item, productSkuCode);
		checkOfferProjectionItemsProperty(item, productSkuCode);
		checkOfferProjectionItemsDateTimes(productSkuCode);
	}

	/**
	 * Verifies that response contains empty items list in multiple sku offer projection information for a case.
	 * when JSON contains information.
	 *
	 * @param offer offer projection expected values.
	 */
	@Then("^Single offer API response contains empty items for multiple sku product projection without sku option$")
	public void checkLatestSkuOfferProjectionEmptyItemsOneLangAfterUpdate(final Map<String, String> offer) {
		response
				.then()
				.assertThat()
				.body(
						"items", empty()
				);
	}

	/**
	 * Verifies that response contains complete offer projection shipping property.
	 *
	 * @param offer          offer projection expected values.
	 * @param productSkuCode sku code of product.
	 */
	private void checkOfferProjectionItemsShippingProperty(final Map<String, String> offer, final String productSkuCode) {
		final Float weight = checkNullValue(offer.get(WEIGHT));
		final Float width = checkNullValue(offer.get(WIDTH));
		final Float length = checkNullValue(offer.get(LENGTH));
		final Float height = checkNullValue(offer.get(HEIGHT));
		final String unitsWeight = Optional.ofNullable(offer.get(UNITS_WEIGHT))
				.filter(StringUtils::isNotEmpty)
				.orElse(null);
		final String unitsLength = Optional.ofNullable(offer.get(UNITS_LENGTH))
				.filter(StringUtils::isNotEmpty)
				.orElse(null);
		response
				.then()
				.assertThat()
				.body(
						SingleOfferApiResponse.getSkuItemByCodePath(productSkuCode) + SingleOfferApiResponse.getShippingPropertyPath(WEIGHT),
						equalTo(weight),
						SingleOfferApiResponse.getSkuItemByCodePath(productSkuCode) + SingleOfferApiResponse.getShippingPropertyPath(WIDTH),
						equalTo(width),
						SingleOfferApiResponse.getSkuItemByCodePath(productSkuCode) + SingleOfferApiResponse.getShippingPropertyPath(HEIGHT),
						equalTo(height),
						SingleOfferApiResponse.getSkuItemByCodePath(productSkuCode) + SingleOfferApiResponse.getShippingPropertyPath(LENGTH),
						equalTo(length),
						SingleOfferApiResponse.getSkuItemByCodePath(productSkuCode) + SingleOfferApiResponse.getShippingPropertyPath(UNITS_WEIGHT),
						equalTo(unitsWeight),
						SingleOfferApiResponse.getSkuItemByCodePath(productSkuCode) + SingleOfferApiResponse.getShippingPropertyPath(UNITS_LENGTH),
						equalTo(unitsLength)
				);
	}

	/**
	 * Verifies that response contains complete offer projection shipping property.
	 *
	 * @param offer          offer projection expected values.
	 * @param productSkuCode sku code of product.
	 */
	private void checkOfferProjectionItemsProperty(final Map<String, String> offer, final String productSkuCode) {
		response
				.then()
				.assertThat()
				.body(
						SingleOfferApiResponse.getSkuItemByCodePath(productSkuCode) + SingleOfferApiResponse.getPropertyValuePath(ITEM_TYPE),
						equalTo(offer.get(ITEM_TYPE)),
						SingleOfferApiResponse.getSkuItemByCodePath(productSkuCode) + SingleOfferApiResponse.getPropertyValuePath(TAX_CODE),
						equalTo(offer.get(TAX_CODE))
				);
	}

	/**
	 * Verifies that response contains complete items date times in offer projection shipping property.
	 *
	 * @param productSkuCode sku code of product.
	 */
	private void checkOfferProjectionItemsDateTimes(final String productSkuCode) {
		final ProductSkuItem productSkuOptionItem = findProductSkuOptionItemByCode(productSkuCode);
		final String enableDate = getSimpleDate(DataHelper.convertToZonedDateTime(productSkuOptionItem.getEnableDate()));
		if (productSkuOptionItem.getDisableDate() != null) {
			final String disableDate = getSimpleDate(DataHelper.convertToZonedDateTime(productSkuOptionItem.getDisableDate()));
			response
					.then()
					.assertThat()
					.body(

							SingleOfferApiResponse.getSkuItemByCodePath(productSkuCode) + SingleOfferApiResponse.getEnableDateTimePath(),
							startsWith(enableDate),
							SingleOfferApiResponse.getSkuItemByCodePath(productSkuCode) + SingleOfferApiResponse.getDisableDateTimePath(),
							startsWith(disableDate)
					);
			return;
		}
		response
				.then()
				.assertThat()
				.body(

						SingleOfferApiResponse.getSkuItemByCodePath(productSkuCode) + SingleOfferApiResponse.getEnableDateTimePath(),
						startsWith(enableDate),
						SingleOfferApiResponse.getSkuItemByCodePath(productSkuCode) + SingleOfferApiResponse.getDisableDateTimePath(),
						nullValue()
				);
	}

	/**
	 * Parse json to list of objects.
	 *
	 * @param path path in json response.
	 * @param <T>  the entity type, e.g. Option or Details.
	 * @return List<T> the entity type, List of Option or Details.
	 */
	private <T> List<T> jsonToObjectList(final String path, final Class<T> tClass) {
		try {
			final CollectionType listType = mapper.getTypeFactory().constructCollectionType(List.class, tClass);
			return mapper.readValue(new ObjectMapper().writeValueAsString(response.getBody().jsonPath().get(path)),
					listType);
		} catch (IOException e) {
			LOG.error(e);
		}
		return null;
	}

	/**
	 * Check details for language.
	 *
	 * @param offer    offer projection expected values.
	 * @param language language.
	 * @param path     path in json response.
	 */
	private void checkDetails(final Map<String, String> offer, final String language, final String path) {
		final List<Detail> detailsList = jsonToObjectList(path, Detail.class);
		if (detailsList != null && !detailsList.isEmpty()) {
			final String firstAttributeKey = attributeContainer.getAttributeKeyByPartialCode(offer.get(FIRST_DETAILS_NAME));
			final String secondAttributeKey = attributeContainer.getAttributeKeyByPartialCode(offer.get("secondDetailsName"));
			final Attribute firstAttribute = findAttributeByKey(firstAttributeKey);
			final Attribute secondAttribute = findAttributeByKey(secondAttributeKey);
			final Detail firstDetail = findDetailsByKey(detailsList, firstAttributeKey);
			final Detail secondDetail = findDetailsByKey(detailsList, secondAttributeKey);
			assertDetailFields(firstDetail, firstAttribute, language, firstAttributeKey);
			assertDetailFields(secondDetail, secondAttribute, language, secondAttributeKey);
			return;
		}
		response
				.then()
				.assertThat()
				.body(
						path, empty()
				);

	}

	/**
	 * Assert details fields.
	 *
	 * @param details      details object.
	 * @param attribute    attribute object.
	 * @param language     language.
	 * @param attributeKey attribute key.
	 */
	private void assertDetailFields(final Detail details, final Attribute attribute, final String language, final String attributeKey) {
		assertThat(details.getName()).isEqualTo(attributeKey);
		assertThat(details.getDisplayName()).isEqualTo(attribute.getName(language));
	}

	/**
	 * Check option for language.
	 *
	 * @param offer    offer projection expected values.
	 * @param language language.
	 * @param path     path in json response.
	 */
	private void checkOptions(final Map<String, String> offer, final String language, final String path) {
		final List<Option> optionList = jsonToObjectList(path, Option.class);
		if (optionList != null && !optionList.isEmpty()) {
			final SkuOption firstSku = skuOptionContainer.getSkuOptionByPartialCode(offer.get("firstSkuCode"));
			final SkuOption secondSku = skuOptionContainer.getSkuOptionByPartialCode(offer.get("secondSkuCode"));
			final SkuOption thirdSku = skuOptionContainer.getSkuOptionByPartialCode(offer.get("thirdSkuCode"));
			final Option firstOption = findOptionByCode(optionList, firstSku.getCode());
			final Option secondOption = findOptionByCode(optionList, secondSku.getCode());
			final Option thirdOption = findOptionByCode(optionList, thirdSku.getCode());
			assertOptionFields(firstOption, firstSku, language, offer.get("firstValue"));
			assertOptionFields(secondOption, secondSku, language, offer.get("secondValue"));
			assertOptionFields(thirdOption, thirdSku, language, offer.get("thirdValue"));
		}
	}

	/**
	 * Assert option fields.
	 *
	 * @param option       option object.
	 * @param skuOption    skuOption object.
	 * @param language     language.
	 * @param skuValueCode sku value code.
	 */
	private void assertOptionFields(final Option option, final SkuOption skuOption, final String language, final String skuValueCode) {
		assertThat(option.getName()).isEqualTo(skuOption.getCode());
		assertThat(option.getDisplayName()).isEqualTo(skuOption.getName(language));
		assertThat(option.getDisplayValue())
				.isEqualTo(skuOption.getSkuOptionValueName(skuOption.getSkuOptionValueCodeByPartialCode(skuValueCode)).get(language));
		assertThat(option.getValue()).isEqualTo(skuOption.getSkuOptionValueCodeByPartialCode(skuValueCode));
	}

	/**
	 * Check details for language.
	 *
	 * @param offer    offer projection expected values.
	 * @param language language.
	 * @param path     path in json response.
	 */
	private void checkOneDetail(final Map<String, String> offer, final String language, final String path) {
		final List<Detail> detailsList = jsonToObjectList(path, Detail.class);
		if (detailsList != null && !detailsList.isEmpty()) {
			final String attributeKey = attributeContainer.getAttributeKeyByPartialCode(offer.get(FIRST_DETAILS_NAME));
			final Attribute firstAttribute = findAttributeByKey(attributeKey);
			final Detail firstDetail = findDetailsByKey(detailsList, attributeKey);
			assertDetailFields(firstDetail, firstAttribute, language, attributeKey);
		}
	}

	/**
	 * Find productSkuOptionItem by code.
	 *
	 * @param productSkuCode product sku code.
	 * @return ProductSkuItem object.
	 */
	private ProductSkuItem findProductSkuOptionItemByCode(final String productSkuCode) {
		return product.getSkuOptionList()
				.stream()
				.filter(item -> item.getItemCode().equals(productSkuCode))
				.findFirst()
				.orElse(new ProductSkuItem());
	}

	/**
	 * Find attribute by key.
	 *
	 * @param attributeKey attribute key.
	 * @return Attribute object.
	 */
	private Attribute findAttributeByKey(final String attributeKey) {
		return attributeContainer.getAttributes().stream()
				.filter(attribute -> attribute.getAttributeKey().equals(attributeKey))
				.findFirst()
				.orElse(new Attribute());
	}

	/**
	 * Find option object by code.
	 *
	 * @param optionList list of options.
	 * @param code       code for searching.
	 * @return Option domain object.
	 */
	private Option findOptionByCode(final List<Option> optionList, final String code) {
		return optionList.stream()
				.filter(option -> option.getName().equals(code))
				.findFirst()
				.orElse(new Option());
	}

	/**
	 * Find details object by key.
	 *
	 * @param detailsList  list of details.
	 * @param attributeKey attribute key for searching.
	 * @return Details domain object.
	 */
	private Detail findDetailsByKey(final List<Detail> detailsList, final String attributeKey) {
		return detailsList.stream()
				.filter(detail -> detail.getName().equals(attributeKey))
				.findFirst()
				.orElse(new Detail());
	}

	/**
	 * Check value on null.
	 *
	 * @param value String value.
	 * @return float value.
	 */
	private Float checkNullValue(final String value) {
		return Optional.of(value)
				.filter(StringUtils::isNotEmpty)
				.map(Float::parseFloat)
				.orElse(null);
	}
}
