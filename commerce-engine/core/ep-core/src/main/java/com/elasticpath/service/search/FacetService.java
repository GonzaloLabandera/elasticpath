/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.service.search;

import java.util.List;
import java.util.Locale;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.search.Facet;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.service.EpPersistenceService;

/**
 * This interface provides facet related services.
 */
public interface FacetService extends EpPersistenceService {

	/**
	 * Saves or updates a given facet.
	 *
	 * @param facet the facet to save or update.
	 * @return Facet the updated facet.
	 * @throws EpServiceException in case of any errors.
	 */
	Facet saveOrUpdate(Facet facet) throws EpServiceException;

	/**
	 * Deletes the facet.
	 *
	 * @param facet the facet to remove.
	 * @throws EpServiceException in case of any errors.
	 */
	void remove(Facet facet) throws EpServiceException;


	/**
	 * Gets a facet with a given UID. Return null if no matching record exists.
	 *
	 * @param facetUid the UID.
	 * @return the facet with the attributes populated if the UID exists, otherwise null.
	 * @throws EpServiceException in case of any errors.
	 */
	Facet getFacet(long facetUid) throws EpServiceException;

	/**
	 * Finds all searchable facets in store and set the localized display name for the given locale.
	 *
	 * @param storeCode the store code
	 * @param defaultLocale the default locale.
	 * @return a collection of facets.
	 * @throws EpServiceException in case of any errors.
	 */
	List<Facet> findAllFacetsForStore(String storeCode, Locale defaultLocale) throws EpServiceException;

	/**
	 * Finds a facets in store by businessObjectId.
	 *
	 * @param storeCode the store code
	 * @param businessObjectId tbusinessObjectId.
	 * @return a facet.
	 * @throws EpServiceException in case of any errors.
	 */
	Facet findFacetByStoreAndBusinessObjectId(String storeCode, String businessObjectId) throws EpServiceException;

	/**
	 * Finds all searchable facets in store and set the localized display name for the given locale.
	 *
	 * @param storeCode the store code
	 * @return a collection of facets.
	 * @throws EpServiceException in case of any errors.
	 */
	List<Facet> findAllFacetableFacetsForStore(String storeCode) throws EpServiceException;

	/**
	 * Finds all searchable attributes.
	 *
	 * @param storeCode store code
	 * @return attributes that are searchable
	 */
	List<Facet> findAllSearchableFacets(String storeCode);

	/**
	 * Find the attributes with the given catalog and attribute usage that are not currently facetable.
	 *
	 * @param attributeUsageId the attributeUsage id
	 * @param otherAttributeUsageId other attributeUsage id
	 * @param storeCode store code
	 * @param catalogUids the catalog uidPks
	 * @return List of attributes by the given catalog and attribute usage id
	 */
	List<Attribute> findByCatalogsAndUsageNotFacetable(int attributeUsageId, int otherAttributeUsageId, String storeCode, List<Long> catalogUids);

	/**
	 * Finds all the {@link SkuOption}s for the specified catalog UID that are not currently facetable.
	 *
	 * @param storeCode store code
	 * @return a {@link List} of {@link SkuOption}s
	 * @param catalogUids the catalog uidPks
	 * @throws EpServiceException in case of any errors
	 */
	List<SkuOption> findAllNotFacetableSkuOptionFromCatalogs(String storeCode, List<Long> catalogUids) throws EpServiceException;

	/**
	 * Finds a facet by facet guid field.
	 *
	 * @param facetGuid facet guid
	 * @return facet object
	 */
	Facet findByGuid(String facetGuid);

	/**
	 * Find facets with guids in the list.
	 * @param facetGuids guids
	 * @return Facets
	 */
	List<Facet> findByGuids(List<String> facetGuids);

	/**
	 * Get all facet guids.
	 * @return guids
	 */
	List<String> findAllGuids();
}