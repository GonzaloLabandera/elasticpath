/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.spi.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.elasticpath.catalog.entity.Projection;
import com.elasticpath.catalog.reader.FindAllResponse;
import com.elasticpath.catalog.reader.ModifiedSince;
import com.elasticpath.catalog.reader.PaginationRequest;

/**
 * Represents a data service for projections.
 */
public interface CatalogService {

	/**
	 * Creates or updates the projections.
	 *
	 * @param projection the projection to create or update.
	 * @param <T>        type of projection.
	 * @return true if projection is saved successfully, false if projection is not saved.
	 */
	<T extends Projection> boolean saveOrUpdate(T projection);

	/**
	 * Adds the deleted projection to a datasource.
	 *
	 * @param type the type of the projection.
	 * @param code the guid of the projection.
	 */
	void delete(String type, String code);

	/**
	 * Adds the tombstone projection.
	 *
	 * @param type  the type of the projection.
	 * @param store store code.
	 * @param code  the guid of the projection.
	 */
	void delete(String type, String store, String code);

	/**
	 * Adds the deleted projection to a datasource.
	 *
	 * @param entities the projection to delete.
	 */
	void delete(List<? extends Projection> entities);

	/**
	 * Adds tombstone projections from list to datasource.
	 *
	 * @param <T>         type of projection.
	 * @param projections list of tombstone projections.
	 */
	<T extends Projection> void deleteAll(List<T> projections);

	/**
	 * Reads a projection from a datasource.
	 *
	 * @param type  the type of the projection.
	 * @param code  the guid of the projection.
	 * @param store the store code of the projection.
	 * @param <T>   type of projection.
	 * @return the projection.
	 */
	<T extends Projection> Optional<T> read(String type, String code, String store);

	/**
	 * Reads a list of projections from a datasource.
	 *
	 * @param type the type of the projection.
	 * @param code the guid of the projection.
	 * @param <T>  type of projection.
	 * @return the list of projections.
	 */
	<T extends Projection> List<T> readAll(String type, String code);

	/**
	 * Reads a list of projections from a datasource.
	 *
	 * @param type  the type of the projection.
	 * @param codes the list of projections codes.
	 * @param <T>   type of projection.
	 * @return the list of projections.
	 */
	<T extends Projection> List<T> readAll(String type, List<String> codes);

	/**
	 * Reads all projections.
	 *
	 * @param type          the type of the projection.
	 * @param store         the store code of the projection.
	 * @param pagination    the pagination request.
	 * @param modifiedSince pagination threshold that instructs the service to return results starting after the provided data.
	 * @param <T>           type of projection.
	 * @return {@link FindAllResponse}.
	 */
	<T extends Projection> FindAllResponse<T> readAll(String type, String store, PaginationRequest pagination, ModifiedSince modifiedSince);

	/**
	 * Reads latest projections with codes from list.
	 *
	 * @param type  the type of the projection.
	 * @param store the store code of the projection.
	 * @param codes list of projection codes.
	 * @param <T>   type of projection.
	 * @return list of Projection
	 */
	<T extends Projection> List<T> readAll(String type, String store, List<String> codes);

	/**
	 * Creates or updates the projections.
	 *
	 * @param projections the list of projections to create or update.
	 * @param <T>         type of projections.
	 * @return the list of saved or updated projections.
	 */
	<T extends Projection> List<T> saveOrUpdateAll(List<T> projections);

	/**
	 * Removes projections of specified type.
	 *
	 * @param type the type of the projection.
	 * @return an amount of records that have been removed.
	 */
	int removeAll(String type);

	/**
	 * Reads nearest date projections to expire.
	 *
	 * @return a nearest date projections to expire.
	 */
	Optional<Date> readNearestExpiredTime();
}
