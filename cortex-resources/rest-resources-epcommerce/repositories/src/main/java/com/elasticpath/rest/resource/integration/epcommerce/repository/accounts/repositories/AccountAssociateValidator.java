/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories;

import java.util.List;

import io.reactivex.Completable;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.rest.command.ExecutionResult;
import com.elasticpath.rest.definition.accounts.AddAssociateFormEntity;

/**
 * Validations for Account Associate.
 */
public interface AccountAssociateValidator {

	/**
	 * Validator for AddAssociateFormEntity.
	 *
	 * @param addAssociateFormEntity addAssociateFormEntity
	 *
	 * @return an error if validation fails, success otherwise
	 */
	Completable validateAddAssociateByEmailFormFilled(AddAssociateFormEntity addAssociateFormEntity);

	/**
	 * Validateo for customer email input in AddAssocaiteFormEntity.
	 *
	 * @param addAssociateFormEntity addAssociateFormEntity
	 * @param customerResult CustomerResult object returned from CustomerRepository method execution
	 *
	 * @return an error if validation fails, success otherwise
	 */
	Completable validateAddAssociateByEmailFormData(AddAssociateFormEntity addAssociateFormEntity, ExecutionResult<List<Customer>> customerResult);

	/**
	 * Validates account associate user role.
	 * @param associateRole account associate role
	 * @param associateId account associate Id
	 * @param currentUserId current user id
	 * @return an error if validation fails, success otherwise
	 */
	Completable validateUserRoleUpdate(String associateRole, String associateId, String currentUserId);
}
