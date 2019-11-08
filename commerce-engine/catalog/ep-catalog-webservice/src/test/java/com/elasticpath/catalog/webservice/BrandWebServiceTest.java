/*
 * Copyright (c) Elastic Path Software Inc., 2018
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

import com.elasticpath.catalog.entity.brand.Brand;
import com.elasticpath.catalog.reader.FindAllResponse;
import com.elasticpath.catalog.reader.PaginationResponse;
import com.elasticpath.catalog.reader.impl.FindAllResponseImpl;
import com.elasticpath.catalog.reader.impl.PaginationResponseImpl;
import com.elasticpath.catalog.webservice.exception.InvalidRequestParameterException;
import com.elasticpath.catalog.webservice.request.entity.EventType;
import com.elasticpath.catalog.webservice.request.entity.RequestBody;
import com.elasticpath.catalog.webservice.request.validator.RequestBodyValidator;
import com.elasticpath.catalog.webservice.services.BrandService;

/**
 * Tests BrandWebServiceRouteBuilder.
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports"})
public class BrandWebServiceTest extends CamelTestSupport {

	private static final String STORE = "store";
	private static final String EXIST_CODE = "existCode";
	private static final String NOT_EXIST_CODE = "notExistCode";
	private static final String INVALID_CODE = "invalidCode";
	private static final String WEB_XML_PATH = "src/test/webapp";
	private static final int TOMCAT_PORT = 8092;
	private static final String CATALOG_URL = "http://localhost:" + TOMCAT_PORT + "/api/syndication/v1/catalog/";
	private static final String BRAND_URI = "/brands/";
	private static final String BRAND_EVENTS = "/brandevents/";
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
	private static final List<Brand> EMPTY_RESULTS = new ArrayList<>();
	private static final String LAST_BRAND_IN_RESPONSE = "lastBrandInResponse";
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
	private BrandService brandService;

	@Mock
	private RequestBodyValidator requestBodyValidator;

	@InjectMocks
	private CatalogWebServiceRestEndpointsRouteBuilder catalogWebServiceRestEndpointsRouteBuilder;

	@InjectMocks
	private BrandWebServiceRouteBuilder brandWebServiceRouteBuilder;

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
		when(requestBodyValidator.validate(any(RequestBody.class))).thenReturn(true);
	}

	@Test
	public void testThatShouldReturnStatus200WhenBrandCodeExist() {
		when(brandService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockBrand()));

		given()
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI + EXIST_CODE)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void testThatShouldReturnContentTypeHeaderEqualsJsonWhenBrandCodeExist() {
		when(brandService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockBrand()));

		given()
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI + EXIST_CODE)
				.then()
				.contentType(JSON);
	}

	@Test
	public void testThatShouldReturnETagHeaderWhenBrandCodeExist() {
		when(brandService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockBrand()));

		given()
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI + EXIST_CODE)
				.then()
				.header(E_TAG_HEADER, is(not(isEmptyString())));
	}


	@Test
	public void testThatShouldReturnContentLengthHeaderWhenBrandCodeExist() {
		when(brandService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockBrand()));

		given()
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI + EXIST_CODE)
				.then()
				.header(CONTENT_LENGTH_HEADER, is(not(isEmptyString())));
	}

	@Test
	public void testThatShouldReturnStatus404WhenBrandCodeNotExist() {
		given()
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI + NOT_EXIST_CODE)
				.then()
				.statusCode(HttpStatus.NOT_FOUND.value());
	}

	@Test
	public void testThatShouldReturnStatus304WhenGetBrandAgainWithETagFromPreviousResult() {
		when(brandService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockBrand()));
		final String eTag = get(CATALOG_URL + STORE + BRAND_URI + EXIST_CODE).header(E_TAG_HEADER);

		given()
				.header(IF_NONE_MATCH_HEADER, eTag)
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI + EXIST_CODE)
				.then()
				.statusCode(HttpStatus.NOT_MODIFIED.value());
	}

	@Test
	public void testThatShouldReturnStatus200WhenGetBrandAgainWithETagFromPreviousResultAndBrandAlreadyUpdated() {
		when(brandService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockBrand()));
		final String eTag = get(CATALOG_URL + STORE + BRAND_URI + EXIST_CODE).header(E_TAG_HEADER);
		when(brandService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockBrand()));

		given()
				.header(IF_NONE_MATCH_HEADER, eTag)
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI + EXIST_CODE)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void testThatShouldReturnModifiedBrandWhenGetBrandAgainWithETagFromPreviousResultAndBrandAlreadyUpdated() throws JsonProcessingException {
		when(brandService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockBrand()));
		final String eTag = get(CATALOG_URL + STORE + BRAND_URI + EXIST_CODE).header(E_TAG_HEADER);

		final Brand modifiedBrand = mockBrand();
		when(brandService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(modifiedBrand));

		given()
				.header(IF_NONE_MATCH_HEADER, eTag)
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI + EXIST_CODE)
				.then()
				.body(is(objectMapper.writeValueAsString(modifiedBrand)));
	}

	@Test
	public void testShouldReturnStatus200WhenRequestWithoutParametersAndLessThan10BrandsInStore() {
		final FindAllResponse<Brand> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockBrand(), mockBrand()));

		when(brandService.getAllBrands(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnPaginationNextLimit10WhenRequestWithoutParametersAndLessThan10BrandsInStore() {
		final FindAllResponse<Brand> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockBrand(), mockBrand()));

		when(brandService.getAllBrands(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI)
				.then()
				.body(PAGINATION_NEXT_LIMIT, is(DEFAULT_LIMIT));
	}

	@Test
	public void shouldReturnEmptyPaginationNextStartAfterWhenRequestWithoutParametersAndLessThan10BrandsInStore() {
		final FindAllResponse<Brand> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockBrand(), mockBrand()));

		when(brandService.getAllBrands(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI)
				.then()
				.body(PAGINATION_NEXT_START_AFTER, isEmptyString());
	}

	@Test
	public void shouldReturnPaginationNextHasMoreResultsFalseWhenRequestWithoutParametersAndLessThan10BrandsInStore() {
		final FindAllResponse<Brand> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockBrand(), mockBrand()));

		when(brandService.getAllBrands(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI)
				.then()
				.body(PAGINATION_NEXT_HAS_MORE_RESULTS, is(false));
	}

	@Test
	public void shouldReturnPaginationNextCurrentDateTimeWhenRequestWithoutParametersAndLessThan10BrandsInStore() {
		final FindAllResponse<Brand> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockBrand(), mockBrand()));

		when(brandService.getAllBrands(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI)
				.then()
				.body(CURRENT_DATE_TIME, not(isEmptyString()));
	}

	@Test
	public void shouldReturnStatus200WhenRequestWithLimit2AndExistMoreBrandsInStore() {
		final FindAllResponse<Brand> findAllResponse = mockFindAllResponse(2, LAST_BRAND_IN_RESPONSE, HAS_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockBrand(), mockBrand()));

		when(brandService.getAllBrands(STORE, "2", null, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnPaginationNextLimit2WhenRequestWithLimit2AndExistMoreBrandsInStore() {
		final FindAllResponse<Brand> findAllResponse = mockFindAllResponse(2, LAST_BRAND_IN_RESPONSE, HAS_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockBrand(), mockBrand()));

		when(brandService.getAllBrands(STORE, "2", null, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI)
				.then()
				.body(PAGINATION_NEXT_LIMIT, is(2));
	}

	@Test
	public void shouldReturnPaginationNextStartAfterBWhenRequestWithLimit2AndExistMoreBrandsInStore() {
		final FindAllResponse<Brand> findAllResponse = mockFindAllResponse(2, LAST_BRAND_IN_RESPONSE, HAS_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockBrand(), mockBrand()));

		when(brandService.getAllBrands(STORE, "2", null, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI)
				.then()
				.body(PAGINATION_NEXT_START_AFTER, is(LAST_BRAND_IN_RESPONSE));
	}

	@Test
	public void shouldReturnPaginationNextHasMoreResultsTrueWhenRequestWithLimit2AndExistMoreBrandsInStore() {
		final FindAllResponse<Brand> findAllResponse = mockFindAllResponse(2, LAST_BRAND_IN_RESPONSE, HAS_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockBrand(), mockBrand()));

		when(brandService.getAllBrands(STORE, "2", null, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI)
				.then()
				.body(PAGINATION_NEXT_HAS_MORE_RESULTS, is(true));
	}

	@Test
	public void shouldReturnPaginationNextCurrentDateTimeWhenRequestWithLimit2AndExistMoreBrandsInStore() {
		final FindAllResponse<Brand> findAllResponse = mockFindAllResponse(2, LAST_BRAND_IN_RESPONSE, HAS_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockBrand(), mockBrand()));

		when(brandService.getAllBrands(STORE, "2", null, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI)
				.then()
				.body(CURRENT_DATE_TIME, not(isEmptyString()));
	}

	@Test
	public void shouldReturnStatus200WhenRequestWithLimit2AndStartAfterExistCodeAndExistMoreBrandsInStore() {
		final FindAllResponse<Brand> findAllResponse = mockFindAllResponse(2, LAST_BRAND_IN_RESPONSE, HAS_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockBrand(), mockBrand()));

		when(brandService.getAllBrands(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnPaginationNextLimit2WhenRequestWithLimit2AndStartAfterExistCodeAndExistMoreBrandsInStore() {
		final FindAllResponse<Brand> findAllResponse = mockFindAllResponse(2, LAST_BRAND_IN_RESPONSE, HAS_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockBrand(), mockBrand()));

		when(brandService.getAllBrands(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI)
				.then()
				.body(PAGINATION_NEXT_LIMIT, is(2));
	}

	@Test
	public void shouldReturnPaginationNextStartAfterDWhenRequestWithLimit2AndStartAfterExistCodeAndExistMoreBrandsInStore() {
		final FindAllResponse<Brand> findAllResponse = mockFindAllResponse(2, LAST_BRAND_IN_RESPONSE, HAS_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockBrand(), mockBrand()));

		when(brandService.getAllBrands(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI)
				.then()
				.body(PAGINATION_NEXT_START_AFTER, is(LAST_BRAND_IN_RESPONSE));
	}

	@Test
	public void shouldReturnPaginationNextHasMoreResultsTrueWhenRequestWithLimit2AndStartAfterExistCodeAndExistMoreBrandsInStore() {
		final FindAllResponse<Brand> findAllResponse = mockFindAllResponse(2, LAST_BRAND_IN_RESPONSE, HAS_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockBrand(), mockBrand()));

		when(brandService.getAllBrands(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI)
				.then()
				.body(PAGINATION_NEXT_HAS_MORE_RESULTS, is(true));
	}

	@Test
	public void shouldNotReturnPaginationNextCurrentDateTimeWhenRequestWithLimit2AndStartAfterExistCodeAndExistMoreBrandsInStore() {
		final FindAllResponse<Brand> findAllResponse = mockFindAllResponse(2, LAST_BRAND_IN_RESPONSE, HAS_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockBrand(), mockBrand()));

		when(brandService.getAllBrands(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI)
				.then()
				.body(CURRENT_DATE_TIME, not(expect()));
	}

	@Test
	public void shouldReturnStatus200WhenRequestWithLimit2AndStartAfterExistCodeAndNoMoreBrandsInStore() {
		final FindAllResponse<Brand> findAllResponse = mockFindAllResponse(2, EMPTY_START_AFTER, NONE_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockBrand(), mockBrand()));

		when(brandService.getAllBrands(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnPaginationNextLimit2WhenRequestWithLimit2AndStartAfterExistCodeAndNoMoreBrandsInStore() {
		final FindAllResponse<Brand> findAllResponse = mockFindAllResponse(2, EMPTY_START_AFTER, NONE_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockBrand(), mockBrand()));

		when(brandService.getAllBrands(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI)
				.then()
				.body(PAGINATION_NEXT_LIMIT, is(2));
	}

	@Test
	public void shouldReturnEmptyPaginationNextStartAfterWhenRequestWithLimit2AndStartAfterExistCodeAndNoMoreBrandsInStore() {
		final FindAllResponse<Brand> findAllResponse = mockFindAllResponse(2, EMPTY_START_AFTER, NONE_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockBrand(), mockBrand()));

		when(brandService.getAllBrands(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI)
				.then()
				.body(PAGINATION_NEXT_START_AFTER, isEmptyString());
	}

	@Test
	public void shouldReturnPaginationNextHasMoreResultsFalseWhenRequestWithLimit2AndStartAfterExistCodeAndNoMoreBrandsInStore() {
		final FindAllResponse<Brand> findAllResponse = mockFindAllResponse(2, EMPTY_START_AFTER, NONE_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockBrand(), mockBrand()));

		when(brandService.getAllBrands(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI)
				.then()
				.body(PAGINATION_NEXT_HAS_MORE_RESULTS, is(false));
	}

	@Test
	public void shouldNotReturnPaginationNextCurrentDateTimeWhenRequestWithLimit2AndStartAfterExistCodeAndNoMoreBrandsInStore() {
		final FindAllResponse<Brand> findAllResponse = mockFindAllResponse(2, EMPTY_START_AFTER, NONE_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockBrand(), mockBrand()));

		when(brandService.getAllBrands(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI)
				.then()
				.body(CURRENT_DATE_TIME, not(expect()));
	}

	@Test
	public void shouldReturnStatus200WhenRequestWithoutParametersAndNoBrandsInStore() {
		final FindAllResponse<Brand> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				EMPTY_RESULTS);

		when(brandService.getAllBrands(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnPaginationNextLimit10WhenRequestWithoutParametersAndNoBrandsInStore() {
		final FindAllResponse<Brand> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				EMPTY_RESULTS);

		when(brandService.getAllBrands(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI)
				.then()
				.body(PAGINATION_NEXT_LIMIT, is(DEFAULT_LIMIT));
	}

	@Test
	public void shouldReturnEmptyPaginationNextStartAfterWhenRequestWithoutParametersAndNoBrandsInStore() {
		final FindAllResponse<Brand> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				EMPTY_RESULTS);

		when(brandService.getAllBrands(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI)
				.then()
				.body(PAGINATION_NEXT_START_AFTER, isEmptyString());
	}

	@Test
	public void shouldReturnPaginationNextHasMoreResultsFalseWhenRequestWithoutParametersAndNoBrandsInStore() {
		final FindAllResponse<Brand> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				EMPTY_RESULTS);

		when(brandService.getAllBrands(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI)
				.then()
				.body(PAGINATION_NEXT_HAS_MORE_RESULTS, is(false));
	}

	@Test
	public void shouldReturnPaginationNextCurrentDateTimeWhenRequestWithoutParametersAndNoBrandsInStore() {
		final FindAllResponse<Brand> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				EMPTY_RESULTS);

		when(brandService.getAllBrands(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI)
				.then()
				.body(CURRENT_DATE_TIME, not(isEmptyString()));
	}

	@Test
	public void shouldReturnEmptyResultsWhenRequestWithoutParametersAndNoBrandsInStore() {
		final FindAllResponse<Brand> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				EMPTY_RESULTS);

		when(brandService.getAllBrands(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI)
				.then()
				.body(RESULTS, empty());
	}

	@Test
	public void testShouldReturnStatus200WhenRequestBodyContainingSingleBrandCode() {
		when(brandService.getLatestBrandsWithCodes(any(), anyList())).thenReturn(Collections.singletonList(mockBrand()));

		final RequestBody requestBody = createValidRequestBodyWithCodes(Collections.singletonList(EXIST_CODE));

		given()
				.contentType(JSON)
				.body(requestBody)
				.when()
				.post(CATALOG_URL + STORE + BRAND_EVENTS)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnStatus200WhenRequestBodyContainingMultipleBrandCodes() {
		when(brandService.getLatestBrandsWithCodes(any(), anyList())).thenReturn(Arrays.asList(mockBrand(), mockBrand()));

		final RequestBody requestBody = createValidRequestBodyWithCodes(Arrays.asList(EXIST_CODE, EXIST_CODE));

		given()
				.contentType(JSON)
				.body(requestBody)
				.when()
				.post(CATALOG_URL + STORE + BRAND_EVENTS)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnStatus206WhenRequestBodyContainingMultipleBrandCodesOneOfWhichInvalid() {
		when(brandService.getLatestBrandsWithCodes(any(), anyList())).thenReturn(Collections.singletonList(mockBrand()));

		final RequestBody requestBody = createValidRequestBodyWithCodes(Arrays.asList(EXIST_CODE, INVALID_CODE));

		given()
				.contentType(JSON)
				.body(requestBody)
				.when()
				.post(CATALOG_URL + STORE + BRAND_EVENTS)
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
				.post(CATALOG_URL + STORE + BRAND_EVENTS)
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
				.post(CATALOG_URL + STORE + BRAND_EVENTS)
				.then()
				.statusCode(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	public void shouldReturnStatus400WhenRequestBodyWrong() {
		given()
				.contentType(JSON)
				.body("wrong")
				.when()
				.post(CATALOG_URL + STORE + BRAND_EVENTS)
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
				.post(CATALOG_URL + STORE + BRAND_EVENTS)
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
				.post(CATALOG_URL + STORE + BRAND_EVENTS)
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
				.post(CATALOG_URL + STORE + BRAND_EVENTS)
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
				.post(CATALOG_URL + STORE + BRAND_EVENTS)
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
				.post(CATALOG_URL + STORE + BRAND_EVENTS)
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
				.post(CATALOG_URL + STORE + BRAND_EVENTS)
				.then()
				.statusCode(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	public void shouldCallValidateRequestBody() {
		given()
				.contentType(JSON)
				.body(new RequestBody())
				.when()
				.post(CATALOG_URL + STORE + BRAND_EVENTS);

		verify(requestBodyValidator).validate(any(RequestBody.class));
	}

	@Test
	public void shouldNotCallGetLatestBrandsWithCodesWhenRequestBodyIsMalformed() {
		final RequestBody requestBody = new RequestBody();

		given()
				.contentType(JSON)
				.body(requestBody)
				.post(CATALOG_URL + STORE + BRAND_EVENTS);

		verify(brandService, never()).getLatestBrandsWithCodes(anyString(), anyList());
	}

	@Test
	public void shouldCallGetLatestBrandsWithCodesWhenRequestBodyIsValid() {
		final RequestBody requestBody = createValidRequestBodyWithCodes(Collections.singletonList(EXIST_CODE));

		given()
				.contentType(JSON)
				.body(requestBody)
				.post(CATALOG_URL + STORE + BRAND_EVENTS);

		verify(brandService).getLatestBrandsWithCodes(anyString(), anyList());
	}

	@Test
	public void shouldReturnNoTEmptyResultsWhenRequestWithModifiedSinceParameter() {
		final FindAllResponse<Brand> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockBrand(), mockBrand()));
		when(brandService.getAllBrands(STORE, "2", null, "2018-01-01T14:47:00.754+00:00", null)).thenReturn(findAllResponse);

		given().param(LIMIT, "2")
				.param("modifiedSince", "2018-01-01T14:47:00.754+00:00")
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI)
				.then()
				.body(RESULTS_SIZE, equalTo(2));
	}

	@Test
	public void shouldReturn400CodeInCaseInvalidParameters() {
		when(brandService.getAllBrands(STORE, "2", null, "A2018-01-01T14:47:00.754+00:00", null)).thenThrow(new InvalidRequestParameterException());

		given().param(LIMIT, "2")
				.param("modifiedSince", "A2018-01-01T14:47:00.754+00:00")
				.when()
				.get(CATALOG_URL + STORE + BRAND_URI)
				.then()
				.statusCode(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	public void validateResponseBodyIsSuccessShouldBeTrueWhenRequestIsValid() throws URISyntaxException, ProcessingException, IOException {
		when(brandService.getLatestBrandsWithCodes(any(), anyList())).thenReturn(Collections.singletonList(mockBrand()));

		final RequestBody requestBody = createValidRequestBodyWithCodes(Collections.singletonList(EXIST_CODE));

		String responseBody = given()
				.contentType(JSON)
				.body(requestBody)
				.post(CATALOG_URL + STORE + BRAND_EVENTS)
				.getBody()
				.asString();

		JsonSchema jsonSchema =
				JsonSchemaFactory.byDefault().getJsonSchema(getClass().getResource("/schema/brand.results.schema.json").toURI().toString());

		assertTrue(jsonSchema.validate(JsonLoader.fromString(responseBody)).isSuccess());
	}

	@Test
	public void shouldReturnStatus200WhenStoreCodeInRequestBodyHasDifferentCaseWithStoreCodeInUri() {
		when(brandService.getLatestBrandsWithCodes(any(), anyList())).thenReturn(Collections.singletonList(mockBrand()));

		final RequestBody requestBody = createValidRequestBodyWithCodes(Collections.singletonList(EXIST_CODE));
		requestBody.getData().put(STORE, STORE.toLowerCase(Locale.ENGLISH));

		given()
				.contentType(JSON)
				.body(requestBody)
				.when()
				.post(CATALOG_URL + STORE.toUpperCase(Locale.ENGLISH) + BRAND_EVENTS)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Override
	protected CamelContext createCamelContext() {
		objectMapper.registerModule(new JavaTimeModule());
		final DataFormat jacksonDataFormat = new JacksonDataFormat(objectMapper, Brand.class);

		final SimpleRegistry simpleRegistry = new SimpleRegistry();
		simpleRegistry.put("json-jackson", jacksonDataFormat);

		final DefaultCamelContext camelContext = new DefaultCamelContext();
		camelContext.setRegistry(simpleRegistry);

		return camelContext;
	}

	@Override
	protected RoutesBuilder[] createRouteBuilders() {
		return new RoutesBuilder[]{catalogWebServiceRestEndpointsRouteBuilder, brandWebServiceRouteBuilder};
	}

	private Brand mockBrand() {
		return new Brand(EXIST_CODE, STORE, new ArrayList<>(), ZonedDateTime.now(), false);
	}

	private RequestBody createValidRequestBodyWithCodes(final List<String> codes) {
		final RequestBody requestBody = new RequestBody();

		final EventType eventType = new EventType();
		eventType.setEventClass("CatalogEventType");
		eventType.setName("BRANDS_UPDATED");

		requestBody.setEventType(eventType);
		requestBody.setGuid("AGGREGATE");

		final HashMap<String, Object> data = new HashMap<>();
		data.put("type", "brand");
		data.put("store", STORE);
		data.put("modifiedDateTime", "2018-01-01T14:47:00+00:00");
		data.put("codes", codes);

		requestBody.setData(data);

		return requestBody;
	}

	private FindAllResponse<Brand> mockFindAllResponse(final int limit, final String startAfter, final boolean hasMoreResults,
														final ZonedDateTime currentDateTime, final List<Brand> results) {
		final PaginationResponse paginationResponse = new PaginationResponseImpl(limit, startAfter, hasMoreResults);
		return new FindAllResponseImpl<>(paginationResponse, currentDateTime, results);
	}

}
