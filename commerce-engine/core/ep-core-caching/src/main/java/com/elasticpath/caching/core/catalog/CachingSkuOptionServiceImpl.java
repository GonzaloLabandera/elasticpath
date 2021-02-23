/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.caching.core.catalog;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.cache.Cache;
import com.elasticpath.caching.core.MutableCachingService;
import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.persistence.api.CachedInstanceDetachmentStrategy;
import com.elasticpath.persistence.dao.ProductTypeDao;
import com.elasticpath.service.catalog.SkuOptionKeyExistException;
import com.elasticpath.service.catalog.SkuOptionService;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;

/**
 * Caching version of the sku option service.
 */
@SuppressWarnings("PMD.TooManyMethods")
public class CachingSkuOptionServiceImpl extends AbstractEpPersistenceServiceImpl implements SkuOptionService, MutableCachingService<SkuOption> {

	private SkuOptionService fallbackSkuOptionService;
	private ProductTypeDao productTypeDao;
	private CachedInstanceDetachmentStrategy detachmentStrategy;

	private Cache<SkuOptionCacheKey, SkuOption> skuOptionsCache;

	/**
	 * Initialize skuOption cache.
	 */
	public void init() {
		List<ProductType> productTypes = productTypeDao.list();

		Map<SkuOption, SkuOptionCacheKey> skuOptionToCacheKeyMap = new HashMap<>();

		for (ProductType productType : productTypes) {
			cacheSkuOptionsInMap(skuOptionToCacheKeyMap, productType.getSkuOptions(), productType.getUidPk());
		}

		flushSkuOptionsToCache(skuOptionToCacheKeyMap);
	}

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
		final SkuOptionCacheKey cacheKey = SkuOptionCacheKey.of(key);
		return skuOptionsCache.get(cacheKey, thisKey -> fallbackSkuOptionService.findByKey(key));
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

	@Override
	public Object getObject(final long uid) throws EpServiceException {
		return get(uid);
	}

	@Override
	public Set<SkuOption> findByProductTypeUid(final Long productTypeUid) {
		SkuOptionCacheKey cacheKey = SkuOptionCacheKey.of(productTypeUid);

		Collection<SkuOption> skuOptions = skuOptionsCache.getAllByPartialKey(cacheKey);

		if (null == skuOptions) {
			skuOptions = fallbackSkuOptionService.findByProductTypeUid(productTypeUid);

			Map<SkuOption, SkuOptionCacheKey> skuOptionToCacheKeyMap = new HashMap<>();

			cacheSkuOptionsInMap(skuOptionToCacheKeyMap, skuOptions, productTypeUid);

			flushSkuOptionsToCache(skuOptionToCacheKeyMap);
		}

		return new HashSet<>(skuOptions);
	}

	@Override
	public SkuOptionValue findOptionValueByOptionAndValueKeys(final String optionKey, final String optionValueKey) throws EpServiceException {
		SkuOption skuOption = findByKey(optionKey);

		return skuOption.getOptionValues().stream()
				.filter(skuOptionValue -> skuOptionValue.getOptionValueKey().equals(optionValueKey))
				.findFirst()
				.get();
	}

	@Override
	public SkuOptionValue findOptionValueByOptionKeyAndValueUid(final String optionKey, final Long skuOptionValueUid) {
		SkuOption skuOption = findByKey(optionKey);

		return skuOption.getOptionValues().stream()
				.filter(skuOptionValue -> skuOptionValue.getUidPk() == skuOptionValueUid)
				.findFirst()
				.get();
	}


	public void setSkuOptionsCache(final Cache<SkuOptionCacheKey, SkuOption> skuOptionsCache) {
		this.skuOptionsCache = skuOptionsCache;
	}

	public void setFallbackSkuOptionService(final SkuOptionService fallbackSkuOptionService) {
		this.fallbackSkuOptionService = fallbackSkuOptionService;
	}

	public void setProductTypeDao(final ProductTypeDao productTypeDao) {
		this.productTypeDao = productTypeDao;
	}

	protected SkuOptionService getFallbackSkuOptionService() {
		return fallbackSkuOptionService;
	}

	protected ProductTypeDao getProductTypeDao() {
		return productTypeDao;
	}

	protected Cache<SkuOptionCacheKey, SkuOption> getSkuOptionsCache() {
		return skuOptionsCache;
	}

	private void cacheSkuOptionsInMap(final Map<SkuOption, SkuOptionCacheKey> skuOptionToCacheKeyMap, final Collection<SkuOption> skuOptions,
									  final Long productTypeUid) {

		skuOptions
				.forEach(skuOption ->
						skuOptionToCacheKeyMap.computeIfAbsent(skuOption, val -> SkuOptionCacheKey.of(skuOption.getOptionKey()))
								.withProductTypeUid(productTypeUid)

				);
	}

	private void flushSkuOptionsToCache(final Map<SkuOption, SkuOptionCacheKey> skuOptionToCacheKeyMap) {
		skuOptionToCacheKeyMap.forEach((skuOption, cacheKey) -> detachAndCache(cacheKey, skuOption));

		//don't wait for GC - SKU options may contain thousands of SKU values or there could be thousands of options (or any combination)
		skuOptionToCacheKeyMap.clear();
	}

	@Override
	public void cache(final SkuOption entity) {
		detachAndCache(SkuOptionCacheKey.of(entity.getOptionKey()), entity);
	}

	@Override
	public void invalidate(final SkuOption entity) {
		skuOptionsCache.remove(SkuOptionCacheKey.of(entity.getOptionKey()));
	}

	@Override
	public void invalidateAll() {
		skuOptionsCache.removeAll();
	}

	private void detachAndCache(final SkuOptionCacheKey cacheKey, final SkuOption skuOption) {
		detachSkuOptionsAndValues(skuOption);
		skuOptionsCache.put(cacheKey, skuOption);
	}

	private void detachSkuOptionsAndValues(final SkuOption skuOption) {
		if (skuOption != null) {
			detachmentStrategy.detach(skuOption);
			skuOption.getOptionValues().forEach(
					skuOptionValue -> detachmentStrategy.detach(skuOptionValue)
			);
		}
	}

	public void setDetachmentStrategy(final CachedInstanceDetachmentStrategy detachmentStrategy) {
		this.detachmentStrategy = detachmentStrategy;
	}
}
