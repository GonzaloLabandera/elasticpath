/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.capability.writer;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.ATTRIBUTE_IDENTITY_TYPE;

import java.util.List;

import com.elasticpath.catalog.entity.attribute.Attribute;
import com.elasticpath.catalog.spi.capabilities.AttributeWriterRepository;
import com.elasticpath.catalog.spi.service.CatalogService;

/**
 * An implementation of {@link AttributeWriterRepository}.
 */
public class AttributeCatalogCapabilityRepositoryImpl implements AttributeWriterRepository {

	private final CatalogService catalogService;

	/**
	 * Constructor.
	 *
	 * @param catalogService data service for Catalog of projections.
	 */
	public AttributeCatalogCapabilityRepositoryImpl(final CatalogService catalogService) {
		this.catalogService = catalogService;
	}

	@Override
	public boolean write(final Attribute projection) {
		return catalogService.saveOrUpdate(projection);
	}

	@Override
	public void delete(final String code) {
		catalogService.delete(ATTRIBUTE_IDENTITY_TYPE, code);
	}

	@Override
	public void delete(final String code, final String store) {
		catalogService.delete(ATTRIBUTE_IDENTITY_TYPE, store, code);
	}

	@Override
	public List<Attribute> writeAll(final List<Attribute> projections) {
		return catalogService.saveOrUpdateAll(projections);
	}

}
