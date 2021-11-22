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

import com.elasticpath.domain.catalog.impl.ProductSkuImpl;
import com.elasticpath.domain.misc.types.ModifierFieldsMapWrapper;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.xpf.connectivity.entity.XPFProductSku;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingItem;

@RunWith(MockitoJUnitRunner.class)
public class ShoppingItemConverterTest {
	@Mock
	private ShoppingItem shoppingItem, childShoppingItem;
	@Mock
	private ProductSkuLookup productSkuLookup;
	@Mock
	private ProductSkuConverter productSkuConverter;
	@Mock
	private ProductSkuImpl productSku, childProductSku;
	@Mock
	private ModifierFieldsMapWrapper modifierFieldsMapWrapper, childModifierFieldsMapWrapper;
	@Mock
	private Map<String, String> modifierFieldsMap, childModifierFieldMap;
	@Mock
	private XPFProductSku xpfParentProductSku;
	@Mock
	private XPFProductSku xpfChildProductSku;

	@InjectMocks
	private ShoppingItemConverter shoppingItemConverter;

	@Test
	public void testConvert() {
		String skuGuid = "skuGuid";
		String shoppingItemGuid = "shoppingItemGuid";
		String childSkuGuid = "childSkuGuid";
		Long quantity = 1L;
		Long childQuantity = 1L;
		when(shoppingItem.getChildren()).thenReturn(Collections.singletonList(childShoppingItem));
		when(shoppingItem.getSkuGuid()).thenReturn(skuGuid);
		when(shoppingItem.getGuid()).thenReturn(shoppingItemGuid);
		when(productSkuLookup.findByGuid(skuGuid)).thenReturn(productSku);
		when(shoppingItem.getQuantity()).thenReturn(quantity.intValue());
		when(shoppingItem.getModifierFields()).thenReturn(modifierFieldsMapWrapper);
		when(modifierFieldsMapWrapper.getMap()).thenReturn(modifierFieldsMap);

		when(childShoppingItem.getChildren()).thenReturn(Collections.emptyList());
		when(childShoppingItem.getSkuGuid()).thenReturn(childSkuGuid);
		when(productSkuLookup.findByGuid(childSkuGuid)).thenReturn(childProductSku);
		when(childShoppingItem.getQuantity()).thenReturn(childQuantity.intValue());
		when(childShoppingItem.getModifierFields()).thenReturn(childModifierFieldsMapWrapper);
		when(childModifierFieldsMapWrapper.getMap()).thenReturn(childModifierFieldMap);
		when(productSkuConverter.convert(new StoreDomainContext<>(childProductSku, Optional.empty()))).thenReturn(xpfParentProductSku);
		when(productSkuConverter.convert(new StoreDomainContext<>(productSku, Optional.empty()))).thenReturn(xpfChildProductSku);

		XPFShoppingItem contextShoppingItem = shoppingItemConverter.convert(new StoreDomainContext<>(shoppingItem, Optional.empty()));

		// Test child ShoppingItem conversion
		XPFShoppingItem contextChildShoppingItem = contextShoppingItem.getChildren().get(0);
		assertEquals(xpfParentProductSku, contextChildShoppingItem.getProductSku());
		assertEquals(childQuantity.longValue(), contextChildShoppingItem.getQuantity());
		assertEquals(childModifierFieldMap, contextChildShoppingItem.getModifierFields());

		// Test ShoppingItem conversion
		assertEquals(xpfChildProductSku, contextShoppingItem.getProductSku());
		assertEquals(quantity.longValue(), contextShoppingItem.getQuantity());
		assertEquals(modifierFieldsMap, contextShoppingItem.getModifierFields());
	}
}