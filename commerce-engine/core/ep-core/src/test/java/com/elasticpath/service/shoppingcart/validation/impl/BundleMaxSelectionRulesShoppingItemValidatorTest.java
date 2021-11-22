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
public class BundleMaxSelectionRulesShoppingItemValidatorTest {
	private static final String SKU_CODE = "skuCode";

	@InjectMocks
	private BundleMaxSelectionRulesShoppingItemValidatorImpl validator;

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
		given(productBundle.getMaxConstituentSelections()).willReturn(1L);
	}

	@Test
	public void testBundleConstituentsCorrect() {
		// Given
		given(bundleShoppingItem.getChildren()).willReturn(Collections.singletonList(constituentShoppingItem1));

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).isEmpty();
	}

	@Test
	public void testBundleConstituentsMoreThanMax() {
		XPFStructuredErrorMessage structuredErrorMessage = new XPFStructuredErrorMessage(XPFStructuredErrorMessageType.NEEDINFO,
				"bundle.exceeds.max.constituents",
				"Bundle contains more than the maximum number of allowed bundle constituents.",
				ImmutableMap.of("item-code", SKU_CODE,
						"max-quantity", "1",
						"current-quantity", "2"
				));

		// Given
		given(bundleShoppingItem.getChildren()).willReturn(Arrays.asList(constituentShoppingItem1, constituentShoppingItem2));

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).containsOnly(structuredErrorMessage);
	}
}
