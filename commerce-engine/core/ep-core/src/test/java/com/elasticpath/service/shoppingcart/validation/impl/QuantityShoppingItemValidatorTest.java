/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Collection;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.xpf.connectivity.context.XPFShoppingItemValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.entity.XPFOperationEnum;
import com.elasticpath.xpf.connectivity.entity.XPFProductSku;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingItem;

@RunWith(MockitoJUnitRunner.class)
public class QuantityShoppingItemValidatorTest {

	private static final String SKU_CODE = "skuCode";

	@InjectMocks
	private QuantityShoppingItemValidatorImpl validator;

	@Mock
	private XPFShoppingItemValidationContext context;

	@Mock
	private XPFShoppingItem shoppingItem;

	@Mock
	private XPFProductSku productSku;

	@Before
	public void setUp() {
		given(context.getShoppingItem()).willReturn(shoppingItem);
		given(context.getShoppingItem().getProductSku()).willReturn(productSku);
		given(productSku.getCode()).willReturn(SKU_CODE);
	}

	@Test
	public void testShoppingItemHasInvalidQuantity() {
		long quantity = 0;

		int minimumQuantity = 1;
		XPFStructuredErrorMessage structuredErrorMessage = new XPFStructuredErrorMessage("field.invalid.minimum.value",
				String.format("'quantity' value '%d' must be greater than or equal to '%d'.", quantity, minimumQuantity),
				ImmutableMap.of("item-code", SKU_CODE,
						"field-name", "quantity",
						"min-value", String.format("%d", minimumQuantity),
						"invalid-value", String.format("%d", quantity)));

		// Given
		given(shoppingItem.getQuantity()).willReturn(quantity);
		given(context.getOperation()).willReturn(XPFOperationEnum.ADD);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).containsOnly(structuredErrorMessage);
	}

	@Test
	public void testShoppingItemHasValidQuantity() {
		// Given
		given(shoppingItem.getQuantity()).willReturn(1L);

		given(context.getOperation()).willReturn(XPFOperationEnum.ADD);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).isEmpty();
	}

	@Test
	public void testShoppingItemHasValidQuantityUpdate() {
		// Given
		given(shoppingItem.getQuantity()).willReturn(0L);
		given(context.getOperation()).willReturn(XPFOperationEnum.UPDATE);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).isEmpty();
	}
}
