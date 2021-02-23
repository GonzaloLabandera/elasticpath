package com.elasticpath.service.auth.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.UserAccountAssociation;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.customer.AccountTreeService;
import com.elasticpath.service.customer.UserAccountAssociationService;
import com.elasticpath.service.permissions.RoleToPermissionsMappingService;
import com.elasticpath.service.store.StoreService;

@RunWith(MockitoJUnitRunner.class)
public class ShiroRolesDeterminationServiceImplTest {
	private static final String SCOPE = "scope";
	private static final String USER_GUID = "userGuid";
	private static final String ACCOUNT_GUID = "accountGuid";
	private static final String PARENT_ACCOUNT_GUID = "parentAccountGuid";
	private static final String EP_ROLE_B2C_SINGLE_SESSION = "b2cSingleSessionRole";
	private static final Set<String> SHIRO_ROLES_B2C_SINGLE_SESSION = Collections.singleton("b2cSingleSessionShiroRole");
	private static final String EP_ROLE_B2C_AUTHENTICATED = "b2cAuthenticatedRole";
	private static final Set<String> SHIRO_ROLES_B2C_AUTHENTICATED = Collections.singleton("b2cAuthenticatedShiroRole");
	private static final String EP_ROLE_ACCOUNT_ASSOCIATION = "accountAssociationRole";
	private static final Set<String> SHIRO_ROLES_ACCOUNT_ASSOCIATION = Collections.singleton("accountAssociationShiroRole");

	@Mock
	private StoreService storeService;

	@Mock
	private UserAccountAssociationService userAccountAssociationService;

	@Mock
	private RoleToPermissionsMappingService roleToPermissionsMappingService;

	@Mock
	private AccountTreeService accountTreeService;

	@InjectMocks
	private ShiroRolesDeterminationServiceImpl shiroRolesDeterminationService;

	@Before
	public void setup() {
		Store store = mock(Store.class);
		when(store.getB2CSingleSessionRole()).thenReturn(EP_ROLE_B2C_SINGLE_SESSION);
		when(store.getB2CAuthenticatedRole()).thenReturn(EP_ROLE_B2C_AUTHENTICATED);
		when(storeService.findStoreWithCode(SCOPE)).thenReturn(store);


		when(roleToPermissionsMappingService.getPermissionsForRole(EP_ROLE_B2C_SINGLE_SESSION)).thenReturn(SHIRO_ROLES_B2C_SINGLE_SESSION);
		when(roleToPermissionsMappingService.getPermissionsForRole(EP_ROLE_B2C_AUTHENTICATED)).thenReturn(SHIRO_ROLES_B2C_AUTHENTICATED);
		when(roleToPermissionsMappingService.getPermissionsForRole(EP_ROLE_ACCOUNT_ASSOCIATION)).thenReturn(SHIRO_ROLES_ACCOUNT_ASSOCIATION);
	}

	private void setupAssociatedAccount() {
		UserAccountAssociation userAccountAssociation = mock(UserAccountAssociation.class);
		when(userAccountAssociation.getAccountRole()).thenReturn(EP_ROLE_ACCOUNT_ASSOCIATION);
		when(userAccountAssociationService.findAssociationForUserAndAccount(USER_GUID, ACCOUNT_GUID)).thenReturn(userAccountAssociation);
	}

	private void setupUnassociatedParentAccount() {
		when(accountTreeService.fetchParentAccountGuidByChildGuid(ACCOUNT_GUID)).thenReturn(Optional.of(PARENT_ACCOUNT_GUID));
		when(userAccountAssociationService.findAssociationForUserAndAccount(USER_GUID, ACCOUNT_GUID)).thenReturn(null);
		when(userAccountAssociationService.findAssociationForUserAndAccount(USER_GUID, PARENT_ACCOUNT_GUID)).thenReturn(null);
	}

	private void setupAssociatedParentAccount() {
		UserAccountAssociation userAccountAssociation = mock(UserAccountAssociation.class);

		when(accountTreeService.fetchParentAccountGuidByChildGuid(ACCOUNT_GUID)).thenReturn(Optional.of(PARENT_ACCOUNT_GUID));
		when(userAccountAssociation.getAccountRole()).thenReturn(EP_ROLE_ACCOUNT_ASSOCIATION);
		when(userAccountAssociationService.findAssociationForUserAndAccount(USER_GUID, ACCOUNT_GUID)).thenReturn(null);
		when(userAccountAssociationService.findAssociationForUserAndAccount(USER_GUID, PARENT_ACCOUNT_GUID)).thenReturn(userAccountAssociation);
	}

	@Test
	public void determineShiroRolesForSingleSessionUserWithoutAccountSharedId() {
		Set<String> shiroRoles = shiroRolesDeterminationService.determineShiroRoles(SCOPE, false, USER_GUID, null);

		assertThat(shiroRoles).containsExactlyInAnyOrderElementsOf(SHIRO_ROLES_B2C_SINGLE_SESSION);
	}

	@Test
	public void determineShiroRolesForAuthenticatedUserWithoutAccountSharedId() {
		Set<String> shiroRoles = shiroRolesDeterminationService.determineShiroRoles(SCOPE, true, USER_GUID, null);

		assertThat(shiroRoles).containsExactlyInAnyOrderElementsOf(SHIRO_ROLES_B2C_AUTHENTICATED);
	}

	@Test
	public void determineShiroRolesForAssociatedAccountSharedId() {
		setupAssociatedAccount();
		Set<String> shiroRoles = shiroRolesDeterminationService.determineShiroRoles(SCOPE, true, USER_GUID, ACCOUNT_GUID);

		assertThat(shiroRoles).containsExactlyInAnyOrderElementsOf(SHIRO_ROLES_ACCOUNT_ASSOCIATION);
	}

	@Test
	public void determineShiroRolesForAssociatedParentAccountSharedId() {
		setupAssociatedParentAccount();

		Set<String> shiroRoles = shiroRolesDeterminationService.determineShiroRoles(SCOPE, true, USER_GUID, ACCOUNT_GUID);

		assertThat(shiroRoles).containsExactlyInAnyOrderElementsOf(SHIRO_ROLES_ACCOUNT_ASSOCIATION);
	}

	@Test
	public void determineShiroRolesForUnassociatedParentAccountSharedId() {
		setupUnassociatedParentAccount();

		Set<String> shiroRoles = shiroRolesDeterminationService.determineShiroRoles(SCOPE, true, USER_GUID, ACCOUNT_GUID);

		assertThat(shiroRoles).isEmpty();
	}

	@Test
	public void determineShiroRolesForInvalidAccountSharedId() {
		when(userAccountAssociationService.findAssociationForUserAndAccount(USER_GUID, ACCOUNT_GUID)).thenReturn(null);
		Set<String> shiroRoles = shiroRolesDeterminationService.determineShiroRoles(SCOPE, true, USER_GUID, ACCOUNT_GUID);
		assertThat(shiroRoles).isEmpty();
	}
}