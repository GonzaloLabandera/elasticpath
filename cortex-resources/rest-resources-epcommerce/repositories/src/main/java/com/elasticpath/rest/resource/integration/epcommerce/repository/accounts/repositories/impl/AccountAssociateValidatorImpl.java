/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.reactivex.Completable;
import org.apache.commons.lang.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.common.dto.StructuredErrorMessageType;
import com.elasticpath.base.exception.structured.EpValidationException;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.rest.command.ExecutionResult;
import com.elasticpath.rest.definition.accounts.AddAssociateFormEntity;
import com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories.AccountAssociateValidator;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ExceptionTransformer;
import com.elasticpath.service.permissions.RoleToPermissionsMappingService;

/**
 * Implementation of {@link AccountAssociateValidator}.
 */
@Component
public class AccountAssociateValidatorImpl implements AccountAssociateValidator {

	private static final Logger LOG = LoggerFactory.getLogger(AccountAssociateValidatorImpl.class);
	private static final String EMAIL = "email";

	private ExceptionTransformer exceptionTransformer;

	private RoleToPermissionsMappingService roleToPermissionMappingService;

	@Override
	public Completable validateAddAssociateByEmailFormFilled(final AddAssociateFormEntity addAssociateFormEntity) {
		List<StructuredErrorMessage> structuredErrorMessageList = new ArrayList<>();
		if (StringUtils.isBlank(addAssociateFormEntity.getEmail())) {
			structuredErrorMessageList.add(prepareStructuredError("email must not be blank", "field.required", "field-name", EMAIL));
		}

		if (StringUtils.isBlank(addAssociateFormEntity.getRole())) {
			structuredErrorMessageList.add(prepareStructuredError("role must not be blank", "field.required", "field-name", "role"));
		}

		if (structuredErrorMessageList.isEmpty()) {
			return Completable.complete();
		}

		return getCompletableError(structuredErrorMessageList, "Account Associate email form validation failure");
	}

	private Completable getCompletableError(final List<StructuredErrorMessage> structuredErrorMessageList, final String errorStr) {
		return Completable.error(exceptionTransformer.getResourceOperationFailure(
				new EpValidationException(errorStr, structuredErrorMessageList)));
	}

	@Override
	public Completable validateAddAssociateByEmailFormData(
			final AddAssociateFormEntity addAssociateFormEntity, final ExecutionResult<List<Customer>> customerResult) {

		List<StructuredErrorMessage> structuredErrorMessageList = new ArrayList<>();
		if (customerResult.isFailure() || customerResult.getData().isEmpty()) {
			structuredErrorMessageList.add(prepareStructuredError("The user with the specified email does not exist.", "user.not.found", EMAIL,
					addAssociateFormEntity.getEmail()));
		} else if (customerResult.getData().size() > 1) {
			structuredErrorMessageList.add(prepareStructuredError("There is more than one user with the given email.", "email.conflict", EMAIL,
					addAssociateFormEntity.getEmail()));
		}

		Optional<StructuredErrorMessage> associateRoleValidationError = validateAssociateRole(addAssociateFormEntity.getRole());
		if (associateRoleValidationError.isPresent()) {
			structuredErrorMessageList.add(associateRoleValidationError.get());
		}

		if (structuredErrorMessageList.isEmpty()) {
			return Completable.complete();
		}

		return getCompletableError(structuredErrorMessageList, "Account Associate email form customer validation failure");
	}

	@Override
	public Completable validateUserRoleUpdate(final String associateRole, final String associateId, final String currentUser) {
		List<StructuredErrorMessage> structuredErrorMessageList = new ArrayList<>();
		if (associateId.equals(currentUser)) {
			structuredErrorMessageList.add(new StructuredErrorMessage(
					StructuredErrorMessageType.ERROR,
					"",
					"User cannot update their own role",
					Collections.emptyMap()));

		}

		Optional<StructuredErrorMessage> associateRoleValidationError = validateAssociateRole(associateRole);
		if (associateRoleValidationError.isPresent()) {
			structuredErrorMessageList.add(associateRoleValidationError.get());
		}

		if (structuredErrorMessageList.isEmpty()) {
			return Completable.complete();
		}

		return getCompletableError(structuredErrorMessageList, "Account associate user role update validation failure");
	}

	private Optional<StructuredErrorMessage> validateAssociateRole(final String associateRole) {
		if (!roleToPermissionMappingService.getDefinedRoleKeys().contains(associateRole)) {
			return Optional.of(
					prepareStructuredError("The specified role is not configured in the system", "role.not.found",	"role",
							associateRole));
		}
		return Optional.empty();
	}

	private StructuredErrorMessage prepareStructuredError(final String debugMsg, final String errorMsg, final String dataKey,
			final String dataValue) {
		LOG.error(debugMsg);
		return new StructuredErrorMessage(
						StructuredErrorMessageType.ERROR,
						errorMsg,
						debugMsg,
						Collections.singletonMap(dataKey, dataValue));
	}

	@Reference
	public void setExceptionTransformer(final ExceptionTransformer exceptionTransformer) {
		this.exceptionTransformer = exceptionTransformer;
	}

	@Reference
	public void setRoleToPermissionMappingService(final RoleToPermissionsMappingService roleToPermissionMappingService) {
		this.roleToPermissionMappingService = roleToPermissionMappingService;
	}
}
