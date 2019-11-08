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

import com.google.common.collect.Maps;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.validation.ConstraintViolationTransformer;
import com.elasticpath.validation.service.ModifierFieldValidationService;
import com.elasticpath.validation.validators.util.DynamicModifierField;
import com.elasticpath.validation.validators.util.DynamicModifierFieldValidator;

/**
 * Service for dynamic validation of {@link ModifierField} fields.
 */
public class ModifierFieldValidationServiceImpl implements ModifierFieldValidationService {

	/*
	 * Flag indicates required suppression or not.
	 */
	private boolean isRequiredSuppression;

	private ConstraintViolationTransformer constraintViolationTransformer;

	@Override
	public List<StructuredErrorMessage> validate(final Map<String, String> itemsToValidate,
			final Set<ModifierField> referentFields) {

		final Map<String, ModifierField> refFieldNameToField = Maps.uniqueIndex(referentFields, ModifierField::getCode);

		if (refFieldNameToField.isEmpty()) {
			return Collections.emptyList();
		}

		Map<String, String> validationItems = referentFields.stream().collect(Collectors.toMap(ModifierField::getCode,
				field -> itemsToValidate.getOrDefault(field.getCode(), "")));

		final Set<ConstraintViolation<DynamicModifierField>> violations = new LinkedHashSet<>();
		for (Map.Entry<String, String> dynamicPropertyToValidate : validationItems.entrySet()) {
			final String propertyNameBeingValidated = dynamicPropertyToValidate.getKey();
			final String propertyValueToValidate = dynamicPropertyToValidate.getValue();

			final ModifierField referentField = refFieldNameToField.get(propertyNameBeingValidated);

			final DynamicModifierField dynamicModifierField = new DynamicModifierField(propertyNameBeingValidated,
					propertyValueToValidate, referentField);

			violations.addAll(new DynamicModifierFieldValidator(isRequiredSuppression)
					.validate(dynamicModifierField));
		}

		return constraintViolationTransformer.transform(violations);
	}

	public void setConstraintViolationTransformer(final ConstraintViolationTransformer constraintViolationTransformer) {
		this.constraintViolationTransformer = constraintViolationTransformer;
	}

	public void setRequiredSuppression(final boolean requiredSuppression) {
		isRequiredSuppression = requiredSuppression;
	}
}
