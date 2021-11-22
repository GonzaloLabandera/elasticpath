/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.caching.core.catalog;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import com.antkorwin.xsync.XSync;

import com.elasticpath.base.cache.CacheResult;
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
 *
 * The caching implemented in this service is unique and different comparing to other caching services because the cached SKU options
 * are consumed during loading of product types and sku option values from the db.
 *
 * OpenJPA will always return a new instance of the same SKU option and option values, flooding the memory with numerous copies.
 * The problem aggravates when a sku option has thousands of values.
 * The SkuOption <-> SkuOptionValue relations form a huge tree of copies that eventually lead to OutOfMemory error.
 *
 * Using <strong>ProductTypePostLoadStrategy</strong> and <strong>ProductSkuOptionValuePostLoadStrategy</strong> strategies
 * the loading of product types and sku option values is redirected first to this service to obtain cached values.
 *
 * The service uses a single cache for storing sku options by key and product type id.
 *
 * Using two caches caused a performance degradation, thus the choice of using a single cache, at the expense of explict casting where required,
 * is justifiable.
 */
@SuppressWarnings("PMD.TooManyMethods")
public class CachingSkuOptionServiceImpl extends AbstractEpPersistenceServiceImpl implements SkuOptionService, MutableCachingService<SkuOption> {

	private final ProductTypeSkuOptionCacheCoheranceEnforcer cacheProductTypeOptionsBiConsumer = new ProductTypeSkuOptionCacheCoheranceEnforcer();
	private final XSync<String> skuOptionsSync = new XSync<>();

	private SkuOptionService fallbackSkuOptionService;
	private ProductTypeDao productTypeDao;
	private CachedInstanceDetachmentStrategy detachmentStrategy;

	/* This cache stores a single SkuOption per option key and a set of sku options per product type uid.
	   Splitting it into 2 different caches will result in performance degradation.
	 */
	private Cache<Object, Object> skuOptionsCache;

	/**
	 * Initialize skuOptions cache.
	 */
	public void init() {
		List<ProductType> productTypes = productTypeDao.list();
		productTypes.forEach(productType -> {
			Set<SkuOption> skuOptions = productType.getSkuOptions();

			detachSkuOptionsAndCache(productType.getUidPk(), skuOptions);
			skuOptions.forEach(skuOption -> detachSkuOptionAndCache(skuOption.getOptionKey(), skuOption));
		});
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
		return (SkuOption) skuOptionsCache.get(key, theKey -> fallbackGetByKey(key));
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
	@SuppressWarnings("unchecked")
	public Set<SkuOption> findByProductTypeUid(final Long productTypeUid) {
		return (Set<SkuOption>) skuOptionsCache.get(productTypeUid,
				theKey -> fallbackSkuOptionService.findByProductTypeUid(productTypeUid),
				cacheProductTypeOptionsBiConsumer);
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


	public void setSkuOptionsCache(final Cache<Object, Object> skuOptionsCache) {
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

	protected Cache<Object, Object> getSkuOptionsCache() {
		return skuOptionsCache;
	}
	public void setDetachmentStrategy(final CachedInstanceDetachmentStrategy detachmentStrategy) {
		this.detachmentStrategy = detachmentStrategy;
	}

	private SkuOption fallbackGetByKey(final String key) {
		SkuOption skuOption = fallbackSkuOptionService.findByKey(key);
		return detachSingleSkuOption(skuOption);
	}

	@Override
	public void cache(final SkuOption skuOption) {
		detachSkuOptionAndCache(skuOption.getOptionKey(), skuOption);
	}

	@Override
	public void invalidate(final SkuOption skuOption) {
		skuOptionsCache.remove(skuOption.getOptionKey());
	}

	@Override
	public void invalidateAll() {
		skuOptionsCache.removeAll();
	}

	private void detachSkuOptionAndCache(final String skuOptionKey, final SkuOption skuOption) {
		detachSingleSkuOption(skuOption);
		skuOptionsCache.put(skuOptionKey, skuOption);
	}

	private void detachSkuOptionsAndCache(final Long productTypeUid, final Set<SkuOption> skuOptions) {
		detachSkuOptions(skuOptions);
		skuOptionsCache.put(productTypeUid, skuOptions);
	}

	private void detachSkuOptions(final Set<SkuOption> skuOptions) {
		if (skuOptions != null) {
			skuOptions.forEach(this::detachSingleSkuOption);
		}
	}

	private SkuOption detachSingleSkuOption(final SkuOption skuOption) {
		if (skuOption != null) {
			detachmentStrategy.detach(skuOption);
			skuOption.getOptionValues().forEach(detachmentStrategy::detach);
		}

		return skuOption;
	}

	/**
	 * This function properly caches product type sku options.
	 *
	 * The <strong>apply</strong> method returns a new set with cached SKU options to avoid flooding the memory with duplicate options.
	 */
	private class ProductTypeSkuOptionCacheCoheranceEnforcer implements BiFunction<Object, Object, Object> {

		@Override
		@SuppressWarnings("unchecked")
		public Object apply(final Object productTypeUid, final Object fallbackProductTypeSkuOptionsObj) {
			Set<SkuOption> fallbackProdTypeSkuOptions = (Set<SkuOption>) fallbackProductTypeSkuOptionsObj;

			Set<SkuOption> productTypeSkuOptionsToCache = new HashSet<>(fallbackProdTypeSkuOptions.size());

			for (SkuOption skuOption : fallbackProdTypeSkuOptions) {
				String skuOptionKey = skuOption.getOptionKey();

				/* the synchronization ensures that 2 different product types have the same reference to a common sku option
				   during multi-thread access
				 */
				skuOptionsSync.execute(skuOptionKey, () -> {
					CacheResult<Object> cachedSkuOptionResult = skuOptionsCache.get(skuOptionKey);

					if (cachedSkuOptionResult.isPresent()) {
						productTypeSkuOptionsToCache.add((SkuOption) cachedSkuOptionResult.get());
					} else {
						detachSingleSkuOption(skuOption);
						skuOptionsCache.put(skuOptionKey, skuOption);
						productTypeSkuOptionsToCache.add(skuOption);
					}
				});
			}
			skuOptionsCache.put(productTypeUid, productTypeSkuOptionsToCache);

			//it's very important to return a set with **cached** options instead the original one to avoid flooding the memory with option copies
			return productTypeSkuOptionsToCache;
		}
	}
}
