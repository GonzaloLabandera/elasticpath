/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.definitions.api.category;

import static com.elasticpath.definitions.utils.DataHelper.getFormatDate;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.definitions.stateobjects.Context;
import com.elasticpath.definitions.testobjects.SingleCategoryApiResponse;
import com.elasticpath.definitions.utils.DataHelper;
import com.elasticpath.selenium.domainobjects.Category;
import com.elasticpath.selenium.domainobjects.Store;
import com.elasticpath.selenium.domainobjects.containers.AttributeContainer;
import com.elasticpath.selenium.domainobjects.containers.CategoryContainer;
import com.elasticpath.selenium.util.Utility;

/**
 * Syndication API Category steps.
 */
//	CHECKSTYLE:OFF: checkstyle:too many parameters
@SuppressWarnings({"PMD.GodClass", "PMD.TooManyMethods", "PMD.TooManyFields", "PMD.ExcessiveClassLength", "PMD.ExcessiveParameterList",
		"PMD.ExcessiveImports", "PMD.ExcessiveMethodLength"})
public class CategoryDefinition {
	private static final Logger LOG = LogManager.getLogger(CategoryDefinition.class);
	private static final String FIRST_LANG_KEY = "firstLanguageLocale";
	private static final String SECOND_LANG_KEY = "secondLanguageLocale";
	private static final String FIRST_LANG_FULL = "firstLanguage";
	private static final String SECOND_LANG_FULL = "secondLanguage";
	private static final String SINGLE_LANG_KEY = "languageLocale";
	private static final String SINGLE_LANG_FULL = "language";
	private static final String MESSAGE = "Could not parse date ";
	private static final String ENABLE_DATE_TIME = "enableDateTime";
	private static final String DISABLE_DATE_TIME = "disableDateTime";
	private static final String NAME = "name";
	private final Context context;
	private final Store store;
	private Response response;
	private final CategoryContainer categoryContainer;
	private final AttributeContainer attributeContainer;
	private static final String EMPTY = "empty";

	/**
	 * Constructor.
	 *
	 * @param context            context state object.
	 * @param store              store state object.
	 * @param categoryContainer  category container state object.
	 * @param attributeContainer attribute container state object.
	 */
	public CategoryDefinition(final Context context, final Store store, final CategoryContainer categoryContainer,
							  final AttributeContainer attributeContainer) {
		this.context = context;
		this.store = store;
		this.categoryContainer = categoryContainer;
		this.attributeContainer = attributeContainer;
	}

	/**
	 * Calls API to retrieve latest version of single category projection.
	 *
	 * @param categoryName category name which should be used to retrieve projection.
	 * @param storeCode    store code which should be used to retrieve projection.
	 */
	@Then("^I retrieve latest version of created in CM category projection for store (.+) and category name (.*) via API$")
	public void sub1Top2categorygetLatestCategoryProjectionPopulatedViaCM(final String storeCode, final String categoryName) {
		getLatestCategoryProjection(storeCode,
				categoryContainer.getCategoryMap().get(categoryContainer.getFullCategoryNameByPartialName(categoryName)).getCategoryCode());
	}

	/**
	 * Calls API to retrieve latest version of single category projection.
	 *
	 * @param categoryName category name which should be used to retrieve projection.
	 */
	@Then("^I retrieve latest version of created in CM category projection for created store and category code (.*) via API$")
	public void getLatestCategoryProjectionPopulatedViaCM(final String categoryName) {
		getLatestCategoryProjection(this.store.getCode(),
				categoryContainer.getCategoryMap().get(categoryContainer.getFullCategoryNameByPartialName(categoryName)).getCategoryCode());
	}

	/**
	 * Verifies that response contains complete category projection information about availability rules.
	 *
	 * @param partialName        code of Category.
	 * @param enableDisableDates dates for comparing with.
	 */
	@And("^Single category API response contains correct availability rules for projection with 2 languages and category code (.*)$")
	public void checkAvailabilityRulesOfCategoryProjection(final String partialName, final Map<String, String> enableDisableDates) {
		final String fullName = categoryContainer.getFullCategoryNameByPartialName(partialName);
		final Category category = categoryContainer.getCategoryMap().get(fullName);

		String enableDateTime =
				getFormatDate(Utility.getDateTimeWithPlus(Integer.valueOf(enableDisableDates.get("availabilityRules.enableDateTime"))),
						"MMM d, y h:mm a");
		String disableDateTime = getFormatDate(Utility.getDateTimeWithPlus(Integer.valueOf(enableDisableDates.get("availabilityRules"
				+ ".disableDateTime"))), "MMM d, y h:mm a");

		response
				.then()
				.assertThat()
				.body(SingleCategoryApiResponse.CODE, equalTo(category.getCategoryCode()),
						SingleCategoryApiResponse.getEnableDateTime(), startsWith(enableDateTime),
						SingleCategoryApiResponse.getDisableDateTime(), startsWith(disableDateTime));
	}

	/**
	 * Verifies that response contains complete tombstone category projection information for a case.
	 * when JSON contains information about 1 language.
	 *
	 * @param categoryName category Name
	 * @param categoryInfo category info.
	 */
	@And("^Single category API response contains complete information for tombstone projection with category name (.*)$")
	public void singleCategoryAPIResponseContainsCompleteInformationForTombstoneProjectionWithTwoLanguage(
			final String categoryName, final Map<String, String> categoryInfo) {
		String fullName = categoryContainer.getFullCategoryNameByPartialName(categoryName);
		checkCategoryProjectionMetadata(categoryInfo, fullName);
	}

	/**
	 * Verifies that response contains complete category projection information for a case.
	 * when JSON contains information about 2 languages.
	 *
	 * @param categoryName category Name
	 * @param categoryInfo category info.
	 * @throws ParseException if fails to parse enable\disable date.
	 */
	@And("^Single category API response contains complete information for projection with 2 languages and category code (.*)$")
	public void categoryAPIResponseCompleteInformationForProjectionWithTwoLanguage(
			final String categoryName, final Map<String, String> categoryInfo) throws ParseException {
		String fullName = categoryContainer.getFullCategoryNameByPartialName(categoryName);
		Category category = categoryContainer.getCategoryMap().get(fullName);
		String nameFirstLang = Optional.ofNullable(categoryInfo.get("displayNameFirstLang")).filter(StringUtils::isNotEmpty)
				.orElseGet(() -> category.getName(categoryInfo.get(FIRST_LANG_FULL)));
		String nameSecondLang = Optional.ofNullable(categoryInfo.get("displayNameSecondLang")).filter(StringUtils::isNotEmpty)
				.orElseGet(() -> category.getName(categoryInfo.get(SECOND_LANG_FULL)));
		String parent = Optional.ofNullable(categoryInfo.get("parent")).filter(StringUtils::isNotEmpty)
				.orElseGet(() -> categoryContainer.getCategoryMap().get(category.getParentCategory()).getCategoryCode());
		if (EMPTY.equals(parent)) {
			parent = null;
		}
		checkCategoryProjectionMetadata(categoryInfo, fullName);
		List<String> child = categoryContainer.getChildCodes(fullName);
		List<String> path = categoryContainer.getPathCodes(fullName);
		String categoryNameProperties = Optional.ofNullable(categoryInfo.get(NAME)).filter(StringUtils::isNotEmpty).orElse("");
		String categoryType = Optional.ofNullable(categoryInfo.get("value")).filter(StringUtils::isNotEmpty)
				.orElseGet(() -> category.getCategoryType());
		String enableDateTime = categoryInfo.get(ENABLE_DATE_TIME);
		String disableDateTime = categoryInfo.get(DISABLE_DATE_TIME);
		if (DataHelper.CM_UI_DATE_PATTERN.matcher(enableDateTime).matches()) {
			enableDateTime = DataHelper.getProjectionDate(enableDateTime);
		} else if (!EMPTY.equals(enableDateTime)) {
			try {
				enableDateTime = DataHelper.getProjectionDate(Utility.getDateTimeWithPlus(Integer.valueOf(enableDateTime)));
			} catch (ParseException | NumberFormatException e) {
				LOG.error(MESSAGE, e);
			}
		}
		if (DataHelper.CM_UI_DATE_PATTERN.matcher(disableDateTime).matches()) {
			disableDateTime = DataHelper.getProjectionDate(disableDateTime);
		} else if (!EMPTY.equals(disableDateTime)) {
			try {
				disableDateTime = DataHelper.getProjectionDate(Utility.getDateTimeWithPlus(Integer.valueOf(disableDateTime)));
			} catch (ParseException | NumberFormatException e) {
				LOG.error(MESSAGE, e);
			}
		}
		String firstLanguageFirstDetailDisplayName = Optional
				.ofNullable(categoryInfo.get("firstLangFirstDetailDisplayName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> attributeContainer.getAttributeNameByPartialCodeAndLanguage(
						attributeContainer.getAttributeKeyByPartialCode(category.getAttrShortTextName()), categoryInfo.get(FIRST_LANG_FULL)));
		String secondLanguageFirstDetailDisplayName = Optional
				.ofNullable(categoryInfo.get("secondLangFirstDetailDisplayName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> attributeContainer.getAttributeNameByPartialCodeAndLanguage(
						attributeContainer.getAttributeKeyByPartialCode(category.getAttrShortTextName()), categoryInfo.get(SECOND_LANG_FULL)));
		String firstLanguageSecondDetailDisplayName = Optional
				.ofNullable(categoryInfo.get("firstLangSecondDetailDisplayName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> attributeContainer.getAttributeNameByPartialCodeAndLanguage(
						attributeContainer.getAttributeKeyByPartialCode(category.getAttrLongTextName()), categoryInfo.get(FIRST_LANG_FULL)));
		String secondLanguageSecondDetailDisplayName = Optional
				.ofNullable(categoryInfo.get("secondLangSecondDetailDisplayName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> attributeContainer.getAttributeNameByPartialCodeAndLanguage(
						attributeContainer.getAttributeKeyByPartialCode(category.getAttrLongTextName()), categoryInfo.get(SECOND_LANG_FULL)));
		String firstLanguageFirstDetailName = Optional
				.ofNullable(categoryInfo.get("firstLangFirstDetailName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> attributeContainer.getAttributeKeyByPartialCode(category.getAttrShortTextName()));
		String secondLanguageFirstDetailName = Optional
				.ofNullable(categoryInfo.get("secondLangFirstDetailName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> attributeContainer.getAttributeKeyByPartialCode(category.getAttrShortTextName()));
		String firstLanguageSecondDetailName = Optional
				.ofNullable(categoryInfo.get("firstLangSecondDetailName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> attributeContainer.getAttributeKeyByPartialCode(category.getAttrLongTextName()));
		String secondLanguageSecondDetailName = Optional
				.ofNullable(categoryInfo.get("secondLangSecondDetailName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> attributeContainer.getAttributeKeyByPartialCode(category.getAttrLongTextName()));
		String firstLanguageFirstDetailValues = Optional
				.ofNullable(categoryInfo.get("firstLangFirstDetailValues"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> category.getAttrShortTextValue());
		String secondLanguageFirstDetailValues = Optional
				.ofNullable(categoryInfo.get("secondLangFirstDetailValues"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> category.getAttrShortTextValue());
		String firstLanguageSecondDetailValues = Optional
				.ofNullable(categoryInfo.get("firstLangSecondDetailValues"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> category.getAttrLongTextValue());
		String secondLanguageSecondDetailValues = Optional
				.ofNullable(categoryInfo.get("secondLangSecondDetailValues"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> category.getAttrLongTextValue());
		String firstLanguageFirstDetailDisplayValues = Optional
				.ofNullable(categoryInfo.get("firstLangFirstDetailDisplayValues"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> category.getAttrShortTextValue());
		String secondLanguageFirstDetailDisplayValues = Optional
				.ofNullable(categoryInfo.get("secondLangFirstDetailDisplayValues"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> category.getAttrShortTextValue());
		String firstLanguageSecondDetailDisplayValues = Optional
				.ofNullable(categoryInfo.get("firstLangSecondDetailDisplayValues"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> category.getAttrLongTextValue());
		String secondLanguageSecondDetailDisplayValues = Optional
				.ofNullable(categoryInfo.get("secondLangSecondDetailDisplayValues"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> category.getAttrLongTextValue());
		String[] children = new String[child.size()];
		response
				.then()
				.assertThat()
				.body(
						SingleCategoryApiResponse.getDisplayNamePath(categoryInfo.get(FIRST_LANG_KEY)), equalTo(nameFirstLang),
						SingleCategoryApiResponse.getDisplayNamePath(categoryInfo.get(SECOND_LANG_KEY)), equalTo(nameSecondLang),
						SingleCategoryApiResponse.getEnableDateTime(), startsWith(enableDateTime),
						SingleCategoryApiResponse.getChildren(), hasItems(child.toArray(children)),
						SingleCategoryApiResponse.getPath(), equalTo(path),
						SingleCategoryApiResponse.getParent(), equalTo(parent),
						SingleCategoryApiResponse.getPropertiesValue(categoryNameProperties), equalTo(categoryType),
						SingleCategoryApiResponse.getPropertiesName(categoryNameProperties), equalTo(categoryNameProperties),
						SingleCategoryApiResponse.getDetailDisplayName(categoryInfo.get(FIRST_LANG_KEY), firstLanguageSecondDetailName),
						equalTo(firstLanguageSecondDetailDisplayName),
						SingleCategoryApiResponse.getDetailDisplayName(categoryInfo.get(SECOND_LANG_KEY), secondLanguageSecondDetailName),
						equalTo(secondLanguageSecondDetailDisplayName),
						SingleCategoryApiResponse.getDetailDisplayName(categoryInfo.get(FIRST_LANG_KEY), firstLanguageFirstDetailName),
						equalTo(firstLanguageFirstDetailDisplayName),
						SingleCategoryApiResponse.getDetailDisplayName(categoryInfo.get(SECOND_LANG_KEY), secondLanguageFirstDetailName),
						equalTo(secondLanguageFirstDetailDisplayName),
						SingleCategoryApiResponse.getDetailName(categoryInfo.get(FIRST_LANG_KEY), firstLanguageSecondDetailName),
						equalTo(firstLanguageSecondDetailName),
						SingleCategoryApiResponse.getDetailName(categoryInfo.get(SECOND_LANG_KEY), secondLanguageSecondDetailName),
						equalTo(secondLanguageSecondDetailName),
						SingleCategoryApiResponse.getDetailName(categoryInfo.get(FIRST_LANG_KEY), firstLanguageFirstDetailName),
						equalTo(firstLanguageFirstDetailName),
						SingleCategoryApiResponse.getDetailName(categoryInfo.get(SECOND_LANG_KEY), secondLanguageFirstDetailName),
						equalTo(secondLanguageFirstDetailName),
						SingleCategoryApiResponse.getDetailDisplayValues(categoryInfo.get(FIRST_LANG_KEY), firstLanguageSecondDetailName),
						equalTo(Collections.singletonList(firstLanguageSecondDetailDisplayValues)),
						SingleCategoryApiResponse.getDetailDisplayValues(categoryInfo.get(SECOND_LANG_KEY), secondLanguageSecondDetailName),
						equalTo(Collections.singletonList(secondLanguageSecondDetailDisplayValues)),
						SingleCategoryApiResponse.getDetailDisplayValues(categoryInfo.get(FIRST_LANG_KEY), firstLanguageFirstDetailName),
						equalTo(Collections.singletonList(firstLanguageFirstDetailDisplayValues)),
						SingleCategoryApiResponse.getDetailDisplayValues(categoryInfo.get(SECOND_LANG_KEY), secondLanguageFirstDetailName),
						equalTo(Collections.singletonList(secondLanguageFirstDetailDisplayValues)),
						SingleCategoryApiResponse.getDetailValues(categoryInfo.get(FIRST_LANG_KEY), firstLanguageSecondDetailName),
						equalTo(Collections.singletonList(firstLanguageSecondDetailValues)),
						SingleCategoryApiResponse.getDetailValues(categoryInfo.get(SECOND_LANG_KEY), secondLanguageSecondDetailName),
						equalTo(Collections.singletonList(secondLanguageSecondDetailValues)),
						SingleCategoryApiResponse.getDetailValues(categoryInfo.get(FIRST_LANG_KEY), firstLanguageFirstDetailName),
						equalTo(Collections.singletonList(firstLanguageFirstDetailValues)),
						SingleCategoryApiResponse.getDetailValues(categoryInfo.get(SECOND_LANG_KEY), secondLanguageFirstDetailName),
						equalTo(Collections.singletonList(secondLanguageFirstDetailValues))
				);

		if (EMPTY.equals(disableDateTime)) {
			response.then().assertThat().body(
					SingleCategoryApiResponse.getDisableDateTime(), not(disableDateTime)
			);
		} else {
			response.then().assertThat().body(
					SingleCategoryApiResponse.getDisableDateTime(), startsWith(disableDateTime)
			);
		}
	}

	/**
	 * Verifies that response contains correct children information.
	 *
	 * @param categoryName category Name
	 */
	@And("^Single category API response contains correct children information for category (.*)$")
	public void singleCategoryAPIResponseChildren(final String categoryName) {
		String fullName = categoryContainer.getFullCategoryNameByPartialName(categoryName);
		List<String> children = categoryContainer.getCategoryChildrenCodes(fullName);
		assertThat(children)
				.as("there are no children saved for for specified category")
				.isNotNull();
		response
				.then()
				.assertThat()
				.body(
						SingleCategoryApiResponse.getChildren(), contains(children.toArray(new String[children.size()]))
				);
	}

	/**
	 * Verifies that response contains complete category projection information for a case.
	 * when JSON contains information about 1 language.
	 *
	 * @param categoryName category Name
	 * @param categoryInfo category info.
	 * @throws ParseException if fails to parse enable\disable date.
	 */
	@And("^Single category API response contains complete information for projection with 1 language and category code (.*)$")
	public void singleCategoryAPIResponseContainsCompleteInformationForProjectionWithOneLanguage(
			final String categoryName, final Map<String, String> categoryInfo) throws ParseException {
		String fullName = categoryContainer.getFullCategoryNameByPartialName(categoryName);
		Category category = categoryContainer.getCategoryMap().get(fullName);
		String displayName = Optional.ofNullable(categoryInfo.get("displayName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> category.getName(categoryInfo.get(SINGLE_LANG_FULL)));
		String parent = Optional.ofNullable(categoryInfo.get("parent")).filter(StringUtils::isNotEmpty)
				.orElseGet(() -> categoryContainer.getCategoryMap().get(category.getParentCategory()).getCategoryCode());
		if (EMPTY.equals(parent)) {
			parent = null;
		}
		checkCategoryProjectionMetadata(categoryInfo, fullName);
		List<String> child = categoryContainer.getChildCodes(fullName);
		String categoryNameProperties = Optional.ofNullable(categoryInfo.get(NAME))
				.filter(StringUtils::isNotEmpty)
				.orElse("");
		List<String> path = categoryContainer.getPathCodes(fullName);
		String categoryType = Optional.ofNullable(categoryInfo.get("value"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(category::getCategoryType);
		String enableDateTime = categoryInfo.get(ENABLE_DATE_TIME);
		String disableDateTime = categoryInfo.get(DISABLE_DATE_TIME);
		if (DataHelper.CM_UI_DATE_PATTERN.matcher(enableDateTime).matches()) {
			enableDateTime = DataHelper.getProjectionDate(enableDateTime);
		} else if (!EMPTY.equals(enableDateTime)) {
			try {
				enableDateTime = DataHelper.getProjectionDate(Utility.getDateTimeWithPlus(Integer.valueOf(enableDateTime)));
			} catch (ParseException | NumberFormatException e) {
				LOG.error(MESSAGE, e);
			}
		}
		if (DataHelper.CM_UI_DATE_PATTERN.matcher(disableDateTime).matches()) {
			disableDateTime = DataHelper.getProjectionDate(disableDateTime);
		} else if (!EMPTY.equals(disableDateTime)) {
			try {
				disableDateTime = DataHelper.getProjectionDate(Utility.getDateTimeWithPlus(Integer.valueOf(disableDateTime)));
			} catch (ParseException | NumberFormatException e) {
				LOG.error(MESSAGE, e);
			}
		}
		final String excludedLocale = Optional.ofNullable(categoryInfo.get("excludedLanguageLocale"))
				.filter(StringUtils::isNotEmpty)
				.orElse("");
		String firstDetailDisplayName = Optional.ofNullable(categoryInfo.get("FirstDetailDisplayName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> attributeContainer.getAttributeNameByPartialCodeAndLanguage(
						attributeContainer.getAttributeKeyByPartialCode(category.getAttrShortTextName()), categoryInfo.get(SINGLE_LANG_FULL)));
		String secondDetailDisplayName = Optional.ofNullable(categoryInfo.get("SecondDetailDisplayName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> attributeContainer.getAttributeNameByPartialCodeAndLanguage(
						attributeContainer.getAttributeKeyByPartialCode(category.getAttrLongTextName()), categoryInfo.get(SINGLE_LANG_FULL)));
		String firstDetailName = Optional.ofNullable(categoryInfo.get("FirstDetailName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> attributeContainer.getAttributeKeyByPartialCode(category.getAttrShortTextName()));
		String secondDetailName = Optional.ofNullable(categoryInfo.get("SecondDetailName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> attributeContainer.getAttributeKeyByPartialCode(category.getAttrLongTextName()));
		String firstDetailDisplayValues = Optional
				.ofNullable(categoryInfo.get("FirstDetailDisplayValues"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> category.getAttrShortTextValue());
		String secondDetailDisplayValues = Optional
				.ofNullable(categoryInfo.get("SecondDetailDisplayValues"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> category.getAttrLongTextValue());
		String firstDetailValues = Optional
				.ofNullable(categoryInfo.get("FirstDetailValues"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> category.getAttrShortTextValue());
		String secondDetailValues = Optional
				.ofNullable(categoryInfo.get("SecondDetailValues"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> category.getAttrLongTextValue());
		String[] children = new String[child.size()];
		response
				.then()
				.assertThat()
				.body(SingleCategoryApiResponse.getDisplayNamePath(categoryInfo.get(SINGLE_LANG_KEY)), equalTo(displayName),
						SingleCategoryApiResponse.getEnableDateTime(), startsWith(enableDateTime),
						SingleCategoryApiResponse.getChildren(), hasItems(child.toArray(children)),
						SingleCategoryApiResponse.getPath(), equalTo(path),
						SingleCategoryApiResponse.getParent(), equalTo(parent),
						SingleCategoryApiResponse.getPropertiesValue(categoryNameProperties), equalTo(categoryType),
						SingleCategoryApiResponse.getPropertiesName(categoryNameProperties), equalTo(categoryNameProperties),
						SingleCategoryApiResponse.getDetailDisplayName(categoryInfo.get(SINGLE_LANG_KEY), firstDetailName),
						equalTo(firstDetailDisplayName),
						SingleCategoryApiResponse.getDetailDisplayName(categoryInfo.get(SINGLE_LANG_KEY), secondDetailName),
						equalTo(secondDetailDisplayName),
						SingleCategoryApiResponse.getDetailName(categoryInfo.get(SINGLE_LANG_KEY), firstDetailName),
						equalTo(firstDetailName),
						SingleCategoryApiResponse.getDetailName(categoryInfo.get(SINGLE_LANG_KEY), secondDetailName),
						equalTo(secondDetailName),
						SingleCategoryApiResponse.getDetailDisplayValues(categoryInfo.get(SINGLE_LANG_KEY), secondDetailName),
						equalTo(Collections.singletonList(secondDetailDisplayValues)),
						SingleCategoryApiResponse.getDetailDisplayValues(categoryInfo.get(SINGLE_LANG_KEY), firstDetailName),
						equalTo(Collections.singletonList(firstDetailDisplayValues)),
						SingleCategoryApiResponse.getDetailValues(categoryInfo.get(SINGLE_LANG_KEY), secondDetailName),
						equalTo(Collections.singletonList(secondDetailValues)),
						SingleCategoryApiResponse.getDetailValues(categoryInfo.get(SINGLE_LANG_KEY), firstDetailName),
						equalTo(Collections.singletonList(firstDetailValues))
				);
		if (!"".equals(excludedLocale)) {
			response
					.then()
					.assertThat()
					.body(
							SingleCategoryApiResponse.getTranslationsLanguagePath(), not(excludedLocale)
					);
		}
		if (EMPTY.equals(disableDateTime)) {
			response.then().assertThat().body(
					SingleCategoryApiResponse.getDisableDateTime(), not(disableDateTime)
			);
		} else {
			response.then().assertThat().body(
					SingleCategoryApiResponse.getDisableDateTime(), startsWith(disableDateTime)
			);
		}
	}

	/**
	 * Verifies that response contains complete linked category projection information f	or a case.
	 * when JSON contains information about 1 language.
	 *
	 * @param categoryName category Name
	 * @param categoryInfo category info.
	 * @throws ParseException if fails to parse enable\disable date.
	 */
	@And("^Single category API response contains complete information for projection with 1 language and linked category code (.*)$")
	public void singleLinkedCategoryAPIResponseContainsCompleteInformationForProjectionWithOneLanguage(
			final String categoryName, final Map<String, String> categoryInfo) throws ParseException {
		String fullName = categoryContainer.getFullCategoryNameByPartialName(categoryName);
		Category category = categoryContainer.getCategoryMap().get(fullName);
		String displayName = Optional.ofNullable(categoryInfo.get("displayName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> category.getName(categoryInfo.get(SINGLE_LANG_FULL)));
		checkCategoryProjectionMetadata(categoryInfo, fullName);
		List<String> child = categoryContainer.getChildCodes(fullName);
		String categoryNameProperties = Optional.ofNullable(categoryInfo.get(NAME))
				.filter(StringUtils::isNotEmpty)
				.orElse("");
		String categoryType = Optional.ofNullable(categoryInfo.get("value"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(category::getCategoryType);
		String enableDateTime = null;
		String disableDateTime = null;
		if (DataHelper.CM_UI_DATE_PATTERN.matcher(categoryInfo.get(ENABLE_DATE_TIME)).matches()) {
			enableDateTime = DataHelper.getProjectionDate(categoryInfo.get(ENABLE_DATE_TIME));
		} else {
			try {
				enableDateTime = DataHelper.getProjectionDate(Utility.getDateTimeWithPlus(Integer.valueOf(categoryInfo.get(ENABLE_DATE_TIME))));
			} catch (ParseException e) {
				LOG.error(MESSAGE, e);
			}
		}
		if (DataHelper.CM_UI_DATE_PATTERN.matcher(categoryInfo.get(DISABLE_DATE_TIME)).matches()) {
			disableDateTime = DataHelper.getProjectionDate(categoryInfo.get(DISABLE_DATE_TIME));
		} else {
			try {
				disableDateTime = DataHelper.getProjectionDate(Utility.getDateTimeWithPlus(Integer.valueOf(categoryInfo.get(DISABLE_DATE_TIME))));
			} catch (ParseException e) {
				LOG.error(MESSAGE, e);
			}
		}
		String firstDetailDisplayName = Optional.ofNullable(categoryInfo.get("FirstDetailDisplayName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> attributeContainer.getAttributeNameByPartialCodeAndLanguage(
						attributeContainer.getAttributeKeyByPartialCode(category.getAttrLongTextName()), categoryInfo.get(SINGLE_LANG_FULL)));
		String secondDetailDisplayName = Optional.ofNullable(categoryInfo.get("SecondDetailDisplayName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> attributeContainer.getAttributeNameByPartialCodeAndLanguage(
						attributeContainer.getAttributeKeyByPartialCode(category.getAttrShortTextName()), categoryInfo.get(SINGLE_LANG_FULL)));
		String firstDetailName = Optional.ofNullable(categoryInfo.get("FirstDetailName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> attributeContainer.getAttributeKeyByPartialCode(category.getAttrLongTextName()));
		String secondDetailName = Optional.ofNullable(categoryInfo.get("SecondDetailName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> attributeContainer.getAttributeKeyByPartialCode(category.getAttrShortTextName()));

		response
				.then()
				.assertThat()
				.body(SingleCategoryApiResponse.getDisplayNamePath(categoryInfo.get(SINGLE_LANG_KEY)), equalTo(displayName),
						SingleCategoryApiResponse.getDisableDateTime(), startsWith(disableDateTime),
						SingleCategoryApiResponse.getEnableDateTime(), startsWith(enableDateTime),
						SingleCategoryApiResponse.getChildren(), equalTo(child),
						SingleCategoryApiResponse.getPropertiesValue(categoryNameProperties), equalTo(categoryType),
						SingleCategoryApiResponse.getPropertiesName(categoryNameProperties), equalTo(categoryNameProperties),
						SingleCategoryApiResponse.getDetailDisplayName(categoryInfo.get(SINGLE_LANG_KEY),
								attributeContainer.getAttributeKeyByPartialCode(categoryContainer.getCategoryMap()
										.get(categoryContainer.getFullCategoryNameByPartialName(categoryName)).getAttrLongTextName())),
						equalTo(firstDetailDisplayName),
						SingleCategoryApiResponse.getDetailDisplayName(categoryInfo.get(SINGLE_LANG_KEY),
								attributeContainer.getAttributeKeyByPartialCode(categoryContainer.getCategoryMap()
										.get(categoryContainer.getFullCategoryNameByPartialName(categoryName)).getAttrShortTextName())),
						equalTo(secondDetailDisplayName),
						SingleCategoryApiResponse.getDetailName(categoryInfo.get(SINGLE_LANG_KEY),
								attributeContainer.getAttributeKeyByPartialCode(categoryContainer.getCategoryMap()
										.get(categoryContainer.getFullCategoryNameByPartialName(categoryName)).getAttrLongTextName())),
						equalTo(firstDetailName),
						SingleCategoryApiResponse.getDetailName(categoryInfo.get(SINGLE_LANG_KEY),
								attributeContainer.getAttributeKeyByPartialCode(categoryContainer.getCategoryMap()
										.get(categoryContainer.getFullCategoryNameByPartialName(categoryName)).getAttrShortTextName())),
						equalTo(secondDetailName),
						SingleCategoryApiResponse.getDetailDisplayValues(categoryInfo.get(SINGLE_LANG_KEY),
								attributeContainer.getAttributeKeyByPartialCode(categoryContainer.getCategoryMap()
										.get(categoryContainer.getFullCategoryNameByPartialName(categoryName)).getAttrLongTextName())),
						equalTo(Collections.singletonList(category.getAttrLongTextValue())),
						SingleCategoryApiResponse.getDetailDisplayValues(categoryInfo.get(SINGLE_LANG_KEY),
								attributeContainer.getAttributeKeyByPartialCode(categoryContainer.getCategoryMap()
										.get(categoryContainer.getFullCategoryNameByPartialName(categoryName)).getAttrShortTextName())),
						equalTo(Collections.singletonList(category.getAttrShortTextValue())),
						SingleCategoryApiResponse.getDetailValues(categoryInfo.get(SINGLE_LANG_KEY),
								attributeContainer.getAttributeKeyByPartialCode(categoryContainer.getCategoryMap()
										.get(categoryContainer.getFullCategoryNameByPartialName(categoryName)).getAttrLongTextName())),
						equalTo(Collections.singletonList(category.getAttrLongTextValue())),
						SingleCategoryApiResponse.getDetailValues(categoryInfo.get(SINGLE_LANG_KEY),
								attributeContainer.getAttributeKeyByPartialCode(categoryContainer.getCategoryMap()
										.get(categoryContainer.getFullCategoryNameByPartialName(categoryName)).getAttrShortTextName())),
						equalTo(Collections.singletonList(category.getAttrShortTextValue()))
				);
	}

	private void getLatestCategoryProjection(final String store, final String code) {
		response = given()
				.when()
				.get(String.format(SingleCategoryApiResponse.CATEGORY_URL, store, code));
		context.setResponse(response);
	}

	private void checkCategoryProjectionMetadata(final Map<String, String> category, final String fullName) {
		Category newCategory = categoryContainer.getCategoryMap().get(categoryContainer.getFullCategoryNameByPartialName(fullName));
		String groupCode = Optional.ofNullable(category.get("code"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(newCategory::getCategoryCode);
		String storeCode = Optional.ofNullable(category.get("store"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(store::getCode);
		response
				.then()
				.assertThat()
				.body(
						SingleCategoryApiResponse.TYPE, equalTo(category.get("type")),
						SingleCategoryApiResponse.CODE, equalTo(groupCode),
						SingleCategoryApiResponse.STORE, equalTo(storeCode),
						SingleCategoryApiResponse.MODIFIED_DATE_TIME, notNullValue(),
						SingleCategoryApiResponse.DELETED, equalTo(Boolean.parseBoolean(category.get("deleted")))
				);
	}
}
