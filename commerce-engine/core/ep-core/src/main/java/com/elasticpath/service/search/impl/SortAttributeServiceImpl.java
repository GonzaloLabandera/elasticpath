/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.service.search.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.search.SortAttribute;
import com.elasticpath.domain.search.SortAttributeGroup;
import com.elasticpath.domain.search.SortValue;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;
import com.elasticpath.service.search.SortAttributeService;

/**
 * Default implementation of SortAttrbuteService.
 */
public class SortAttributeServiceImpl extends AbstractEpPersistenceServiceImpl implements SortAttributeService {

	private static final int BUSINESS_OBJECT_INDEX = 0;

	private static final int DESCENDING_INDEX = 1;

	private static final int ATTRIBUTE_TYPE_INDEX = 2;

	private static final int NAME_INDEX = 3;

	/**
	 * Find by store code and locale code.
	 */
	static final String SORT_ATTRIBUTE_FIND_BY_STORE_CODE_AND_LOCALE_CODE = "SORT_ATTRIBUTE_FIND_BY_STORE_CODE_AND_LOCALE_CODE";
	/**
	 * Find by guid.
	 */
	static final String SORT_ATTRIBUTE_FIND_BY_GUID = "SORT_ATTRIBUTE_FIND_BY_GUID";
	/**
	 * Find by guid and locale code.
	 */
	static final String SORT_ATTRIBUTE_NAME_BY_GUID_AND_LOCALE_CODE = "SORT_ATTRIBUTE_NAME_BY_GUID_AND_LOCALE_CODE";
	/**
	 * Find by id.
	 */
	static final String SORT_ATTRIBUTE_FIND_BY_UID = "SORT_ATTRIBUTE_FIND_BY_UID";
	/**
	 * Find by store code.
	 */
	static final String SORT_ATTRIBUTE_FIND_BY_STORE_CODE = "SORT_ATTRIBUTE_FIND_BY_STORE_CODE";

	private static final String SORT_ATTRIBUTE_GUIDS_LABEL = "sortAttributeGuids";
	private static final String CATALOG_IDS = "catalogIds";
	private static final String SORT_ATTRIBUTE_FIND_BY_GUIDS = "SORT_ATTRIBUTE_FIND_BY_GUIDS";
	private static final String SELECT_ALL_SORT_ATTRIBUTE_GUIDS = "SELECT_ALL_SORT_ATTRIBUTE_GUIDS";
	private static final String FIND_SORTABLE_PRODUCT_ATTRIBUTES = "FIND_SORTABLE_PRODUCT_ATTRIBUTES";
	private static final String DEFAULT_SORT_ATTRIBUTE_FIND_BY_STORE_CODE = "DEFAULT_SORT_ATTRIBUTE_FIND_BY_STORE_CODE";


	@Override
	public SortAttribute saveOrUpdate(final SortAttribute sortAttribute) throws EpServiceException {
		sanityCheck();

		SortAttribute persistedSortAttribute = findByGuid(sortAttribute.getGuid());
		sortAttribute.setUidPk(persistedSortAttribute == null ? 0 : persistedSortAttribute.getUidPk());

		return getPersistenceEngine().saveOrUpdate(sortAttribute);
	}

	@Override
	public void remove(final SortAttribute sortAttribute) throws EpServiceException {
		sanityCheck();
		getPersistenceEngine().delete(sortAttribute);
	}

	@Override
	public List<SortAttribute> findSortAttributesByStoreCode(final String storeCode) {
		sanityCheck();
		return getPersistenceEngine().retrieveByNamedQuery(SORT_ATTRIBUTE_FIND_BY_STORE_CODE, storeCode);
	}

	@Override
	public List<String> findSortAttributeGuidsByStoreCodeAndLocalCode(final String storeCode, final String localeCode) {
		sanityCheck();
		return getPersistenceEngine().retrieveByNamedQuery(SORT_ATTRIBUTE_FIND_BY_STORE_CODE_AND_LOCALE_CODE, storeCode, localeCode);
	}

	@Override
	public SortAttribute findByGuid(final String guid) {
		sanityCheck();
		List<SortAttribute> sortAttributes = getPersistenceEngine().retrieveByNamedQuery(SORT_ATTRIBUTE_FIND_BY_GUID, guid);

		return sortAttributes.stream()
				.findFirst()
				.orElse(null);
	}

	@Override
	public SortValue findSortValueByGuidAndLocaleCode(final String guid, final String localeCode) {
		sanityCheck();

		List<Object[]> list = getPersistenceEngine().retrieveByNamedQuery(SORT_ATTRIBUTE_NAME_BY_GUID_AND_LOCALE_CODE, guid, localeCode);

		return list.stream()
				.findFirst()
				.map(this::buildSortValue)
				.orElse(null);
	}

	private SortValue buildSortValue(final Object[] objects) {
		return new SortValue((String) objects[BUSINESS_OBJECT_INDEX],
				(Boolean) objects[DESCENDING_INDEX],
				(SortAttributeGroup) objects[ATTRIBUTE_TYPE_INDEX],
				(String) objects[NAME_INDEX]);
	}

	private SortAttribute findByUid(final long uid) {
		List<SortAttribute> sortAttributes = getPersistenceEngine().retrieveByNamedQuery(SORT_ATTRIBUTE_FIND_BY_UID, uid);

		return sortAttributes.stream()
				.findFirst()
				.orElse(null);
	}

	@Override
	public Object getObject(final long uid) throws EpServiceException {
		sanityCheck();
		return findByUid(uid);
	}


	@Override
	public List<SortAttribute> findByGuids(final List<String> sortAttributeGuids) {
		sanityCheck();

		return getPersistenceEngine().retrieveByNamedQueryWithList(SORT_ATTRIBUTE_FIND_BY_GUIDS, SORT_ATTRIBUTE_GUIDS_LABEL, sortAttributeGuids);
	}

	@Override
	public List<String> findAllGuids() {
		sanityCheck();

		return getPersistenceEngine().retrieveByNamedQuery(SELECT_ALL_SORT_ATTRIBUTE_GUIDS);
	}

	@Override
	public List<Attribute> findSortableProductAttributesByCatalogIds(final List<Long> catalogIds) {
		sanityCheck();
		List<Attribute> attributes = getPersistenceEngine().retrieveByNamedQueryWithList(FIND_SORTABLE_PRODUCT_ATTRIBUTES, CATALOG_IDS, catalogIds);

		return attributes.stream()
				.filter(attribute -> !attribute.isMultiValueEnabled())
				.collect(Collectors.toList());
	}

	@Override
	public SortAttribute getDefaultSortAttributeForStore(final String storeCode) {
		return (SortAttribute) getPersistenceEngine().retrieveByNamedQuery(DEFAULT_SORT_ATTRIBUTE_FIND_BY_STORE_CODE, storeCode).stream()
				.findFirst()
				.orElse(null);
	}

	@Override
	public void removeAllLocalizedName(final SortAttribute sortAttribute) {

		if (sortAttribute.getLocalizedNames() == null) {
			return;
		}

		sortAttribute.getLocalizedNames().values()
				.forEach(getPersistenceEngine()::delete);

		sortAttribute.getLocalizedNames().clear();
	}
}
