/*
 * Copyright © 2015 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.category;

import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.Product;

/**
 * Repository class for parentCategory search.
 */
public interface CategoryRepository {

	/**
	 * Find root categories for Store.
	 *
	 * @param storeCode the store code
	 * @return ExecutionResult with the root categories
	 */
	Observable<Category> findRootCategories(String storeCode);

	/**
	 * Find by store and category code.
	 *
	 * @param storeCode the store code
	 * @param categoryCode the category code
	 * @return ExecutionResult with the category
	 */
	Single<Category> findByStoreAndCategoryCode(String storeCode, String categoryCode);

	/**
	 * Find category children.
	 *
	 * @param storeCode the store code
	 * @param parentCategoryCode the parent category code
	 * @return ExecutionResult with the child categories
	 */
	Observable<Category> findChildren(String storeCode, String parentCategoryCode);

	/**
	 * Find by category GUID.
	 *
	 * @param categoryGuid the category GUID
	 * @return ExecutionResult with the category
	 */
	Single<Category> findByGuid(String categoryGuid);

	/**
	 * Get all featured products for the category.
	 * @param categoryUid category uidpk
	 * @return featured products
	 */
	Observable<Product> getFeaturedProducts(long categoryUid);
}
