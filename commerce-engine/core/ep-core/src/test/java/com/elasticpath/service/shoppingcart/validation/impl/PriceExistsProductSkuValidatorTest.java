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

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.catalog.impl.BundleIdentifierImpl;
import com.elasticpath.xpf.connectivity.context.XPFProductSkuValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.entity.XPFPrice;
import com.elasticpath.xpf.connectivity.entity.XPFProductSku;

@RunWith(MockitoJUnitRunner.class)
public class PriceExistsProductSkuValidatorTest {

	private static final String SKU_CODE = "sku_code";
	private static final String PARENT_SKU_CODE = "parent_sku_code";

	@InjectMocks
	private PriceExistsProductSkuValidatorImpl validator;

	@Mock
	private XPFProductSkuValidationContext context;

	@Mock
	private Product product;

	@Mock
	private ProductBundle calculatedProductBundle;

	@Mock
	private ProductSku productSku;

	@Mock
	private ProductSku parentProductSku;

	@Mock
	private XPFProductSku contextProductSku;

	@Mock
	private XPFProductSku contextParentProductSku;

	@Mock
	private XPFPrice price;

	@Mock
	private ProductSkuLookup productSkuLookup;

	@Before
	public void setUp() {

		given(context.getProductSku()).willReturn(contextProductSku);
		given(context.getParentProductSku()).willReturn(contextParentProductSku);

		given(contextProductSku.getCode()).willReturn(SKU_CODE);
		given(contextParentProductSku.getCode()).willReturn(PARENT_SKU_CODE);

		given(productSkuLookup.findBySkuCode(SKU_CODE)).willReturn(productSku);
		given(productSkuLookup.findBySkuCode(PARENT_SKU_CODE)).willReturn(parentProductSku);

		given(calculatedProductBundle.isCalculated()).willReturn(true);

		validator.setBundleIdentifier(new BundleIdentifierImpl());
	}

	@Test
	public void testProductPriceExist() {
		// Given
		given(productSku.getProduct()).willReturn(product);
		given(context.getPromotedPrice()).willReturn(price);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Than
		assertThat(messageCollections).isEmpty();
	}

	@Test
	public void testProductPriceDoesNotExist() {
		XPFStructuredErrorMessage structuredErrorMessage = new XPFStructuredErrorMessage("item.missing.price",
				String.format("Item '%s' does not have a price.", SKU_CODE),
				ImmutableMap.of("item-code", SKU_CODE));
		// Given
		given(productSku.getProduct()).willReturn(product);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).containsOnly(structuredErrorMessage);
	}

	@Test
	public void testProductPriceDoesNotExistForCalculatedBundle() {
		// Given
		given(productSku.getProduct()).willReturn(calculatedProductBundle);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).isEmpty();
	}

	@Test
	public void testProductPriceDoesNotExistForBundleWithParentAssignedBundle() {
		// Given
		given(context.getParentProductSku()).willReturn(contextParentProductSku);
		given(productSku.getProduct()).willReturn(calculatedProductBundle);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).isEmpty();
	}
}
