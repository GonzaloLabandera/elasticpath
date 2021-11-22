/*
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.validation.validators.util;

import java.util.Set;
import javax.validation.Validator;

import com.elasticpath.cache.Cache;
import com.elasticpath.commons.util.Pair;
import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.validation.defs.AbstractConstraintDef;
import com.elasticpath.validation.defs.LengthDef;
import com.elasticpath.validation.defs.NotBlankDef;

/**
 * Validator used for validation of dynamic fields, like {@link ModifierField}.
 * It uses Hibernate implementation because of the ability to add constraints programmatically.
 */
public class DynamicModifierFieldValidator extends AbstractConstraintValidator<DynamicModifierField> {

	private final boolean isRequiredSuppression;

	/**
	 * Default constructor.
	 * Initializes required Hibernate structures.
	 *
	 * @param isRequiredSuppression flag indicates whether required constraint should be applied or not.
	 * @param requiredValidatorCache a cache for "is required" dynamic value validators
	 * @param completeValidatorCache a cache for "complete" dynamic value validators
	 */
	public DynamicModifierFieldValidator(final boolean isRequiredSuppression,
										 final Cache<Pair<Object, Set<String>>, Validator> requiredValidatorCache,
										 final Cache<Pair<Object, Set<String>>, Validator> completeValidatorCache) {
		super(DynamicModifierField.class, requiredValidatorCache, completeValidatorCache);
		this.isRequiredSuppression = isRequiredSuppression;
	}

	@Override
	public Validator getIsRequiredValidator(final DynamicModifierField dynamicValue) {
		initHibernateValidator();
		addIsRequiredConstraint(dynamicValue);
		return getValidatorWithCurrentConstraints();
	}

	@Override
	public Validator getCompleteValidator(final DynamicModifierField dynamicValue) {
		initHibernateValidator();
		addAdditionalConstraints(dynamicValue);
		return getValidatorWithCurrentConstraints();
	}

	private void addIsRequiredConstraint(final DynamicModifierField fieldToValidate) {
		final String fieldName = fieldToValidate.getFieldName();
		final ModifierField referentField = fieldToValidate.getValueTypeDefinition();

		if (referentField.isRequired() && !isRequiredSuppression) {
			getConstraintMappingContext().constraint(
					new NotBlankDef()
							.fieldName(fieldName));
		}
	}

	private void addAdditionalConstraints(final DynamicModifierField fieldToValidate) {
		final String fieldName = fieldToValidate.getFieldName();
		final ModifierField referentField = fieldToValidate.getValueTypeDefinition();
		final AbstractConstraintDef<?>[] typeConstraintDefs =
				referentField.getFieldType().getConstraintDefs().orElse(new AbstractConstraintDef<?>[]{});

		if (referentField.getMaxSize() != null) {
			getConstraintMappingContext().constraint(
					new LengthDef()
							.max(referentField.getMaxSize())
							.fieldName(fieldName));
		}

		// Additional constraints based on field type. Each field may contain one or more constraint definitions
		final String[] validFieldOptions = fieldToValidate.getValidOptions().toArray(new String[0]);
		for (AbstractConstraintDef<?> constraintDef : typeConstraintDefs) {
			constraintDef.validFieldOptions(validFieldOptions);
			constraintDef.fieldName(fieldName);
			getConstraintMappingContext().constraint(constraintDef);
		}
	}

	private Validator getValidatorWithCurrentConstraints() {
		return getConfiguration().addMapping(getConstraintMapping())
				.buildValidatorFactory()
				.getValidator();
	}
}
