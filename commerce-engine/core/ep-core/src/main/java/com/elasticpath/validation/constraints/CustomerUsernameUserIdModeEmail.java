/**
 * Copyright (c) Elastic Path Software Inc., 2012
 */
package com.elasticpath.validation.constraints;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import com.elasticpath.validation.validators.impl.CustomerUsernameUserIdModeEmailValidator;

/**
 * A constraint that should check that usernames are validating correctly.
 */
@Target(ElementType.TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = CustomerUsernameUserIdModeEmailValidator.class)
@Documented
public @interface CustomerUsernameUserIdModeEmail {
	/**
	 * Constraint violation message.
	 *
	 * @return String message, default CustomerUsernameUserIdModeEmail.message
	 */
	String message() default "{com.elasticpath.validation.constraints.CustomerUsernameUserIdModeEmail.message}";

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
