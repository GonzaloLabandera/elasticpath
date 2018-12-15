/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.caching.core.catalog;

import java.util.List;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.cache.Cache;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.service.catalog.SkuOptionKeyExistException;
import com.elasticpath.service.catalog.SkuOptionService;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;

/**
 * Caching version of the sku option service.
 */
public class CachingSkuOptionServiceImpl extends AbstractEpPersistenceServiceImpl implements SkuOptionService {

	private  SkuOptionService fallbackSkuOptionService;
	private Cache<String, SkuOption> findByKeyCache;

	@Override
	public SkuOption add(final SkuOption skuOption) throws SkuOptionKeyExistException {
		return fallbackSkuOptionService.add(skuOption);
	}

	@Override
	public SkuOption update(final SkuOption skuOption) throws SkuOptionKeyExistException {
		return fallbackSkuOptionService.update(skuOption);
	}

	@Override
	public void remove(final SkuOption skuOption) throws EpServiceException {
		fallbackSkuOptionService.remove(skuOption);
	}

	@Override
	public SkuOption load(final long skuOptionUid) throws EpServiceException {
		return fallbackSkuOptionService.load(skuOptionUid);
	}

	@Override
	public SkuOption get(final long skuOptionUid) throws EpServiceException {
		return fallbackSkuOptionService.get(skuOptionUid);
	}

	@Override
	public List<SkuOption> list() throws EpServiceException {
		return fallbackSkuOptionService.list();
	}

	@Override
	public List<SkuOption> findAllSkuOptionFromCatalog(final long catalogUid) throws EpServiceException {
		return fallbackSkuOptionService.findAllSkuOptionFromCatalog(catalogUid);
	}

	@Override
	public boolean keyExists(final String key) throws EpServiceException {
		return fallbackSkuOptionService.keyExists(key);
	}

	@Override
	public boolean keyExists(final SkuOption skuOption) throws EpServiceException {
		return fallbackSkuOptionService.keyExists(skuOption);
	}

	@Override
	public SkuOption findByKey(final String key) throws EpServiceException {
		if (findByKeyCache.get(key) != null) {
			return findByKeyCache.get(key);
		}

		SkuOption skuOption = fallbackSkuOptionService.findByKey(key);
		findByKeyCache.put(key, skuOption);
		return skuOption;
	}

	@Override
	public List<Long> getSkuOptionInUseUidList() {
		return fallbackSkuOptionService.getSkuOptionInUseUidList();
	}

	@Override
	public boolean isSkuOptionInUse(final long skuOptionUid) throws EpServiceException {
		return fallbackSkuOptionService.isSkuOptionInUse(skuOptionUid);
	}

	@Override
	public boolean isSkuOptionValueInUse(final long skuOptionValueUid) throws EpServiceException {
		return fallbackSkuOptionService.isSkuOptionValueInUse(skuOptionValueUid);
	}

	@Override
	public SkuOption saveOrUpdate(final SkuOption skuOption) throws EpServiceException {
		return fallbackSkuOptionService.saveOrUpdate(skuOption);
	}

	@Override
	public List<Long> getSkuOptionValueInUseUidList() {
		return fallbackSkuOptionService.getSkuOptionValueInUseUidList();
	}

	@Override
	public long findUidPkByKey(final String key) throws EpServiceException {
		return fallbackSkuOptionService.findUidPkByKey(key);
	}

	@Override
	public SkuOption addOptionValue(final SkuOptionValue skuOptionValue, final SkuOption skuOption) throws SkuOptionKeyExistException {
		return fallbackSkuOptionService.addOptionValue(skuOptionValue, skuOption);
	}

	@Override
	public boolean optionValueKeyExists(final String skuOptionValueKey) throws EpServiceException {
		return fallbackSkuOptionService.optionValueKeyExists(skuOptionValueKey);
	}

	@Override
	public SkuOptionValue findOptionValueByKey(final String key) throws EpServiceException {
		return fallbackSkuOptionService.findOptionValueByKey(key);
	}

	@Override
	public void notifySkuOptionUpdated(final SkuOption skuOption) {
		fallbackSkuOptionService.notifySkuOptionUpdated(skuOption);
	}

	@Override
	public void add(final SkuOptionValue skuOptionValue) throws SkuOptionKeyExistException {
		fallbackSkuOptionService.add(skuOptionValue);
	}

	@Override
	public SkuOptionValue update(final SkuOptionValue skuOptionValue) throws SkuOptionKeyExistException {
		return fallbackSkuOptionService.update(skuOptionValue);
	}

	@Override
	public void remove(final SkuOptionValue skuOptionValue) throws EpServiceException {
		fallbackSkuOptionService.remove(skuOptionValue);
	}

	public void setFindByKeyCache(final Cache<String, SkuOption> findByKeyCache) {
		this.findByKeyCache = findByKeyCache;
	}

	@Override
	public Object getObject(final long uid) throws EpServiceException {
		return get(uid);
	}

	public void setFallbackSkuOptionService(final SkuOptionService fallbackSkuOptionService) {
		this.fallbackSkuOptionService = fallbackSkuOptionService;
	}
}
