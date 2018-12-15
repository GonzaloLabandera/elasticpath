/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.caching.core.faceting;

import java.util.List;
import java.util.Locale;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.cache.Cache;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.search.Facet;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;
import com.elasticpath.service.search.FacetService;

/**
 * Caching version of the Facet service
 */
public class CachingFacetServiceImpl  extends AbstractEpPersistenceServiceImpl implements FacetService {

	private Cache<String, Facet> findByGuidCache;
	private Cache<String, List<Facet>> searchableFacetsCache;
	private FacetService fallbackFacetService;


	@Override
	public Facet saveOrUpdate(final Facet facet) throws EpServiceException {
		return fallbackFacetService.saveOrUpdate(facet);
	}

	@Override
	public void remove(final Facet facet) throws EpServiceException {
		fallbackFacetService.remove(facet);
	}

	@Override
	public Facet getFacet(final long facetUid) throws EpServiceException {
		return fallbackFacetService.getFacet(facetUid);
	}

	@Override
	public List<Facet> findAllFacetsForStore(final String storeCode, final Locale defaultLocale) throws EpServiceException {
		return fallbackFacetService.findAllFacetsForStore(storeCode, defaultLocale);
	}

	@Override
	public List<Facet> findAllFacetableFacetsForStore(final String storeCode) throws EpServiceException {
		return fallbackFacetService.findAllFacetableFacetsForStore(storeCode);
	}

	@Override
	public List<Facet> findAllSearchableFacets(final String storeCode) {

		List<Facet> facets = this.searchableFacetsCache.get(storeCode);
		if (facets != null) {
			return facets;
		}
		List<Facet> allSearchableFacets = fallbackFacetService.findAllSearchableFacets(storeCode);
		searchableFacetsCache.put(storeCode, allSearchableFacets);
		return allSearchableFacets;
	}

	@Override
	public List<Attribute> findByCatalogsAndUsageNotFacetable(
			final int attributeUsageId,
			final int otherAttributeUsageId,
			final String storeCode,
			final List<Long> catalogUids) {
		return fallbackFacetService.findByCatalogsAndUsageNotFacetable(attributeUsageId, otherAttributeUsageId, storeCode, catalogUids);
	}

	@Override
	public List<SkuOption> findAllNotFacetableSkuOptionFromCatalogs(final String storeCode, final List<Long> catalogUids)
			throws EpServiceException {
		return fallbackFacetService.findAllNotFacetableSkuOptionFromCatalogs(storeCode, catalogUids);
	}

	@Override
	public Facet findByGuid(final String facetGuid) {
		if (findByGuidCache.get(facetGuid) != null) {
			return findByGuidCache.get(facetGuid);
		}
		Facet facet = fallbackFacetService.findByGuid(facetGuid);
		findByGuidCache.put(facetGuid, facet);

		return facet;
	}

	@Override
	public Object getObject(final long uid) throws EpServiceException {
		return getFacet(uid);
	}

	public void setFindByGuidCache(final Cache<String, Facet> findByGuidCache) {
		this.findByGuidCache = findByGuidCache;
	}

	public void setFallbackFacetService(final FacetService fallbackFacetService) {
		this.fallbackFacetService = fallbackFacetService;
	}

	public void setSearchableFacetsCache(final Cache<String, List<Facet>> searchableFacetsCache) {
		this.searchableFacetsCache = searchableFacetsCache;
	}
}
