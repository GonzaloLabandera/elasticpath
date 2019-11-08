/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.plugin.capability.reader;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.CATEGORY_IDENTITY_TYPE;

import java.util.List;
import java.util.Optional;

import com.elasticpath.catalog.entity.category.Category;
import com.elasticpath.catalog.entity.category.CategoryReaderCapability;
import com.elasticpath.catalog.reader.FindAllResponse;
import com.elasticpath.catalog.reader.ModifiedSince;
import com.elasticpath.catalog.reader.PaginationRequest;
import com.elasticpath.catalog.spi.service.CatalogService;

/**
 * An implementation of {@link CategoryReaderCapability}.
 */
public class CategoryReaderCapabilityImpl implements CategoryReaderCapability {

    private final CatalogService catalogService;

    /**
     * Constructor.
     *
     * @param catalogService data service for Catalog of projections.
     */
    public CategoryReaderCapabilityImpl(final CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Override
    public Optional<Category> get(final String store, final String code) {
        return catalogService.read(CATEGORY_IDENTITY_TYPE, code, store);
    }

    @Override
    public FindAllResponse<Category> findAll(final String store, final PaginationRequest pagination, final ModifiedSince modifiedSince) {
        return catalogService.readAll(CATEGORY_IDENTITY_TYPE, store, pagination, modifiedSince);
    }

    @Override
    public List<Category> findAllWithCodes(final String store, final List<String> codes) {
        return catalogService.readAll(CATEGORY_IDENTITY_TYPE, store, codes);
    }
}
