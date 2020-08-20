/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.validation.service;

import java.util.Set;
import javax.validation.ConstraintViolation;

import com.elasticpath.domain.customer.Customer;

/**
 * Service for validation of {@link Customer} fields.
 */
public interface CustomerConstraintValidationService {

	/**
	 * Performs general-purpose constraint validation on the customer.
	 *
	 * @param customer {@link Customer} object to check.
	 * @return set of constraint violations, if any.
	 * @throws IllegalArgumentException if customer is null.
	 */
	Set<ConstraintViolation<Customer>> validate(Customer customer);

	/**
	 * Performs constraint validation on the customer required for registration.
	 *
	 * @param customer {@link Customer} object to check.
	 * @return set of constraint violations, if any.
	 */
	Set<ConstraintViolation<Customer>> validateUserRegistrationConstraints(Customer customer);
}
