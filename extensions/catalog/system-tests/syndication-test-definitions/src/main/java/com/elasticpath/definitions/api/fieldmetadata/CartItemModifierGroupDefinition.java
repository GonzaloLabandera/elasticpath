/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.definitions.api.fieldmetadata;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.elasticpath.definitions.api.helpers.Constants;
import com.elasticpath.definitions.api.helpers.StepsHelper;
import com.elasticpath.definitions.stateobjects.Context;
import com.elasticpath.definitions.testobjects.SingleGroupApiResponse;
import com.elasticpath.selenium.domainobjects.CartItemModifierGroup;
import com.elasticpath.selenium.domainobjects.Store;


/**
 * Syndication API CartItemModifierGroup steps.
 */
public class CartItemModifierGroupDefinition {

	private static final String FIRST_LANG_LOCALE_KEY = "firstLanguageLocale";
	private static final String SECOND_LANG_LOCALE_KEY = "secondLanguageLocale";
	private static final String SINGLE_LANG_LOCALE_KEY = "languageLocale";
	private static final String FIRST_LANG_KEY = "firstLanguage";
	private static final String SECOND_LANG_KEY = "secondLanguage";
	private static final String SINGLE_LANG_KEY = "language";
	private static final String FIRST_FIELD_NAME = "FirstFieldName";
	private static final String SECOND_FIELD_NAME = "SecondFieldName";
	private static final String THIRD_FIELD_NAME = "ThirdFieldName";
	private static final String FIRST_FIELD_TYPE = "FirstFieldDataType";
	private static final String SECOND_FIELD_TYPE = "SecondFieldDataType";
	private static final String THIRD_FIELD_TYPE = "ThirdFieldDataType";
	private static final String FIRST_FIELD_MAX_SIZE = "FirstFieldMaxSize";
	private static final String SECOND_FIELD_MAX_SIZE = "SecondFieldMaxSize";
	private static final String THIRD_FIELD_MAX_SIZE = "ThirdFieldMaxSize";
	private static final String THIRD_FIELD_FIRST_VALUE = "ThirdFieldFirstValue";
	private static final String THIRD_FIELD_SECOND_VALUE = "ThirdFieldSecondValue";
	private static final String THIRD_FIELD_FIRST_DISPLAY_NAME = "ThirdFieldFirstDisplayName";
	private static final String THIRD_FIELD_SECOND_DISPLAY_NAME = "ThirdFieldSecondDisplayName";
	private final Context context;
	private final Store store;
	private final CartItemModifierGroup cartItemModifierGroup;
	private Response response;

	/**
	 * Constructor.
	 *
	 * @param context               context state object
	 * @param cartItemModifierGroup cart item modifier group state object
	 * @param store                 store state object
	 */
	public CartItemModifierGroupDefinition(final Context context, final CartItemModifierGroup cartItemModifierGroup,
										   final Store store) {
		this.cartItemModifierGroup = cartItemModifierGroup;
		this.store = store;
		this.context = context;
	}

	/**
	 * Calls API to retrieve latest version of single cart item modifier group projection.
	 *
	 * @param storeCode store code which should be used to retrieve projection.
	 */
	@Then("^I retrieve latest version of created in CM group projection for store (.+) via API$")
	public void getLatestGroupProjectionPopulatedViaCM(final String storeCode) {
		getLatestGroupProjection(storeCode, cartItemModifierGroup.getGroupCode());
	}

	/**
	 * Verifies that response contains complete group projection information for a case.
	 * when JSON contains information about 1 language.
	 *
	 * @param group group projection.
	 */
	@SuppressWarnings({"PMD.ExcessiveMethodLength", "checkstyle:methodlength"})
	@And("^Single group API response contains complete information for projection with 3 group values and 1 language$")
	public void singleGroupAPIResponseContainsCompleteInformationForProjectionWithGroupValuesAndOneLanguage(final Map<String, String> group) {
		checkGroupProjectionMetadata(group);
		String firstFieldCode = cartItemModifierGroup.getGroupFieldCodeByPartialCode(group.get(FIRST_FIELD_NAME));
		String secondFieldCode = cartItemModifierGroup.getGroupFieldCodeByPartialCode(group.get(SECOND_FIELD_NAME));
		String thirdFieldCode = cartItemModifierGroup.getGroupFieldCodeByPartialCode(group.get(THIRD_FIELD_NAME));
		Boolean firstFieldRequired = Boolean.valueOf(Optional.ofNullable(group.get("FirstFieldRequired"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> String.valueOf(cartItemModifierGroup.getFieldByCode(firstFieldCode).isFieldRequired())));
		Boolean secondFieldRequired = Boolean.valueOf(Optional.ofNullable(group.get("SecondFieldRequired"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> String.valueOf(cartItemModifierGroup.getFieldByCode(secondFieldCode).isFieldRequired())));
		Boolean thirdFieldRequired = Boolean.valueOf(Optional.ofNullable(group.get("ThirdFieldRequired"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> String.valueOf(cartItemModifierGroup.getFieldByCode(thirdFieldCode).isFieldRequired())));
		String firstFieldType = Optional.ofNullable(group.get(FIRST_FIELD_TYPE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(firstFieldCode).getFieldType());
		String secondFieldType = Optional.ofNullable(group.get(SECOND_FIELD_TYPE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(secondFieldCode).getFieldType());
		String thirdFieldType = Optional.ofNullable(group.get(THIRD_FIELD_TYPE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(thirdFieldCode).getFieldType());
		String firstShortMaxSize = Optional.ofNullable(group.get(FIRST_FIELD_MAX_SIZE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(firstFieldCode).getShortTextSize());
		String secondShortMaxSize = Optional.ofNullable(group.get(SECOND_FIELD_MAX_SIZE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(secondFieldCode).getShortTextSize());
		String thirdShortMaxSize = Optional.ofNullable(group.get(THIRD_FIELD_MAX_SIZE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(thirdFieldCode).getShortTextSize());
		String thirdFieldValuesFirstOptionName = Optional.ofNullable(group.get(THIRD_FIELD_FIRST_VALUE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> (String) cartItemModifierGroup.getFieldByCode(thirdFieldCode).getOptions().keySet().toArray()[1]);
		String thirdFieldValuesSecondOptionName = Optional.ofNullable(group.get(THIRD_FIELD_SECOND_VALUE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> (String) cartItemModifierGroup.getFieldByCode(thirdFieldCode).getOptions().keySet().toArray()[0]);
		String excludedLocale = Optional.ofNullable(group.get("excludedLanguageLocale"))
				.filter(StringUtils::isNotEmpty)
				.orElse("");
		String groupName = Optional.ofNullable(group.get("displayName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getName(group.get(SINGLE_LANG_KEY)));
		String firstFieldName = Optional.ofNullable(group.get("FirstFieldDisplayName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(firstFieldCode).getName(group.get(SINGLE_LANG_KEY)));
		String secondFieldName = Optional.ofNullable(group.get("SecondFieldDisplayName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(secondFieldCode).getName(group.get(SINGLE_LANG_KEY)));
		String thirdFieldName = Optional.ofNullable(group.get("ThirdFieldDisplayName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(thirdFieldCode).getName(group.get(SINGLE_LANG_KEY)));
		String thirdFieldValuesFirstOptionValue = Optional.ofNullable(group.get(THIRD_FIELD_FIRST_DISPLAY_NAME))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup
						.getFieldByCode(thirdFieldCode)
						.getOptions()
						.get("thirdFieldValuesFirstOptionName")
						.get(group.get(SINGLE_LANG_KEY)));
		String thirdFieldValuesSecondOptionValue = Optional.ofNullable(group.get(THIRD_FIELD_SECOND_DISPLAY_NAME))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup
						.getFieldByCode(thirdFieldCode)
						.getOptions()
						.get(thirdFieldValuesSecondOptionName)
						.get(group.get(SINGLE_LANG_KEY))
				);
		response
				.then()
				.assertThat()
				.body(
						SingleGroupApiResponse.getDisplayNamePath(group.get(SINGLE_LANG_LOCALE_KEY)), equalTo(groupName),
						SingleGroupApiResponse.getGroupValuePath(group.get(SINGLE_LANG_LOCALE_KEY)), hasItem(firstFieldCode),
						SingleGroupApiResponse.getGroupNamePath(
								group.get(SINGLE_LANG_LOCALE_KEY), firstFieldCode), equalTo(firstFieldName),
						SingleGroupApiResponse.getGroupValuePath(group.get(SINGLE_LANG_LOCALE_KEY)), hasItem(secondFieldCode),
						SingleGroupApiResponse.getGroupNamePath(
								group.get(SINGLE_LANG_LOCALE_KEY), secondFieldCode), equalTo(secondFieldName),
						SingleGroupApiResponse.getGroupValuePath(group.get(SINGLE_LANG_LOCALE_KEY)), hasItem(thirdFieldCode),
						SingleGroupApiResponse.getGroupNamePath(
								group.get(SINGLE_LANG_LOCALE_KEY), thirdFieldCode), equalTo(thirdFieldName),
						SingleGroupApiResponse.getGroupFieldDataType(
								group.get(SINGLE_LANG_LOCALE_KEY), firstFieldCode), equalTo(firstFieldType),
						SingleGroupApiResponse.getGroupFieldDataType(
								group.get(SINGLE_LANG_LOCALE_KEY), secondFieldCode), equalTo(secondFieldType),
						SingleGroupApiResponse.getGroupFieldDataType(
								group.get(SINGLE_LANG_LOCALE_KEY), thirdFieldCode), equalTo(thirdFieldType),
						SingleGroupApiResponse.groupFieldIsRequired(
								group.get(SINGLE_LANG_LOCALE_KEY), thirdFieldCode), equalTo(firstFieldRequired),
						SingleGroupApiResponse.groupFieldIsRequired(
								group.get(SINGLE_LANG_LOCALE_KEY), secondFieldCode), equalTo(secondFieldRequired),
						SingleGroupApiResponse.groupFieldIsRequired(
								group.get(SINGLE_LANG_LOCALE_KEY), thirdFieldCode), equalTo(thirdFieldRequired),
						SingleGroupApiResponse.getGroupFieldMaxSize(
								group.get(SINGLE_LANG_LOCALE_KEY), firstFieldCode), equalTo(NumberUtils.createInteger(firstShortMaxSize)),
						SingleGroupApiResponse.getGroupFieldMaxSize(
								group.get(SINGLE_LANG_LOCALE_KEY), secondFieldCode), equalTo(NumberUtils.createInteger(secondShortMaxSize)),
						SingleGroupApiResponse.getGroupFieldMaxSize(
								group.get(SINGLE_LANG_LOCALE_KEY), thirdFieldCode), equalTo(NumberUtils.createInteger(thirdShortMaxSize)),
						SingleGroupApiResponse.getGroupFieldFields(
								group.get(SINGLE_LANG_LOCALE_KEY), firstFieldCode), empty(),
						SingleGroupApiResponse.getGroupFieldFields(
								group.get(SINGLE_LANG_LOCALE_KEY), secondFieldCode), empty(),
						SingleGroupApiResponse.getGroupFieldsName(
								group.get(SINGLE_LANG_LOCALE_KEY), thirdFieldValuesFirstOptionName, thirdFieldCode),
						equalTo(thirdFieldValuesFirstOptionValue),
						SingleGroupApiResponse.getGroupFieldsName(
								group.get(SINGLE_LANG_LOCALE_KEY), thirdFieldValuesSecondOptionName, thirdFieldCode),
						equalTo(thirdFieldValuesSecondOptionValue),
						SingleGroupApiResponse.getGroupFieldsValue(
								group.get(SINGLE_LANG_LOCALE_KEY), thirdFieldValuesFirstOptionValue, thirdFieldCode),
						equalTo(thirdFieldValuesFirstOptionName),
						SingleGroupApiResponse.getGroupFieldsValue(
								group.get(SINGLE_LANG_LOCALE_KEY), thirdFieldValuesSecondOptionValue, thirdFieldCode),
						equalTo(thirdFieldValuesSecondOptionName)
				);
		if (!"".equals(excludedLocale)) {
			response
					.then()
					.assertThat()
					.body(
							SingleGroupApiResponse.getTranslationsLanguagePath(), not(hasItem(excludedLocale))
					);
		}
	}

	/**
	 * Verifies that response contains complete group projection information for a case.
	 * when JSON contains information about 2 languages.
	 *
	 * @param group group projection.
	 */
	@SuppressWarnings({"PMD.ExcessiveMethodLength", "checkstyle:methodlength"})
	@And("^Single group API response contains complete information for projection with 3 group values and 2 languages$")
	public void singleGroupAPIResponseContainsCompleteInformationForProjectionWithGroupValuesAndTwoLanguage(final Map<String, String> group) {
		checkGroupProjectionMetadata(group);
		String groupNameFirstLang = Optional.ofNullable(group.get("displayNameFirstLang"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getName(group.get(FIRST_LANG_KEY)));
		String groupNameSecondLang = Optional.ofNullable(group.get("displayNameSecondLang"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getName(group.get(SECOND_LANG_KEY)));
		String firstFieldCode = cartItemModifierGroup.getGroupFieldCodeByPartialCode(group.get(FIRST_FIELD_NAME));
		String secondFieldCode = cartItemModifierGroup.getGroupFieldCodeByPartialCode(group.get(SECOND_FIELD_NAME));
		String thirdFieldCode = cartItemModifierGroup.getGroupFieldCodeByPartialCode(group.get(THIRD_FIELD_NAME));
		Boolean firstFieldRequired = Boolean.valueOf(Optional.ofNullable(group.get("FirstFieldRequired"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> String.valueOf(cartItemModifierGroup.getFieldByCode(firstFieldCode).isFieldRequired())));
		Boolean secondFieldRequired = Boolean.valueOf(Optional.ofNullable(group.get("SecondFieldRequired"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> String.valueOf(cartItemModifierGroup.getFieldByCode(secondFieldCode).isFieldRequired())));
		Boolean thirdFieldRequired = Boolean.valueOf(Optional.ofNullable(group.get("ThirdFieldRequired"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> String.valueOf(cartItemModifierGroup.getFieldByCode(thirdFieldCode).isFieldRequired())));
		String firstFieldType = Optional.ofNullable(group.get(FIRST_FIELD_TYPE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(firstFieldCode).getFieldType());
		String secondFieldType = Optional.ofNullable(group.get(SECOND_FIELD_TYPE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(secondFieldCode).getFieldType());
		String thirdFieldType = Optional.ofNullable(group.get(THIRD_FIELD_TYPE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(thirdFieldCode).getFieldType());
		String firstShortMaxSize = Optional.ofNullable(group.get(FIRST_FIELD_MAX_SIZE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(firstFieldCode).getShortTextSize());
		String secondShortMaxSize = Optional.ofNullable(group.get(SECOND_FIELD_MAX_SIZE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(secondFieldCode).getShortTextSize());
		String thirdShortMaxSize = Optional.ofNullable(group.get(THIRD_FIELD_MAX_SIZE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(thirdFieldCode).getShortTextSize());
		String thirdFieldValuesFirstOptionName = Optional.ofNullable(group.get(THIRD_FIELD_FIRST_VALUE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> (String) cartItemModifierGroup.getFieldByCode(thirdFieldCode).getOptions().keySet().toArray()[1]);
		String thirdFieldValuesSecondOptionName = Optional.ofNullable(group.get(THIRD_FIELD_SECOND_VALUE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> (String) cartItemModifierGroup.getFieldByCode(thirdFieldCode).getOptions().keySet().toArray()[0]);
		String firstGroupFieldNameFirstLang = Optional.ofNullable(group.get("FirstFieldDisplayNameFirstLang"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(firstFieldCode).getName(group.get(FIRST_LANG_KEY)));
		String secondGroupFieldNameFirstLang = Optional.ofNullable(group.get("SecondFieldDisplayNameFirstLang"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(secondFieldCode).getName(group.get(FIRST_LANG_KEY)));
		String thirdGroupFieldNameFirstLang = Optional.ofNullable(group.get("ThirdFieldDisplayNameFirstLang"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(thirdFieldCode).getName(group.get(FIRST_LANG_KEY)));
		String firstGroupFieldNameSecondLang = Optional.ofNullable(group.get("FirstFieldDisplayNameSecondLang"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(firstFieldCode).getName(group.get(SECOND_LANG_KEY)));
		String secondGroupFieldNameSecondLang = Optional.ofNullable(group.get("SecondFieldDisplayNameSecondLang"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(secondFieldCode).getName(group.get(SECOND_LANG_KEY)));
		String thirdGroupFieldNameSecondLang = Optional.ofNullable(group.get("ThirdFieldDisplayNameSecondLang"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(thirdFieldCode).getName(group.get(SECOND_LANG_KEY)));
		String thirdFieldValuesFirstOptionValueFirstLang = Optional.ofNullable(group.get(THIRD_FIELD_FIRST_DISPLAY_NAME))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup
						.getFieldByCode(thirdFieldCode)
						.getOptions()
						.get(thirdFieldValuesFirstOptionName)
						.get(group.get(FIRST_LANG_KEY)));
		String thirdFieldValuesSecondOptionValueFirstLang = Optional.ofNullable(group.get(THIRD_FIELD_SECOND_DISPLAY_NAME))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup
						.getFieldByCode(thirdFieldCode)
						.getOptions()
						.get(thirdFieldValuesSecondOptionName)
						.get(group.get(FIRST_LANG_KEY)));
		String thirdFieldValuesFirstOptionValueSecondLang = Optional.ofNullable(group.get(THIRD_FIELD_FIRST_DISPLAY_NAME))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup
						.getFieldByCode(thirdFieldCode)
						.getOptions()
						.get(thirdFieldValuesFirstOptionName)
						.get(group.get(SECOND_LANG_KEY)));
		String thirdFieldValuesSecondOptionValueSecondLang = Optional.ofNullable(group.get(THIRD_FIELD_SECOND_DISPLAY_NAME))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup
						.getFieldByCode(thirdFieldCode)
						.getOptions()
						.get(thirdFieldValuesSecondOptionName)
						.get(group.get(SECOND_LANG_KEY)));
		response
				.then()
				.assertThat()
				.body(
						SingleGroupApiResponse.getDisplayNamePath(group.get(FIRST_LANG_LOCALE_KEY)), equalTo(groupNameFirstLang),
						SingleGroupApiResponse.getGroupValuePath(group.get(FIRST_LANG_LOCALE_KEY)), hasItem(firstFieldCode),
						SingleGroupApiResponse.getGroupNamePath(
								group.get(FIRST_LANG_LOCALE_KEY), firstFieldCode), equalTo(firstGroupFieldNameFirstLang),
						SingleGroupApiResponse.getGroupNamePath(
								group.get(FIRST_LANG_LOCALE_KEY), secondFieldCode), equalTo(secondGroupFieldNameFirstLang),
						SingleGroupApiResponse.getGroupNamePath(
								group.get(FIRST_LANG_LOCALE_KEY), thirdFieldCode), equalTo(thirdGroupFieldNameFirstLang),
						SingleGroupApiResponse.getGroupNamePath(
								group.get(SECOND_LANG_LOCALE_KEY), firstFieldCode), equalTo(firstGroupFieldNameSecondLang),
						SingleGroupApiResponse.getGroupNamePath(
								group.get(SECOND_LANG_LOCALE_KEY), secondFieldCode), equalTo(secondGroupFieldNameSecondLang),
						SingleGroupApiResponse.getGroupNamePath(
								group.get(SECOND_LANG_LOCALE_KEY), thirdFieldCode), equalTo(thirdGroupFieldNameSecondLang),
						SingleGroupApiResponse.getGroupValuePath(group.get(FIRST_LANG_LOCALE_KEY)), hasItem(secondFieldCode),
						SingleGroupApiResponse.getGroupNamePath(
								group.get(FIRST_LANG_LOCALE_KEY), secondFieldCode), equalTo(secondGroupFieldNameFirstLang),
						SingleGroupApiResponse.getGroupValuePath(group.get(FIRST_LANG_LOCALE_KEY)), hasItem(thirdFieldCode),
						SingleGroupApiResponse.getGroupNamePath(
								group.get(FIRST_LANG_LOCALE_KEY), thirdFieldCode), equalTo(thirdGroupFieldNameFirstLang),
						SingleGroupApiResponse.getGroupFieldDataType(
								group.get(FIRST_LANG_LOCALE_KEY), firstFieldCode), equalTo(firstFieldType),
						SingleGroupApiResponse.getGroupFieldDataType(
								group.get(FIRST_LANG_LOCALE_KEY), secondFieldCode), equalTo(secondFieldType),
						SingleGroupApiResponse.getGroupFieldDataType(
								group.get(FIRST_LANG_LOCALE_KEY), thirdFieldCode), equalTo(thirdFieldType),
						SingleGroupApiResponse.groupFieldIsRequired(
								group.get(FIRST_LANG_LOCALE_KEY), thirdFieldCode), equalTo(firstFieldRequired),
						SingleGroupApiResponse.groupFieldIsRequired(
								group.get(FIRST_LANG_LOCALE_KEY), secondFieldCode), equalTo(secondFieldRequired),
						SingleGroupApiResponse.groupFieldIsRequired(
								group.get(FIRST_LANG_LOCALE_KEY), thirdFieldCode), equalTo(thirdFieldRequired),
						SingleGroupApiResponse.getGroupFieldMaxSize(
								group.get(FIRST_LANG_LOCALE_KEY), firstFieldCode),
						equalTo(NumberUtils.createInteger(firstShortMaxSize)),
						SingleGroupApiResponse.getGroupFieldMaxSize(
								group.get(FIRST_LANG_LOCALE_KEY), secondFieldCode),
						equalTo(NumberUtils.createInteger(secondShortMaxSize)),
						SingleGroupApiResponse.getGroupFieldMaxSize(
								group.get(FIRST_LANG_LOCALE_KEY), thirdFieldCode),
						equalTo(NumberUtils.createInteger(thirdShortMaxSize)),
						SingleGroupApiResponse.getGroupFieldFields(
								group.get(FIRST_LANG_LOCALE_KEY), firstFieldCode), empty(),
						SingleGroupApiResponse.getGroupFieldFields(
								group.get(FIRST_LANG_LOCALE_KEY), secondFieldCode), empty(),
						SingleGroupApiResponse.getGroupFieldsName(
								group.get(FIRST_LANG_LOCALE_KEY), thirdFieldValuesFirstOptionName, thirdFieldCode),
						equalTo(thirdFieldValuesFirstOptionValueFirstLang),
						SingleGroupApiResponse.getGroupFieldsName(
								group.get(FIRST_LANG_LOCALE_KEY), thirdFieldValuesSecondOptionName, thirdFieldCode),
						equalTo(thirdFieldValuesSecondOptionValueFirstLang),
						SingleGroupApiResponse.getGroupFieldsValue(
								group.get(FIRST_LANG_LOCALE_KEY), thirdFieldValuesFirstOptionValueFirstLang, thirdFieldCode),
						equalTo(thirdFieldValuesFirstOptionName),
						SingleGroupApiResponse.getGroupFieldsValue(
								group.get(FIRST_LANG_LOCALE_KEY), thirdFieldValuesSecondOptionValueFirstLang, thirdFieldCode),
						equalTo(thirdFieldValuesSecondOptionName),
						SingleGroupApiResponse.getDisplayNamePath(group.get(SECOND_LANG_LOCALE_KEY)), equalTo(groupNameSecondLang),
						SingleGroupApiResponse.getGroupValuePath(group.get(SECOND_LANG_LOCALE_KEY)), hasItem(firstFieldCode),
						SingleGroupApiResponse.getGroupNamePath(
								group.get(SECOND_LANG_LOCALE_KEY), firstFieldCode), equalTo(firstGroupFieldNameSecondLang),
						SingleGroupApiResponse.getGroupValuePath(group.get(SECOND_LANG_LOCALE_KEY)), hasItem(secondFieldCode),
						SingleGroupApiResponse.getGroupNamePath(
								group.get(SECOND_LANG_LOCALE_KEY), secondFieldCode), equalTo(secondGroupFieldNameSecondLang),
						SingleGroupApiResponse.getGroupValuePath(group.get(SECOND_LANG_LOCALE_KEY)), hasItem(thirdFieldCode),
						SingleGroupApiResponse.getGroupNamePath(
								group.get(SECOND_LANG_LOCALE_KEY), thirdFieldCode), equalTo(thirdGroupFieldNameSecondLang),
						SingleGroupApiResponse.getGroupFieldDataType(
								group.get(SECOND_LANG_LOCALE_KEY), firstFieldCode), equalTo(firstFieldType),
						SingleGroupApiResponse.getGroupFieldDataType(
								group.get(SECOND_LANG_LOCALE_KEY), secondFieldCode), equalTo(secondFieldType),
						SingleGroupApiResponse.getGroupFieldDataType(
								group.get(SECOND_LANG_LOCALE_KEY), thirdFieldCode), equalTo(thirdFieldType),
						SingleGroupApiResponse.groupFieldIsRequired(
								group.get(SECOND_LANG_LOCALE_KEY), thirdFieldCode), equalTo(firstFieldRequired),
						SingleGroupApiResponse.groupFieldIsRequired(
								group.get(SECOND_LANG_LOCALE_KEY), secondFieldCode), equalTo(secondFieldRequired),
						SingleGroupApiResponse.groupFieldIsRequired(
								group.get(SECOND_LANG_LOCALE_KEY), thirdFieldCode), equalTo(thirdFieldRequired),
						SingleGroupApiResponse.getGroupFieldMaxSize(
								group.get(SECOND_LANG_LOCALE_KEY), firstFieldCode),
						equalTo(NumberUtils.createInteger(firstShortMaxSize)),
						SingleGroupApiResponse.getGroupFieldMaxSize(
								group.get(SECOND_LANG_LOCALE_KEY), secondFieldCode),
						equalTo(NumberUtils.createInteger(secondShortMaxSize)),
						SingleGroupApiResponse.getGroupFieldMaxSize(
								group.get(SECOND_LANG_LOCALE_KEY), thirdFieldCode),
						equalTo(NumberUtils.createInteger(thirdShortMaxSize)),
						SingleGroupApiResponse.getGroupFieldFields(
								group.get(SECOND_LANG_LOCALE_KEY), firstFieldCode), empty(),
						SingleGroupApiResponse.getGroupFieldFields(
								group.get(SECOND_LANG_LOCALE_KEY), secondFieldCode), empty(),
						SingleGroupApiResponse.getGroupFieldsName(
								group.get(SECOND_LANG_LOCALE_KEY), thirdFieldValuesFirstOptionName, thirdFieldCode),
						equalTo(thirdFieldValuesFirstOptionValueSecondLang),
						SingleGroupApiResponse.getGroupFieldsName(
								group.get(SECOND_LANG_LOCALE_KEY), thirdFieldValuesSecondOptionName, thirdFieldCode),
						equalTo(thirdFieldValuesSecondOptionValueSecondLang),
						SingleGroupApiResponse.getGroupFieldsValue(
								group.get(SECOND_LANG_LOCALE_KEY), thirdFieldValuesFirstOptionValueSecondLang, thirdFieldCode),
						equalTo(thirdFieldValuesFirstOptionName),
						SingleGroupApiResponse.getGroupFieldsValue(
								group.get(SECOND_LANG_LOCALE_KEY), thirdFieldValuesSecondOptionValueSecondLang, thirdFieldCode),
						equalTo(thirdFieldValuesSecondOptionName)
				);
	}

	/**
	 * Verifies that response contains complete group fields projection information for a case group with 3 fields and 2 languages.
	 *
	 * @param group group projection.
	 */
	@SuppressWarnings({"PMD.ExcessiveMethodLength", "checkstyle:methodlength"})
	@And("^Single group API response contains complete fields information for group with 3 fields and 2 languages$")
	public void singleGroupAPIResponseFieldsCheck(final Map<String, String> group) {
		String firstFieldCode = cartItemModifierGroup.getGroupFieldCodeByPartialCode(group.get(FIRST_FIELD_NAME));
		String secondFieldCode = cartItemModifierGroup.getGroupFieldCodeByPartialCode(group.get(SECOND_FIELD_NAME));
		String thirdFieldCode = cartItemModifierGroup.getGroupFieldCodeByPartialCode(group.get(THIRD_FIELD_NAME));
		String firstFieldType = Optional.ofNullable(group.get(FIRST_FIELD_TYPE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(firstFieldCode).getFieldType());
		String secondFieldType = Optional.ofNullable(group.get(SECOND_FIELD_TYPE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(secondFieldCode).getFieldType());
		String thirdFieldType = Optional.ofNullable(group.get(THIRD_FIELD_TYPE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(thirdFieldCode).getFieldType());
		String firstShortMaxSize = Optional.ofNullable(group.get(FIRST_FIELD_MAX_SIZE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(firstFieldCode).getShortTextSize());
		String secondShortMaxSize = Optional.ofNullable(group.get(SECOND_FIELD_MAX_SIZE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(secondFieldCode).getShortTextSize());
		String thirdShortMaxSize = Optional.ofNullable(group.get(THIRD_FIELD_MAX_SIZE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(thirdFieldCode).getShortTextSize());
		String thirdFieldValuesFirstOptionName = Optional.ofNullable(group.get(THIRD_FIELD_FIRST_VALUE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> (String) cartItemModifierGroup.getFieldByCode(thirdFieldCode).getOptions().keySet().toArray()[1]);
		String thirdFieldValuesSecondOptionName = Optional.ofNullable(group.get(THIRD_FIELD_SECOND_VALUE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> (String) cartItemModifierGroup.getFieldByCode(thirdFieldCode).getOptions().keySet().toArray()[0]);
		String firstGroupFieldNameFirstLang = Optional.ofNullable(group.get("FirstFieldDisplayNameFirstLang"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(firstFieldCode).getName(group.get(FIRST_LANG_KEY)));
		String secondGroupFieldNameFirstLang = Optional.ofNullable(group.get("SecondFieldDisplayNameFirstLang"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(secondFieldCode).getName(group.get(FIRST_LANG_KEY)));
		String thirdGroupFieldNameFirstLang = Optional.ofNullable(group.get("ThirdFieldDisplayNameFirstLang"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(thirdFieldCode).getName(group.get(FIRST_LANG_KEY)));
		String firstGroupFieldNameSecondLang = Optional.ofNullable(group.get("FirstFieldDisplayNameSecondLang"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(firstFieldCode).getName(group.get(SECOND_LANG_KEY)));
		String secondGroupFieldNameSecondLang = Optional.ofNullable(group.get("SecondFieldDisplayNameSecondLang"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(secondFieldCode).getName(group.get(SECOND_LANG_KEY)));
		String thirdGroupFieldNameSecondLang = Optional.ofNullable(group.get("ThirdFieldDisplayNameSecondLang"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(thirdFieldCode).getName(group.get(SECOND_LANG_KEY)));
		String thirdFieldValuesFirstOptionValueFirstLang = Optional.ofNullable(group.get(THIRD_FIELD_FIRST_DISPLAY_NAME))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup
						.getFieldByCode(thirdFieldCode)
						.getOptions()
						.get(thirdFieldValuesFirstOptionName)
						.get(group.get(FIRST_LANG_KEY)));
		String thirdFieldValuesSecondOptionValueFirstLang = Optional.ofNullable(group.get(THIRD_FIELD_SECOND_DISPLAY_NAME))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup
						.getFieldByCode(thirdFieldCode)
						.getOptions()
						.get(thirdFieldValuesSecondOptionName)
						.get(group.get(FIRST_LANG_KEY)));
		String thirdFieldValuesFirstOptionValueSecondLang = Optional.ofNullable(group.get(THIRD_FIELD_FIRST_DISPLAY_NAME))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup
						.getFieldByCode(thirdFieldCode)
						.getOptions()
						.get(thirdFieldValuesFirstOptionName)
						.get(group.get(SECOND_LANG_KEY)));
		String thirdFieldValuesSecondOptionValueSecondLang = Optional.ofNullable(group.get(THIRD_FIELD_SECOND_DISPLAY_NAME))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup
						.getFieldByCode(thirdFieldCode)
						.getOptions()
						.get(thirdFieldValuesSecondOptionName)
						.get(group.get(SECOND_LANG_KEY)));
		response
				.then()
				.assertThat()
				.body(
						SingleGroupApiResponse.getGroupValuePath(group.get(FIRST_LANG_LOCALE_KEY)), hasItem(firstFieldCode),
						SingleGroupApiResponse.getGroupNamePath(
								group.get(FIRST_LANG_LOCALE_KEY), firstFieldCode), equalTo(firstGroupFieldNameFirstLang),
						SingleGroupApiResponse.getGroupNamePath(
								group.get(FIRST_LANG_LOCALE_KEY), secondFieldCode), equalTo(secondGroupFieldNameFirstLang),
						SingleGroupApiResponse.getGroupNamePath(
								group.get(FIRST_LANG_LOCALE_KEY), thirdFieldCode), equalTo(thirdGroupFieldNameFirstLang),
						SingleGroupApiResponse.getGroupNamePath(
								group.get(SECOND_LANG_LOCALE_KEY), firstFieldCode), equalTo(firstGroupFieldNameSecondLang),
						SingleGroupApiResponse.getGroupNamePath(
								group.get(SECOND_LANG_LOCALE_KEY), secondFieldCode), equalTo(secondGroupFieldNameSecondLang),
						SingleGroupApiResponse.getGroupNamePath(
								group.get(SECOND_LANG_LOCALE_KEY), thirdFieldCode), equalTo(thirdGroupFieldNameSecondLang),
						SingleGroupApiResponse.getGroupValuePath(group.get(FIRST_LANG_LOCALE_KEY)), hasItem(secondFieldCode),
						SingleGroupApiResponse.getGroupNamePath(
								group.get(FIRST_LANG_LOCALE_KEY), secondFieldCode), equalTo(secondGroupFieldNameFirstLang),
						SingleGroupApiResponse.getGroupValuePath(group.get(FIRST_LANG_LOCALE_KEY)), hasItem(thirdFieldCode),
						SingleGroupApiResponse.getGroupNamePath(
								group.get(FIRST_LANG_LOCALE_KEY), thirdFieldCode), equalTo(thirdGroupFieldNameFirstLang),
						SingleGroupApiResponse.getGroupFieldDataType(
								group.get(FIRST_LANG_LOCALE_KEY), firstFieldCode), equalTo(firstFieldType),
						SingleGroupApiResponse.getGroupFieldDataType(
								group.get(FIRST_LANG_LOCALE_KEY), secondFieldCode), equalTo(secondFieldType),
						SingleGroupApiResponse.getGroupFieldDataType(
								group.get(FIRST_LANG_LOCALE_KEY), thirdFieldCode), equalTo(thirdFieldType),
						SingleGroupApiResponse.getGroupFieldFields(
								group.get(FIRST_LANG_LOCALE_KEY), firstFieldCode), empty(),
						SingleGroupApiResponse.getGroupFieldFields(
								group.get(FIRST_LANG_LOCALE_KEY), secondFieldCode), empty(),
						SingleGroupApiResponse.getGroupFieldsName(
								group.get(FIRST_LANG_LOCALE_KEY), thirdFieldValuesFirstOptionName, thirdFieldCode),
						equalTo(thirdFieldValuesFirstOptionValueFirstLang),
						SingleGroupApiResponse.getGroupFieldsName(
								group.get(FIRST_LANG_LOCALE_KEY), thirdFieldValuesSecondOptionName, thirdFieldCode),
						equalTo(thirdFieldValuesSecondOptionValueFirstLang),
						SingleGroupApiResponse.getGroupFieldsValue(
								group.get(FIRST_LANG_LOCALE_KEY), thirdFieldValuesFirstOptionValueFirstLang, thirdFieldCode),
						equalTo(thirdFieldValuesFirstOptionName),
						SingleGroupApiResponse.getGroupFieldsValue(
								group.get(FIRST_LANG_LOCALE_KEY), thirdFieldValuesSecondOptionValueFirstLang, thirdFieldCode),
						equalTo(thirdFieldValuesSecondOptionName),
						SingleGroupApiResponse.getGroupValuePath(group.get(SECOND_LANG_LOCALE_KEY)), hasItem(firstFieldCode),
						SingleGroupApiResponse.getGroupNamePath(
								group.get(SECOND_LANG_LOCALE_KEY), firstFieldCode), equalTo(firstGroupFieldNameSecondLang),
						SingleGroupApiResponse.getGroupValuePath(group.get(SECOND_LANG_LOCALE_KEY)), hasItem(secondFieldCode),
						SingleGroupApiResponse.getGroupNamePath(
								group.get(SECOND_LANG_LOCALE_KEY), secondFieldCode), equalTo(secondGroupFieldNameSecondLang),
						SingleGroupApiResponse.getGroupValuePath(group.get(SECOND_LANG_LOCALE_KEY)), hasItem(thirdFieldCode),
						SingleGroupApiResponse.getGroupNamePath(
								group.get(SECOND_LANG_LOCALE_KEY), thirdFieldCode), equalTo(thirdGroupFieldNameSecondLang),
						SingleGroupApiResponse.getGroupFieldDataType(
								group.get(SECOND_LANG_LOCALE_KEY), firstFieldCode), equalTo(firstFieldType),
						SingleGroupApiResponse.getGroupFieldDataType(
								group.get(SECOND_LANG_LOCALE_KEY), secondFieldCode), equalTo(secondFieldType),
						SingleGroupApiResponse.getGroupFieldDataType(
								group.get(SECOND_LANG_LOCALE_KEY), thirdFieldCode), equalTo(thirdFieldType),
						SingleGroupApiResponse.getGroupFieldFields(
								group.get(SECOND_LANG_LOCALE_KEY), firstFieldCode), empty(),
						SingleGroupApiResponse.getGroupFieldFields(
								group.get(SECOND_LANG_LOCALE_KEY), secondFieldCode), empty(),
						SingleGroupApiResponse.getGroupFieldsName(
								group.get(SECOND_LANG_LOCALE_KEY), thirdFieldValuesFirstOptionName, thirdFieldCode),
						equalTo(thirdFieldValuesFirstOptionValueSecondLang),
						SingleGroupApiResponse.getGroupFieldsName(
								group.get(SECOND_LANG_LOCALE_KEY), thirdFieldValuesSecondOptionName, thirdFieldCode),
						equalTo(thirdFieldValuesSecondOptionValueSecondLang),
						SingleGroupApiResponse.getGroupFieldsValue(
								group.get(SECOND_LANG_LOCALE_KEY), thirdFieldValuesFirstOptionValueSecondLang, thirdFieldCode),
						equalTo(thirdFieldValuesFirstOptionName),
						SingleGroupApiResponse.getGroupFieldsValue(
								group.get(SECOND_LANG_LOCALE_KEY), thirdFieldValuesSecondOptionValueSecondLang, thirdFieldCode),
						equalTo(thirdFieldValuesSecondOptionName)
				);
		Map<String, String> fieldCodesMaxSizes = new HashMap<>();
		fieldCodesMaxSizes.put(firstFieldCode, firstShortMaxSize);
		fieldCodesMaxSizes.put(secondFieldCode, secondShortMaxSize);
		fieldCodesMaxSizes.put(thirdFieldCode, thirdShortMaxSize);
		List<String> languages = new ArrayList<>();
		languages.add(FIRST_LANG_LOCALE_KEY);
		languages.add(SECOND_LANG_LOCALE_KEY);
		for (String language : languages) {
			for (Map.Entry<String, String> fieldCodeMaxSize : fieldCodesMaxSizes.entrySet()) {
				if (!"".equals(fieldCodeMaxSize.getValue())) {
					response
							.then()
							.assertThat()
							.body(
									SingleGroupApiResponse.getGroupFieldMaxSize(
											group.get(language), fieldCodeMaxSize.getKey()),
									equalTo(NumberUtils.createInteger(fieldCodeMaxSize.getValue()))
							);
				}
			}
		}
	}

	/**
	 * Verifies that response contains complete group fields projection information for a case group with 3 fields and 1 language.
	 *
	 * @param group group projection.
	 */
	@SuppressWarnings({"PMD.ExcessiveMethodLength", "checkstyle:methodlength"})
	@And("^Single group API response contains complete fields information for group with 3 fields and 1 language$")
	public void singleGroupAPIResponseFieldsCheck1Lang(final Map<String, String> group) {
		String firstFieldCode = cartItemModifierGroup.getGroupFieldCodeByPartialCode(group.get(FIRST_FIELD_NAME));
		String secondFieldCode = cartItemModifierGroup.getGroupFieldCodeByPartialCode(group.get(SECOND_FIELD_NAME));
		String thirdFieldCode = cartItemModifierGroup.getGroupFieldCodeByPartialCode(group.get(THIRD_FIELD_NAME));
		String firstFieldType = Optional.ofNullable(group.get(FIRST_FIELD_TYPE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(firstFieldCode).getFieldType());
		String secondFieldType = Optional.ofNullable(group.get(SECOND_FIELD_TYPE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(secondFieldCode).getFieldType());
		String thirdFieldType = Optional.ofNullable(group.get(THIRD_FIELD_TYPE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(thirdFieldCode).getFieldType());
		String firstShortMaxSize = Optional.ofNullable(group.get(FIRST_FIELD_MAX_SIZE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(firstFieldCode).getShortTextSize());
		String secondShortMaxSize = Optional.ofNullable(group.get(SECOND_FIELD_MAX_SIZE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(secondFieldCode).getShortTextSize());
		String thirdShortMaxSize = Optional.ofNullable(group.get(THIRD_FIELD_MAX_SIZE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(thirdFieldCode).getShortTextSize());
		String thirdFieldValuesFirstOptionName = Optional.ofNullable(group.get(THIRD_FIELD_FIRST_VALUE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> (String) cartItemModifierGroup.getFieldByCode(thirdFieldCode).getOptions().keySet().toArray()[1]);
		String thirdFieldValuesSecondOptionName = Optional.ofNullable(group.get(THIRD_FIELD_SECOND_VALUE))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> (String) cartItemModifierGroup.getFieldByCode(thirdFieldCode).getOptions().keySet().toArray()[0]);
		String excludedLocale = Optional.ofNullable(group.get("excludedLanguageLocale"))
				.filter(StringUtils::isNotEmpty)
				.orElse("");
		String firstFieldName = Optional.ofNullable(group.get("FirstFieldDisplayName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(firstFieldCode).getName(group.get(SINGLE_LANG_KEY)));
		String secondFieldName = Optional.ofNullable(group.get("SecondFieldDisplayName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(secondFieldCode).getName(group.get(SINGLE_LANG_KEY)));
		String thirdFieldName = Optional.ofNullable(group.get("ThirdFieldDisplayName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup.getFieldByCode(thirdFieldCode).getName(group.get(SINGLE_LANG_KEY)));
		String thirdFieldValuesFirstOptionValue = Optional.ofNullable(group.get(THIRD_FIELD_FIRST_DISPLAY_NAME))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup
						.getFieldByCode(thirdFieldCode)
						.getOptions()
						.get(thirdFieldValuesFirstOptionName)
						.get(group.get(SINGLE_LANG_KEY)));
		String thirdFieldValuesSecondOptionValue = Optional.ofNullable(group.get(THIRD_FIELD_SECOND_DISPLAY_NAME))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> cartItemModifierGroup
						.getFieldByCode(thirdFieldCode)
						.getOptions()
						.get(thirdFieldValuesSecondOptionName)
						.get(group.get(SINGLE_LANG_KEY))
				);
		response
				.then()
				.assertThat()
				.body(
						SingleGroupApiResponse.getGroupValuePath(group.get(SINGLE_LANG_LOCALE_KEY)), hasItem(firstFieldCode),
						SingleGroupApiResponse.getGroupNamePath(
								group.get(SINGLE_LANG_LOCALE_KEY), firstFieldCode), equalTo(firstFieldName),
						SingleGroupApiResponse.getGroupValuePath(group.get(SINGLE_LANG_LOCALE_KEY)), hasItem(secondFieldCode),
						SingleGroupApiResponse.getGroupNamePath(
								group.get(SINGLE_LANG_LOCALE_KEY), secondFieldCode), equalTo(secondFieldName),
						SingleGroupApiResponse.getGroupValuePath(group.get(SINGLE_LANG_LOCALE_KEY)), hasItem(thirdFieldCode),
						SingleGroupApiResponse.getGroupNamePath(
								group.get(SINGLE_LANG_LOCALE_KEY), thirdFieldCode), equalTo(thirdFieldName),
						SingleGroupApiResponse.getGroupFieldDataType(
								group.get(SINGLE_LANG_LOCALE_KEY), firstFieldCode), equalTo(firstFieldType),
						SingleGroupApiResponse.getGroupFieldDataType(
								group.get(SINGLE_LANG_LOCALE_KEY), secondFieldCode), equalTo(secondFieldType),
						SingleGroupApiResponse.getGroupFieldDataType(
								group.get(SINGLE_LANG_LOCALE_KEY), thirdFieldCode), equalTo(thirdFieldType),
						SingleGroupApiResponse.getGroupFieldFields(
								group.get(SINGLE_LANG_LOCALE_KEY), firstFieldCode), empty(),
						SingleGroupApiResponse.getGroupFieldFields(
								group.get(SINGLE_LANG_LOCALE_KEY), secondFieldCode), empty(),
						SingleGroupApiResponse.getGroupFieldsName(
								group.get(SINGLE_LANG_LOCALE_KEY), thirdFieldValuesFirstOptionName, thirdFieldCode),
						equalTo(thirdFieldValuesFirstOptionValue),
						SingleGroupApiResponse.getGroupFieldsName(
								group.get(SINGLE_LANG_LOCALE_KEY), thirdFieldValuesSecondOptionName, thirdFieldCode),
						equalTo(thirdFieldValuesSecondOptionValue),
						SingleGroupApiResponse.getGroupFieldsValue(
								group.get(SINGLE_LANG_LOCALE_KEY), thirdFieldValuesFirstOptionValue, thirdFieldCode),
						equalTo(thirdFieldValuesFirstOptionName),
						SingleGroupApiResponse.getGroupFieldsValue(
								group.get(SINGLE_LANG_LOCALE_KEY), thirdFieldValuesSecondOptionValue, thirdFieldCode),
						equalTo(thirdFieldValuesSecondOptionName)
				);
		if (!"".equals(excludedLocale)) {
			response
					.then()
					.assertThat()
					.body(
							SingleGroupApiResponse.getTranslationsLanguagePath(), not(hasItem(excludedLocale))
					);
		}
		Map<String, String> fieldCodesMaxSizes = new HashMap<>();
		fieldCodesMaxSizes.put(firstFieldCode, firstShortMaxSize);
		fieldCodesMaxSizes.put(secondFieldCode, secondShortMaxSize);
		fieldCodesMaxSizes.put(thirdFieldCode, thirdShortMaxSize);
		for (Map.Entry<String, String> fieldCodeMaxSize : fieldCodesMaxSizes.entrySet()) {
			if (!"".equals(fieldCodeMaxSize.getValue())) {
				response
						.then()
						.assertThat()
						.body(
								SingleGroupApiResponse.getGroupFieldMaxSize(
										group.get(SINGLE_LANG_LOCALE_KEY), fieldCodeMaxSize.getKey()),
								equalTo(NumberUtils.createInteger(fieldCodeMaxSize.getValue()))
						);
			}
		}
	}

	/**
	 * Verifies that response contains complete group projection information for deleted group.
	 *
	 * @param group group projection expected values.
	 */
	@And("^Single group API response contains complete information for projection of deleted cartGroup$")
	public void singleGroupAPIResponseContainsCompleteInformationForProjectionOfDeletedCartItemModifierGroup(final Map<String, String> group) {
		checkGroupProjectionMetadata(group);
		response
				.then()
				.assertThat()
				.body(
						SingleGroupApiResponse.TRANSLATIONS, empty()
				);
	}

	/**
	 * Calls API to retrieve group projections for exist stores.
	 */
	@Then("^There are group projections for all exist stores$")
	public void checkGroupProjectionsForExistStores() {
		final List<String> storesList = store.getStoreCodesList();
		for (String oneStore : storesList) {
			getLatestGroupProjection(oneStore, cartItemModifierGroup.getGroupCode());
			response
					.then()
					.assertThat()
					.body(
							SingleGroupApiResponse.STORE, equalTo(oneStore)
					);
		}
	}

	/**
	 * Calls API to retrieve latest version of single cart item modifier group projection populated in DB.
	 *
	 * @param store store code to pass it in URL
	 * @param code  cart item modifier group code to pass it in URL
	 */
	private void getLatestGroupProjection(final String store, final String code) {
		StepsHelper.sleep(Constants.API_SLEEP_TIME);
		response = given()
				.when()
				.get(String.format(SingleGroupApiResponse.GROUP_URL, store, code));
		context.setResponse(response);
	}

	/**
	 * Verifies that response contains complete group projection information except Translations block.
	 *
	 * @param group group projection.
	 */
	private void checkGroupProjectionMetadata(final Map<String, String> group) {
		String groupCode = Optional.ofNullable(group.get("code"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(this.cartItemModifierGroup::getGroupCode);

		String storeCode = Optional.ofNullable(group.get("store"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(store::getCode);
		response
				.then()
				.assertThat()
				.body(
						SingleGroupApiResponse.TYPE, equalTo(group.get("type")),
						SingleGroupApiResponse.CODE, equalTo(groupCode),
						SingleGroupApiResponse.STORE, equalTo(storeCode),
						SingleGroupApiResponse.MODIFIED_DATE_TIME, notNullValue(),
						SingleGroupApiResponse.DELETED, equalTo(Boolean.parseBoolean(group.get("deleted")))
				);
	}
}
