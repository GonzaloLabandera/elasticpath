/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.bulk.brand;

import static com.elasticpath.catalog.update.processor.connectivity.impl.BrandUpdateProcessorImpl.PRODUCTS;

import java.util.List;

import com.elasticpath.catalog.bulk.BulkEventHandler;
import com.elasticpath.messaging.EventMessage;

/**
 * An implementation of {@link BulkEventHandler} for processing BRAND_BULK_UPDATE event.
 */
public class BrandBulkUpdateEventHandler implements BulkEventHandler {

	private final BrandBulkUpdateProcessor brandBulkUpdateProcessor;

	/**
	 * Constructor.
	 *
	 * @param brandBulkUpdateProcessor brandBulkUpdateProcessor.
	 */
	public BrandBulkUpdateEventHandler(final BrandBulkUpdateProcessor brandBulkUpdateProcessor) {
		this.brandBulkUpdateProcessor = brandBulkUpdateProcessor;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void handleBulkEvent(final EventMessage eventMessage) {
		final String brand = eventMessage.getGuid();

		final List<String> offers = (List<String>) eventMessage.getData().get(PRODUCTS);

		brandBulkUpdateProcessor.updateBrandDisplayNamesInOffers(offers, brand);
	}

}
