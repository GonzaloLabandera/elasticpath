/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.xpf.connectivity.entity.XPFProductSku;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingItem;

/**
 * Converts {@code com.elasticpath.common.dto.ShoppingItemDto} to {@code com.elasticpath.xpf.connectivity.context.ShoppingItem}.
 */
public class ShoppingItemDtoConverter implements Converter<StoreDomainContext<ShoppingItemDto>, XPFShoppingItem> {

	private ProductSkuLookup productSkuLookup;
	private ProductSkuConverter productSkuConverter;

	@Override
	public XPFShoppingItem convert(final StoreDomainContext<ShoppingItemDto> shoppingItemStoreDomainContext) {
		ShoppingItemDto shoppingItemDto = shoppingItemStoreDomainContext.getDomain();
		Optional<Store> store = shoppingItemStoreDomainContext.getStore();
		List<XPFShoppingItem> xpfShoppingItems =
				shoppingItemDto.getConstituents().stream()
						.filter(ShoppingItemDto::isSelected)
						.map(shoppingItemDtoConstituent -> this.convert(new StoreDomainContext<>(shoppingItemDtoConstituent, store)))
						.collect(Collectors.toList());

		ProductSku productSku = productSkuLookup.findBySkuCode(shoppingItemDto.getSkuCode());
		XPFProductSku xpfProductSku = productSkuConverter.convert(new StoreDomainContext<>(productSku, Optional.empty()));

		return new XPFShoppingItem(
				shoppingItemDto.getGuid(),
				xpfProductSku,
				xpfShoppingItems,
				shoppingItemDto.getQuantity(),
				shoppingItemDto.getItemFields());
	}

	public void setProductSkuLookup(final ProductSkuLookup productSkuLookup) {
		this.productSkuLookup = productSkuLookup;
	}

	public void setProductSkuConverter(final ProductSkuConverter productSkuConverter) {
		this.productSkuConverter = productSkuConverter;
	}
}
