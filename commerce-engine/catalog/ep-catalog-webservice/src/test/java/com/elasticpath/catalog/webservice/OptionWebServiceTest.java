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

import com.elasticpath.catalog.entity.option.Option;
import com.elasticpath.catalog.reader.FindAllResponse;
import com.elasticpath.catalog.reader.PaginationResponse;
import com.elasticpath.catalog.reader.impl.FindAllResponseImpl;
import com.elasticpath.catalog.reader.impl.PaginationResponseImpl;
import com.elasticpath.catalog.webservice.exception.InvalidRequestParameterException;
import com.elasticpath.catalog.webservice.request.entity.EventType;
import com.elasticpath.catalog.webservice.request.entity.RequestBody;
import com.elasticpath.catalog.webservice.request.validator.RequestBodyValidator;
import com.elasticpath.catalog.webservice.services.OptionService;

/**
 * Tests OptionWebServiceRouteBuilder.
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports"})
public class OptionWebServiceTest extends CamelTestSupport {

	private static final String STORE = "store";
	private static final String EXIST_CODE = "existCode";
	private static final String NOT_EXIST_CODE = "notExistCode";
	private static final String INVALID_CODE = "invalidCode";
	private static final String WEB_XML_PATH = "src/test/webapp";
	private static final int TOMCAT_PORT = 8091;
	private static final String CATALOG_URL = "http://localhost:" + TOMCAT_PORT + "/api/syndication/v1/catalog/";
	private static final String OPTIONS_URI = "/options/";
	private static final String OPTION_EVENTS = "/optionevents/";
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
	private static final List<Option> EMPTY_RESULTS = new ArrayList<>();
	private static final String LAST_OPTION_IN_RESPONSE = "lastOptionInResponse";
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
	private OptionService optionService;

	@Mock
	private RequestBodyValidator requestBodyValidator;

	@InjectMocks
	private CatalogWebServiceRestEndpointsRouteBuilder catalogWebServiceRestEndpointsRouteBuilder;

	@InjectMocks
	private OptionWebServiceRouteBuilder optionWebServiceRouteBuilder;

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
	public void testThatShouldReturnStatus200WhenOptionCodeExist() {
		when(optionService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockOption()));

		given()
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI + EXIST_CODE)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void testThatShouldReturnContentTypeHeaderEqualsJsonWhenOptionCodeExist() {
		when(optionService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockOption()));

		given()
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI + EXIST_CODE)
				.then()
				.contentType(JSON);
	}

	@Test
	public void testThatShouldReturnETagHeaderWhenOptionCodeExist() {
		when(optionService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockOption()));

		given()
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI + EXIST_CODE)
				.then()
				.header(E_TAG_HEADER, is(not(isEmptyString())));
	}


	@Test
	public void testThatShouldReturnContentLengthHeaderWhenOptionCodeExist() {
		when(optionService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockOption()));

		given()
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI + EXIST_CODE)
				.then()
				.header(CONTENT_LENGTH_HEADER, is(not(isEmptyString())));
	}

	@Test
	public void testThatShouldReturnStatus404WhenOptionCodeNotExist() {
		given()
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI + NOT_EXIST_CODE)
				.then()
				.statusCode(HttpStatus.NOT_FOUND.value());
	}

	@Test
	public void testThatShouldReturnStatus304WhenGetOptionAgainWithETagFromPreviousResult() {
		when(optionService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockOption()));
		final String eTag = get(CATALOG_URL + STORE + OPTIONS_URI + EXIST_CODE).header(E_TAG_HEADER);

		given()
				.header(IF_NONE_MATCH_HEADER, eTag)
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI + EXIST_CODE)
				.then()
				.statusCode(HttpStatus.NOT_MODIFIED.value());
	}

	@Test
	public void testThatShouldReturnStatus200WhenGetOptionAgainWithETagFromPreviousResultAndOptionAlreadyUpdated() {
		when(optionService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockOption()));
		final String eTag = get(CATALOG_URL + STORE + OPTIONS_URI + EXIST_CODE).header(E_TAG_HEADER);
		when(optionService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockOption()));

		given()
				.header(IF_NONE_MATCH_HEADER, eTag)
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI + EXIST_CODE)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void testThatShouldReturnModifiedOptionWhenGetOptionAgainWithETagFromPreviousResultAndOptionAlreadyUpdated()
			throws JsonProcessingException {
		when(optionService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockOption()));
		final String eTag = get(CATALOG_URL + STORE + OPTIONS_URI + EXIST_CODE).header(E_TAG_HEADER);

		final Option modifiedOption = mockOption();
		when(optionService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(modifiedOption));

		given()
				.header(IF_NONE_MATCH_HEADER, eTag)
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI + EXIST_CODE)
				.then()
				.body(is(objectMapper.writeValueAsString(modifiedOption)));
	}

	@Test
	public void testShouldReturnStatus200WhenRequestWithoutParametersAndLessThan10OptionsInStore() {
		final FindAllResponse<Option> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockOption(), mockOption()));

		when(optionService.getAllOptions(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnPaginationNextLimit10WhenRequestWithoutParametersAndLessThan10OptionsInStore() {
		final FindAllResponse<Option> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockOption(), mockOption()));

		when(optionService.getAllOptions(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI)
				.then()
				.body(PAGINATION_NEXT_LIMIT, is(DEFAULT_LIMIT));
	}

	@Test
	public void shouldReturnEmptyPaginationNextStartAfterWhenRequestWithoutParametersAndLessThan10OptionsInStore() {
		final FindAllResponse<Option> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockOption(), mockOption()));

		when(optionService.getAllOptions(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI)
				.then()
				.body(PAGINATION_NEXT_START_AFTER, isEmptyString());
	}

	@Test
	public void shouldReturnPaginationNextHasMoreResultsFalseWhenRequestWithoutParametersAndLessThan10OptionsInStore() {
		final FindAllResponse<Option> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockOption(), mockOption()));

		when(optionService.getAllOptions(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI)
				.then()
				.body(PAGINATION_NEXT_HAS_MORE_RESULTS, is(false));
	}

	@Test
	public void shouldReturnPaginationNextCurrentDateTimeWhenRequestWithoutParametersAndLessThan10OptionsInStore() {
		final FindAllResponse<Option> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockOption(), mockOption()));

		when(optionService.getAllOptions(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI)
				.then()
				.body(CURRENT_DATE_TIME, not(isEmptyString()));
	}

	@Test
	public void shouldReturnStatus200WhenRequestWithLimit2AndExistMoreOptionsInStore() {
		final FindAllResponse<Option> findAllResponse = mockFindAllResponse(2, LAST_OPTION_IN_RESPONSE, HAS_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockOption(), mockOption()));

		when(optionService.getAllOptions(STORE, "2", null, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnPaginationNextLimit2WhenRequestWithLimit2AndExistMoreOptionsInStore() {
		final FindAllResponse<Option> findAllResponse = mockFindAllResponse(2, LAST_OPTION_IN_RESPONSE, HAS_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockOption(), mockOption()));

		when(optionService.getAllOptions(STORE, "2", null, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI)
				.then()
				.body(PAGINATION_NEXT_LIMIT, is(2));
	}

	@Test
	public void shouldReturnPaginationNextStartAfterBWhenRequestWithLimit2AndExistMoreOptionsInStore() {
		final FindAllResponse<Option> findAllResponse = mockFindAllResponse(2, LAST_OPTION_IN_RESPONSE, HAS_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockOption(), mockOption()));

		when(optionService.getAllOptions(STORE, "2", null, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI)
				.then()
				.body(PAGINATION_NEXT_START_AFTER, is(LAST_OPTION_IN_RESPONSE));
	}

	@Test
	public void shouldReturnPaginationNextHasMoreResultsTrueWhenRequestWithLimit2AndExistMoreOptionsInStore() {
		final FindAllResponse<Option> findAllResponse = mockFindAllResponse(2, LAST_OPTION_IN_RESPONSE, HAS_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockOption(), mockOption()));

		when(optionService.getAllOptions(STORE, "2", null, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI)
				.then()
				.body(PAGINATION_NEXT_HAS_MORE_RESULTS, is(true));
	}

	@Test
	public void shouldReturnPaginationNextCurrentDateTimeWhenRequestWithLimit2AndExistMoreOptionsInStore() {
		final FindAllResponse<Option> findAllResponse = mockFindAllResponse(2, LAST_OPTION_IN_RESPONSE, HAS_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockOption(), mockOption()));

		when(optionService.getAllOptions(STORE, "2", null, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI)
				.then()
				.body(CURRENT_DATE_TIME, not(isEmptyString()));
	}

	@Test
	public void shouldReturnStatus200WhenRequestWithLimit2AndStartAfterExistCodeAndExistMoreOptionsInStore() {
		final FindAllResponse<Option> findAllResponse = mockFindAllResponse(2, LAST_OPTION_IN_RESPONSE, HAS_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockOption(), mockOption()));

		when(optionService.getAllOptions(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnPaginationNextLimit2WhenRequestWithLimit2AndStartAfterExistCodeAndExistMoreOptionsInStore() {
		final FindAllResponse<Option> findAllResponse = mockFindAllResponse(2, LAST_OPTION_IN_RESPONSE, HAS_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockOption(), mockOption()));

		when(optionService.getAllOptions(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI)
				.then()
				.body(PAGINATION_NEXT_LIMIT, is(2));
	}

	@Test
	public void shouldReturnPaginationNextStartAfterDWhenRequestWithLimit2AndStartAfterExistCodeAndExistMoreOptionsInStore() {
		final FindAllResponse<Option> findAllResponse = mockFindAllResponse(2, LAST_OPTION_IN_RESPONSE, HAS_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockOption(), mockOption()));

		when(optionService.getAllOptions(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI)
				.then()
				.body(PAGINATION_NEXT_START_AFTER, is(LAST_OPTION_IN_RESPONSE));
	}

	@Test
	public void shouldReturnPaginationNextHasMoreResultsTrueWhenRequestWithLimit2AndStartAfterExistCodeAndExistMoreOptionsInStore() {
		final FindAllResponse<Option> findAllResponse = mockFindAllResponse(2, LAST_OPTION_IN_RESPONSE, HAS_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockOption(), mockOption()));

		when(optionService.getAllOptions(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI)
				.then()
				.body(PAGINATION_NEXT_HAS_MORE_RESULTS, is(true));
	}

	@Test
	public void shouldNotReturnPaginationNextCurrentDateTimeWhenRequestWithLimit2AndStartAfterExistCodeAndExistMoreOptionsInStore() {
		final FindAllResponse<Option> findAllResponse = mockFindAllResponse(2, LAST_OPTION_IN_RESPONSE, HAS_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockOption(), mockOption()));

		when(optionService.getAllOptions(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI)
				.then()
				.body(CURRENT_DATE_TIME, not(expect()));
	}

	@Test
	public void shouldReturnStatus200WhenRequestWithLimit2AndStartAfterExistCodeAndNoMoreOptionsInStore() {
		final FindAllResponse<Option> findAllResponse = mockFindAllResponse(2, EMPTY_START_AFTER, NONE_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockOption(), mockOption()));

		when(optionService.getAllOptions(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnPaginationNextLimit2WhenRequestWithLimit2AndStartAfterExistCodeAndNoMoreOptionsInStore() {
		final FindAllResponse<Option> findAllResponse = mockFindAllResponse(2, EMPTY_START_AFTER, NONE_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockOption(), mockOption()));

		when(optionService.getAllOptions(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI)
				.then()
				.body(PAGINATION_NEXT_LIMIT, is(2));
	}

	@Test
	public void shouldReturnEmptyPaginationNextStartAfterWhenRequestWithLimit2AndStartAfterExistCodeAndNoMoreOptionsInStore() {
		final FindAllResponse<Option> findAllResponse = mockFindAllResponse(2, EMPTY_START_AFTER, NONE_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockOption(), mockOption()));

		when(optionService.getAllOptions(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI)
				.then()
				.body(PAGINATION_NEXT_START_AFTER, isEmptyString());
	}

	@Test
	public void shouldReturnPaginationNextHasMoreResultsFalseWhenRequestWithLimit2AndStartAfterExistCodeAndNoMoreOptionsInStore() {
		final FindAllResponse<Option> findAllResponse = mockFindAllResponse(2, EMPTY_START_AFTER, NONE_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockOption(), mockOption()));

		when(optionService.getAllOptions(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI)
				.then()
				.body(PAGINATION_NEXT_HAS_MORE_RESULTS, is(false));
	}

	@Test
	public void shouldNotReturnPaginationNextCurrentDateTimeWhenRequestWithLimit2AndStartAfterExistCodeAndNoMoreOptionsInStore() {
		final FindAllResponse<Option> findAllResponse = mockFindAllResponse(2, EMPTY_START_AFTER, NONE_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockOption(), mockOption()));

		when(optionService.getAllOptions(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI)
				.then()
				.body(CURRENT_DATE_TIME, not(expect()));
	}

	@Test
	public void shouldReturnStatus200WhenRequestWithoutParametersAndNoOptionsInStore() {
		final FindAllResponse<Option> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				EMPTY_RESULTS);

		when(optionService.getAllOptions(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnPaginationNextLimit10WhenRequestWithoutParametersAndNoOptionsInStore() {
		final FindAllResponse<Option> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				EMPTY_RESULTS);

		when(optionService.getAllOptions(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI)
				.then()
				.body(PAGINATION_NEXT_LIMIT, is(DEFAULT_LIMIT));
	}

	@Test
	public void shouldReturnEmptyPaginationNextStartAfterWhenRequestWithoutParametersAndNoOptionsInStore() {
		final FindAllResponse<Option> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				EMPTY_RESULTS);

		when(optionService.getAllOptions(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI)
				.then()
				.body(PAGINATION_NEXT_START_AFTER, isEmptyString());
	}

	@Test
	public void shouldReturnPaginationNextHasMoreResultsFalseWhenRequestWithoutParametersAndNoOptionsInStore() {
		final FindAllResponse<Option> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				EMPTY_RESULTS);

		when(optionService.getAllOptions(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI)
				.then()
				.body(PAGINATION_NEXT_HAS_MORE_RESULTS, is(false));
	}

	@Test
	public void shouldReturnPaginationNextCurrentDateTimeWhenRequestWithoutParametersAndNoOptionsInStore() {
		final FindAllResponse<Option> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				EMPTY_RESULTS);

		when(optionService.getAllOptions(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI)
				.then()
				.body(CURRENT_DATE_TIME, not(isEmptyString()));
	}

	@Test
	public void shouldReturnEmptyResultsWhenRequestWithoutParametersAndNoOptionsInStore1() {
		final FindAllResponse<Option> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				EMPTY_RESULTS);

		when(optionService.getAllOptions(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI)
				.then()
				.body(RESULTS, empty());
	}

	@Test
	public void testShouldReturnStatus200WhenRequestBodyContainingSingleOptionCode() {
		when(optionService.getLatestOptionsWithCodes(any(), anyList())).thenReturn(Collections.singletonList(mockOption()));

		final RequestBody requestBody = createValidRequestBodyWithCodes(Collections.singletonList(EXIST_CODE));

		given()
				.contentType(JSON)
				.body(requestBody)
				.when()
				.post(CATALOG_URL + STORE + OPTION_EVENTS)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnStatus200WhenRequestBodyContainingMultipleOptionCodes() {
		when(optionService.getLatestOptionsWithCodes(any(), anyList())).thenReturn(Arrays.asList(mockOption(), mockOption()));

		final RequestBody requestBody = createValidRequestBodyWithCodes(Arrays.asList(EXIST_CODE, EXIST_CODE));

		given()
				.contentType(JSON)
				.body(requestBody)
				.when()
				.post(CATALOG_URL + STORE + OPTION_EVENTS)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnStatus206WhenRequestBodyContainingMultipleOptionCodesOneOfWhichInvalid() {
		when(optionService.getLatestOptionsWithCodes(any(), anyList())).thenReturn(Collections.singletonList(mockOption()));

		final RequestBody requestBody = createValidRequestBodyWithCodes(Arrays.asList(EXIST_CODE, INVALID_CODE));

		given()
				.contentType(JSON)
				.body(requestBody)
				.when()
				.post(CATALOG_URL + STORE + OPTION_EVENTS)
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
				.post(CATALOG_URL + STORE + OPTION_EVENTS)
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
				.post(CATALOG_URL + STORE + OPTION_EVENTS)
				.then()
				.statusCode(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	public void shouldReturnStatus400WhenRequestBodyWrong() {
		given()
				.contentType(JSON)
				.body("wrong")
				.when()
				.post(CATALOG_URL + STORE + OPTION_EVENTS)
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
				.post(CATALOG_URL + STORE + OPTION_EVENTS)
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
				.post(CATALOG_URL + STORE + OPTION_EVENTS)
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
				.post(CATALOG_URL + STORE + OPTION_EVENTS)
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
				.post(CATALOG_URL + STORE + OPTION_EVENTS)
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
				.post(CATALOG_URL + STORE + OPTION_EVENTS)
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
				.post(CATALOG_URL + STORE + OPTION_EVENTS)
				.then()
				.statusCode(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	public void shouldCallValidateRequestBody() {
		given()
				.contentType(JSON)
				.body(new RequestBody())
				.when()
				.post(CATALOG_URL + STORE + OPTION_EVENTS);

		verify(requestBodyValidator).validate(any(RequestBody.class));
	}

	@Test
	public void shouldNotCallGetLatestOptionsWithCodesWhenRequestBodyIsMalformed() {
		final RequestBody requestBody = new RequestBody();

		given()
				.contentType(JSON)
				.body(requestBody)
				.post(CATALOG_URL + STORE + OPTION_EVENTS);

		verify(optionService, never()).getLatestOptionsWithCodes(anyString(), anyList());
	}

	@Test
	public void shouldCallGetLatestOptionsWithCodesWhenRequestBodyIsValid() {
		final RequestBody requestBody = createValidRequestBodyWithCodes(Collections.singletonList(EXIST_CODE));

		given()
				.contentType(JSON)
				.body(requestBody)
				.post(CATALOG_URL + STORE + OPTION_EVENTS);

		verify(optionService).getLatestOptionsWithCodes(anyString(), anyList());
	}

	@Test
	public void shouldReturnNotEmptyResultsWhenRequestWithModifiedSinceParameter() {
		final FindAllResponse<Option> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockOption(), mockOption()));
		when(optionService.getAllOptions(STORE, "2", null, "2018-01-01T14:47:00.754+00:00", null)).thenReturn(findAllResponse);

		given().param(LIMIT, "2")

				.param("modifiedSince", "2018-01-01T14:47:00.754+00:00")
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI)
				.then()
				.body(RESULTS_SIZE, equalTo(2));
	}

	@Test
	public void shouldReturn400CodeInCaseInvalidParameters() {
		when(optionService.getAllOptions(STORE, "2", null, "A2018-01-01T14:47:00.754+00:00", null)).thenThrow(new InvalidRequestParameterException());

		given().param(LIMIT, "2")

				.param("modifiedSince", "A2018-01-01T14:47:00.754+00:00")
				.when()
				.get(CATALOG_URL + STORE + OPTIONS_URI)
				.then()
				.statusCode(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	public void validateResponseBodyIsSuccessShouldBeTrueWhenRequestIsValid() throws URISyntaxException, ProcessingException, IOException {
		when(optionService.getLatestOptionsWithCodes(any(), anyList())).thenReturn(Collections.singletonList(mockOption()));

		final RequestBody requestBody = createValidRequestBodyWithCodes(Collections.singletonList(EXIST_CODE));

		String responseBody = given()
				.contentType(JSON)
				.body(requestBody)
				.post(CATALOG_URL + STORE + OPTION_EVENTS)
				.getBody()
				.asString();

		JsonSchema jsonSchema =
				JsonSchemaFactory.byDefault().getJsonSchema(getClass().getResource("/schema/option.results.schema.json").toURI().toString());

		assertTrue(jsonSchema.validate(JsonLoader.fromString(responseBody)).isSuccess());
	}

	@Test
	public void shouldReturnStatus200WhenStoreCodeInRequestBodyHasDifferentCaseWithStoreCodeInUri() {
		when(optionService.getLatestOptionsWithCodes(any(), anyList())).thenReturn(Collections.singletonList(mockOption()));

		final RequestBody requestBody = createValidRequestBodyWithCodes(Collections.singletonList(EXIST_CODE));
		requestBody.getData().put(STORE, STORE.toLowerCase(Locale.ENGLISH));

		given()
				.contentType(JSON)
				.body(requestBody)
				.when()
				.post(CATALOG_URL + STORE.toUpperCase(Locale.ENGLISH) + OPTION_EVENTS)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Override
	protected CamelContext createCamelContext() {
		objectMapper.registerModule(new JavaTimeModule());
		final DataFormat jacksonDataFormat = new JacksonDataFormat(objectMapper, Option.class);

		final SimpleRegistry simpleRegistry = new SimpleRegistry();
		simpleRegistry.put("json-jackson", jacksonDataFormat);

		final DefaultCamelContext camelContext = new DefaultCamelContext();
		camelContext.setRegistry(simpleRegistry);

		return camelContext;
	}

	@Override
	protected RoutesBuilder[] createRouteBuilders() {
		return new RoutesBuilder[]{catalogWebServiceRestEndpointsRouteBuilder, optionWebServiceRouteBuilder};
	}

	private Option mockOption() {
		return mockOption(ZonedDateTime.now(), EXIST_CODE);
	}

	private Option mockOption(final ZonedDateTime time, final String code) {
		return new Option(code, STORE, new ArrayList<>(), time, false);
	}

	private RequestBody createValidRequestBodyWithCodes(final List<String> codes) {
		final RequestBody requestBody = new RequestBody();

		final EventType eventType = new EventType();
		eventType.setEventClass("CatalogEventType");
		eventType.setName("OPTIONS_UPDATED");

		requestBody.setEventType(eventType);
		requestBody.setGuid("AGGREGATE");

		final HashMap<String, Object> data = new HashMap<>();
		data.put("type", "option");
		data.put("store", STORE);
		data.put("modifiedDateTime", "2018-01-01T14:47:00+00:00");
		data.put("codes", codes);

		requestBody.setData(data);

		return requestBody;
	}

	private FindAllResponse<Option> mockFindAllResponse(final int limit, final String startAfter, final boolean hasMoreResults,
														final ZonedDateTime currentDateTime, final List<Option> results) {
		final PaginationResponse paginationResponse = new PaginationResponseImpl(limit, startAfter, hasMoreResults);
		return new FindAllResponseImpl<>(paginationResponse, currentDateTime, results);
	}

}
