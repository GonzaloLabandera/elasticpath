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

import com.elasticpath.xpf.connectivity.context.XPFProductSkuValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.entity.XPFProduct;
import com.elasticpath.xpf.connectivity.entity.XPFProductSku;

@RunWith(MockitoJUnitRunner.class)
public class SoldSeparatelyProductSkuValidatorTest {

	private static final String SKU_CODE = "sku_code";

	@InjectMocks
	private SoldSeparatelyProductSkuValidatorImpl validator;

	@Mock
	private XPFProductSkuValidationContext context;

	@Mock
	private XPFProduct product;

	@Mock
	private XPFProductSku productSku;

	@Before
	public void setUp() {
		given(productSku.getProduct()).willReturn(product);
		given(productSku.getCode()).willReturn(SKU_CODE);
		given(context.getProductSku()).willReturn(productSku);
	}

	@Test
	public void testProductIsNotSoldSeparately() {
		XPFStructuredErrorMessage structuredErrorMessage = new XPFStructuredErrorMessage("item.not.sold.separately",
				String.format("Item '%s' is not sold separately.", SKU_CODE),
				ImmutableMap.of("item-code", SKU_CODE));

		// Given
		given(product.isNotSoldSeparately()).willReturn(true);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).containsOnly(structuredErrorMessage);
	}

	@Test
	public void testProductIsSoldSeparately() {

		// Given
		given(product.isNotSoldSeparately()).willReturn(false);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).isEmpty();
	}

}
