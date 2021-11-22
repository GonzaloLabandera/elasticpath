/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.validation.validators.util;

import java.util.Objects;
import java.util.Set;

/**
 * A wrapper class for a generic class used in dynamic validation.
 * @param <T> the type definition class for the value
 */
public class DynamicValue<T> {
	private final T valueTypeDefinition;
	private final Set<String> validOptions;

	/**
	 * Constructor.
	 * @param valueTypeDefinition the value type definition
	 * @param validOptions the set of valid options if applicable
	 */
	public DynamicValue(final T valueTypeDefinition,
						final Set<String> validOptions) {
		this.valueTypeDefinition = valueTypeDefinition;
		Objects.requireNonNull(validOptions);
		this.validOptions = validOptions;
	}

	@SuppressWarnings({"PMD.MethodReturnsInternalArray"})
	public Set<String> getValidOptions() {
		return validOptions;
	}

	public T getValueTypeDefinition() {
		return valueTypeDefinition;
	}
}

