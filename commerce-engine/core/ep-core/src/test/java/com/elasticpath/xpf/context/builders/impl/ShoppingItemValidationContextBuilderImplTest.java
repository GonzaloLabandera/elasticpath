/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.context.builders.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.common.pricing.service.PriceLookupFacade;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Price;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.xpf.connectivity.context.XPFShoppingItemValidationContext;
import com.elasticpath.xpf.connectivity.entity.XPFOperationEnum;
import com.elasticpath.xpf.connectivity.entity.XPFPrice;
import com.elasticpath.xpf.connectivity.entity.XPFProductSku;
import com.elasticpath.xpf.connectivity.entity.XPFShopper;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingCart;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingItem;
import com.elasticpath.xpf.converters.PriceConverter;
import com.elasticpath.xpf.converters.ShoppingCartConverter;
import com.elasticpath.xpf.converters.ShoppingItemConverter;
import com.elasticpath.xpf.converters.ShoppingItemDtoConverter;
import com.elasticpath.xpf.converters.StoreDomainContext;

@SuppressWarnings("PMD.TooManyFields")
@RunWith(MockitoJUnitRunner.class)
public class ShoppingItemValidationContextBuilderImplTest {
	@Mock
	private ShoppingCart shoppingCart;
	@Mock
	private XPFShoppingCart contextShoppingCart;
	@Mock
	private ShoppingItemDto shoppingItemDto;
	@Mock
	private XPFShoppingItem contextShoppingItem;
	@Mock
	private ShoppingItem parentShoppingItem;
	@Mock
	private XPFShoppingItem xpfParentShoppingItem;
	@Mock
	private ProductSku productSku;
	@Mock
	private XPFProductSku contextProductSku;
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
	private XPFShoppingItem childShoppingItem;
	@Mock
	private Product product;
	@Mock
	private Product childProduct;
	@Mock
	private Catalog catalog;
	@Mock
	private PriceConverter priceConverter;
	@Mock
	private ShoppingItemDtoConverter shoppingItemDtoConverter;
	@Mock
	private ShoppingCartConverter shoppingCartConverter;
	@Mock
	private ProductSku childProductSku;
	@Mock
	private XPFProductSku xpfChildProductSku;
	@Mock
	private ProductSkuLookup productSkuLookup;
	@Mock
	private PriceLookupFacade priceLookupFacade;
	@Mock
	private ShoppingItemConverter xpfShoppingItemConverter;

	@InjectMocks
	private ShoppingItemValidationContextBuilderImpl shoppingItemDtoValidationContextBuilder;

	@Rule
	public ExpectedException exceptionThrown = ExpectedException.none();

	private void setupForTestBuildWithFullInputs() {
		String skuCode = "skuCode";
		String childSkuCode = "childSkuCode";
		when(productSkuLookup.findBySkuCode(skuCode)).thenReturn(productSku);
		when(productSkuLookup.findBySkuCode(childSkuCode)).thenReturn(childProductSku);

		when(xpfShoppingItemConverter.convert(new StoreDomainContext<>(parentShoppingItem, store))).thenReturn(xpfParentShoppingItem);
		when(xpfParentShoppingItem.getProductSku()).thenReturn(contextParentProductSku);
		when(contextShoppingCart.getShopper()).thenReturn(contextShopper);

		when(shoppingCart.getStore()).thenReturn(store);
		when(shoppingCart.getShopper()).thenReturn(shopper);
		when(priceLookupFacade.getPromotedPriceForSku(productSku, store, shopper)).thenReturn(promotedPrice);
		when(priceConverter.convert(promotedPrice)).thenReturn(contextPromotedPrice);

		when(shoppingItemDtoConverter.convert(new StoreDomainContext<>(shoppingItemDto, store))).thenReturn(contextShoppingItem);
		when(shoppingCartConverter.convert(shoppingCart)).thenReturn(contextShoppingCart);

		when(childShoppingItem.getProductSku()).thenReturn(xpfChildProductSku);
		when(xpfChildProductSku.getCode()).thenReturn(childSkuCode);
		when(contextShoppingItem.getChildren()).thenReturn(Collections.singletonList(childShoppingItem));
		when(contextShoppingItem.getProductSku()).thenReturn(contextProductSku);
		when(contextShoppingItem.getProductSku().getCode()).thenReturn(skuCode);
		when(productSku.getProduct()).thenReturn(product);
		when(childProductSku.getProduct()).thenReturn(childProduct);
		when(store.getCatalog()).thenReturn(catalog);
		when(product.isInCatalog(catalog)).thenReturn(true);
		when(childProduct.isInCatalog(catalog)).thenReturn(true);
	}

	/**
	 * Test with parent as type ShoppingItem.
	 */
	@Test
	public void testBuildWithParentAsShoppingItemTypeAndFullInputs() {
		setupForTestBuildWithFullInputs();

		XPFShoppingItemValidationContext shoppingItemValidationContext =
				shoppingItemDtoValidationContextBuilder.build(shoppingCart, shoppingItemDto, parentShoppingItem, XPFOperationEnum.UPDATE);

		assertEquals(contextProductSku, shoppingItemValidationContext.getShoppingItem().getProductSku());
		assertEquals(contextParentProductSku, shoppingItemValidationContext.getParentShoppingItem().getProductSku());
		assertEquals(contextShopper, shoppingItemValidationContext.getShoppingCart().getShopper());
		assertEquals(contextPromotedPrice, shoppingItemValidationContext.getPromotedPrice());
		assertEquals(contextShoppingItem, shoppingItemValidationContext.getShoppingItem());
		assertEquals(contextShoppingCart, shoppingItemValidationContext.getShoppingCart());
		assertEquals(XPFOperationEnum.UPDATE, shoppingItemValidationContext.getOperation());
		assertEquals(childShoppingItem, shoppingItemValidationContext.getChildren().get(0).getShoppingItem());
		assertTrue(shoppingItemValidationContext.isInStoreCatalog());
	}

	@Test
	public void testConvertWithParentAsShoppingItemTypeAndMinInputs() {
		setupForTestBuildWithFullInputs();
		XPFShoppingItemValidationContext shoppingItemValidationContext =
				shoppingItemDtoValidationContextBuilder.build(shoppingCart, shoppingItemDto, parentShoppingItem, XPFOperationEnum.UPDATE);

		assertEquals(contextProductSku, shoppingItemValidationContext.getShoppingItem().getProductSku());
		assertEquals(contextParentProductSku, shoppingItemValidationContext.getParentShoppingItem().getProductSku());
		assertEquals(contextShopper, shoppingItemValidationContext.getShoppingCart().getShopper());
		assertEquals(contextPromotedPrice, shoppingItemValidationContext.getPromotedPrice());
		assertEquals(contextShoppingItem, shoppingItemValidationContext.getShoppingItem());
		assertEquals(contextShoppingCart, shoppingItemValidationContext.getShoppingCart());
		assertEquals(XPFOperationEnum.UPDATE, shoppingItemValidationContext.getOperation());
		assertEquals(childShoppingItem, shoppingItemValidationContext.getChildren().get(0).getShoppingItem());
		assertTrue(shoppingItemValidationContext.isInStoreCatalog());
	}

	@Test
	public void testBuildWithParentAsShoppingItemTypeAndMissingRequiredShoppingCart() {
		exceptionThrown.expect(NullPointerException.class);
		exceptionThrown.expectMessage("Required field shoppingCart for building XPFShoppingItemValidationContext is missing.");

		shoppingItemDtoValidationContextBuilder.build(null, shoppingItemDto, parentShoppingItem, null);
	}

	@Test
	public void testGetAllContexts() {
		XPFShoppingItemValidationContext shoppingItemValidationContext = mock(XPFShoppingItemValidationContext.class);
		XPFShoppingItemValidationContext shoppingItemValidationContextChild = mock(XPFShoppingItemValidationContext.class);
		when(shoppingItemValidationContext.getChildren()).thenReturn(Collections.singletonList(shoppingItemValidationContextChild));
		List<XPFShoppingItemValidationContext> allContexts =
				shoppingItemDtoValidationContextBuilder.getAllContextsStream(shoppingItemValidationContext).collect(Collectors.toList());
		assertThat(allContexts).containsExactly(shoppingItemValidationContext, shoppingItemValidationContextChild);
	}
}
