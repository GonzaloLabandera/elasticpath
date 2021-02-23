/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.caching.core.catalog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.cache.Cache;
import com.elasticpath.cache.CacheLoader;
import com.elasticpath.caching.core.MutableCachingService;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.service.catalog.ProductLookup;

/**
 * A cached version of the {@link ProductLookup} interface.
 */
public class CachingProductLookupImpl implements ProductLookup, MutableCachingService<Product> {

	private final CacheLoader<String, Long> productByGuidLoader = new ProductByGuidLoader();
	private final CacheLoader<Long, Product> productByUidLoader = new ProductByUidLoader();

	private Cache<Long, Product> productByUidCache;
	private Cache<String, Long> productUidByGuidCache;

	private ProductLookup fallbackLookup;

	@Override
	@SuppressWarnings("unchecked")
	public <P extends Product> P findByUid(final long uidpk) throws EpServiceException {
		return (P) getProductByUidCache().get(uidpk, productByUidLoader::load);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <P extends Product> List<P> findByUids(final Collection<Long> uidPks) throws EpServiceException {
		Map<Long, Product> result = getProductByUidCache().getAll(uidPks, productByUidLoader::loadAll);
		return new ArrayList<>((Collection<P>) result.values());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <P extends Product> P findByGuid(final String guid) throws EpServiceException {
		final Long productUidPk = getProductUidByGuidCache().get(guid, productByGuidLoader::load);
		if (productUidPk != null) {
			return findByUid(productUidPk);
		}
		return null;
	}

	@Override
	public <P extends Product> List<P> findByGuids(final Collection<String> guids) throws EpServiceException {
		final Map<String, Long> result = getProductUidByGuidCache().getAll(guids, productByGuidLoader::loadAll);
		return findByUids(result.values());
	}

	@Override
	public void cache(final Product entity) {
		getProductByUidCache().put(entity.getUidPk(), entity);
		getProductUidByGuidCache().put(entity.getGuid(), entity.getUidPk());
	}

	@Override
	public void invalidate(final Product entity) {
		getProductByUidCache().remove(entity.getUidPk());
		getProductUidByGuidCache().remove(entity.getGuid());
	}

	@Override
	public void invalidateAll() {
		getProductByUidCache().removeAll();
		getProductUidByGuidCache().removeAll();
	}

	protected Cache<Long, Product> getProductByUidCache() {
		return productByUidCache;
	}

	public void setProductByUidCache(final Cache<Long, Product> productByUidCache) {

		this.productByUidCache = productByUidCache;
	}

	protected Cache<String, Long> getProductUidByGuidCache() {
		return productUidByGuidCache;
	}

	public void setProductUidByGuidCache(final Cache<String, Long> productUidByGuidCache) {
		this.productUidByGuidCache = productUidByGuidCache;
	}

	public void setFallbackLookup(final ProductLookup lookup) {
		this.fallbackLookup = lookup;
	}

	protected ProductLookup getFallbackLookup() {
		return fallbackLookup;
	}

	/**
	 * A CacheLoader which loads Products by uidPk.
	 */
	private class ProductByUidLoader implements CacheLoader<Long, Product> {
		@Override
		public Product load(final Long key) {
			Product product = getFallbackLookup().findByUid(key);

			cacheProductUidByGuidIfRequired(product);

			return product;
		}

		@Override
		public Map<Long, Product> loadAll(final Iterable<? extends Long> keys) {
			List<Product> products = getFallbackLookup().findByUids(Lists.newArrayList(keys));
			Map<Long, Product> productMap = new LinkedHashMap<>(products.size() * 2);
			for (Product product : products) {
				productMap.put(product.getUidPk(), product);
				cacheProductUidByGuidIfRequired(product);
			}

			return productMap;
		}

		/*
		This method is called from findByUid and findByUids methods and used for
		caching a guid-uidPk pair so findByGuid calls can utilize cache more efficiently
	 */
		private void cacheProductUidByGuidIfRequired(final Product dbProduct) {
			if (dbProduct != null && getProductUidByGuidCache().get(dbProduct.getGuid()) == null) {
				getProductUidByGuidCache().put(dbProduct.getGuid(), dbProduct.getUidPk());
			}
		}
	}

	/**
	 * A CacheLoader which loads Product by guid (code) and cache product in
	 * ProductUidToProductCache cache.
	 *
	 * Load product by GUID should rarely occur, if ever.
	 */
	private class ProductByGuidLoader implements CacheLoader<String, Long> {
		@Override
		public Long load(final String guid) {
			final Product product = getFallbackLookup().findByGuid(guid);

			//null check is required for sync tool when syncing deleted products
			if (product != null) {
				final Long productUidPk = product.getUidPk();

				getProductByUidCache().put(productUidPk, product);

				return productUidPk;
			}

			return null;
		}

		@Override
		public Map<String, Long> loadAll(final Iterable<? extends String> keys) {
			final List<Product> products = getFallbackLookup().findByGuids(Lists.newArrayList(keys));
			final Map<String, Long> productMap = new LinkedHashMap<>(products.size() * 2);
			for (Product product : products) {
				productMap.put(product.getGuid(), product.getUidPk());
				getProductByUidCache().put(product.getUidPk(), product);
			}
			return productMap;
		}
	}
}
