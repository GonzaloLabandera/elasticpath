/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.definitions.api.brand;

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
import org.apache.commons.lang3.StringUtils;

import com.elasticpath.definitions.api.helpers.Constants;
import com.elasticpath.definitions.api.helpers.ContextDrivenStepsHelper;
import com.elasticpath.definitions.api.helpers.StepsHelper;
import com.elasticpath.definitions.stateobjects.Context;
import com.elasticpath.definitions.stateobjects.Projection;
import com.elasticpath.definitions.stateobjects.content.BrandProjectionContent;
import com.elasticpath.definitions.testobjects.SingleBrandApiResponse;
import com.elasticpath.selenium.domainobjects.Brand;
import com.elasticpath.selenium.domainobjects.Store;

/**
 * Syndication API Brand steps.
 */
public class BrandDefinition {

	private static final String FIRST_LANG_KEY = "firstLanguageLocale";
	private static final String SECOND_LANG_KEY = "secondLanguageLocale";
	private static final String SINGLE_LANG_KEY = "languageLocale";
	private final Store store;
	private final Brand brand;
	private final Projection brandProjection;
	private final BrandProjectionContent brandProjectionContent;
	private final SingleBrandApiResponse singleBrandApiResponse;
	private Context context;
	private Response response;

	/**
	 * Constructor.
	 *
	 * @param store      store state object
	 * @param context    context state object
	 * @param brand      brand state object
	 * @param projection projection state object
	 * @param content    content state object
	 */
	public BrandDefinition(
			final Store store, final Context context, final Brand brand, final Projection projection, final BrandProjectionContent content) {
		this.context = context;
		this.response = this.context.getResponse();
		this.store = store;
		this.brand = brand;
		this.brandProjection = projection;
		this.brandProjectionContent = content;
		singleBrandApiResponse = new SingleBrandApiResponse();
	}

	/**
	 * Calls API to retrieve latest version of single brand projection.
	 *
	 * @param storeCode store code which should be used to retrieve projection.
	 */
	@Then("^I retrieve latest version of created in CM brand projection for store (.+) via API$")
	public void getLatestBrandProjectionPopulatedViaCM(final String storeCode) {
		getLatestBrandProjection(storeCode, brand.getCode());
	}

	private void getLatestBrandProjection(final String store, final String code) {
		StepsHelper.sleep(Constants.API_SLEEP_TIME);
		response = given()
				.when()
				.get(String.format(SingleBrandApiResponse.BRAND_URL, store, code));
		context.setResponse(response);
	}

	/**
	 * Calls API to retrieve latest version of single brand projection populated in DB.
	 */
	@Then("^I retrieve created in DB brand projection for generated store via API$")
	public void getBrandProjectionPopulatedInDb() {
		getLatestBrandProjection(brandProjection.getStore(), brandProjection.getCode());
	}

	/**
	 * Calls API to retrieve single brand projection populated in DB using ETag received in previous call and verifies response status code.
	 *
	 * @param statusCode expected response status code
	 * @throws InterruptedException waiting for response was interrupted
	 */
	@Then("^I retrieve created in DB brand projection for created store using ETag from previous API call and see response status code (\\d+)$")
	public void getBrandProjectionPopulatedInDbETag(final int statusCode) throws InterruptedException {
		ContextDrivenStepsHelper helper = new ContextDrivenStepsHelper(this.context);
		this.context = helper.getLatestProjectionETag(
				SingleBrandApiResponse.BRAND_URL,
				brandProjection.getStore(),
				brandProjection.getCode(),
				statusCode);
		this.response = this.context.getResponse();
		this.response
				.then()
				.statusCode(statusCode);
	}

	/**
	 * Calls API to retrieve latest version of single brand projection.
	 *
	 * @param storeCode store code which should be used to retrieve projection
	 */
	@Then("^I retrieve created in DB brand projection for store (.+) via API$")
	public void getBrandProjectionPopulatedInDb(final String storeCode) {
		getLatestBrandProjection(storeCode, brandProjection.getCode());
	}

	/**
	 * Calls API to retrieve single brand projection.
	 *
	 * @param code brand code which should be used to retrieve projection
	 */
	@Then("^I retrieve brand projection (.+) for store generated in DB via API$")
	public void getLatestOptionProjectionPopulatedInDbByCode(final String code) {
		getLatestBrandProjection(brandProjection.getStore(), code);
	}

	/**
	 * Verifies that response contains complete brand projection information for a case.
	 * when JSON contains information about 2 languages.
	 *
	 * @param brand brand projection.
	 */
	@Then("^Single brand API response contains complete information for projection with 2 languages$")
	public void checkLatestBrandProjectionTwoLang(final Map<String, String> brand) {
		checkBrandProjectionMetadata(brand);
		final String brandNameFirstLang = Optional.ofNullable(brand.get("displayNameFirstLang"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> this.brand.getName(brand.get("firstLanguage")));
		final String brandNameSecondLang = Optional.ofNullable(brand.get("displayNameSecondLang"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> this.brand.getName(brand.get("secondLanguage")));
		response
				.then()
				.assertThat()
				.body(
						singleBrandApiResponse.getDisplayNamePath(brand.get(FIRST_LANG_KEY)), equalTo(brandNameFirstLang),
						singleBrandApiResponse.getDisplayNamePath(brand.get(SECOND_LANG_KEY)), equalTo(brandNameSecondLang));
	}

	/**
	 * Verifies that response contains complete brand projection information for a case.
	 * when JSON contains information about 1 language.
	 *
	 * @param brand brand projection.
	 */
	@Then("^Single brand API response contains complete information for projection with 1 language$")
	public void checkLatestBrandProjectionOneLang(final Map<String, String> brand) {
		checkBrandProjectionMetadata(brand);
		final String brandName = Optional.ofNullable(brand.get("displayName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> this.brand.getName(brand.get("language")));
		final String excludedLocale = Optional.ofNullable(brand.get("excludedLanguageLocale"))
				.filter(StringUtils::isNotEmpty)
				.orElse("");
		response
				.then()
				.assertThat()
				.body(
						singleBrandApiResponse.getDisplayNamePath(brand.get(SINGLE_LANG_KEY)), equalTo(brandName));
		if (!"".equals(excludedLocale)) {
			response
					.then()
					.assertThat()
					.body(
							singleBrandApiResponse.getTranslationsLanguagePath(), not(excludedLocale)
					);
		}
	}

	/**
	 * Verifies that response contains complete brand projection information for deleted brand.
	 *
	 * @param brand brand projection expected values.
	 */
	@Then("^Single brand API response contains complete information for projection of deleted brand$")
	public void checkLatestDeletedBrandProjection(final Map<String, String> brand) {
		checkBrandProjectionMetadata(brand);
		response
				.then()
				.assertThat()
				.body(
						SingleBrandApiResponse.TRANSLATIONS, empty()
				);
	}

	/**
	 * Verifies that response contains complete brand projection information except Translations block.
	 *
	 * @param brand brand projection.
	 */
	private void checkBrandProjectionMetadata(final Map<String, String> brand) {
		final String brandCode = Optional.ofNullable(brand.get("code"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(this.brand::getCode);
		final String storeCode = Optional.ofNullable(brand.get("store"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(store::getCode);
		response
				.then()
				.assertThat()
				.body(
						SingleBrandApiResponse.TYPE, equalTo(brand.get("type")),
						SingleBrandApiResponse.CODE, equalTo(brandCode),
						SingleBrandApiResponse.STORE, equalTo(storeCode),
						SingleBrandApiResponse.MODIFIED_DATE_TIME, notNullValue(),
						SingleBrandApiResponse.DELETED, equalTo(Boolean.parseBoolean(brand.get("deleted")))
				);
	}

	/**
	 * Calls API to retrieve brand projections for exist stores.
	 */
	@Then("^There are brand projections for all exist stores$")
	public void checkBrandProjectionsForExistStores() {
		final List<String> storesList = store.getStoreCodesList();
		for (String oneStore : storesList) {
			getLatestBrandProjection(oneStore, brand.getCode());
			response
					.then()
					.assertThat()
					.body(
							SingleBrandApiResponse.STORE, equalTo(oneStore)
					);
		}
	}

	/**
	 * Verifies that response contains full brand projection information for brand projection which was generated.
	 *
	 * @param brand brand projection expected values
	 */
	@Then("^Single brand projection API response has same values as (?:generated|updated) projection$")
	public void checkGeneratedBrandProjection(final Map<String, String> brand) {
		String code = Optional.ofNullable(brand.get("code"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(brandProjection::getCode);
		String storeCode = Optional.ofNullable(brand.get("store"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(brandProjection::getStore);
		String skuName = Optional.ofNullable(brand.get("displayName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(brandProjectionContent::getDisplayName);
		response
				.then()
				.assertThat()
				.body(
						SingleBrandApiResponse.TYPE, equalTo(brand.get("type")),
						SingleBrandApiResponse.CODE, equalTo(code),
						SingleBrandApiResponse.STORE, equalTo(storeCode),
						SingleBrandApiResponse.MODIFIED_DATE_TIME, notNullValue(),
						SingleBrandApiResponse.DELETED, equalTo(Boolean.parseBoolean(brand.get("deleted"))),
						singleBrandApiResponse.getDisplayNamePath(brand.get(SINGLE_LANG_KEY)), equalTo(skuName)
				);
		response.then().assertThat().header(Constants.ETAG, not(empty()));
	}
}
