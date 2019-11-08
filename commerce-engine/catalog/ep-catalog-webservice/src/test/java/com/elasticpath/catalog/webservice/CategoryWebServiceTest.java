/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.webservice;

import static io.restassured.RestAssured.expect;
import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.servlet.ServletException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.catalina.LifecycleException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import com.elasticpath.catalog.entity.AvailabilityRules;
import com.elasticpath.catalog.entity.ProjectionProperties;
import com.elasticpath.catalog.entity.Property;
import com.elasticpath.catalog.entity.category.Category;
import com.elasticpath.catalog.entity.category.CategoryProperties;
import com.elasticpath.catalog.entity.translation.CategoryTranslation;
import com.elasticpath.catalog.reader.FindAllResponse;
import com.elasticpath.catalog.reader.PaginationResponse;
import com.elasticpath.catalog.reader.impl.FindAllResponseImpl;
import com.elasticpath.catalog.reader.impl.PaginationResponseImpl;
import com.elasticpath.catalog.webservice.exception.InvalidRequestParameterException;
import com.elasticpath.catalog.webservice.request.entity.EventType;
import com.elasticpath.catalog.webservice.request.entity.RequestBody;
import com.elasticpath.catalog.webservice.request.validator.RequestBodyValidator;
import com.elasticpath.catalog.webservice.services.CategoryService;
/**
 * Tests CategoryWebServiceRouteBuilder.
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports"})
public class CategoryWebServiceTest extends CamelTestSupport {

	private static final String STORE = "store";
	private static final String EXIST_CODE = "existCode";
	private static final String NOT_EXIST_CODE = "notExistCode";
	private static final String INVALID_CODE = "invalidCode";
	private static final String WEB_XML_PATH = "src/test/webapp";
	private static final int TOMCAT_PORT = 8090;
	private static final String CATALOG_URL = "http://localhost:" + TOMCAT_PORT + "/api/syndication/v1/catalog/";
	private static final String CATEGORIES_URI = "/categories/";
	private static final String CATEGORY_EVENTS = "/categoryevents/";
	private static final String CATEGORY_CHILDREN = "/children/";
	private static final String E_TAG_HEADER = "ETag";
	private static final String IF_NONE_MATCH_HEADER = "If-None-Match";
	private static final String CONTENT_LENGTH_HEADER = "Content-Length";
	private static final TomcatRunner TOMCAT_RUNNER = new TomcatRunner(WEB_XML_PATH, TOMCAT_PORT);
	private static final int DEFAULT_LIMIT = 10;
	private static final String EMPTY_START_AFTER = "";
	private static final boolean HAS_MORE_RESULTS = true;
	private static final boolean NONE_MORE_RESULTS = false;
	private static final ZonedDateTime CURRENT_DATE = ZonedDateTime.now();
	private static final ZonedDateTime NONE_CURRENT_DATE_TIME = null;
	private static final List<Category> EMPTY_RESULTS = new ArrayList<>();
	private static final String LAST_CATEGORY_IN_RESPONSE = "lastCategoryInResponse";
	private static final String PAGINATION_NEXT_LIMIT = "pagination.next.limit";
	private static final String PAGINATION_NEXT_START_AFTER = "pagination.next.startAfter";
	private static final String PAGINATION_NEXT_HAS_MORE_RESULTS = "pagination.next.hasMoreResults";
	private static final String CURRENT_DATE_TIME = "currentDateTime";
	private static final String RESULTS = "results";
	private static final String RESULTS_SIZE = "results.size()";

	private static final String LIMIT = "limit";
	private static final String START_AFTER = "startAfter";

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Mock
	private CategoryService categoryService;

	@Mock
	private RequestBodyValidator requestBodyValidator;

	@InjectMocks
	private CatalogWebServiceRestEndpointsRouteBuilder catalogWebServiceRestEndpointsRouteBuilder;

	@InjectMocks
	private CategoryWebServiceRouteBuilder categoryWebServiceRouteBuilder;

	@BeforeClass
	public static void setUpClass() throws ServletException, LifecycleException {
		TOMCAT_RUNNER.startUpTomcat();
	}

	@AfterClass
	public static void shutDownClass() throws LifecycleException {
		TOMCAT_RUNNER.shutDownTomcat();
	}

	@Before
	public void setUpTest() {
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		when(requestBodyValidator.validate(any(RequestBody.class))).thenReturn(true);
	}

	@Test
	public void testThatShouldReturnStatus200WhenCategoryCodeExist() {
		when(categoryService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockCategory()));

		given()
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI + EXIST_CODE)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void testThatShouldReturnContentTypeHeaderEqualsJsonWhenCategoryCodeExist() {
		when(categoryService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockCategory()));

		given()
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI + EXIST_CODE)
				.then()
				.contentType(JSON);
	}

	@Test
	public void testThatShouldReturnETagHeaderWhenCategoryCodeExist() {
		when(categoryService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockCategory()));

		given()
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI + EXIST_CODE)
				.then()
				.header(E_TAG_HEADER, is(not(isEmptyString())));
	}


	@Test
	public void testThatShouldReturnContentLengthHeaderWhenCategoryCodeExist() {
		when(categoryService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockCategory()));

		given()
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI + EXIST_CODE)
				.then()
				.header(CONTENT_LENGTH_HEADER, is(not(isEmptyString())));
	}

	@Test
	public void testThatShouldReturnStatus404WhenCategoryCodeNotExist() {
		given()
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI + NOT_EXIST_CODE)
				.then()
				.statusCode(HttpStatus.NOT_FOUND.value());
	}

	@Test
	public void testThatShouldReturnStatus304WhenGetCategoryAgainWithETagFromPreviousResult() {
		when(categoryService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockCategory()));
		final String eTag = get(CATALOG_URL + STORE + CATEGORIES_URI + EXIST_CODE).header(E_TAG_HEADER);

		given()
				.header(IF_NONE_MATCH_HEADER, eTag)
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI + EXIST_CODE)
				.then()
				.statusCode(HttpStatus.NOT_MODIFIED.value());
	}

	@Test
	public void testThatShouldReturnStatus200WhenGetCategoryAgainWithETagFromPreviousResultAndCategoryAlreadyUpdated() {
		when(categoryService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockCategory()));
		final String eTag = get(CATALOG_URL + STORE + CATEGORIES_URI + EXIST_CODE).header(E_TAG_HEADER);
		when(categoryService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockCategory()));

		given()
				.header(IF_NONE_MATCH_HEADER, eTag)
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI + EXIST_CODE)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnModifiedCategoryWhenGetCategoryAgainWithETagFromPreviousResultAndCategoryAlreadyUpdated() throws JsonProcessingException {
		when(categoryService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockCategory()));
		final String eTag = get(CATALOG_URL + STORE + CATEGORIES_URI + EXIST_CODE).header(E_TAG_HEADER);

		final Category modifiedCategory = mockCategory();
		when(categoryService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(modifiedCategory));

		given()
				.header(IF_NONE_MATCH_HEADER, eTag)
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI + EXIST_CODE)
				.then()
				.body(is(objectMapper.writeValueAsString(modifiedCategory)));
	}

	@Test
	public void testShouldReturnStatus200WhenRequestWithoutParametersAndLessThan10CategoriesInStore() {
		final FindAllResponse<Category> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockCategory(), mockCategory()));

		when(categoryService.getAllCategories(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnPaginationNextLimit10WhenRequestWithoutParametersAndLessThan10CategoriesInStore() {
		final FindAllResponse<Category> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockCategory(), mockCategory()));

		when(categoryService.getAllCategories(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI)
				.then()
				.body(PAGINATION_NEXT_LIMIT, is(DEFAULT_LIMIT));
	}

	@Test
	public void shouldReturnEmptyPaginationNextStartAfterWhenRequestWithoutParametersAndLessThan10CategoriesInStore() {
		final FindAllResponse<Category> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockCategory(), mockCategory()));

		when(categoryService.getAllCategories(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI)
				.then()
				.body(PAGINATION_NEXT_START_AFTER, isEmptyString());
	}

	@Test
	public void shouldReturnPaginationNextHasMoreResultsFalseWhenRequestWithoutParametersAndLessThan10CategoriesInStore() {
		final FindAllResponse<Category> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockCategory(), mockCategory()));

		when(categoryService.getAllCategories(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI)
				.then()
				.body(PAGINATION_NEXT_HAS_MORE_RESULTS, is(false));
	}

	@Test
	public void shouldReturnPaginationNextCurrentDateTimeWhenRequestWithoutParametersAndLessThan10CategoriesInStore() {
		final FindAllResponse<Category> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockCategory(), mockCategory()));

		when(categoryService.getAllCategories(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI)
				.then()
				.body(CURRENT_DATE_TIME, not(isEmptyString()));
	}

	@Test
	public void shouldReturnStatus200WhenRequestWithLimit2AndExistMoreCategoriesInStore() {
		final FindAllResponse<Category> findAllResponse = mockFindAllResponse(2, LAST_CATEGORY_IN_RESPONSE, HAS_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockCategory(), mockCategory()));

		when(categoryService.getAllCategories(STORE, "2", null, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnPaginationNextLimit2WhenRequestWithLimit2AndExistMoreCategoriesInStore() {
		final FindAllResponse<Category> findAllResponse = mockFindAllResponse(2, LAST_CATEGORY_IN_RESPONSE, HAS_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockCategory(), mockCategory()));

		when(categoryService.getAllCategories(STORE, "2", null, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI)
				.then()
				.body(PAGINATION_NEXT_LIMIT, is(2));
	}

	@Test
	public void shouldReturnPaginationNextStartAfterBWhenRequestWithLimit2AndExistMoreCategoriesInStore() {
		final FindAllResponse<Category> findAllResponse = mockFindAllResponse(2, LAST_CATEGORY_IN_RESPONSE, HAS_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockCategory(), mockCategory()));

		when(categoryService.getAllCategories(STORE, "2", null, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI)
				.then()
				.body(PAGINATION_NEXT_START_AFTER, is(LAST_CATEGORY_IN_RESPONSE));
	}

	@Test
	public void shouldReturnPaginationNextHasMoreResultsTrueWhenRequestWithLimit2AndExistMoreCategoriesInStore() {
		final FindAllResponse<Category> findAllResponse = mockFindAllResponse(2, LAST_CATEGORY_IN_RESPONSE, HAS_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockCategory(), mockCategory()));

		when(categoryService.getAllCategories(STORE, "2", null, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI)
				.then()
				.body(PAGINATION_NEXT_HAS_MORE_RESULTS, is(true));
	}

	@Test
	public void shouldReturnPaginationNextCurrentDateTimeWhenRequestWithLimit2AndExistMoreCategoriesInStore() {
		final FindAllResponse<Category> findAllResponse = mockFindAllResponse(2, LAST_CATEGORY_IN_RESPONSE, HAS_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockCategory(), mockCategory()));

		when(categoryService.getAllCategories(STORE, "2", null, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI)
				.then()
				.body(CURRENT_DATE_TIME, not(isEmptyString()));
	}

	@Test
	public void shouldReturnStatus200WhenRequestWithLimit2AndStartAfterExistCodeAndExistMoreCategoriesInStore() {
		final FindAllResponse<Category> findAllResponse = mockFindAllResponse(2, LAST_CATEGORY_IN_RESPONSE, HAS_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockCategory(), mockCategory()));

		when(categoryService.getAllCategories(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnPaginationNextLimit2WhenRequestWithLimit2AndStartAfterExistCodeAndExistMoreCategoriesInStore() {
		final FindAllResponse<Category> findAllResponse = mockFindAllResponse(2, LAST_CATEGORY_IN_RESPONSE, HAS_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockCategory(), mockCategory()));

		when(categoryService.getAllCategories(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI)
				.then()
				.body(PAGINATION_NEXT_LIMIT, is(2));
	}

	@Test
	public void shouldReturnPaginationNextStartAfterDWhenRequestWithLimit2AndStartAfterExistCodeAndExistMoreCategoriesInStore() {
		final FindAllResponse<Category> findAllResponse = mockFindAllResponse(2, LAST_CATEGORY_IN_RESPONSE, HAS_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockCategory(), mockCategory()));

		when(categoryService.getAllCategories(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI)
				.then()
				.body(PAGINATION_NEXT_START_AFTER, is(LAST_CATEGORY_IN_RESPONSE));
	}

	@Test
	public void shouldReturnPaginationNextHasMoreResultsTrueWhenRequestWithLimit2AndStartAfterExistCodeAndExistMoreCategoriesInStore() {
		final FindAllResponse<Category> findAllResponse = mockFindAllResponse(2, LAST_CATEGORY_IN_RESPONSE, HAS_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockCategory(), mockCategory()));

		when(categoryService.getAllCategories(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI)
				.then()
				.body(PAGINATION_NEXT_HAS_MORE_RESULTS, is(true));
	}

	@Test
	public void shouldNotReturnPaginationNextCurrentDateTimeWhenRequestWithLimit2AndStartAfterExistCodeAndExistMoreCategoriesInStore() {
		final FindAllResponse<Category> findAllResponse = mockFindAllResponse(2, LAST_CATEGORY_IN_RESPONSE, HAS_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockCategory(), mockCategory()));

		when(categoryService.getAllCategories(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI)
				.then()
				.body(CURRENT_DATE_TIME, not(expect()));
	}

	@Test
	public void shouldReturnStatus200WhenRequestWithLimit2AndStartAfterExistCodeAndNoMoreCategoriesInStore() {
		final FindAllResponse<Category> findAllResponse = mockFindAllResponse(2, EMPTY_START_AFTER, NONE_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockCategory(), mockCategory()));

		when(categoryService.getAllCategories(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnPaginationNextLimit2WhenRequestWithLimit2AndStartAfterExistCodeAndNoMoreCategoriesInStore() {
		final FindAllResponse<Category> findAllResponse = mockFindAllResponse(2, EMPTY_START_AFTER, NONE_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockCategory(), mockCategory()));

		when(categoryService.getAllCategories(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI)
				.then()
				.body(PAGINATION_NEXT_LIMIT, is(2));
	}

	@Test
	public void shouldReturnEmptyPaginationNextStartAfterWhenRequestWithLimit2AndStartAfterExistCodeAndNoMoreCategoriesInStore() {
		final FindAllResponse<Category> findAllResponse = mockFindAllResponse(2, EMPTY_START_AFTER, NONE_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockCategory(), mockCategory()));

		when(categoryService.getAllCategories(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI)
				.then()
				.body(PAGINATION_NEXT_START_AFTER, isEmptyString());
	}

	@Test
	public void shouldReturnPaginationNextHasMoreResultsFalseWhenRequestWithLimit2AndStartAfterExistCodeAndNoMoreCategoriesInStore() {
		final FindAllResponse<Category> findAllResponse = mockFindAllResponse(2, EMPTY_START_AFTER, NONE_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockCategory(), mockCategory()));

		when(categoryService.getAllCategories(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI)
				.then()
				.body(PAGINATION_NEXT_HAS_MORE_RESULTS, is(false));
	}

	@Test
	public void shouldNotReturnPaginationNextCurrentDateTimeWhenRequestWithLimit2AndStartAfterExistCodeAndNoMoreCategoriesInStore() {
		final FindAllResponse<Category> findAllResponse = mockFindAllResponse(2, EMPTY_START_AFTER, NONE_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockCategory(), mockCategory()));

		when(categoryService.getAllCategories(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI)
				.then()
				.body(CURRENT_DATE_TIME, not(expect()));
	}

	@Test
	public void shouldReturnStatus200WhenRequestWithoutParametersAndNoCategoriesInStore() {
		final FindAllResponse<Category> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				EMPTY_RESULTS);

		when(categoryService.getAllCategories(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnPaginationNextLimit10WhenRequestWithoutParametersAndNoCategoriesInStore() {
		final FindAllResponse<Category> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				EMPTY_RESULTS);

		when(categoryService.getAllCategories(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI)
				.then()
				.body(PAGINATION_NEXT_LIMIT, is(DEFAULT_LIMIT));
	}

	@Test
	public void shouldReturnEmptyPaginationNextStartAfterWhenRequestWithoutParametersAndNoCategoriesInStore() {
		final FindAllResponse<Category> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				EMPTY_RESULTS);

		when(categoryService.getAllCategories(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI)
				.then()
				.body(PAGINATION_NEXT_START_AFTER, isEmptyString());
	}

	@Test
	public void shouldReturnPaginationNextHasMoreResultsFalseWhenRequestWithoutParametersAndNoCategoriesInStore() {
		final FindAllResponse<Category> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				EMPTY_RESULTS);

		when(categoryService.getAllCategories(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI)
				.then()
				.body(PAGINATION_NEXT_HAS_MORE_RESULTS, is(false));
	}

	@Test
	public void shouldReturnPaginationNextCurrentDateTimeWhenRequestWithoutParametersAndNoCategoriesInStore() {
		final FindAllResponse<Category> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				EMPTY_RESULTS);

		when(categoryService.getAllCategories(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI)
				.then()
				.body(CURRENT_DATE_TIME, not(isEmptyString()));
	}

	@Test
	public void shouldReturnEmptyResultsWhenRequestWithoutParametersAndNoCategoriesInStore1() {
		final FindAllResponse<Category> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				EMPTY_RESULTS);

		when(categoryService.getAllCategories(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI)
				.then()
				.body(RESULTS, empty());
	}

	@Test
	public void testShouldReturnStatus200WhenRequestBodyContainingSingleCategoryCode() {
		when(categoryService.getLatestCategoriesWithCodes(any(), anyList())).thenReturn(Collections.singletonList(mockCategory()));

		final RequestBody requestBody = createValidRequestBodyWithCodes(Collections.singletonList(EXIST_CODE));

		given()
				.contentType(JSON)
				.body(requestBody)
				.when()
				.post(CATALOG_URL + STORE + CATEGORY_EVENTS)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnStatus200WhenRequestBodyContainingMultipleCategoryCodes() {
		when(categoryService.getLatestCategoriesWithCodes(any(), anyList())).thenReturn(Arrays.asList(mockCategory(), mockCategory()));

		final RequestBody requestBody = createValidRequestBodyWithCodes(Arrays.asList(EXIST_CODE, EXIST_CODE));

		given()
				.contentType(JSON)
				.body(requestBody)
				.when()
				.post(CATALOG_URL + STORE + CATEGORY_EVENTS)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnStatus206WhenRequestBodyContainingMultipleCategoryCodesOneOfWhichInvalid() {
		when(categoryService.getLatestCategoriesWithCodes(any(), anyList())).thenReturn(Collections.singletonList(mockCategory()));

		final RequestBody requestBody = createValidRequestBodyWithCodes(Arrays.asList(EXIST_CODE, INVALID_CODE));

		given()
				.contentType(JSON)
				.body(requestBody)
				.when()
				.post(CATALOG_URL + STORE + CATEGORY_EVENTS)
				.then()
				.statusCode(HttpStatus.PARTIAL_CONTENT.value());
	}

	@Test
	public void shouldReturnStatus400WhenRequestBodyContainsEmptyString() {
		final String malformedRequest = "";

		given()
				.contentType(JSON)
				.body(malformedRequest)
				.when()
				.post(CATALOG_URL + STORE + CATEGORY_EVENTS)
				.then()
				.statusCode(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	public void shouldReturnStatus400WhenRequestBodyContainsWithEmptyJson() {
		final String malformedRequest = "{}";

		given()
				.contentType(JSON)
				.body(malformedRequest)
				.when()
				.post(CATALOG_URL + STORE + CATEGORY_EVENTS)
				.then()
				.statusCode(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	public void shouldReturnStatus400WhenRequestBodyWrong() {
		given()
				.contentType(JSON)
				.body("wrong")
				.when()
				.post(CATALOG_URL + STORE + CATEGORY_EVENTS)
				.then()
				.statusCode(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	public void shouldReturnStatus400WhenRequestBodyContainsNullFields() {
		final RequestBody malformedRequest = new RequestBody();

		given()
				.contentType(JSON)
				.body(malformedRequest)
				.when()
				.post(CATALOG_URL + STORE + CATEGORY_EVENTS)
				.then()
				.statusCode(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	public void shouldReturnStatus400WhenRequestBodyContainsInvalidEventTypeClass() {
		final RequestBody malformedRequest = createValidRequestBodyWithCodes(Collections.singletonList(EXIST_CODE));
		malformedRequest.getEventType().setEventClass(INVALID_CODE);

		given()
				.contentType(JSON)
				.body(malformedRequest)
				.when()
				.post(CATALOG_URL + STORE + CATEGORY_EVENTS)
				.then()
				.statusCode(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	public void shouldReturnStatus400WhenRequestBodyContainsInvalidEventTypeName() {
		final RequestBody malformedRequest = createValidRequestBodyWithCodes(Collections.singletonList(EXIST_CODE));
		malformedRequest.getEventType().setName(INVALID_CODE);

		given()
				.contentType(JSON)
				.body(malformedRequest)
				.when()
				.post(CATALOG_URL + STORE + CATEGORY_EVENTS)
				.then()
				.statusCode(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	public void shouldReturnStatus400WhenRequestBodyContainsInvalidGuid() {
		final RequestBody malformedRequest = createValidRequestBodyWithCodes(Collections.singletonList(EXIST_CODE));
		malformedRequest.setGuid(INVALID_CODE);

		given()
				.contentType(JSON)
				.body(malformedRequest)
				.when()
				.post(CATALOG_URL + STORE + CATEGORY_EVENTS)
				.then()
				.statusCode(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	public void shouldReturnStatus400WhenRequestBodyContainsInvalidDataType() {
		final RequestBody malformedRequest = createValidRequestBodyWithCodes(Collections.singletonList(EXIST_CODE));
		malformedRequest.getData().put("type", INVALID_CODE);

		given()
				.contentType(JSON)
				.body(malformedRequest)
				.when()
				.post(CATALOG_URL + STORE + CATEGORY_EVENTS)
				.then()
				.statusCode(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	public void shouldReturnStatus400WhenRequestBodyContainsInvalidDataStore() {
		final RequestBody malformedRequest = createValidRequestBodyWithCodes(Collections.singletonList(EXIST_CODE));
		malformedRequest.getData().put("store", INVALID_CODE);

		given()
				.contentType(JSON)
				.body(malformedRequest)
				.when()
				.post(CATALOG_URL + STORE + CATEGORY_EVENTS)
				.then()
				.statusCode(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	public void shouldCallValidateRequestBody() {
		given()
				.contentType(JSON)
				.body(new RequestBody())
				.when()
				.post(CATALOG_URL + STORE + CATEGORY_EVENTS);

		verify(requestBodyValidator).validate(any(RequestBody.class));
	}

	@Test
	public void shouldNotCallgetLatestCategoriesWithCodesWhenRequestBodyIsMalformed() {
		final RequestBody requestBody = new RequestBody();

		given()
				.contentType(JSON)
				.body(requestBody)
				.post(CATALOG_URL + STORE + CATEGORY_EVENTS);

		verify(categoryService, never()).getLatestCategoriesWithCodes(anyString(), anyList());
	}

	@Test
	public void shouldCallgetLatestCategoriesWithCodesWhenRequestBodyIsValid() {
		final RequestBody requestBody = createValidRequestBodyWithCodes(Collections.singletonList(EXIST_CODE));

		given()
				.contentType(JSON)
				.body(requestBody)
				.post(CATALOG_URL + STORE + CATEGORY_EVENTS);

		verify(categoryService).getLatestCategoriesWithCodes(anyString(), anyList());
	}

	@Test
	public void shouldReturnNoTEmptyResultsWhenRequestWithModifiedSinceParameter() {
		final FindAllResponse<Category> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockCategory(), mockCategory()));
		when(categoryService.getAllCategories(STORE, "2", null, "2018-01-01T14:47:00.754+00:00", null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, "2")
				.contentType(JSON)
				.param("modifiedSince", "2018-01-01T14:47:00.754+00:00")
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI)
				.then()
				.body(RESULTS_SIZE, equalTo(2));
	}

	@Test
	public void shouldReturn400CodeInCaseInvalidParameters() {
		when(categoryService.getAllCategories(STORE, "2", null, "A2018-01-01T14:47:00.754+00:00", null))
				.thenThrow(new InvalidRequestParameterException());

		given()
				.contentType(JSON)
				.param(LIMIT, "2")
				.param("modifiedSince", "A2018-01-01T14:47:00.754+00:00")
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI)
				.then()
				.statusCode(HttpStatus.BAD_REQUEST.value());
		verify(categoryService).getAllCategories(STORE, "2", null, "A2018-01-01T14:47:00.754+00:00", null);
	}

	@Test
	public void validateResponseBodyIsSuccessShouldBeTrueWhenRequestIsValid() throws URISyntaxException, ProcessingException, IOException {
		when(categoryService.getLatestCategoriesWithCodes(any(), anyList())).thenReturn(Collections.singletonList(mockCategory()));

		final RequestBody requestBody = createValidRequestBodyWithCodes(Collections.singletonList(EXIST_CODE));

		final String responseBody = given()
				.contentType(JSON)
				.body(requestBody)
				.post(CATALOG_URL + STORE + CATEGORY_EVENTS)
				.getBody()
				.asString();

		JsonSchema jsonSchema =
				JsonSchemaFactory.byDefault().getJsonSchema(getClass().getResource("/schema/category.results.schema.json").toURI().toString());

		assertTrue(jsonSchema.validate(JsonLoader.fromString(responseBody)).isSuccess());
	}

	@Test
	public void shouldReturnStatus200WhenStoreCodeInRequestBodyHasDifferentCaseWithStoreCodeInUri() {
		when(categoryService.getLatestCategoriesWithCodes(any(), anyList())).thenReturn(Collections.singletonList(mockCategory()));

		final RequestBody requestBody = createValidRequestBodyWithCodes(Collections.singletonList(EXIST_CODE));
		requestBody.getData().put(STORE, STORE.toLowerCase(Locale.ENGLISH));

		given()
				.contentType(JSON)
				.body(requestBody)
				.when()
				.post(CATALOG_URL + STORE.toUpperCase(Locale.ENGLISH) + CATEGORY_EVENTS)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnStatus200WhenGetChildrenRequestAndChildrenArePresentInCategory() {
		final List<Category> children = Arrays.asList(mockCategory(), mockCategory());
		when(categoryService.getChildren(STORE, EXIST_CODE)).thenReturn(children);

		given()
				.contentType(JSON)
				.get(CATALOG_URL + STORE + CATEGORIES_URI + EXIST_CODE + CATEGORY_CHILDREN)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnStatus200WhenGetChildrenRequestAndChildrenAreNotPresentInCategory() throws IOException {
		final List<Category> children = Collections.emptyList();
		when(categoryService.getChildren(STORE, EXIST_CODE)).thenReturn(children);

		given()
				.contentType(JSON)
				.get(CATALOG_URL + STORE + CATEGORIES_URI + EXIST_CODE + CATEGORY_CHILDREN)
				.then()
				.statusCode(HttpStatus.OK.value());

		final String responseBody = given()
				.contentType(JSON)
				.get(CATALOG_URL + STORE + CATEGORIES_URI + EXIST_CODE + CATEGORY_CHILDREN)
				.getBody()
				.asString();
		assertEquals("{\"results\":[]}", responseBody);
	}

	@Test
	public void validateResponseBodyIsSuccessShouldBeTrueWhenResponseWithChildrenIsValid() throws IOException, URISyntaxException,
			ProcessingException {
		final List<Category> children = Arrays.asList(mockCategory(), mockCategory());
		when(categoryService.getChildren(STORE, EXIST_CODE)).thenReturn(children);

		final String responseBody = given()
				.contentType(JSON)
				.get(CATALOG_URL + STORE + CATEGORIES_URI + EXIST_CODE + CATEGORY_CHILDREN)
				.getBody()
				.asString();

		JsonSchema jsonSchema =
				JsonSchemaFactory.byDefault().getJsonSchema(getClass().getResource("/schema/category.children.schema.json").toURI().toString());

		assertTrue(jsonSchema.validate(JsonLoader.fromString(responseBody)).isSuccess());
	}

	@Test
	public void testThatShouldNotReturnDisableDateTime() {
		when(categoryService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockCategory()));

		given()
				.when()
				.get(CATALOG_URL + STORE + CATEGORIES_URI + EXIST_CODE)
				.then()
				.body("disableDateTime", equalTo(null));
	}

	@Override
	protected CamelContext createCamelContext() {
		objectMapper.registerModule(new JavaTimeModule());
		final DataFormat jacksonDataFormat = new JacksonDataFormat(objectMapper, Category.class);

		final SimpleRegistry simpleRegistry = new SimpleRegistry();
		simpleRegistry.put("json-jackson", jacksonDataFormat);

		final DefaultCamelContext camelContext = new DefaultCamelContext();
		camelContext.setRegistry(simpleRegistry);

		return camelContext;
	}

	@Override
	protected RoutesBuilder[] createRouteBuilders() {
		return new RoutesBuilder[]{catalogWebServiceRestEndpointsRouteBuilder, categoryWebServiceRouteBuilder};
	}

	private Category mockCategory() {
		return mockCategoryWithChildren(Collections.emptyList());
	}

	private Category mockCategoryWithChildren(final List<String> children) {
		final ProjectionProperties projectionProperties = new ProjectionProperties(EXIST_CODE, STORE, ZonedDateTime.now(), false);
		final List<Property> categorySpecificProperties = Collections.emptyList();
		final CategoryProperties categoryProperties = new CategoryProperties(projectionProperties, categorySpecificProperties);

		final Object extensions = new Object();
		final List<CategoryTranslation> categoryTranslations = Collections.emptyList();

		return new Category(categoryProperties, extensions, categoryTranslations, children, new AvailabilityRules(null, null),
				Collections.emptyList(), null);
	}

	private RequestBody createValidRequestBodyWithCodes(final List<String> codes) {
		final RequestBody requestBody = new RequestBody();

		final EventType eventType = new EventType();
		eventType.setEventClass("CatalogEventType");
		eventType.setName("CATEGORIES_UPDATED");

		requestBody.setEventType(eventType);
		requestBody.setGuid("AGGREGATE");

		final HashMap<String, Object> data = new HashMap<>();
		data.put("type", "category");
		data.put("store", STORE);
		data.put("modifiedDateTime", "2018-01-01T14:47:00+00:00");
		data.put("codes", codes);

		requestBody.setData(data);

		return requestBody;
	}

	private FindAllResponse<Category> mockFindAllResponse(final int limit, final String startAfter, final boolean hasMoreResults,
														  final ZonedDateTime currentDateTime, final List<Category> results) {
		final PaginationResponse paginationResponse = new PaginationResponseImpl(limit, startAfter, hasMoreResults);
		return new FindAllResponseImpl<>(paginationResponse, currentDateTime, results);
	}
}
