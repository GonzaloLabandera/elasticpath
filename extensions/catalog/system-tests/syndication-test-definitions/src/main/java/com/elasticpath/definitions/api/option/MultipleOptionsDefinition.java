package com.elasticpath.definitions.api.option;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import cucumber.api.java.en.Then;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;

import com.elasticpath.definitions.api.helpers.ContextDrivenStepsHelper;
import com.elasticpath.definitions.api.helpers.StepsHelper;
import com.elasticpath.definitions.stateobjects.Context;
import com.elasticpath.definitions.stateobjects.ObjectsContext;
import com.elasticpath.definitions.stateobjects.Projection;
import com.elasticpath.definitions.stateobjects.content.OptionProjectionContent;
import com.elasticpath.definitions.testobjects.JmsCatalogEventMessage;
import com.elasticpath.definitions.testobjects.MultipleOptionsApiResponse;


/**
 * Syndication API Option steps.
 */
public class MultipleOptionsDefinition {

	private static final String SINGLE_LANG_KEY = "languageLocale";
	private static final String KEY_TYPE = "type";
	private static final String KEY_DELETED = "deleted";
	private Context context;
	private final ObjectsContext objectsContext;
	private final Projection optionProjection;
	private final OptionProjectionContent optionProjectionContent;
	private final MultipleOptionsApiResponse multipleOptionsResponse;
	private final JmsCatalogEventMessage message;
	private Response response;

	/**
	 * Constructor.
	 *
	 * @param context                 context state object
	 * @param objectsContext          objects context state object
	 * @param projection              option projection state object
	 * @param optionProjectionContent option projection content state object
	 */
	public MultipleOptionsDefinition(final Context context, final ObjectsContext objectsContext,
									 final Projection projection, final OptionProjectionContent optionProjectionContent) {
		this.context = context;
		this.objectsContext = objectsContext;
		this.response = this.context.getResponse();
		this.optionProjection = projection;
		this.optionProjectionContent = optionProjectionContent;
		multipleOptionsResponse = new MultipleOptionsApiResponse();
		message = new JmsCatalogEventMessage();
	}

	/**
	 * Calls API to retrieve a single sku option projection populated in DB by POST request with body.
	 */
	@Then("^I retrieve a created in DB option projection for created store via POST request$")
	public void getOptionProjectionPopulatedInDbPost() {
		getOptionProjectionPopulatedInDbStoresPost(optionProjection.getStore());
	}

	/**
	 * Calls API to retrieve a single sku option projection populated in DB by POST request with body.
	 *
	 * @param store store code
	 */
	@Then("^I retrieve a created in DB option projection for generated store via POST request when body contains store code (.+)$")
	public void getOptionProjectionPopulatedInDbStoresPost(final String store) {
		getOptionProjectionPost(optionProjection.getStore(), store, Collections.singletonList(optionProjection.getCode()));
	}

	/**
	 * Calls API to retrieve a single sku option projection populated in DB by POST request with body.
	 */
	@Then("^I retrieve 2 created in DB option projections for created store via POST request$")
	public void getOptionProjectionsPopulatedInDbPost() {
		List<String> codes = new ArrayList<>();
		codes.add(objectsContext.getFirstAddedProjection().getCode());
		codes.add(optionProjection.getCode());
		getOptionProjectionPost(optionProjection.getStore(), optionProjection.getStore(), codes);
	}

	/**
	 * Calls API to retrieve a single sku option projection populated in DB by POST request with body when codes list contains one non-existent sku
	 * code.
	 */
	@Then("^I retrieve a created in DB option projection for created store via POST request when codes list contains one non-existent sku code$")
	public void getOptionProjectionsPopulatedInDbWrongCodePost() {
		List<String> codes = new ArrayList<>();
		codes.add(optionProjection.getCode());
		codes.add("NON-EXISTENT-CODE");
		getOptionProjectionPost(optionProjection.getStore(), optionProjection.getStore(), codes);
	}

	/**
	 * Calls API to retrieve a single sku option projection populated in DB by POST request with malformed body.
	 */
	@Then("^I retrieve a created in DB option projection for created store via POST request with malformed body$")
	public void getOptionProjectionPopulatedInDbPostMalformedBody() {
		final String body = "Malformed body";
		response = given()
				.body(body)
				.when()
				.contentType(ContentType.JSON)
				.post(String.format(multipleOptionsResponse.getPostOptionUrl(), optionProjection.getStore()));
		context.setResponse(response);
	}

	/**
	 * Calls API to retrieve all the sku option projections populated in DB starting from the first response page or after specified  sku code.
	 *
	 * @param parameters URL query parameters
	 */
	@Then("^I retrieve option projections created in DB for created store$")
	public void getOptionProjectionsFirstsOrAfterProvidedCodePopulatedInDb(final Map<String, String> parameters) {
		ContextDrivenStepsHelper helper = new ContextDrivenStepsHelper(this.context);
		String startAfter = Optional.ofNullable(parameters.get("startAfterCode"))
				.filter(StringUtils::isNotEmpty)
				.orElse("");
		this.context = helper.getProjectionsPopulatedInDb(
				parameters,
				startAfter,
				String.format(multipleOptionsResponse.getOptionsUrl(), optionProjection.getStore())
		);
		response = this.context.getResponse();
	}

	/**
	 * Calls API to retrieve all the sku option projections populated in DB starting from the first response page or after specified  sku code.
	 *
	 * @param parameters URL query parameters
	 * @param store      store code
	 */
	@Then("^I retrieve option projections created in DB for store (.+)$")
	public void getOptionProjectionsByStoreCodePopulatedInDb(final String store, final Map<String, String> parameters) {
		ContextDrivenStepsHelper helper = new ContextDrivenStepsHelper(this.context);
		String startAfter = Optional.ofNullable(parameters.get("startAfterCode"))
				.filter(StringUtils::isNotEmpty)
				.orElse("");
		this.context = helper.getProjectionsPopulatedInDb(
				parameters,
				startAfter,
				String.format(multipleOptionsResponse.getOptionsUrl(), store)
		);
		response = this.context.getResponse();
	}

	/**
	 * Calls API to retrieve all the sku option projections populated in DB using startAfter parameter from the previous response.
	 *
	 * @param parameters URL query parameters
	 */
	@Then("^I retrieve option projections created in DB for created store starting after previously received response page$")
	public void getOptionProjectionsAfterPagePopulatedInDb(final Map<String, String> parameters) {
		ContextDrivenStepsHelper helper = new ContextDrivenStepsHelper(this.context);
		JsonPath body = context.getResponse().getBody().jsonPath();
		String startAfter = body.getString(MultipleOptionsApiResponse.START_AFTER);
		this.context = helper.getProjectionsPopulatedInDb(
				parameters,
				startAfter,
				String.format(multipleOptionsResponse.getOptionsUrl(), optionProjection.getStore())
		);
		response = this.context.getResponse();
	}

	/**
	 * Sends POST request to retrieve a single sku option projection populated in DB.
	 *
	 * @param urlStore store code which should be used in POST request Url
	 * @param store    store code which should be used in POST request body
	 * @param codes    a list of the options codes
	 */
	private void getOptionProjectionPost(final String urlStore, final String store, final List<String> codes) {
		ContextDrivenStepsHelper helper = new ContextDrivenStepsHelper(context);
		final String body = message.getPostBody(message.getOptionMessageName(), codes, store, message.getOptionEventType());
		this.context = helper.getProjectionsPost(body, urlStore, multipleOptionsResponse.getPostOptionUrl());
		response = this.context.getResponse();
	}

	/**
	 * Verifies that response contains full sku option projection information for option projection which was generated.
	 *
	 * @param option sku option projection expected values
	 */
	@Then("^Multiple options API response has same values as (?:latest |)(?:generated|updated) option projection$")
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
						multipleOptionsResponse.getType(skuCode), equalTo(option.get(KEY_TYPE)),
						multipleOptionsResponse.getCode(skuCode), equalTo(skuCode),
						multipleOptionsResponse.getStore(skuCode), equalTo(storeCode),
						multipleOptionsResponse.getModifiedDate(skuCode), notNullValue(),
						multipleOptionsResponse.getDeleted(skuCode), equalTo(Boolean.parseBoolean(option.get(KEY_DELETED))),
						multipleOptionsResponse.getDisplayNamePath(skuCode, option.get(SINGLE_LANG_KEY)), equalTo(skuName),
						multipleOptionsResponse.getFirstOptionValuePath(skuCode, option.get(SINGLE_LANG_KEY)), equalTo(skuValueCode),
						multipleOptionsResponse.getFirstOptionValueNamePath(skuCode, option.get(SINGLE_LANG_KEY)), equalTo(skuValueName)
				);
	}

	/**
	 * Verifies that response contains full sku option projection information for the first option projection which was generated.
	 *
	 * @param option sku option projection expected values
	 */
	@Then("^Multiple options API response has same values as the first generated option projection$")
	public void checkFirstGeneratedOptionProjection(final Map<String, String> option) {
		String skuCode = Optional.ofNullable(option.get("code"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(objectsContext.getFirstAddedProjection()::getCode);
		String storeCode = Optional.ofNullable(option.get("store"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(objectsContext.getFirstAddedProjection()::getStore);
		response
				.then()
				.assertThat()
				.body(
						multipleOptionsResponse.getType(skuCode), equalTo(option.get(KEY_TYPE)),
						multipleOptionsResponse.getCode(skuCode), equalTo(skuCode),
						multipleOptionsResponse.getStore(skuCode), equalTo(storeCode),
						multipleOptionsResponse.getModifiedDate(skuCode), notNullValue(),
						multipleOptionsResponse.getDeleted(skuCode), equalTo(Boolean.parseBoolean(option.get(KEY_DELETED))),
						multipleOptionsResponse.getDisplayNamePath(skuCode, option.get(SINGLE_LANG_KEY)), equalTo(option.get("displayName")),
						multipleOptionsResponse.getFirstOptionValuePath(skuCode, option.get(SINGLE_LANG_KEY)), equalTo(option.get("optionValue")),
						multipleOptionsResponse.getFirstOptionValueNamePath(skuCode, option.get(SINGLE_LANG_KEY)),
						equalTo(option.get("optionDisplayValue"))
				);
	}

	/**
	 * Verifies that response contains full option projection information for all the created option projections.
	 *
	 * @param options sku option projections expected values
	 */
	@Then("^Multiple options API response has the same values as all the generated option projections$")
	public void checkAllGeneratedOptionProjection(final Map<String, String> options) {
		List<String> skuCodes = objectsContext.getProjectionsCodes();
		checkSpecifiedOptionProjection(options, skuCodes);
	}

	/**
	 * Verifies that response contains full option projection information for provided option projections.
	 *
	 * @param options sku option projections expected values
	 */
	@Then("^Multiple options API response has the same values as only provided option projections$")
	public void checkProvidedOptionProjection(final Map<String, String> options) {
		List<String> skuCodes = new ArrayList<>();
		List<String> generatedSkuCodes = objectsContext.getProjectionsCodes();
		List<String> optionsDisplayNames = StepsHelper.parseByComma(options.get("displayNames"));
		for (String generatedCode : generatedSkuCodes) {
			for (String providedName : optionsDisplayNames) {
				if (objectsContext.getProjection(generatedCode).getContent().contains(providedName)) {
					skuCodes.add(generatedCode);
				}
			}
		}
		checkSpecifiedOptionProjection(options, skuCodes);
	}

	/**
	 * Verifies that response contains full option projection information for provided option projections.
	 *
	 * @param options sku option projections expected values
	 */
	private void checkSpecifiedOptionProjection(final Map<String, String> options, final List<String> skuCodes) {
		String storeCode = objectsContext.getFirstAddedProjection().getStore();
		List<String> optionsDisplayNames = StepsHelper.parseByComma(options.get("displayNames"));
		List<String> optionsValues = StepsHelper.parseByComma(options.get("optionValues"));
		List<String> optionsDisplayValues = StepsHelper.parseByComma(options.get("optionDisplayValues"));
		int codeIndex = 0;
		for (String skuCode : skuCodes) {
			response
					.then()
					.assertThat()
					.body(
							multipleOptionsResponse.getType(skuCode), equalTo(options.get(KEY_TYPE)),
							multipleOptionsResponse.getCode(skuCode), equalTo(skuCode),
							multipleOptionsResponse.getStore(skuCode), equalTo(storeCode),
							multipleOptionsResponse.getModifiedDate(skuCode), notNullValue(),
							multipleOptionsResponse.getDeleted(skuCode), equalTo(Boolean.parseBoolean(options.get(KEY_DELETED))),
							multipleOptionsResponse.getDisplayNamePath(skuCode, options.get(SINGLE_LANG_KEY)),
							equalTo(optionsDisplayNames.get(codeIndex)),
							multipleOptionsResponse.getFirstOptionValuePath(skuCode, options.get(SINGLE_LANG_KEY)),
							equalTo(optionsValues.get(codeIndex)),
							multipleOptionsResponse.getFirstOptionValueNamePath(skuCode, options.get(SINGLE_LANG_KEY)),
							equalTo(optionsDisplayValues.get(codeIndex))
					);
			codeIndex++;
		}
		Collections.sort(skuCodes);
		response
				.then()
				.assertThat()
				.body(
						multipleOptionsResponse.getAllCodesPath(), contains(skuCodes.toArray())
				);
	}

	/**
	 * Verifies that response contains empty results.
	 */
	@Then("^Multiple options API response has empty results$")
	public void checkEmptyResult() {
		response
				.then()
				.assertThat()
				.body(
						MultipleOptionsApiResponse.RESULTS, empty()
				);
	}

	/**
	 * Verifies that response contains full option projection information for specified pagination parameters and page index.
	 *
	 * @param pageSize max number of projections in API response JSON results element
	 * @param options  sku option projections expected values
	 */
	@Then("^Multiple options API response page with size (\\d+) has correct option projections and pagination block$")
	public void checkResponsePageOptionProjections(final int pageSize, final Map<String, String> options) {
		int sublistFrom = 0;
		List<String> responseSkuCodes = objectsContext.getProjectionsCodes();
		Collections.sort(responseSkuCodes);
		if (!"".equals(context.getStartAfter())) {
			sublistFrom = StepsHelper.getStartIndex(responseSkuCodes, context.getStartAfter());
		}
		if (sublistFrom == -1) {
			checkEmptyResult();
			return;
		}
		int sublistTo = sublistFrom + pageSize;
		if (sublistTo >= responseSkuCodes.size()) {
			sublistTo = responseSkuCodes.size();
		}
		responseSkuCodes = responseSkuCodes.subList(sublistFrom, sublistTo);
		String startAfter = "";
		if (Boolean.parseBoolean(options.get("hasMoreResults"))) {
			startAfter = responseSkuCodes.get(responseSkuCodes.size() - 1);
		}
		String storeCode = objectsContext.getFirstAddedProjection().getStore();
		List<String> optionsDisplayNames = StepsHelper.parseByComma(options.get("displayNames"));
		List<String> optionsValues = StepsHelper.parseByComma(options.get("optionValues"));
		List<String> optionsDisplayValues = StepsHelper.parseByComma(options.get("optionDisplayValues"));
		List<String> skuCodes = objectsContext.getProjectionsCodes();
		int codeIndex;
		for (String responseSkuCode : responseSkuCodes) {
			codeIndex = 0;
			for (String skuCode : skuCodes) {
				if (skuCode.equals(responseSkuCode)) {
					response
							.then()
							.assertThat()
							.body(
									multipleOptionsResponse.getType(skuCode), equalTo(options.get(KEY_TYPE)),
									multipleOptionsResponse.getCode(skuCode), equalTo(skuCode),
									multipleOptionsResponse.getStore(skuCode), equalTo(storeCode),
									multipleOptionsResponse.getModifiedDate(skuCode), notNullValue(),
									multipleOptionsResponse.getDeleted(skuCode), equalTo(Boolean.parseBoolean(options.get(KEY_DELETED))),
									multipleOptionsResponse.getDisplayNamePath(skuCode, options.get(SINGLE_LANG_KEY)),
									equalTo(optionsDisplayNames.get(codeIndex)),
									multipleOptionsResponse.getFirstOptionValuePath(skuCode, options.get(SINGLE_LANG_KEY)),
									equalTo(optionsValues.get(codeIndex)),
									multipleOptionsResponse.getFirstOptionValueNamePath(skuCode, options.get(SINGLE_LANG_KEY)),
									equalTo(optionsDisplayValues.get(codeIndex))
							);
					break;
				}
				codeIndex++;
			}
		}
		response
				.then()
				.assertThat()
				.body(
						multipleOptionsResponse.getAllCodesPath(), contains(responseSkuCodes.toArray())
				);
		response
				.then()
				.assertThat()
				.body(
						MultipleOptionsApiResponse.LIMIT, equalTo(pageSize),
						MultipleOptionsApiResponse.START_AFTER, equalTo(startAfter),
						MultipleOptionsApiResponse.HAS_MORE_RESULTS, equalTo(Boolean.parseBoolean(options.get("hasMoreResults")))
				);
	}

	/**
	 * Verifies that response contains pagination block with correct parameters.
	 *
	 * @param pagination sku option projections expected values
	 */
	@Then("^Multiple options API response has pagination block$")
	public void checkOptionProjectionPagination(final Map<String, String> pagination) {
		String startAfterCode = Optional.ofNullable(pagination.get("startAfter"))
				.filter(StringUtils::isNotEmpty)
				.orElse("");
		response
				.then()
				.assertThat()
				.body(
						MultipleOptionsApiResponse.LIMIT, equalTo(Integer.parseInt(pagination.get("limit"))),
						MultipleOptionsApiResponse.START_AFTER, equalTo(startAfterCode),
						MultipleOptionsApiResponse.HAS_MORE_RESULTS, equalTo(Boolean.parseBoolean(pagination.get("hasMoreResults")))
				);
	}

	/**
	 * Verifies that response contains non empty currentDateTime.
	 */
	@Then("^Multiple options API response has non empty currentDateTime$")
	public void checkOptionProjectionCurrentDateTime() {
		response
				.then()
				.assertThat()
				.body(
						MultipleOptionsApiResponse.CURRENT_DATE_TIME, notNullValue()
				);
	}

	/**
	 * Verifies that response does not contain currentDateTime.
	 */
	@Then("^Multiple options API response does not have currentDateTime element$")
	public void checkOptionProjectionNoCurrentDateTime() {
		response
				.then()
				.assertThat()
				.body(
						multipleOptionsResponse.getExistenceCheckPath(MultipleOptionsApiResponse.CURRENT_DATE_TIME), is(false)
				);
	}

	/**
	 * Verifies that response does not contain the first generated option projection.
	 */
	@Then("^Multiple options API response does not have the first generated option projection$")
	public void checkExcludedFirstGeneratedOptionProjection() {
		response
				.then()
				.assertThat()
				.body(
						multipleOptionsResponse.getCodePath(), not(hasItem(objectsContext.getFirstAddedProjection().getCode()))
				);
	}
}
