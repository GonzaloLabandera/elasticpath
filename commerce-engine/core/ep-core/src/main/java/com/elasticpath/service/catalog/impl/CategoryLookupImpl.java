/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.service.catalog.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.impl.AbstractCategoryImpl;
import com.elasticpath.persistence.api.FetchGroupLoadTuner;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.persistence.support.FetchGroupConstants;
import com.elasticpath.service.catalog.CategoryLookup;

/**
 * {@link com.elasticpath.service.catalog.CategoryLookup} implementation that reads categories directly from the persistence engine.
 */
public class CategoryLookupImpl implements CategoryLookup {
	/** Query which retrieves a CategoryImpl by guid. */
	protected static final String CATEGORY_SELECT_BY_GUID = "CATEGORY_SELECT_BY_GUID";
	/** Query which retrieves a CategoryImpl by guid and catalog code. */
	protected static final String CATEGORY_SELECT_BY_CODE_AND_CATALOG_CODE = "CATEGORY_SELECT_BY_CODE_AND_CATALOG_CODE";
	/** Query which retrieves a LinkedCategoryImpl by guid and catalog code. */
	protected static final String LINKED_CATEGORY_FIND_BY_CODE_AND_CATALOG_CODE = "LINKED_CATEGORY_FIND_BY_CODE_AND_CATALOG_CODE";
	/** Query which retrieves a CategoryImpl by parent guid. */
	protected static final String SUBCATEGORY_SELECT_BY_PARENT_GUID = "SUBCATEGORY_SELECT_BY_PARENT_GUID";

	private static final String PLACE_HOLDER_FOR_LIST = "list";

	private PersistenceEngine persistenceEngine;
	private BeanFactory beanFactory;
	private FetchGroupLoadTuner loadTuner;

	@Override
	@SuppressWarnings("unchecked")
	public <C extends Category> C findByUid(final long uidPk) {
		return (C) getPersistenceEngine()
			.withLoadTuners(getLoadTuner())
			.get(AbstractCategoryImpl.class, uidPk);
	}

	@Override
	public <C extends Category> List<C> findByUids(final Collection<Long> categoryUids) {
		return getPersistenceEngine()
			.withLoadTuners(getLoadTuner())
			.retrieveByNamedQueryWithList("CATEGORY_BY_UIDS", PLACE_HOLDER_FOR_LIST, categoryUids);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <C extends Category> C findByGuid(final String guid) {
		final List<Category> categories = getPersistenceEngine()
			.withLoadTuners(getLoadTuner())
			.retrieveByNamedQuery(CATEGORY_SELECT_BY_GUID, guid);

		if (categories.isEmpty()) {
			return null;
		}

		return (C) categories.get(0);
	}

	@Override
	public <C extends Category> C findByCategoryCodeAndCatalog(final String code, final Catalog catalog) {
		return findByCategoryAndCatalogCode(code, catalog.getCode());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <C extends Category> C findByCategoryAndCatalogCode(final String categoryCode, final String catalogCode) {
		if (catalogCode == null) {
			throw new EpServiceException("catalogCode cannot be null");
		}

		List<Category> categories = getPersistenceEngine()
			.withLoadTuners(getLoadTuner())
			.retrieveByNamedQuery(CATEGORY_SELECT_BY_CODE_AND_CATALOG_CODE, categoryCode, catalogCode);

		if (CollectionUtils.isEmpty(categories)) {
			categories = getPersistenceEngine()
				.withLoadTuners(getLoadTuner())
				.retrieveByNamedQuery(LINKED_CATEGORY_FIND_BY_CODE_AND_CATALOG_CODE, categoryCode, catalogCode);
		}

		if (categories.isEmpty()) {
			return null;
		}

		return (C) categories.get(0);
	}

	@Override
	public <C extends Category> C findByCompoundCategoryAndCatalogCodes(final String compoundGuid) {
		final String categoryCode = compoundGuid.substring(0, compoundGuid.indexOf(Category.CATEGORY_LEGACY_GUID_DELIMITER));
		final String catalogCode = compoundGuid.substring(compoundGuid.indexOf(Category.CATEGORY_LEGACY_GUID_DELIMITER)
				+ Category.CATEGORY_LEGACY_GUID_DELIMITER.length());
		try {
			return findByCategoryAndCatalogCode(categoryCode, catalogCode);
		} catch (final EpServiceException exception) {
			return null;
		}
	}

	@Override
	public <C extends Category> List<C> findChildren(final Category category) {
		List<C> children = getPersistenceEngine()
			.withLoadTuners(getLoadTuner())
			.retrieveByNamedQuery(SUBCATEGORY_SELECT_BY_PARENT_GUID, category.getGuid());

		List<C> sortedChildren = new ArrayList<>(children);
		Collections.sort(sortedChildren);

		return sortedChildren;
	}

	@Override
	public <C extends Category> C findParent(final Category child) {
		if (child.getParentGuid() == null) {
			return null;
		}

		return findByGuid(child.getParentGuid());
	}

	protected PersistenceEngine getPersistenceEngine() {
		return persistenceEngine;
	}

	public void setPersistenceEngine(final PersistenceEngine persistenceEngine) {
		this.persistenceEngine = persistenceEngine;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}

	/**
	 * Returns the load tuner used to load {@link Category} objects.
	 * @return the load tuner used to load {@link Category} objects.
	 */
	protected FetchGroupLoadTuner getLoadTuner() {
		if (loadTuner == null) {
			loadTuner = getBeanFactory().getPrototypeBean(ContextIdNames.FETCH_GROUP_LOAD_TUNER, FetchGroupLoadTuner.class);
			loadTuner.addFetchGroup(
					FetchGroupConstants.CATALOG,
					FetchGroupConstants.CATALOG_DEFAULTS,
					FetchGroupConstants.CATEGORY_AVAILABILITY,
					FetchGroupConstants.CATEGORY_BASIC,
					FetchGroupConstants.CATEGORY_INDEX,
					FetchGroupConstants.CATEGORY_LDF,
					FetchGroupConstants.CATEGORY_ATTRIBUTES);
		}

		return loadTuner;
	}
}
