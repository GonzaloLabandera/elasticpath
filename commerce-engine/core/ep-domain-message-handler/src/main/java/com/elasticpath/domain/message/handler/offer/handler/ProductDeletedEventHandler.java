/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.offer.handler;

import org.apache.log4j.Logger;

import com.elasticpath.catalog.update.processor.capabilities.ProductUpdateProcessor;
import com.elasticpath.domain.message.handler.EventMessageHandler;
import com.elasticpath.messaging.EventMessage;

/**
 * Implementation of {@link EventMessageHandler} for {@link com.elasticpath.domain.catalog.Product} deleted event.
 */
public class ProductDeletedEventHandler implements EventMessageHandler {

	private static final Logger LOGGER = Logger.getLogger(ProductDeletedEventHandler.class);

	private final ProductUpdateProcessor productUpdateProcessor;

	/**
	 * Constructor.
	 *
	 * @param productUpdateProcessor domain update service capability for processing {@link com.elasticpath.domain.catalog.Product} update
	 *                                  notifications.
	 */
	public ProductDeletedEventHandler(final ProductUpdateProcessor productUpdateProcessor) {
		this.productUpdateProcessor = productUpdateProcessor;
	}

	@Override
	public void handleMessage(final EventMessage eventMessage) {
		LOGGER.debug("Processing PRODUCT_DELETED event for Product with guid: " + eventMessage.getGuid());

		productUpdateProcessor.processProductDeleted(eventMessage.getGuid());
	}

}
