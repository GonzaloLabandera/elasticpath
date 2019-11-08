/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.capability.reader;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.ATTRIBUTE_IDENTITY_TYPE;

import java.util.List;
import java.util.Optional;

import com.elasticpath.catalog.entity.attribute.Attribute;
import com.elasticpath.catalog.entity.attribute.AttributeReaderCapability;
import com.elasticpath.catalog.reader.FindAllResponse;
import com.elasticpath.catalog.reader.ModifiedSince;
import com.elasticpath.catalog.reader.PaginationRequest;
import com.elasticpath.catalog.spi.service.CatalogService;

/**
 * An implementation of {@link AttributeReaderCapability}.
 */
public class AttributeReaderCapabilityImpl implements AttributeReaderCapability {

    private final CatalogService catalogService;

    /**
     * Constructor.
     *
     * @param catalogService data service for Catalog of projections.
     */
    public AttributeReaderCapabilityImpl(final CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Override
    public Optional<Attribute> get(final String store, final String code) {
        return catalogService.read(ATTRIBUTE_IDENTITY_TYPE, code, store);
    }

    @Override
    public FindAllResponse<Attribute> findAll(final String store, final PaginationRequest pagination, final ModifiedSince modifiedSince) {
        return catalogService.readAll(ATTRIBUTE_IDENTITY_TYPE, store, pagination, modifiedSince);
    }

    @Override
    public List<Attribute> findAllWithCodes(final String store, final List<String> codes) {
        return catalogService.readAll(ATTRIBUTE_IDENTITY_TYPE, store, codes);
    }
}
