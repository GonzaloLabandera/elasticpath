/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.wiremock.examples.jackson;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

import com.elasticpath.wiremock.examples.jackson.dto.DynamicJacksonRequest;
import com.elasticpath.wiremock.examples.jackson.dto.DynamicJacksonResponse;
import com.elasticpath.wiremock.examples.jackson.dto.DynamicJacksonResponseLine;

/**
 * A custom extensions of WireMock {@link ResponseDefinitionTransformer} that reads
 * a more complex JSON request which uses Jackson so deserialize the JSON into POJO(s)
 * {@link DynamicJacksonRequest}.  It then uses those POJO's to build the response POJO(s)
 * {@link DynamicJacksonResponse} which also use Jackson to serialize into response JSON.
 */
public class DynamicJacksonResponseDefinitionTransformer extends ResponseDefinitionTransformer {

	@Override
	public ResponseDefinition transform(final Request request, final ResponseDefinition responseDefinition, final FileSource fileSource,
			final Parameters parameters) {

		String responseBody;
		int status = HttpURLConnection.HTTP_OK;
		try {
			ObjectMapper mapper = new ObjectMapper();
			DynamicJacksonRequest requestObj = mapper.readValue(request.getBody(), DynamicJacksonRequest.class);

			DynamicJacksonResponse responseObj = buildResponseObject(requestObj);
			mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
			responseBody = mapper.writeValueAsString(responseObj);
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

	/**
	 * Creates and returns a new response object.
	 *
	 * @param requestObj  the request lines object is used to create a list of response lines with the same size
	 * @return the fully populated and complete response
	 */
	private DynamicJacksonResponse buildResponseObject(final DynamicJacksonRequest requestObj) {
		DynamicJacksonResponse responseObj = new DynamicJacksonResponse();
		responseObj.setCreateDate(new Date());
		responseObj.setStatus("Complete");

		List<DynamicJacksonResponseLine> responseLines = new LinkedList<>();
		requestObj.getRequestLines().forEach(requestLine -> {
			DynamicJacksonResponseLine responseLine = new DynamicJacksonResponseLine();
			responseLine.setId(requestLine.getId());
			responseLine.setDoubleVal(Double.valueOf(1.0));
			responseLines.add(responseLine);
		});
		responseObj.setResponseLines(responseLines);
		return responseObj;
	}

	@Override
	public String getName() {
		return "example-jackson-transformer";
	}

	@Override
	public boolean applyGlobally() {
		return false;
	}

}
