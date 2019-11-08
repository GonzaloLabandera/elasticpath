/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.wiremock.cucumber.steps


import cucumber.api.java.After
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

import org.apache.commons.lang3.StringUtils
import org.assertj.core.api.Assertions

public class WireMockSteps {

	private static final String BASE_URL = StringUtils.defaultString(System.getProperty("ep.mock.baseurl"), "http://localhost:8080/mock/")

	private static final String HEALTHCHECK_URI = "/healthcheck"
	private static final MediaType JSON	= MediaType.get("application/json; charset=utf-8")
	private static final MediaType XML	= MediaType.get("text/xml; charset=utf-8")

	private static final OkHttpClient CLIENT = new OkHttpClient()

	private static Response response

	@Given('^Wiremock service is up$')
	static void verifyWiremockStarted() {
		getEndpoint(HEALTHCHECK_URI)
		validateCodeWithEmptyBody(HttpURLConnection.HTTP_OK);
	}

	@When('^I GET the (.+) endpoint$')
	static void getEndpoint(String endpoint) throws IOException {
		Request request = new Request.Builder()
				.url(BASE_URL + endpoint)
				.build()

		response = CLIENT.newCall(request).execute()
	}

	@When('^I POST to the (.+) endpoint with body (.*)$')
	static void postEndpoint(String endpoint, String postBodyStr) throws IOException {
		RequestBody postBody = RequestBody.create(JSON, postBodyStr)
		Request request = new Request.Builder()
				.url(BASE_URL + endpoint)
				.post(postBody)
				.build()

		response = CLIENT.newCall(request).execute()
	}

	@When('^I POST to the (.+) endpoint with request (.*)$')
	static void postEndpointFromFile(String endpoint, String fileName) throws IOException {
		URL requestBodyFile = WireMockSteps.class.getClassLoader().getResource(fileName)
		RequestBody postBody

		if (fileName.endsWith(".xml")) {
			postBody = RequestBody.create(XML, new File(requestBodyFile.toURI()))
		} else {
			postBody = RequestBody.create(JSON, new File(requestBodyFile.toURI()))
		}

		Request request = new Request.Builder()
				.url(BASE_URL + endpoint)
				.post(postBody)
				.build()

		response = CLIENT.newCall(request).execute()
	}

	@Then('I receive a (\\d+) response with an empty body$')
	static void validateCodeWithEmptyBody(Integer httpResponseCode) {
		Assertions.assertThat(response.code())
				.as("HTTP response was not " + httpResponseCode)
				.isEqualTo(httpResponseCode.intValue())

		Assertions.assertThat(response.body().bytes())
				.as("Response body was not empty.")
				.isEmpty()
	}

	@Then('I receive a (\\d+) response with a body that contains (.*)$')
	static void validateCodeAndBodyContains(Integer httpResponseCode, String responseValidationStr) {
		Assertions.assertThat(response.code())
				.as("HTTP response was not " + httpResponseCode)
				.isEqualTo(httpResponseCode.intValue())

		Assertions.assertThat(response.body().string())
				.as("Response body did not contain " + responseValidationStr + ".")
				.contains(responseValidationStr)
	}


	@After
	static void closeDown() {
		if (response != null) {
			response.close()
		}
	}
}
