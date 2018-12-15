/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.item.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableSortedMap;
import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.domain.catalog.BundleConstituent;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.id.util.CompositeIdUtil;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.service.catalog.BundleIdentifier;

/**
 * Repository that consolidates access to item domain related concepts such as product, sku, and item configuration.
 */
@Singleton
@Named("itemRepository")
public class ItemRepositoryImpl implements ItemRepository {
	private static final String ITEM_NOT_FOUND_MESSAGE = "Item not found.";
	private static final String PRODUCT_NOT_FOUND = "Product cannot be null.";
	private static final String DEFAULT_SKU_NOT_FOUND = "Product must have a default sku.";
	private static final String BUNDLE_CONS_NOT_FOUND = "Bundle constituent not found.";
	private static final String CONSTITUENT_NOT_BUNDLE = "Constituent is not a bundle.";
	private static final String COMPONENT_NOT_FOUND = "Component not found.";

	private final ProductSkuRepository productSkuRepository;
	private final BundleIdentifier bundleIdentifier;
	private final ReactiveAdapter reactiveAdapter;


	/**
	 * Creates instance with needed services wired in.
	 *
	 * @param productSkuRepository product sku repository
	 * @param bundleIdentifier     class for evaluating bundle information about a product
	 * @param reactiveAdapter      the reactive adapter
	 */
	@Inject
	public ItemRepositoryImpl(
			@Named("productSkuRepository")  final ProductSkuRepository productSkuRepository,
			@Named("bundleIdentifier")      final BundleIdentifier bundleIdentifier,
			@Named("reactiveAdapter") 		final ReactiveAdapter reactiveAdapter) {

		this.productSkuRepository = productSkuRepository;
		this.bundleIdentifier = bundleIdentifier;
		this.reactiveAdapter = reactiveAdapter;
	}

	private ProductSku getVerifiedDefaultSku(final Product product) {
		assert product != null : PRODUCT_NOT_FOUND;

		ProductSku defaultSku = product.getDefaultSku();
		assert defaultSku != null : DEFAULT_SKU_NOT_FOUND;
		return defaultSku;
	}

	@Override
	public String getDefaultItemIdForProduct(final Product product) {
		ProductSku defaultSku = getVerifiedDefaultSku(product);

		return getItemIdForSku(defaultSku);
	}

	@Override
	public String getItemIdForSku(final ProductSku productSku) {
		String skuCode = productSku.getSkuCode();
		return CompositeIdUtil.encodeCompositeId(createItemIdMap(skuCode));
	}

	@Override
	@CacheResult
	public IdentifierPart<Map<String, String>> getItemIdForProductSku(final ProductSku productSku) {
		return CompositeIdentifier.of(createItemIdMap(productSku.getSkuCode()));
	}

	@Override
	@CacheResult
	public IdentifierPart<Map<String, String>> getItemIdMap(final String skuCode) {
		return CompositeIdentifier.of(createItemIdMap(skuCode));
	}

	@Override
	@CacheResult(uniqueIdentifier = "asProductBundle")
	public Single<ProductBundle> asProductBundle(final Product product) {
		return reactiveAdapter.fromNullableAsSingle(() -> product, "product should not be null.")
				.flatMap(prod -> reactiveAdapter.fromServiceAsSingle(() -> bundleIdentifier.asProductBundle(prod)));
	}

	private Map<String, String> createItemIdMap(final String skuCode) {
		return ImmutableSortedMap.of(SKU_CODE_KEY, skuCode);
	}

	@Override
	@CacheResult(uniqueIdentifier = "getSkuForItemId")
	public Single<ProductSku> getSkuForItemId(final Map<String, String> itemIdMap) {
		return getSkuCodeForItemIdMap(itemIdMap)
				.flatMap(productSkuRepository::getProductSkuWithAttributesByCode);
	}

	private Single<String> getSkuCodeForItemIdMap(final Map<String, String> itemIdMap) {
		assert itemIdMap != null : "itemId should never be null.";
		return reactiveAdapter.fromNullableAsSingle(() -> itemIdMap.get(SKU_CODE_KEY), ITEM_NOT_FOUND_MESSAGE);
	}

	@Override
	@CacheResult(uniqueIdentifier = "isItemBundle")
	public Single<Boolean> isItemBundle(final Map<String, String> itemIdMap) {
		return getSkuCodeForItemIdMap(itemIdMap)
				.flatMap(productSkuRepository::isProductBundleByCode);
	}

	@Override
	@CacheResult(uniqueIdentifier = "isProductSkuExistForItemId")
	public Single<Boolean> isProductSkuExistForItemId(final Map<String, String> itemIdMap) {
		return getSkuCodeForItemIdMap(itemIdMap)
				.flatMap(productSkuRepository::isProductSkuExistByCode);
	}

	@Override
	@CacheResult(uniqueIdentifier = "getSkuOptionsForItemId")
	public Observable<SkuOption> getSkuOptionsForItemId(final Map<String, String> itemIdMap) {
		return getSkuCodeForItemIdMap(itemIdMap)
				.flatMapObservable(productSkuRepository::getProductSkuOptionsByCode);
	}

	@Override
	public Single<BundleConstituent> findBundleConstituentAtPathEnd(final Map<String, String> rootItemIdMap,
																	final Iterator<String> guidPathFromRootItem) {
		return getSkuForItemId(rootItemIdMap)
				.map(ProductSku::getProduct)
				.flatMap(this::asProductBundle)
				.map(ProductBundle::getConstituents)
				.flatMap(bundleConstituents -> findBundleConstituentWithGuid(bundleConstituents, guidPathFromRootItem.next()))
				.flatMap(bundleConstituent -> getNestedBundleConstituent(bundleConstituent, guidPathFromRootItem))
				.onErrorResumeNext(Single.error(ResourceOperationFailure.notFound(COMPONENT_NOT_FOUND)));
	}

	@Override
	public Single<BundleConstituent> getNestedBundleConstituent(final BundleConstituent currBundleConstituent,
																final Iterator<String> guidPathFromRootItem) {
		if (!guidPathFromRootItem.hasNext()) {
			return Single.just(currBundleConstituent);
		}

		return getProductBundleFromConstituent(currBundleConstituent)
				.map(ProductBundle::getConstituents)
				.flatMap(bundleConstituents -> findBundleConstituentWithGuid(bundleConstituents, guidPathFromRootItem.next()))
				.flatMap(bundleConstituent -> getNestedBundleConstituent(bundleConstituent, guidPathFromRootItem));
	}

	@Override
	public Single<ProductBundle> getProductBundleFromConstituent(final BundleConstituent bundleConstituent) {
		if (bundleConstituent.getConstituent().isBundle()) {
			Product product = bundleConstituent.getConstituent().getProduct();
			return asProductBundle(product);
		}

		return Single.error(ResourceOperationFailure.notFound(CONSTITUENT_NOT_BUNDLE));
	}

	@Override
	public Single<BundleConstituent> findBundleConstituentWithGuid(final List<BundleConstituent> bundleConstituents, final String guid) {
		for (BundleConstituent bundleConstituent : bundleConstituents) {
			if (bundleConstituent.getGuid().equals(guid)) {
				return Single.just(bundleConstituent);
			}
		}
		return Single.error(ResourceOperationFailure.notFound(BUNDLE_CONS_NOT_FOUND));
	}
}
