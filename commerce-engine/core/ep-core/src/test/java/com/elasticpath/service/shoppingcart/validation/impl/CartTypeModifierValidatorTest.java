/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.modifier.ModifierType;
import com.elasticpath.domain.shoppingcart.CartType;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.impl.CartData;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.shoppingcart.validation.ShoppingCartValidationContext;
import com.elasticpath.validation.impl.ConstraintViolationTransformerImpl;
import com.elasticpath.validation.service.impl.ModifierFieldValidationServiceImpl;

@RunWith(MockitoJUnitRunner.class)
public class CartTypeModifierValidatorTest {

	private static final int MAX_SIZE = 10;

	@InjectMocks
	private CartTypeModifierValidatorImpl validator;

	private final ModifierFieldValidationServiceImpl validationService = new ModifierFieldValidationServiceImpl();

	private final ConstraintViolationTransformerImpl constraintViolationTransformer = new ConstraintViolationTransformerImpl();

	@Mock
	private ShoppingCartValidationContext context;

	@Mock
	private ShoppingCart shoppingCart;

	@Mock
	private ModifierGroup modifierGroup;

	@Mock
	private Store store;

	@Mock
	private CartType cartType;
	@Mock
	private ModifierField referentField;
	public static final String NAME = "name";
	public static final String FIELD_CHARACTER = "a";

	@Before
	public void setUp() {

		when(context.getShoppingCart()).thenReturn(shoppingCart);
		when(shoppingCart.getStore()).thenReturn(store);
		when(store.getShoppingCartTypes()).thenReturn(Collections.singletonList(cartType));
		when(cartType.getModifiers()).thenReturn(Collections.singletonList(modifierGroup));

		validationService.setConstraintViolationTransformer(constraintViolationTransformer);

		validator.setModifierFieldValidationService(validationService);

	}

	@Test
	public void testModifierIncorrect() {

		// when
		Map<String, String> idMap = new HashMap<>();
		idMap.put("{field.invalid.size}", "field.invalid.size");

		constraintViolationTransformer.setIdMap(idMap);

		String fieldName = NAME;
		StringBuilder builder = new StringBuilder(FIELD_CHARACTER);
		for (int i = 0; i < MAX_SIZE + 1; i++) {
			builder.append(FIELD_CHARACTER);
		}
		String fieldValue = builder.toString();


		CartData cartData = new CartData(fieldName, fieldValue);
		Set<ModifierField> referentFields = new HashSet<>();

		when(referentField.getCode()).thenReturn(fieldName);
		when(referentField.getFieldType()).thenReturn(ModifierType.SHORT_TEXT);
		when(referentField.isRequired()).thenReturn(true);
		when(referentField.getMaxSize()).thenReturn(MAX_SIZE);

		referentFields.add(referentField);
		when(modifierGroup.getModifierFields()).thenReturn(referentFields);
		when(shoppingCart.getCartData()).thenReturn(Collections.singletonMap(NAME, cartData));

		// When
		Collection<StructuredErrorMessage> actualStructuredErrorMessages = validator.validate(context);


		// Then
		assertThat(actualStructuredErrorMessages).size().isEqualTo(1);

		StructuredErrorMessage actualStructuredErrorMessage = actualStructuredErrorMessages.iterator().next();
		String expectedDebugMessage = String.format("'%s' value must contain between 0 and %d characters.", fieldName, MAX_SIZE);

		assertThat(actualStructuredErrorMessage.getData().get("field-name")).isEqualTo(fieldName);
		assertThat(actualStructuredErrorMessage.getDebugMessage()).isEqualTo(expectedDebugMessage);
		assertThat(actualStructuredErrorMessage.getMessageId()).isEqualTo("field.invalid.size");
	}

	@Test
	public void testModifierCorrect() {
		// when
		Map<String, String> idMap = new HashMap<>();
		idMap.put("{field.invalid.size}", "field.invalid.size");

		constraintViolationTransformer.setIdMap(idMap);

		String fieldName = NAME;
		String fieldValue = "123";
		CartData cartData = new CartData(fieldName, fieldValue);

		Map<String, String> itemsToValidate = new HashMap<>();
		itemsToValidate.put(fieldName, fieldValue);

		Set<ModifierField> referentFields = new HashSet<>();

		when(referentField.getCode()).thenReturn(fieldName);
		when(referentField.getFieldType()).thenReturn(ModifierType.SHORT_TEXT);
		when(referentField.isRequired()).thenReturn(true);
		when(referentField.getMaxSize()).thenReturn(MAX_SIZE);

		referentFields.add(referentField);
		when(modifierGroup.getModifierFields()).thenReturn(referentFields);
		when(shoppingCart.getCartData()).thenReturn(Collections.singletonMap(NAME, cartData));

		// When
		Collection<StructuredErrorMessage> actualStructuredErrorMessages = validator.validate(context);

		// Then
		assertThat(actualStructuredErrorMessages).isEmpty();

	}
}

