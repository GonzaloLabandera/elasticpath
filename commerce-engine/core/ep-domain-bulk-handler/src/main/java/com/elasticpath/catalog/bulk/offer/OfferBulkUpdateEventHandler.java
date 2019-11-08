/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.bulk.offer;

import static com.elasticpath.catalog.update.processor.connectivity.impl.ProductUpdateProcessorImpl.PRODUCTS;

import java.util.List;

import com.elasticpath.catalog.bulk.BulkEventHandler;
import com.elasticpath.messaging.EventMessage;

/**
 * An implementation of {@link BulkEventHandler} for processing OFFER_BULK_UPDATE and CATEGORY_BULK_UPDATE event to update offers.
 */
public class OfferBulkUpdateEventHandler implements BulkEventHandler {

	private final OfferBulkUpdateProcessor offerBulkUpdateProcessor;

	/**
	 * Constructor.
	 *
	 * @param offerBulkUpdateProcessor offer bulk update processor.
	 */
	public OfferBulkUpdateEventHandler(final OfferBulkUpdateProcessor offerBulkUpdateProcessor) {
		this.offerBulkUpdateProcessor = offerBulkUpdateProcessor;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void handleBulkEvent(final EventMessage eventMessage) {
		final List<String> offers = (List<String>) eventMessage.getData().get(PRODUCTS);

		offerBulkUpdateProcessor.updateOffers(offers);
	}

}
