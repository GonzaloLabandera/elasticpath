/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.auth.impl;

import java.util.Collections;
import java.util.Set;

import com.elasticpath.domain.customer.UserAccountAssociation;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.auth.ShiroRolesDeterminationService;
import com.elasticpath.service.customer.AccountTreeService;
import com.elasticpath.service.customer.UserAccountAssociationService;
import com.elasticpath.service.permissions.RoleToPermissionsMappingService;
import com.elasticpath.service.store.StoreService;

/**
 * Service for determining which Shiro roles a user should have.
 */
public class ShiroRolesDeterminationServiceImpl implements ShiroRolesDeterminationService {
	private StoreService storeService;
	private UserAccountAssociationService userAccountAssociationService;
	private AccountTreeService accountTreeService;
	private RoleToPermissionsMappingService roleToPermissionsMappingService;

	@Override
	public Set<String> determineShiroRoles(final String scope, final boolean isAuthenticated, final String userGuid, final String accountGuid) {
		String elasticPathRole = determineElasticPathRole(scope, isAuthenticated, userGuid, accountGuid);
		if (elasticPathRole == null) {
			return Collections.emptySet();
		}
		return roleToPermissionsMappingService.getPermissionsForRole(elasticPathRole);
	}

	/**
	 * Determine the Elastic Path role for the passed user and account.
	 *
	 * @param scope the current scope
	 * @param isAuthenticated if the signed-in user is authenticated (not single session)
	 * @param userGuid the signed-in user's GUID
	 * @param accountGuid the account GUID that the user is transacting on behalf of (or null)
	 * @return the Elastic Path role
	 */
	protected String determineElasticPathRole(final String scope, final boolean isAuthenticated, final String userGuid, final String accountGuid) {
		if (accountGuid == null) {
			Store store = storeService.findStoreWithCode(scope);
			if (isAuthenticated) {
				return store.getB2CAuthenticatedRole();
			} else {
				return store.getB2CSingleSessionRole();
			}
		}
		UserAccountAssociation accountAssociation = findUserAccountAssociation(userGuid, accountGuid);
		if (accountAssociation == null) {
			return null;
		}
		return accountAssociation.getAccountRole();
	}

	/**
	 * Find the user account association for the user and account, if it exists. Looks up the account hierarcy
	 * to find an association to a parent account if none exists for the passed-in account.
	 * @param userGuid The user guid.
	 * @param accountGuid The account guid.
	 * @return The UserAccountAssociation if one exists, or null.
	 */
	protected UserAccountAssociation findUserAccountAssociation(final String userGuid, final String accountGuid) {
		UserAccountAssociation userAccountAssociation = userAccountAssociationService.findAssociationForUserAndAccount(userGuid, accountGuid);

		if (userAccountAssociation == null) {
			return accountTreeService.fetchParentAccountGuidByChildGuid(accountGuid)
					.map(guid -> findUserAccountAssociation(userGuid, guid))
					.orElse(null);
		}

		return userAccountAssociation;
	}

	protected StoreService getStoreService() {
		return storeService;
	}

	public void setStoreService(final StoreService storeService) {
		this.storeService = storeService;
	}

	protected UserAccountAssociationService getUserAccountAssociationService() {
		return userAccountAssociationService;
	}

	public void setUserAccountAssociationService(final UserAccountAssociationService userAccountAssociationService) {
		this.userAccountAssociationService = userAccountAssociationService;
	}

	protected RoleToPermissionsMappingService getRoleToPermissionsMappingService() {
		return roleToPermissionsMappingService;
	}

	public void setRoleToPermissionsMappingService(final RoleToPermissionsMappingService roleToPermissionsMappingService) {
		this.roleToPermissionsMappingService = roleToPermissionsMappingService;
	}

	protected AccountTreeService getAccountTreeService() {
		return accountTreeService;
	}

	public void setAccountTreeService(final AccountTreeService accountTreeService) {
		this.accountTreeService = accountTreeService;
	}
}
