/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog;

import java.util.List;
import java.util.Optional;

import com.elasticpath.catalog.entity.Projection;
import com.elasticpath.catalog.reader.FindAllResponse;
import com.elasticpath.catalog.reader.ModifiedSince;
import com.elasticpath.catalog.reader.PaginationRequest;

/**
 * A reader for latest version projections.
 *
 * @param <T> - type of projection.
 */
public interface ReadLatestVersionCapability<T extends Projection> extends CatalogReaderCapability {

    /**
     * Reads the latest projection.
     *
     * @param storeCode the storeCode of the projection.
     * @param code the code of the projection.
     * @return the projection.
     */
    Optional<T> get(String storeCode, String code);

    /**
     * Reads all projections.
     *
     * @param store  the storeCode of the projection.
     * @param pagination the pagination request.
     * @param modifiedSince  pagination threshold that instructs the service to return results starting after the provided data.
     * @return {@link FindAllResponse}.
     */
    FindAllResponse<T> findAll(String store, PaginationRequest pagination, ModifiedSince modifiedSince);

    /**
     * Reads latest projection with codes from list.
     *
     * @param store  the store code of the projection.
     * @param codes list of projection codes.
     * @return list of projections.
     */
    List<T> findAllWithCodes(String store, List<String> codes);

}
