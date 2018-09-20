/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.service.catalog;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Category;

/**
 * Provide category-related business service.
 */
@SuppressWarnings("PMD.TooManyMethods")
public interface CategoryService {

	/**
	 * Adds the given category.
	 *
	 * @param category the category to add
	 * @return the persisted instance of category
	 * @throws EpServiceException - in case of any errors
	 * @throws com.elasticpath.commons.exception.DuplicateKeyException if there is a duplicate code for the category
	 */
	Category add(Category category) throws EpServiceException;

	/**
	 * Updates the given category.
	 *
	 * @param category the category to update
	 * @return the updated category instance
	 * @throws EpServiceException - in case of any errors
	 */
	Category update(Category category) throws EpServiceException;

	/**
	 * Retrieve a list of root categories.
	 *
	 * @param catalog the catalog to get the root categories for
	 * @param availableOnly set it to <code>true</code> to only list available root categories
	 * @return return all root categories
	 */
	List<Category> listRootCategories(Catalog catalog, boolean availableOnly);

	/**
	 * Retrieve the {@link Category} with the given category code.
	 *
	 * @param categoryCode the category code
	 * @return the category with the given GUID
	 * @throws EpServiceException in case of any error
	 */
	Category findByCode(String categoryCode);

	/**
	 * Save or update the given category.
	 *
	 * @param category the category to save or update
	 * @return the updated object instance
	 * @throws EpServiceException - in case of any errors
	 */
	Category saveOrUpdate(Category category) throws EpServiceException;

	/**
	 * Query if the category has sub categories. For performance.
	 *
	 * @param categoryUid the category's uid
	 * @return true if the category has subcategories
	 */
	boolean hasSubCategories(long categoryUid);

	/**
	 * Retrieve all descendant category UIDs of the given category UID.
	 *
	 * @param categoryUid the category UID.
	 * @return the list of UID of the direct and indirect sub-categories of the given start category.
	 */
	List<Long> findDescendantCategoryUids(long categoryUid);

	/**
	 * Retrieve all descendant category UIDs of the given category UIDs.
	 *
	 * @param categoryUids the category UIDs.
	 * @return the list of UIDs of the direct and indirect sub-categories of the given start category UIDs.
	 */
	List<Long> findDescendantCategoryUids(List<Long> categoryUids);

	/**
	 * Retrieve all direct descendant Category objects of the given category guid. <br>
	 * For example, if Category-A has children SubCategory-A and SubCategory-B, and SubCategory-A has child SubSubCategory-A, then this method will
	 * return a list containing only SubCategory-A and SubCategory-B (not SubSubCategory-A since it is not a direct descendant of Category-A).
	 *
	 * @param categoryGuid the category gUID.
	 * @return the list of direct sub-categories of the given start category.
	 */
	List<Category> findDirectDescendantCategories(String categoryGuid);

	/**
	 * Return <code>true</code> if the product with the given product UID is in the category with the given category UID. <br>
	 * Otherwise, <code>false</code>.
	 *
	 * @param productUid the product UID
	 * @param categoryUid the category UID
	 * @return <code>true</code> if the product with the given product UID is in the category with the given category UID. <br>
	 *         Otherwise, <code>false</code>
	 */
	boolean isProductInCategory(long productUid, long categoryUid);

	/**
	 * Check if the category has any products under it.
	 *
	 * @param categoryUid the UID of the category to be checked.
	 * @return true if the category has products
	 * @throws EpServiceException in case of any error
	 */
	boolean hasProduct(long categoryUid) throws EpServiceException;

	/**
	 * Deletes the category and all sub categories. Category needs to be empty before it is removed.
	 *
	 * @param categoryUid the UID of the category to remove
	 * @throws EpServiceException - in case of any errors
	 */
	void removeCategoryTree(long categoryUid) throws EpServiceException;

	/**
	 * Re orders (swaps the ordering field) of the two parameter categories.
	 *
	 * @param uidOne UID of a category to reorder
	 * @param uidTwo UID of a category to reorder
	 * @throws EpServiceException in case of any errors
	 */
	void updateOrder(long uidOne, long uidTwo) throws EpServiceException;

	/**
	 * Returns all available category UIDs as a list.
	 *
	 * @return all available category UIDs as a list
	 */
	List<Long> findAvailableUids();

	/**
	 * Get the category UID of the given category identifier. <br>
	 * The given category identifier will first be dealt as a GUID to try to find a category UID in the given catalog. <br>
	 * It no category GUID is found and the given identifier is a <code>long</code> value, itself will be dealt as UID, but on a global scope
	 * (looking both inside and outside the given catalog).
	 *
	 * @param categoryId the category GUID or UID
	 * @param catalog the catalog to search in for the GUID
	 * @return the category UID, otherwise 0
	 * @throws EpServiceException in case of any errors
	 */
	long findUidById(String categoryId, Catalog catalog) throws EpServiceException;

	/**
	 * Checks whether the given category code exists in any catalog. <br>
	 * A category code exists if it is already in use in the system for that type of object.
	 *
	 * @param code the category code
	 * @return whether the given category code exists
	 * @throws EpServiceException in case of any errors
	 */
	boolean isCodeInUse(String code) throws EpServiceException;

	/**
	 * Checks whether a category with the given guid already exists in the system.
	 *
	 * @param guid the guid
	 * @return true if a category with the given guid already exists in the system, false otherwise
	 * @throws EpServiceException in case of any errors
	 */
	boolean isGuidInUse(String guid);

	/**
	 * Returns all category UIDs as a list.
	 *
	 * @return all category UIDs as a list
	 */
	List<Long> findAllUids();

	/**
	 * Retrieves list of <code>Category</code> UIDs where the last modified date is later than the specified date.
	 *
	 * @param date date to compare with the last modified date
	 * @return list of <code>Category</code> whose last modified date is later than the specified date
	 */
	List<Long> findUidsByModifiedDate(Date date);

	/**
	 * Retrieves list of category UIDs where the deleted date is later than the specified date.
	 *
	 * @param date date to compare with the deleted date
	 * @return list of category UIDs whose deleted date is later than the specified date
	 */
	List<Long> findUidsByDeletedDate(Date date);

	/**
	 * Returns the set of category UIDs, which are ancestors of the given product UID. <br>
	 * The category UID of the category where the product lives is not apart of the returned set.
	 *
	 * @param productUid the product UID
	 * @return a set of category UIDs
	 */
	Set<Long> findAncestorCategoryUidsByProductUid(long productUid);

	/**
	 * Returns the set of category uids which are ancestors of the given categoryUid. <br>
	 * The given categoryUid is not a part of the returned set.
	 *
	 * @param categoryUid - The uid of the category to search for ancestors' uids on.
	 * @return A set of ancestor category UIDs.
	 */
	Set<Long> findAncestorCategoryUidsByCategoryUid(long categoryUid);

	/**
	 * Returns the set of category codes which are ancestors of the given categoryUid. <br>
	 * The category code corresponding to the given categoryUid is not in the returned set.
	 *
	 * @param categoryUid - The uid of the category to search for ancestors' codes on.
	 * @return A set of ancestor category codes.
	 */
	Set<String> findAncestorCategoryCodesByCategoryUid(long categoryUid);

	/**
	 * Returns a list of <code>Category</code> UIDs based on the given Catalog UIDPK.
	 *
	 * @param catalogUid identifier of the catalog
	 * @return a list of <code>Catalog</code>s UIDs
	 */
	List<Long> findCategoryUidsForCatalog(long catalogUid);

	/**
	 * Returns a list of <code>Category</code> UIDs based on the given Catalog Code.
	 *
	 * @param catalogCode is code of the catalog
	 * @return a list of <code>Catalog</code>s UIDs
	 */
	List<Long> findCategoryUidsForCatalog(String catalogCode);

	/**
	 * Get a list of featured product by the category UID.
	 *
	 * @param categoryUid the category uidPk.
	 * @return a list of Object arrays where
	 *         <ul>
	 *         <li>the element at index <code>0</code> is a <code>Product</code></li>
	 *         <li>and the element at index <code>1</code> is an <code>Integer</code> feature order value.</li>
	 *         </ul>
	 */
	List<Object[]> getFeaturedProductsList(long categoryUid);

	/**
	 * Get a list of featured product UID by the category UID.
	 *
	 * @param categoryUid the category UID.
	 * @return a list of product UID.
	 */
	List<Long> findFeaturedProductUidList(long categoryUid);

	/**
	 * Creates a {@link Category} that is linked to the given {@code masterCategory}, with its parent set to the given {@code parentCategory}, and
	 * its catalog set to the given {@link Catalog}. <br>
	 * This method creates a linked category to the given {@code masterCategory}, derives all of its products, and then recursively does the same for
	 * any of the {@code masterCategory}'s sub-categories.
	 *
	 * @param masterCategoryUid the uid of category to link to
	 * @param parentCategoryUid the uid of category to set the parent, set to -1 if linked category is a root category
	 * @param catalogUid the catalog uid that contains the linked category
	 * @return the newly created linked {@link Category}
	 */
	Category addLinkedCategory(long masterCategoryUid, long parentCategoryUid, long catalogUid);

	/**
	 * Adds all products in the master category of the given {@link Category linked category} (including sub-categories). <br>
	 * A linked category is one that {@link Category#isLinked()} returns {@code true}.
	 *
	 * @param linkedCategory the {@link Category} whose products/sub-category should be excluded
	 * @return the updated category
	 * @throws EpServiceException in case of any errors
	 */
	Category addLinkedCategoryProducts(Category linkedCategory);

	/**
	 * Removes all products in the given {@link Category} (including sub-categories). <br>
	 * The behaviour is undefined in the case where you remove products from a category for which any of the products only exists in that category.
	 *
	 * @param category the {@link Category} whose products/sub-category should be excluded
	 * @return the updated category
	 * @throws EpServiceException in case of any errors
	 */
	Category removeCategoryProducts(Category category);

	/**
	 * Returns a <code>List</code> of all Category objects linked to the Category indicated by the given <code>masterCategoryUid</code>.
	 *
	 * @param masterCategoryUid the master category uid to look up
	 * @return a <code>List</code> of all UIDs of all Category objects linked to the Category indicated by the given <code>masterCategoryUid</code>
	 */
	List<Category> findLinkedCategories(long masterCategoryUid);

	/**
	 * Deletes the linked category and all sub-categories. Unlinks contained products.
	 *
	 * @param linkedCategory the uid of the linked category to remove
	 * @throws EpServiceException - in case of any errors
	 */
	void removeLinkedCategoryTree(Category linkedCategory) throws EpServiceException;

	/**
	 * Reorders the given Category up. <br>
	 * That is, the Category's order value will be swapped with the order value of the Category above it. <br>
	 * If there are no Category objects above this one (i.e. this Category is 'first' in the list), then do nothing.
	 *
	 * @param category the Category to reorder
	 */
	void updateCategoryOrderUp(Category category);

	/**
	 * Reorders the given Category down. <br>
	 * That is, the Category's order value will be swapped with the order value of the Category below it. <br>
	 * If there are no Category objects below this one (i.e. this Category is 'last' in the list), then do nothing.
	 *
	 * @param category the Category to reorder
	 */
	void updateCategoryOrderDown(Category category);

	/**
	 * Determines the number of root categories in the given <code>Catalog</code>.
	 *
	 * @param catalogUid the id of the <code>Catalog</code> to get the count for
	 * @return the number of root categories
	 */
	int getRootCategoryCount(long catalogUid);

	/**
	 * Determines the number of sub categories in the given <code>Category</code>.
	 *
	 * @param categoryUid the id of the <code>Category</code> to get the count for
	 * @return the number of root categories
	 */
	int getSubCategoryCount(long categoryUid);

	/**
	 * Gets the category code associated with a given category uidPk.
	 *
	 * @param uidPk - The unique ID of the category to get the code for.
	 * @return The category code of the category if it exists, empty string otherwise.
	 */
	String findCodeByUid(long uidPk);

	/**
	 * Gets the category uidPk associated with a given compound category guid.
	 *
	 * @param compoundCategoryGuid - The unique compound category guid to get the uidPk for.
	 * @return The category uidPk of the category if it exists, -1 value otherwise.
	 */
	Long findUidByCompoundGuid(String compoundCategoryGuid);

	/**
	 * Get the top-level catalog for the given category. If the category is a linked category then its master category's catalog will be obtained.
	 *
	 * @param category the category for which to retrieve the master catalog
	 * @return the requested catalog, or null if one cannot be found
	 * @throws com.elasticpath.persistence.api.EpPersistenceException in case of error
	 */
	Catalog getMasterCatalog(Category category);

	/**
	 * Determines if the category with the given guid exists.
	 *
	 * @param compoundGuid the compound Category/Catalog guid.
	 * @return true if the category/Catalog exists, false otherwise.
	 */
	boolean categoryExistsWithCompoundGuid(String compoundGuid);

	/**
	 * Finds set of all parent category uids by set of category uids.
	 *
	 * @param categoryUidSet the category uids set
	 * @return the set of all parents for given set of category uids. <br>
	 *         The result set will have the tree order (first root category uids then their children and so on)
	 */
	Set<Long> findAncestorCategoryUidsWithTreeOrder(Set<Long> categoryUidSet);

	/**
	 * Finds the maximum ordering of a root category.
	 *
	 * @param catalogUid UID of the catalog to find root category ordering
	 * @return maximum ordering of root categories or 0 if the parent is not found
	 */
	int findMaxRootOrdering(long catalogUid);

	/**
	 * Finds the minimum ordering of a root category.
	 *
	 * @param catalogUid UID of the catalog to find root category ordering
	 * @return minimum ordering of root categories or 0 if the parent is not found
	 */
	int findMinRootOrdering(long catalogUid);

	/**
	 * Finds the maximum ordering of the direct children to the given category.
	 *
	 * @param category parent {@link Category} to find ordering
	 * @return maximum ordering of child categories or 0 if the parent is not found
	 */
	int findMaxChildOrdering(Category category);

	/**
	 * Finds the minimum ordering of the direct children to the given category.
	 *
	 * @param category parent {@link Category} to find ordering
	 * @return minimum ordering of child categories or 0 if the parent is not found
	 */
	int findMinChildOrdering(Category category);

	/**
	 * Returns the category path of the given category as a <code>List&lt;Category&gt;</code>. The root category will be the first
	 * entry in the list.  The category passed in as a parameter will be the last entry in the list.
	 *
	 * @return the category path as a <code>List</code>.
	 * @param category the category whose path should be returned.
	 */
	List<Category> getPath(Category category);
}
