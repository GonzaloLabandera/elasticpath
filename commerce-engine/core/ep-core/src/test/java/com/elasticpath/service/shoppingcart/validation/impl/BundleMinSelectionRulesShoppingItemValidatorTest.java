/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Arrays;
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
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessageType;
import com.elasticpath.xpf.connectivity.entity.XPFProductBundle;
import com.elasticpath.xpf.connectivity.entity.XPFProductSku;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingItem;

@RunWith(MockitoJUnitRunner.class)
public class BundleMinSelectionRulesShoppingItemValidatorTest {

	private static final String SKU_CODE = "skuCode";

	@InjectMocks
	private BundleMinSelectionRulesShoppingItemValidatorImpl validator;

	@Mock
	private XPFShoppingItemValidationContext context;

	@Mock
	private XPFProductSku productSku;

	@Mock
	private XPFShoppingItem bundleShoppingItem;

	@Mock
	private XPFShoppingItem constituentShoppingItem1;

	@Mock
	private XPFShoppingItem constituentShoppingItem2;

	@Mock
	private XPFProductBundle productBundle;

	@Before
	public void setUp() {
		given(context.getShoppingItem()).willReturn(bundleShoppingItem);
		given(bundleShoppingItem.getProductSku()).willReturn(productSku);
		given(productSku.getProduct()).willReturn(productBundle);
		given(productSku.getCode()).willReturn(SKU_CODE);
		given(productBundle.getMinConstituentSelections()).willReturn(2L);
		given(context.getShoppingItem()).willReturn(bundleShoppingItem);
	}

	@Test
	public void testBundleConstituentsCorrect() {

		// Given
		given(bundleShoppingItem.getChildren()).willReturn(Arrays.asList(constituentShoppingItem1, constituentShoppingItem2));

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).isEmpty();
	}

	@Test
	public void testBundleConstituentsLessThanMin() {
		XPFStructuredErrorMessage structuredErrorMessage = new XPFStructuredErrorMessage(XPFStructuredErrorMessageType.NEEDINFO,
				"bundle.does.not.contain.min.constituents",
				"Bundle does not contain the minimum number of required bundle constituents.",
				ImmutableMap.of("item-code", SKU_CODE,
						"min-quantity", "2",
						"current-quantity", "1"
				));

		// Given
		given(bundleShoppingItem.getChildren()).willReturn(Collections.singletonList(constituentShoppingItem1));

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).containsOnly(structuredErrorMessage);
	}
}
