/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.validation.constraints;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import com.elasticpath.validation.validators.impl.RegisteredCustomerAllOrNoneUsernamePasswordSaltFieldsExistValidator;

/**
 * A constraint that should check that for a registered customer, either all or none of the username, password and salt are populated.
 */
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = RegisteredCustomerAllOrNoneUsernamePasswordSaltFieldsExistValidator.class)
@Documented
public @interface RegisteredCustomerAllOrNoneUsernamePasswordSaltFieldsExist {
	/**
	 * Constraint violation message.
	 *
	 * @return String message, default RegisteredCustomerUsernameUniqueByStoreValidator.message
	 */
	String message() default "{com.elasticpath.validation.validators.impl.RegisteredCustomerUsernameUniqueByStoreValidator.message}";

	/**
	 * Groups associated to this constraint.
	 *
	 * @return Class[] groups, default empty
	 */
	Class<?>[] groups() default { };

	/**
	 * Payload for the constraint.
	 *
	 * @return Class[] payload, default empty
	 */
	Class<? extends Payload>[] payload() default { };
}
