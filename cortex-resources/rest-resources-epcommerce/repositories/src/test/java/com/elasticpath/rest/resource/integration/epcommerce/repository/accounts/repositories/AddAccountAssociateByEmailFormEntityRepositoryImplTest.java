/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import io.reactivex.Completable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.UserAccountAssociation;
import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.command.ExecutionResult;
import com.elasticpath.rest.command.ExecutionResultFactory;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.AccountsIdentifier;
import com.elasticpath.rest.definition.accounts.AddAssociateFormEntity;
import com.elasticpath.rest.definition.accounts.AddAssociateFormIdentifier;
import com.elasticpath.rest.definition.accounts.AssociatesIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.service.customer.UserAccountAssociationService;

@RunWith(MockitoJUnitRunner.class)
public class AddAccountAssociateByEmailFormEntityRepositoryImplTest {

	private static final String TEST_ERROR_MESSAGE = "Test Error Message";
	private static final String ASSOCIATE_GUID = "associate_guid";
	private static final String ACCOUNT_GUID = "account_guid";
	private static final String ROLE = "associate_role";
	private static final String EMAIL = "associate_email";
	private static final String SCOPE = "SCOPE";

	@Mock
	private AddAssociateFormEntity addAssociateFormEntity;

	@Mock
	private AddAssociateFormIdentifier addAssociateFormIdentifier;

	@Mock
	private Customer customer;

	@Mock
	private UserAccountAssociation userAccountAssociation;

	@Mock
	private AccountAssociateValidator accountAssociateValidator;

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private UserAccountAssociationService userAccountAssociationService;

	@InjectMocks
	private AddAccountAssociateByEmailFormEntityRepositoryImpl addAccountAssociateByEmailFormEntityRepository;

	@Test
	public void submitWithValidationFailure() {
		when(addAssociateFormEntity.getEmail()).thenReturn("");
		when(accountAssociateValidator.validateAddAssociateByEmailFormFilled(any(AddAssociateFormEntity.class)))
				.thenReturn(Completable.error(ResourceOperationFailure.badRequestBody(TEST_ERROR_MESSAGE, Collections.emptyList())));
		ExecutionResult<List<Customer>> customerResult = ExecutionResultFactory.createReadOK(Collections.singletonList(customer));
		when(customerRepository.
				findCustomersByProfileAttributeKeyAndValue(eq(CustomerImpl.ATT_KEY_CP_EMAIL), anyString())).thenReturn(customerResult);
		addAccountAssociateByEmailFormEntityRepository.submit(addAssociateFormEntity, addAssociateFormIdentifier)
				.test()
				.assertError(throwable ->
						TEST_ERROR_MESSAGE.equals(((ResourceOperationFailure) throwable).getMessage()));
		verify(accountAssociateValidator).validateAddAssociateByEmailFormFilled(eq(addAssociateFormEntity));
	}

	@Test
	public void submitExistingUserAccountAssociation() {
		when(addAssociateFormEntity.getEmail()).thenReturn(EMAIL);
		when(userAccountAssociation.getUserGuid()).thenReturn(ASSOCIATE_GUID);
		when(customer.getGuid()).thenReturn(ASSOCIATE_GUID);
		when(accountAssociateValidator.validateAddAssociateByEmailFormFilled(any(AddAssociateFormEntity.class))).thenReturn(Completable.complete());
		when(accountAssociateValidator.validateAddAssociateByEmailFormData(any(), any())).thenReturn(Completable.complete());
		when(userAccountAssociationService.findAssociationForUserAndAccount(anyString(), anyString())).thenReturn(userAccountAssociation);
		ExecutionResult<List<Customer>> customerResult = ExecutionResultFactory.createReadOK(Collections.singletonList(customer));
		when(customerRepository.
				findCustomersByProfileAttributeKeyAndValue(eq(CustomerImpl.ATT_KEY_CP_EMAIL), eq(EMAIL))).thenReturn(customerResult);

		addAccountAssociateByEmailFormEntityRepository.submit(addAssociateFormEntity, getAddAssociateFormIdentifier())
				.test()
				.assertNoErrors();

		verify(accountAssociateValidator).validateAddAssociateByEmailFormFilled(eq(addAssociateFormEntity));
		verify(userAccountAssociationService, never()).associateUserToAccount(eq(ASSOCIATE_GUID), eq(ACCOUNT_GUID), eq(ROLE));
	}

	@Test
	public void submit() {
		when(addAssociateFormEntity.getEmail()).thenReturn(EMAIL);
		when(addAssociateFormEntity.getRole()).thenReturn(ROLE);
		when(userAccountAssociation.getUserGuid()).thenReturn(ASSOCIATE_GUID);
		when(customer.getGuid()).thenReturn(ASSOCIATE_GUID);
		when(accountAssociateValidator.validateAddAssociateByEmailFormFilled(any(AddAssociateFormEntity.class))).thenReturn(Completable.complete());
		when(accountAssociateValidator.validateAddAssociateByEmailFormData(any(), any())).thenReturn(Completable.complete());
		when(userAccountAssociationService.findAssociationForUserAndAccount(anyString(), anyString())).thenReturn(null);
		when(userAccountAssociationService.associateUserToAccount(eq(ASSOCIATE_GUID), eq(ACCOUNT_GUID), eq(ROLE)))
				.thenReturn(userAccountAssociation);
		ExecutionResult<List<Customer>> customerResult = ExecutionResultFactory.createReadOK(Collections.singletonList(customer));
		when(customerRepository.
				findCustomersByProfileAttributeKeyAndValue(eq(CustomerImpl.ATT_KEY_CP_EMAIL), eq(EMAIL))).thenReturn(customerResult);

		addAccountAssociateByEmailFormEntityRepository.submit(addAssociateFormEntity, getAddAssociateFormIdentifier())
				.test()
				.assertNoErrors();

		verify(accountAssociateValidator).validateAddAssociateByEmailFormFilled(eq(addAssociateFormEntity));
		verify(userAccountAssociationService).associateUserToAccount(eq(ASSOCIATE_GUID), eq(ACCOUNT_GUID), eq(ROLE));
	}

	private AddAssociateFormIdentifier getAddAssociateFormIdentifier() {
		AccountsIdentifier accountsIdentifier = AccountsIdentifier.builder()
				.withScope(StringIdentifier.of(SCOPE))
				.build();

		AccountIdentifier accountIdentifier = AccountIdentifier.builder()
				.withAccountId(StringIdentifier.of(ACCOUNT_GUID))
				.withAccounts(accountsIdentifier)
				.build();
		AssociatesIdentifier associatesIdentifier = AssociatesIdentifier.builder()
				.withAccount(accountIdentifier)
				.build();

		return AddAssociateFormIdentifier.builder()
				.withAssociates(associatesIdentifier)
				.build();
	}
}