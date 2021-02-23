/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.validator.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.drools.core.util.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.exception.structured.EpValidationException;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.ResourceStatus;
import com.elasticpath.rest.command.ExecutionResult;
import com.elasticpath.rest.definition.accounts.AddAssociateFormEntity;
import com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories.impl.AccountAssociateValidatorImpl;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ExceptionTransformer;
import com.elasticpath.service.permissions.RoleToPermissionsMappingService;

/**
 * Contains tests for {@link AccountAssociateValidatorImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AccountAssociateValidatorImplTest {

	private static final String DUMMY_ERROR_STRING = "Error occurred";
	private static final String EMAIL_STRING = "test@elasticpath.com";
	private static final String ROLE_STRING = "BUYER_ADMIN";

	@Mock
	private AddAssociateFormEntity addAssociateFormEntity;

	@Mock
	private ExceptionTransformer exceptionTransformer;

	@Mock
	private RoleToPermissionsMappingService roleToPermissionsMappingService;

	@Mock
	private Customer customer;

	@InjectMocks
	private AccountAssociateValidatorImpl validator;

	@Captor
	private ArgumentCaptor<EpValidationException> epValidationExceptionArgumentCaptor;

	@Test
	public void testValidateAddAssociateByEmailForm() {
		when(addAssociateFormEntity.getEmail()).thenReturn(EMAIL_STRING);
		when(addAssociateFormEntity.getRole()).thenReturn(ROLE_STRING);
		validator.validateAddAssociateByEmailFormFilled(addAssociateFormEntity)
				.test()
				.assertNoErrors();

		// Email not specified
		when(addAssociateFormEntity.getEmail()).thenReturn(StringUtils.EMPTY);
		when(addAssociateFormEntity.getRole()).thenReturn(ROLE_STRING);
		when(exceptionTransformer.getResourceOperationFailure(epValidationExceptionArgumentCaptor.capture()))
				.thenReturn(ResourceOperationFailure.badRequestBody(DUMMY_ERROR_STRING, Collections.emptyList()));
		validator.validateAddAssociateByEmailFormFilled(addAssociateFormEntity)
				.test()
				.assertError(throwable -> DUMMY_ERROR_STRING.equals(throwable.getMessage()));
		assertEquals(1, epValidationExceptionArgumentCaptor.getValue().getStructuredErrorMessages().size());

		// Role not specified
		when(addAssociateFormEntity.getEmail()).thenReturn(EMAIL_STRING);
		when(addAssociateFormEntity.getRole()).thenReturn(StringUtils.EMPTY);
		validator.validateAddAssociateByEmailFormFilled(addAssociateFormEntity)
				.test()
				.assertError(throwable -> DUMMY_ERROR_STRING.equals(throwable.getMessage()));
		assertEquals(1, epValidationExceptionArgumentCaptor.getValue().getStructuredErrorMessages().size());

		// Email and Role not specified
		when(addAssociateFormEntity.getEmail()).thenReturn(StringUtils.EMPTY);
		when(addAssociateFormEntity.getRole()).thenReturn(StringUtils.EMPTY);
		validator.validateAddAssociateByEmailFormFilled(addAssociateFormEntity)
				.test()
				.assertError(throwable -> DUMMY_ERROR_STRING.equals(throwable.getMessage()));
		assertEquals(2, epValidationExceptionArgumentCaptor.getValue().getStructuredErrorMessages().size());
	}

	@Test
	public void testValidateAddAssociateByEmailFormData() {
		when(addAssociateFormEntity.getEmail()).thenReturn(EMAIL_STRING);
		when(addAssociateFormEntity.getRole()).thenReturn(ROLE_STRING);
		when(exceptionTransformer.getResourceOperationFailure(epValidationExceptionArgumentCaptor.capture()))
				.thenReturn(ResourceOperationFailure.badRequestBody(DUMMY_ERROR_STRING, Collections.emptyList()));

		// Success case
		ExecutionResult<List<Customer>> customerResult = getExecutionResult(ResourceStatus.READ_OK, Collections.singletonList(customer));
		when(roleToPermissionsMappingService.getDefinedRoleKeys()).thenReturn(Collections.singleton(ROLE_STRING));

		validator.validateAddAssociateByEmailFormData(addAssociateFormEntity, customerResult)
				.test()
				.assertNoErrors();
		
		// Customer execution result failure
		customerResult = getExecutionResult(ResourceStatus.BAD_REQUEST_BODY, Collections.singletonList(customer));
		when(roleToPermissionsMappingService.getDefinedRoleKeys()).thenReturn(Collections.singleton(ROLE_STRING));
		
		validator.validateAddAssociateByEmailFormData(addAssociateFormEntity, customerResult)
				.test()
				.assertError(throwable -> DUMMY_ERROR_STRING.equals(throwable.getMessage()));
		assertEquals(1, epValidationExceptionArgumentCaptor.getValue().getStructuredErrorMessages().size());

		// Customer execution result no matching customer
		customerResult = getExecutionResult(ResourceStatus.READ_OK, Collections.emptyList());
		when(roleToPermissionsMappingService.getDefinedRoleKeys()).thenReturn(Collections.singleton(ROLE_STRING));

		validator.validateAddAssociateByEmailFormData(addAssociateFormEntity, customerResult)
				.test()
				.assertError(throwable -> DUMMY_ERROR_STRING.equals(throwable.getMessage()));
		assertEquals(1, epValidationExceptionArgumentCaptor.getValue().getStructuredErrorMessages().size());

		// Customer execution result multiple matching customers
		customerResult = getExecutionResult(ResourceStatus.READ_OK, Collections.nCopies(2, customer));
		when(roleToPermissionsMappingService.getDefinedRoleKeys()).thenReturn(Collections.singleton(ROLE_STRING));

		validator.validateAddAssociateByEmailFormData(addAssociateFormEntity, customerResult)
				.test()
				.assertError(throwable -> DUMMY_ERROR_STRING.equals(throwable.getMessage()));
		assertEquals(1, epValidationExceptionArgumentCaptor.getValue().getStructuredErrorMessages().size());

		// Customer execution result failure and incorrect role
		customerResult = getExecutionResult(ResourceStatus.BAD_REQUEST_BODY, Collections.singletonList(customer));
		when(roleToPermissionsMappingService.getDefinedRoleKeys()).thenReturn(Collections.emptySet());

		validator.validateAddAssociateByEmailFormData(addAssociateFormEntity, customerResult)
				.test()
				.assertError(throwable -> DUMMY_ERROR_STRING.equals(throwable.getMessage()));
		assertEquals(2, epValidationExceptionArgumentCaptor.getValue().getStructuredErrorMessages().size());

		// Customer execution result success and incorrect role
		customerResult = getExecutionResult(ResourceStatus.READ_OK, Collections.singletonList(customer));
		when(roleToPermissionsMappingService.getDefinedRoleKeys()).thenReturn(Collections.emptySet());

		validator.validateAddAssociateByEmailFormData(addAssociateFormEntity, customerResult)
				.test()
				.assertError(throwable -> DUMMY_ERROR_STRING.equals(throwable.getMessage()));
		assertEquals(1, epValidationExceptionArgumentCaptor.getValue().getStructuredErrorMessages().size());
	}

	private ExecutionResult<List<Customer>> getExecutionResult(final ResourceStatus resourceStatus, final List<Customer> customerList) {
		ExecutionResult<List<Customer>> customerResult =
				ExecutionResult.<List<Customer>>builder()
						.withResourceStatus(resourceStatus)
						.withData(customerList)
						.build();
		return customerResult;
	}

}