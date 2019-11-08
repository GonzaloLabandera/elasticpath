/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.webservice.services;

import java.util.List;
import java.util.Optional;

import org.apache.camel.language.Simple;

import com.elasticpath.catalog.entity.category.Category;
import com.elasticpath.catalog.reader.FindAllResponse;

/**
 * Reads an Category entity from a datasource.
 */
public interface CategoryService {

	/**
	 * Reads a projection from a datasource.
	 *
	 * @param store the store code of the projection.
	 * @param code  the guid of the projection.
	 * @return the projection.
	 */
	Optional<Category> get(@Simple("${header.storeCode}") String store, @Simple("${header.categoryCode}") String code);

	/**
	 * Reads children of Category.
	 *
	 * @param store the store code of the projection.
	 * @param code  the guid of the projection.
	 * @return the list of Category children projections.
	 */
	List<Category> getChildren(@Simple("${header.storeCode}") String store, @Simple("${header.categoryCode}") String code);

	/**
	 * Returns a list of all Category entities contained within a given Store.
	 * The results are sorted by Category code in ascending order.
	 *
	 * @param store               the store code of the projection.
	 * @param limit               the number of Categories to return in a single request.
	 * @param startAfter          pagination cursor that instructs the service to return results starting after the provided Category.
	 * @param modifiedSince       pagination threshold that instructs the service to return results starting after the provided data.
	 * @param modifiedSinceOffset the time in minutes by which to backdate the modifiedSince parameter value.
	 * @return list of all Options contained within a given Store.
	 */
	FindAllResponse<Category> getAllCategories(@Simple("${header.storeCode}") String store, @Simple("${header.limit}") String limit,
											   @Simple("${header.startAfter}") String startAfter,
											   @Simple("${header.modifiedSince}") String modifiedSince,
											   @Simple("${header.modifiedSinceOffset}") String modifiedSinceOffset);

	/**
	 * Returns a list of latest Category entities with codes from a given list.
	 *
	 * @param store the store code of the Option.
	 * @param codes list of Category codes.
	 * @return list of all Categories contained within a given Store.
	 */
	List<Category> getLatestCategoriesWithCodes(@Simple("${body.getData.get(store)}") String store,
												@Simple("${body.getData.get(codes)}") List<String> codes);

}
