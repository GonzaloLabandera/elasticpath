/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.capability.reader;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.FIELD_METADATA_IDENTITY_TYPE;

import java.util.List;
import java.util.Optional;

import com.elasticpath.catalog.entity.fieldmetadata.FieldMetaDataReaderCapability;
import com.elasticpath.catalog.entity.fieldmetadata.FieldMetadata;
import com.elasticpath.catalog.reader.FindAllResponse;
import com.elasticpath.catalog.reader.ModifiedSince;
import com.elasticpath.catalog.reader.PaginationRequest;
import com.elasticpath.catalog.spi.service.CatalogService;

/**
 * An implementation of {@link FieldMetaDataReaderCapability}.
 */
public class FieldMetaDataReaderCapabilityImpl implements FieldMetaDataReaderCapability {

	private final CatalogService catalogService;

	/**
	 * Constructor.
	 *
	 * @param catalogService data service for Catalog of projections.
	 */
	public FieldMetaDataReaderCapabilityImpl(final CatalogService catalogService) {
		this.catalogService = catalogService;
	}

	@Override
	public Optional<FieldMetadata> get(final String store, final String code) {
		return catalogService.read(FIELD_METADATA_IDENTITY_TYPE, code, store);
	}

	@Override
	public FindAllResponse<FieldMetadata> findAll(final String store, final PaginationRequest pagination, final ModifiedSince modifiedSince) {
		return catalogService.readAll(FIELD_METADATA_IDENTITY_TYPE, store, pagination, modifiedSince);
	}

	@Override
	public List<FieldMetadata> findAllWithCodes(final String store, final List<String> codes) {
		return catalogService.readAll(FIELD_METADATA_IDENTITY_TYPE, store, codes);
	}
}
