package com.elasticpath.rest.resource.authorization.strategy.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.rest.id.ResourceIdentifier;
import com.elasticpath.rest.id.reader.ResourceIdentifierReader;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.RolePrincipal;
import com.elasticpath.rest.identity.ScopePrincipal;
import com.elasticpath.rest.identity.UserPrincipal;
import com.elasticpath.rest.identity.util.PrincipalsUtil;
import com.elasticpath.service.auth.ShiroRolesDeterminationService;

@RunWith(MockitoJUnitRunner.class)
public class AccountResourcePrincipalOverrideStrategyImplTest {

	private static final String ACCOUNT_GUID = "accountId";
	private static final String USER_GUID = "userId";
	private static final String URI = "/some/where";
	private static final String SCOPE = "scope";

	@Mock
	private ShiroRolesDeterminationService shiroRolesDeterminationService;

	@Mock
	private ResourceIdentifierReader resourceIdentifierReader;

	@Mock
	private ResourceIdentifier resourceIdentifier;

	@InjectMocks
	private AccountResourcePrincipalOverrideStrategyImpl strategyUnderTest;

	@Before
	public void setUp() {
		StringIdentifier identifier = StringIdentifier.of(ACCOUNT_GUID);
		when(resourceIdentifierReader.readIdentifierPart(resourceIdentifier, AccountResourcePrincipalOverrideStrategyImpl.ACCOUNT_ID_KEY))
				.thenReturn(identifier);
	}

	private Collection<Principal> createTestPrincipals() {
		List<Principal> principals = new ArrayList<>(PrincipalsUtil.createRolePrincipals(Arrays.asList("testRoleOne", "testRoleTwo")));
		principals.add(new ScopePrincipal(SCOPE));
		principals.add(new UserPrincipal(USER_GUID));
		return principals;
	}

	private Collection<Principal> createExpectedPrincipals() {
		List<Principal> principals = new ArrayList<>(PrincipalsUtil.createRolePrincipals(Arrays.asList("testRoleOneExpected")));
		principals.add(new RolePrincipal("OWNER"));
		principals.add(new ScopePrincipal(SCOPE));
		principals.add(new UserPrincipal(USER_GUID));
		return principals;
	}

	@Test
	public void testOverridingPrincipals() {
		Set<String> expectedPermissions = Collections.singleton("testRoleOneExpected");

		when(shiroRolesDeterminationService.determineShiroRoles(SCOPE, true, USER_GUID, ACCOUNT_GUID)).thenReturn(expectedPermissions);

		Collection<Principal> expectedPrincipals = createExpectedPrincipals();
		Collection<Principal> actualPrincipals = strategyUnderTest.override(URI, resourceIdentifier, createTestPrincipals());

		assertThat(actualPrincipals).containsExactlyInAnyOrderElementsOf(expectedPrincipals);
	}
}
