/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;

import io.reactivex.Observable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.UserAccountAssociation;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.AssociateIdentifier;
import com.elasticpath.rest.definition.accounts.AssociatesIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.service.customer.UserAccountAssociationService;

@RunWith(MockitoJUnitRunner.class)
public class AccountAssociateListRepositoryImplTest {

	private static final String ACCOUNT_GUID = "ACCOUNT_GUID";

	private static final String ASSOCIATE_GUID = "ASSOCIATE_GUID";

	@Mock
	private AssociatesIdentifier associatesIdentifier;

	@Mock
	private AccountIdentifier accountIdentifier;

	@Mock
	private UserAccountAssociation userAccountAssociation;

	@Mock
	private UserAccountAssociationService userAccountAssociationService;

	@InjectMocks
	private AccountAssociateListRepositoryImpl accountAssociateListRepository;

	@Test
	public void getElements() {
		when(associatesIdentifier.getAccount()).thenReturn(accountIdentifier);
		when(accountIdentifier.getAccountId()).thenReturn(StringIdentifier.of(ACCOUNT_GUID));
		when(userAccountAssociation.getUserGuid()).thenReturn(ASSOCIATE_GUID);
		//when(userAccountAssociation.getAccountGuid()).thenReturn(ACCOUNT_GUID);
		when(userAccountAssociationService.findAssociationsForAccount(ACCOUNT_GUID)).thenReturn(Collections.singletonList(userAccountAssociation));

		Observable<AssociateIdentifier> associateIdentifierObservable = accountAssociateListRepository.getElements(associatesIdentifier);
		assertThat(associateIdentifierObservable.blockingIterable())
				.hasSize(1)
				.hasOnlyOneElementSatisfying(identifier ->
						assertThat(identifier.getAssociateId().getValue()).isEqualTo(ASSOCIATE_GUID));
	}
}