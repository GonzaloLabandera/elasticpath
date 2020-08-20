/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.validation.service.impl;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.validation.groups.AccountValidation;
import com.elasticpath.validation.groups.PasswordCheck;
import com.elasticpath.validation.groups.UserValidation;
import com.elasticpath.validation.groups.UsernameUniqueCheck;
import com.elasticpath.validation.service.CustomerConstraintValidationService;

/**
 * Service for validation of {@link Customer} fields.
 */
public class CustomerConstraintValidationServiceImpl implements CustomerConstraintValidationService {

	private Validator validator;

	@Override
	public Set<ConstraintViolation<Customer>> validateUserRegistrationConstraints(final Customer customer) {
		return validator.validate(customer, PasswordCheck.class, UsernameUniqueCheck.class);
	}

	@Override
	public Set<ConstraintViolation<Customer>> validate(final Customer customer) {
		Objects.requireNonNull(customer, "Customer cannot be null.");
		final Set<ConstraintViolation<Customer>> customerViolations;

		if (customer.isAnonymous()) {
			customerViolations = validateAnonymousCustomer(customer);
		} else if (customer.getCustomerType().equals(CustomerType.ACCOUNT)) {
			customerViolations = validateAccount(customer);
		} else {
			customerViolations = validateRegisteredUser(customer);
		}
		return customerViolations;
	}

	private Set<ConstraintViolation<Customer>> validateAnonymousCustomer(final Customer customer) {
		return Stream.concat(
				validator.validateProperty(customer, "username", Customer.class).stream(),
				validator.validateProperty(customer, "email", Customer.class).stream()).collect(Collectors.toSet());
	}

	private Set<ConstraintViolation<Customer>> validateAccount(final Customer customer) {
		return validator.validate(customer, AccountValidation.class);
	}

	private Set<ConstraintViolation<Customer>> validateRegisteredUser(final Customer customer) {
		return validator.validate(customer, UserValidation.class);
	}

	public void setValidator(final Validator validator) {
		this.validator = validator;
	}

	protected Validator getValidator() {
		return validator;
	}
}
