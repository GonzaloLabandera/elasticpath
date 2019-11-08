/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.validation.validators.util;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;

import com.google.common.collect.Lists;

import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.AttributeMultiValueType;
import com.elasticpath.validation.defs.AbstractConstraintDef;
import com.elasticpath.validation.defs.MultiOptionDef;
import com.elasticpath.validation.defs.NotBlankDef;
import com.elasticpath.validation.defs.SingleOptionDef;

/**
 * Validator used for validation of dynamic fields, like {@link Attribute}.
 * It uses Hibernate implementation because of the ability to add constraints programmatically.
 */
public class DynamicAttributeValueValidator extends ConstraintValidator<DynamicAttributeValue> {

	/**
	 * Default constructor.
	 * Initializes required Hibernate structures.
	 */
	public DynamicAttributeValueValidator() {
		super(DynamicAttributeValue.class);
	}

	/**
	 * Validate dynamic field.
	 *
	 * @param valueToValidate a value to validate.
	 * @return a set of {@link ConstraintViolation} instances or empty set.
	 */
	public Set<ConstraintViolation<DynamicAttributeValue>> validate(final DynamicAttributeValue valueToValidate) {

		addIsRequiredConstraint(valueToValidate);

		Set<ConstraintViolation<DynamicAttributeValue>> violations = validateWithCurrentConstraints(valueToValidate);
		if (!violations.isEmpty()) {
			return violations;
		}

		addAdditionalConstraints(valueToValidate);

		return validateWithCurrentConstraints(valueToValidate);
	}

	private void addIsRequiredConstraint(final DynamicAttributeValue valueToValidate) {
		final String attributeKey = valueToValidate.getAttributeKey();
		final Attribute attribute = valueToValidate.getAttribute();

		if (attribute.isRequired()) {
			getConstraintMappingContext().constraint(
					new NotBlankDef()
							.fieldName(attributeKey));
		}
	}

	private void addAdditionalConstraints(final DynamicAttributeValue valueToValidate) {
		final String attributeKey = valueToValidate.getAttributeKey();
		final Attribute attribute = valueToValidate.getAttribute();
		List<AbstractConstraintDef<?>> typeConstraintDefs = Lists.newArrayList();

		attribute.getAttributeType().getConstraintDefs().ifPresent(
				constraintDefs -> Collections.addAll(typeConstraintDefs, constraintDefs));

		// Additional constraints based on field type. Each field may contain one or more constraint definitions
		final String[] validFieldOptions = valueToValidate.getValidOptions().orElse(new String[]{});

		// allows validation invoker to define pick lists
		if (validFieldOptions.length > 0) {
			// use attribute multi-value flag to pick validator
			if (AttributeMultiValueType.SINGLE_VALUE.equals(attribute.getMultiValueType())) {
				typeConstraintDefs.add(new SingleOptionDef());
			} else {
				typeConstraintDefs.add(new MultiOptionDef());
			}
		}

		for (AbstractConstraintDef<?> constraintDef : typeConstraintDefs) {
			constraintDef.validFieldOptions(validFieldOptions);
			constraintDef.fieldName(attributeKey);
			getConstraintMappingContext().constraint(constraintDef);
		}
	}

	private Set<ConstraintViolation<DynamicAttributeValue>> validateWithCurrentConstraints(
			final DynamicAttributeValue valueToValidate) {

		return getConfiguration().addMapping(getConstraintMapping())
				.buildValidatorFactory()
				.getValidator()
				.validate(valueToValidate);
	}

}
