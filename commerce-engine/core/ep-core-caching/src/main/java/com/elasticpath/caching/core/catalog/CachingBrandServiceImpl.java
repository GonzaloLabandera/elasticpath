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
import com.elasticpath.domain.catalog.Brand;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.service.catalog.BrandService;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;

/**
 * Caching implementation of {@link BrandService}.
 */
public class CachingBrandServiceImpl extends AbstractEpPersistenceServiceImpl implements BrandService, MutableCachingService<Brand> {

	private final CacheLoader<String, Brand> brandByCodeCacheLoader = new BrandByCodeCacheLoader();

	private BrandService fallbackService;
	private Cache<String, Brand> brandByCodeCache;

	@Override
	public Brand add(final Brand brand) throws EpServiceException {
		return getFallbackService().add(brand);
	}

	@Override
	public Brand get(final long brandUid) throws EpServiceException {
		return getFallbackService().get(brandUid);
	}

	@Override
	public Brand saveOrUpdate(final Brand brand) throws EpServiceException {
		return getFallbackService().saveOrUpdate(brand);
	}

	@Override
	public void remove(final Brand brand) throws EpServiceException {
		getFallbackService().remove(brand);
	}

	@Override
	public List<Brand> list() throws EpServiceException {
		return getFallbackService().list();
	}

	@Override
	public boolean isInUse(final long uidToCheck) throws EpServiceException {
		return getFallbackService().isInUse(uidToCheck);
	}

	@Override
	public List<Brand> findAllBrandsFromCatalog(final long catalogUid) throws EpServiceException {
		return getFallbackService().findAllBrandsFromCatalog(catalogUid);
	}

	@Override
	public List<Brand> findAllBrandsFromCatalogList(final Collection<Catalog> catalogs) throws EpServiceException {
		return getFallbackService().findAllBrandsFromCatalogList(catalogs);
	}

	@Override
	public List<Long> getBrandInUseUidList() {
		return getFallbackService().getBrandInUseUidList();
	}

	@Override
	public List<Long> getBrandInUseUidList(final Collection<Long> brandUids) throws EpServiceException {
		return getFallbackService().getBrandInUseUidList();
	}

	@Override
	public List<Brand> getBrandInUseList() {
		return getFallbackService().getBrandInUseList();
	}

	@Override
	public Brand update(final Brand brand) throws EpServiceException {
		return getFallbackService().update(brand);
	}

	@Override
	public boolean codeExists(final String code) throws EpServiceException {
		return getFallbackService().codeExists(code);
	}

	@Override
	public boolean codeExists(final Brand brand) throws EpServiceException {
		return getFallbackService().codeExists(brand);
	}

	@Override
	public Brand findByCode(final String code) throws EpServiceException {
		return getBrandByCodeCache().get(code, brandByCodeCacheLoader::load);
	}

	@Override
	public List<String[]> getBrandCodeNameList() {
		return getFallbackService().getBrandCodeNameList();
	}

	@Override
	public Object getObject(final long uid) throws EpServiceException {
		return getFallbackService().getObject(uid);
	}

	@Override
	public void cache(final Brand entity) {
		getBrandByCodeCache().put(entity.getCode(), entity);
	}

	@Override
	public void invalidate(final Brand entity) {
		getBrandByCodeCache().remove(entity.getCode());
	}

	@Override
	public void invalidateAll() {
		getBrandByCodeCache().removeAll();
	}

	protected BrandService getFallbackService() {
		return fallbackService;
	}

	public void setFallbackService(final BrandService fallbackService) {
		this.fallbackService = fallbackService;
	}

	protected CacheLoader<String, Brand> getBrandByCodeCacheLoader() {
		return brandByCodeCacheLoader;
	}

	protected Cache<String, Brand> getBrandByCodeCache() {
		return brandByCodeCache;
	}

	public void setBrandByCodeCache(final Cache<String, Brand> brandByCodeCache) {
		this.brandByCodeCache = brandByCodeCache;
	}

	/**
	 * Brand by code cache loader.
	 */
	protected class BrandByCodeCacheLoader implements CacheLoader<String, Brand> {
		@Override
		public Brand load(final String key) {
			return getFallbackService().findByCode(key);
		}

		@Override
		public Map<String, Brand> loadAll(final Iterable<? extends String> keys) {
			throw new UnsupportedOperationException("Not yet implemented");
		}
	}
}
