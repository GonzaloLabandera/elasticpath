/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.relos.rs.authentication.epcommerce.filter;

import java.io.IOException;
import java.util.Collections;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

import com.elasticpath.base.exception.structured.EpStructureErrorMessageException;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.rest.command.ExecutionResult;
import com.elasticpath.rest.relos.rs.authentication.epcommerce.util.ErrorUtil;
import com.elasticpath.rest.relos.rs.authentication.request.AddHeadersRequestWrapper;
import com.elasticpath.rest.relos.rs.authentication.request.HeadersRemoverRequestWrapper;
import com.elasticpath.rest.relos.rs.authentication.request.ModifiedHeaderRequestWrapper;
import com.elasticpath.rest.relos.rs.subject.SubjectHeaderConstants;
import com.elasticpath.rest.relos.rs.subject.util.SubjectHeadersUtil;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerIdentifierService;

/**
 * Sets x-ep-user-id header to guid in jwt authorization specific scenarios when x-ep-user-id header might contain sharedId or attribute value.
 */
@Component(property = {
		Constants.SERVICE_RANKING + ":Integer=9",
		HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_REGEX + "=^(?!(\\/(system|healthcheck|oauth2))).*"  }
)
public class ConvertUserIdHeaderFilter implements Filter {
	@Reference
	private CustomerIdentifierService customerIdentifierService;

	@Override
	public void init(final FilterConfig filterConfig) {
		// nothing
	}

	@Override
	public void destroy() {
		// nothing
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		final String issuer = SubjectHeadersUtil.getIssuerFromRequest(httpRequest);

		String userId = SubjectHeadersUtil.getUserIdFromRequest(httpRequest);
		if (StringUtils.isNotBlank(userId)) {
			try {
				HttpServletRequest updatedRequest = updateUserIdHeaderWithCustomerGuid(httpRequest, issuer, userId, CustomerType.REGISTERED_USER);
				chain.doFilter(updatedRequest, response);
			} catch (EpStructureErrorMessageException ex) {
				ErrorUtil.reportFailure(httpResponse, HttpServletResponse.SC_BAD_REQUEST, ex.getStructuredErrorMessages().get(0));
			}
		} else {
			chain.doFilter(request, response);
		}
	}

	/**
	 * For JWT token based authentication with guid not present in x-ep-user-id header, ex: punchout, this method retrieves the guid based on
	 * input params and sets the UserIdentifier in x-ep-user-id header to derived guid. This ensures expectation of x-ep-user-id header to contain
	 * guid in all cases. Invokes CustomerIdentifierStrategyUtil.deriveCustomerGuid to derive guid using customer identifier strategy based on
	 * input parameters.
	 *
	 * @param httpRequest the httpRequest
	 * @param issuer the issuer specified in jwt token
	 * @param userId the user id specified in the jwt token
	 * @param customerType customer type
	 */
	private HttpServletRequest updateUserIdHeaderWithCustomerGuid(final HttpServletRequest httpRequest, final String issuer, final String userId,
			final CustomerType customerType) {
		ExecutionResult<String> deriveGuidExecutionResult = customerIdentifierService.deriveCustomerGuid(userId, customerType, issuer);
		if (deriveGuidExecutionResult.isSuccessful()) {
			Multimap<String, String> headers = ModifiedHeaderRequestWrapper.createHeaderMultimap();
			headers.put(SubjectHeaderConstants.USER_ID, deriveGuidExecutionResult.getData());
			HttpServletRequest updatedHttpRequest =
					new HeadersRemoverRequestWrapper(httpRequest, Collections.singletonList(SubjectHeaderConstants.USER_ID));

			updatedHttpRequest = new AddHeadersRequestWrapper(updatedHttpRequest, headers);
			return updatedHttpRequest;
		} else {
			throw ErrorUtil.createStructuredErrorMessageException(
					"authentication.customer.not.found", "No customer found for the provided user ID" + ".");
		}
	}
}
