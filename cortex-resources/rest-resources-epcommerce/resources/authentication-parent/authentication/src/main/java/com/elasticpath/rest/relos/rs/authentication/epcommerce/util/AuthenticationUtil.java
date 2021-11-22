/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.relos.rs.authentication.epcommerce.util;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.elasticpath.base.exception.structured.EpStructureErrorMessageException;
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
	 * @throws EpStructureErrorMessageException if size of the scopes is more than one or if scopes is empty
	 */
	public static void isValidScopes(final Collection<String> scopes) throws EpStructureErrorMessageException {
		if (scopes == null || scopes.isEmpty()) {
			throw ErrorUtil.createStructuredErrorMessageException("authentication.missing.scopes", "Missing scope headers");
		}

		if (scopes.size() > 1) {
			throw ErrorUtil.createStructuredErrorMessageException("authentication.too.many.scopes", "Too many scopes in request header");
		}
	}

	/**
	 * Checks user's role.
	 *
	 * @param roles         user's role
	 * @param roleValidator valid user roles list
	 * @throws EpStructureErrorMessageException if size of the roles is more than one or roles is empty or is invalid as per roleValidator
	 */
	public static void isValidRoles(final Collection<String> roles, final RoleValidator roleValidator) throws EpStructureErrorMessageException {
		if (roles == null || roles.isEmpty()) {
			throw ErrorUtil.createStructuredErrorMessageException("authentication.missing.header", "Missing role headers");
		}

		if (roles.size() > 1) {
			throw  ErrorUtil.createStructuredErrorMessageException("authentication.too.many.roles", "Too many roles in request header");
		}

		if (roles.stream().findAny().map(role -> !roleValidator.isValidRole(role)).orElse(true)) {
			throw ErrorUtil.createStructuredErrorMessageException("authentication.wrong.role",
					String.format("Current role is invalid. Valid roles are: %s", String.join(",", roleValidator.getValidUserRoles())));
		}
	}
}
