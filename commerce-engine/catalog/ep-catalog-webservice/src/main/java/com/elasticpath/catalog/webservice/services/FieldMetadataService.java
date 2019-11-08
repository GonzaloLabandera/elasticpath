/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.webservice.services;

import java.util.List;
import java.util.Optional;

import org.apache.camel.language.Simple;

import com.elasticpath.catalog.entity.fieldmetadata.FieldMetadata;
import com.elasticpath.catalog.reader.FindAllResponse;

/**
 * A service for reading {@link FieldMetadata}.
 */
public interface FieldMetadataService {
	/**
	 * Reads a projection from a datasource.
	 *
	 * @param store the store code of the projection.
	 * @param code  the guid of the projection.
	 * @return the projection.
	 */
	Optional<FieldMetadata> get(@Simple("${header.storeCode}") String store, @Simple("${header.fieldMetadataCode}") String code);

	/**
	 * Returns a list of all FieldMetadata contained within a given Store.
	 * The results are sorted by FieldMetadata code in ascending order.
	 *
	 * @param store               the store code of the projection.
	 * @param limit               the number of FieldMetadata to return in a single request.
	 * @param startAfter          pagination cursor that instructs the service to return results starting after the provided FieldMetadata.
	 * @param modifiedSince       pagination threshold that instructs the service to return results starting after the provided data.
	 * @param modifiedSinceOffset the time in minutes by which to backdate the modifiedSince parameter value.
	 * @return list of all FieldMetadatas contained within a given Store.
	 */
	FindAllResponse<FieldMetadata> getAllFieldMetadata(@Simple("${header.storeCode}") String store, @Simple("${header.limit}") String limit,
										  @Simple("${header.startAfter}") String startAfter, @Simple("${header.modifiedSince}") String modifiedSince,
										  @Simple("${header.modifiedSinceOffset}") String modifiedSinceOffset);

	/**
	 * Returns a list of latest FieldMetadata with codes from a given list.
	 *
	 * @param store the store code of the FieldMetadata.
	 * @param codes list of FieldMetadata codes.
	 * @return list of all FieldMetadata contained within a given Store.
	 */
	List<FieldMetadata> getLatestFieldMetadataWithCodes(@Simple("${body.getData.get(store)}") String store,
														@Simple("${body.getData.get(codes)}") List<String> codes);
}
