/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.caching.core.catalog;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.cache.Cache;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.service.catalog.CategoryService;

/**
 * Caching version of the category service.
 */
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.GodClass" })
public class CachingCategoryServiceImpl implements CategoryService {

	private Cache<Long, String> findCodeByUidCache;
	private Cache<Long, List<Long>> findDescendantCategoriesByUidCache;
	private Cache<Long, List<Long>> findFeaturedProductsUidCache;
	private CategoryService fallbackCategoryService;

	@Override
	public String findCodeByUid(final long uidPk) {
		return findCodeByUidCache.get(uidPk, key -> fallbackCategoryService.findCodeByUid(uidPk));
	}

	@Override
	public Category add(final Category category) throws EpServiceException {
		return fallbackCategoryService.add(category);
	}

	@Override
	public Category update(final Category category) throws EpServiceException {
		return fallbackCategoryService.update(category);
	}

	@Override
	public List<Category> listRootCategories(final Catalog catalog, final boolean availableOnly) {
		return fallbackCategoryService.listRootCategories(catalog, availableOnly);
	}

	@Override
	public Category findByCode(final String categoryCode) {
		return fallbackCategoryService.findByCode(categoryCode);
	}

	@Override
	public List<Long> findUidsByAttribute(final Attribute attribute) {
		return fallbackCategoryService.findUidsByAttribute(attribute);
	}

	@Override
	public List<String> findCodesByUids(final List<Long> productUids) {
		return fallbackCategoryService.findCodesByUids(productUids);
	}

	@Override
	public Category saveOrUpdate(final Category category) throws EpServiceException {
		return fallbackCategoryService.saveOrUpdate(category);
	}

	@Override
	public boolean hasSubCategories(final long categoryUid) {
		return fallbackCategoryService.hasSubCategories(categoryUid);
	}

	@Override
	public List<Long> findDescendantCategoryUids(final long categoryUid) {
		return findDescendantCategoriesByUidCache.get(categoryUid,
				cacheKey -> fallbackCategoryService.findDescendantCategoryUids(categoryUid));
	}

	@Override
	public List<Long> findDescendantCategoryUids(final List<Long> categoryUids) {
		return fallbackCategoryService.findDescendantCategoryUids(categoryUids);
	}

	@Override
	public List<Category> findDirectDescendantCategories(final String categoryGuid) {
		return fallbackCategoryService.findDirectDescendantCategories(categoryGuid);
	}

	@Override
	public boolean isProductInCategory(final long productUid, final long categoryUid) {
		return fallbackCategoryService.isProductInCategory(productUid, categoryUid);
	}

	@Override
	public boolean hasProduct(final long categoryUid) throws EpServiceException {
		return fallbackCategoryService.hasProduct(categoryUid);
	}

	@Override
	public void removeCategoryTree(final long categoryUid) throws EpServiceException {
		fallbackCategoryService.removeCategoryTree(categoryUid);
	}

	@Override
	public void updateOrder(final Category categoryOne, final long uidTwo) throws EpServiceException {
		fallbackCategoryService.updateOrder(categoryOne, uidTwo);
	}

	@Override
	public List<Long> findAvailableUids() {
		return fallbackCategoryService.findAvailableUids();
	}

	@Override
	public long findUidById(final String categoryId, final Catalog catalog) throws EpServiceException {
		return fallbackCategoryService.findUidById(categoryId, catalog);
	}

	@Override
	public boolean isCodeInUse(final String code) throws EpServiceException {
		return fallbackCategoryService.isCodeInUse(code);
	}

	@Override
	public boolean isGuidInUse(final String guid) {
		return fallbackCategoryService.isGuidInUse(guid);
	}

	@Override
	public List<Long> findAllUids() {
		return fallbackCategoryService.findAllUids();
	}

	@Override
	public List<Long> findUidsByModifiedDate(final Date date) {
		return fallbackCategoryService.findUidsByModifiedDate(date);
	}

	@Override
	public List<Long> findUidsByDeletedDate(final Date date) {
		return fallbackCategoryService.findUidsByDeletedDate(date);
	}

	@Override
	public Set<Long> findAncestorCategoryUidsByProductUid(final long productUid) {
		return fallbackCategoryService.findAncestorCategoryUidsByProductUid(productUid);
	}

	@Override
	public Set<Long> findAncestorCategoryUidsByCategoryUid(final long categoryUid) {
		return fallbackCategoryService.findAncestorCategoryUidsByCategoryUid(categoryUid);
	}

	@Override
	public Set<String> findAncestorCategoryCodesByCategoryUid(final long categoryUid) {
		return fallbackCategoryService.findAncestorCategoryCodesByCategoryUid(categoryUid);
	}

	@Override
	public List<Long> findCategoryUidsForCatalog(final long catalogUid) {
		return fallbackCategoryService.findCategoryUidsForCatalog(catalogUid);
	}

	@Override
	public List<Long> findCategoryUidsForCatalog(final String catalogCode) {
		return fallbackCategoryService.findCategoryUidsForCatalog(catalogCode);
	}

	@Override
	public List<Object[]> getFeaturedProductsList(final long categoryUid) {
		return fallbackCategoryService.getFeaturedProductsList(categoryUid);
	}

	@Override
	public List<Long> findFeaturedProductUidList(final long categoryUid) {
		return findFeaturedProductsUidCache.get(categoryUid, cacheKey -> fallbackCategoryService.findFeaturedProductUidList(categoryUid));
	}

	@Override
	public Category addLinkedCategory(final long masterCategoryUid, final long parentCategoryUid, final long catalogUid) {
		return fallbackCategoryService.addLinkedCategory(masterCategoryUid, parentCategoryUid, catalogUid);
	}

	@Override
	public Category addLinkedCategoryProducts(final Category linkedCategory) {
		return fallbackCategoryService.addLinkedCategoryProducts(linkedCategory);
	}

	@Override
	public Category removeCategoryProducts(final Category category) {
		return fallbackCategoryService.removeCategoryProducts(category);
	}

	@Override
	public List<Category> findLinkedCategories(final long masterCategoryUid) {
		return fallbackCategoryService.findLinkedCategories(masterCategoryUid);
	}

	@Override
	public void removeLinkedCategoryTree(final Category linkedCategory) throws EpServiceException {
		fallbackCategoryService.removeLinkedCategoryTree(linkedCategory);
	}

	@Override
	public void updateCategoryOrderUp(final Category category) {
		fallbackCategoryService.updateCategoryOrderUp(category);
	}

	@Override
	public void updateCategoryOrderDown(final Category category) {
		fallbackCategoryService.updateCategoryOrderDown(category);
	}

	@Override
	public int getRootCategoryCount(final long catalogUid) {
		return fallbackCategoryService.getRootCategoryCount(catalogUid);
	}

	@Override
	public int getSubCategoryCount(final long categoryUid) {
		return fallbackCategoryService.getSubCategoryCount(categoryUid);
	}

	@Override
	public Long findUidByCompoundGuid(final String compoundCategoryGuid) {
		return fallbackCategoryService.findUidByCompoundGuid(compoundCategoryGuid);
	}

	@Override
	public Catalog getMasterCatalog(final Category category) {
		return fallbackCategoryService.getMasterCatalog(category);
	}

	@Override
	public boolean categoryExistsWithCompoundGuid(final String compoundGuid) {
		return fallbackCategoryService.categoryExistsWithCompoundGuid(compoundGuid);
	}

	@Override
	public Set<Long> findAncestorCategoryUidsWithTreeOrder(final Set<Long> categoryUidSet) {
		return fallbackCategoryService.findAncestorCategoryUidsWithTreeOrder(categoryUidSet);
	}

	@Override
	public int findMaxRootOrdering(final long catalogUid) {
		return fallbackCategoryService.findMaxRootOrdering(catalogUid);
	}

	@Override
	public int findMinRootOrdering(final long catalogUid) {
		return fallbackCategoryService.findMinRootOrdering(catalogUid);
	}

	@Override
	public int findMaxChildOrdering(final Category category) {
		return fallbackCategoryService.findMaxChildOrdering(category);
	}

	@Override
	public int findMinChildOrdering(final Category category) {
		return fallbackCategoryService.findMinChildOrdering(category);
	}

	@Override
	public List<Category> getPath(final Category category) {
		return fallbackCategoryService.getPath(category);
	}

	@Override
	public List<Category> findCategoriesByCatalogUid(final long catalogUid) {
		return fallbackCategoryService.findCategoriesByCatalogUid(catalogUid);
	}

	@Override
	public boolean canSyndicate(final Category category) {
		return fallbackCategoryService.canSyndicate(category);
	}

	public void setFindCodeByUidCache(final Cache<Long, String> findCodeByUidCache) {
		this.findCodeByUidCache = findCodeByUidCache;
	}

	public void setFallbackCategoryService(final CategoryService fallbackCategoryService) {
		this.fallbackCategoryService = fallbackCategoryService;
	}

	public void setFindDescendantCategoriesByUidCache(final Cache<Long, List<Long>> findDescendantCategoriesByUidCache) {
		this.findDescendantCategoriesByUidCache = findDescendantCategoriesByUidCache;
	}

	public void setFindFeaturedProductsUidCache(final Cache<Long, List<Long>> findFeaturedProductsUidCache) {
		this.findFeaturedProductsUidCache = findFeaturedProductsUidCache;
	}
}
