/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.wiremock.examples.simple;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

/**
 * A custom extensions of WireMock {@link ResponseDefinitionTransformer} that reads
 * a single value from a JSON request and sets that value in the response.
 */
public class DynamicSimpleDefinitionTransformer extends ResponseDefinitionTransformer {

	@Override
	public boolean applyGlobally() {
		return false;
	}

	@Override
	public ResponseDefinition transform(final Request request, final ResponseDefinition responseDefinition, final FileSource fileSource,
			final Parameters parameters) {

		String responseBody;
		int status = HttpURLConnection.HTTP_OK;
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode requestJson = mapper.readTree(request.getBody());

			String requestServiceValue = requestJson.get("service").asText();
			responseBody = "{ \"service\":\"" + requestServiceValue + "\"}";
		} catch (IOException e) {
			responseBody = "{ \"error\":\"Could not parse request JSON\"}";
			status = HttpURLConnection.HTTP_BAD_REQUEST;
		}

		return new ResponseDefinitionBuilder()
				.withStatus(status)
				.withHeader("Content-Type", "application/json")
				.withBody(responseBody)
				.build();

	}

	@Override
	public String getName() {
		return "example-simple-transformer";
	}
}
