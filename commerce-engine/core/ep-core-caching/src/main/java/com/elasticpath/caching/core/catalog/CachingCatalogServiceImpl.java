/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.caching.core.catalog;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.cache.Cache;
import com.elasticpath.cache.CacheLoader;
import com.elasticpath.caching.core.MutableCachingService;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.persistence.api.FetchGroupLoadTuner;
import com.elasticpath.persistence.api.LoadTuner;
import com.elasticpath.service.catalog.CatalogService;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;

/**
 * Caching implementation of {@link CatalogService}.
 */
public class CachingCatalogServiceImpl extends AbstractEpPersistenceServiceImpl implements CatalogService, MutableCachingService<Catalog> {

	private final CacheLoader<String, Catalog> catalogByCodeCacheLoader = new CatalogByCodeCacheLoader();

	private CatalogService fallbackService;
	private Cache<String, Catalog> catalogByCodeCache;

	@Override
	public Catalog saveOrUpdate(final Catalog catalog) throws EpServiceException {
		return getFallbackService().saveOrUpdate(catalog);
	}

	@Override
	public void remove(final Catalog catalog) throws EpServiceException {
		getFallbackService().remove(catalog);
	}

	@Override
	public Catalog getCatalog(final long catalogUid) throws EpServiceException {
		return getFallbackService().getCatalog(catalogUid);
	}

	@Override
	public List<Long> findAllCatalogUids() throws EpServiceException {
		return getFallbackService().findAllCatalogUids();
	}

	@Override
	public List<Catalog> findAllCatalogs() throws EpServiceException {
		return getFallbackService().findAllCatalogs();
	}

	@Override
	public List<Long> findMasterCatalogUids() throws EpServiceException {
		return getFallbackService().findMasterCatalogUids();
	}

	@Override
	public Catalog findByName(final String name) throws EpServiceException {
		return getFallbackService().findByName(name);
	}

	@Override
	public Catalog findByCode(final String code) throws EpServiceException {
		return getCatalogByCodeCache().get(code, catalogByCodeCacheLoader::load);
	}

	@Override
	public boolean nameExists(final String catalogName) throws EpServiceException {
		return getFallbackService().nameExists(catalogName);
	}

	@Override
	public boolean codeExists(final String code) throws EpServiceException {
		return getFallbackService().codeExists(code);
	}

	@Override
	public Catalog load(final long catalogUid, final FetchGroupLoadTuner fetchGroupLoadTuner,
						final boolean cleanExistingGroups) throws EpServiceException {
		return getFallbackService().load(catalogUid, fetchGroupLoadTuner, cleanExistingGroups);
	}

	@Override
	public boolean catalogInUse(final long catalogUid) throws EpServiceException {
		return getFallbackService().catalogInUse(catalogUid);
	}

	@Override
	public List<Catalog> findMasterCatalogs() throws EpServiceException {
		return getFallbackService().findMasterCatalogs();
	}

	@Override
	public Collection<Locale> findAllCatalogLocales() {
		return getFallbackService().findAllCatalogLocales();
	}

	@Override
	public Catalog findByGuid(final String code, final LoadTuner loadTuner) {
		return getFallbackService().findByGuid(code, loadTuner);
	}

	@Override
	public List<Catalog> listAllCatalogsWithCodes(final List<String> codes) {
		return getFallbackService().listAllCatalogsWithCodes(codes);
	}

	@Override
	public List<Catalog> findMastersUsedByVirtualCatalog(final String catalogCode) {
		return getFallbackService().findMastersUsedByVirtualCatalog(catalogCode);
	}

	@Override
	public Object getObject(final long uid) throws EpServiceException {
		return getFallbackService().getObject(uid);
	}

	@Override
	public void cache(final Catalog entity) {
		getCatalogByCodeCache().put(entity.getCode(), entity);
	}

	@Override
	public void invalidate(final Catalog entity) {
		getCatalogByCodeCache().remove(entity.getCode());
	}

	@Override
	public void invalidateAll() {
		getCatalogByCodeCache().removeAll();
	}

	protected CatalogService getFallbackService() {
		return fallbackService;
	}

	public void setFallbackService(final CatalogService fallbackService) {
		this.fallbackService = fallbackService;
	}

	protected CacheLoader<String, Catalog> getCatalogByCodeCacheLoader() {
		return catalogByCodeCacheLoader;
	}

	protected Cache<String, Catalog> getCatalogByCodeCache() {
		return catalogByCodeCache;
	}

	public void setCatalogByCodeCache(final Cache<String, Catalog> catalogByCodeCache) {
		this.catalogByCodeCache = catalogByCodeCache;
	}

	/**
	 * Catalog by code cache loader.
	 */
	protected class CatalogByCodeCacheLoader implements CacheLoader<String, Catalog> {
		@Override
		public Catalog load(final String key) {
			return getFallbackService().findByCode(key);
		}

		@Override
		public Map<String, Catalog> loadAll(final Iterable<? extends String> keys) {
			throw new UnsupportedOperationException("Not yet implemented");
		}
	}
}
