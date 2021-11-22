/*
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.validation;

import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.common.dto.StructuredErrorResolution;

/**
 * Transforms multiple {@link javax.validation.ConstraintViolation} to a list of {@link StructuredErrorMessage}.
 */
public interface ConstraintViolationTransformer {

	/**
	 * Provides a way to generate {@link StructuredErrorMessage} for {@link ConstraintViolation}.
	 *
	 * @param <T> type of the domain object
	 * @param errors set of ConstraintViolations
	 * @return List<StructuredErrorMessage> StructuredErrorMessage list
	 */
	<T> List<StructuredErrorMessage> transform(Set<ConstraintViolation<T>> errors);

	/**
	 * Provides a way to generate {@link StructuredErrorMessage} for {@link ConstraintViolation}.
	 *
	 * @param <T> type of the domain object
	 * @param errors set of ConstraintViolations
	 * @param resolution the resolution details to use for any generated structured error messages
	 * @return List<StructuredErrorMessage> StructuredErrorMessage list
	 */
	<T> List<StructuredErrorMessage> transform(Set<ConstraintViolation<T>> errors, StructuredErrorResolution resolution);
}
