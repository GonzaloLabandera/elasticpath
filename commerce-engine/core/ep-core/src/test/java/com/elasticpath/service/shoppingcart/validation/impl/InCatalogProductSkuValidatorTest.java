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
import com.elasticpath.xpf.connectivity.entity.XPFProductSku;

@RunWith(MockitoJUnitRunner.class)
public class InCatalogProductSkuValidatorTest {

	@InjectMocks
	private InCatalogProductSkuValidatorImpl validator;

	@Mock
	private XPFProductSku productSku;

	@Mock
	private XPFProductSkuValidationContext context;
	private static final String SKU_CODE = "sku_code";

	@Before
	public void setUp() {
		given(context.getProductSku()).willReturn(productSku);
		given(productSku.getCode()).willReturn(SKU_CODE);
	}

	@Test
	public void testProductIsNotInCatalog() {
		XPFStructuredErrorMessage structuredErrorMessage = new XPFStructuredErrorMessage("item.not.in.store.catalog",
				String.format("Item '%s' is not part of the current store's catalog.", SKU_CODE),
				ImmutableMap.of("item-code", SKU_CODE));

		// Given
		given(context.isInStoreCatalog()).willReturn(false);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).containsOnly(structuredErrorMessage);
	}

	@Test
	public void testProductIsInCatalog() {
		// Given
		given(context.isInStoreCatalog()).willReturn(true);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).isEmpty();
	}

}
