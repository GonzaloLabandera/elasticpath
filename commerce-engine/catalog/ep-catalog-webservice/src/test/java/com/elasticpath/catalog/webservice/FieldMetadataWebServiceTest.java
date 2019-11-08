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

import com.elasticpath.catalog.entity.fieldmetadata.FieldMetadata;
import com.elasticpath.catalog.reader.FindAllResponse;
import com.elasticpath.catalog.reader.PaginationResponse;
import com.elasticpath.catalog.reader.impl.FindAllResponseImpl;
import com.elasticpath.catalog.reader.impl.PaginationResponseImpl;
import com.elasticpath.catalog.webservice.exception.InvalidRequestParameterException;
import com.elasticpath.catalog.webservice.request.entity.EventType;
import com.elasticpath.catalog.webservice.request.entity.RequestBody;
import com.elasticpath.catalog.webservice.request.validator.RequestBodyValidator;
import com.elasticpath.catalog.webservice.services.FieldMetadataService;

/**
 * Tests FieldMetadataWebServiceRouteBuilder.
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports"})
public class FieldMetadataWebServiceTest extends CamelTestSupport {

	private static final String STORE = "store";
	private static final String EXIST_CODE = "existCode";
	private static final String NOT_EXIST_CODE = "notExistCode";
	private static final String INVALID_CODE = "invalidCode";
	private static final String WEB_XML_PATH = "src/test/webapp";
	private static final int TOMCAT_PORT = 8097;
	private static final String CATALOG_URL = "http://localhost:" + TOMCAT_PORT + "/api/syndication/v1/catalog/";
	private static final String FIELD_METADATA_URI = "/fieldmetadata/";
	private static final String FIELD_METADATA_EVENTS = "/fieldmetadataevents/";
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
	private static final List<FieldMetadata> EMPTY_RESULTS = new ArrayList<>();
	private static final String LAST_FIELD_METADATA_IN_RESPONSE = "lastFieldMetadataInResponse";
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
	private FieldMetadataService fieldMetadataService;

	@Mock
	private RequestBodyValidator requestBodyValidator;

	@InjectMocks
	private CatalogWebServiceRestEndpointsRouteBuilder catalogWebServiceRestEndpointsRouteBuilder;

	@InjectMocks
	private FieldMetadataWebServiceRouteBuilder fieldMetadataWebServiceRouteBuilder;

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
	public void testThatShouldReturnStatus200WhenFieldMetadataCodeExist() {
		when(fieldMetadataService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockFieldmetadata()));

		given()
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI + EXIST_CODE)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void testThatShouldReturnContentTypeHeaderEqualsJsonWhenFieldMetadataCodeExist() {
		when(fieldMetadataService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockFieldmetadata()));

		given()
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI + EXIST_CODE)
				.then()
				.contentType(JSON);
	}

	@Test
	public void testThatShouldReturnETagHeaderWhenFieldMetadataCodeExist() {
		when(fieldMetadataService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockFieldmetadata()));

		given()
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI + EXIST_CODE)
				.then()
				.header(E_TAG_HEADER, is(not(isEmptyString())));
	}


	@Test
	public void testThatShouldReturnContentLengthHeaderWhenFieldMetadataCodeExist() {
		when(fieldMetadataService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockFieldmetadata()));

		given()
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI + EXIST_CODE)
				.then()
				.header(CONTENT_LENGTH_HEADER, is(not(isEmptyString())));
	}

	@Test
	public void testThatShouldReturnStatus404WhenFieldMetadataCodeNotExist() {
		given()
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI + NOT_EXIST_CODE)
				.then()
				.statusCode(HttpStatus.NOT_FOUND.value());
	}

	@Test
	public void testThatShouldReturnStatus304WhenGetFieldMetadataAgainWithETagFromPreviousResult() {
		when(fieldMetadataService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockFieldmetadata()));
		final String eTag = get(CATALOG_URL + STORE + FIELD_METADATA_URI + EXIST_CODE).header(E_TAG_HEADER);

		given()
				.header(IF_NONE_MATCH_HEADER, eTag)
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI + EXIST_CODE)
				.then()
				.statusCode(HttpStatus.NOT_MODIFIED.value());
	}

	@Test
	public void testThatShouldReturnStatus200WhenGetFieldMetadataAgainWithETagFromPreviousResultAndFieldMetadataAlreadyUpdated() {
		when(fieldMetadataService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockFieldmetadata()));
		final String eTag = get(CATALOG_URL + STORE + FIELD_METADATA_URI + EXIST_CODE).header(E_TAG_HEADER);
		when(fieldMetadataService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockFieldmetadata()));

		given()
				.header(IF_NONE_MATCH_HEADER, eTag)
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI + EXIST_CODE)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void testThatShouldReturnModifiedFieldMetadataWhenGetFieldMetadataAgainWithETagFromPreviousResultAndFieldMetadataAlreadyUpdated()
			throws JsonProcessingException {
		when(fieldMetadataService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockFieldmetadata()));
		final String eTag = get(CATALOG_URL + STORE + FIELD_METADATA_URI + EXIST_CODE).header(E_TAG_HEADER);

		final FieldMetadata mockFieldmetadata = mockFieldmetadata();
		when(fieldMetadataService.get(STORE, EXIST_CODE)).thenReturn(Optional.of(mockFieldmetadata));

		given()
				.header(IF_NONE_MATCH_HEADER, eTag)
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI + EXIST_CODE)
				.then()
				.body(is(objectMapper.writeValueAsString(mockFieldmetadata)));
	}

	@Test
	public void testShouldReturnStatus200WhenRequestWithoutParametersAndLessThan10FieldMetadatasInStore() {
		final FindAllResponse<FieldMetadata> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockFieldmetadata(), mockFieldmetadata()));

		when(fieldMetadataService.getAllFieldMetadata(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnPaginationNextLimit10WhenRequestWithoutParametersAndLessThan10FieldMetadatasInStore() {
		final FindAllResponse<FieldMetadata> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockFieldmetadata(), mockFieldmetadata()));

		when(fieldMetadataService.getAllFieldMetadata(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI)
				.then()
				.body(PAGINATION_NEXT_LIMIT, is(DEFAULT_LIMIT));
	}

	@Test
	public void shouldReturnEmptyPaginationNextStartAfterWhenRequestWithoutParametersAndLessThan10FieldMetadatasInStore() {
		final FindAllResponse<FieldMetadata> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockFieldmetadata(), mockFieldmetadata()));

		when(fieldMetadataService.getAllFieldMetadata(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI)
				.then()
				.body(PAGINATION_NEXT_START_AFTER, isEmptyString());
	}

	@Test
	public void shouldReturnPaginationNextHasMoreResultsFalseWhenRequestWithoutParametersAndLessThan10FieldMetadatasInStore() {
		final FindAllResponse<FieldMetadata> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockFieldmetadata(), mockFieldmetadata()));

		when(fieldMetadataService.getAllFieldMetadata(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI)
				.then()
				.body(PAGINATION_NEXT_HAS_MORE_RESULTS, is(false));
	}

	@Test
	public void shouldReturnPaginationNextCurrentDateTimeWhenRequestWithoutParametersAndLessThan10FieldMetadatasInStore() {
		final FindAllResponse<FieldMetadata> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockFieldmetadata(), mockFieldmetadata()));

		when(fieldMetadataService.getAllFieldMetadata(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI)
				.then()
				.body(CURRENT_DATE_TIME, not(isEmptyString()));
	}

	@Test
	public void shouldReturnStatus200WhenRequestWithLimit2AndExistMoreFieldMetadatasInStore() {
		final FindAllResponse<FieldMetadata> findAllResponse = mockFindAllResponse(2, LAST_FIELD_METADATA_IN_RESPONSE, HAS_MORE_RESULTS,
				CURRENT_DATE,
				Arrays.asList(mockFieldmetadata(), mockFieldmetadata()));

		when(fieldMetadataService.getAllFieldMetadata(STORE, "2", null, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnPaginationNextLimit2WhenRequestWithLimit2AndExistMoreFieldMetadatasInStore() {
		final FindAllResponse<FieldMetadata> findAllResponse = mockFindAllResponse(2, LAST_FIELD_METADATA_IN_RESPONSE, HAS_MORE_RESULTS,
				CURRENT_DATE,
				Arrays.asList(mockFieldmetadata(), mockFieldmetadata()));

		when(fieldMetadataService.getAllFieldMetadata(STORE, "2", null, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI)
				.then()
				.body(PAGINATION_NEXT_LIMIT, is(2));
	}

	@Test
	public void shouldReturnPaginationNextStartAfterBWhenRequestWithLimit2AndExistMoreFieldMetadatasInStore() {
		final FindAllResponse<FieldMetadata> findAllResponse = mockFindAllResponse(2, LAST_FIELD_METADATA_IN_RESPONSE, HAS_MORE_RESULTS,
				CURRENT_DATE,
				Arrays.asList(mockFieldmetadata(), mockFieldmetadata()));

		when(fieldMetadataService.getAllFieldMetadata(STORE, "2", null, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI)
				.then()
				.body(PAGINATION_NEXT_START_AFTER, is(LAST_FIELD_METADATA_IN_RESPONSE));
	}

	@Test
	public void shouldReturnPaginationNextHasMoreResultsTrueWhenRequestWithLimit2AndExistMoreFieldMetadatasInStore() {
		final FindAllResponse<FieldMetadata> findAllResponse = mockFindAllResponse(2, LAST_FIELD_METADATA_IN_RESPONSE, HAS_MORE_RESULTS,
				CURRENT_DATE,
				Arrays.asList(mockFieldmetadata(), mockFieldmetadata()));

		when(fieldMetadataService.getAllFieldMetadata(STORE, "2", null, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI)
				.then()
				.body(PAGINATION_NEXT_HAS_MORE_RESULTS, is(true));
	}

	@Test
	public void shouldReturnPaginationNextCurrentDateTimeWhenRequestWithLimit2AndExistMoreFieldMetadatasInStore() {
		final FindAllResponse<FieldMetadata> findAllResponse = mockFindAllResponse(2, LAST_FIELD_METADATA_IN_RESPONSE, HAS_MORE_RESULTS,
				CURRENT_DATE,
				Arrays.asList(mockFieldmetadata(), mockFieldmetadata()));

		when(fieldMetadataService.getAllFieldMetadata(STORE, "2", null, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI)
				.then()
				.body(CURRENT_DATE_TIME, not(isEmptyString()));
	}

	@Test
	public void shouldReturnStatus200WhenRequestWithLimit2AndStartAfterExistCodeAndExistMoreFieldMetadatasInStore() {
		final FindAllResponse<FieldMetadata> findAllResponse = mockFindAllResponse(2, LAST_FIELD_METADATA_IN_RESPONSE, HAS_MORE_RESULTS,
				NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockFieldmetadata(), mockFieldmetadata()));

		when(fieldMetadataService.getAllFieldMetadata(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnPaginationNextLimit2WhenRequestWithLimit2AndStartAfterExistCodeAndExistMoreFieldMetadatasInStore() {
		final FindAllResponse<FieldMetadata> findAllResponse = mockFindAllResponse(2, LAST_FIELD_METADATA_IN_RESPONSE, HAS_MORE_RESULTS,
				NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockFieldmetadata(), mockFieldmetadata()));

		when(fieldMetadataService.getAllFieldMetadata(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI)
				.then()
				.body(PAGINATION_NEXT_LIMIT, is(2));
	}

	@Test
	public void shouldReturnPaginationNextStartAfterDWhenRequestWithLimit2AndStartAfterExistCodeAndExistMoreFieldMetadatasInStore() {
		final FindAllResponse<FieldMetadata> findAllResponse = mockFindAllResponse(2, LAST_FIELD_METADATA_IN_RESPONSE, HAS_MORE_RESULTS,
				NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockFieldmetadata(), mockFieldmetadata()));

		when(fieldMetadataService.getAllFieldMetadata(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI)
				.then()
				.body(PAGINATION_NEXT_START_AFTER, is(LAST_FIELD_METADATA_IN_RESPONSE));
	}

	@Test
	public void shouldReturnPaginationNextHasMoreResultsTrueWhenRequestWithLimit2AndStartAfterExistCodeAndExistMoreFieldMetadatasInStore() {
		final FindAllResponse<FieldMetadata> findAllResponse = mockFindAllResponse(2, LAST_FIELD_METADATA_IN_RESPONSE, HAS_MORE_RESULTS,
				NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockFieldmetadata(), mockFieldmetadata()));

		when(fieldMetadataService.getAllFieldMetadata(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI)
				.then()
				.body(PAGINATION_NEXT_HAS_MORE_RESULTS, is(true));
	}

	@Test
	public void shouldNotReturnPaginationNextCurrentDateTimeWhenRequestWithLimit2AndStartAfterExistCodeAndExistMoreFieldMetadatasInStore() {
		final FindAllResponse<FieldMetadata> findAllResponse = mockFindAllResponse(2, LAST_FIELD_METADATA_IN_RESPONSE, HAS_MORE_RESULTS,
				NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockFieldmetadata(), mockFieldmetadata()));

		when(fieldMetadataService.getAllFieldMetadata(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI)
				.then()
				.body(CURRENT_DATE_TIME, not(expect()));
	}

	@Test
	public void shouldReturnStatus200WhenRequestWithLimit2AndStartAfterExistCodeAndNoMoreFieldMetadatasInStore() {
		final FindAllResponse<FieldMetadata> findAllResponse = mockFindAllResponse(2, EMPTY_START_AFTER, NONE_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockFieldmetadata(), mockFieldmetadata()));

		when(fieldMetadataService.getAllFieldMetadata(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnPaginationNextLimit2WhenRequestWithLimit2AndStartAfterExistCodeAndNoMoreFieldMetadatasInStore() {
		final FindAllResponse<FieldMetadata> findAllResponse = mockFindAllResponse(2, EMPTY_START_AFTER, NONE_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockFieldmetadata(), mockFieldmetadata()));

		when(fieldMetadataService.getAllFieldMetadata(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI)
				.then()
				.body(PAGINATION_NEXT_LIMIT, is(2));
	}

	@Test
	public void shouldReturnEmptyPaginationNextStartAfterWhenRequestWithLimit2AndStartAfterExistCodeAndNoMoreFieldMetadatasInStore() {
		final FindAllResponse<FieldMetadata> findAllResponse = mockFindAllResponse(2, EMPTY_START_AFTER, NONE_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockFieldmetadata(), mockFieldmetadata()));

		when(fieldMetadataService.getAllFieldMetadata(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI)
				.then()
				.body(PAGINATION_NEXT_START_AFTER, isEmptyString());
	}

	@Test
	public void shouldReturnPaginationNextHasMoreResultsFalseWhenRequestWithLimit2AndStartAfterExistCodeAndNoMoreFieldMetadatasInStore() {
		final FindAllResponse<FieldMetadata> findAllResponse = mockFindAllResponse(2, EMPTY_START_AFTER, NONE_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockFieldmetadata(), mockFieldmetadata()));

		when(fieldMetadataService.getAllFieldMetadata(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI)
				.then()
				.body(PAGINATION_NEXT_HAS_MORE_RESULTS, is(false));
	}

	@Test
	public void shouldNotReturnPaginationNextCurrentDateTimeWhenRequestWithLimit2AndStartAfterExistCodeAndNoMoreFieldMetadatasInStore() {
		final FindAllResponse<FieldMetadata> findAllResponse = mockFindAllResponse(2, EMPTY_START_AFTER, NONE_MORE_RESULTS, NONE_CURRENT_DATE_TIME,
				Arrays.asList(mockFieldmetadata(), mockFieldmetadata()));

		when(fieldMetadataService.getAllFieldMetadata(STORE, "2", EXIST_CODE, null, null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, 2)
				.param(START_AFTER, EXIST_CODE)
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI)
				.then()
				.body(CURRENT_DATE_TIME, not(expect()));
	}

	@Test
	public void shouldReturnStatus200WhenRequestWithoutParametersAndNoFieldMetadatasInStore() {
		final FindAllResponse<FieldMetadata> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				EMPTY_RESULTS);

		when(fieldMetadataService.getAllFieldMetadata(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnPaginationNextLimit10WhenRequestWithoutParametersAndNoFieldMetadatasInStore() {
		final FindAllResponse<FieldMetadata> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				EMPTY_RESULTS);

		when(fieldMetadataService.getAllFieldMetadata(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI)
				.then()
				.body(PAGINATION_NEXT_LIMIT, is(DEFAULT_LIMIT));
	}

	@Test
	public void shouldReturnEmptyPaginationNextStartAfterWhenRequestWithoutParametersAndNoFieldMetadatasInStore() {
		final FindAllResponse<FieldMetadata> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				EMPTY_RESULTS);

		when(fieldMetadataService.getAllFieldMetadata(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI)
				.then()
				.body(PAGINATION_NEXT_START_AFTER, isEmptyString());
	}

	@Test
	public void shouldReturnPaginationNextHasMoreResultsFalseWhenRequestWithoutParametersAndNoFieldMetadatasInStore() {
		final FindAllResponse<FieldMetadata> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				EMPTY_RESULTS);

		when(fieldMetadataService.getAllFieldMetadata(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI)
				.then()
				.body(PAGINATION_NEXT_HAS_MORE_RESULTS, is(false));
	}

	@Test
	public void shouldReturnPaginationNextCurrentDateTimeWhenRequestWithoutParametersAndNoFieldMetadatasInStore() {
		final FindAllResponse<FieldMetadata> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				EMPTY_RESULTS);

		when(fieldMetadataService.getAllFieldMetadata(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI)
				.then()
				.body(CURRENT_DATE_TIME, not(isEmptyString()));
	}

	@Test
	public void shouldReturnEmptyResultsWhenRequestWithoutParametersAndNoFieldMetadatasInStore1() {
		final FindAllResponse<FieldMetadata> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				EMPTY_RESULTS);

		when(fieldMetadataService.getAllFieldMetadata(STORE, null, null, null, null)).thenReturn(findAllResponse);

		given()
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI)
				.then()
				.body(RESULTS, empty());
	}

	@Test
	public void testShouldReturnStatus200WhenRequestBodyContainingSingleFieldMetadataCode() {
		when(fieldMetadataService.getLatestFieldMetadataWithCodes(any(), anyList())).thenReturn(Collections.singletonList(mockFieldmetadata()));

		final RequestBody requestBody = createValidRequestBodyWithCodes(Collections.singletonList(EXIST_CODE));

		given()
				.contentType(JSON)
				.body(requestBody)
				.when()
				.post(CATALOG_URL + STORE + FIELD_METADATA_EVENTS)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnStatus200WhenRequestBodyContainingMultipleFieldMetadataCodes() {
		when(fieldMetadataService.getLatestFieldMetadataWithCodes(any(), anyList())).thenReturn(Arrays.asList(mockFieldmetadata(),
				mockFieldmetadata()));

		final RequestBody requestBody = createValidRequestBodyWithCodes(Arrays.asList(EXIST_CODE, EXIST_CODE));

		given()
				.contentType(JSON)
				.body(requestBody)
				.when()
				.post(CATALOG_URL + STORE + FIELD_METADATA_EVENTS)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnStatus206WhenRequestBodyContainingMultipleFieldMetadataCodesOneOfWhichInvalid() {
		when(fieldMetadataService.getLatestFieldMetadataWithCodes(any(), anyList())).thenReturn(Collections.singletonList(mockFieldmetadata()));

		final RequestBody requestBody = createValidRequestBodyWithCodes(Arrays.asList(EXIST_CODE, INVALID_CODE));

		given()
				.contentType(JSON)
				.body(requestBody)
				.when()
				.post(CATALOG_URL + STORE + FIELD_METADATA_EVENTS)
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
				.post(CATALOG_URL + STORE + FIELD_METADATA_EVENTS)
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
				.post(CATALOG_URL + STORE + FIELD_METADATA_EVENTS)
				.then()
				.statusCode(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	public void shouldReturnStatus400WhenRequestBodyWrong() {
		given()
				.contentType(JSON)
				.body("wrong")
				.when()
				.post(CATALOG_URL + STORE + FIELD_METADATA_EVENTS)
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
				.post(CATALOG_URL + STORE + FIELD_METADATA_EVENTS)
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
				.post(CATALOG_URL + STORE + FIELD_METADATA_EVENTS)
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
				.post(CATALOG_URL + STORE + FIELD_METADATA_EVENTS)
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
				.post(CATALOG_URL + STORE + FIELD_METADATA_EVENTS)
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
				.post(CATALOG_URL + STORE + FIELD_METADATA_EVENTS)
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
				.post(CATALOG_URL + STORE + FIELD_METADATA_EVENTS)
				.then()
				.statusCode(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	public void shouldCallValidateRequestBody() {
		given()
				.contentType(JSON)
				.body(new RequestBody())
				.when()
				.post(CATALOG_URL + STORE + FIELD_METADATA_EVENTS);

		verify(requestBodyValidator).validate(any(RequestBody.class));
	}

	@Test
	public void shouldNotCallGetLatestFieldMetadatasWithCodesWhenRequestBodyIsMalformed() {
		final RequestBody requestBody = new RequestBody();

		given()
				.contentType(JSON)
				.body(requestBody)
				.post(CATALOG_URL + STORE + FIELD_METADATA_EVENTS);

		verify(fieldMetadataService, never()).getLatestFieldMetadataWithCodes(anyString(), anyList());
	}

	@Test
	public void shouldCallGetLatestFieldMetadatasWithCodesWhenRequestBodyIsValid() {
		final RequestBody requestBody = createValidRequestBodyWithCodes(Collections.singletonList(EXIST_CODE));

		given()
				.contentType(JSON)
				.body(requestBody)
				.post(CATALOG_URL + STORE + FIELD_METADATA_EVENTS);

		verify(fieldMetadataService).getLatestFieldMetadataWithCodes(anyString(), anyList());
	}

	@Test
	public void shouldReturnNoTEmptyResultsWhenRequestWithModifiedSinceParameter() {
		final FindAllResponse<FieldMetadata> findAllResponse = mockFindAllResponse(DEFAULT_LIMIT, EMPTY_START_AFTER, NONE_MORE_RESULTS, CURRENT_DATE,
				Arrays.asList(mockFieldmetadata(), mockFieldmetadata()));
		when(fieldMetadataService.getAllFieldMetadata(STORE, "2", null, "2018-01-01T14:47:00.754+00:00", null)).thenReturn(findAllResponse);

		given()
				.param(LIMIT, "2")
				.contentType(JSON)
				.param("modifiedSince", "2018-01-01T14:47:00.754+00:00")
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI)
				.then()
				.body(RESULTS_SIZE, equalTo(2));
	}

	@Test
	public void shouldReturn400CodeInCaseInvalidParameters() {
		when(fieldMetadataService.getAllFieldMetadata(STORE, "2", null, "A2018-01-01T14:47:00.754+00:00", null))
				.thenThrow(new InvalidRequestParameterException());

		given()
				.contentType(JSON)
				.param(LIMIT, "2")
				.param("modifiedSince", "A2018-01-01T14:47:00.754+00:00")
				.when()
				.get(CATALOG_URL + STORE + FIELD_METADATA_URI)
				.then()
				.statusCode(HttpStatus.BAD_REQUEST.value());
		verify(fieldMetadataService).getAllFieldMetadata(STORE, "2", null, "A2018-01-01T14:47:00.754+00:00", null);
	}

	@Test
	public void validateResponseBodyIsSuccessShouldBeTrueWhenRequestIsValid() throws URISyntaxException, ProcessingException, IOException {
		when(fieldMetadataService.getLatestFieldMetadataWithCodes(any(), anyList())).thenReturn(Collections.singletonList(mockFieldmetadata()));

		final RequestBody requestBody = createValidRequestBodyWithCodes(Collections.singletonList(EXIST_CODE));

		String responseBody = given()
				.contentType(JSON)
				.body(requestBody)
				.post(CATALOG_URL + STORE + FIELD_METADATA_EVENTS)
				.getBody()
				.asString();

		JsonSchema jsonSchema =
				JsonSchemaFactory.byDefault().getJsonSchema(getClass().getResource("/schema/fieldMetadata.results.schema.json").toURI().toString());

		assertTrue(jsonSchema.validate(JsonLoader.fromString(responseBody)).isSuccess());
	}

	@Test
	public void shouldReturnStatus200WhenStoreCodeInRequestBodyHasDifferentCaseWithStoreCodeInUri() {
		when(fieldMetadataService.getLatestFieldMetadataWithCodes(any(), anyList())).thenReturn(Collections.singletonList(mockFieldmetadata()));

		final RequestBody requestBody = createValidRequestBodyWithCodes(Collections.singletonList(EXIST_CODE));
		requestBody.getData().put(STORE, STORE.toLowerCase(Locale.ENGLISH));

		given()
				.contentType(JSON)
				.body(requestBody)
				.when()
				.post(CATALOG_URL + STORE.toUpperCase(Locale.ENGLISH) + FIELD_METADATA_EVENTS)
				.then()
				.statusCode(HttpStatus.OK.value());
	}

	@Override
	protected CamelContext createCamelContext() {
		objectMapper.registerModule(new JavaTimeModule());
		final DataFormat jacksonDataFormat = new JacksonDataFormat(objectMapper, FieldMetadata.class);

		final SimpleRegistry simpleRegistry = new SimpleRegistry();
		simpleRegistry.put("json-jackson", jacksonDataFormat);

		final DefaultCamelContext camelContext = new DefaultCamelContext();
		camelContext.setRegistry(simpleRegistry);

		return camelContext;
	}


	@Override
	protected RoutesBuilder[] createRouteBuilders() {
		return new RoutesBuilder[]{catalogWebServiceRestEndpointsRouteBuilder, fieldMetadataWebServiceRouteBuilder};
	}

	private FieldMetadata mockFieldmetadata() {
		return new FieldMetadata(EXIST_CODE, STORE, new ArrayList<>(), ZonedDateTime.now(), false);
	}

	private RequestBody createValidRequestBodyWithCodes(final List<String> codes) {
		final RequestBody requestBody = new RequestBody();

		final EventType eventType = new EventType();
		eventType.setEventClass("CatalogEventType");
		eventType.setName("FIELD_METADATA_UPDATED");

		requestBody.setEventType(eventType);
		requestBody.setGuid("AGGREGATE");

		final HashMap<String, Object> data = new HashMap<>();
		data.put("type", "fieldMetadata");
		data.put("store", STORE);
		data.put("modifiedDateTime", "2018-01-01T14:47:00+00:00");
		data.put("codes", codes);

		requestBody.setData(data);

		return requestBody;
	}

	private FindAllResponse<FieldMetadata> mockFindAllResponse(final int limit, final String startAfter, final boolean hasMoreResults,
															   final ZonedDateTime currentDateTime, final List<FieldMetadata> results) {
		final PaginationResponse paginationResponse = new PaginationResponseImpl(limit, startAfter, hasMoreResults);
		return new FindAllResponseImpl<>(paginationResponse, currentDateTime, results);
	}
}
