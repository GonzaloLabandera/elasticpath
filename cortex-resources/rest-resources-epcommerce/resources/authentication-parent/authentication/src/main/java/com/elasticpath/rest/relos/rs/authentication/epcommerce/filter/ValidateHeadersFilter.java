/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.relos.rs.authentication.epcommerce.filter;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

import com.elasticpath.base.exception.structured.EpStructureErrorMessageException;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.UserAccountAssociation;
import com.elasticpath.rest.command.ExecutionResult;
import com.elasticpath.rest.relos.rs.authentication.epcommerce.util.AuthenticationUtil;
import com.elasticpath.rest.relos.rs.authentication.epcommerce.util.ErrorUtil;
import com.elasticpath.rest.relos.rs.subject.util.SubjectHeadersUtil;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.store.StoreRepository;
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

	@Reference
	private StoreRepository storeRepository;

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
		try {
			AuthenticationUtil.isValidRoles(roles, roleValidator);
		} catch (EpStructureErrorMessageException exception) {
			ErrorUtil.reportFailure(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, exception.getStructuredErrorMessages().get(0));
			return;
		}

		final Collection<String> scopes = SubjectHeadersUtil.getUserScopesFromRequest(httpRequest);
		try {
			AuthenticationUtil.isValidScopes(scopes);
		} catch (EpStructureErrorMessageException exception) {
			ErrorUtil.reportFailure(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, exception.getStructuredErrorMessages().get(0));
			return;
		}

		try {
			final String scope = scopes.stream().findFirst().orElse(null);
			isValidAssociatedStoreStatus(scope);
		} catch (EpStructureErrorMessageException exception) {
			ErrorUtil.reportFailure(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, exception.getStructuredErrorMessages().get(0));
			return;
		}

		String accountSharedId = SubjectHeadersUtil.getAccountSharedIdFromRequest(httpRequest);
		if (StringUtils.isNotBlank(accountSharedId)) {
			try {
				validateAccountSharedId(httpRequest, accountSharedId);
			} catch (EpStructureErrorMessageException epStructureErrorMessageException) {
				ErrorUtil.reportFailure(httpResponse, HttpServletResponse.SC_UNAUTHORIZED,
						epStructureErrorMessageException.getStructuredErrorMessages().get(0));
				return;
			}
		}

		chain.doFilter(request, response);
	}

	private void isValidAssociatedStoreStatus(final String scope) {
		Map<String, String> data = new HashMap<>();
		data.put("store-code", scope);

		if (!storeRepository.isStoreCodeEnabled(scope).blockingGet()) {
			throw ErrorUtil.createStructuredErrorMessageException(
					"store.disabled", "The selected store is disabled or not in an OPEN state.", data);
		}
	}

	private void validateAccountSharedId(final HttpServletRequest httpRequest, final String accountSharedId) {
		ExecutionResult<Customer> accountResult = customerRepository.findCustomerBySharedId(null, accountSharedId);
		if (accountResult.isFailure()) {
			throw ErrorUtil.createStructuredErrorMessageException(
					"authentication.account.not.found", "No account found for the provided shared ID.");
		}

		if (Customer.STATUS_DISABLED == accountResult.getData().getStatus()) {
			throw ErrorUtil.createStructuredErrorMessageException("authentication.account.disabled", "The selected account is disabled.");
		}

		String customerGuid = SubjectHeadersUtil.getUserIdFromRequest(httpRequest);
		UserAccountAssociation associationResult = userAccountAssociationService.findAssociationForUserAndAccount(customerGuid,
				accountResult.getData().getGuid());

		if (associationResult == null) {
			throw ErrorUtil.createStructuredErrorMessageException(
					"authentication.account.not.associated", "You are not authorized to shop on behalf of the selected account.");
		}
	}
}
