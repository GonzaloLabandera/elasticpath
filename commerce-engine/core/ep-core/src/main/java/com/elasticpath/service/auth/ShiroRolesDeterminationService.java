/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.auth;

import java.util.Set;

/**
 * Service for determining which Shiro roles a user should have.
 */
public interface ShiroRolesDeterminationService {
	/**
	 * Determine the applicable Shiro roles for the passed user and account.
	 *
	 * @param scope the current scope
	 * @param isAuthenticated if the signed-in user is authenticated (not single session)
	 * @param userGuid the signed-in user's GUID
	 * @param accountGuid the account GUID that the user is transacting on behalf of (or null)
	 * @return the set of applicable Shiro roles
	 */
	Set<String> determineShiroRoles(String scope, boolean isAuthenticated, String userGuid, String accountGuid);
}
