/*
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.validation.validators.util;

import java.lang.annotation.ElementType;
import java.util.Collections;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationProviderResolver;
import javax.validation.bootstrap.ProviderSpecificBootstrap;
import javax.validation.spi.ValidationProvider;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.context.PropertyConstraintMappingContext;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;

import com.elasticpath.domain.cartmodifier.CartItemModifierField;
import com.elasticpath.validation.defs.AbstractConstraintDef;
import com.elasticpath.validation.defs.LengthDef;
import com.elasticpath.validation.defs.NotBlankDef;

/**
 * Validator used for validation of dynamic fields, like {@link CartItemModifierField}.
 * It uses Hibernate implementation because of the ability to add constraints programmatically.
 */
public class DynamicCartItemModifierFieldValidator {

	private static final ProviderSpecificBootstrap<HibernateValidatorConfiguration> HIBERNATE_BOOTSTRAP =
			Validation.byProvider(HibernateValidator.class);

	private static final String VALIDATION_MESSAGES_PREFIX = "ValidationMessages";

	private final HibernateValidatorConfiguration configuration;
	private final ConstraintMapping constraintMapping;
	private final PropertyConstraintMappingContext constraintMappingContext;
	private final boolean isRequiredSuppression;

	/**
	 * Default constructor.
	 * Initializes required Hibernate structures.
	 *
	 * @param isRequiredSuppression flag indicates whether required constraint should be applied or not.
	 */
	public DynamicCartItemModifierFieldValidator(final boolean isRequiredSuppression) {
		this.configuration = HIBERNATE_BOOTSTRAP
				.providerResolver(createValidationProviderResolver(new HibernateValidator()))
				.configure()
				.messageInterpolator(
						new ResourceBundleMessageInterpolator(
								new PlatformResourceBundleLocator(VALIDATION_MESSAGES_PREFIX,
										this.getClass().getClassLoader())
						)
				);
		this.constraintMapping = configuration.createConstraintMapping();

		//tell validator what type and field need to be validated
		this.constraintMappingContext = constraintMapping
				.type(DynamicCartItemModifierField.class)
				.property("valueToValidate", ElementType.FIELD);
		this.isRequiredSuppression = isRequiredSuppression;
	}

	/**
	 * Validate dynamic field.
	 *
	 * @param fieldToValidate a field to validate.
	 * @return a set of {@link ConstraintViolation} instances or empty set.
	 */
	public Set<ConstraintViolation<DynamicCartItemModifierField>> validate(final DynamicCartItemModifierField fieldToValidate) {

		addIsRequiredConstraint(fieldToValidate);

		Set<ConstraintViolation<DynamicCartItemModifierField>> violations = validateWithCurrentConstraints(fieldToValidate);
		if (!violations.isEmpty()) {
			return violations;
		}

		addAdditionalConstraints(fieldToValidate);

		return validateWithCurrentConstraints(fieldToValidate);
	}

	private void addIsRequiredConstraint(final DynamicCartItemModifierField fieldToValidate) {
		final String fieldName = fieldToValidate.getFieldName();
		final CartItemModifierField referentField = fieldToValidate.getReferentField();

		if (referentField.isRequired() && !isRequiredSuppression) {
			constraintMappingContext.constraint(
					new NotBlankDef()
							.fieldName(fieldName));
		}
	}

	private void addAdditionalConstraints(final DynamicCartItemModifierField fieldToValidate) {
		final String fieldName = fieldToValidate.getFieldName();
		final CartItemModifierField referentField = fieldToValidate.getReferentField();
		final AbstractConstraintDef<?>[] typeConstraintDefs =
				referentField.getFieldType().getConstraintDefs().orElse(new AbstractConstraintDef<?>[]{});

		if (referentField.getMaxSize() != null) {
			constraintMappingContext.constraint(
					new LengthDef()
							.max(referentField.getMaxSize())
							.fieldName(fieldName));
		}

		// Additional constraints based on field type. Each field may contain one or more constraint definitions
		final String[] validFieldOptions = fieldToValidate.getValidOptions().orElse(new String[]{});
		for (AbstractConstraintDef<?> constraintDef : typeConstraintDefs) {
			constraintDef.validFieldOptions(validFieldOptions);
			constraintDef.fieldName(fieldName);
			constraintMappingContext.constraint(constraintDef);
		}
	}

	private Set<ConstraintViolation<DynamicCartItemModifierField>> validateWithCurrentConstraints(
			final DynamicCartItemModifierField fieldToValidate) {

		return configuration.addMapping(constraintMapping)
				.buildValidatorFactory()
				.getValidator()
				.validate(fieldToValidate);
	}

	private ValidationProviderResolver createValidationProviderResolver(final ValidationProvider<?> provider) {
		return () -> Collections.singletonList(provider);
	}
}
