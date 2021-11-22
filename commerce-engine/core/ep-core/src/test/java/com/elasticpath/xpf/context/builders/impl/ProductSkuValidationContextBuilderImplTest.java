/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.context.builders.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.common.pricing.service.PriceLookupFacade;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Price;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.store.Store;
import com.elasticpath.xpf.connectivity.context.XPFProductSkuValidationContext;
import com.elasticpath.xpf.connectivity.entity.XPFPrice;
import com.elasticpath.xpf.connectivity.entity.XPFProductSku;
import com.elasticpath.xpf.connectivity.entity.XPFShopper;
import com.elasticpath.xpf.converters.PriceConverter;
import com.elasticpath.xpf.converters.ProductSkuConverter;
import com.elasticpath.xpf.converters.ShopperConverter;
import com.elasticpath.xpf.converters.StoreDomainContext;
import com.elasticpath.xpf.converters.XPFConverterUtil;

@RunWith(MockitoJUnitRunner.class)
public class ProductSkuValidationContextBuilderImplTest {
	@Mock
	private ProductSku productSku;
	@Mock
	private XPFProductSku contextProductSku;
	@Mock
	private ProductSku parentProductSku;
	@Mock
	private XPFProductSku contextParentProductSku;
	@Mock
	private Shopper shopper;
	@Mock
	private XPFShopper contextShopper;
	@Mock
	private Price promotedPrice;
	@Mock
	private XPFPrice contextPromotedPrice;
	@Mock
	private Store store;
	@Mock
	private List<XPFProductSkuValidationContext> children;
	@Mock
	private Product product;
	@Mock
	private Catalog catalog;
	@Mock
	private ProductSkuConverter productSkuConverter;
	@Mock
	private ShopperConverter shopperConverter;
	@Mock
	private PriceConverter priceConverter;
	@Mock
	private XPFConverterUtil xpfConverterUtil;
	@Mock
	private PriceLookupFacade priceLookupFacade;
	@InjectMocks
	private ProductSkuValidationContextBuilderImpl productSkuValidationContextBuilder;

	@Rule
	public ExpectedException exceptionThrown = ExpectedException.none();

	@Test
	public void testBuildWithFullInputs() {
		when(productSkuConverter.convert(new StoreDomainContext<>(productSku, store))).thenReturn(contextProductSku);
		when(productSkuConverter.convert(new StoreDomainContext<>(parentProductSku, store))).thenReturn(contextParentProductSku);
		when(shopperConverter.convert(shopper)).thenReturn(contextShopper);
		when(priceLookupFacade.getPromotedPriceForSku(productSku, store, shopper)).thenReturn(promotedPrice);
		when(priceConverter.convert(promotedPrice)).thenReturn(contextPromotedPrice);
		when(xpfConverterUtil.getProductConstituentsAsValidationContexts(productSku, shopper, store)).thenReturn(children);

		when(productSku.getProduct()).thenReturn(product);
		when(store.getCatalog()).thenReturn(catalog);
		when(product.isInCatalog(catalog)).thenReturn(true);

		XPFProductSkuValidationContext productSkuValidationContext =
				productSkuValidationContextBuilder.build(productSku, parentProductSku, shopper, store);

		assertEquals(contextProductSku, productSkuValidationContext.getProductSku());
		assertEquals(contextParentProductSku, productSkuValidationContext.getParentProductSku());
		assertEquals(contextShopper, productSkuValidationContext.getShopper());
		assertEquals(contextPromotedPrice, productSkuValidationContext.getPromotedPrice());
		assertEquals(children, productSkuValidationContext.getChildren());
		assertTrue(productSkuValidationContext.isInStoreCatalog());
	}

	@Test
	public void testBuildWithMinInputs() {
		when(productSkuConverter.convert(new StoreDomainContext<>(productSku, store))).thenReturn(contextProductSku);
		when(shopperConverter.convert(shopper)).thenReturn(contextShopper);
		when(priceLookupFacade.getPromotedPriceForSku(productSku, store, shopper)).thenReturn(null);
		when(xpfConverterUtil.getProductConstituentsAsValidationContexts(productSku, shopper, store)).thenReturn(Collections.emptyList());

		when(productSku.getProduct()).thenReturn(product);
		when(store.getCatalog()).thenReturn(catalog);
		when(product.isInCatalog(catalog)).thenReturn(true);

		XPFProductSkuValidationContext productSkuValidationContext =
				productSkuValidationContextBuilder.build(productSku, null, shopper, store);

		assertEquals(contextProductSku, productSkuValidationContext.getProductSku());
		assertNull(productSkuValidationContext.getParentProductSku());
		assertEquals(contextShopper, productSkuValidationContext.getShopper());
		assertNull(productSkuValidationContext.getPromotedPrice());
		assertTrue(productSkuValidationContext.getChildren().isEmpty());
		assertTrue(productSkuValidationContext.isInStoreCatalog());
	}

	@Test
	public void testBuildWithMissingRequiredProductSku() {
		exceptionThrown.expect(NullPointerException.class);
		exceptionThrown.expectMessage("Required field productSku for building XPFProductSkuValidationContext is missing.");

		productSkuValidationContextBuilder.build(null, null, shopper, store);
	}

	@Test
	public void testBuildWithMissingRequiredStore() {
		exceptionThrown.expect(NullPointerException.class);
		exceptionThrown.expectMessage("Required field store for building XPFProductSkuValidationContext is missing.");

		productSkuValidationContextBuilder.build(productSku, null, shopper, null);
	}

	@Test
	public void testBuildWithMissingRequiredShopper() {
		exceptionThrown.expect(NullPointerException.class);
		exceptionThrown.expectMessage("Required field shopper for building XPFProductSkuValidationContext is missing.");

		productSkuValidationContextBuilder.build(productSku, null, null, store);
	}
}