/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.validator;

/**
 * Represents a base interface for validator.
 *
 * @param <T> type of entity for validation.
 */
public interface Validator<T> {

	/**
	 * Validate entity.
	 *
	 * @param source validated data
	 */
	void validate(T source);

}
