/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.validation.service;

import com.elasticpath.validation.domain.ValidationConstraint;
import com.elasticpath.validation.domain.ValidationResult;

/**
 * Validation engine that performs validation on a declarative constraint.
 */
public interface ValidationEngine {

	/**
	 * Validate a value using declarative constraints provided.
	 * @param valueToValidate the value to check
	 * @param constraint the constraint to be validated
	 * @return validation result object that represents the outcome of validation
	 */
	ValidationResult validate(Object valueToValidate,
			ValidationConstraint constraint);

}
