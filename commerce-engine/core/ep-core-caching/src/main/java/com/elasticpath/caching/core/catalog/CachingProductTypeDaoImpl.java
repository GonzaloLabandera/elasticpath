/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.caching.core.catalog;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.cache.Cache;
import com.elasticpath.cache.CacheLoader;
import com.elasticpath.caching.core.MutableCachingService;
import com.elasticpath.commons.exception.DuplicateKeyException;
import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.persistence.dao.ProductTypeDao;
import com.elasticpath.persistence.dao.impl.AbstractDaoImpl;
import com.elasticpath.service.misc.TimeService;

/**
 * Caching implementation of {@link ProductTypeDao}.
 */
public class CachingProductTypeDaoImpl extends AbstractDaoImpl implements ProductTypeDao, MutableCachingService<ProductType> {

	private final CacheLoader<String, ProductType> productTypeByNameCacheLoader = new ProductTypeByNameCacheLoader();

	private ProductTypeDao fallbackDao;
	private Cache<String, ProductType> productTypeByNameCache;

	@Override
	public void remove(final ProductType productType) throws EpServiceException {
		getFallbackDao().remove(productType);
	}

	@Override
	public Object getObject(final long uid, final Collection<String> fieldsToLoad) throws EpServiceException {
		return getFallbackDao().getObject(uid, fieldsToLoad);
	}

	@Override
	public ProductType findProductType(final String name) {
		return getFallbackDao().findProductType(name);
	}

	@Override
	public ProductType findProductTypeWithAttributes(final String name) {
		return getProductTypeByNameCache().get(name, getProductTypeByNameCacheLoader());
	}

	@Override
	public ProductType initialize(final ProductType productType) {
		return getFallbackDao().initialize(productType);
	}

	@Override
	public boolean isInUse(final long uidToCheck) throws EpServiceException {
		return getFallbackDao().isInUse(uidToCheck);
	}

	@Override
	public List<Long> listUsedUids() {
		return getFallbackDao().listUsedUids();
	}

	@Override
	public List<ProductType> findAllProductTypeFromCatalog(final long catalogUid) throws EpServiceException {
		return getFallbackDao().findAllProductTypeFromCatalog(catalogUid);
	}

	@Override
	public List<ProductType> list() throws EpServiceException {
		return getFallbackDao().list();
	}

	@Override
	public void setTimeService(final TimeService timeService) {
		getFallbackDao().setTimeService(timeService);
	}

	@Override
	public ProductType update(final ProductType productType) throws DuplicateKeyException {
		return getFallbackDao().update(productType);
	}

	@Override
	public ProductType get(final long uid) throws EpServiceException {
		return getFallbackDao().get(uid);
	}

	@Override
	public ProductType add(final ProductType productType) throws DuplicateKeyException {
		return getFallbackDao().add(productType);
	}

	@Override
	public ProductType findByGuid(final String guid) {
		return getFallbackDao().findByGuid(guid);
	}

	@Override
	public ProductType findBySkuCode(final String skuCode) {
		return getFallbackDao().findBySkuCode(skuCode);
	}

	@Override
	public void cache(final ProductType entity) {
		getProductTypeByNameCache().put(entity.getName(), entity);
	}

	@Override
	public void invalidate(final ProductType entity) {
		getProductTypeByNameCache().remove(entity.getName());
	}

	@Override
	public void invalidateAll() {
		getProductTypeByNameCache().removeAll();
	}

	protected ProductTypeDao getFallbackDao() {
		return fallbackDao;
	}

	public void setFallbackDao(final ProductTypeDao fallbackDao) {
		this.fallbackDao = fallbackDao;
	}

	protected CacheLoader<String, ProductType> getProductTypeByNameCacheLoader() {
		return productTypeByNameCacheLoader;
	}

	protected Cache<String, ProductType> getProductTypeByNameCache() {
		return productTypeByNameCache;
	}

	public void setProductTypeByNameCache(final Cache<String, ProductType> productTypeByNameCache) {
		this.productTypeByNameCache = productTypeByNameCache;
	}

	/**
	 * Product type by name cache loader.
	 */
	protected class ProductTypeByNameCacheLoader implements CacheLoader<String, ProductType> {
		@Override
		public ProductType load(final String key) {
			return getFallbackDao().findProductTypeWithAttributes(key);
		}

		@Override
		public Map<String, ProductType> loadAll(final Iterable<? extends String> keys) {
			throw new UnsupportedOperationException("Not yet implemented");
		}
	}
}
