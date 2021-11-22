/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.context.builders.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.elasticpath.common.pricing.service.PriceLookupFacade;
import com.elasticpath.domain.store.Store;
import com.elasticpath.xpf.connectivity.context.XPFProductSkuValidationContext;
import com.elasticpath.xpf.connectivity.context.XPFShoppingItemValidationContext;
import com.elasticpath.xpf.connectivity.entity.XPFPrice;
import com.elasticpath.xpf.connectivity.entity.XPFProductSku;
import com.elasticpath.xpf.connectivity.entity.XPFShopper;
import com.elasticpath.xpf.context.builders.ProductSkuValidationContextBuilder;
import com.elasticpath.xpf.converters.PriceConverter;
import com.elasticpath.xpf.converters.ProductSkuConverter;
import com.elasticpath.xpf.converters.ShopperConverter;
import com.elasticpath.xpf.converters.StoreDomainContext;
import com.elasticpath.xpf.converters.XPFConverterUtil;

/**
 * Implementation of {@code com.elasticpath.xpf.context.builders.ProductSkuValidationContextBuilder}.
 */
public class ProductSkuValidationContextBuilderImpl implements ProductSkuValidationContextBuilder {

	private ProductSkuConverter xpfProductSkuConverter;

	private ShopperConverter xpfShopperConverter;

	private PriceConverter xpfPriceConverter;

	private XPFConverterUtil xpfXPFConverterUtil;

	private PriceLookupFacade priceLookupFacade;

	@Override
	public XPFProductSkuValidationContext build(
			final com.elasticpath.domain.catalog.ProductSku productSku,
			final com.elasticpath.domain.catalog.ProductSku parentProductSku,
			final com.elasticpath.domain.shopper.Shopper shopper,
			final Store store) {

		Objects.requireNonNull(productSku,
				"Required field productSku for building XPFProductSkuValidationContext is missing.");

		Objects.requireNonNull(store,
				"Required field store for building XPFProductSkuValidationContext is missing.");

		Objects.requireNonNull(shopper,
				"Required field shopper for building XPFProductSkuValidationContext is missing.");

		XPFProductSku contextProductSku = xpfProductSkuConverter.convert(new StoreDomainContext<>(productSku, store));
		XPFProductSku contextParentProductSku = parentProductSku == null ? null
				: xpfProductSkuConverter.convert(new StoreDomainContext<>(parentProductSku, store));
		XPFShopper contextShopper = xpfShopperConverter.convert(shopper);

		com.elasticpath.domain.catalog.Price price = priceLookupFacade.getPromotedPriceForSku(productSku, store, shopper);
		XPFPrice contextPromotedPrice = xpfPriceConverter.convert(price);

		List<XPFProductSkuValidationContext> children =
				xpfXPFConverterUtil.getProductConstituentsAsValidationContexts(productSku, shopper, store);

		boolean inStoreCatalog = productSku.getProduct().isInCatalog(store.getCatalog());

		return new XPFProductSkuValidationContext(
				contextProductSku,
				contextParentProductSku,
				contextShopper,
				contextPromotedPrice,
				inStoreCatalog,
				children);
	}

	@Override
	public XPFProductSkuValidationContext build(final XPFShoppingItemValidationContext context) {
		List<XPFProductSkuValidationContext> productSkuContexts = context.getChildren().stream()
				.map(this::build)
				.collect(Collectors.toList());

		XPFProductSku xpfParentProductSku = context.getParentShoppingItem() == null ? null : context.getParentShoppingItem().getProductSku();

		return new XPFProductSkuValidationContext(
				context.getShoppingItem().getProductSku(),
				xpfParentProductSku,
				context.getShoppingCart().getShopper(),
				context.getPromotedPrice(),
				context.isInStoreCatalog(),
				productSkuContexts);
	}

	public void setXpfProductSkuConverter(final ProductSkuConverter xpfProductSkuConverter) {
		this.xpfProductSkuConverter = xpfProductSkuConverter;
	}

	public void setXpfShopperConverter(final ShopperConverter xpfShopperConverter) {
		this.xpfShopperConverter = xpfShopperConverter;
	}

	public void setXpfPriceConverter(final PriceConverter xpfPriceConverter) {
		this.xpfPriceConverter = xpfPriceConverter;
	}

	public void setXpfXPFConverterUtil(final XPFConverterUtil xpfXPFConverterUtil) {
		this.xpfXPFConverterUtil = xpfXPFConverterUtil;
	}

	public void setPriceLookupFacade(final PriceLookupFacade priceLookupFacade) {
		this.priceLookupFacade = priceLookupFacade;
	}
}
