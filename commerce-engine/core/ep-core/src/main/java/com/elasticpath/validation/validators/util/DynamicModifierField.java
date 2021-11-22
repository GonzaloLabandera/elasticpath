/*
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.validation.validators.util;

import java.util.stream.Collectors;

import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierFieldOption;

/**
 * A wrapper class for {@link ModifierField} used
 * in dynamic validation.
 */
public class DynamicModifierField extends DynamicValue<ModifierField> {
	private final String valueToValidate;
	private final String fieldName;

	/**
	 * Custom constructor.
	 *
	 * @param fieldName       the name of the field being validated.
	 * @param valueToValidate field value to validate.
	 * @param referentField   referent field.
	 */
	public DynamicModifierField(final String fieldName,
								final String valueToValidate,
								final ModifierField referentField) {
		super(referentField, referentField.getModifierFieldOptions()
				.stream()
				.map(ModifierFieldOption::getValue)
				.collect(Collectors.toSet()));
		this.fieldName = fieldName;
		this.valueToValidate = valueToValidate;
	}

	/**
	 * In case of {@link com.elasticpath.domain.modifier.ModifierType#PICK_MULTI_OPTION}
	 * return zero or more invalid options or null in all other cases/types.
	 *
	 * @return null - if field doesn't have options or validated value is null/empty;
	 * String array with zero or more invalid options.
	 */
	public String[] getInvalidOptions() {
		return MultiOptionHandler.getInvalidOptions(valueToValidate, getValidOptions().toArray(new String[0]));
	}

	/**
	 * Returns the name of the field being validated.
	 * E.g. email
	 *
	 * @return field name.
	 */
	public String getFieldName() {
		return fieldName;
	}
}
