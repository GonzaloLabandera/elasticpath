/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.bulk.option;

import static com.elasticpath.catalog.update.processor.connectivity.impl.SkuOptionUpdateProcessorImpl.PRODUCTS;

import java.util.List;

import com.elasticpath.catalog.bulk.BulkEventHandler;
import com.elasticpath.messaging.EventMessage;

/**
 * An implementation of {@link BulkEventHandler} for processing OPTION_BULK_UPDATE event.
 */
public class SkuOptionBulkEventHandler implements BulkEventHandler {

	private final SkuOptionBulkUpdateProcessor skuOptionBulkUpdateProcessor;

	/**
	 * Constructor.
	 *
	 * @param skuOptionBulkUpdateProcessor skuOptionBulkUpdateProcessor.
	 */
	public SkuOptionBulkEventHandler(final SkuOptionBulkUpdateProcessor skuOptionBulkUpdateProcessor) {
		this.skuOptionBulkUpdateProcessor = skuOptionBulkUpdateProcessor;
	}


	@Override
	@SuppressWarnings("unchecked")
	public void handleBulkEvent(final EventMessage eventMessage) {
		final String skuOption = eventMessage.getGuid();

		final List<String> offers = (List<String>) eventMessage.getData().get(PRODUCTS);

		skuOptionBulkUpdateProcessor.updateSkuOptionDisplayNamesInOffers(offers, skuOption);
	}

}