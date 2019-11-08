/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.validation.validators.util;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.elasticpath.domain.attribute.Attribute;

/**
 * A wrapper class for {@link Attribute} used in dynamic validation.
 */
public class DynamicAttributeValue {
	private final String valueToValidate;
	private final String attributeKey;
	private final Attribute attribute;
	private final String[] validOptions;

	/**
	 * Custom constructor.
	 *  @param attributeKey    the key of the field being validated.
	 * @param valueToValidate field value to validate.
	 * @param attribute       the referent attribute.
	 * @param validOptions    the set of valid options if applicable
	 */
	public DynamicAttributeValue(final String attributeKey,
			final String valueToValidate,
			final Attribute attribute,
			final Set<String> validOptions) {

		this.attributeKey = attributeKey;
		this.valueToValidate = valueToValidate;
		this.attribute = attribute;

		Objects.requireNonNull(validOptions);
		this.validOptions = validOptions.toArray(new String[] {});
	}

	/**
	 * In case of {@link com.elasticpath.domain.modifier.ModifierType#PICK_MULTI_OPTION}
	 * return zero or more invalid options or null in all other cases/types.
	 *
	 * @return null - if field doesn't have options or validated value is null/empty;
	 * String array with zero or more invalid options.
	 */
	public String[] getInvalidOptions() {
		return MultiOptionHandler.getInvalidOptions(valueToValidate, validOptions);
	}

	/**
	 * Returns the key of the attribute being validated.
	 * E.g. CP_DOB
	 *
	 * @return attribute key.
	 */
	public String getAttributeKey() {
		return attributeKey;
	}

	/**
	 * Get valid field options, if any.
	 * Applicable to {@link com.elasticpath.domain.modifier.ModifierType#PICK_SINGLE_OPTION} and
	 * {@link com.elasticpath.domain.modifier.ModifierType#PICK_MULTI_OPTION} field types.
	 *
	 * @return an array of valid options, if available or empty array.
	 */
	public Optional<String[]> getValidOptions() {
		return Optional.ofNullable(validOptions);
	}

	public Attribute getAttribute() {
		return attribute;
	}
}
