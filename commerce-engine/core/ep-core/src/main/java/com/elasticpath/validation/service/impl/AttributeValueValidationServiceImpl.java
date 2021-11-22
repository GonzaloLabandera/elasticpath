/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.validation.service.impl;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import com.google.common.collect.Maps;
import org.apache.commons.collections.MapUtils;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.cache.Cache;
import com.elasticpath.cache.impl.AlwaysMissCache;
import com.elasticpath.commons.util.Pair;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.validation.ConstraintViolationTransformer;
import com.elasticpath.validation.service.AttributeValueValidationService;
import com.elasticpath.validation.validators.util.DynamicAttributeValue;
import com.elasticpath.validation.validators.util.DynamicAttributeValueValidator;

/**
 * Service for dynamic validation of {@link Attribute} values.
 */
public class AttributeValueValidationServiceImpl implements AttributeValueValidationService {

	private ConstraintViolationTransformer constraintViolationTransformer;

	@Override
	public List<StructuredErrorMessage> validate(final Map<String, String> valuesToValidate,
												 final Map<Attribute, Set<String>> referentAllowedValues) {
		return validateWithCache(valuesToValidate, referentAllowedValues, new AlwaysMissCache<>(), new AlwaysMissCache<>());
	}

	@Override
	public List<StructuredErrorMessage> validateWithCache(final Map<String, String> valuesToValidate,
														  final Map<Attribute, Set<String>> referentAllowedValues,
														  final Cache<Pair<Object, Set<String>>, Validator> requiredValidatorCache,
														  final Cache<Pair<Object, Set<String>>, Validator> completeValidatorCache) {

		Objects.requireNonNull(valuesToValidate, "No attribute values were provided to validate.");

		if (valuesToValidate.isEmpty()) {
			return Collections.emptyList();
		}

		final Map<String, Attribute> refAttributeKeyToAttribute = Maps.uniqueIndex(referentAllowedValues.keySet(), Attribute::getKey);

		if (MapUtils.isEmpty(refAttributeKeyToAttribute)) {
			throw new IllegalStateException("Cannot validate attribute values because attribute definitions were not provided.");
		}

		final Set<ConstraintViolation<DynamicAttributeValue>> violations = new LinkedHashSet<>();
		for (Map.Entry<String, String> dynamicAttributeToValidate : valuesToValidate.entrySet()) {
			final String attributeKeyBeingValidated = dynamicAttributeToValidate.getKey();
			final String attributeValueToValidate = dynamicAttributeToValidate.getValue();

			final Attribute referentAttribute = refAttributeKeyToAttribute.get(attributeKeyBeingValidated);

			final DynamicAttributeValue dynamicAttributeValue = new DynamicAttributeValue(attributeKeyBeingValidated,
					attributeValueToValidate, referentAttribute,
					Optional.ofNullable(referentAllowedValues.get(referentAttribute)).orElse(Collections.emptySet()));

			violations.addAll(getDynamicAttributeValueValidator(requiredValidatorCache, completeValidatorCache)
					.validate(dynamicAttributeValue));
		}

		return constraintViolationTransformer.transform(violations);
	}

	/**
	 * Protected for extension.
	 * @param requiredValidatorCache a cache for "is required" dynamic value validators
	 * @param completeValidatorCache a cache for "complete" dynamic value validators
	 * @return the validator
	 */
	protected DynamicAttributeValueValidator getDynamicAttributeValueValidator(
			final Cache<Pair<Object, Set<String>>, Validator> requiredValidatorCache,
			final Cache<Pair<Object, Set<String>>, Validator> completeValidatorCache) {
		return new DynamicAttributeValueValidator(requiredValidatorCache, completeValidatorCache);
	}

	public void setConstraintViolationTransformer(final ConstraintViolationTransformer constraintViolationTransformer) {
		this.constraintViolationTransformer = constraintViolationTransformer;
	}
}
