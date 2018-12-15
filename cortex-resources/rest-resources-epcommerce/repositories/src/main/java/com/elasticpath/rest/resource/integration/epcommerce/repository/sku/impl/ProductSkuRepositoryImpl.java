/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.sku.impl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalogview.StoreProduct;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.chain.Assign;
import com.elasticpath.rest.chain.ExecutionResultChain;
import com.elasticpath.rest.chain.OnFailure;
import com.elasticpath.rest.command.ExecutionResult;
import com.elasticpath.rest.command.ExecutionResultFactory;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.service.catalog.BundleIdentifier;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.catalogview.StoreProductService;
import com.elasticpath.service.store.StoreService;

/**
 * Product SKU repository.
 */
@Singleton
@Named("productSkuRepository")
public class ProductSkuRepositoryImpl implements ProductSkuRepository {

	private static final String NOT_FOUND_MESSAGE = "Could not find item for item ID.";
	private static final String PRODUCT_NOT_FOUND_FOR_SKU = "Product not found for product sku.";

	private final ProductSkuLookup productSkuLookup;

	private final BundleIdentifier bundleIdentifier;

	private final ReactiveAdapter reactiveAdapter;

	private final StoreService storeService;

	private final StoreProductService storeProductService;

	/**
	 * Instantiates a new product sku repository.
	 *
	 * @param productSkuLookup the product sku lookup
	 * @param bundleIdentifier class for evaluating bundle information about a product
	 * @param reactiveAdapter the reactive adapter
	 * @param storeService the store service
	 * @param storeProductService the store product service
	 */
	@Inject
	public ProductSkuRepositoryImpl(
			@Named("productSkuLookup") final ProductSkuLookup productSkuLookup,
			@Named("bundleIdentifier") final BundleIdentifier bundleIdentifier,
			@Named("reactiveAdapter")  final ReactiveAdapter reactiveAdapter,
			@Named("storeService") final StoreService storeService,
			@Named("storeProductService") final StoreProductService storeProductService) {

		this.productSkuLookup = productSkuLookup;
		this.bundleIdentifier = bundleIdentifier;
		this.reactiveAdapter = reactiveAdapter;
		this.storeService = storeService;
		this.storeProductService = storeProductService;
	}

	@Override
	@CacheResult(uniqueIdentifier = "getProductSkuWithAttributesByCode")
	public Single<ProductSku> getProductSkuWithAttributesByCode(final String skuCode) {
		return reactiveAdapter.fromServiceAsSingle(() -> productSkuLookup.findBySkuCode(skuCode), NOT_FOUND_MESSAGE);
	}

	@Override
	@CacheResult(uniqueIdentifier = "getProductSkuWithAttributesByGuidAsSingle")
	public Single<ProductSku> getProductSkuWithAttributesByGuidAsSingle(final String skuGuid) {
		return reactiveAdapter.fromServiceAsSingle(() -> productSkuLookup.findByGuid(skuGuid), NOT_FOUND_MESSAGE);
	}

	@Override
	@CacheResult(uniqueIdentifier = "getProductSkuWithAttributesByGuid")
	public ExecutionResult<ProductSku> getProductSkuWithAttributesByGuid(final String skuGuid) {
		return new ExecutionResultChain() {
			public ExecutionResult<?> build() {
				ProductSku productSku = Assign.ifNotNull(productSkuLookup.findByGuid(skuGuid),
						OnFailure.returnNotFound(NOT_FOUND_MESSAGE));
				return ExecutionResultFactory.createReadOK(productSku);
			}
		}.execute();
	}

	@Override
	@CacheResult(uniqueIdentifier = "isProductBundleByGuid")
	public Single<Boolean> isProductBundleByGuid(final String skuGuid) {
		return getProductSkuWithAttributesByGuidAsSingle(skuGuid)
				.flatMap(productSku -> reactiveAdapter.fromNullableAsSingle(productSku::getProduct, PRODUCT_NOT_FOUND_FOR_SKU))
				.flatMap(this::isProductBundle);
	}

	@Override
	@CacheResult(uniqueIdentifier = "isProductBundleByCode")
	public Single<Boolean> isProductBundleByCode(final String skuCode) {
		return getProductSkuWithAttributesByCode(skuCode)
				.flatMap(productSku -> reactiveAdapter.fromNullableAsSingle(productSku::getProduct, PRODUCT_NOT_FOUND_FOR_SKU))
				.flatMap(this::isProductBundle);
	}

	private Single<Boolean> isProductBundle(final Product product) {
		return reactiveAdapter.fromServiceAsSingle(() -> bundleIdentifier.isBundle(product));
	}

	@Override
	@CacheResult(uniqueIdentifier = "isProductSkuExistByCode")
	public Single<Boolean> isProductSkuExistByCode(final String skuCode) {
		return reactiveAdapter.fromServiceAsSingle(() -> productSkuLookup.isProductSkuExist(skuCode), NOT_FOUND_MESSAGE);
	}

	@Override
	@CacheResult(uniqueIdentifier = "isDisplayableProductSkuForStore")
	public Single<Boolean> isDisplayableProductSkuForStore(final String productSkuCode, final String storeCode) {

		return reactiveAdapter.<ProductSku>fromServiceAsMaybe(() -> productSkuLookup.findBySkuCode(productSkuCode))
				.map(productSku -> storeProductService.getProductForStore(productSku.getProduct(),
						storeService.findStoreWithCode(storeCode)))
				.map(StoreProduct::isProductDisplayable)
				.switchIfEmpty(Maybe.just(false))
				.toSingle();

	}

	@Override
	@CacheResult
	public Observable<SkuOption> getProductSkuOptionsByCode(final String skuCode) {
		return getProductSkuWithAttributesByCode(skuCode)
				.map(productSku -> productSku.getProduct().getProductType().getSkuOptions())
				.flatMapObservable(Observable::fromIterable);
	}
}
