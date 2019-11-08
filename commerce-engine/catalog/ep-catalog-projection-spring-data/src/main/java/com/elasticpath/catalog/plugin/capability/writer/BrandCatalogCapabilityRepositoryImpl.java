/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.capability.writer;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.BRAND_IDENTITY_TYPE;

import java.util.List;

import com.elasticpath.catalog.entity.brand.Brand;
import com.elasticpath.catalog.spi.capabilities.BrandWriterRepository;
import com.elasticpath.catalog.spi.service.CatalogService;

/**
 * An implementation of {@link BrandWriterRepository}.
 */
public class BrandCatalogCapabilityRepositoryImpl implements BrandWriterRepository {

	private final CatalogService catalogService;

	/**
	 * Constructor.
	 *
	 * @param catalogService data service for Catalog of projections.
	 */
	public BrandCatalogCapabilityRepositoryImpl(final CatalogService catalogService) {
		this.catalogService = catalogService;
	}

	@Override
	public boolean write(final Brand projection) {
		return catalogService.saveOrUpdate(projection);
	}

	@Override
	public void delete(final String code) {
		catalogService.delete(BRAND_IDENTITY_TYPE, code);
	}

	@Override
	public void delete(final String code, final String store) {
		catalogService.delete(BRAND_IDENTITY_TYPE, store, code);
	}

	@Override
	public List<Brand> writeAll(final List<Brand> projections) {
		return catalogService.saveOrUpdateAll(projections);
	}

}
