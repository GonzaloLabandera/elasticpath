/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.validation.validators.util;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.validation.Validator;

import com.google.common.collect.Lists;

import com.elasticpath.cache.Cache;
import com.elasticpath.commons.util.Pair;
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
public class DynamicAttributeValueValidator extends AbstractConstraintValidator<DynamicAttributeValue> {

	/**
	 * Default constructor. Initializes required Hibernate structures.
	 *
	 * @param requiredValidatorCache a cache for "is required" dynamic value validators
	 * @param completeValidatorCache a cache for "complete" dynamic value validators
	 */
	public DynamicAttributeValueValidator(final Cache<Pair<Object, Set<String>>, Validator> requiredValidatorCache,
										  final Cache<Pair<Object, Set<String>>, Validator> completeValidatorCache) {
		super(DynamicAttributeValue.class, requiredValidatorCache, completeValidatorCache);
	}

	@Override
	public Validator getIsRequiredValidator(final DynamicAttributeValue dynamicValue) {
		initHibernateValidator();
		addIsRequiredConstraint(dynamicValue);
		return getValidatorWithCurrentConstraints();
	}

	@Override
	public Validator getCompleteValidator(final DynamicAttributeValue dynamicValue) {
		initHibernateValidator();
		addAdditionalConstraints(dynamicValue);
		return getValidatorWithCurrentConstraints();
	}

	private void addIsRequiredConstraint(final DynamicAttributeValue valueToValidate) {
		final String attributeKey = valueToValidate.getAttributeKey();
		final Attribute attribute = valueToValidate.getValueTypeDefinition();

		if (attribute.isRequired()) {
			getConstraintMappingContext().constraint(
					new NotBlankDef()
							.fieldName(attributeKey));
		}
	}

	private void addAdditionalConstraints(final DynamicAttributeValue valueToValidate) {
		final String attributeKey = valueToValidate.getAttributeKey();
		final Attribute attribute = valueToValidate.getValueTypeDefinition();
		List<AbstractConstraintDef<?>> typeConstraintDefs = Lists.newArrayList();

		attribute.getAttributeType().getConstraintDefs().ifPresent(
				constraintDefs -> Collections.addAll(typeConstraintDefs, constraintDefs));

		// Additional constraints based on field type. Each field may contain one or more constraint definitions
		final String[] validFieldOptions = valueToValidate.getValidOptions().toArray(new String[0]);

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

	private Validator getValidatorWithCurrentConstraints() {
		return getConfiguration().addMapping(getConstraintMapping())
				.buildValidatorFactory()
				.getValidator();
	}

}
