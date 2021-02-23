/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.UserAccountAssociation;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.AccountsIdentifier;
import com.elasticpath.rest.definition.accounts.AssociateEntity;
import com.elasticpath.rest.definition.accounts.AssociateIdentifier;
import com.elasticpath.rest.definition.accounts.AssociatesIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories.impl.AccountAssociateValidatorImpl;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;
import com.elasticpath.service.customer.UserAccountAssociationService;

@RunWith(MockitoJUnitRunner.class)
public class AccountAssociateEntityRepositoryImplTest {

	private static final String ASSOCIATE_GUID = "associate_guid";
	private static final String ACCOUNT_GUID = "account_guid";
	private static final String TEST_USER_GUID = "TEST_USER_GUID";
	private static final String ROLE = "associate_role";
	private static final IdentifierPart<String> SCOPE = StringIdentifier.of("scope");
	public static final String CANNOT_DELETE_THEIR_OWN_ASSOCIATION = "User cannot delete their own association.";

	@Mock
	private AssociateEntity associateEntity;

	@Mock
	private UserAccountAssociation userAccountAssociation;

	@Mock
	private UserAccountAssociationService userAccountAssociationService;

	@Mock
	private AccountAssociateValidatorImpl accountAssociateValidator;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private ResourceOperationContext resourceOperationContext;

	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapterImpl;

	@InjectMocks
	private AccountAssociateEntityRepositoryImpl accountAssociateEntityRepository;

	@Before
	public void setUp() {
		when(userAccountAssociationService.findAssociationForUserAndAccount(ASSOCIATE_GUID, ACCOUNT_GUID)).thenReturn(userAccountAssociation);
		when(userAccountAssociationService.findAssociationsForAccount(ACCOUNT_GUID)).thenReturn(Collections.singletonList(userAccountAssociation));
		when(userAccountAssociation.getUserGuid()).thenReturn(ASSOCIATE_GUID);
		when(userAccountAssociation.getAccountRole()).thenReturn(ROLE);
		when(resourceOperationContext.getResourceIdentifier()).thenReturn(Optional.of(getAccountIdentifier()));
		when(associateEntity.getRole()).thenReturn(ROLE);
		accountAssociateEntityRepository.setReactiveAdapter(reactiveAdapterImpl);
	}

	@Test
	public void findOne() {
		AssociateIdentifier associateIdentifier = getAssociateIdentifier();

		Single<AssociateEntity> associateEntitySingle = accountAssociateEntityRepository.findOne(associateIdentifier);

		assertEquals(ROLE, associateEntitySingle.blockingGet().getRole());
	}

	@Test
	public void findAll() {
		Observable<AssociateIdentifier> associateIdentifierObservable = accountAssociateEntityRepository.findAll(SCOPE);
		assertThat(associateIdentifierObservable.blockingIterable())
				.hasSize(1)
				.hasOnlyOneElementSatisfying(identifier ->
						assertThat(identifier.getAssociateId())
								.isEqualTo(StringIdentifier.of(ASSOCIATE_GUID)));
	}

	@Test
	public void deleteIncorrectUser() {
		when(resourceOperationContext.getUserIdentifier()).thenReturn(ASSOCIATE_GUID);

		accountAssociateEntityRepository.delete(getAssociateIdentifier())
				.test()
				.assertError(ResourceOperationFailure.badRequestBody(CANNOT_DELETE_THEIR_OWN_ASSOCIATION));
	}

	@Test
	public void delete() {
		when(resourceOperationContext.getUserIdentifier()).thenReturn(TEST_USER_GUID);
		doNothing().when(userAccountAssociationService).remove(anyString(), anyString());
		accountAssociateEntityRepository.delete(getAssociateIdentifier())
				.test()
				.assertNoErrors();

		verify(userAccountAssociationService).remove(eq(ASSOCIATE_GUID), eq(ACCOUNT_GUID));
	}

	@Test
	public void updateWithErrors() {
		when(resourceOperationContext.getUserIdentifier()).thenReturn(TEST_USER_GUID);
		when(accountAssociateValidator.validateUserRoleUpdate(anyString(), anyString(), anyString()))
				.thenReturn(Completable.error(ResourceOperationFailure.badRequestBody()));

		accountAssociateEntityRepository.update(associateEntity, getAssociateIdentifier())
				.test()
				.assertError(ResourceOperationFailure.badRequestBody());

		verify(accountAssociateValidator).validateUserRoleUpdate(eq(ROLE), eq(ASSOCIATE_GUID), eq(TEST_USER_GUID));
		verifyNoMoreInteractions(userAccountAssociationService);
	}

	@Test
	public void update() {
		when(resourceOperationContext.getUserIdentifier()).thenReturn(TEST_USER_GUID);
		when(accountAssociateValidator.validateUserRoleUpdate(anyString(), anyString(), anyString())).thenReturn(Completable.complete());
		when(userAccountAssociationService.update(any(), any(), any())).thenReturn(userAccountAssociation);

		accountAssociateEntityRepository.update(associateEntity, getAssociateIdentifier())
				.test()
				.assertNoErrors();

		verify(accountAssociateValidator).validateUserRoleUpdate(eq(ROLE), eq(ASSOCIATE_GUID), eq(TEST_USER_GUID));
		verify(userAccountAssociationService).update(eq(ASSOCIATE_GUID), eq(ACCOUNT_GUID), eq(ROLE));
	}

	private AssociateIdentifier getAssociateIdentifier() {
		AccountIdentifier accountIdentifier = getAccountIdentifier();
		AssociatesIdentifier associatesIdentifier = AssociatesIdentifier.builder()
				.withAccount(accountIdentifier).build();
		return AssociateIdentifier.builder()
				.withAssociateId(StringIdentifier.of(ASSOCIATE_GUID))
				.withAssociates(associatesIdentifier)
				.build();
	}

	private AccountIdentifier getAccountIdentifier() {
		AccountsIdentifier accountsIdentifier = AccountsIdentifier.builder()
				.withScope(SCOPE)
				.build();
		AccountIdentifier accountIdentifier = AccountIdentifier.builder()
				.withAccountId(StringIdentifier.of(ACCOUNT_GUID))
				.withAccounts(accountsIdentifier)
				.build();
		return accountIdentifier;
	}
}