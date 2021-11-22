/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.validation.validators.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.validation.constraints.RegisteredCustomerUsernameUniqueByStore;

/**
 * Validator for a registered customer, their chosen username is unique across the store they are associated with.
 */
public class RegisteredCustomerUsernameUniqueByStoreValidator  implements
		ConstraintValidator<RegisteredCustomerUsernameUniqueByStore, Customer> {

	private CustomerService customerService;

	@Override
	public void initialize(final RegisteredCustomerUsernameUniqueByStore constraintAnnotation) {
		// do nothing
	}

	/**
	 * {@inheritDoc} <br>
	 * Validates that for a registered customer, their chosen username is unique across the store they are associated with.
	 */
	@Override
	public boolean isValid(final Customer customer, final ConstraintValidatorContext context) {
		if ((CustomerType.SINGLE_SESSION_USER != customer.getCustomerType())
				&& StringUtils.isNotEmpty(customer.getUsername())
				&& customerService.isCustomerByUserNameExists(customer)) {

			buildConstraintViolation(context,
					"{com.elasticpath.validation.validators.impl.RegisteredCustomerUsernameUniqueByStoreValidator.message}");

			return false;
		}

		return true;
	}

	/**
	 * Builds the constraint violation.
	 *
	 * @param context the context
	 * @param template the template
	 */
	protected void buildConstraintViolation(final ConstraintValidatorContext context, final String template) {
		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(template).addNode("username").addConstraintViolation();
	}

	public void setCustomerService(final CustomerService customerService) {
		this.customerService = customerService;
	}

}
