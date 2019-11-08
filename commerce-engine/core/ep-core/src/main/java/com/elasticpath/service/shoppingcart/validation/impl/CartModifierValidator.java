/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.validation.service.ModifierFieldValidationService;

/**
 * Base class for Modifier*Validator classes.
 */
public class CartModifierValidator {
	private ModifierFieldValidationService modifierFieldValidationService;

	private Set<ModifierField> getModifierFields(final Set<ModifierGroup> cartItemModifierGroups) {
		return cartItemModifierGroups.stream().map(ModifierGroup::getModifierFields)
				.flatMap(Set::stream)
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	/**
	 * Calls validation service.
	 * @param itemsToValidate items to validate
	 * @param cartItemModifierGroups modifiers groups
	 * @return list of structured error messages
	 */
	protected List<StructuredErrorMessage> baseValidate(final Map<String, String> itemsToValidate,
			final Set<ModifierGroup> cartItemModifierGroups) {
		return modifierFieldValidationService.validate(itemsToValidate,
				getModifierFields(cartItemModifierGroups));
	}

	protected ModifierFieldValidationService getModifierFieldValidationService() {
		return modifierFieldValidationService;
	}

	public void setModifierFieldValidationService(final ModifierFieldValidationService modifierFieldValidationService) {
		this.modifierFieldValidationService = modifierFieldValidationService;
	}
}
