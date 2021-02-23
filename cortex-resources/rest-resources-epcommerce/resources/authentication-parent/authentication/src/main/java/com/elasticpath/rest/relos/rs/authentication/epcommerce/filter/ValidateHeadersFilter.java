/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.relos.rs.authentication.epcommerce.filter;

import static com.elasticpath.rest.relos.rs.authentication.epcommerce.util.AuthenticationUtil.createFailedExecutionResult;
import static com.elasticpath.rest.relos.rs.authentication.epcommerce.util.AuthenticationUtil.isValidRoles;
import static com.elasticpath.rest.relos.rs.authentication.epcommerce.util.AuthenticationUtil.isValidScopes;
import static com.elasticpath.rest.relos.rs.authentication.epcommerce.util.AuthenticationUtil.reportFailure;

import java.io.IOException;
import java.util.Collection;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.UserAccountAssociation;
import com.elasticpath.rest.command.ExecutionResult;
import com.elasticpath.rest.relos.rs.subject.util.SubjectHeadersUtil;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.service.customer.UserAccountAssociationService;
import com.elasticpath.service.permissions.RoleValidator;

/**
 * Filter for sanity check.
 */
@Component(property = {
		Constants.SERVICE_RANKING + ":Integer=4",
		HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_REGEX + "=^(?!(\\/(system|healthcheck|oauth2))).*"}
)
public class ValidateHeadersFilter implements Filter {

	@Reference
	private CustomerRepository customerRepository;

	@Reference
	private UserAccountAssociationService userAccountAssociationService;

	@Reference
	private RoleValidator roleValidator;

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
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		final Collection<String> roles = SubjectHeadersUtil.getUserRolesFromRequest(httpRequest);
		ExecutionResult<Customer> validCustomerRoles = isValidRoles(roles, roleValidator);
		if (validCustomerRoles.isFailure()) {
			reportFailure(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, validCustomerRoles);
			return;
		}

		final Collection<String> scopes = SubjectHeadersUtil.getUserScopesFromRequest(httpRequest);
		ExecutionResult<Customer> validCustomerScopes = isValidScopes(scopes);
		if (validCustomerScopes.isFailure()) {
			reportFailure(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, validCustomerScopes);
			return;
		}

		String accountSharedId = SubjectHeadersUtil.getAccountSharedIdFromRequest(httpRequest);
		if (StringUtils.isNotBlank(accountSharedId)) {
			ExecutionResult<Customer> accountValidation = validateAccountSharedId(httpRequest, accountSharedId);
			if (accountValidation.isFailure()) {
				reportFailure(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, accountValidation);
				return;
			}
		}

		chain.doFilter(request, response);
	}

	private ExecutionResult<Customer> validateAccountSharedId(final HttpServletRequest httpRequest, final String accountSharedId) {
		ExecutionResult<Customer> accountResult = customerRepository.findCustomerBySharedId(null, accountSharedId);
		if (accountResult.isFailure()) {
			return createFailedExecutionResult("authentication.account.not.found", "No account found for the provided shared ID.");
		}

		if (Customer.STATUS_DISABLED == accountResult.getData().getStatus()) {
			return createFailedExecutionResult("authentication.account.disabled", "The selected account is disabled.");
		}

		String customerGuid = SubjectHeadersUtil.getUserIdFromRequest(httpRequest);
		UserAccountAssociation associationResult = userAccountAssociationService.findAssociationForUserAndAccount(customerGuid,
				accountResult.getData().getGuid());

		if (associationResult == null) {
			return createFailedExecutionResult("authentication.account.not.associated", "You are not authorized to shop on behalf of the "
					+ "selected account.");
		}

		return accountResult;
	}
}
