/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.capability.reader;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.OFFER_IDENTITY_TYPE;

import java.util.List;
import java.util.Optional;

import com.elasticpath.catalog.entity.offer.Offer;
import com.elasticpath.catalog.entity.offer.OfferReaderCapability;
import com.elasticpath.catalog.reader.FindAllResponse;
import com.elasticpath.catalog.reader.ModifiedSince;
import com.elasticpath.catalog.reader.PaginationRequest;
import com.elasticpath.catalog.spi.service.CatalogService;

/**
 * An implementation of {@link OfferReaderCapability}.
 */
public class OfferReaderCapabilityImpl implements OfferReaderCapability {

	private final CatalogService catalogService;

	/**
	 * Constructor.
	 *
	 * @param catalogService data service for Catalog of projections.
	 */
	public OfferReaderCapabilityImpl(final CatalogService catalogService) {
		this.catalogService = catalogService;
	}

	/**
	 * Get Offer projection.
	 *
	 * @param store store.
	 * @param code  the code of the projection.
	 * @return container object for Offer projection.
	 */
	public Optional<Offer> get(final String store, final String code) {
		return catalogService.read(OFFER_IDENTITY_TYPE, code, store);
	}

	@Override
	public FindAllResponse<Offer> findAll(final String store, final PaginationRequest pagination, final ModifiedSince modifiedSince) {
		return catalogService.readAll(OFFER_IDENTITY_TYPE, store, pagination, modifiedSince);
	}

	@Override
	public List<Offer> findAllWithCodes(final String store, final List<String> codes) {
		return catalogService.readAll(OFFER_IDENTITY_TYPE, store, codes);
	}

}
