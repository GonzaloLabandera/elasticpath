/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.validation.validators.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.validation.constraints.RegisteredCustomerAllOrNoneUsernamePasswordSaltFieldsExist;

/**
 * Validator for a registered customer, their chosen username is unique across the store they are associated with.
 */
public class RegisteredCustomerAllOrNoneUsernamePasswordSaltFieldsExistValidator implements
		ConstraintValidator<RegisteredCustomerAllOrNoneUsernamePasswordSaltFieldsExist, Customer> {

	@Override
	public void initialize(final RegisteredCustomerAllOrNoneUsernamePasswordSaltFieldsExist constraintAnnotation) {
		// do nothing
	}

	/**
	 * Validates that for a registered customer, either all or none of username password, password salt need to be present.
	 */
	@Override
	public boolean isValid(final Customer customer, final ConstraintValidatorContext context) {
		if ((CustomerType.REGISTERED_USER == customer.getCustomerType())
				&& !areAllUsernamePasswordSet(customer) && !areAllUsernamePasswordEmpty(customer)) {

			buildConstraintViolation(context,
					"{com.elasticpath.validation.validators.impl.RegisteredCustomerAllOrNoneUsernamePasswordSaltFieldsExistValidator.message}");

			return false;
		}

		return true;
	}

	private boolean areAllUsernamePasswordSet(final Customer customer) {
		return StringUtils.isNotEmpty(customer.getUsername())
				&& StringUtils.isNotEmpty(customer.getPassword());
	}

	private boolean areAllUsernamePasswordEmpty(final Customer customer) {
		return StringUtils.isEmpty(customer.getUsername())
				&& StringUtils.isEmpty(customer.getPassword());
	}

	/**
	 * Builds the constraint violation.
	 *
	 * @param context the context
	 * @param template the template
	 */
	protected void buildConstraintViolation(final ConstraintValidatorContext context, final String template) {
		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(template)
				.addConstraintViolation();
	}

}
