/**
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.authorization.strategy.impl;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.elasticpath.rest.id.ResourceIdentifier;
import com.elasticpath.rest.id.reader.ResourceIdentifierReader;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.RolePrincipal;
import com.elasticpath.rest.identity.ScopePrincipal;
import com.elasticpath.rest.identity.UserPrincipal;
import com.elasticpath.rest.identity.util.PrincipalsUtil;
import com.elasticpath.rest.resource.authorization.strategy.PrincipalOverrideStrategy;
import com.elasticpath.service.auth.ShiroRolesDeterminationService;

/**
 * Implementation of PrincipalOverrideStrategy for account resources.
 */
public class AccountResourcePrincipalOverrideStrategyImpl implements PrincipalOverrideStrategy {

	/**
	 * Account ID identifier key.
	 */
	public static final String ACCOUNT_ID_KEY = "accounts.account-id";

	private static final String PUBLIC_ROLE = "PUBLIC";

	private static final String OWNER_PERMISSION = "OWNER";

	private ShiroRolesDeterminationService shiroRolesDeterminationService;

	private ResourceIdentifierReader resourceIdentifierReader;

	private List<String> resourceNames;

	@Override
	public boolean shouldOverride(final String resourceName) {
		return resourceNames.contains(resourceName);
	}

	@Override
	public Collection<Principal> override(final String uri, final ResourceIdentifier resourceIdentifier, final Collection<Principal> principals) {
		String scope = PrincipalsUtil.getFirstPrincipalByType(principals, ScopePrincipal.class).getValue();
		UserPrincipal userPrincipal = PrincipalsUtil.getFirstPrincipalByType(principals, UserPrincipal.class);
		String userGuid = userPrincipal.getValue();
		String accountGuid = extractAccountId(resourceIdentifier);

		Set<String> shiroRoles = shiroRolesDeterminationService.determineShiroRoles(scope, isAuthenticated(principals), userGuid, accountGuid);

		return createPrincipals(new ArrayList<>(shiroRoles), userPrincipal, PrincipalsUtil.getFirstPrincipalByType(principals, ScopePrincipal.class));
	}

	private Collection<Principal> createPrincipals(final List<String> permissions, final UserPrincipal userPrincipal,
												   final ScopePrincipal scopePrincipal) {
		List<Principal> principals = new ArrayList<>(PrincipalsUtil.createRolePrincipals(permissions));
		principals.addAll(PrincipalsUtil.createRolePrincipals(Collections.singletonList(OWNER_PERMISSION)));
		principals.add(userPrincipal);
		principals.add(scopePrincipal);

		return principals;
	}

	private String extractAccountId(final ResourceIdentifier identifier) {
		StringIdentifier accountId = resourceIdentifierReader.readIdentifierPart(identifier, ACCOUNT_ID_KEY);
		return accountId.getValue();
	}

	private boolean isAuthenticated(final Collection<Principal> principals) {
		return principals.stream()
				.filter(principal -> RolePrincipal.class.isAssignableFrom(principal.getClass()))
				.map(principal -> ((RolePrincipal) principal).getValue())
				.noneMatch(PUBLIC_ROLE::equalsIgnoreCase);
	}

	protected ResourceIdentifierReader getResourceIdentifierReader() {
		return resourceIdentifierReader;
	}

	public void setResourceIdentifierReader(final ResourceIdentifierReader resourceIdentifierReader) {
		this.resourceIdentifierReader = resourceIdentifierReader;
	}

	protected ShiroRolesDeterminationService getShiroRolesDeterminationService() {
		return shiroRolesDeterminationService;
	}

	public void setShiroRolesDeterminationService(final ShiroRolesDeterminationService shiroRolesDeterminationService) {
		this.shiroRolesDeterminationService = shiroRolesDeterminationService;
	}

	protected List<String> getResourceNames() {
		return resourceNames;
	}

	public void setResourceNames(final List<String> resourceNames) {
		this.resourceNames = resourceNames;
	}
}
