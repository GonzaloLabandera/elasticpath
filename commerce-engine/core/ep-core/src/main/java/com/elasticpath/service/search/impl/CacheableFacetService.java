/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.service.search.impl;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.search.Facet;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.service.search.FacetService;

/**
 * This class implements the decorator pattern and it is intended for subclassing by
 * caching implementations.
 *
 * It extends {@link FacetService} and delegates all non-overridden calls to the
 * non-caching implementation.
 *
 * It is the responsibility of the sub-class to inject required decorated, non-caching, {@link FacetServiceImpl}.
 *
 */
public interface CacheableFacetService extends FacetService {

	@Override
	default Facet saveOrUpdate(Facet facet) throws EpServiceException {
		return getDecorated().saveOrUpdate(facet);
	}

	@Override
	default void remove(Facet facet) throws EpServiceException {
		getDecorated().remove(facet);
	}

	@Override
	default Facet getFacet(long facetUid) throws EpServiceException {
		return getDecorated().getFacet(facetUid);
	}

	@Override
	default List<Facet> findAllFacetsForStore(String storeCode, Locale defaultLocale) throws EpServiceException {
		return findAllFacetsForStore(storeCode, defaultLocale);
	}

	@Override
	default Facet findFacetByStoreAndBusinessObjectId(String storeCode, String businessObjectId) throws EpServiceException {
		return getDecorated().findFacetByStoreAndBusinessObjectId(storeCode, businessObjectId);
	}

	@Override
	default List<Facet> findAllFacetableFacetsForStore(String storeCode) throws EpServiceException {
		return getDecorated().findAllFacetableFacetsForStore(storeCode);
	}

	@Override
	default List<Facet> findAllSearchableFacets(String storeCode) {
		return getDecorated().findAllSearchableFacets(storeCode);
	}

	@Override
	default List<Attribute> findByCatalogsAndUsageNotFacetable(int attributeUsageId, int otherAttributeUsageId, String storeCode,
															   List<Long> catalogUids) {
		return getDecorated().findByCatalogsAndUsageNotFacetable(attributeUsageId, otherAttributeUsageId, storeCode, catalogUids);
	}

	@Override
	default List<SkuOption> findAllNotFacetableSkuOptionFromCatalogs(String storeCode, List<Long> catalogUids) throws EpServiceException {
		return getDecorated().findAllNotFacetableSkuOptionFromCatalogs(storeCode, catalogUids);
	}

	@Override
	default Facet findByGuid(String facetGuid) {
		return getDecorated().findByGuid(facetGuid);
	}

	@Override
	default List<Facet> findByGuids(List<String> facetGuids) {
		return getDecorated().findByGuids(facetGuids);
	}

	@Override
	default List<String> findAllGuids() {
		return getDecorated().findAllGuids();
	}

	@Override
	default void setPersistenceEngine(PersistenceEngine persistenceEngine) {
		getDecorated().setPersistenceEngine(persistenceEngine);
	}

	@Override
	default PersistenceEngine getPersistenceEngine() {
		return getDecorated().getPersistenceEngine();
	}

	@Override
	default Object getObject(long uid) throws EpServiceException {
		return getDecorated().getObject(uid);
	}

	@Override
	default Object getObject(long uid, Collection<String> fieldsToLoad) throws EpServiceException {
		return getDecorated().getObject(uid, fieldsToLoad);
	}

	/**
	 * Set a {@link FacetService} to decorate.
	 *
	 * @param decorated {@link FacetService}.
	 */
	void setDecorated(FacetService decorated);

	/**
	 * Get decorated {@link FacetService}.
	 *
	 * @return Decorated {@link FacetService}.
	 */
	FacetService getDecorated();
}