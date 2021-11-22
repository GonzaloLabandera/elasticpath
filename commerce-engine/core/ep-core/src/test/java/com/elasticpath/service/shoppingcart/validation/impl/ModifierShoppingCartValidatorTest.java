/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.domain.modifier.ModifierType;
import com.elasticpath.service.shoppingcart.MulticartItemListTypeLocationProvider;
import com.elasticpath.validation.impl.ConstraintViolationTransformerImpl;
import com.elasticpath.validation.service.impl.ModifierFieldValidationServiceImpl;
import com.elasticpath.xpf.connectivity.context.XPFShoppingCartValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.entity.XPFCartType;
import com.elasticpath.xpf.connectivity.entity.XPFModifierField;
import com.elasticpath.xpf.connectivity.entity.XPFModifierGroup;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingCart;
import com.elasticpath.xpf.connectivity.entity.XPFStore;
import com.elasticpath.xpf.converters.XPFStructuredErrorMessageConverter;

@RunWith(MockitoJUnitRunner.class)
public class ModifierShoppingCartValidatorTest {

	private static final int MAX_SIZE = 10;
	private static final String FIELD_INVALID_SIZE = "field.invalid.size";

	@InjectMocks
	private ModifierShoppingCartValidatorImpl validator;

	@Spy
	private final ModifierFieldValidationServiceImpl validationService = new ModifierFieldValidationServiceImpl();

	private final ConstraintViolationTransformerImpl constraintViolationTransformer = new ConstraintViolationTransformerImpl();

	@Mock
	private XPFShoppingCartValidationContext context;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private XPFShoppingCart xpfShoppingCart;

	@Mock
	private XPFModifierGroup xpfModifierGroup;

	@Mock
	private XPFStore xpfStore;

	@Mock
	private MulticartItemListTypeLocationProvider multicartItemListTypeLocationProvider;

	@Mock
	private XPFCartType xpfCartType;
	@Mock
	private XPFModifierField xpfReferentField;

	@Mock
	private XPFStructuredErrorMessageConverter xpfStructuredErrorMessageConverter;

	@Spy
	private CartModifierValidator cartModifierValidator;

	private static final String NAME = "name";
	private static final String FIELD_CHARACTER = "a";
	private static final String STORE_CODE = "store";
	private static final String CART_TYPE_NAME = "SHOPPING_CART";
	private static final String CART_ORDER_GUID = "cartOrderGuid";

	@Before
	public void setUp() {
		when(context.getShoppingCart()).thenReturn(xpfShoppingCart);
		when(xpfShoppingCart.getShopper().getStore()).thenReturn(xpfStore);
		when(xpfStore.getCartTypes()).thenReturn(Collections.singleton(xpfCartType));
		when(xpfStore.getCode()).thenReturn(STORE_CODE);
		when(xpfCartType.getModifierGroups()).thenReturn(Collections.singleton(xpfModifierGroup));
		when(xpfCartType.getName()).thenReturn(CART_TYPE_NAME);
		when(multicartItemListTypeLocationProvider.getMulticartItemListTypeForStore(STORE_CODE)).thenReturn(CART_TYPE_NAME);
		when(xpfShoppingCart.getCartOrderGuid()).thenReturn(CART_ORDER_GUID);

		validationService.setConstraintViolationTransformer(constraintViolationTransformer);
		cartModifierValidator.setModifierFieldValidationService(validationService);
	}

	@Test
	public void testModifierIncorrect() {

		// when
		Map<String, String> idMap = new HashMap<>();
		idMap.put("{field.invalid.size}", FIELD_INVALID_SIZE);

		constraintViolationTransformer.setIdMap(idMap);

		String fieldName = NAME;
		StringBuilder builder = new StringBuilder(FIELD_CHARACTER);
		for (int i = 0; i < MAX_SIZE + 1; i++) {
			builder.append(FIELD_CHARACTER);
		}
		String fieldValue = builder.toString();

		Map<String, String> cartData = new HashMap<>();
		cartData.put(fieldName, fieldValue);

		List<XPFModifierField> referentFields = new ArrayList<>();

		when(xpfReferentField.getCode()).thenReturn(fieldName);
		when(xpfReferentField.getModifierType()).thenReturn(ModifierType.SHORT_TEXT.getName());
		when(xpfReferentField.isRequired()).thenReturn(true);
		when(xpfReferentField.getMaxSize()).thenReturn(MAX_SIZE);

		referentFields.add(xpfReferentField);
		when(xpfModifierGroup.getModifierFields()).thenReturn(referentFields);

		when(xpfShoppingCart.getModifierFields()).thenReturn(cartData);

		String expectedDebugMessage = String.format("'%s' value must contain between 0 and %d characters.", fieldName, MAX_SIZE);

		Map<String, String> expectedData = new HashMap<>();
		expectedData.put("field-name", "name");
		expectedData.put("min", "0");
		expectedData.put("max", "10");
		expectedData.put("invalid-value", fieldValue);
		XPFStructuredErrorMessage expectedXPFStructuredErrorMessage =
				new XPFStructuredErrorMessage(FIELD_INVALID_SIZE, expectedDebugMessage, expectedData);
		StructuredErrorMessage expectedStructuredErrorMessage =
				new StructuredErrorMessage(FIELD_INVALID_SIZE, expectedDebugMessage, expectedData);

		given(xpfStructuredErrorMessageConverter.convert(expectedStructuredErrorMessage)).willReturn(expectedXPFStructuredErrorMessage);


		// When
		Collection<XPFStructuredErrorMessage> actualStructuredErrorMessages = validator.validate(context);


		// Then
		assertThat(actualStructuredErrorMessages).size().isEqualTo(1);

		XPFStructuredErrorMessage actualStructuredErrorMessage = actualStructuredErrorMessages.iterator().next();

		assertThat(actualStructuredErrorMessage.getData().get("field-name")).isEqualTo(fieldName);
		assertThat(actualStructuredErrorMessage.getDebugMessage()).isEqualTo(expectedDebugMessage);
		assertThat(actualStructuredErrorMessage.getMessageId()).isEqualTo(FIELD_INVALID_SIZE);
	}

	@Test
	public void testModifierCorrect() {
		// when
		Map<String, String> idMap = new HashMap<>();
		idMap.put("{field.invalid.size}", FIELD_INVALID_SIZE);

		constraintViolationTransformer.setIdMap(idMap);

		String fieldName = NAME;
		String fieldValue = "123";

		Map<String, String> cartData = new HashMap<>();
		cartData.put(fieldName, fieldValue);

		Map<String, String> itemsToValidate = new HashMap<>();
		itemsToValidate.put(fieldName, fieldValue);

		List<XPFModifierField> referentFields = new ArrayList<>();

		when(xpfReferentField.getCode()).thenReturn(fieldName);
		when(xpfReferentField.getModifierType()).thenReturn(ModifierType.SHORT_TEXT.getName());
		when(xpfReferentField.isRequired()).thenReturn(true);
		when(xpfReferentField.getMaxSize()).thenReturn(MAX_SIZE);

		referentFields.add(xpfReferentField);
		when(xpfModifierGroup.getModifierFields()).thenReturn(referentFields);

		when(xpfShoppingCart.getModifierFields()).thenReturn(cartData);

		// When
		Collection<XPFStructuredErrorMessage> actualStructuredErrorMessages = validator.validate(context);

		// Then
		assertThat(actualStructuredErrorMessages).isEmpty();

	}
}

