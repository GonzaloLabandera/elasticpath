/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import io.reactivex.observers.TestObserver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.rest.definition.accounts.AccountEntity;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.profiles.ProfileIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.identity.attribute.SubjectAttribute;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.service.customer.CustomerService;

@RunWith(MockitoJUnitRunner.class)
public class SelectedAccountFromProfileLinksRepositoryImplTest {

	private static final String ACCOUNT_SHARED_ID = "accountSharedId";
	private static final String ACCOUNT_GUID = "accountGuid";
	private static final String SCOPE = "testScope";

	@Mock
	private ResourceOperationContext resourceOperationContext;
	@Mock
	private CustomerService customerService;
	@Mock
	private ProfileIdentifier profileIdentifier;
	@Mock
	private Subject subject;
	@Mock
	private SubjectAttribute subjectAttribute;
	@InjectMocks
	private SelectedAccountFromProfileLinksRepositoryImpl<AccountEntity, AccountIdentifier> repository;

	@Before
	public void setUp() {
		when(profileIdentifier.getScope()).thenReturn(StringIdentifier.of(SCOPE));
		when(resourceOperationContext.getSubject()).thenReturn(subject);
		when(resourceOperationContext.getSubject().getAttributes()).thenReturn(Collections.singletonList(subjectAttribute));
		when(subjectAttribute.getType()).thenReturn("ACCOUNT_SHARED_ID");
		when(subjectAttribute.getValue()).thenReturn(ACCOUNT_SHARED_ID);
		when(customerService.findCustomerGuidBySharedId(ACCOUNT_SHARED_ID, CustomerType.ACCOUNT)).thenReturn(ACCOUNT_GUID);
	}

	@Test
	public void testThatGetElementsReturnsAccountIdentifier() {
		final TestObserver<AccountIdentifier> testObserver = repository.getElements(profileIdentifier).test();
		testObserver.assertComplete();
		testObserver.assertNoErrors();
		verify(customerService).findCustomerGuidBySharedId(ACCOUNT_SHARED_ID, CustomerType.ACCOUNT);
	}
}
