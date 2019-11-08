/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.capability.writer;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.FIELD_METADATA_IDENTITY_TYPE;

import java.util.List;

import com.elasticpath.catalog.entity.fieldmetadata.FieldMetadata;
import com.elasticpath.catalog.spi.capabilities.FieldMetadataWriterRepository;
import com.elasticpath.catalog.spi.service.CatalogService;

/**
 * An implementation of {@link FieldMetadataWriterRepository}.
 */
public class FieldMetadataCatalogCapabilityRepositoryImpl implements FieldMetadataWriterRepository {

	private final CatalogService catalogService;

	/**
	 * Constructor.
	 *
	 * @param catalogService {@link CatalogService}.
	 */
	public FieldMetadataCatalogCapabilityRepositoryImpl(final CatalogService catalogService) {
		this.catalogService = catalogService;
	}

	@Override
	public boolean write(final FieldMetadata projection) {
		return catalogService.saveOrUpdate(projection);
	}

	@Override
	public void delete(final String code) {
		catalogService.delete(FIELD_METADATA_IDENTITY_TYPE, code);
	}

	@Override
	public void delete(final String code, final String store) {
		catalogService.delete(FIELD_METADATA_IDENTITY_TYPE, store, code);
	}

	@Override
	public List<FieldMetadata> writeAll(final List<FieldMetadata> projections) {
		return catalogService.saveOrUpdateAll(projections);
	}

}
