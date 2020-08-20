/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;

import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.common.dto.StructuredErrorMessageType;
import com.elasticpath.base.common.dto.StructuredErrorResolution;
import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.shoppingcart.CartType;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.service.shoppingcart.MulticartItemListTypeLocationProvider;
import com.elasticpath.service.shoppingcart.validation.ShoppingCartValidationContext;
import com.elasticpath.service.shoppingcart.validation.ShoppingCartValidator;
import com.elasticpath.validation.validators.util.DynamicModifierField;
import com.elasticpath.validation.validators.util.DynamicModifierFieldValidator;

/**
 * Determines if cart configuration is incomplete.
 */
public class ModifierShoppingCartValidatorImpl implements ShoppingCartValidator {

	/**
	 * Message id for this validation.
	 */
	public static final String MESSAGE_ID = "cart.missing.data";

	/*
	 * Flag indicates required suppression or not.
	 */
	private boolean isRequiredSuppression;

	private CartOrder cartOrder;

	private static final String ERROR_ID = "cart.missing.data";

	private static final String FIELD_NAME = "field-name";

	private static final String EXCLUDED_ATTRIBUTES_REGEX = "payload|groups|message|validFieldOptions";

	private static final String DYNAMIC_VALIDATION_FIELD_NAME_KEY = "fieldName";

	private static final String DEBUG_MESSAGE = " cart descriptor value is required.";

	private MulticartItemListTypeLocationProvider multicartItemListTypeLocationProvider;

	@Override
	public Collection<StructuredErrorMessage> validate(final ShoppingCartValidationContext context) {
		// We don't have a cart order to check once we're in the ValidationCheckoutAction.
		if (context.getCartOrder() == null) {
			return Collections.emptyList();
		}

		cartOrder = context.getCartOrder();

		final ShoppingCart shoppingCart = context.getShoppingCart();
		Map<String, String> cartData = new HashMap<>();
		shoppingCart.getCartData().forEach((key, data) -> cartData.put(data.getKey(), data.getValue()));
		Optional<CartType> cartTypeOptional = shoppingCart.getStore().getShoppingCartTypes().stream()
				.filter(cartType -> multicartItemListTypeLocationProvider
						.getMulticartItemListTypeForStore(shoppingCart.getStore().getCode()).equals(cartType.getName()))
				.findFirst();

		if (!cartTypeOptional.isPresent()) {
			return Collections.emptySet();
		}
		CartType cartType = cartTypeOptional.get();
		List<ModifierGroup> modifierGroups = cartType.getModifiers();

		Set<ModifierField> modifierFields = modifierGroups.stream().map(ModifierGroup::getModifierFields)
				.flatMap(Set::stream)
				.collect(Collectors.toCollection(LinkedHashSet::new));

		return validate(cartData, modifierFields);
	}

	private List<StructuredErrorMessage> validate(final Map<String, String> itemsToValidate,
												  final Set<ModifierField> referentFields) {

		if (referentFields.isEmpty()) {
			return Collections.emptyList();
		}

		final Map<String, ModifierField> refFieldNameToField = Maps.uniqueIndex(referentFields, ModifierField::getCode);

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

		return transform(violations);
	}

	private <T> List<StructuredErrorMessage> transform(final Set<ConstraintViolation<T>> errors) {
		if (CollectionUtils.isEmpty(errors)) {
			return Collections.emptyList();
		}

		return errors.stream()
				.map(this::transform)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	private <T> StructuredErrorMessage transform(final ConstraintViolation<T> constraintViolation) {
		String constraintViolationId = constraintViolation.getMessageTemplate();

		if (constraintViolationId.equalsIgnoreCase(constraintViolation.getMessage())) {
			return null;
		}

		String fieldName = constraintViolation.getPropertyPath().toString();

		Map<String, String> data = new HashMap<>();
		data.put(FIELD_NAME, fieldName);

		// Iterate through all the attributes and grab the ones not on the excluded regex.
		Map<String, Object> attributes = constraintViolation.getConstraintDescriptor().getAttributes();
		attributes.forEach((key, value) -> {
			if (!key.matches(EXCLUDED_ATTRIBUTES_REGEX)) {
				// Grab the fieldname from the attributes and overwrite the existing one.
				// This is for dynamic validation.
				if (DYNAMIC_VALIDATION_FIELD_NAME_KEY.equalsIgnoreCase(key) && !value.toString().isEmpty()) {
					data.put(FIELD_NAME, value.toString());
				} else {
					if (!DYNAMIC_VALIDATION_FIELD_NAME_KEY.equalsIgnoreCase(key)) {
						data.put(key, value.toString());
					}
				}
			}
		});

		return new StructuredErrorMessage(StructuredErrorMessageType.NEEDINFO, ERROR_ID, "'" + data.get(FIELD_NAME) + "'"
				+ DEBUG_MESSAGE, data, new StructuredErrorResolution(CartOrder.class, cartOrder.getGuid()));
	}

	public void setRequiredSuppression(final boolean requiredSuppression) {
		isRequiredSuppression = requiredSuppression;
	}

	public void setMulticartItemListTypeLocationProvider(final MulticartItemListTypeLocationProvider multicartItemListTypeLocationProvider) {
		this.multicartItemListTypeLocationProvider = multicartItemListTypeLocationProvider;
	}
}
