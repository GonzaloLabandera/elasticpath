/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.service.search;

import java.util.List;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.search.SortAttribute;
import com.elasticpath.domain.search.SortValue;
import com.elasticpath.service.EpPersistenceService;

/**
 * Sort Attribute service for adding, removing, and retrieving sort attributes.
 */
public interface SortAttributeService extends EpPersistenceService {

	/**
	 * Save or update a sort attribute.
	 * @param sortAttribute sort attribute
	 * @throws EpServiceException in case of any errors.
	 * @return the sort attribute
	 */
	SortAttribute saveOrUpdate(SortAttribute sortAttribute) throws EpServiceException;

	/**
	 * Remove a sort attribute.
	 * @throws EpServiceException in case of any errors.
	 * @param sortAttribute sort attribute
	 */
	void remove(SortAttribute sortAttribute) throws EpServiceException;

	/**
	 * Find all sort attributes by a store.
	 * @param storeCode code of the store
	 * @return sort attributes
	 */
	List<SortAttribute> findSortAttributesByStoreCode(String storeCode);

	/**
	 * Find all sort values by a store and locale code.
	 * @param storeCode code of the store
	 * @param localeCode locale code
	 * @return sort value
	 */
	List<String> findSortAttributeGuidsByStoreCodeAndLocalCode(String storeCode, String localeCode);

	/**
	 * Find a sort attribute.
	 * @param guid guid
	 * @return sort attribute
	 */
	SortAttribute findByGuid(String guid);

	/**
	 * Find sort attributes with guids in the list.
	 * @param sortAttributeGuids guids
	 * @return list of SortAttribute
	 */
	List<SortAttribute> findByGuids(List<String> sortAttributeGuids);

	/**
	 * Get all sort attribute guids.
	 * @return guids
	 */
	List<String> findAllGuids();

	/**
	 * Find a sort value by guid and locale code.
	 * @param guid guid
	 * @param localeCode locale code
	 * @return sort value
	 */
	SortValue findSortValueByGuidAndLocaleCode(String guid, String localeCode);

	/**
	 * Find all sortable product attributes that are within the catalog ids.
	 * @param catalogIds uipks of catalogs
	 * @return attributes
	 */
	List<Attribute> findSortableProductAttributesByCatalogIds(List<Long> catalogIds);

	/**
	 * Gets the default sort for a store.
	 * @param storeCode store code
	 * @return default sort
	 */
	SortAttribute getDefaultSortAttributeForStore(String storeCode);
}
