/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.capability.reader;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.OPTION_IDENTITY_TYPE;

import java.util.List;
import java.util.Optional;

import com.elasticpath.catalog.entity.option.Option;
import com.elasticpath.catalog.entity.option.OptionReaderCapability;
import com.elasticpath.catalog.reader.FindAllResponse;
import com.elasticpath.catalog.reader.ModifiedSince;
import com.elasticpath.catalog.reader.PaginationRequest;
import com.elasticpath.catalog.spi.service.CatalogService;

/**
 * An implementation of {@link OptionReaderCapability}.
 */
public class OptionReaderCapabilityImpl implements OptionReaderCapability {

    private final CatalogService catalogService;

    /**
     * Constructor.
     *
     * @param catalogService data service for Catalog of projections.
     */
    public OptionReaderCapabilityImpl(final CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Override
    public Optional<Option> get(final String store, final String code) {
        return catalogService.read(OPTION_IDENTITY_TYPE, code, store);
    }

    @Override
    public FindAllResponse<Option> findAll(final String store, final PaginationRequest pagination, final ModifiedSince modifiedSince) {
        return catalogService.readAll(OPTION_IDENTITY_TYPE, store, pagination, modifiedSince);
    }

	@Override
	public List<Option> findAllWithCodes(final String store, final List<String> codes) {
		return catalogService.readAll(OPTION_IDENTITY_TYPE, store, codes);
	}

}
