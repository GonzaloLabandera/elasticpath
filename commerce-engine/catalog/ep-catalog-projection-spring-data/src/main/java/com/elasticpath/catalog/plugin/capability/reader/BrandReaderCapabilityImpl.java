/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.capability.reader;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.BRAND_IDENTITY_TYPE;

import java.util.List;
import java.util.Optional;

import com.elasticpath.catalog.entity.brand.Brand;
import com.elasticpath.catalog.entity.brand.BrandReaderCapability;
import com.elasticpath.catalog.reader.FindAllResponse;
import com.elasticpath.catalog.reader.ModifiedSince;
import com.elasticpath.catalog.reader.PaginationRequest;
import com.elasticpath.catalog.spi.service.CatalogService;

/**
 * An implementation of {@link BrandReaderCapability}.
 */
public class BrandReaderCapabilityImpl implements BrandReaderCapability {

	private final CatalogService catalogService;

	/**
	 * Constructor.
	 *
	 * @param catalogService data service for Catalog of projections.
	 */
	public BrandReaderCapabilityImpl(final CatalogService catalogService) {
		this.catalogService = catalogService;
	}

	/**
	 * Get Brand projection.
	 *
	 * @param store store.
	 * @param code  the code of the projection.
	 * @return container object for Offer projection.
	 */
	public Optional<Brand> get(final String store, final String code) {
		return catalogService.read(BRAND_IDENTITY_TYPE, code, store);
	}

	@Override
	public FindAllResponse<Brand> findAll(final String store, final PaginationRequest pagination, final ModifiedSince modifiedSince) {
		return catalogService.readAll(BRAND_IDENTITY_TYPE, store, pagination, modifiedSince);
	}

	@Override
	public List<Brand> findAllWithCodes(final String store, final List<String> codes) {
		return catalogService.readAll(BRAND_IDENTITY_TYPE, store, codes);
	}

}
