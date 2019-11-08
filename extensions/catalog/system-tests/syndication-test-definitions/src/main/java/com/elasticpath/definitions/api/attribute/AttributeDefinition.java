/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.definitions.api.attribute;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import cucumber.api.java.en.Then;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;

import com.elasticpath.definitions.stateobjects.Context;
import com.elasticpath.definitions.testobjects.SingleAttributeApiResponse;
import com.elasticpath.selenium.domainobjects.Attribute;
import com.elasticpath.selenium.domainobjects.Store;

/**
 * Syndication API Attribute steps.
 */
public class AttributeDefinition {

	private static final String FIRST_LANG_KEY = "firstLanguageLocale";
	private static final String SECOND_LANG_KEY = "secondLanguageLocale";
	private static final String SINGLE_LANG_KEY = "languageLocale";
	private final Store store;
	private final Context context;
	private final Attribute attribute;
	private Response response;

	/**
	 * COnstructor.
	 *
	 * @param store     store state object
	 * @param context   context state object
	 * @param attribute attribute state object
	 */
	public AttributeDefinition(final Store store, final Context context, final Attribute attribute) {
		this.context = context;
		this.response = this.context.getResponse();
		this.store = store;
		this.attribute = attribute;
	}

	/**
	 * Calls API to retrieve latest version of single attribute projection.
	 *
	 * @param storeCode store code which should be used to retrieve projection.
	 */
	@Then("^I retrieve latest version of created in CM attribute projection for store (.+) via API$")
	public void getLatestAttributeProjectionPopulatedViaCM(final String storeCode) {
		getLatestAttributeProjection(storeCode, attribute.getKey());
	}

	/**
	 * Verifies that response contains complete attribute projection information for a case
	 * when JSON contains information about 2 languages.
	 *
	 * @param attribute attribute projection expected values.
	 */
	@Then("^Single attribute API response contains complete information for projection with 2 languages$")
	public void checkLatestAttributeProjectionTwoLang(final Map<String, String> attribute) {
		checkAttributeProjectionMetadata(attribute);
		final String attrNameFirstLang = Optional.ofNullable(attribute.get("displayNameFirstLang"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> this.attribute.getName(attribute.get("firstLanguage")));
		final String attrNameSecondLang = Optional.ofNullable(attribute.get("displayNameSecondLang"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> this.attribute.getName(attribute.get("secondLanguage")));
		final String type = Optional.ofNullable(attribute.get("dataType"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(this.attribute::getAttributeType);
		final String isMultiValue = Optional.ofNullable(attribute.get("isMultiValue"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> String.valueOf(this.attribute.isMultiValuesAllowed()));
		response
				.then()
				.assertThat()
				.body(
						SingleAttributeApiResponse.getDisplayNamePath(attribute.get(FIRST_LANG_KEY)), equalTo(attrNameFirstLang),
						SingleAttributeApiResponse.getDataTypePath(attribute.get(FIRST_LANG_KEY)), equalTo(type),
						SingleAttributeApiResponse.getMultiValuePath(attribute.get(FIRST_LANG_KEY)), equalTo(Boolean.parseBoolean(isMultiValue)),
						SingleAttributeApiResponse.getMultiValuePath(attribute.get(FIRST_LANG_KEY)), equalTo(Boolean.parseBoolean(isMultiValue)),
						SingleAttributeApiResponse.getDisplayNamePath(attribute.get(SECOND_LANG_KEY)), equalTo(attrNameSecondLang),
						SingleAttributeApiResponse.getDataTypePath(attribute.get(SECOND_LANG_KEY)), equalTo(type),
						SingleAttributeApiResponse.getMultiValuePath(attribute.get(SECOND_LANG_KEY)), equalTo(Boolean.parseBoolean(isMultiValue))
				);
	}

	/**
	 * Verifies that response contains complete attribute projection information for a case
	 * when JSON contains information about and 1 language.
	 *
	 * @param attribute attribute projection expected values.
	 */
	@Then("^Single attribute API response contains complete information for projection with 1 language$")
	public void checkLatestAttributeProjectionOneLang(final Map<String, String> attribute) {
		checkAttributeProjectionMetadata(attribute);
		final String attrName = Optional.ofNullable(attribute.get("displayName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> this.attribute.getName(attribute.get("language")));
		final String excludedLocale = Optional.ofNullable(attribute.get("excludedLanguageLocale"))
				.filter(StringUtils::isNotEmpty)
				.orElse("");
		response
				.then()
				.assertThat()
				.body(
						SingleAttributeApiResponse.getDisplayNamePath(attribute.get(SINGLE_LANG_KEY)), equalTo(attrName),
						SingleAttributeApiResponse.getDataTypePath(attribute.get(SINGLE_LANG_KEY)), equalTo(attribute.get("dataType")),
						SingleAttributeApiResponse.getMultiValuePath(attribute.get(SINGLE_LANG_KEY)),
						equalTo(Boolean.parseBoolean(attribute.get("isMultiValue")))
				);
		if (!"".equals(excludedLocale)) {
			response
					.then()
					.assertThat()
					.body(
							SingleAttributeApiResponse.getTranslationsLanguagePath(), not(excludedLocale)
					);
		}
	}

	/**
	 * Verifies that response contains complete attribute projection information for deleted attribute.
	 *
	 * @param attribute attribute projection expected values.
	 */
	@Then("^Single attribute API response contains complete information for projection of deleted attribute$")
	public void checkLatestDeletedAttributeProjection(final Map<String, String> attribute) {
		checkAttributeProjectionMetadata(attribute);
		response
				.then()
				.assertThat()
				.body(
						SingleAttributeApiResponse.TRANSLATIONS, empty()
				);
	}

	/**
	 * Calls API to retrieve latest version of single attribute projection populated in DB.
	 *
	 * @param store store code to pass it in URL.
	 * @param key   attribute key to pass it in URL.
	 */
	private void getLatestAttributeProjection(final String store, final String key) {
		response = given()
				.when()
				.get(String.format(SingleAttributeApiResponse.ATTRIBUTE_URL, store, key));
		context.setResponse(response);
	}

	/**
	 * Verifies that response contains complete attribute projection information except Translations block.
	 *
	 * @param attribute attribute projection expected values.
	 */
	private void checkAttributeProjectionMetadata(final Map<String, String> attribute) {
		String attributeKey = Optional.ofNullable(attribute.get("code"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(this.attribute::getKey);
		String storeCode = Optional.ofNullable(attribute.get("store"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(store::getCode);
		response
				.then()
				.assertThat()
				.body(
						SingleAttributeApiResponse.TYPE, equalTo(attribute.get("type")),
						SingleAttributeApiResponse.CODE, equalTo(attributeKey),
						SingleAttributeApiResponse.STORE, equalTo(storeCode),
						SingleAttributeApiResponse.MODIFIED_DATE_TIME, notNullValue(),
						SingleAttributeApiResponse.DELETED, equalTo(Boolean.parseBoolean(attribute.get("deleted")))
				);
	}

	/**
	 * Calls API to retrieve attribute projections for all exist stores.
	 */
	@Then("^There are attribute projections for all exist stores$")
	public void checkAttributeProjectionsForExistStores() {
		final List<String> storesList = store.getStoreCodesList();
		for (String oneStore : storesList) {
			getLatestAttributeProjection(oneStore, attribute.getKey());
			response
					.then()
					.assertThat()
					.body(
							SingleAttributeApiResponse.STORE, equalTo(oneStore)
					);
		}
	}
}
