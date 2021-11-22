/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.context.builders.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.common.pricing.service.PriceLookupFacade;
import com.elasticpath.domain.catalog.Price;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.xpf.connectivity.context.XPFShoppingItemValidationContext;
import com.elasticpath.xpf.connectivity.entity.XPFOperationEnum;
import com.elasticpath.xpf.connectivity.entity.XPFPrice;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingCart;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingItem;
import com.elasticpath.xpf.context.builders.ShoppingItemValidationContextBuilder;
import com.elasticpath.xpf.converters.PriceConverter;
import com.elasticpath.xpf.converters.ProductSkuConverter;
import com.elasticpath.xpf.converters.ShopperConverter;
import com.elasticpath.xpf.converters.ShoppingCartConverter;
import com.elasticpath.xpf.converters.ShoppingItemConverter;
import com.elasticpath.xpf.converters.ShoppingItemDtoConverter;
import com.elasticpath.xpf.converters.StoreDomainContext;
import com.elasticpath.xpf.converters.XPFConverterUtil;

/**
 * Implementation of {@code com.elasticpath.xpf.context.builders.ShoppingItemDtoValidationContextBuilder}.
 */
public class ShoppingItemValidationContextBuilderImpl implements ShoppingItemValidationContextBuilder {

	private ProductSkuConverter xpfProductSkuConverter;

	private ShopperConverter xpfShopperConverter;

	private ShoppingCartConverter xpfShoppingCartConverter;

	private ShoppingItemConverter xpfShoppingItemConverter;

	private ShoppingItemDtoConverter xpfShoppingItemDtoConverter;

	private PriceConverter xpfPriceConverter;

	private XPFConverterUtil xpfXPFConverterUtil;

	private ProductSkuLookup productSkuLookup;

	private PriceLookupFacade priceLookupFacade;

	private ShoppingItemValidationContextBuilder xpfShoppingItemValidationContextBuilder;

	private XPFShoppingItemValidationContext buildInternal(
			final XPFShoppingCart xpfShoppingCart,
			final XPFShoppingItem xpfParentShoppingItem,
			final XPFShoppingItem xpfShoppingItem,
			final XPFOperationEnum xpfOperation,
			final Shopper shopper,
			final Store store) {
		List<XPFShoppingItemValidationContext> children = xpfShoppingItem.getChildren().stream()
				.map(child -> buildInternal(xpfShoppingCart, xpfShoppingItem, child, xpfOperation, shopper, store))
				.collect(Collectors.toList());

		ProductSku productSku = productSkuLookup.findBySkuCode(xpfShoppingItem.getProductSku().getCode());
		Price price = priceLookupFacade.getPromotedPriceForSku(productSku, store, shopper);
		XPFPrice xpfPromotedPrice = xpfPriceConverter.convert(price);

		boolean inStoreCatalog = productSku.getProduct().isInCatalog(store.getCatalog());

		return new XPFShoppingItemValidationContext(
				xpfParentShoppingItem,
				xpfShoppingItem,
				inStoreCatalog,
				xpfShoppingCart,
				xpfPromotedPrice,
				xpfOperation,
				children);
	}

	@Override
	public XPFShoppingItemValidationContext build(
			final ShoppingCart shoppingCart,
			final ShoppingItem shoppingItem,
			final ShoppingItem parentShoppingItem,
			final XPFOperationEnum operation) {
		Objects.requireNonNull(shoppingCart,
				"Required field shoppingCart for building XPFShoppingItemValidationContext is missing.");

		XPFShoppingCart xpfShoppingCart = xpfShoppingCartConverter.convert(shoppingCart);
		return build(xpfShoppingCart, shoppingItem, parentShoppingItem, operation, shoppingCart.getShopper(), shoppingCart.getStore());
	}

	@Override
	public XPFShoppingItemValidationContext build(
			final XPFShoppingCart xpfShoppingCart,
			final ShoppingItem shoppingItem,
			final ShoppingItem parentShoppingItem,
			final XPFOperationEnum operation,
			final Shopper shopper,
			final Store store) {
		Objects.requireNonNull(xpfShoppingCart,
				"Required field xpfShoppingCart for building XPFShoppingItemValidationContext is missing.");
		Objects.requireNonNull(shopper,
				"Required field shopper for building XPFShoppingItemValidationContext is missing.");
		Objects.requireNonNull(store,
				"Required field store for building XPFShoppingItemValidationContext is missing.");

		XPFShoppingItem xpfParentShoppingItem = null;
		if (parentShoppingItem != null) {
			xpfParentShoppingItem = xpfShoppingItemConverter.convert(new StoreDomainContext<>(parentShoppingItem, store));
		}

		XPFShoppingItem xpfShoppingItem = xpfShoppingItemConverter.convert(new StoreDomainContext<>(shoppingItem, store));

		return buildInternal(xpfShoppingCart, xpfParentShoppingItem, xpfShoppingItem, operation, shopper, store);
	}

	@Override
	public XPFShoppingItemValidationContext build(
			final ShoppingCart shoppingCart,
			final ShoppingItemDto shoppingItemDto,
			final ShoppingItem parentShoppingItem,
			final XPFOperationEnum xpfOperation) {

		Objects.requireNonNull(shoppingCart,
				"Required field shoppingCart for building XPFShoppingItemValidationContext is missing.");

		XPFShoppingCart xpfShoppingCart = xpfShoppingCartConverter.convert(shoppingCart);
		return build(xpfShoppingCart, shoppingItemDto, parentShoppingItem, xpfOperation, shoppingCart.getShopper(), shoppingCart.getStore());
	}

	@Override
	public XPFShoppingItemValidationContext build(
			final XPFShoppingCart xpfShoppingCart,
			final ShoppingItemDto shoppingItemDto,
			final ShoppingItem parentShoppingItem,
			final XPFOperationEnum xpfOperation,
			final Shopper shopper,
			final Store store) {

		Objects.requireNonNull(xpfShoppingCart,
				"Required field xpfShoppingCart for building XPFShoppingItemValidationContext is missing.");
		Objects.requireNonNull(shoppingItemDto,
				"Required field shoppingItemDto for building XPFShoppingItemValidationContext is missing.");
		Objects.requireNonNull(shopper,
				"Required field shopper for building XPFShoppingItemValidationContext is missing.");
		Objects.requireNonNull(store,
				"Required field store for building XPFShoppingItemValidationContext is missing.");

		XPFShoppingItem xpfShoppingItem = xpfShoppingItemDtoConverter.convert(new StoreDomainContext<>(shoppingItemDto, store));

		XPFShoppingItem xpfParentShoppingItem = null;
		if (parentShoppingItem != null) {
			xpfParentShoppingItem = xpfShoppingItemConverter.convert(new StoreDomainContext<>(parentShoppingItem, store));
		}

		return buildInternal(
				xpfShoppingCart, xpfParentShoppingItem, xpfShoppingItem, xpfOperation, shopper, store);
	}

	@Override
	public Stream<XPFShoppingItemValidationContext> getAllContextsStream(final XPFShoppingItemValidationContext context) {
		return getAllContextsForList(Collections.singletonList(context));
	}

	private Stream<XPFShoppingItemValidationContext> getAllContextsForList(final List<XPFShoppingItemValidationContext> contexts) {
		return contexts.stream()
				.flatMap(context -> Stream.concat(Stream.of(context), getAllContextsForList(context.getChildren())));
	}

	protected ProductSkuConverter getXpfProductSkuConverter() {
		return xpfProductSkuConverter;
	}

	public void setXpfProductSkuConverter(final ProductSkuConverter xpfProductSkuConverter) {
		this.xpfProductSkuConverter = xpfProductSkuConverter;
	}

	protected ShopperConverter getXpfShopperConverter() {
		return xpfShopperConverter;
	}

	public void setXpfShopperConverter(final ShopperConverter xpfShopperConverter) {
		this.xpfShopperConverter = xpfShopperConverter;
	}

	protected ShoppingItemConverter getXpfShoppingItemConverter() {
		return xpfShoppingItemConverter;
	}

	protected ShoppingCartConverter getXpfShoppingCartConverter() {
		return xpfShoppingCartConverter;
	}

	public void setXpfShoppingCartConverter(final ShoppingCartConverter xpfShoppingCartConverter) {
		this.xpfShoppingCartConverter = xpfShoppingCartConverter;
	}

	public void setXpfShoppingItemConverter(final ShoppingItemConverter xpfShoppingItemConverter) {
		this.xpfShoppingItemConverter = xpfShoppingItemConverter;
	}

	protected ShoppingItemDtoConverter getXpfShoppingItemDtoConverter() {
		return xpfShoppingItemDtoConverter;
	}

	public void setXpfShoppingItemDtoConverter(final ShoppingItemDtoConverter xpfShoppingItemDtoConverter) {
		this.xpfShoppingItemDtoConverter = xpfShoppingItemDtoConverter;
	}

	protected PriceConverter getXpfPriceConverter() {
		return xpfPriceConverter;
	}

	public void setXpfPriceConverter(final PriceConverter xpfPriceConverter) {
		this.xpfPriceConverter = xpfPriceConverter;
	}

	protected XPFConverterUtil getXpfXPFConverterUtil() {
		return xpfXPFConverterUtil;
	}

	public void setXpfXPFConverterUtil(final XPFConverterUtil xpfXPFConverterUtil) {
		this.xpfXPFConverterUtil = xpfXPFConverterUtil;
	}

	protected ProductSkuLookup getProductSkuLookup() {
		return productSkuLookup;
	}

	public void setProductSkuLookup(final ProductSkuLookup productSkuLookup) {
		this.productSkuLookup = productSkuLookup;
	}

	protected PriceLookupFacade getPriceLookupFacade() {
		return priceLookupFacade;
	}

	public void setPriceLookupFacade(final PriceLookupFacade priceLookupFacade) {
		this.priceLookupFacade = priceLookupFacade;
	}

	protected ShoppingItemValidationContextBuilder getXpfShoppingItemValidationContextBuilder() {
		return xpfShoppingItemValidationContextBuilder;
	}

	public void setXpfShoppingItemValidationContextBuilder(final ShoppingItemValidationContextBuilder xpfShoppingItemValidationContextBuilder) {
		this.xpfShoppingItemValidationContextBuilder = xpfShoppingItemValidationContextBuilder;
	}
}
