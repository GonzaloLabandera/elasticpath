/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.plugin.capability.writer;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.CATEGORY_IDENTITY_TYPE;

import java.util.List;

import com.elasticpath.catalog.entity.category.Category;
import com.elasticpath.catalog.spi.capabilities.CategoryWriterRepository;
import com.elasticpath.catalog.spi.service.CatalogService;

/**
 * An implementation of {@link CategoryWriterRepository}.
 */
public class CategoryCatalogCapabilityRepositoryImpl implements CategoryWriterRepository {

	private final CatalogService catalogService;

	/**
	 * Constructor.
	 *
	 * @param catalogService data service for Catalog of projections.
	 */
	public CategoryCatalogCapabilityRepositoryImpl(final CatalogService catalogService) {
		this.catalogService = catalogService;
	}

	@Override
	public boolean write(final Category projection) {
		return catalogService.saveOrUpdate(projection);
	}

	@Override
	public void delete(final String code) {
		catalogService.delete(CATEGORY_IDENTITY_TYPE, code);
	}

	@Override
	public void delete(final String code, final String store) {
		catalogService.delete(CATEGORY_IDENTITY_TYPE, store, code);
	}

	@Override
	public List<Category> writeAll(final List<Category> projections) {
		return catalogService.saveOrUpdateAll(projections);
	}
}
