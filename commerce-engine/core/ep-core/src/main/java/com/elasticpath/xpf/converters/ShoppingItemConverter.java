/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.xpf.connectivity.entity.XPFProductSku;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingItem;

/**
 * Converts {@code com.elasticpath.domain.shoppingcart.ShoppingItem} to {@code com.elasticpath.xpf.connectivity.context.ShoppingItem}.
 */
public class ShoppingItemConverter implements Converter<StoreDomainContext<ShoppingItem>, XPFShoppingItem> {

	private ProductSkuLookup productSkuLookup;
	private ProductSkuConverter productSkuConverter;

	@Override
	public XPFShoppingItem convert(final StoreDomainContext<ShoppingItem> shoppingItemStoreDomainContext) {
		ShoppingItem shoppingItem = shoppingItemStoreDomainContext.getDomain();
		Optional<Store> store = shoppingItemStoreDomainContext.getStore();

		// Recurring call to convert method on children
		List<XPFShoppingItem> xpfShoppingItems =
				shoppingItem.getChildren().stream()
						.map(childShoppingItem -> this.convert(new StoreDomainContext<>(childShoppingItem, store)))
						.collect(Collectors.toList());

		ProductSku productSku = productSkuLookup.findByGuid(shoppingItem.getSkuGuid());
		XPFProductSku xpfProductSku = productSkuConverter.convert(new StoreDomainContext<>(productSku, store));

		return new XPFShoppingItem(
				shoppingItem.getGuid(),
				xpfProductSku,
				xpfShoppingItems,
				shoppingItem.getQuantity(),
				shoppingItem.getModifierFields().getMap());
	}

	public void setProductSkuLookup(final ProductSkuLookup productSkuLookup) {
		this.productSkuLookup = productSkuLookup;
	}

	public void setProductSkuConverter(final ProductSkuConverter productSkuConverter) {
		this.productSkuConverter = productSkuConverter;
	}
}
