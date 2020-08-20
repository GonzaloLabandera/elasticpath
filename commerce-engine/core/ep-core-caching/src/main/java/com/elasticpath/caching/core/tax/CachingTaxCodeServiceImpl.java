/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.caching.core.tax;

import java.util.List;
import java.util.Map;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.cache.Cache;
import com.elasticpath.cache.CacheLoader;
import com.elasticpath.caching.core.MutableCachingService;
import com.elasticpath.domain.tax.TaxCode;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;
import com.elasticpath.service.tax.TaxCodeExistException;
import com.elasticpath.service.tax.TaxCodeService;

/**
 * Caching implementation of {@link TaxCodeService}.
 */
public class CachingTaxCodeServiceImpl extends AbstractEpPersistenceServiceImpl implements TaxCodeService, MutableCachingService<TaxCode> {

	private final CacheLoader<String, TaxCode> taxCodeByCodeCacheLoader = new TaxCodeByCodeCacheLoader();

	private TaxCodeService fallbackService;
	private Cache<String, TaxCode> taxCodeByCodeCache;

	@Override
	public TaxCode add(final TaxCode taxCode) throws TaxCodeExistException {
		return getFallbackService().add(taxCode);
	}

	@Override
	public TaxCode update(final TaxCode taxCode) throws TaxCodeExistException {
		return getFallbackService().update(taxCode);
	}

	@Override
	public void remove(final TaxCode taxCode) throws EpServiceException {
		getFallbackService().remove(taxCode);
	}

	@Override
	public List<TaxCode> list() throws EpServiceException {
		return getFallbackService().list();
	}

	@Override
	public TaxCode load(final long taxCodeUid) throws EpServiceException {
		return getFallbackService().load(taxCodeUid);
	}

	@Override
	public TaxCode get(final long taxCodeUid) throws EpServiceException {
		return getFallbackService().get(taxCodeUid);
	}

	@Override
	public List<String> getTaxCodesInUse() throws EpServiceException {
		return getFallbackService().getTaxCodesInUse();
	}

	@Override
	public boolean taxCodeExists(final String code) throws EpServiceException {
		return getFallbackService().taxCodeExists(code);
	}

	@Override
	public boolean taxCodeExists(final TaxCode code) throws EpServiceException {
		return getFallbackService().taxCodeExists(code);
	}

	@Override
	public TaxCode findByCode(final String code) throws EpServiceException {
		return getTaxCodeByCodeCache().get(code, getTaxCodeByCodeCacheLoader());
	}

	@Override
	public TaxCode findByGuid(final String guid) throws EpServiceException {
		return getFallbackService().findByGuid(guid);
	}

	@Override
	public Object getObject(final long uid) throws EpServiceException {
		return getFallbackService().getObject(uid);
	}

	@Override
	public void cache(final TaxCode entity) {
		getTaxCodeByCodeCache().put(entity.getCode(), entity);
	}

	@Override
	public void invalidate(final TaxCode entity) {
		getTaxCodeByCodeCache().remove(entity.getCode());
	}

	@Override
	public void invalidateAll() {
		getTaxCodeByCodeCache().removeAll();
	}

	protected TaxCodeService getFallbackService() {
		return fallbackService;
	}

	public void setFallbackService(final TaxCodeService fallbackService) {
		this.fallbackService = fallbackService;
	}

	protected CacheLoader<String, TaxCode> getTaxCodeByCodeCacheLoader() {
		return taxCodeByCodeCacheLoader;
	}

	protected Cache<String, TaxCode> getTaxCodeByCodeCache() {
		return taxCodeByCodeCache;
	}

	public void setTaxCodeByCodeCache(final Cache<String, TaxCode> taxCodeByCodeCache) {
		this.taxCodeByCodeCache = taxCodeByCodeCache;
	}

	/**
	 * Tax code by code cache loader.
	 */
	protected class TaxCodeByCodeCacheLoader implements CacheLoader<String, TaxCode> {
		@Override
		public TaxCode load(final String key) {
			return getFallbackService().findByCode(key);
		}

		@Override
		public Map<String, TaxCode> loadAll(final Iterable<? extends String> keys) {
			throw new UnsupportedOperationException("Not yet implemented");
		}
	}
}
