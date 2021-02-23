/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.caching.core.faceting;

import java.util.List;

import com.elasticpath.cache.Cache;
import com.elasticpath.domain.search.Facet;
import com.elasticpath.service.search.FacetService;
import com.elasticpath.service.search.impl.CacheableFacetService;

/**
 * Caching version of the Facet service.
 */
public class CachingFacetServiceImpl implements CacheableFacetService {

	private Cache<String, Facet> findByGuidCache;
	private Cache<String, List<Facet>> searchableFacetsCache;
	private FacetService decorated;

	@Override
	public List<Facet> findAllSearchableFacets(final String storeCode) {
		return searchableFacetsCache.get(storeCode, cacheKey -> getDecorated().findAllSearchableFacets(storeCode));
	}

	@Override
	public Facet findByGuid(final String facetGuid) {
		return findByGuidCache.get(facetGuid, cacheKey -> getDecorated().findByGuid(facetGuid));
	}

	@Override
	public void setDecorated(final FacetService decorated) {
		this.decorated = decorated;
	}

	@Override
	public FacetService getDecorated() {
		return decorated;
	}

	public void setFindByGuidCache(final Cache<String, Facet> findByGuidCache) {
		this.findByGuidCache = findByGuidCache;
	}

	public void setSearchableFacetsCache(final Cache<String, List<Facet>> searchableFacetsCache) {
		this.searchableFacetsCache = searchableFacetsCache;
	}
}
