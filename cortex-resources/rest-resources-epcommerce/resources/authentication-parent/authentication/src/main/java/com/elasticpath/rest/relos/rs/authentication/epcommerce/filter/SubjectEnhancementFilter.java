/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.relos.rs.authentication.epcommerce.filter;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.exception.structured.EpStructureErrorMessageException;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.UserAccountAssociation;
import com.elasticpath.rest.command.ExecutionResult;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.identity.UserPrincipal;
import com.elasticpath.rest.identity.attribute.AccountSharedIdSubjectAttribute;
import com.elasticpath.rest.identity.attribute.SubjectAttribute;
import com.elasticpath.rest.identity.type.ImmutableSubject;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.relos.rs.authentication.epcommerce.util.AuthenticationUtil;
import com.elasticpath.rest.relos.rs.subject.SubjectStorage;
import com.elasticpath.rest.relos.rs.subject.util.SubjectHeadersUtil;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerIdentifierService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.service.customer.UserAccountAssociationService;

/**
 * Enhances the subject with metadata attributes from the header.
 * Replaces the subject stored in the subjectStorage TL.
 *
 * This is a bit of a hack, as it should really be done in cortex/api-platform.
 * However, we don't want b2b business case logic leaking into api-platform, so this will do for now.
 *
 * Also, this needs to be done AFTER the subject has been placed into subject storage,
 * which currently is done by a filter with service ranking of 2. Therefore this has a service ranking of 1 to follow that.
 *
 */
@Component(property = {
		Constants.SERVICE_RANKING + ":Integer=1",
		HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_REGEX + "=^(?!(\\/(system|healthcheck|oauth2))).*"  }
)
public class SubjectEnhancementFilter implements Filter {

	private static final String ACCOUNT_ID_ATTR_KEY = "account-shared-id";

	@Reference
	private SubjectStorage subjectStorage;

	@Reference
	private CustomerIdentifierService customerIdentifierService;

	@Reference
	private UserAccountAssociationService userAccountAssociationService;

	@Reference
	private CustomerRepository customerRepository;

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

		Collection<String> scopes = SubjectHeadersUtil.getUserScopesFromRequest(httpRequest);
		if (scopes.size() > 1) {
			AuthenticationUtil.reportFailure(httpResponse, HttpServletResponse.SC_BAD_REQUEST,
					createStructuredErrorMessage("authentication.too.many.scopes", "Too many scopes in request header"));
			return;
		}
		String scope = scopes.stream().findFirst().orElse(null);
		String userId = SubjectHeadersUtil.getUserIdFromRequest(httpRequest);
		if (scope == null || userId == null) {
			AuthenticationUtil.reportFailure(httpResponse, HttpServletResponse.SC_BAD_REQUEST,
					createStructuredErrorMessage("authentication.missing.headers", "Missing user id and/or scope headers"));
		}

		String issuer = SubjectHeadersUtil.getIssuerFromRequest(httpRequest);
		String accountSharedId = SubjectHeadersUtil.getAccountSharedIdFromRequest(httpRequest);

		try {
			updateSubjectWithCustomerGuid(subjectStorage.getSubject(), issuer, userId, scope);
			if (StringUtils.isNotEmpty(accountSharedId)) {
				updateSubjectWithAccountSharedId(subjectStorage.getSubject(), accountSharedId);
			}
			chain.doFilter(request, response);
		} catch (EpStructureErrorMessageException ex) {
			AuthenticationUtil.reportFailure(httpResponse, HttpServletResponse.SC_BAD_REQUEST, ex.getStructuredErrorMessages().get(0));
		}
	}

	private void updateSubjectStorage(final Collection<Principal> principals, final Collection<SubjectAttribute> attributes) {
		Subject wrappedSubject = new ImmutableSubject(principals, attributes);
		subjectStorage.storeSubject(wrappedSubject);
	}

	/**
	 * For JWT token based authentication with guid not present in Subject.getUserIdentifier(), ex: punchout, this method retrieves the guid based on
	 * input params and sets the UserIdentifier in Subject to derived guid. This ensures expectation of Subject.getUserIdentifier() call to return
	 * guid in all cases is met. Invokes CustomerIdentifierStrategyUtil.deriveCustomerGuid to derive guid using customer identifier strategy based on
	 * input parameters.
	 *
	 * @param subject the subject
	 * @param issuer the issuer specified in jwt token
	 * @param userId the user id specified in the jwt token
	 * @param storeCode store code
	 */
	private void updateSubjectWithCustomerGuid(final Subject subject, final String issuer, final String userId, final String storeCode) {
		ExecutionResult<String> deriveGuidExecutionResult = customerIdentifierService.deriveCustomerGuid(userId, storeCode, issuer);
		if (deriveGuidExecutionResult.isSuccessful()) {
			Collection<Principal> principals = updateUserPrincipalWithGuid(subject, deriveGuidExecutionResult.getData());
			updateSubjectStorage(principals, subject.getAttributes());
		} else {
			throw createStructuredErrorMessageException("authentication.customer.not.found", "No customer found for the provided user ID.");
		}
	}

	private void updateSubjectWithAccountSharedId(final Subject subject, final String accountSharedId) {
		ExecutionResult<Customer> accountResult = customerRepository.findCustomerBySharedId(null, accountSharedId);
		if (accountResult.isFailure()) {
			throw createStructuredErrorMessageException("authentication.account.not.found", "No account found for the provided shared ID.");
		}

		if (Customer.STATUS_DISABLED == accountResult.getData().getStatus()) {
			throw createStructuredErrorMessageException("authentication.account.disabled", "The selected account is disabled.");
		}

		String customerGuid = SubjectUtil.getUserPrincipal(subject).getValue();
		UserAccountAssociation associationResult = userAccountAssociationService.findAssociationForUserAndAccount(customerGuid,
				accountResult.getData().getGuid());
		if (associationResult == null) {
			throw createStructuredErrorMessageException("authentication.account.not.associated", "You are not authorized to shop on behalf of the "
					+ "selected account.");
		}

		Collection<SubjectAttribute> attributes = new ArrayList<>(subject.getAttributes());
		attributes.add(new AccountSharedIdSubjectAttribute(ACCOUNT_ID_ATTR_KEY, accountSharedId));

		updateSubjectStorage(subject.getPrincipals(), attributes);
	}

	private EpStructureErrorMessageException createStructuredErrorMessageException(final String messageId, final String debugMessage) {
		return new EpStructureErrorMessageException(debugMessage, Collections.singletonList(createStructuredErrorMessage(messageId, debugMessage)));
	}

	private StructuredErrorMessage createStructuredErrorMessage(final String messageId, final String debugMessage) {
		return new StructuredErrorMessage(messageId, debugMessage, new HashMap<>());
	}

	private Collection<Principal> updateUserPrincipalWithGuid(final Subject subject, final String guid) {
		UserPrincipal oldUserPrincipal = SubjectUtil.getUserPrincipal(subject);
		UserPrincipal newUserPrincipal = new UserPrincipal(guid);
		HashSet<Principal> principles = new HashSet<>(subject.getPrincipals());
		principles.remove(oldUserPrincipal);
		principles.add(newUserPrincipal);

		return Collections.unmodifiableCollection(principles);
	}
}
