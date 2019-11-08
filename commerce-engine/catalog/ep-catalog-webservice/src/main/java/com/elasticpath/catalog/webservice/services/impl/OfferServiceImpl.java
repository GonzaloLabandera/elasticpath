/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.webservice.services.impl;

import java.util.List;
import java.util.Optional;

import com.elasticpath.catalog.entity.offer.Offer;
import com.elasticpath.catalog.entity.offer.OfferReaderCapability;
import com.elasticpath.catalog.reader.FindAllResponse;
import com.elasticpath.catalog.reader.impl.ModifiedSinceImpl;
import com.elasticpath.catalog.reader.impl.PaginationRequestImpl;
import com.elasticpath.catalog.spi.CatalogProjectionPluginProvider;
import com.elasticpath.catalog.webservice.exception.NoReaderCapabilityMatchedException;
import com.elasticpath.catalog.webservice.services.OfferService;
import com.elasticpath.service.misc.TimeService;

/**
 * An implementation of {@link OfferService}.
 */
public class OfferServiceImpl extends ReaderServiceImpl implements OfferService {

	private final OfferReaderCapability reader;

	/**
	 * Constructor.
	 *
	 * @param provider    is provider of plugin capabilities.
	 * @param timeService the time service.
	 */
	public OfferServiceImpl(final CatalogProjectionPluginProvider provider, final TimeService timeService) {
		super(timeService);
		this.reader = provider.getCatalogProjectionPlugin()
				.getReaderCapability(OfferReaderCapability.class)
				.orElseThrow(NoReaderCapabilityMatchedException::new);
	}

	@Override
	public Optional<Offer> get(final String store, final String code) {
		return reader.get(store, code);
	}

	@Override
	public FindAllResponse<Offer> getAllOffers(final String store, final String limit, final String startAfter, final String modifiedSince,
											   final String modifiedSinceOffset) {
		validateLimit(limit);
		validateModifiedSince(modifiedSince, modifiedSinceOffset);

		return reader.findAll(store, new PaginationRequestImpl(limit, startAfter), new ModifiedSinceImpl(convertDate(modifiedSince),
				convertSinceOffset(modifiedSinceOffset)));
	}

	@Override
	public List<Offer> getLatestOffersWithCodes(final String store, final List<String> codes) {
		return reader.findAllWithCodes(store, codes);
	}
}
