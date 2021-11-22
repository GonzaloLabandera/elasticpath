/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.validation.service.ModifierFieldValidationService;
import com.elasticpath.xpf.connectivity.entity.XPFModifierField;
import com.elasticpath.xpf.connectivity.entity.XPFModifierGroup;

/**
 * Base class for Modifier*Validator classes.
 */
public class CartModifierValidator {

	@Autowired
	@Named("cachedModifierFieldValidationService")
	private ModifierFieldValidationService modifierFieldValidationService;

	private Set<XPFModifierField> getModifierFields(final Set<XPFModifierGroup> cartItemModifierGroups) {
		return cartItemModifierGroups.stream().map(XPFModifierGroup::getModifierFields)
				.flatMap(Collection::stream)
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	/**
	 * Calls validation service.
	 * @param itemsToValidate items to validate
	 * @param cartItemModifierGroups modifiers groups
	 * @return list of structured error messages
	 */
	protected List<StructuredErrorMessage> baseValidate(final Map<String, String> itemsToValidate,
			final Set<XPFModifierGroup> cartItemModifierGroups) {
		return modifierFieldValidationService.validate(itemsToValidate, getModifierFields(cartItemModifierGroups), null);
	}

	void setModifierFieldValidationService(final ModifierFieldValidationService modifierFieldValidationService) {
		this.modifierFieldValidationService = modifierFieldValidationService;
	}
}
