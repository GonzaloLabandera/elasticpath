/*
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.tools.sync.target.impl;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.service.catalog.CategoryService;
import com.elasticpath.tools.sync.exception.SyncToolConfigurationException;
import com.elasticpath.tools.sync.exception.SyncToolRuntimeException;
import com.elasticpath.tools.sync.merge.impl.MergeExceptionFactory;

/**
 * Category dao adapter.
 */
public class CategoryDaoAdapterImpl extends AbstractDaoAdapter<Category> {
	
	private CategoryService categoryService;
	
	private BeanFactory beanFactory;

	@Override
	public void add(final Category newPersistence) throws SyncToolRuntimeException {
		verifyThatTheParentCategoryExists(newPersistence);

		categoryService.add(newPersistence);

		if (newPersistence.isLinked()) {
			categoryService.addLinkedCategoryProducts(newPersistence);
		}
	}

	@Override
	public Category createBean(final Category category) {
		if (category.isLinked()) {
			return beanFactory.getBean(ContextIdNames.LINKED_CATEGORY);
		}
		return beanFactory.getBean(ContextIdNames.CATEGORY);
	}

	@Override
	public Category get(final String guid) {
		try {
			return (Category) getEntityLocator().locatePersistence(guid, Category.class);
		} catch (SyncToolConfigurationException e) {
			throw new SyncToolRuntimeException("Unable to locate persistence", e);
		}
	}

	@Override
	public boolean remove(final String guid) throws SyncToolRuntimeException {
		final Category category = get(guid);
		if (category == null) {
			return false;
		}

		if (category.isLinked()) {
			categoryService.removeLinkedCategoryTree(category);
		} else {
			categoryService.removeCategoryTree(category.getUidPk());
		}
		return true;
	}

	@Override
	public Category update(final Category mergedPersistence) throws SyncToolRuntimeException {
		verifyThatTheParentCategoryExists(mergedPersistence);

		return categoryService.update(mergedPersistence);
	}

	/**
	 * Verifies that the category's parent category guid is valid (i.e. exists) before persistence.
	 *
	 * @param category the category to verify
	 */
	protected void verifyThatTheParentCategoryExists(final Category category) {
		if (category.getParentGuid() == null) {
			return;
		}

		if (!categoryService.isGuidInUse(category.getParentGuid())) {
			throw MergeExceptionFactory.createEntityNotFoundException(category.getClass(), category.getParentGuid());
		}
	}

	/**
	 * @param categoryService the categoryService to set
	 */
	public void setCategoryService(final CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	/**
	 * @param beanFactory the beanFactory to set
	 */
	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

}
