/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.relos.rs.authentication.epcommerce.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.exception.structured.EpStructureErrorMessageException;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.rest.ResourceStatus;
import com.elasticpath.rest.advise.Message;
import com.elasticpath.rest.command.ExecutionResult;
import com.elasticpath.rest.command.ExecutionResultFactory;
import com.elasticpath.rest.identity.RolePrincipal;
import com.elasticpath.rest.util.collection.CollectionUtil;
import com.elasticpath.service.permissions.RoleValidator;

/**
 * Utilities for use in authentication.
 */
public final class AuthenticationUtil {

	private static final Logger LOG = LoggerFactory.getLogger(AuthenticationUtil.class);

	/*
	 * Delimiter char used to join the scope and username. The scope will be first,
	 * username is second.
	 */
	private static final char SCOPE_DELIM = '\u0007';

	private AuthenticationUtil() {
		//static class
	}

	/**
	 * Creates a collection of GrantedAuthorities from a collection of Principals.
	 *
	 * @param principals the collection of Principals
	 * @return a collection of GrantedAuthorities
	 */
	public static Collection<GrantedAuthority> createAuthorities(final Collection<RolePrincipal> principals) {
		final Collection<GrantedAuthority> authorities;
		if (CollectionUtil.isNotEmpty(principals)) {
			authorities = new ArrayList<>(principals.size());
			for (RolePrincipal principal : principals) {
				authorities.add(new SimpleGrantedAuthority(principal.getValue()));
			}
		} else {
			authorities = Collections.emptyList();
		}
		return authorities;
	}

	/**
	 * Split a combined scope and username string into a string array.
	 * array[0] is scope, array[1] is username.
	 *
	 * @param combined the combined scope and username string
	 * @return the parts in an array.
	 */
	public static String[] splitPrincipals(final String combined) {
		String[] parts = StringUtils.split(combined, SCOPE_DELIM);
		assert parts.length == 2 : "combined String of scope and username incorrectly delimited: " + combined;
		return parts;
	}

	/**
	 * Combines a scope and username into a single string.
	 *
	 * @param scope    the scope
	 * @param username the username
	 * @return the combined string
	 */
	public static String combinePrincipals(final String scope, final String username) {
		return scope + SCOPE_DELIM + username;
	}

	/**
	 * Update the httpResponse with the passed HTTP response status and error message.
	 *
	 * @param httpResponse   the HTTP servlet response
	 * @param responseStatus the HTTP response status to set
	 * @param errorMessage   the error message to put in the body of the response
	 * @throws IOException if an exception occurs while setting the response body
	 */
	public static void reportFailure(final HttpServletResponse httpResponse, final int responseStatus, final StructuredErrorMessage errorMessage)
			throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(errorMessage);
		try (PrintWriter writer = httpResponse.getWriter()) {
			writer.write(jsonStr);
		}
		httpResponse.setStatus(responseStatus);
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
	 * @return {@link EpStructureErrorMessageException}
	 */
	public static StructuredErrorMessage createStructuredErrorMessage(final String messageId, final String debugMessage) {
		return new StructuredErrorMessage(messageId, debugMessage, new HashMap<>());
	}

	/**
	 * Decodes Base64 json.
	 *
	 * @param base64EncodedJson Base64 json
	 * @return {@link JsonObject}
	 */
	public static JsonObject readBase64EncodedJson(final String base64EncodedJson) {
		try {
			byte[] jsonBytes = Base64.getDecoder().decode(base64EncodedJson);
			ByteArrayInputStream jsonInputStream = new ByteArrayInputStream(jsonBytes);

			try (JsonReader reader = Json.createReader(jsonInputStream)) {
				return reader.readObject();
			}
		} catch (IllegalArgumentException | JsonException e) {
			LOG.error("Error decoding payload {}", base64EncodedJson, e);
			return null;
		}
	}

	/**
	 * Checks user's scopes.
	 *
	 * @param scopes user's scopes
	 * @return true, if size of the scope is more than one, false otherwise
	 */
	public static ExecutionResult<Customer> isValidScopes(final Collection<String> scopes) {
		if (scopes == null || scopes.isEmpty()) {
			return createFailedExecutionResult("authentication.missing.scopes", "Missing scope headers");
		}

		if (scopes.size() > 1) {
			return createFailedExecutionResult("authentication.too.many.scopes", "Too many scopes in request header");
		}

		return ExecutionResultFactory.createUpdateOK();
	}

	/**
	 * Checks user's role.
	 *
	 * @param roles         user's role
	 * @param roleValidator valid user roles list
	 * @return true, if size of the role is more than one, false otherwise
	 */
	public static ExecutionResult<Customer> isValidRoles(final Collection<String> roles, final RoleValidator roleValidator) {
		if (roles == null || roles.isEmpty()) {
			return createFailedExecutionResult("authentication.missing.header", "Missing role headers");
		}

		if (roles.size() > 1) {
			return createFailedExecutionResult("authentication.too.many.roles", "Too many roles in request header");
		}

		if (roles.stream().findAny().map(role -> !roleValidator.isValidRole(role)).orElse(true)) {
			return createFailedExecutionResult("authentication.wrong.role",
					String.format("Current role is invalid. Valid roles are: %s", roleValidator.getValidUserRoles()
							.stream()
							.collect(Collectors.joining(","))));
		}

		return ExecutionResultFactory.createUpdateOK();
	}

	/**
	 * Creates failed execution result.
	 *
	 * @param messageId    message id
	 * @param debugMessage debug message
	 * @return {@link ExecutionResult<Customer>}
	 */
	public static ExecutionResult<Customer> createFailedExecutionResult(final String messageId, final String debugMessage) {
		return ExecutionResult.<Customer>builder()
				.withStructuredErrorMessages(Collections.singletonList(Message.builder()
						.withId(messageId)
						.withDebugMessage(debugMessage)
						.build()))
				.withResourceStatus(ResourceStatus.BAD_REQUEST_BODY)
				.build();
	}

	/**
	 * Handles failure execution result.
	 *
	 * @param httpResponse                   {@link HttpServletResponse}
	 * @param responseStatus                 the HTTP response status to set
	 * @param failureCustomerExecutionResult {@link ExecutionResult<Customer>}
	 * @throws IOException in case of errors
	 */
	public static void reportFailure(final HttpServletResponse httpResponse, final int responseStatus,
									 final ExecutionResult<Customer> failureCustomerExecutionResult)
			throws IOException {

		final Message errorMessage = getMessageFromExecutionResult(failureCustomerExecutionResult);
		final String messageId = Optional.ofNullable(errorMessage).map(Message::getId).orElse(null);
		final String debugMessage = Optional.ofNullable(errorMessage).map(Message::getDebugMessage).orElse(null);

		reportFailure(httpResponse, responseStatus, createStructuredErrorMessage(messageId, debugMessage));
	}

	private static Message getMessageFromExecutionResult(final ExecutionResult<Customer> user) {
		return user.getStructuredErrorMessages().stream().findFirst().orElse(null);
	}
}
