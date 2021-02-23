/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.caching.core.search;

import java.util.Collection;
import java.util.List;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.cache.Cache;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.search.SortAttribute;
import com.elasticpath.domain.search.SortValue;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.service.search.SortAttributeService;

/**
 * Caching implementation of SortAttributeService.
 */
public class CachingSortAttributeServiceImpl implements SortAttributeService {


	private SortAttributeService fallbackService;
	private Cache<String, SortValue> findByStoreCodeAndLocaleCodeCache;
	private Cache<String, SortAttribute> defaultSortAttributeForStoreCache;

	public void setFallbackService(final SortAttributeService fallbackService) {
		this.fallbackService = fallbackService;
	}

	public void setFindByStoreCodeAndLocaleCodeCache(final Cache<String, SortValue> findByStoreCodeAndLocaleCodeCache) {
		this.findByStoreCodeAndLocaleCodeCache = findByStoreCodeAndLocaleCodeCache;
	}

	@Override
	public SortAttribute saveOrUpdate(final SortAttribute sortAttribute) throws EpServiceException {
		return fallbackService.saveOrUpdate(sortAttribute);
	}

	@Override
	public void remove(final SortAttribute sortAttribute) throws EpServiceException {
		fallbackService.remove(sortAttribute);
	}

	@Override
	public List<SortAttribute> findSortAttributesByStoreCode(final String storeCode) {
		return fallbackService.findSortAttributesByStoreCode(storeCode);
	}

	@Override
	public List<String> findSortAttributeGuidsByStoreCodeAndLocalCode(final String storeCode, final String localeCode) {
		return fallbackService.findSortAttributeGuidsByStoreCodeAndLocalCode(storeCode, localeCode);
	}

	@Override
	public SortAttribute findByGuid(final String guid) {
		return fallbackService.findByGuid(guid);
	}

	@Override
	public List<SortAttribute> findByGuids(final List<String> sortAttributeGuids) {
		return fallbackService.findByGuids(sortAttributeGuids);
	}

	@Override
	public List<String> findAllGuids() {
		return fallbackService.findAllGuids();
	}

	@Override
	public SortValue findSortValueByGuidAndLocaleCode(final String guid, final String localeCode) {
		final String key = guid + ":" + localeCode;
		return findByStoreCodeAndLocaleCodeCache.get(key, cacheKey -> fallbackService.findSortValueByGuidAndLocaleCode(guid, localeCode));
	}

	@Override
	public List<Attribute> findSortableProductAttributesByCatalogIds(final List<Long> catalogIds) {
		return fallbackService.findSortableProductAttributesByCatalogIds(catalogIds);
	}

	@Override
	public SortAttribute getDefaultSortAttributeForStore(final String storeCode) {
		return defaultSortAttributeForStoreCache.get(storeCode, cacheKey -> fallbackService.getDefaultSortAttributeForStore(storeCode));
	}

	@Override
	public void removeAllLocalizedName(final SortAttribute sortAttribute) {
		fallbackService.removeAllLocalizedName(sortAttribute);
	}

	@Override
	public void setPersistenceEngine(final PersistenceEngine persistenceEngine) {
		fallbackService.setPersistenceEngine(persistenceEngine);
	}

	@Override
	public PersistenceEngine getPersistenceEngine() {
		return fallbackService.getPersistenceEngine();
	}

	@Override
	public Object getObject(final long uid) throws EpServiceException {
		return fallbackService.getObject(uid);
	}

	@Override
	public Object getObject(final long uid, final Collection<String> fieldsToLoad) throws EpServiceException {
		return fallbackService.getObject(uid, fieldsToLoad);
	}

	public void setDefaultSortAttributeForStoreCache(final Cache<String, SortAttribute> defaultSortAttributeForStoreCache) {
		this.defaultSortAttributeForStoreCache = defaultSortAttributeForStoreCache;
	}
}
