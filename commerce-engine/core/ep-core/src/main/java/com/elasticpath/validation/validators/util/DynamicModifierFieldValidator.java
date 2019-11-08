/*
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.validation.validators.util;

import java.util.Set;
import javax.validation.ConstraintViolation;

import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.validation.defs.AbstractConstraintDef;
import com.elasticpath.validation.defs.LengthDef;
import com.elasticpath.validation.defs.NotBlankDef;

/**
 * Validator used for validation of dynamic fields, like {@link ModifierField}.
 * It uses Hibernate implementation because of the ability to add constraints programmatically.
 */
public class DynamicModifierFieldValidator extends ConstraintValidator<DynamicModifierField> {

	private final boolean isRequiredSuppression;

	/**
	 * Default constructor.
	 * Initializes required Hibernate structures.
	 *
	 * @param isRequiredSuppression flag indicates whether required constraint should be applied or not.
	 */
	public DynamicModifierFieldValidator(final boolean isRequiredSuppression) {
		super(DynamicModifierField.class);
		this.isRequiredSuppression = isRequiredSuppression;
	}

	/**
	 * Validate dynamic field.
	 *
	 * @param fieldToValidate a field to validate.
	 * @return a set of {@link ConstraintViolation} instances or empty set.
	 */
	public Set<ConstraintViolation<DynamicModifierField>> validate(final DynamicModifierField fieldToValidate) {

		addIsRequiredConstraint(fieldToValidate);

		Set<ConstraintViolation<DynamicModifierField>> violations = validateWithCurrentConstraints(fieldToValidate);
		if (!violations.isEmpty()) {
			return violations;
		}

		addAdditionalConstraints(fieldToValidate);

		return validateWithCurrentConstraints(fieldToValidate);
	}

	private void addIsRequiredConstraint(final DynamicModifierField fieldToValidate) {
		final String fieldName = fieldToValidate.getFieldName();
		final ModifierField referentField = fieldToValidate.getReferentField();

		if (referentField.isRequired() && !isRequiredSuppression) {
			getConstraintMappingContext().constraint(
					new NotBlankDef()
							.fieldName(fieldName));
		}
	}

	private void addAdditionalConstraints(final DynamicModifierField fieldToValidate) {
		final String fieldName = fieldToValidate.getFieldName();
		final ModifierField referentField = fieldToValidate.getReferentField();
		final AbstractConstraintDef<?>[] typeConstraintDefs =
				referentField.getFieldType().getConstraintDefs().orElse(new AbstractConstraintDef<?>[]{});

		if (referentField.getMaxSize() != null) {
			getConstraintMappingContext().constraint(
					new LengthDef()
							.max(referentField.getMaxSize())
							.fieldName(fieldName));
		}

		// Additional constraints based on field type. Each field may contain one or more constraint definitions
		final String[] validFieldOptions = fieldToValidate.getValidOptions().orElse(new String[]{});
		for (AbstractConstraintDef<?> constraintDef : typeConstraintDefs) {
			constraintDef.validFieldOptions(validFieldOptions);
			constraintDef.fieldName(fieldName);
			getConstraintMappingContext().constraint(constraintDef);
		}
	}

	private Set<ConstraintViolation<DynamicModifierField>> validateWithCurrentConstraints(
			final DynamicModifierField fieldToValidate) {

		return getConfiguration().addMapping(getConstraintMapping())
				.buildValidatorFactory()
				.getValidator()
				.validate(fieldToValidate);
	}
}
