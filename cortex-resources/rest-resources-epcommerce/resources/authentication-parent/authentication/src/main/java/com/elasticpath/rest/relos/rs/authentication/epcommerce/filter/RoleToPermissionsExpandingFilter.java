/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.relos.rs.authentication.epcommerce.filter;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.Multimap;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.rest.relos.rs.authentication.request.AddHeadersRequestWrapper;
import com.elasticpath.rest.relos.rs.authentication.request.ModifiedHeaderRequestWrapper;
import com.elasticpath.rest.relos.rs.subject.SubjectHeaderConstants;
import com.elasticpath.rest.relos.rs.subject.util.SubjectHeadersUtil;
import com.elasticpath.service.auth.ShiroRolesDeterminationService;
import com.elasticpath.service.customer.CustomerService;

/**
 * Extends x-ep-user-roles header.
 */
@Component(property = {
		Constants.SERVICE_RANKING + ":Integer=3",
		HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_REGEX + "=^(?!(\\/(system|healthcheck|oauth2))).*"}
)
public class RoleToPermissionsExpandingFilter implements Filter {

	private static final String OWNER_PERMISSION = "OWNER";

	@Reference
	private ShiroRolesDeterminationService shiroRolesDeterminationService;

	@Reference
	private CustomerService customerService;

	@Override
	public void doFilter(final ServletRequest httpRequest, final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException {

		final Set<String> roles = extractShiroRoles((HttpServletRequest) httpRequest);

		final Multimap<String, String> headers = ModifiedHeaderRequestWrapper.createHeaderMultimap();
		headers.putAll(SubjectHeaderConstants.USER_ROLES, roles);
		headers.put(SubjectHeaderConstants.USER_ROLES, OWNER_PERMISSION);

		chain.doFilter(new AddHeadersRequestWrapper((HttpServletRequest) httpRequest, headers), response);
	}

	/**
	 * Extracts Shiro roles based on parameters in {@link HttpServletRequest}.
	 *
	 * @param httpRequest the httpRequest
	 * @return set of Shiro roles
	 */
	private Set<String> extractShiroRoles(final HttpServletRequest httpRequest) {
		Collection<String> scopes = SubjectHeadersUtil.getUserScopesFromRequest(httpRequest);
		String scope = scopes.stream().findFirst().orElseThrow(() -> new EpSystemException("Expected scope header."));
		boolean isAuthenticated = SubjectHeadersUtil.isAuthenticated(httpRequest);
		String userGuid = SubjectHeadersUtil.getUserIdFromRequest(httpRequest);
		String accountSharedId = SubjectHeadersUtil.getAccountSharedIdFromRequest(httpRequest);
		String accountGuid = null;
		if (accountSharedId != null) {
			accountGuid = customerService.findCustomerGuidBySharedId(accountSharedId);
		}

		return shiroRolesDeterminationService.determineShiroRoles(scope, isAuthenticated, userGuid, accountGuid);
	}

	@Override
	public void init(final FilterConfig filterConfig) {
		//nothing to do
	}

	@Override
	public void destroy() {
		//nothing to do
	}
}
