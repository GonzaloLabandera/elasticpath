/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.xpf.connectivity.entity.XPFProductSku;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingItem;

@RunWith(MockitoJUnitRunner.class)
public class ShoppingItemDtoConverterTest {
	@Mock
	private ShoppingItemDto shoppingItemDto, childShoppingItemDto;
	@Mock
	private Map<String, String> itemFieldsMap, childItemFieldsMap;
	@Mock
	private XPFProductSku xpfProductSku;
	@Mock
	private ProductSku productSku;
	@Mock
	private ProductSkuConverter productSkuConverter;
	@Mock
	private ProductSkuLookup productSkuLookup;

	@InjectMocks
	private ShoppingItemDtoConverter shoppingItemDtoConverter;

	@Test
	public void testConvert() {
		String skuCode = "skuCode";
		String childSkuCode = "childSkuCode";
		Long quantity = 1L;
		Long childQuantity = 1L;
		when(shoppingItemDto.getConstituents()).thenReturn(Collections.singletonList(childShoppingItemDto));
		when(shoppingItemDto.getSkuCode()).thenReturn(skuCode);
		when(shoppingItemDto.getQuantity()).thenReturn(quantity.intValue());
		when(shoppingItemDto.getItemFields()).thenReturn(itemFieldsMap);

		when(childShoppingItemDto.getConstituents()).thenReturn(Collections.emptyList());
		when(childShoppingItemDto.getSkuCode()).thenReturn(childSkuCode);
		when(childShoppingItemDto.getQuantity()).thenReturn(childQuantity.intValue());
		when(childShoppingItemDto.getItemFields()).thenReturn(childItemFieldsMap);
		when(childShoppingItemDto.isSelected()).thenReturn(true);
		when(productSkuLookup.findBySkuCode(skuCode)).thenReturn(productSku);
		when(productSkuLookup.findBySkuCode(childSkuCode)).thenReturn(productSku);
		when(productSkuConverter.convert(new StoreDomainContext<>(productSku, Optional.empty()))).thenReturn(xpfProductSku);

		XPFShoppingItem contextShoppingItem = shoppingItemDtoConverter.convert(new StoreDomainContext<>(shoppingItemDto, Optional.empty()));

		// Test child ShoppingItemDto conversion
		XPFShoppingItem contextChildShoppingItem = contextShoppingItem.getChildren().get(0);
		assertEquals(xpfProductSku, contextChildShoppingItem.getProductSku());
		assertEquals(childQuantity.longValue(), contextChildShoppingItem.getQuantity());
		assertEquals(childItemFieldsMap, contextChildShoppingItem.getModifierFields());

		// Test ShoppingItemDto conversion
		assertEquals(xpfProductSku, contextShoppingItem.getProductSku());
		assertEquals(quantity.longValue(), contextShoppingItem.getQuantity());
		assertEquals(itemFieldsMap, contextShoppingItem.getModifierFields());
	}
}