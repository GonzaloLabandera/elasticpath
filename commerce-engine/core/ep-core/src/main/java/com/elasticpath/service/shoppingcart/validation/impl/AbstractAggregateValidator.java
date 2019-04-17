/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import java.util.Collection;
import java.util.stream.Collectors;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.service.shoppingcart.validation.Validator;

/**
 * Collects the results from several embedded validators when a single validate call is made on this validator.
 *
 * @param <T> the type of context object that the embedded validators expect.
 * @param <S> the type of context object that this validator expects.
 */
public abstract class AbstractAggregateValidator<T, S extends T> implements Validator<S> {

	private Collection<Validator<T>> validators;

	@Override
	public Collection<StructuredErrorMessage> validate(final S context) {
		return validators.stream()
				.map(strategy -> strategy.validate(context))
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	protected Collection<Validator<T>> getValidators() {
		return validators;
	}

	public void setValidators(final Collection<Validator<T>> validators) {
		this.validators = validators;
	}
}
