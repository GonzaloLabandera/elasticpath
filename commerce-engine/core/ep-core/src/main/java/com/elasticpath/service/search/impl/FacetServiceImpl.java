/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.service.search.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.common.dto.search.RangeFacet;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.search.Facet;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;
import com.elasticpath.service.search.FacetService;

/**
 * Default implementation of {@link FacetService}
 */
@SuppressWarnings("unchecked")
public class FacetServiceImpl extends AbstractEpPersistenceServiceImpl implements FacetService {

	private static final Logger LOG = LoggerFactory.getLogger(FacetServiceImpl.class);

	private static final String CATALOG_IDS_LABEL = "catalogIds";

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public Facet saveOrUpdate(final Facet facet) throws EpServiceException {
		sanityCheck();
		final Facet foundFacet = findByGuid(facet.getFacetGuid());
		facet.setUidPk(foundFacet == null ? 0 : foundFacet.getUidPk());
		return getPersistenceEngine().saveOrUpdate(facet);
	}

	@Override
	public void remove(final Facet facet) throws EpServiceException {
		sanityCheck();
		getPersistenceEngine().delete(facet);
	}

	public Facet getFacet(final long facetUid) throws EpServiceException {
		sanityCheck();
		if (facetUid <= 0) {
			return getBean(ContextIdNames.FACET_SERVICE);
		}
		return getPersistentBeanFinder().get(ContextIdNames.FACET_SERVICE, facetUid);
	}

	@Override
	public List<Facet> findAllFacetsForStore(final String storeCode, final Locale locale) throws EpServiceException {
		sanityCheck();
		List<Facet> facetsFromDatabase = getPersistenceEngine().retrieveByNamedQuery("FIND_ALL_FACETS_BY_STORE_CODE", storeCode);

		for (Facet facet : facetsFromDatabase) {
			if (facet != null) {
				facet.setDisplayNameMap(getFacetDisplayMap(facet));
				facet.setSortedRangeFacet(getRangeFacetMap(facet.getRangeFacetValues()));
			}
		}

		return facetsFromDatabase;
	}

	@Override
	public List<Facet> findAllFacetableFacetsForStore(final String storeCode) throws EpServiceException {
		sanityCheck();
		List<Facet> facetsFromDatabase = getPersistenceEngine().retrieveByNamedQuery("FIND_FACETABLE_FACETS_BY_STORE_CODE", storeCode);

		for (Facet facet : facetsFromDatabase) {
			if (facet != null) {
				facet.setDisplayNameMap(getFacetDisplayMap(facet));
				facet.setSortedRangeFacet(getRangeFacetMap(facet.getRangeFacetValues()));
			}
		}

		return facetsFromDatabase;
	}

	@Override
	public List<Facet> findAllSearchableFacets(final String storeCode) {
		sanityCheck();

		return getPersistenceEngine().retrieveByNamedQuery("FIND_ALL_SEARCHABLE_FACETS_BY_STORE_CODE", storeCode);
	}

	@Override
	public Object getObject(final long uid) throws EpServiceException {
		return getFacet(uid);
	}

	private Map<String, String> getFacetDisplayMap(final Facet facet) {
		Map<String, String> map = new HashMap<>();
		try {
			if (facet != null && facet.getDisplayName() != null) {
				map = objectMapper.readValue(facet.getDisplayName(), Map.class);
			}
		} catch (IOException e) {
			LOG.error("Error trying to parse facet display name JSON: ", e);
		}
		return map;
	}

	private SortedSet<RangeFacet> getRangeFacetMap(final String rangeFaceValuesAsString) {
		SortedSet<RangeFacet> rangeFacets = new TreeSet<>();
		try {
			if (rangeFaceValuesAsString != null) {
				rangeFacets.addAll(Arrays.asList(objectMapper.readValue(rangeFaceValuesAsString, RangeFacet[].class)));
			}
		} catch (IOException e) {
			LOG.error("Error trying to parse range facet JSON: ", e);
		}
		return rangeFacets;
	}

	@Override
	public List<Attribute> findByCatalogsAndUsageNotFacetable(final int attributeUsageId, final int otherAttributeUsageId, final String storeCode,
															  final List<Long> catalogUids) {
		sanityCheck();

		return getPersistenceEngine().retrieveByNamedQueryWithList("ATTRIBUTE_FIND_BY_CATALOG_USAGE_NOT_FACETABLE", CATALOG_IDS_LABEL,
				catalogUids, attributeUsageId, otherAttributeUsageId, storeCode);
	}

	@Override
	public List<SkuOption> findAllNotFacetableSkuOptionFromCatalogs(final String storeCode, final List<Long> catalogUids) throws EpServiceException {
		sanityCheck();

		return getPersistenceEngine().retrieveByNamedQueryWithList("SKU_OPTION_SELECT_CATALOG_NOT_FACETABLE", CATALOG_IDS_LABEL,
				catalogUids, storeCode);
	}

	@Override
	public Facet findByGuid(final String facetGuid) {
		sanityCheck();

		final List<Facet> facetList = getPersistenceEngine().retrieveByNamedQuery("FACET_FIND_BY_GUID", facetGuid);
		facetList.forEach(facet -> {
			facet.setDisplayNameMap(getFacetDisplayMap(facet));
			facet.setSortedRangeFacet(getRangeFacetMap(facet.getRangeFacetValues()));
		});

		return facetList.stream()
				.findFirst()
				.orElse(null);
	}
}