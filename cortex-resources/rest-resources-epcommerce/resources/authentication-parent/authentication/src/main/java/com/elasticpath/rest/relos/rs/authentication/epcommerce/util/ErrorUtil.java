/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.relos.rs.authentication.epcommerce.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.exception.structured.EpStructureErrorMessageException;
import com.elasticpath.rest.jackson.RelosJacksonConfigurator;
import com.elasticpath.rest.jackson.RelosJacksonObjectMapper;
import com.elasticpath.rest.schema.StructuredMessage;

/**
 * Utilities for error reporting.
 */
public final class ErrorUtil {

	private ErrorUtil() {
		//static class
	}

	/**
	 * Update the httpResponse with the passed HTTP response status and error message.
	 *
	 * @param httpResponse           the HTTP servlet response
	 * @param responseStatus         the HTTP response status to set
	 * @param structuredErrorMessage the error message to put in the body of the response
	 * @throws IOException if an exception occurs while setting the response body
	 */
	public static void reportFailure(final HttpServletResponse httpResponse,
									 final int responseStatus,
									 final StructuredErrorMessage structuredErrorMessage) throws IOException {

		// Map to StructuredMessage to standardize the error output in format.
		StructuredMessage structuredMessage = StructuredMessage.builder()
				.withData(structuredErrorMessage.getData())
				.withId(structuredErrorMessage.getMessageId())
				.withDebugMessage(structuredErrorMessage.getDebugMessage())
				.withType(structuredErrorMessage.getType().getName())
				.build();

		RelosJacksonObjectMapper mapper = new RelosJacksonObjectMapper();
		mapper.activate();
		RelosJacksonConfigurator configurator = new RelosJacksonConfigurator();
		configurator.objectMapper = mapper;
		configurator.activate();
		String jsonStr = mapper.writeValueAsString(structuredMessage);
		try (PrintWriter writer = httpResponse.getWriter()) {
			writer.write(jsonStr);
		}
		httpResponse.setStatus(responseStatus);
		httpResponse.setHeader("Content-Type", "application/json");
	}

	/**
	 * Creates {@link EpStructureErrorMessageException}.
	 *
	 * @param messageId    message id
	 * @param debugMessage debug message
	 * @return {@link EpStructureErrorMessageException}
	 */
	public static EpStructureErrorMessageException createStructuredErrorMessageException(final String messageId, final String debugMessage) {
		return new EpStructureErrorMessageException(debugMessage, Collections.singletonList(createStructuredErrorMessage(messageId, debugMessage)));
	}

	/**
	 * Creates {@link EpStructureErrorMessageException}.
	 *
	 * @param messageId    message id
	 * @param debugMessage debug message
	 * @param data         error message data
	 * @return {@link EpStructureErrorMessageException}
	 */
	public static EpStructureErrorMessageException createStructuredErrorMessageException(final String messageId,
																						 final String debugMessage,
																						 final Map<String, String> data) {
		return new EpStructureErrorMessageException(debugMessage,
				Collections.singletonList(createStructuredErrorMessage(messageId, debugMessage, data)));
	}

	/**
	 * Creates {@link StructuredErrorMessage}.
	 *
	 * @param messageId    message id
	 * @param debugMessage debug message
	 * @return {@link StructuredErrorMessage}
	 */
	public static StructuredErrorMessage createStructuredErrorMessage(final String messageId, final String debugMessage) {
		return new StructuredErrorMessage(messageId, debugMessage, new HashMap<>());
	}

	/**
	 * Creates {@link StructuredErrorMessage}.
	 *
	 * @param messageId    message id
	 * @param debugMessage debug message
	 * @param data         error message data
	 * @return {@link StructuredErrorMessage}
	 */
	public static StructuredErrorMessage createStructuredErrorMessage(final String messageId,
																	  final String debugMessage,
																	  final Map<String, String> data) {
		return new StructuredErrorMessage(messageId, debugMessage, data);
	}
}