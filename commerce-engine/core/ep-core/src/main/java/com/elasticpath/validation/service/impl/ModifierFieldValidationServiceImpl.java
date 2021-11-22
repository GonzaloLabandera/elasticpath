/*
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.validation.service.impl;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import com.google.common.collect.Maps;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.common.dto.StructuredErrorResolution;
import com.elasticpath.cache.Cache;
import com.elasticpath.cache.impl.AlwaysMissCache;
import com.elasticpath.commons.util.Pair;
import com.elasticpath.domain.modifier.impl.ModifierFieldAdapter;
import com.elasticpath.validation.ConstraintViolationTransformer;
import com.elasticpath.validation.service.ModifierFieldValidationService;
import com.elasticpath.validation.validators.util.DynamicModifierField;
import com.elasticpath.validation.validators.util.DynamicModifierFieldValidator;
import com.elasticpath.xpf.connectivity.entity.XPFModifierField;

/**
 * Service for dynamic validation of {@link XPFModifierField} fields.
 */
public class ModifierFieldValidationServiceImpl implements ModifierFieldValidationService {

	private boolean isRequiredSuppression;
	private ConstraintViolationTransformer constraintViolationTransformer;

	@Override
	public List<StructuredErrorMessage> validate(final Map<String, String> itemsToValidate,
												 final Set<XPFModifierField> referentFields,
												 final StructuredErrorResolution resolution) {
		return validateWithCache(itemsToValidate, referentFields, resolution, new AlwaysMissCache<>(), new AlwaysMissCache<>());
	}

	@Override
	public List<StructuredErrorMessage> validateWithCache(final Map<String, String> itemsToValidate,
														  final Set<XPFModifierField> referentFields,
														  final StructuredErrorResolution resolution,
														  final Cache<Pair<Object, Set<String>>, Validator> requiredValidatorCache,
														  final Cache<Pair<Object, Set<String>>, Validator> completeValidatorCache) {
		final Map<String, XPFModifierField> refFieldNameToField = Maps.uniqueIndex(referentFields, XPFModifierField::getCode);

		if (refFieldNameToField.isEmpty()) {
			return Collections.emptyList();
		}

		Map<String, String> validationItems = referentFields.stream().collect(Collectors.toMap(XPFModifierField::getCode,
				field -> itemsToValidate.getOrDefault(field.getCode(), "")));

		final Set<ConstraintViolation<DynamicModifierField>> violations = new LinkedHashSet<>();
		for (Map.Entry<String, String> dynamicPropertyToValidate : validationItems.entrySet()) {
			final String propertyNameBeingValidated = dynamicPropertyToValidate.getKey();
			final String propertyValueToValidate = dynamicPropertyToValidate.getValue();

			final XPFModifierField referentField = refFieldNameToField.get(propertyNameBeingValidated);
			ModifierFieldAdapter modifierFieldAdapter = new ModifierFieldAdapter(referentField);

			final DynamicModifierField dynamicModifierField = new DynamicModifierField(propertyNameBeingValidated,
					propertyValueToValidate, modifierFieldAdapter);

			violations.addAll(getDynamicModifierFieldValidator(requiredValidatorCache, completeValidatorCache)
					.validate(dynamicModifierField));
		}

		return constraintViolationTransformer.transform(violations, resolution);
	}

	/**
	 * Protected for extension.
	 * @param requiredValidatorCache a cache for "is required" dynamic value validators
	 * @param completeValidatorCache a cache for "complete" dynamic value validators
	 * @return the validator
	 */
	protected DynamicModifierFieldValidator getDynamicModifierFieldValidator(
			final Cache<Pair<Object, Set<String>>, Validator> requiredValidatorCache,
			final Cache<Pair<Object, Set<String>>, Validator> completeValidatorCache) {
		return new DynamicModifierFieldValidator(isRequiredSuppression, requiredValidatorCache, completeValidatorCache);
	}

	public void setConstraintViolationTransformer(final ConstraintViolationTransformer constraintViolationTransformer) {
		this.constraintViolationTransformer = constraintViolationTransformer;
	}

	public void setRequiredSuppression(final boolean requiredSuppression) {
		isRequiredSuppression = requiredSuppression;
	}
}
