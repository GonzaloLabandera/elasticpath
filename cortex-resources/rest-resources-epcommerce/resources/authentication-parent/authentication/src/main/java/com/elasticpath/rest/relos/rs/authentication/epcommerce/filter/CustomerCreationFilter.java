/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.relos.rs.authentication.epcommerce.filter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.Collection;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.rest.command.ExecutionResult;
import com.elasticpath.rest.command.ExecutionResultFactory;
import com.elasticpath.rest.relos.rs.subject.util.SubjectHeadersUtil;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.toggles.FeatureToggles;

/**
 * Create a new customer for the user identified by subject headers if not already exists.
 */
@Component(property = {
		Constants.SERVICE_RANKING + ":Integer=6",
		HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_PATTERN + "=/*" }
)
public class CustomerCreationFilter implements Filter {

	private static final Logger LOG = LoggerFactory.getLogger(CustomerCreationFilter.class);

	private final boolean trustedSubjectHeaderModeEnabled =
			FeatureToggles.getFeatureToggle(FeatureToggles.Toggle.TRUSTED_SUBJECT_HEADER_MODE);

	@Reference
	private CustomerRepository customerRepository;

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException {

		if (trustedSubjectHeaderModeEnabled) {
			chain.doFilter(request, response);
			return;
		}

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		Collection<String> scopes = SubjectHeadersUtil.getUserScopesFromRequest(httpRequest);
		if (scopes.size() > 1) {
			httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			try (PrintWriter writer = httpResponse.getWriter()) {
				writer.write("Too many scopes");
			}
			return;
		}

		String userId = SubjectHeadersUtil.getUserIdFromRequest(httpRequest);
		String storeCode = scopes.stream().findFirst().orElse(null);

		if (userId != null && storeCode != null) {
			String metadata = SubjectHeadersUtil.getUserMetadataFromRequest(httpRequest);
			ExecutionResult<Void> result = ensureCustomerCreated(userId, storeCode, metadata);
			if (!result.isSuccessful()) {
				LOG.error("Error creating customer with userId '{}' and storeCode '{}'", userId, storeCode);
				httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
		}

		chain.doFilter(request, response);
	}

	private ExecutionResult<Void> ensureCustomerCreated(
			final String userId, final String storeCode, final String metadata) {

		ExecutionResult<Void> customerExistsResult = customerRepository.isCustomerGuidExists(userId);
		if (customerExistsResult.isSuccessful()) {
			return customerExistsResult;
		}

		Customer newCustomer = buildNewCustomer(userId, storeCode, metadata);
		ExecutionResult<Customer> customerAddResult = customerRepository.addUnauthenticatedUser(newCustomer);
		if (customerAddResult.isSuccessful()) {
			return ExecutionResultFactory.createCreateOKWithData(null, false);
		}

		return customerRepository.isCustomerGuidExists(userId);
	}

	private Customer buildNewCustomer(final String userId, final String storeCode, final String metadata) {
		Customer newCustomer = customerRepository.createNewCustomerEntity();

		// profiles resource looks up customer by guid using cortex subject id
		newCustomer.setUserId(userId);
		newCustomer.setGuid(userId);

		newCustomer.setStoreCode(storeCode);
		newCustomer.setAnonymous(false);

		if (metadata != null) {
			JsonObject metadataObject = readBase64EncodedJson(metadata);
			if (metadataObject != null) {
				newCustomer.setFirstName(safeGetString(metadataObject, "first-name"));
				newCustomer.setLastName(safeGetString(metadataObject, "last-name"));
			}
		}

		return newCustomer;
	}

	private JsonObject readBase64EncodedJson(final String base64EncodedJson) {
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

	private String safeGetString(final JsonObject jsonObject, final String key) {
		if (jsonObject.containsKey(key)) {
			try {
				return jsonObject.getString(key);
			} catch (ClassCastException e) {
				LOG.error("The value for key '{}' is not a string", key, e);
				return null;
			}
		}
		return null;
	}

	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
		// nothing
	}

	@Override
	public void destroy() {
		// nothing
	}
}
