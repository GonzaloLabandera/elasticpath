/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.caching.core.catalog;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.Lists;

import com.elasticpath.cache.CacheLoader;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;

/**
 * Product uid by sku identifier cache loader.
 */
public class ProductUidBySkuIdentifierCacheLoader implements CacheLoader<String, Long> {

	private final Function<List<String>, List<ProductSku>> fallbackLoader;
	private final Function<ProductSku, String> identifierGetter;
	private final Consumer<Product> cachingConsumer;

	/**
	 * Constructor.
	 *
	 * @param fallbackLoader   the fallback loader
	 * @param identifierGetter the identifier getter
	 * @param cachingConsumer  the caching consumer
	 */
	public ProductUidBySkuIdentifierCacheLoader(final Function<List<String>, List<ProductSku>> fallbackLoader,
												final Function<ProductSku, String> identifierGetter, final Consumer<Product> cachingConsumer) {
		this.fallbackLoader = fallbackLoader;
		this.identifierGetter = identifierGetter;
		this.cachingConsumer = cachingConsumer;
	}

	@Override
	public Long load(final String key) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public Map<String, Long> loadAll(final Iterable<? extends String> keys) {
		final List<ProductSku> productSkus = fallbackLoader.apply(Lists.newArrayList(keys));
		final Map<String, Long> uidByIdentifierMap = new LinkedHashMap<>(productSkus.size() * 2);
		for (ProductSku productSku : productSkus) {
			final Product product = productSku.getProduct();
			cachingConsumer.accept(product);
			uidByIdentifierMap.put(identifierGetter.apply(productSku), product.getUidPk());
		}
		return uidByIdentifierMap;
	}
}