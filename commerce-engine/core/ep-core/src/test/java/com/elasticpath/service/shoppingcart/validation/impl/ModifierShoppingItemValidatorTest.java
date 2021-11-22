/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.domain.modifier.ModifierType;
import com.elasticpath.validation.impl.ConstraintViolationTransformerImpl;
import com.elasticpath.validation.service.impl.ModifierFieldValidationServiceImpl;
import com.elasticpath.xpf.connectivity.context.XPFShoppingItemValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.entity.XPFModifierField;
import com.elasticpath.xpf.connectivity.entity.XPFModifierGroup;
import com.elasticpath.xpf.connectivity.entity.XPFProduct;
import com.elasticpath.xpf.connectivity.entity.XPFProductSku;
import com.elasticpath.xpf.connectivity.entity.XPFProductType;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingItem;
import com.elasticpath.xpf.converters.XPFStructuredErrorMessageConverter;

@RunWith(MockitoJUnitRunner.class)
public class ModifierShoppingItemValidatorTest {

	private static final int MEDIUM_MAX_SIZE = 10;
	private static final String FIELD_INVALID_DECIMAL_FORMAT = "field.invalid.decimal.format";

	@InjectMocks
	private ModifierShoppingItemValidatorImpl validator;
	@Spy
	private final ModifierFieldValidationServiceImpl modifierFieldValidationService = new ModifierFieldValidationServiceImpl();

	private final ConstraintViolationTransformerImpl constraintViolationTransformer = new ConstraintViolationTransformerImpl();

	@Mock
	private XPFShoppingItemValidationContext context;

	@Mock
	private XPFProductSku xpfProductSku;

	@Mock
	private XPFProduct xpfProduct;

	@Mock
	private XPFProductType xpfProductType;

	@Mock
	private XPFModifierGroup xpfModifierGroup;

	@Mock
	private XPFShoppingItem shoppingItem;

	@Mock
	private XPFModifierField referentField;

	@Mock
	private XPFStructuredErrorMessageConverter xpfStructuredErrorMessageConverter;

	@Spy
	private CartModifierValidator cartModifierValidator;

	@Before
	public void setUp() {

		given(shoppingItem.getProductSku()).willReturn(xpfProductSku);
		given(xpfProductSku.getProduct()).willReturn(xpfProduct);
		given(context.getShoppingItem()).willReturn(shoppingItem);

		given(xpfProduct.getProductType()).willReturn(xpfProductType);
		given(xpfProductType.getModifierGroups()).willReturn(ImmutableSet.of(xpfModifierGroup));

		modifierFieldValidationService.setConstraintViolationTransformer(constraintViolationTransformer);
		cartModifierValidator.setModifierFieldValidationService(modifierFieldValidationService);
	}

	@Test
	public void testItemModifierIncorrect() {

		// Given
		Map<String, String> idMap = new HashMap<>();
		idMap.put("{field.invalid.decimal.format}", FIELD_INVALID_DECIMAL_FORMAT);

		constraintViolationTransformer.setIdMap(idMap);

		String decimalFieldName = "amount";
		String fieldValue = "123abc";

		Map<String, String> itemsToValidate = new HashMap<>();
		itemsToValidate.put(decimalFieldName, fieldValue);

		List<XPFModifierField> referentFields = new ArrayList<>();

		given(referentField.getCode()).willReturn(decimalFieldName);
		given(referentField.getModifierType()).willReturn(ModifierType.DECIMAL.getName());
		given(referentField.isRequired()).willReturn(true);
		given(referentField.getMaxSize()).willReturn(MEDIUM_MAX_SIZE);

		referentFields.add(referentField);
		given(xpfModifierGroup.getModifierFields()).willReturn(referentFields);

		given(shoppingItem.getModifierFields()).willReturn(itemsToValidate);


		String expectedDebugMessage = String.format("'%s' value '%s' must be a decimal.", decimalFieldName, fieldValue);

		Map<String, String> expectedData = new HashMap<>();
		expectedData.put("field-name", "amount");
		expectedData.put("invalid-value", "123abc");
		XPFStructuredErrorMessage expectedXPFStructuredErrorMessage =
				new XPFStructuredErrorMessage(FIELD_INVALID_DECIMAL_FORMAT, expectedDebugMessage, expectedData);
		StructuredErrorMessage expectedStructuredErrorMessage =
				new StructuredErrorMessage(FIELD_INVALID_DECIMAL_FORMAT, expectedDebugMessage, expectedData);

		given(xpfStructuredErrorMessageConverter.convert(expectedStructuredErrorMessage)).willReturn(expectedXPFStructuredErrorMessage);

		// When
		Collection<XPFStructuredErrorMessage> actualStructuredErrorMessages = validator.validate(context);


		// Then
		assertThat(actualStructuredErrorMessages).size().isEqualTo(1);

		XPFStructuredErrorMessage actualStructuredErrorMessage = actualStructuredErrorMessages.iterator().next();

		assertThat(actualStructuredErrorMessage.getData().get("field-name")).isEqualTo(decimalFieldName);
		assertThat(actualStructuredErrorMessage.getDebugMessage()).isEqualTo(expectedDebugMessage);
		assertThat(actualStructuredErrorMessage.getMessageId()).isEqualTo(FIELD_INVALID_DECIMAL_FORMAT);
	}

	@Test
	public void testItemModifierCorrect() {
		// Given
		Map<String, String> idMap = new HashMap<>();
		idMap.put("{field.invalid.decimal.format}", FIELD_INVALID_DECIMAL_FORMAT);

		constraintViolationTransformer.setIdMap(idMap);

		String decimalFieldName = "amount";
		String fieldValue = "123";

		Map<String, String> itemsToValidate = new HashMap<>();
		itemsToValidate.put(decimalFieldName, fieldValue);

		List<XPFModifierField> referentFields = new ArrayList<>();

		given(referentField.getCode()).willReturn(decimalFieldName);
		given(referentField.getModifierType()).willReturn(ModifierType.DECIMAL.getName());
		given(referentField.isRequired()).willReturn(true);
		given(referentField.getMaxSize()).willReturn(MEDIUM_MAX_SIZE);

		referentFields.add(referentField);
		given(xpfModifierGroup.getModifierFields()).willReturn(referentFields);

		given(shoppingItem.getModifierFields()).willReturn(itemsToValidate);


		// When
		Collection<XPFStructuredErrorMessage> actualStructuredErrorMessages = validator.validate(context);


		// Then
		assertThat(actualStructuredErrorMessages).isEmpty();

	}
}

