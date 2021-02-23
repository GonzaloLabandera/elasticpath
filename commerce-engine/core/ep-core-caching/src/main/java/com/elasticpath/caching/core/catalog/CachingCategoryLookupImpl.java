/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.caching.core.catalog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import com.elasticpath.cache.Cache;
import com.elasticpath.cache.CacheLoader;
import com.elasticpath.cache.CacheResult;
import com.elasticpath.caching.core.MutableCachingService;
import com.elasticpath.commons.util.CategoryGuidUtil;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.service.catalog.CategoryLookup;

/**
 * A Caching implementation of the {@link com.elasticpath.service.catalog.CategoryLookup} interface.
 */
@SuppressWarnings("PMD.GodClass")
public class CachingCategoryLookupImpl implements CategoryLookup, MutableCachingService<Category> {
	private final CacheLoader<Long, Category> categoriesByUidLoader = new CategoriesByUidpkLoader();
	private final CacheLoader<String, Long> categoryUidByGuidLoader = new CategoryUidPkByGuidLoader();
	private final CacheLoader<String, Long> categoryUidByCompoundGuidLoader = new CategoryUidPkByCompoundGuidLoader();

	private final CategoryGuidUtil categoryGuidUtil = new CategoryGuidUtil();

	private Cache<Long, Category> categoryByUidCache;
	private Cache<String, Long> categoryUidByGuidCache;
	private Cache<String, Long> categoryUidByCompoundGuidCache;
	private Cache<Long, List<Long>> childCategoryCache;

	private CategoryLookup fallbackReader;


	@Override
	@SuppressWarnings("unchecked")
	public <C extends Category> C findByUid(final long uidPk) {
		return (C) getCategoryByUidCache().get(uidPk, categoriesByUidLoader::load);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <C extends Category> List<C> findByUids(final Collection<Long> categoryUids) {
		Map<Long, Category> resultMap = getCategoryByUidCache().getAll(categoryUids, categoriesByUidLoader::loadAll);
		return new ArrayList<>((Collection<C>) resultMap.values());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <C extends Category> C findByGuid(final String guid) {
		final Long categoryUidPk = getCategoryUidByGuidCache().get(guid, categoryUidByGuidLoader::load);
		final CacheResult<Category> category = getCategoryByUidCache().get(categoryUidPk);
		return category.isPresent()
				? (C) category.get()
				: null;
	}

	@Override
	public <C extends Category> C findByCategoryCodeAndCatalog(final String code, final Catalog catalog) {
		return findByCategoryAndCatalogCode(code, catalog.getGuid());
	}

	@Override
	public <C extends Category> C findByCategoryAndCatalogCode(final String categoryCode, final String catalogCode) {
		String compoundGuid = getCategoryGuidUtil().get(categoryCode, catalogCode);

		return findByCompoundCategoryAndCatalogCodes(compoundGuid);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <C extends Category> C findByCompoundCategoryAndCatalogCodes(final String compoundGuid) {
		final Long categoryUidPk = getCategoryUidByCompoundGuidCache().get(compoundGuid, categoryUidByCompoundGuidLoader::load);
		final CacheResult<Category> result = getCategoryByUidCache().get(categoryUidPk);
		return result.isPresent()
				? (C) result.get()
				: null;
	}

	@Override
	public <C extends Category> List<C> findChildren(final Category parent) {
		final CacheResult<List<Long>> childIds = getChildCategoryCache().get(parent.getUidPk());
		if (childIds.isPresent()) {
			return Objects.nonNull(childIds.get())
					? findByUids(childIds.get())
					: Collections.emptyList();
		}

		final List<C> children = getFallbackReader().findChildren(parent);
		final List<Long> resultIds = children.stream()
				.map(Persistable::getUidPk)
				.collect(ImmutableList.toImmutableList());
		getChildCategoryCache().put(parent.getUidPk(), resultIds);

		return children;
	}

	@Override
	public <C extends Category> C findParent(final Category child) {
		if (child.getParentGuid() == null) {
			return null;
		}

		return findByGuid(child.getParentGuid());
	}

	@Override
	public void cache(final Category entity) {
		getCategoryByUidCache().put(entity.getUidPk(), entity);
		getCategoryUidByGuidCache().put(entity.getGuid(), entity.getUidPk());
		getCategoryUidByCompoundGuidCache().put(entity.getCompoundGuid(), entity.getUidPk());
		// clears the category from child caches to have it be repopulated on the next request instead of having to perform a lookup here
		getChildCategoryCache().removeAll();
	}

	@Override
	public void invalidate(final Category entity) {
		getCategoryByUidCache().remove(entity.getUidPk());
		getCategoryUidByGuidCache().remove(entity.getGuid());
		getCategoryUidByCompoundGuidCache().remove(entity.getCompoundGuid());
		getChildCategoryCache().remove(entity.getUidPk());
	}

	@Override
	public void invalidateAll() {
		getCategoryByUidCache().removeAll();
		getCategoryUidByGuidCache().removeAll();
		getCategoryUidByCompoundGuidCache().removeAll();
		getChildCategoryCache().removeAll();
	}

	protected Cache<Long, Category> getCategoryByUidCache() {
		return categoryByUidCache;
	}

	public void setCategoryByUidCache(final Cache<Long, Category> categoryByUidCache) {
		this.categoryByUidCache = categoryByUidCache;
	}

	protected Cache<String, Long> getCategoryUidByGuidCache() {
		return categoryUidByGuidCache;
	}

	public void setCategoryUidByGuidCache(final Cache<String, Long> categoryUidByGuidCache) {
		this.categoryUidByGuidCache = categoryUidByGuidCache;
	}

	protected Cache<String, Long> getCategoryUidByCompoundGuidCache() {
		return categoryUidByCompoundGuidCache;
	}

	public void setCategoryUidByCompoundGuidCache(final Cache<String, Long> categoryUidByCompoundGuidCache) {
		this.categoryUidByCompoundGuidCache = categoryUidByCompoundGuidCache;
	}

	protected CategoryLookup getFallbackReader() {
		return fallbackReader;
	}

	public void setFallbackReader(final CategoryLookup fallbackReader) {
		this.fallbackReader = fallbackReader;
	}

	protected CategoryGuidUtil getCategoryGuidUtil() {
		return categoryGuidUtil;
	}

	public void setChildCategoryCache(final Cache<Long, List<Long>> childCategoryCache) {
		this.childCategoryCache = childCategoryCache;
	}

	protected Cache<Long, List<Long>> getChildCategoryCache() {
		return childCategoryCache;
	}

	protected CacheLoader<Long, Category> getCategoriesByUidLoader() {
		return categoriesByUidLoader;
	}

	protected CacheLoader<String, Long> getCategoryUidByGuidLoader() {
		return categoryUidByGuidLoader;
	}

	protected CacheLoader<String, Long> getCategoryUidByCompoundGuidLoader() {
		return categoryUidByCompoundGuidLoader;
	}

	/**
	 * CacheLoader which loads Categories by uidPk.
	 */
	protected class CategoriesByUidpkLoader implements CacheLoader<Long, Category> {
		@Override
		public Category load(final Long key) {
			Category category = getFallbackReader().findByUid(key);

			cacheCategoryUidIfRequired(category, categoryUidByGuidCache);
			cacheCategoryUidIfRequired(category, categoryUidByCompoundGuidCache);

			return category;
		}

		@Override
		public Map<Long, Category> loadAll(final Iterable<? extends Long> keys) {
			List<Category> loaded = getFallbackReader().findByUids(Lists.newArrayList(keys));
			Map<Long, Category> result = new LinkedHashMap<>(loaded.size() * 2);
			for (Category category : loaded) {
				result.put(category.getUidPk(), category);

				cacheCategoryUidIfRequired(category, categoryUidByGuidCache);
				cacheCategoryUidIfRequired(category, categoryUidByCompoundGuidCache);
			}

			return result;
		}
	}

	/**
	 * CacheLoader which loads Categories by Guid.
	 */
	protected class CategoryUidPkByGuidLoader implements CacheLoader<String, Long> {
		@Override
		public Long load(final String key) {
			Category category = getFallbackReader().findByGuid(key);

			cacheCategoryIfRequired(category, categoryByUidCache);
			cacheCategoryUidIfRequired(category, categoryUidByCompoundGuidCache);

			return category == null ? null
					: category.getUidPk();
		}

		@Override
		public Map<String, Long> loadAll(final Iterable<? extends String> keys) {

			throw new UnsupportedOperationException("Not yet implemented");
		}
	}

	/**
	 * CacheLoader which loads Categories by Compound Guid (CategoryCode + CatalogCode).
	 */
	protected class CategoryUidPkByCompoundGuidLoader implements CacheLoader<String, Long> {
		@Override
		public Long load(final String key) {
			Category category = getFallbackReader().findByCompoundCategoryAndCatalogCodes(key);

			cacheCategoryIfRequired(category, categoryByUidCache);
			cacheCategoryUidIfRequired(category, categoryUidByGuidCache);

			return category == null ? null
					: category.getUidPk();
		}

		@Override
		public Map<String, Long> loadAll(final Iterable<? extends String> keys) {

			throw new UnsupportedOperationException("Not yet implemented");
		}

	}

	private void cacheCategoryIfRequired(final Category dbCategory, final Cache<Long, Category> cache) {
		if (dbCategory != null && !cache.get(dbCategory.getUidPk()).isPresent()) {
			cache.put(dbCategory.getUidPk(), dbCategory);
		}
	}

	private void cacheCategoryUidIfRequired(final Category dbCategory, final Cache<String, Long> cache) {
		if (dbCategory == null) {
			return;
		}

		String guid = cache.equals(categoryUidByGuidCache) ? dbCategory.getGuid()
				: dbCategory.getCompoundGuid();

		if (guid != null && !cache.get(guid).isPresent()) {
			cache.put(guid, dbCategory.getUidPk());
		}
	}
}
