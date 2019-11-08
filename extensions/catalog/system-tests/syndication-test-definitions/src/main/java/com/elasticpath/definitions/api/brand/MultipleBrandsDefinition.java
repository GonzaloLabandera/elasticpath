package com.elasticpath.definitions.api.brand;

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
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;

import com.elasticpath.definitions.api.helpers.ContextDrivenStepsHelper;
import com.elasticpath.definitions.api.helpers.StepsHelper;
import com.elasticpath.definitions.stateobjects.Context;
import com.elasticpath.definitions.stateobjects.ObjectsContext;
import com.elasticpath.definitions.stateobjects.Projection;
import com.elasticpath.definitions.stateobjects.content.BrandProjectionContent;
import com.elasticpath.definitions.testobjects.JmsCatalogEventMessage;
import com.elasticpath.definitions.testobjects.MultipleBrandsApiResponse;


/**
 * Syndication API Brand steps.
 */
public class MultipleBrandsDefinition {

	private static final String SINGLE_LANG_KEY = "languageLocale";
	private static final String KEY_TYPE = "type";
	private static final String KEY_DELETED = "deleted";
	private final ObjectsContext objectsContext;
	private final Projection brandProjection;
	private final BrandProjectionContent brandProjectionContent;
	private final MultipleBrandsApiResponse multipleBrandsResponse;
	private final JmsCatalogEventMessage message;
	private Context context;
	private Response response;

	/**
	 * Constructor.
	 *
	 * @param context                context state object
	 * @param objectsContext         objects context state object
	 * @param projection             brand projection state object
	 * @param brandProjectionContent brand projection content state object
	 */
	public MultipleBrandsDefinition(final Context context, final ObjectsContext objectsContext,
									final Projection projection, final BrandProjectionContent brandProjectionContent) {
		this.context = context;
		this.objectsContext = objectsContext;
		this.response = this.context.getResponse();
		this.brandProjection = projection;
		this.brandProjectionContent = brandProjectionContent;
		multipleBrandsResponse = new MultipleBrandsApiResponse();
		message = new JmsCatalogEventMessage();
	}

	/**
	 * Calls API to retrieve a brand projections populated in DB by POST request with body.
	 */
	@Then("^I retrieve a created in DB brand projection for created store via POST request$")
	public void getBrandProjectionPopulatedInDbPost() {
		getBrandProjectionPopulatedDbStoresPost(brandProjection.getStore());
	}

	/**
	 * Calls API to retrieve a brand projection populated in DB by POST request with body.
	 *
	 * @param store store code which should be used in POST request Url
	 */
	@Then("^I retrieve a created in DB brand projection for generated store via POST request when body contains store code (.+)$")
	public void getBrandProjectionPopulatedDbStoresPost(final String store) {
		getBrandProjectionPost(brandProjection.getStore(), store, Collections.singletonList(brandProjection.getCode()));
	}

	/**
	 * Calls API to retrieve a brand projection populated in DB by POST request with body.
	 */
	@Then("^I retrieve 2 created in DB brand projections for created store via POST request$")
	public void getBrandProjectionsPopulatedInDbPost() {
		List<String> codes = new ArrayList<>();
		codes.add(objectsContext.getFirstAddedProjection().getCode());
		codes.add(brandProjection.getCode());
		getBrandProjectionPost(brandProjection.getStore(), brandProjection.getStore(), codes);
	}

	/**
	 * Calls API to retrieve a brand projection populated in DB by POST request with body when codes list contains one non-existent brand code.
	 */
	@Then("^I retrieve a created in DB brand projection for created store via POST request when codes list contains one non-existent brand code$")
	public void getBrandProjectionsPopulatedInDbWrongCodePost() {
		List<String> codes = new ArrayList<>();
		codes.add(brandProjection.getCode());
		codes.add("NON-EXISTENT-CODE");
		getBrandProjectionPost(brandProjection.getStore(), brandProjection.getStore(), codes);
	}

	/**
	 * Calls API to retrieve a brand projection populated in DB by POST request with malformed body.
	 */
	@Then("^I retrieve a created in DB brand projection for created store via POST request with malformed body$")
	public void getBrandProjectionPopulatedInDbPostMalformedBody() {
		ContextDrivenStepsHelper helper = new ContextDrivenStepsHelper(context);
		final String body = "Malformed body";
		this.context = helper.getProjectionsPost(body, brandProjection.getStore(), multipleBrandsResponse.getPostUrl());
		this.context.getResponse().prettyPrint();
		response = this.context.getResponse();
	}

	/**
	 * Calls API to retrieve all the brand projections populated in DB starting from the first response page or after specified  sku code.
	 *
	 * @param parameters URL query parameters
	 */
	@Then("^I retrieve brand projections created in DB for created store$")
	public void getProjectionsFirstsOrAfterProvidedCodePopulatedInDb(final Map<String, String> parameters) {
		ContextDrivenStepsHelper helper = new ContextDrivenStepsHelper(this.context);
		String startAfter = Optional.ofNullable(parameters.get("startAfterCode"))
				.filter(StringUtils::isNotEmpty)
				.orElse("");
		this.context = helper.getProjectionsPopulatedInDb(
				parameters,
				startAfter,
				String.format(multipleBrandsResponse.getBrandsUrl(), brandProjection.getStore())
		);
		response = this.context.getResponse();
	}

	/**
	 * Calls API to retrieve all the brand projections populated in DB starting from the first response page or after specified  sku code.
	 *
	 * @param parameters URL query parameters
	 * @param store      store code
	 */
	@Then("^I retrieve brand projections created in DB for store (.+)$")
	public void getBrandProjectionsByStoreCodePopulatedInDb(final String store, final Map<String, String> parameters) {
		ContextDrivenStepsHelper helper = new ContextDrivenStepsHelper(this.context);
		String startAfter = Optional.ofNullable(parameters.get("startAfterCode"))
				.filter(StringUtils::isNotEmpty)
				.orElse("");
		this.context = helper.getProjectionsPopulatedInDb(
				parameters,
				startAfter,
				String.format(multipleBrandsResponse.getBrandsUrl(), store)
		);
		response = this.context.getResponse();
	}

	/**
	 * Calls API to retrieve all the brand projections populated in DB using startAfter parameter from the previous response.
	 *
	 * @param parameters URL query parameters
	 */
	@Then("^I retrieve brand projections created in DB for created store starting after previously received response page$")
	public void getBrandProjectionsAfterPagePopulatedInDb(final Map<String, String> parameters) {
		ContextDrivenStepsHelper helper = new ContextDrivenStepsHelper(this.context);
		JsonPath body = context.getResponse().getBody().jsonPath();
		String startAfter = body.getString(MultipleBrandsApiResponse.START_AFTER);
		this.context = helper.getProjectionsPopulatedInDb(
				parameters,
				startAfter,
				String.format(multipleBrandsResponse.getBrandsUrl(), brandProjection.getStore())
		);
		response = this.context.getResponse();
	}

	/**
	 * Sends POST request to retrieve brand projections populated in DB.
	 *
	 * @param urlStore store code which should be used in POST request Url
	 * @param store    store code which should be used in POST request body
	 * @param codes    a list of the brands codes
	 */
	private void getBrandProjectionPost(final String urlStore, final String store, final List<String> codes) {
		ContextDrivenStepsHelper helper = new ContextDrivenStepsHelper(context);
		final String body = message.getPostBody(message.getBrandMessageName(), codes, store, message.getBrandEventType());
		this.context = helper.getProjectionsPost(body, urlStore, multipleBrandsResponse.getPostUrl());
		this.context.getResponse().prettyPrint();
		response = this.context.getResponse();
	}

	/**
	 * Verifies that response contains full brand projection information for brand projection which was generated.
	 *
	 * @param brand brand projection expected values
	 */
	@Then("^Multiple brands API response has same values as (?:latest |)(?:generated|updated) brand projection$")
	public void checkGeneratedBrandProjection(final Map<String, String> brand) {
		String code = Optional.ofNullable(brand.get("code"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(brandProjection::getCode);
		String storeCode = Optional.ofNullable(brand.get("store"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(brandProjection::getStore);
		String brandName = Optional.ofNullable(brand.get("displayName"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(brandProjectionContent::getDisplayName);
		response
				.then()
				.assertThat()
				.body(
						multipleBrandsResponse.getType(code), equalTo(brand.get(KEY_TYPE)),
						multipleBrandsResponse.getCode(code), equalTo(code),
						multipleBrandsResponse.getStore(code), equalTo(storeCode),
						multipleBrandsResponse.getModifiedDate(code), notNullValue(),
						multipleBrandsResponse.getDeleted(code), equalTo(Boolean.parseBoolean(brand.get(KEY_DELETED))),
						multipleBrandsResponse.getDisplayNamePath(code, brand.get(SINGLE_LANG_KEY)), equalTo(brandName)
				);
	}

	/**
	 * Verifies that response contains full brand projection information for the first brand projection which was generated.
	 *
	 * @param brand brand projection expected values
	 */
	@Then("^Multiple brands API response has same values as the first generated brand projection$")
	public void checkFirstGeneratedBrandProjection(final Map<String, String> brand) {
		String code = Optional.ofNullable(brand.get("code"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(objectsContext.getFirstAddedProjection()::getCode);
		String storeCode = Optional.ofNullable(brand.get("store"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(objectsContext.getFirstAddedProjection()::getStore);
		response
				.then()
				.assertThat()
				.body(
						multipleBrandsResponse.getType(code), equalTo(brand.get(KEY_TYPE)),
						multipleBrandsResponse.getCode(code), equalTo(code),
						multipleBrandsResponse.getStore(code), equalTo(storeCode),
						multipleBrandsResponse.getModifiedDate(code), notNullValue(),
						multipleBrandsResponse.getDeleted(code), equalTo(Boolean.parseBoolean(brand.get(KEY_DELETED))),
						multipleBrandsResponse.getDisplayNamePath(code, brand.get(SINGLE_LANG_KEY)), equalTo(brand.get("displayName"))
				);
	}

	/**
	 * Verifies that response contains full brand projection information for all the created brand projections.
	 *
	 * @param brands brand projections expected values
	 */
	@Then("^Multiple brands API response has the same values as all the generated brand projections$")
	public void checkAllGeneratedBrandProjection(final Map<String, String> brands) {
		List<String> brandCodes = objectsContext.getProjectionsCodes();
		checkSpecifiedBrandProjection(brands, brandCodes);
	}

	/**
	 * Verifies that response contains full brand projection information for provided brand projections.
	 *
	 * @param brands brand projections expected values
	 */
	@Then("^Multiple brands API response has the same values as only provided brand projections$")
	public void checkProvidedBrandProjection(final Map<String, String> brands) {
		List<String> brandCodes = new ArrayList<>();
		List<String> generatedBrandCodes = objectsContext.getProjectionsCodes();
		List<String> brandsDisplayNames = StepsHelper.parseByComma(brands.get("displayNames"));
		for (String generatedCode : generatedBrandCodes) {
			for (String providedName : brandsDisplayNames) {
				if (objectsContext.getProjection(generatedCode).getContent().contains(providedName)) {
					brandCodes.add(generatedCode);
				}
			}
		}
		checkSpecifiedBrandProjection(brands, brandCodes);
	}

	/**
	 * Verifies that response contains full brand projection information for provided brand projections.
	 *
	 * @param brands brand projections expected values
	 * @param codes  brand codes of populated in DB projections
	 */
	private void checkSpecifiedBrandProjection(final Map<String, String> brands, final List<String> codes) {
		String storeCode = objectsContext.getFirstAddedProjection().getStore();
		List<String> brandsDisplayNames = StepsHelper.parseByComma(brands.get("displayNames"));
		int codeIndex = 0;
		for (String code : codes) {
			response
					.then()
					.assertThat()
					.body(
							multipleBrandsResponse.getType(code), equalTo(brands.get(KEY_TYPE)),
							multipleBrandsResponse.getCode(code), equalTo(code),
							multipleBrandsResponse.getStore(code), equalTo(storeCode),
							multipleBrandsResponse.getModifiedDate(code), notNullValue(),
							multipleBrandsResponse.getDeleted(code), equalTo(Boolean.parseBoolean(brands.get(KEY_DELETED))),
							multipleBrandsResponse.getDisplayNamePath(code, brands.get(SINGLE_LANG_KEY)), equalTo(brandsDisplayNames.get(codeIndex))
					);
			codeIndex++;
		}
		Collections.sort(codes);
		response
				.then()
				.assertThat()
				.body(
						multipleBrandsResponse.getAllCodesPath(), contains(codes.toArray())
				);
	}

	/**
	 * Verifies that response contains empty results.
	 */
	@Then("^Multiple brands API response has empty results$")
	public void checkEmptyResult() {
		response
				.then()
				.assertThat()
				.body(
						MultipleBrandsApiResponse.RESULTS, empty()
				);
	}

	/**
	 * Verifies that response contains full brand projection information for specified pagination parameters and page index.
	 *
	 * @param pageSize max number of projections in API response JSON results element
	 * @param brands   brand projections expected values
	 */
	@Then("^Multiple brands API response page with size (\\d+) has correct brand projections and pagination block$")
	public void checkResponsePageBrandProjections(final int pageSize, final Map<String, String> brands) {
		int sublistFrom = 0;
		List<String> responseBrandCodes = objectsContext.getProjectionsCodes();
		Collections.sort(responseBrandCodes);
		if (!"".equals(this.context.getStartAfter())) {
			sublistFrom = StepsHelper.getStartIndex(responseBrandCodes, this.context.getStartAfter());
		}
		if (sublistFrom == -1) {
			checkEmptyResult();
			return;
		}
		int sublistTo = sublistFrom + pageSize;
		if (sublistTo >= responseBrandCodes.size()) {
			sublistTo = responseBrandCodes.size();
		}
		responseBrandCodes = responseBrandCodes.subList(sublistFrom, sublistTo);
		String startAfter = "";
		if (Boolean.parseBoolean(brands.get("hasMoreResults"))) {
			startAfter = responseBrandCodes.get(responseBrandCodes.size() - 1);
		}
		String storeCode = objectsContext.getFirstAddedProjection().getStore();
		List<String> brandsDisplayNames = StepsHelper.parseByComma(brands.get("displayNames"));
		List<String> brandCodes = objectsContext.getProjectionsCodes();
		int codeIndex;
		for (String responseBrandCode : responseBrandCodes) {
			codeIndex = 0;
			for (String brandCode : brandCodes) {
				if (brandCode.equals(responseBrandCode)) {
					response
							.then()
							.assertThat()
							.body(
									multipleBrandsResponse.getType(brandCode), equalTo(brands.get(KEY_TYPE)),
									multipleBrandsResponse.getCode(brandCode), equalTo(brandCode),
									multipleBrandsResponse.getStore(brandCode), equalTo(storeCode),
									multipleBrandsResponse.getModifiedDate(brandCode), notNullValue(),
									multipleBrandsResponse.getDeleted(brandCode), equalTo(Boolean.parseBoolean(brands.get(KEY_DELETED))),
									multipleBrandsResponse.getDisplayNamePath(brandCode, brands.get(SINGLE_LANG_KEY)),
									equalTo(brandsDisplayNames.get(codeIndex))
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
						multipleBrandsResponse.getAllCodesPath(), contains(responseBrandCodes.toArray())
				);
		response
				.then()
				.assertThat()
				.body(
						MultipleBrandsApiResponse.LIMIT, equalTo(pageSize),
						MultipleBrandsApiResponse.START_AFTER, equalTo(startAfter),
						MultipleBrandsApiResponse.HAS_MORE_RESULTS, equalTo(Boolean.parseBoolean(brands.get("hasMoreResults")))
				);
	}

	/**
	 * Verifies that response contains pagination block with correct parameters.
	 *
	 * @param pagination sku brand projections expected values
	 */
	@Then("^Multiple brands API response has pagination block$")
	public void checkBrandProjectionPagination(final Map<String, String> pagination) {
		String startAfterCode = Optional.ofNullable(pagination.get("startAfter"))
				.filter(StringUtils::isNotEmpty)
				.orElse("");
		response
				.then()
				.assertThat()
				.body(
						MultipleBrandsApiResponse.LIMIT, equalTo(Integer.parseInt(pagination.get("limit"))),
						MultipleBrandsApiResponse.START_AFTER, equalTo(startAfterCode),
						MultipleBrandsApiResponse.HAS_MORE_RESULTS, equalTo(Boolean.parseBoolean(pagination.get("hasMoreResults")))
				);
	}

	/**
	 * Verifies that response contains non empty currentDateTime.
	 */
	@Then("^Multiple brands API response has non empty currentDateTime$")
	public void checkBrandProjectionCurrentDateTime() {
		response
				.then()
				.assertThat()
				.body(
						MultipleBrandsApiResponse.CURRENT_DATE_TIME, notNullValue()
				);
	}

	/**
	 * Verifies that response does not contain currentDateTime.
	 */
	@Then("^Multiple brands API response does not have currentDateTime element$")
	public void checkBrandProjectionNoCurrentDateTime() {
		response
				.then()
				.assertThat()
				.body(
						multipleBrandsResponse.getExistenceCheckPath(MultipleBrandsApiResponse.CURRENT_DATE_TIME), is(false)
				);
	}

	/**
	 * Verifies that response does not contain the first generated brand projection.
	 */
	@Then("^Multiple brands API response does not have the first generated brand projection$")
	public void checkExcludedFirstGeneratedBrandProjection() {
		response
				.then()
				.assertThat()
				.body(
						multipleBrandsResponse.getCodePath(), not(hasItem(objectsContext.getFirstAddedProjection().getCode()))
				);
	}
}
