/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import io.reactivex.Observable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.rest.definition.accounts.AccountBuyerRolesIdentifier;
import com.elasticpath.rest.definition.references.ReferencesIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.ScopePrincipal;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.resource.ResourceOperationContext;

@RunWith(MockitoJUnitRunner.class)
public class AccountBuyerRolesLinksRepositoryImplTest {
	private static final String SCOPE = "mobee";
	@Mock
	private ResourceOperationContext context;

	@InjectMocks
	private AccountBuyerRolesLinksRepositoryImpl repository;

	@Before
	public void setUp() {
		final Subject subject = mock(Subject.class);
		when(context.getSubject()).thenReturn(subject);
		when(subject.getPrincipals()).thenReturn(Collections.singletonList(new ScopePrincipal(SCOPE)));
	}

	@Test
	public void testGetElements() {
		final Observable<AccountBuyerRolesIdentifier> accountIdentifiersObservable = repository.getElements(mock(ReferencesIdentifier.class));

		assertThat(accountIdentifiersObservable.blockingIterable())
				.hasSize(1)
				.hasOnlyOneElementSatisfying(identifier ->
						assertThat(identifier.getAccounts().getScope())
								.isEqualTo(StringIdentifier.of(SCOPE)));
	}
}
