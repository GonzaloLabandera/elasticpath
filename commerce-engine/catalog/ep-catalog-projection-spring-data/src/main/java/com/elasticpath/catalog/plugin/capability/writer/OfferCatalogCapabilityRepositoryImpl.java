/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.capability.writer;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.OFFER_IDENTITY_TYPE;

import java.util.List;

import com.elasticpath.catalog.entity.offer.Offer;
import com.elasticpath.catalog.spi.capabilities.OfferWriterRepository;
import com.elasticpath.catalog.spi.service.CatalogService;

/**
 * An implementation of {@link OfferWriterRepository}.
 */
public class OfferCatalogCapabilityRepositoryImpl implements OfferWriterRepository {

	private final CatalogService catalogService;

	/**
	 * Constructor.
	 *
	 * @param catalogService {@link CatalogService}.
	 */
	public OfferCatalogCapabilityRepositoryImpl(final CatalogService catalogService) {
		this.catalogService = catalogService;
	}

	@Override
	public boolean write(final Offer offer) {
		return catalogService.saveOrUpdate(offer);
	}

	@Override
	public void delete(final String code) {
		catalogService.delete(OFFER_IDENTITY_TYPE, code);
	}

	@Override
	public void delete(final String code, final String store) {
		catalogService.delete(OFFER_IDENTITY_TYPE, store, code);
	}

	@Override
	public List<Offer> writeAll(final List<Offer> projections) {
		return catalogService.saveOrUpdateAll(projections);
	}

}
