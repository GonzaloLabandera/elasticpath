/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.wiremock.examples;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.BeforeClass;
import org.junit.Test;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;

import org.xml.sax.SAXException;

import com.elasticpath.wiremock.examples.jackson.dto.DynamicJacksonResponse;

public class WireMockExampleServicesTest {

	private static final OkHttpClient CLIENT = new OkHttpClient();
	private static final MediaType JSON	= MediaType.get("application/json; charset=utf-8");
	private static final String DYNAMIC_TEST_VALUE = "dynamicTest";
	private static final String[] EXAMPLE_TRANSFORMER_CLASS_NAMES = {
		"com.elasticpath.wiremock.examples.jackson.DynamicJacksonResponseDefinitionTransformer",
		"com.elasticpath.wiremock.examples.simple.DynamicSimpleDefinitionTransformer",
		"com.elasticpath.wiremock.examples.soap.DynamicSoapResponseDefinitionTransformer"
	};

	private static WireMockServer wireMockServer;

	@BeforeClass
	public static void setup() throws XPathExpressionException, IOException, ParserConfigurationException, SAXException {
		wireMockServer = new WireMockServer(
				wireMockConfig()
						.dynamicPort()
						.extensions(EXAMPLE_TRANSFORMER_CLASS_NAMES)
						.extensions(new ResponseTemplateTransformer(false))
						.usingFilesUnderClasspath("wiremock")

		);
		wireMockServer.start();
	}

	/**
	 * Test the /healthcheck service.  This services is a no-op that simply returns HTTP 200.
	 *
	 * @throws IOException
	 */
	@Test
	public void testHealthCheck() throws IOException {

		Request request = new Request.Builder()
				.url(wireMockServer.url("healthcheck"))
				.build();

		try (Response response = CLIENT.newCall(request).execute()) {
			assertEquals(HttpURLConnection.HTTP_OK, response.code());
		}
	}

	/**
	 * Test the static mapping example mock service.  This service
	 * always returns a static response.
	 *
	 * @throws IOException
	 */
	@Test
	public void testStaticMapping() throws IOException {

		Request request = new Request.Builder()
				.url(wireMockServer.url("static"))
				.build();

		try (Response response = CLIENT.newCall(request).execute()) {
			assertEquals(HttpURLConnection.HTTP_OK, response.code());
			assertEquals("application/json", response.header("Content-Type"));
			assertEquals("max-age=86400", response.header("Cache-Control"));

			JsonNode node = new ObjectMapper().readTree(response.body().bytes());
			assertEquals("1", node.get("service").asText());
		}
	}

	/**
	 * Test the static templated mapping example mock service.  This service
	 * always returns a static response, but the static response varies based on the value passed in the URL.
	 *
	 * @throws IOException
	 */
	@Test
	public void testStaticTemplatedMapping() throws IOException {
		checkTemplatedResponse("1");
		checkTemplatedResponse("2");
		checkTemplatedResponse("3");
	}

	private void checkTemplatedResponse(final String responseId) throws IOException {
		Request request = new Request.Builder()
				.url(wireMockServer.url("staticByPath/" + responseId))
				.build();

		try (Response response = CLIENT.newCall(request).execute()) {
			assertEquals(HttpURLConnection.HTTP_OK, response.code());

			JsonNode node = new ObjectMapper().readTree(response.body().bytes());
			assertEquals(responseId, node.get("responseId").asText());
		}

	}

	/**
	 * Test the jackson mapping mock service.  This service takes the supplied value for the "service"
	 * param from the request and injects it into the response.
	 *
	 * @throws IOException
	 */
	@Test
	public void testDynamicMapping() throws IOException {
		RequestBody postBody = RequestBody.create(JSON, "{ \"service\":\"" + DYNAMIC_TEST_VALUE + "\"}");
		Request request = new Request.Builder()
				.url(wireMockServer.url("simple"))
				.post(postBody)
				.build();

		try (Response response = CLIENT.newCall(request).execute()) {
			assertEquals(HttpURLConnection.HTTP_OK, response.code());
			assertEquals("application/json", response.header("Content-Type"));

			JsonNode node = new ObjectMapper().readTree(response.body().bytes());
			assertEquals(DYNAMIC_TEST_VALUE, node.get("service").asText());
		}

	}

	/**
	 * Test for the jackson mapping mock service that uses Jackson.  The mock service deserializes
	 * a complex JSON payload, and uses those values to generate a response object which is serialized into
	 * a JSON response.
	 *
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void testDynamicMappingWithJackson() throws IOException, URISyntaxException {
		URL requestBodyFile = this.getClass().getClassLoader().getResource("jackson/request.json");
		RequestBody postBody = RequestBody.create(JSON, new File(requestBodyFile.toURI()));
		Request request = new Request.Builder()
				.url(wireMockServer.url("jackson"))
				.post(postBody)
				.build();

		try (Response response = CLIENT.newCall(request).execute()) {
			assertEquals(HttpURLConnection.HTTP_OK, response.code());
			assertEquals("application/json", response.header("Content-Type"));
			DynamicJacksonResponse responseObj = new ObjectMapper().readValue(response.body().bytes(), DynamicJacksonResponse.class);
			assertEquals(2, responseObj.getResponseLines().size());
		}

	}
}
