/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.service.catalog.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.exception.DuplicateKeyException;
import com.elasticpath.commons.exception.EpCategoryNotEmptyException;
import com.elasticpath.commons.exception.IllegalOperationException;
import com.elasticpath.commons.util.CategoryGuidUtil;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.CategoryDeleted;
import com.elasticpath.domain.catalog.CategoryLoadTuner;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.persistence.api.FetchGroupLoadTuner;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.persistence.dao.ProductDao;
import com.elasticpath.persistence.support.FetchGroupConstants;
import com.elasticpath.service.catalog.CatalogService;
import com.elasticpath.service.catalog.CategoryLookup;
import com.elasticpath.service.catalog.CategoryService;
import com.elasticpath.service.catalog.ProductService;
import com.elasticpath.service.misc.FetchPlanHelper;
import com.elasticpath.service.search.IndexNotificationService;
import com.elasticpath.service.search.IndexType;

/**
 * The default implementation of <code>CategoryService</code>.
 */
@SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ExcessiveClassLength", "PMD.TooManyMethods", "PMD.GodClass" })
public class CategoryServiceImpl implements CategoryService {

	private static final Logger LOG = Logger.getLogger(CategoryServiceImpl.class);

	private static final String PLACE_HOLDER_FOR_LIST = "list";

	private CategoryLoadTuner categoryLoadTunerAll;
	private CategoryLoadTuner categoryLoadTunerMinimal;
	private CategoryLoadTuner categoryLoadTunerDefault;
	private FetchPlanHelper fetchPlanHelper;
	private ProductService productService;
	private CatalogService catalogService;
	private FetchGroupLoadTuner defaultFetchGroupLoadTuner;
	private ProductDao productDao;
	private PersistenceEngine persistenceEngine;
	private BeanFactory beanFactory;

	/** Load tuner used in operations related to linking products to categories or linked categories. */
	private FetchGroupLoadTuner linkProductCategoryLoadTuner;

	private static final String DUPLICATE_GUID = "Inconsistent data -- duplicate guid:";
	private CategoryLookup categoryLookup;
	private CategoryGuidUtil categoryGuidUtil;

	/**
	 * Adds the given category.
	 * This implementation must first check whether a category with the given code already exists, since
	 * master categories and linked categories can share the same code but are in fact different (there
	 * can be multiple linked categories with the same code as long as they're in different catalogs, and
	 * linked categories share the same code as their master categories).
	 *
	 * Calls {@link #masterCategoryExists(String)} and {@link #linkedCategoryExists(String, String)}.
	 *
	 * @param category the category to add
	 * @return the persisted instance of category
	 * @throws EpServiceException - in case of any errors
	 * @throws DuplicateKeyException if a category with the given code already exists
	 */
	@Override
	public Category add(final Category category) throws EpServiceException {
		if ((category.isLinked() && linkedCategoryExists(category.getCode(), category.getCatalog().getCode()))
			|| (!category.isLinked() && masterCategoryExists(category.getCode()))) {
			throw new DuplicateKeyException(
					"Category code '" + category.getCode() + "' already exists in catalog " + category.getCatalog().getCode());
		}
		getPersistenceEngine().save(category);
		getProductService().notifyCategoryUpdated(category);
		getIndexNotificationService().addNotificationForEntityIndexUpdate(IndexType.CATEGORY, category.getUidPk());
		return category;
	}

	/**
	 * Checks whether a master category with the given category code exists in the database.
	 * @param categoryCode the code to search for.
	 * @return true if a category with the given code exists, false if not
	 */
	protected boolean masterCategoryExists(final String categoryCode) {
		return getPersistenceEngine().<Long>retrieveByNamedQuery("CATEGORY_COUNT_BY_CODE", categoryCode).get(0) >= 1;
	}

	/**
	 * Checks whether a linked category with the given code already exists in the catalog
	 * represented by the given catalog code.
	 * @param categoryCode the linked category code, or the code for the master category to which the
	 * linked category belongs, since linked categories and their master categories use the same code.
	 * @param catalogCode the code for the catalog in which to check for the existence of the linked category code
	 * @return true if it exists, false if not
	 */
	protected boolean linkedCategoryExists(final String categoryCode, final String catalogCode) {
		return getPersistenceEngine().<Long>retrieveByNamedQuery("LINKED_CATEGORY_COUNT_BY_CODE", categoryCode, catalogCode).get(0) >= 1;
	}

	/**
	 * Get the top-level catalog for the given category. If the category is
	 * a linked category then its master category's catalog will be obtained.
	 * @param category the category for which to retrieve the master catalog
	 * @return the requested catalog, or null if one cannot be found
	 * @throws com.elasticpath.persistence.api.EpPersistenceException in case of error
	 */
	@Override
	public Catalog getMasterCatalog(final Category category) {
		List<Catalog> catalogList;
		if (category.isLinked()) {
			catalogList = getPersistenceEngine().<Catalog>retrieveByNamedQuery(
					"FIND_MASTER_CATALOG_FOR_LINKED_CATEGORY", category.getUidPk());
		} else {
			catalogList = getPersistenceEngine().<Catalog>retrieveByNamedQuery(
					"FIND_CATALOG_FOR_CATEGORY", category.getUidPk());
		}
		if (!catalogList.isEmpty()) {
			return catalogList.get(0);
		}
		return null;
	}

	/**
	 * Updates the given category.
	 *
	 * @param category the category to update
	 * @return the updated category instance
	 * @throws EpServiceException - in case of any errors
	 */
	@Override
	public Category update(final Category category) throws EpServiceException {
		final Category updatedCategory = getPersistenceEngine().update(category);
		getProductService().notifyCategoryUpdated(updatedCategory);
		getIndexNotificationService().addNotificationForEntityIndexUpdate(IndexType.CATEGORY, updatedCategory.getUidPk());
		return updatedCategory;
	}

	/**
	 * Extension point to override the parent class for categories, the return
	 * value of this is passed to persistenceEngine methods load() and get().
	 * To override the category implementations this should prove useful
	 * @return the class that is the parent of linked and master categories.
	 */
	protected Class<? extends Category> getAbstractCategoryImplClass() {
		return getBeanFactory().getBeanImplClass(ContextIdNames.ABSTRACT_CATEGORY);
	}

	/**
	 * Retrieve a list of root categories.
	 *
	 * @param catalog the catalog to get the root categories for
	 * @param availableOnly set it to <code>true</code> to only list available root categories
	 * @return return all root categories
	 */
	@Override
	public List<Category> listRootCategories(final Catalog catalog, final boolean availableOnly) {
		if (catalog == null) {
			return Collections.emptyList();
		}

		List<Category> readOnlyResultCategory;
		List<Category> readOnlyResultLinkedCategory = Collections.emptyList();

		FetchGroupLoadTuner loadTuner = getDefaultFetchGroupLoadTuner();
		fetchPlanHelper.configureFetchGroupLoadTuner(loadTuner);
		if (availableOnly) {
			final Date now = new Date();
			readOnlyResultCategory = getPersistenceEngine().retrieveByNamedQuery("CATEGORY_LIST_AVAILABLE_ROOT", now, now, catalog.getUidPk());
			readOnlyResultLinkedCategory = getPersistenceEngine().retrieveByNamedQuery("LINKED_CATEGORY_LIST_AVAILABLE_ROOT",
					now,
					now,
					catalog.getUidPk());

		} else {
			readOnlyResultCategory = getPersistenceEngine().retrieveByNamedQuery("CATEGORY_LIST_ROOT", catalog.getUidPk());
		}
		fetchPlanHelper.clearFetchPlan();

		final List<Category> result = new ArrayList<>(readOnlyResultCategory.size() + readOnlyResultLinkedCategory.size());
		result.addAll(readOnlyResultCategory);
		result.addAll(readOnlyResultLinkedCategory);
		Collections.sort(result);
		return result;
	}

	/**
	 * Retrieve the {@link Category} with the given GUID.
	 *
	 * @param categoryCode the category code
	 * @return the category with the given GUID
	 * @throws EpServiceException in case of any error
	 */
	@Override
	public Category findByCode(final String categoryCode) {
		List<Category> categories;
		fetchPlanHelper.configureCategoryFetchPlan(categoryLoadTunerMinimal);
		categories = getPersistenceEngine().retrieveByNamedQuery("CATEGORY_SELECT_BY_CODE", categoryCode);

		fetchPlanHelper.clearFetchPlan();

		if (categories.isEmpty()) {
			return null;
		}

		if (categories.size() > 1) {
			throw new EpServiceException(DUPLICATE_GUID + categoryCode);
		}
		return categories.get(0);
	}




	/**
	 * Retrieve the {@link Category} with the given GUID in a particular catalog. The returned
	 * Category could be either linked or non-linked depending on the catalog. Give a load tuner
	 * to tune the result or {@code null} to tune to the default.
	 *
	 * @param guid the GUID of the category
	 * @param catalog the catalog to search in
	 * @param loadTuner the load tuner to use
	 * @return the category with the given GUID
	 * @throws EpServiceException in case of any error
	 */
	protected Category findByGuid(final String guid, final Catalog catalog, final FetchGroupLoadTuner loadTuner) {
		if (catalog == null) {
			LOG.error("Attempt to find category with null catalog for category guid: " + guid);
			return null;
		}

		if (loadTuner == null) {
			fetchPlanHelper.configureCategoryFetchPlan(categoryLoadTunerDefault);
		} else {
			fetchPlanHelper.configureFetchGroupLoadTuner(loadTuner);
		}

		List<Category> categories = getPersistenceEngine().retrieveByNamedQuery("CATEGORY_FIND_BY_CODE_CATALOG", guid, catalog.getUidPk());
		if (categories == null || categories.isEmpty()) {
			categories = getPersistenceEngine().retrieveByNamedQuery("LINKED_CATEGORY_FIND_BY_CODE_CATALOG", guid, catalog.getUidPk());
		}

		fetchPlanHelper.clearFetchPlan();

		if (categories.isEmpty()) {
			return null;
		}

		if (categories.size() > 1) {
			throw new EpServiceException(DUPLICATE_GUID + guid);
		}
		return categories.get(0);
	}

	/**
	 * Save or update the given category.
	 *
	 * @param category the category to save or update
	 * @return the updated category
	 * @throws EpServiceException - in case of any errors
	 */
	@Override
	public Category saveOrUpdate(final Category category) throws EpServiceException {
		final Category updatedCategory = getPersistenceEngine().saveOrMerge(category);

		getProductService().notifyCategoryUpdated(updatedCategory);
		getIndexNotificationService().addNotificationForEntityIndexUpdate(IndexType.CATEGORY, updatedCategory.getUidPk());
		return updatedCategory;
	}

	/**
	 * Query if the category has sub categories. For performance.
	 *
	 * @param categoryUid the category's uid
	 * @return true if the category has subcategories
	 */
	@Override
	public boolean hasSubCategories(final long categoryUid) {
		return getSubCategoryCount(categoryUid) > 0;
	}

	/**
	 * Retrieve all descendant category UIDs of the given category UID.
	 *
	 * @param categoryUid the category UID.
	 * @return the list of UID of the direct and indirect sub-categories of the given start
	 *         category.
	 */
	@Override
	public List<Long> findDescendantCategoryUids(final long categoryUid) {
		List<Long> result = new ArrayList<>();
		if (categoryUid > 0) {
			final List<Long> queryResponse = getPersistenceEngine().retrieveByNamedQuery("CATEGORY_LIST_SUBCATEGORY_UIDS", Long.valueOf(categoryUid));
			result.addAll(queryResponse);
		} else {
			result = Collections.emptyList();
		}

		if (result != null && !result.isEmpty()) {
			result.addAll(findDescendantCategoryUids(result));
		}
		return result;
	}

	@Override
	public List<Category> findDirectDescendantCategories(final String categoryGuid) {
		if (categoryGuid == null) {
			return Collections.emptyList();
		}

		final List<Category> queryResponse = getPersistenceEngine().retrieveByNamedQuery("CATEGORY_LIST_SUBCATEGORY", categoryGuid);
		List<Category> result = new ArrayList<>(queryResponse);
		Collections.sort(result);

		return result;
	}

	/**
	 * Retrieve all descendant category UIDs of the given category UIDs.
	 *
	 * @param categoryUids the category UIDs.
	 * @return the list of UIDs of the direct and indirect sub-categories of the given start
	 *         category UIDs.
	 */
	@Override
	public List<Long> findDescendantCategoryUids(final List<Long> categoryUids) {
		final List<Long> result = new ArrayList<>();
		List<Long> subCategoryUids = getPersistenceEngine().retrieveByNamedQueryWithList(
				"CATEGORY_UID_SELECT_BY_PARENT_UIDS", PLACE_HOLDER_FOR_LIST, categoryUids);
		while (!subCategoryUids.isEmpty()) {
			result.addAll(subCategoryUids);
			subCategoryUids = getPersistenceEngine().retrieveByNamedQueryWithList("CATEGORY_UID_SELECT_BY_PARENT_UIDS", PLACE_HOLDER_FOR_LIST,
					subCategoryUids);
		}

		return result;
	}

	/**
	 * Return <code>true</code> if the product with the given product UID is in the category
	 * with the given category UID. Otherwise, <code>false</code>.
	 *
	 * @param productUid the product UID
	 * @param categoryUid the category UID
	 * @return <code>true</code> if the product with the given product UID is in the category
	 *         with the given category UID. Otherwise, <code>false</code>
	 */
	@Override
	public boolean isProductInCategory(final long productUid, final long categoryUid) {
		final List<?> result = getPersistenceEngine().retrieveByNamedQuery("SELECT_PRODUCT_CATEGORY_ASSOCIATION",
				Long.valueOf(productUid), Long.valueOf(categoryUid));
		return !result.isEmpty();
	}

	@Override
	public boolean hasProduct(final long categoryUid) throws EpServiceException {
		return getProductService().hasProductsInCategory(categoryUid);
	}

	/**
	 * Deletes the product category.
	 *
	 * @param categoryUid the category to be removed
	 * @throws EpServiceException - in case of any errors
	 */
	private void removeProductCategory(final long categoryUid) throws EpServiceException {
		getPersistenceEngine().executeNamedQuery("PRODUCTCATEGORY_DELETE_BY_CATEGORY_UID", Long.valueOf(categoryUid));
	}

	/**
	 * Deletes the category and subcategories.
	 * Fails with exception if any category is not empty.
	 *
	 * @param category the category to remove
	 * @throws EpServiceException - in case of any errors
	 */
	private void removeCategory(final Category category) throws EpServiceException {
		if (hasProduct(category.getUidPk())) {
			throw new EpCategoryNotEmptyException("Category must be empty.");
		}
		// Delete the sub-categories
		for (final Category subCategory : getCategoryLookup().findChildren(category)) {
			removeCategory(subCategory);
		}

		getPersistenceEngine().delete(category);
		addCategoryDeleted(category.getUidPk());
	}

	private void addCategoryDeleted(final long uid) {
		final CategoryDeleted categoryDeleted = getBean(ContextIdNames.CATEGORY_DELETED);
		categoryDeleted.setCategoryUid(uid);
		categoryDeleted.setDeletedDate(new Date());
		getPersistenceEngine().save(categoryDeleted);
	}

	@Override
	public void removeCategoryTree(final long categoryUid) throws EpServiceException {
		final Category curCategory = getCategoryLookup().findByUid(categoryUid);

		getPersistenceEngine().evictObjectFromCache(curCategory);

		// First, remove any categories that are linked to this one
		final List<Category> linkedCategories = findLinkedCategories(categoryUid);
		for (final Category linkedCategory : linkedCategories) {
			removeLinkedCategoryTree(linkedCategory);
		}

		// Second, remove the category
		removeCategory(curCategory);
	}

	/**
	 * Re orders (swaps the ordering field) of the two parameter categories. If ordering hasn't been set before, then will go thru the whole parent
	 * category and order all the child categories first.
	 *
	 * @param uidOne UID of a category to reorder
	 * @param uidTwo UID of a category to reorder
	 * @throws EpServiceException in case of any errors
	 */
	@Override
	public void updateOrder(final long uidOne, final long uidTwo) throws EpServiceException {
		// don't need to populate attributes
		fetchPlanHelper.configureCategoryFetchPlan(categoryLoadTunerMinimal);
		Category one = getPersistenceEngine().load(getAbstractCategoryImplClass(), uidOne);
		Category two = getPersistenceEngine().load(getAbstractCategoryImplClass(), uidTwo);
		fetchPlanHelper.clearFetchPlan();
		if (one.getCatalog().getUidPk() != two.getCatalog().getUidPk()) {
			throw new EpServiceException("Cannot update the order of categories in different catalogs.");
		}

		if (one.getOrdering() == two.getOrdering()) {
			final Category parent = getCategoryLookup().findParent(one);
			List<Category> children;
			if (parent == null) { // root category
				children = listRootCategories(one.getCatalog(), false);
			} else {
				children = getCategoryLookup().findChildren(parent);
			}
			Collections.sort(children);

			int ordering = 0; // set all orderings in the whole category
			for (Category category : children) {
				category.setOrdering(ordering++);

				final Category result = saveOrUpdate(category);

				// refresh
				if (result.getUidPk() == uidOne) {
					one = result;
				}
				if (result.getUidPk() == uidTwo) {
					two = result;
				}
			}
		}

		final int tempOrdering = one.getOrdering(); // swap orderings
		one.setOrdering(two.getOrdering());
		two.setOrdering(tempOrdering);
		saveOrUpdate(one);
		saveOrUpdate(two);
	}

	/**
	 * Sets the <code>CategoryLoadTuner</code> for populating all data.
	 *
	 * @param categoryLoadTunerAll the <code>CategoryLoadTuner</code> for populating all data.
	 */
	public void setCategoryLoadTunerAll(final CategoryLoadTuner categoryLoadTunerAll) {
		this.categoryLoadTunerAll = categoryLoadTunerAll;
	}

	/**
	 * Sets the <code>CategoryLoadTuner</code> for populating minimal data.
	 *
	 * @param categoryLoadTunerMinimal the <code>CategoryLoadTuner</code> for populating minimal data.
	 */
	public void setCategoryLoadTunerMinimal(final CategoryLoadTuner categoryLoadTunerMinimal) {
		this.categoryLoadTunerMinimal = categoryLoadTunerMinimal;
	}

	/**
	 * Sets the default <code>CategoryLoadTuner</code>.
	 *
	 * @param categoryLoadTunerDefault the default <code>CategoryLoadTuner</code>
	 */
	public void setCategoryLoadTunerDefault(final CategoryLoadTuner categoryLoadTunerDefault) {
		this.categoryLoadTunerDefault = categoryLoadTunerDefault;
	}

	/**
	 * Returns the <code>CategoryLoadTuner</code> for populating all data.
	 *
	 * @return the <code>CategoryLoadTuner</code> for populating all data
	 */
	public CategoryLoadTuner getCategoryLoadTunerAll() {
		return categoryLoadTunerAll;
	}

	/**
	 * Returns the default <code>CategoryLoadTuner</code>.
	 *
	 * @return the default <code>CategoryLoadTuner</code>
	 */
	public CategoryLoadTuner getCategoryLoadTunerDefault() {
		return categoryLoadTunerDefault;
	}

	/**
	 * Returns the <code>CategoryLoadTuner</code> for populating minimal data.
	 *
	 * @return the <code>CategoryLoadTuner</code> for populating minimal data
	 */
	public CategoryLoadTuner getCategoryLoadTunerMinimal() {
		return categoryLoadTunerMinimal;
	}

	/**
	 * Returns all available category UIDs as a list.
	 *
	 * @return all available category UIDs as a list
	 */
	@Override
	public List<Long> findAvailableUids() {
		final Date now = new Date();

		// Query both the category and linked category tables
		final List<Long> availableCategoryUids = getPersistenceEngine().retrieveByNamedQuery("CATEGORY_UIDS_AVAILABLE", now, now);
		final List<Long> availableLinkedCategoryUids = getPersistenceEngine().retrieveByNamedQuery("LINKED_CATEGORY_UIDS_AVAILABLE", now, now);

		final List<Long> result = new ArrayList<>(availableCategoryUids.size() + availableLinkedCategoryUids.size());
		result.addAll(availableCategoryUids);
		result.addAll(availableLinkedCategoryUids);

		return result;
	}

	/**
	 * Get the category UID of the given category guid.
	 * It is first assumed that the given category identifier is a GUID, and the method will
	 * attempt to load the Category given that assumption. If that fails, the identifier
	 * is assumed to be a UID and is returned as such.
	 *
	 * @param categoryId the category GUID or UID
	 * @param catalog the catalog to search in for the GUID
	 * @return the category UID, otherwise 0
	 * @throws EpServiceException in case of any errors
	 */
	@Override
	public long findUidById(final String categoryId, final Catalog catalog) throws EpServiceException {
		if (catalog == null) {
			throw new EpServiceException("catalog cannot be null");
		}

		final List<Long> results = getPersistenceEngine().retrieveByNamedQuery(
				"CATEGORY_UID_SELECT_BY_CODE_AND_CATALOG_UID", categoryId, catalog.getUidPk());

		if (results.isEmpty()) {
			try {
				return Long.parseLong(categoryId);
			} catch (final NumberFormatException e) {
				return 0L;
			}
		} else if (results.size() == 1) {
			return results.get(0);
		} else {
			throw new EpServiceException(DUPLICATE_GUID + categoryId);
		}
	}

	/**
	 * Gets the category code associated with a given category uidPk.
	 *
	 * @param uidPk - The unique ID of the category to get the code for.
	 * @return The category code of the category if it exists,
	 *         empty string otherwise.
	 */
	@Override
	public String findCodeByUid(final long uidPk) {
		List<String> result = getPersistenceEngine().retrieveByNamedQuery("CATEGORY_CODE_SELECT_BY_UID", uidPk);
		if (result.isEmpty()) {
			// the category could be linked, which shares a code with it's master - get the code from the master category
			result = getPersistenceEngine().retrieveByNamedQuery("LINKED_CATEGORY_CODE_SELECT_BY_UID", uidPk);
		}

		// Default the code to an empty string if not one and only one code is found
		String code = "";
		if (result.size() == 1) {
			code = result.get(0);
		}

		return code;
	}

	/**
	 * Gets the category uidPk associated with a given compound category guid.
	 *
	 * @param compoundCategoryGuid - The unique compound category guid to get the uidPk for.
	 * @return The category uidPk of the category if it exists, null otherwise
	 */
	@Override
	public Long findUidByCompoundGuid(final String compoundCategoryGuid) {
		final String categoryGuid = getCategoryGuidUtil().parseCategoryGuid(compoundCategoryGuid);
		final String catalogGuid = getCategoryGuidUtil().parseCatalogGuid(compoundCategoryGuid);
		List<Long> categoryUids = getPersistenceEngine().retrieveByNamedQuery("CATEGORY_UID_SELECT_BY_CATEGORY_AND_CATALOG_CODE",
				categoryGuid, catalogGuid);
		if (categoryUids == null || categoryUids.isEmpty()) {
			categoryUids = getPersistenceEngine().retrieveByNamedQuery(
					"LINKED_CATEGORY_UID_SELECT_BY_CATEGORY_AND_CATALOG_CODE", categoryGuid, catalogGuid);
		}

		if (categoryUids.isEmpty()) {
			return null;
		}

		return categoryUids.get(0);
	}

	/**
	 * Checks whether the given category GUID exists or not, for category, i.e. category code. A
	 * GUID exists if it is already in use in the system for that type of object.
	 *
	 * @param code the category code
	 * @return whether the given GUID(code) exists
	 * @throws EpServiceException in case of any errors
	 */
	@Override
	public boolean isCodeInUse(final String code) throws EpServiceException {
		if (code == null) {
			return false;
		}

		return masterCategoryExists(code);
	}

	@Override
	public boolean isGuidInUse(final String guid) {
		if (guid == null) {
			return false;
		}

		return getPersistenceEngine().<Long>retrieveByNamedQuery("CATEGORY_GUID_COUNT", guid).get(0) > 0;
	}

	/**
	 * Returns all category UIDs as a list.
	 *
	 * @return all category UIDs as a list
	 */
	@Override
	public List<Long> findAllUids() {
		return getPersistenceEngine().retrieveByNamedQuery("CATEGORY_UIDS_ALL");
	}

	/**
	 * Retrieves list of <code>Category</code> UIDs where the last modified date is later than
	 * the specified date.
	 *
	 * @param date date to compare with the last modified date
	 * @return list of <code>Category</code> whose last modified date is later than the specified date
	 */
	@Override
	public List<Long> findUidsByModifiedDate(final Date date) {
		return getPersistenceEngine().retrieveByNamedQuery("CATEGORY_UIDS_SELECT_BY_MODIFIED_DATE", date);
	}

	/**
	 * Retrieves list of category UIDs where the deleted date is later than the specified date.
	 *
	 * @param date date to compare with the deleted date
	 * @return list of category UIDs whose deleted date is later than the specified date
	 */
	@Override
	public List<Long> findUidsByDeletedDate(final Date date) {
		return getPersistenceEngine().retrieveByNamedQuery("CATEGORY_UIDS_SELECT_BY_DELETED_DATE", date);
	}

	/**
	 * Returns the set of category UIDs, which are ancestors of the given product UID. The category UID of the category where the product lives is
	 * not apart of the returned set.
	 *
	 * @param productUid the product UID
	 * @return a set of category UIDs
	 */
	@Override
	public Set<Long> findAncestorCategoryUidsByProductUid(final long productUid) {
		final List<Long> categoryUidList = getPersistenceEngine().retrieveByNamedQuery("CATEGORY_UID_SELECT_BY_PRODUCT_UID",
			Long.valueOf(productUid));
		if (categoryUidList.isEmpty()) {
			return Collections.emptySet();
		}

		// remove duplicate UIDs
		return new HashSet<>(findAncestorCategoryUids(categoryUidList));
	}

	/**
	 * Returns the set of category uids which are ancestors of the given categoryUid.
	 * The given categoryUid is not a part of the returned set.
	 *
	 * @param categoryUid - The uid of the category to search for ancestors' uids on.
	 * @return A set of ancestor category UIDs.
	 */
	@Override
	public Set<Long> findAncestorCategoryUidsByCategoryUid(final long categoryUid) {
		// remove duplicate UIDs
		return new HashSet<>(findAncestorCategoryUids(categoryUid));
	}

	/**
	 * Returns the set of category codes which are ancestors of the given categoryUid.
	 * The category code corresponding to the given categoryUid is not in the returned set.
	 *
	 * @param categoryUid - The uid of the category to search for ancestors' codes on.
	 * @return A set of ancestor category codes.
	 */
	@Override
	public Set<String> findAncestorCategoryCodesByCategoryUid(final long categoryUid) {
		// Get the list of ancestor uids
		final List<Long> parentUids =  findAncestorCategoryUids(categoryUid);

		// Get the category codes associated with the ancestor uids
		final List<String> parentCodes = getPersistenceEngine()
			.retrieveByNamedQueryWithList("CATEGORY_CODES_SELECT_BY_UIDS", PLACE_HOLDER_FOR_LIST, parentUids);

		return new HashSet<>(parentCodes);
	}

	/**
	 * Returns the list of category uids which are ancestors of the given categoryUid.
	 * The given categoryUid is not a part of the returned list. The categoryUid is
	 * turned into a list for further processing.
	 *
	 * @param categoryUid - The uid of the category to search for ancestors' codes on.
	 * @return A list of ancestor category UIDs.
	 */
	private List<Long> findAncestorCategoryUids(final long categoryUid) {
		// Turn the categoryUid into a list to be passed to the query
		final List<Long> categoryUids = Arrays.asList(categoryUid);

		return findAncestorCategoryUids(categoryUids);
	}

	/**
	 * Returns the list of category uids which are ancestors of the given categoryUids.
	 * The given categoryUids are not a part of the returned list.
	 *
	 * @param categoryUids - The uids of the categories to search for ancestors' codes on.
	 * @return A list of ancestor category UIDs.
	 */
	private List<Long> findAncestorCategoryUids(final List<Long> categoryUids) {
		final List<Long> result = new ArrayList<>();

		// Get the list of immediate parent uids
		List<Long> parentUids = getPersistenceEngine()
			.retrieveByNamedQueryWithList("CATEGORY_UID_SELECT_BY_CHILDREN_UIDS", PLACE_HOLDER_FOR_LIST, categoryUids);

		// For each level of parents, get their parents' uids and add them to the list until no parents are left
		while (!parentUids.isEmpty()) {
			result.addAll(parentUids);
			parentUids = getPersistenceEngine()
				.retrieveByNamedQueryWithList("CATEGORY_UID_SELECT_BY_CHILDREN_UIDS", PLACE_HOLDER_FOR_LIST, parentUids);
		}

		return result;
	}

	/**
	 * Returns a list of <code>Category</code> UIDs based on the given Catalog UIDPK.
	 *
	 * @param catalogUid identifier of the catalog
	 * @return a list of <code>Catalog</code>s UIDs
	 */
	@Override
	public List<Long> findCategoryUidsForCatalog(final long catalogUid) {
		return getPersistenceEngine().retrieveByNamedQuery("CATEGORY_UIDS_FOR_CATALOG", catalogUid);
	}

	/**
	 * Returns a list of <code>Category</code> UIDs based on the given Catalog Code.
	 *
	 * @param catalogCode is code of the catalog
	 * @return a list of <code>Catalog</code>s UIDs
	 */
	@Override
	public List<Long> findCategoryUidsForCatalog(final String catalogCode) {
		return getPersistenceEngine().retrieveByNamedQuery("CATEGORY_UIDS_FOR_CATALOG_BY_CODE", catalogCode);
	}

	/**
	 * Get a list of featured product by the category UID.
	 *
	 * @param categoryUid the category uidPk.
	 * @return a list of Object arrays where the element at index <code>0</code> is a
	 *         <code>Product</code> and the element at index <code>1</code> is an
	 *         <code>Integer</code> feature order value.
	 */
	@Override
	public List<Object[]> getFeaturedProductsList(final long categoryUid) {
		return getPersistenceEngine().retrieveByNamedQuery("FEATURED_PRODUCT_SELECT_BY_CAT_UID", Long.valueOf(categoryUid));
	}

	/**
	 * Get a list of featured product UID by the category UID.
	 *
	 * @param categoryUid the category UID.
	 * @return a list of product UID.
	 */
	@Override
	public List<Long> findFeaturedProductUidList(final long categoryUid) {
		return getPersistenceEngine().retrieveByNamedQuery("FEATURED_PRODUCT_UID_SELECT_BY_CAT_UID", Long.valueOf(categoryUid));
	}

	/**
	 * Set the <code>ProductService</code>.
	 *
	 * @param productService the <code>ProductService</code>
	 */
	public void setProductService(final ProductService productService) {
		this.productService = productService;
	}

	/**
	 * Get a reference to the product service. Note that the product service may not be set through Spring due to a circular reference.
	 *
	 * @return the <code>ProductService</code> instance
	 */
	private ProductService getProductService() {
		if (productService == null) {
			productService = getBean(ContextIdNames.PRODUCT_SERVICE);
		}
		return productService;
	}

	/**
	 * Sets the fetch plan helper.
	 *
	 * @param fetchPlanHelper the fetch plan helper
	 */
	public void setFetchPlanHelper(final FetchPlanHelper fetchPlanHelper) {
		this.fetchPlanHelper = fetchPlanHelper;
	}

	/**
	 * Creates a new linked category (in the given <code>catalog</code>) to the
	 * given <code>masterCategory</code>and additional linked categories for
	 * all of the <code>masterCategory</code>'s sub-categories. The top-level
	 * linked category is set to the given <code>parentCategory</code>.
	 * This implementation calls {@link #addLinkedCategory(Category, Category, Catalog, int)}.
	 *
	 * @param masterCategory the category to which the top-level linked category is linked.
	 * @param parentCategory the category to be the parent of the new top-level linked category
	 * @param catalog the catalog in which the new linked categories should be created
	 * @return the new top-level linked category
	 */
	protected Category addLinkedCategory(final Category masterCategory, final Category parentCategory, final Catalog catalog) {
		return addLinkedCategory(masterCategory, parentCategory, catalog, 0);
	}

	/**
	 * Creates a {@link Category} that is linked to the given {@code masterCategory}, with its
	 * parent set to the given {@code parentCategory}, and its catalog set to the given
	 * {@link Catalog}. This method creates a linked category to the given {@code masterCategory},
	 * derives all of its products, and then recursively does the same for any of the
	 * {@code masterCategory}'s sub-categories.
	 * This implementation calls {@link #updateProductsWithNewLinkedCategory(Category)}
	 * and {@link #saveOrMerge(Category)}.
	 *
	 * @param masterCategory the {@link Category} to link to
	 * @param parentCategory the {@link Category} to set the parent to; {@code null} if linked
	 *            category is a root category
	 * @param catalog the {@link Catalog} that contains the linked category
	 * @param depth the current depth of the tree of linked categories
	 * @return the newly created linked {@link Category}
	 * @throws DuplicateKeyException if a linked category for the given master category already exists in the given catalog
	 * @throws IllegalOperationException if an attempt is made to add a linked category to a master catalog.
	 */
	protected Category addLinkedCategory(final Category masterCategory, final Category parentCategory, final Catalog catalog, final int depth) {
		if (catalog.isMaster()) {
			throw new IllegalOperationException("A linked category cannot be created in a master catalog.");
		}
		if (linkedCategoryExists(masterCategory.getCode(), catalog.getCode())) {
			throw new DuplicateKeyException("A category linked to " + masterCategory.getCode() + " already exists in catalog " + catalog.getCode());
		}

		final Category newLinkedCategory = createLinkedCategory(masterCategory, parentCategory, catalog);
		final Category updatedCategory = saveOrMerge(newLinkedCategory);

		for (final Category currCategory : getCategoryLookup().findChildren(masterCategory)) {
			addLinkedCategory(currCategory, newLinkedCategory, catalog, depth + 1);
		}

		if (depth == 0) { //We're at the top of the tree of new linked categories
			updateCategoryProductsRecursively(updatedCategory);
			return updatedCategory;
		}

		// Products start to get indexed as soon as they are saved resulting in invalid categories
		// for products, need to re-index those products now
		return newLinkedCategory;
	}

	/**
	 * Traverses recursively through all the categories and calls {@link #updateProductsWithNewLinkedCategory(Category)}.
	 *
	 * @param category the category which products have to be updated
	 */
	private void updateCategoryProductsRecursively(final Category category) {
		updateProductsWithNewLinkedCategory(category);
		//loop through all the new linked categories, and for each one loop through its products, adding the category to each product
		for (final Category subCategory : getCategoryLookup().findChildren(category)) {
			updateCategoryProductsRecursively(subCategory);
		}
	}

	/**
	 * Calls the persistence engine to save or merge the given category.
	 * This implementation does not call the product service to update
	 * the category's products.
	 * @param category the category to save
	 * @return the saved category
	 */
	protected Category saveOrMerge(final Category category) {
		final Category updatedCategory = getPersistenceEngine().saveOrMerge(category);
		getIndexNotificationService().addNotificationForEntityIndexUpdate(IndexType.CATEGORY, updatedCategory.getUidPk());
		return updatedCategory;
	}

	/**
	 * Creates a new {@link com.elasticpath.domain.catalog.impl.LinkedCategoryImpl}. The ordering will be the same as the given Master Category,
	 * and the 'included' bit will be set to true.
	 * @param masterCategory the linked category's master category
	 * @param parentCategory the linked category's parent category
	 * @param catalog the catalog in which the linked category will be created
	 * @return the new linked category
	 */
	private Category createLinkedCategory(final Category masterCategory, final Category parentCategory, final Catalog catalog) {
		final Category newLinkedCategory = getBean(ContextIdNames.LINKED_CATEGORY);
		newLinkedCategory.setCatalog(catalog);
		newLinkedCategory.setMasterCategory(masterCategory);
		newLinkedCategory.setParent(parentCategory);
		newLinkedCategory.setIncluded(true);
		newLinkedCategory.setOrdering(masterCategory.getOrdering());
		return newLinkedCategory;
	}

	/**
	 * Adds the given linked category to every product in the given linked category's
	 * {@code masterCategory}.
	 *
	 * Makes the given linked category the default category in this catalog for all affected products.
	 *
	 * @param subCategory the linked subCategory that should be added to products
	 */
	protected void updateProductsWithNewLinkedCategory(final Category subCategory) {
		// JPA seems to trigger a field access when a category is included.
		fetchPlanHelper.configureFetchGroupLoadTuner(getLinkProductCategoryLoadTuner(), true);
		for (final Product currProduct
				: getProductService().findByCategoryUid(subCategory.getMasterCategory().getUidPk(), getLinkProductCategoryLoadTuner())) {
			currProduct.addCategory(subCategory);
			currProduct.setCategoryAsDefault(subCategory);
			getProductService().saveOrUpdate(currProduct);
		}
		getProductService().notifyCategoryUpdated(subCategory);
		fetchPlanHelper.clearFetchPlan();
	}

	/**
	 * Creates a {@link Category} that is linked to the given {@code masterCategory}, with its
	 * parent set to the given {@code parentCategory}, and its catalog set to the given
	 * {@link Catalog}. This method creates a linked category to the given {@code masterCategory},
	 * and then recursively does the same for any of the {@code masterCategory}'s sub-categories.
	 *
	 * For each product in each of the master categories, the new linked category of the same level
	 * is added to the product. Once a linked category has been added to all of the appropriate products,
	 * the product service is notified that the category has been updated.
	 *
	 * This implementation uses the given UIDs to load the master category, parent category, and catalog from the
	 * database and calls {@link #addLinkedCategory(Category, Category, Catalog)} to do the work.
	 *
	 * @param masterCategoryUid the uid of category to link to
	 * @param parentCategoryUid the uid of category to set the parent, set to -1 if linked
	 *            category is a root category
	 * @param catalogUid the catalog uid that contains the linked category
	 * @return the newly created linked {@link Category}
	 */
	@Override
	public Category addLinkedCategory(final long masterCategoryUid, final long parentCategoryUid, final long catalogUid) {
		final FetchGroupLoadTuner uidLoadTuner = getBean(ContextIdNames.FETCH_GROUP_LOAD_TUNER);
		uidLoadTuner.addFetchGroup(FetchGroupConstants.NONE);

		final Category masterCategory = getCategoryLookup().findByUid(masterCategoryUid);

		Category parentCategory = null;
		if (parentCategoryUid != -1) {
			parentCategory = getCategoryLookup().findByUid(parentCategoryUid);
		}

		final Catalog catalog = catalogService.load(catalogUid, uidLoadTuner, true);
		return addLinkedCategory(masterCategory, parentCategory, catalog);
	}

	/**
	 * Determines the number of root categories in the given <code>Catalog</code>.
	 *
	 * @param catalogUid the id of the <code>Catalog</code> to get the count for
	 * @return the number of root categories
	 */
	@Override
	public int getRootCategoryCount(final long catalogUid) {
		Long numRootCategoriesInCatalog = 0L;
		final List<Object> rootCountResults =
			getPersistenceEngine().retrieveByNamedQuery("COUNT_ROOT_CATEGORIES", catalogUid);
		if (rootCountResults.size() == 1) {
			numRootCategoriesInCatalog = (Long) rootCountResults.get(0);
		}
		return numRootCategoriesInCatalog.intValue();
	}

	/**
	 * Determines the number of sub categories in the given <code>Category</code>.
	 *
	 * @param categoryUid the id of the <code>Category</code> to get the count for
	 * @return the number of root categories
	 */
	@Override
	public int getSubCategoryCount(final long categoryUid) {
		int numSubCategories = 0;
		final List<Object> rootCountResults =
			getPersistenceEngine().retrieveByNamedQuery("COUNT_SUBCATEGORIES_IN_CATEGORY", categoryUid);
		if (rootCountResults.size() == 1) {
			numSubCategories = ((Long) rootCountResults.get(0)).intValue();
		}
		return numSubCategories;
	}

	/**
	 * Adds all products in the master category of the given {@link Category linked category}
	 * (including sub-categories). A linked category is one that {@link Category#isLinked()}
	 * returns {@code true}.
	 *
	 * @param linkedCategory the {@link Category} whose products/sub-category should be included
	 * @return the updated category
	 * @throws EpServiceException in case of any errors
	 */
	@Override
	public Category addLinkedCategoryProducts(final Category linkedCategory) {
		if (!linkedCategory.isLinked()) {
			throw new EpServiceException("linkedCategory must be a linked category");
		}

		final Collection<Product> products = getProductService().findByCategoryUid(linkedCategory.getMasterCategory().getUidPk(),
				getLinkProductCategoryLoadTuner());

		// JPA seems to trigger a field access when a category is added. Use same fetch
		// plan so that fields aren't loaded because it was cleared in the previous call.
		fetchPlanHelper.configureFetchGroupLoadTuner(getLinkProductCategoryLoadTuner(), true);

		for (final Product product : products) {
			product.addCategory(linkedCategory);
			getProductDao().saveOrUpdate(product);
		}

		// Set the linked category's Include flag to true
		linkedCategory.setIncluded(true);
		final Category result = saveOrUpdate(linkedCategory);

		fetchPlanHelper.clearFetchPlan();

		// Use recursion to include products in any sub-categories
		for (final Category currCategory : getCategoryLookup().findChildren(result)) {
			addLinkedCategoryProducts(currCategory);
		}

		// Now we're done we notify the indexer that the products in this category need to be reindexed.
		getProductService().notifyCategoryUpdated(result);
		return result;
	}

	/**
	 * Removes all products in the given {@link Category} (including sub-categories). The
	 * behaviour is undefined in the case where you remove products from a category for which any
	 * of the products only exists in that category.
	 *
	 * @param category the {@link Category} whose products/sub-category should be excluded
	 * @return the updated category
	 * @throws EpServiceException in case of any errors
	 */
	@Override
	public Category removeCategoryProducts(final Category category) {

		Category result = category;
		getProductService().notifyCategoryUpdated(result);

		removeProductCategory(category.getUidPk());

		// Set the linked category's Include flag to false
		if (category.isLinked()) {
			category.setIncluded(false);
			result = saveOrUpdate(category);
		}

		// Use recursion to exclude products in any sub-categories
		for (final Category currCategory : getCategoryLookup().findChildren(result)) {
			removeCategoryProducts(currCategory);
		}

		// Products start to get indexed as soon as they are saved resulting in invalid categories
		// for products, need to re-index those products now
		return result;
	}

	/**
	 * Returns a <code>List</code> of Category objects linked to the Category indicated by the given <code>masterCategoryUid</code>.
	 *
	 * @param masterCategoryUid the master category uid to look up
	 * @return a <code>List</code> of all UIDs of all Category objects linked to the Category indicated by the given <code>masterCategoryUid</code>
	 */
	@Override
	public List<Category> findLinkedCategories(final long masterCategoryUid) {
		return getPersistenceEngine().retrieveByNamedQuery("LINKED_CATEGORY_SELECT_BY_MASTER_CATEGORY_UID", Long.valueOf(masterCategoryUid));
	}

	@Override
	public void removeLinkedCategoryTree(final Category linkedCategory) throws EpServiceException {
		if (!linkedCategory.isLinked()) {
			throw new IllegalArgumentException("Category(uidPk=" + linkedCategory.getUidPk() + ") is not a linked category.");
		}
		// First, exclude all the products in this category
		removeCategoryProducts(linkedCategory);

		final Collection<Category> children = getCategoryLookup().findChildren(linkedCategory);
		for (final Category currCategory : children) {
			removeLinkedCategoryTree(currCategory);
		}

		// delete it from the database
		getPersistenceEngine().delete(linkedCategory);
		getPersistenceEngine().evictObjectFromCache(linkedCategory);
		addCategoryDeleted(linkedCategory.getUidPk());
	}

	/**
	 * Reorders the given Category up. That is, the Category's order value will be swapped with the order value of the Category above it. If there
	 * are no Category objects above this one (i.e. this Category is 'first' in the list), then do nothing.
	 *
	 * @param category the Category to reorder
	 */
	@Override
	public void updateCategoryOrderUp(final Category category) {
		final List<Category> listRootCategories;
		long categoryUidToSwap = -1;

		if (category.getParentGuid() == null) {
			// If reordering a root category, scan the other root categories in the catalog
			listRootCategories = this.listRootCategories(category.getCatalog(), false);
		} else {
			// If reordering a non-root category, scan the other categories with the same parent
			listRootCategories = findDirectDescendantCategories(category.getParentGuid());
		}

		// Find the category to swap positions with. It will be the category that is 'above' this one (order-wise).
		Category lastCategory = null;
		for (final Category currCategory : listRootCategories) {
			if (currCategory.getUidPk() == category.getUidPk()) {
				if (lastCategory != null) {
					categoryUidToSwap = lastCategory.getUidPk();
				}
				break;
			}
			lastCategory = currCategory;
		}

		if (categoryUidToSwap != -1) {
			updateOrder(category.getUidPk(), categoryUidToSwap);
		}
	}

	/**
	 * Reorders the given Category down. That is, the Category's order value will be swapped with the order value of the Category below it. If there
	 * are no Category objects below this one (i.e. this Category is 'last' in the list), then do nothing.
	 *
	 * @param category the Category to reorder
	 */
	@Override
	public void updateCategoryOrderDown(final Category category) {
		final List<Category> listRootCategories;
		long categoryUidToSwap = -1;

		if (category.getParentGuid() == null) {
			// If reordering a root category, scan the other root categories in the catalog
			listRootCategories = this.listRootCategories(category.getCatalog(), false);
		} else {
			// If reordering a non-root category, scan the other categories with the same parent
			listRootCategories = findDirectDescendantCategories(category.getParentGuid());
		}

		// Find the category to swap positions with. It will be the category that is 'below' this one (order-wise).
		for (final Iterator<Category> categoryIterator = listRootCategories.iterator(); categoryIterator.hasNext();) {
			if (categoryIterator.next().getUidPk() == category.getUidPk()) {
				if (categoryIterator.hasNext()) {
					categoryUidToSwap = categoryIterator.next().getUidPk();
				}
				break;
			}
		}

		if (categoryUidToSwap != -1) {
			updateOrder(category.getUidPk(), categoryUidToSwap);
		}
	}

	private FetchGroupLoadTuner getDefaultFetchGroupLoadTuner() {
		if (defaultFetchGroupLoadTuner == null) {
			final FetchGroupLoadTuner defaultFetchGroupLoadTuner = getBean(ContextIdNames.FETCH_GROUP_LOAD_TUNER);
			defaultFetchGroupLoadTuner.addFetchGroup(FetchGroupConstants.CATEGORY_BASIC,
					FetchGroupConstants.CATEGORY_ATTRIBUTES,
					FetchGroupConstants.CATALOG_DEFAULTS, // need default locale
					FetchGroupConstants.CATEGORY_AVAILABILITY
			);
			this.defaultFetchGroupLoadTuner = defaultFetchGroupLoadTuner;
		}
		return defaultFetchGroupLoadTuner;
	}

	private FetchGroupLoadTuner getLinkProductCategoryLoadTuner() {
		if (linkProductCategoryLoadTuner == null) {
			final FetchGroupLoadTuner loadTuner = getBean(ContextIdNames.FETCH_GROUP_LOAD_TUNER);
			loadTuner.addFetchGroup(FetchGroupConstants.LINK_PRODUCT_CATEGORY, FetchGroupConstants.PRODUCT_HASH_MINIMAL,
					FetchGroupConstants.CATEGORY_HASH_MINIMAL, FetchGroupConstants.CATALOG_DEFAULTS);
			linkProductCategoryLoadTuner = loadTuner;
		}
		return linkProductCategoryLoadTuner;
	}

	/**
	 * Sets the {@link CatalogService} instance to use.
	 *
	 * @param catalogService the {@link CatalogService} instance to use
	 */
	public void setCatalogService(final CatalogService catalogService) {
		this.catalogService = catalogService;
	}

	/**
	 * @return an instance of {@link IndexNotificationService}
	 */
	public IndexNotificationService getIndexNotificationService() {
		return getBean("indexNotificationService");
	}

	@Override
	public boolean categoryExistsWithCompoundGuid(final String compoundGuid) {
		final String categoryCode = compoundGuid.substring(0, compoundGuid.indexOf(Category.CATEGORY_LEGACY_GUID_DELIMITER));
		final String catalogCode = compoundGuid.substring(compoundGuid.indexOf(Category.CATEGORY_LEGACY_GUID_DELIMITER)
				+ Category.CATEGORY_LEGACY_GUID_DELIMITER.length());

		final Map<String, Object> parameters = new HashMap<>();
		parameters.put("categoryCode", categoryCode);
		parameters.put("catalogCode", catalogCode);
		List<Long> results = getPersistenceEngine().retrieveByNamedQuery("CATEGORY_EXISTS_FOR_COMPOUND_GUID", parameters);
		if (results.isEmpty() || results.get(0) == 0) {
			results = getPersistenceEngine().retrieveByNamedQuery("LINKED_CATEGORY_EXISTS_FOR_COMPOUND_GUID", parameters);
		}
		return !results.isEmpty() && results.get(0) > 0;
	}

	@Override
	public Set<Long> findAncestorCategoryUidsWithTreeOrder(final Set<Long> categoryUidSet) {
		final Set<Long> resultSet = new LinkedHashSet<>();

		List<Long> parents = getPersistenceEngine().retrieveByNamedQueryWithList("CATEGORY_UID_SELECT_BY_CHILDREN_UIDS", PLACE_HOLDER_FOR_LIST,
				categoryUidSet);

		if (!parents.isEmpty()) {
			resultSet.addAll(findAncestorCategoryUidsWithTreeOrder(new HashSet<>(parents)));
			resultSet.addAll(parents);
		}

		return resultSet;
	}

	protected ProductDao getProductDao() {
		return productDao;
	}

	public void setProductDao(final ProductDao productDao) {
		this.productDao = productDao;
	}

	@Override
	public int findMaxRootOrdering(final long catalogUid) {
		List<Integer> ordering = getPersistenceEngine().retrieveByNamedQuery("CATEGORY_ROOT_MAX_ORDERING", catalogUid);
		if (ordering == null || ordering.isEmpty() || ordering.get(0) == null) {
			return 0;
		}
		return ordering.get(0);
	}

	@Override
	public int findMinRootOrdering(final long catalogUid) {
		List<Integer> ordering = getPersistenceEngine().retrieveByNamedQuery("CATEGORY_ROOT_MIN_ORDERING", catalogUid);
		if (ordering == null || ordering.isEmpty() || ordering.get(0) == null) {
			return 0;
		}
		return ordering.get(0);
	}

	@Override
	public int findMaxChildOrdering(final Category category) {
		List<Integer> ordering = getPersistenceEngine().retrieveByNamedQuery("CATEGORY_CHILD_MAX_ORDERING", category.getUidPk());
		if (ordering == null || ordering.isEmpty() || ordering.get(0) == null) {
			return 0;
		}
		return ordering.get(0);
	}

	@Override
	public int findMinChildOrdering(final Category category) {
		List<Integer> ordering = getPersistenceEngine().retrieveByNamedQuery("CATEGORY_CHILD_MIN_ORDERING", category.getUidPk());
		if (ordering == null || ordering.isEmpty() || ordering.get(0) == null) {
			return 0;
		}
		return ordering.get(0);
	}

	@Override
	public List<Category> getPath(final Category category) {
		List<Category> path = new ArrayList<>();
		Category cursor = category;
		while (cursor != null) {
			path.add(cursor);
			cursor = getCategoryLookup().findParent(cursor);
		}

		return Lists.reverse(path);
	}

	/**
	 * Retrieves a bean from the bean factory.
	 *
	 * @param beanName the name of the bean to retrieve
	 * @param <T> the bean's type
	 * @return the bean
	 */
	protected <T> T getBean(final String beanName) {
		return getBeanFactory().getBean(beanName);
	}

	protected PersistenceEngine getPersistenceEngine() {
		return persistenceEngine;
	}

	public void setPersistenceEngine(final PersistenceEngine persistenceEngine) {
		this.persistenceEngine = persistenceEngine;
	}

	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	protected CategoryLookup getCategoryLookup() {
		return categoryLookup;
	}

	public void setCategoryLookup(final CategoryLookup categoryLookup) {
		this.categoryLookup = categoryLookup;
	}

	protected CategoryGuidUtil getCategoryGuidUtil() {
		return categoryGuidUtil;
	}

	public void setCategoryGuidUtil(final CategoryGuidUtil categoryGuidUtil) {
		this.categoryGuidUtil = categoryGuidUtil;
	}
}
