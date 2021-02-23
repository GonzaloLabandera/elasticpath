/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.caching.core.pricing;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.elasticpath.cache.Cache;
import com.elasticpath.domain.pricing.PriceAdjustment;
import com.elasticpath.service.pricing.PriceAdjustmentService;

/**
 * A cached version of the {@link PriceAdjustmentService} interface.
 */
public class CachingPriceAdjustmentServiceImpl implements PriceAdjustmentService {

	private Cache<PriceListGuidAndBundleConstituentsKey, Map<String, PriceAdjustment>> cache;
	private PriceAdjustmentService fallbackService;


	@Override
	public Map<String, PriceAdjustment> findByPriceListAndBundleConstituentsAsMap(final String priceListGuid,
																				  final Collection<String> bundleConstituentsIds) {

		final PriceListGuidAndBundleConstituentsKey cacheKey = new PriceListGuidAndBundleConstituentsKey(priceListGuid, bundleConstituentsIds);
		return cache.get(cacheKey, key -> fallbackService.findByPriceListAndBundleConstituentsAsMap(key.getPriceListGuid(),
				key.getBundleConstituentsIds()));
	}

	// non-cached calls

	@Override
	public void delete(final PriceAdjustment priceAdjustment) {
		fallbackService.delete(priceAdjustment);
	}

	@Override
	public List<PriceAdjustment> findByPriceList(final String plGuid) {
		return fallbackService.findByPriceList(plGuid);
	}

	@Override
	public Collection<PriceAdjustment> findAllAdjustmentsOnBundle(final String plGuid, final Collection<String> bcList) {
		return fallbackService.findAllAdjustmentsOnBundle(plGuid, bcList);
	}


	//getters/setters

	protected Cache<PriceListGuidAndBundleConstituentsKey, Map<String, PriceAdjustment>> getCache() {
		return cache;
	}

	public void setCache(final Cache<PriceListGuidAndBundleConstituentsKey, Map<String, PriceAdjustment>> cache) {
		this.cache = cache;
	}

	public void setFallbackService(final PriceAdjustmentService service) {
		this.fallbackService = service;
	}

	protected PriceAdjustmentService getFallbackService() {
		return fallbackService;
	}

}
