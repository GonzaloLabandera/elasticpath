/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.xpf.connectivity.context.XPFShoppingItemValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.entity.XPFBundleConstituent;
import com.elasticpath.xpf.connectivity.entity.XPFProduct;
import com.elasticpath.xpf.connectivity.entity.XPFProductBundle;
import com.elasticpath.xpf.connectivity.entity.XPFProductSku;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingItem;

@RunWith(MockitoJUnitRunner.class)
public class BundleStructureShoppingItemDtoValidatorTest {

	private static final String SKU_CODE = "skuCode";

	@InjectMocks
	private BundleStructureShoppingItemValidatorImpl validator;

	@Mock
	private XPFShoppingItemValidationContext context;

	@Mock
	private XPFProductSku xpfProductSku;

	@Mock
	private XPFShoppingItem xpfParentShoppingItemDto;

	@Mock
	private XPFShoppingItem xpfChildShoppingItemDto;

	@Mock
	private XPFProductBundle xpfProductBundle;

	@Mock
	private XPFBundleConstituent xpfBundleConstituent;

	@Mock
	private XPFProduct xpfProduct;

	@Before
	public void setUp() {
		given(context.getShoppingItem()).willReturn(xpfParentShoppingItemDto);
		given(xpfParentShoppingItemDto.getProductSku()).willReturn(xpfProductSku);
		given(xpfProductSku.getProduct()).willReturn(xpfProductBundle);
		given(xpfProductSku.getCode()).willReturn(SKU_CODE);
		given(xpfProduct.getCode()).willReturn(SKU_CODE);
		given(xpfProductBundle.isBundle()).willReturn(true);
		given(xpfProductBundle.getConstituents()).willReturn(Collections.singletonList(xpfBundleConstituent));
		given(xpfBundleConstituent.getProduct()).willReturn(xpfProduct);
		given(xpfParentShoppingItemDto.getChildren()).willReturn(Collections.singletonList(xpfChildShoppingItemDto));
		given(xpfChildShoppingItemDto.getProductSku()).willReturn(xpfProductSku);
	}

	@Test
	public void testBundleStructureCorrect() {

		// Given
		given(xpfChildShoppingItemDto.getProductSku().getProduct().getCode()).willReturn(SKU_CODE);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).isEmpty();
	}

	@Test
	public void testBundleStructureIncorrect() {
		XPFStructuredErrorMessage structuredErrorMessage = new XPFStructuredErrorMessage("item.invalid.bundle.structure",
				"Requested item configuration does not have a valid bundle structure.",
				ImmutableMap.of("item-code", SKU_CODE));

		// Given
		given(xpfChildShoppingItemDto.getProductSku().getProduct().getCode()).willReturn("skuCode1");

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).containsOnly(structuredErrorMessage);
	}
}