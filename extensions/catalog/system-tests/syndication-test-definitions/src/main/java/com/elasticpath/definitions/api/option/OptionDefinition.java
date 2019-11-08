package com.elasticpath.definitions.api.option;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import cucumber.api.java.en.Then;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;

import com.elasticpath.definitions.api.helpers.Constants;
import com.elasticpath.definitions.api.helpers.ContextDrivenStepsHelper;
import com.elasticpath.definitions.stateobjects.Context;
import com.elasticpath.definitions.stateobjects.Projection;
import com.elasticpath.definitions.stateobjects.content.OptionProjectionContent;
import com.elasticpath.definitions.testobjects.SingleOptionApiResponse;
import com.elasticpath.selenium.domainobjects.SkuOption;
import com.elasticpath.selenium.domainobjects.Store;


/**
 * Syndication API Option steps.
 */
public class OptionDefinition {

	private static final String FIRST_LANG_KEY = "firstLanguageLocale";
	private static final String SECOND_LANG_KEY = "secondLanguageLocale";
	private static final String SINGLE_LANG_KEY = "languageLocale";
	private final Store store;
	private final SkuOption skuOption;
	private final Projection optionProjection;
	private final OptionProjectionContent optionProjectionContent;
	private final SingleOptionApiResponse singleOptionResponse;
	private Context context;
	private Response response;

	/**
	 * COnstructor.
	 *
	 * @param store                   store state object
	 * @param context                 context state object
	 * @param skuOption               sku option state object
	 * @param projection              option projection state object
	 * @param optionProjectionContent option projection content state object
	 */
	public OptionDefinition(final Store store, final Context context, final SkuOption skuOption, final Projection projection,
							final OptionProjectionContent optionProjectionContent) {
		this.context = context;
		this.response = this.context.getResponse();
		this.store = store;
		this.skuOption = skuOption;
		this.optionProjection = projection;
		this.optionProjectionContent = optionProjectionContent;
		singleOptionResponse = new SingleOptionApiResponse();
	}

	/**
	 * Calls API to retrieve latest version of single sku option projection for new created store.
	 */
	@Then("^I retrieve latest version of created in CM option projection for created store via API$")
	public void getLatestOptionProjectionPopulatedViaCM() {
		getLatestOptionProjection(store.getCode(), skuOption.getCode());
	}

	/**
	 * Calls API to retrieve latest version of single sku option projection.
	 *
	 * @param storeCode store code which should be used to retrieve projection
	 */
	@Then("^I retrieve latest version of created in CM option projection for store (.+) via API$")
	public void getLatestOptionProjectionPopulatedViaCM(final String storeCode) {
		getLatestOptionProjection(storeCode, skuOption.getCode());
	}

	/**
	 * Calls API to retrieve latest version of single sku option projection.
	 *
	 * @param storeCode store code which should be used to retrieve projection
	 */
	@Then("^I retrieve latest version of created in DB option projection for store (.+) via API$")
	public void getLatestOptionProjectionPopulatedInDb(final String storeCode) {
		getLatestOptionProjection(storeCode, optionProjection.getCode());
	}

	/**
	 * Calls API to retrieve latest version of single sku option projection.
	 *
	 * @param code option code which should be used to retrieve projection
	 */
	@Then("^I retrieve latest version of option projection (.+) for store generated in DB via API$")
	public void getLatestOptionProjectionPopulatedInDbByCode(final String code) {
		getLatestOptionProjection(optionProjection.getStore(), code);
	}

	/**
	 * Calls API to retrieve latest version of single sku option projection populated in DB.
	 */
	@Then("^I retrieve latest version of created in DB option projection for created store via API$")
	public void getLatestOptionProjectionPopulatedInDb() {
		getLatestOptionProjection(optionProjection.getStore(), optionProjection.getCode());
	}

	/**
	 * Calls API to retrieve option projections for exist stores.
	 */
	@Then("^There are sku projections for all exist stores$")
	public void checkOptionProjectionsForExistStores() {
		final List<String> storesList = store.getStoreCodesList();
		for (String oneStore : storesList) {
			getLatestOptionProjection(oneStore, skuOption.getCode());
			response
					.then()
					.assertThat()
					.body(
							SingleOptionApiResponse.STORE, equalTo(oneStore)
					);
		}
	}

	/**
	 * Calls API to retrieve single sku option projection populated in DB using ETag received in previous call and verifies response status code.
	 *
	 * @param statusCode expected response status code
	 * @throws InterruptedException waiting for response was interrupted
	 */
	@Then("^I retrieve created in DB option projection for created store using ETag from previous API call and see response status code (\\d+)$")
	public void getLatestOptionProjectionPopulatedInDbETag(final int statusCode) throws InterruptedException {
		ContextDrivenStepsHelper helper = new ContextDrivenStepsHelper(this.context);
		this.context = helper.getLatestProjectionETag(
				SingleOptionApiResponse.OPTION_URL,
				optionProjection.getStore(),
				optionProjection.getCode(),
				statusCode);
		this.response = this.context.getResponse();
		this.response
				.then()
				.statusCode(statusCode);
	}

	/**
	 * Verifies that response contains complete sku option projection information for a case.
	 * when JSON contains information about 2 sku option values and 2 languages.
	 *
	 * @param option sku option projection expected values
	 */
	@Then("^Single option API response contains complete information for projection with 2 sku option values and 2 languages$")
	public void checkLatestOptionProjectionTwoOptionValuesTwoLang(final Map<String, String> option) {
		checkOptionProjectionMetadata(option);
		String firstSkuValueCode = skuOption.getSkuOptionValueCodeByPartialCode(option.get("firstOptionValue"));
		String secondSkuValueCode = skuOption.getSkuOptionValueCodeByPartialCode(option.get("secondOptionValue"));
		String skuNameFirstLang = Optional.ofNullable(option.get("displayNameFirstLang"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> skuOption.getName(option.get("firstLanguage")));
		String skuNameSecondLang = Optional.ofNullable(option.get("displayNameSecondLang"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> skuOption.getName(option.get("secondLanguage")));
		String firstSkuValueFirstLangName = Optional.ofNullable(option.get("firstOptionValueNameFirstLang"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> skuOption.getSkuOptionValueName(firstSkuValueCode).get(option.get("firstLanguage")));
		String firstSkuValueSecondLangName = Optional.ofNullable(option.get("firstOptionValueNameSecondLang"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> skuOption.getSkuOptionValueName(firstSkuValueCode).get(option.get("secondLanguage")));
		String secondSkuValueFirstLangName = Optional.ofNullable(option.get("secondOptionValueNameFirstLang"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> skuOption.getSkuOptionValueName(secondSkuValueCode).get(option.get("firstLanguage")));
		String secondSkuValueSecondLangName = Optional.ofNullable(option.get("secondOptionValueNameSecondLang"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> skuOption.getSkuOptionValueName(secondSkuValueCode).get(option.get("secondLanguage")));
		response
				.then()
				.assertThat()
				.body(
						singleOptionResponse.getDisplayNamePath(option.get(FIRST_LANG_KEY)), equalTo(skuNameFirstLang),
						singleOptionResponse.getOptionValuePath(option.get(FIRST_LANG_KEY)), hasItem(firstSkuValueCode),
						singleOptionResponse.getOptionValueNamePath(
								option.get(FIRST_LANG_KEY), firstSkuValueCode), equalTo(firstSkuValueFirstLangName),
						singleOptionResponse.getOptionValuePath(option.get(FIRST_LANG_KEY)), hasItem(secondSkuValueCode),
						singleOptionResponse.getOptionValueNamePath(
								option.get(FIRST_LANG_KEY), secondSkuValueCode), equalTo(secondSkuValueFirstLangName),
						singleOptionResponse.getDisplayNamePath(option.get(SECOND_LANG_KEY)), equalTo(skuNameSecondLang),
						singleOptionResponse.getOptionValuePath(option.get(SECOND_LANG_KEY)), hasItem(firstSkuValueCode),
						singleOptionResponse.getOptionValueNamePath(
								option.get(SECOND_LANG_KEY), firstSkuValueCode), equalTo(firstSkuValueSecondLangName),
						singleOptionResponse.getOptionValuePath(option.get(SECOND_LANG_KEY)), hasItem(secondSkuValueCode),
						singleOptionResponse.getOptionValueNamePath(
								option.get(SECOND_LANG_KEY), secondSkuValueCode), equalTo(secondSkuValueSecondLangName)
				);
	}

	/**
	 * Verifies that response contains complete sku option projection information for a case.
	 * when JSON contains information about 2 sku option values and 1 language.
	 *
	 * @param option sku option projection expected values
	 */
	@Then("^Single option API response contains complete information for projection with 2 sku option values and 1 language$")
	public void checkLatestOptionProjectionTwoOptionValuesOneLang(final Map<String, String> option) {
		checkOptionProjectionMetadata(option);
		String firstSkuValueCode = skuOption.getSkuOptionValueCodeByPartialCode(option.get("firstOptionValue"));
		String secondSkuValueCode = skuOption.getSkuOptionValueCodeByPartialCode(option.get("secondOptionValue"));
		String skuName = Optional.ofNullable(option.get("displayName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> skuOption.getName(option.get("language")));
		String firstSkuValueName = Optional.ofNullable(option.get("firstOptionValueName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> skuOption.getSkuOptionValueName(firstSkuValueCode).get(option.get("language")));
		String secondSkuValueName = Optional.ofNullable(option.get("secondOptionValueName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> skuOption.getSkuOptionValueName(secondSkuValueCode).get(option.get("language")));
		String excludedLocale = Optional.ofNullable(option.get("excludedLanguageLocale"))
				.filter(StringUtils::isNotEmpty)
				.orElse("");
		response
				.then()
				.assertThat()
				.body(
						singleOptionResponse.getDisplayNamePath(option.get(SINGLE_LANG_KEY)), equalTo(skuName),
						singleOptionResponse.getOptionValuePath(option.get(SINGLE_LANG_KEY)), hasItem(firstSkuValueCode),
						singleOptionResponse.getOptionValueNamePath(
								option.get(SINGLE_LANG_KEY), firstSkuValueCode), equalTo(firstSkuValueName),
						singleOptionResponse.getOptionValuePath(option.get(SINGLE_LANG_KEY)), hasItem(secondSkuValueCode),
						singleOptionResponse.getOptionValueNamePath(
								option.get(SINGLE_LANG_KEY), secondSkuValueCode), equalTo(secondSkuValueName)
				);
		if (!"".equals(excludedLocale)) {
			response
					.then()
					.assertThat()
					.body(
							singleOptionResponse.getTranslationsLanguagePath(), not(hasItem(excludedLocale))
					);
		}
	}

	/**
	 * Verifies that response contains complete sku option projection information for deleted sku option.
	 *
	 * @param option sku option projection expected values
	 */
	@Then("^Single option API response contains complete information for projection of deleted sku option$")
	public void checkLatestDeletedOptionProjection(final Map<String, String> option) {
		checkOptionProjectionMetadata(option);
		response
				.then()
				.assertThat()
				.body(
						SingleOptionApiResponse.TRANSLATIONS, empty()
				);
	}

	/**
	 * Verifies that response contains complete sku option projection information except Translations block.
	 *
	 * @param option sku option projection expected values
	 */
	private void checkOptionProjectionMetadata(final Map<String, String> option) {
		String skuCode = Optional.ofNullable(option.get("code"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(skuOption::getCode);
		String storeCode = Optional.ofNullable(option.get("store"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(store::getCode);
		response
				.then()
				.assertThat()
				.body(
						SingleOptionApiResponse.TYPE, equalTo(option.get("type")),
						SingleOptionApiResponse.CODE, equalTo(skuCode),
						SingleOptionApiResponse.STORE, equalTo(storeCode),
						SingleOptionApiResponse.MODIFIED_DATE_TIME, notNullValue(),
						SingleOptionApiResponse.DELETED, equalTo(Boolean.parseBoolean(option.get("deleted")))
				);
	}

	/**
	 * Verifies that response contains full sku option projection information for option projection which was generated.
	 *
	 * @param option sku option projection expected values
	 */
	@Then("^Single option API response has same values as (?:generated|updated) option projection$")
	public void checkGeneratedOptionProjection(final Map<String, String> option) {
		String skuCode = Optional.ofNullable(option.get("code"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(optionProjection::getCode);
		String storeCode = Optional.ofNullable(option.get("store"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(optionProjection::getStore);
		String skuName = Optional.ofNullable(option.get("displayName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(optionProjectionContent::getDisplayName);
		String skuValueCode = Optional.ofNullable(option.get("optionValue"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(optionProjectionContent::getOptionValue);
		String skuValueName = Optional.ofNullable(option.get("optionDisplayValue"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(optionProjectionContent::getDisplayValue);
		response
				.then()
				.assertThat()
				.body(
						SingleOptionApiResponse.TYPE, equalTo(option.get("type")),
						SingleOptionApiResponse.CODE, equalTo(skuCode),
						SingleOptionApiResponse.STORE, equalTo(storeCode),
						SingleOptionApiResponse.MODIFIED_DATE_TIME, notNullValue(),
						SingleOptionApiResponse.DELETED, equalTo(Boolean.parseBoolean(option.get("deleted"))),
						singleOptionResponse.getDisplayNamePath(option.get(SINGLE_LANG_KEY)), equalTo(skuName),
						singleOptionResponse.getFirstOptionValuePath(option.get(SINGLE_LANG_KEY)), equalTo(skuValueCode),
						singleOptionResponse.getFirstOptionValueNamePath(option.get(SINGLE_LANG_KEY)), equalTo(skuValueName)
				);
		response.then().assertThat().header(Constants.ETAG, not(empty()));
	}

	/**
	 * Verifies that an ETag header from the last response is not the same as an ETag header from the previous response.
	 */
	@Then("^Response ETag header differs from previous API call's ETag header$")
	public void compareDifferentETags() {
		assertThat(context.getPreviousResponse().header(Constants.ETAG))
				.as("ETags in two API calls are the same while projection content is different")
				.isNotEqualTo(response.header(Constants.ETAG));
	}

	/**
	 * Verifies that an ETag header from the last response is the same as an ETag header from the previous response.
	 */
	@Then("^Response ETag header is the same as in the previous API call's header$")
	public void compareETags() {
		assertThat(context.getPreviousResponse().header(Constants.ETAG))
				.as("ETags in two API calls are the same while projection content is different")
				.isEqualTo(response.header(Constants.ETAG));
	}

	/**
	 * Calls API to retrieve latest version of single sku option projection populated in DB.
	 *
	 * @param store store code to pass it in URL
	 * @param code  sku option code to pass it in URL
	 */
	private void getLatestOptionProjection(final String store, final String code) {
		response = given()
				.when()
				.get(String.format(SingleOptionApiResponse.OPTION_URL, store, code));
		context.setResponse(response);
	}

}
