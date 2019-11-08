/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.webservice.services;

import java.util.List;
import java.util.Optional;

import org.apache.camel.language.Simple;

import com.elasticpath.catalog.entity.brand.Brand;
import com.elasticpath.catalog.reader.FindAllResponse;

/**
 * A service for reading {@link Brand}.
 */
public interface BrandService {

	/**
	 * Reads a Brand entity containing offer projections from a datasource.
	 *
	 * @param store the store code of the projection.
	 * @param code  the guid of the projection.
	 * @return the projection.
	 */
	Optional<Brand> get(@Simple("${header.storeCode}") String store, @Simple("${header.brandCode}") String code);

	/**
	 * Returns a list of all Brand entities contained within a given Store.
	 * The results are sorted by Brand code in ascending order.
	 *
	 * @param store               the store code of the projection.
	 * @param limit               the number of Brands to return in a single request.
	 * @param startAfter          pagination cursor that instructs the service to return results starting after the provided Brand.
	 * @param modifiedSince       pagination threshold that instructs the service to return results starting after the provided data.
	 * @param modifiedSinceOffset the time in minutes by which to backdate the modifiedSince parameter value.
	 * @return list of all Brands contained within a given Store.
	 */
	FindAllResponse<Brand> getAllBrands(@Simple("${header.storeCode}") String store, @Simple("${header.limit}") String limit,
										@Simple("${header.startAfter}") String startAfter, @Simple("${header.modifiedSince}") String modifiedSince,
										@Simple("${header.modifiedSinceOffset}") String modifiedSinceOffset);

	/**
	 * Returns a list of latest Brand entities with codes from a given list.
	 *
	 * @param store the store code of the Brand.
	 * @param codes list of Brand codes.
	 * @return list of all Brands contained within a given Store.
	 */
	List<Brand> getLatestBrandsWithCodes(@Simple("${body.getData.get(store)}") String store,
										 @Simple("${body.getData.get(codes)}") List<String> codes);

}
