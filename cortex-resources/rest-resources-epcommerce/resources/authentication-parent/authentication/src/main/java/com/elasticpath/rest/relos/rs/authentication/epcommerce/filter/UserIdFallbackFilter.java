/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.relos.rs.authentication.epcommerce.filter;

import static com.elasticpath.rest.relos.rs.authentication.epcommerce.util.AuthenticationUtil.createFailedExecutionResult;
import static com.elasticpath.rest.relos.rs.authentication.epcommerce.util.AuthenticationUtil.readBase64EncodedJson;
import static com.elasticpath.rest.relos.rs.authentication.epcommerce.util.AuthenticationUtil.reportFailure;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import javax.json.JsonObject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Multimap;
import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.impl.CustomerRoleMapper;
import com.elasticpath.rest.chain.Assign;
import com.elasticpath.rest.chain.BrokenChainException;
import com.elasticpath.rest.chain.Ensure;
import com.elasticpath.rest.command.ExecutionResult;
import com.elasticpath.rest.relos.rs.authentication.request.AddHeadersRequestWrapper;
import com.elasticpath.rest.relos.rs.authentication.request.ModifiedHeaderRequestWrapper;
import com.elasticpath.rest.relos.rs.subject.SubjectHeaderConstants;
import com.elasticpath.rest.relos.rs.subject.util.SubjectHeadersUtil;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.dto.CustomerDTO;

/**
 * Class is used  to handle the circumstance where the JWT Token “sub” claim is not specified,
 * and instead uses the “user-id” in the metadata to lookup or create a single session user based on shared ID.
 */
@Component(property = {
		Constants.SERVICE_RANKING + ":Integer=5",
		HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_REGEX + "=^(?!(\\/(system|healthcheck|oauth2))).*"}
)
public class UserIdFallbackFilter implements Filter {

	@Reference
	private CustomerRepository customerRepository;

	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
		//nothing to do
	}

	@Override
	public void destroy() {
		//nothing to do
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException,
			ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;

		// Skip processing if user ID has already been determined from OAuth or some other means.
		if (isUserIdHeaderPresent(httpRequest)) {
			chain.doFilter(httpRequest, response);
			return;
		}

		// Skip processing if JWT token was not specified or if it does not contain metadata claim.
		final String encodedMetadata = SubjectHeadersUtil.getUserMetadataFromRequest(httpRequest);
		if (isMetadataEmpty(encodedMetadata)) {
			chain.doFilter(httpRequest, response);
			return;
		}

		HttpServletResponse httpResponse = (HttpServletResponse) response;

		try {
			final Customer user = getFallbackUser(httpRequest, encodedMetadata);
			// Skip code execution for thread that is late.
			if (Objects.isNull(user)) {
				return;
			}

			httpRequest = populateUserIdHeader(user, httpRequest);
			httpRequest = populatePublicUserRole(httpRequest);
		} catch (final BrokenChainException exception) {
			reportFailure(httpResponse, HttpServletResponse.SC_BAD_REQUEST, getFailureExecutionResult(exception));
			return;
		}

		chain.doFilter(httpRequest, response);
	}

	private HttpServletRequest populatePublicUserRole(final HttpServletRequest httpRequest) {
		final Multimap<String, String> headers = ModifiedHeaderRequestWrapper.createHeaderMultimap();
		headers.put(SubjectHeaderConstants.USER_ROLES, CustomerRoleMapper.PUBLIC);

		return new AddHeadersRequestWrapper(httpRequest, headers);
	}

	private ExecutionResult<Customer> getFailureExecutionResult(final BrokenChainException exception) {
		return exception.getBrokenResult();
	}

	private Customer getFallbackUser(final HttpServletRequest httpRequest, final String encodedMetadata) {
		final JsonObject metadata = readBase64EncodedJson(encodedMetadata);
		Ensure.isTrue(Objects.nonNull(metadata), createFailedExecutionResult("authentication.invalid.JWT.metadata",
				"Error decoding payload " + encodedMetadata));

		final String userId = metadata.getString("user-id", null);
		Ensure.isTrue(StringUtils.isNotEmpty(userId), createFailedExecutionResult("JWT.token.missing.sub.and.user-id",
				"JWT token must contain either a sub claim or user-id metadata"));

		final String scope = findUserScope(httpRequest);
		final CustomerDTO customerDTO = createCustomerDTO(metadata, scope);
		final String accountSharedId = SubjectHeadersUtil.getAccountSharedIdFromRequest(httpRequest);

		return Assign.ifSuccessful(customerRepository.findOrCreateUser(customerDTO, scope, userId, accountSharedId));
	}

	private CustomerDTO createCustomerDTO(final JsonObject metadata, final String scope) {
		return new CustomerDTO(metadata, scope);
	}

	private String findUserScope(final HttpServletRequest httpRequest) {
		final Collection<String> scopes = SubjectHeadersUtil.getUserScopesFromRequest(httpRequest);

		return scopes.stream().findFirst().orElse(null);
	}

	private HttpServletRequest populateUserIdHeader(final Customer user, final HttpServletRequest httpRequest) {
		return createRequestWithUserId(httpRequest, user.getGuid());
	}

	private boolean isUserIdHeaderPresent(final HttpServletRequest httpRequest) {
		final String userIdHeader = SubjectHeadersUtil.getUserIdFromRequest(httpRequest);

		return StringUtils.isNotEmpty(userIdHeader);
	}

	private boolean isMetadataEmpty(final String encodedMetadata) {
		return StringUtils.isEmpty(encodedMetadata);
	}

	/**
	 * Creates {@link HttpServletRequest} for given user id.
	 *
	 * @param httpRequest {@link HttpServletRequest}
	 * @param userId      user id
	 * @return {@link HttpServletRequest} with filled user id
	 */
	private HttpServletRequest createRequestWithUserId(final HttpServletRequest httpRequest, final String userId) {
		Multimap<String, String> headers = ModifiedHeaderRequestWrapper.createHeaderMultimap();
		headers.put(SubjectHeaderConstants.USER_ID, userId);

		return new AddHeadersRequestWrapper(httpRequest, headers);
	}
}
