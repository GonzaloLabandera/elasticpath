/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.bulk.offer;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.OFFER_IDENTITY_TYPE;

import java.util.ArrayList;
import java.util.List;

import com.elasticpath.catalog.entity.offer.Offer;
import com.elasticpath.catalog.spi.service.CatalogService;
import com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.Converter;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.catalog.ProductLookup;
import com.elasticpath.service.store.StoreService;

/**
 * An implementation of {@link OfferBulkUpdateProcessor}.
 */
public class OfferBulkUpdateProcessorImpl implements OfferBulkUpdateProcessor {

	private final CatalogService catalogService;
	private final ProductLookup productLookup;
	private final StoreService storeService;
	private final Converter<Product, Offer> converter;

	/**
	 * Constructor.
	 *
	 * @param catalogService data service for Catalog of projections.
	 * @param productLookup  {@link ProductLookup} data service.
	 * @param storeService   the storeService to set
	 * @param converter      converter for create Offer projection from Product.
	 */
	public OfferBulkUpdateProcessorImpl(final CatalogService catalogService, final ProductLookup productLookup, final StoreService storeService,
										final Converter<Product, Offer> converter) {
		this.catalogService = catalogService;
		this.productLookup = productLookup;
		this.storeService = storeService;
		this.converter = converter;
	}

	@Override
	public void updateOffers(final List<String> offerCodes) {
		final List<Offer> offerProjections = catalogService.readAll(OFFER_IDENTITY_TYPE, offerCodes);

		final List<Offer> updatedOffers = new ArrayList<>();
		final List<Offer> removedOffers = new ArrayList<>();

		for (final Offer offer : offerProjections) {
			final Product product = productLookup.findByGuid(offer.getIdentity().getCode());
			final Store store = storeService.findStoreWithCode(offer.getIdentity().getStore());
			final Offer updatedOffer = converter.convert(product, store, store.getCatalog());

			if (updatedOffer.isDeleted()) {
				if (!offer.isDeleted()) {
					removedOffers.add(updatedOffer);
				}
			} else {
				updatedOffers.add(updatedOffer);
			}
		}

		catalogService.saveOrUpdateAll(updatedOffers);
		catalogService.deleteAll(removedOffers);
	}

}
