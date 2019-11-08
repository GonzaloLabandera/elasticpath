/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.brand.handler;

import org.apache.log4j.Logger;

import com.elasticpath.catalog.update.processor.capabilities.BrandUpdateProcessor;
import com.elasticpath.domain.message.handler.EventMessageHandler;
import com.elasticpath.messaging.EventMessage;

/**
 * Implementation of {@link EventMessageHandler} for {@link com.elasticpath.domain.catalog.Brand} deleted event.
 */
public class BrandDeletedEventHandler implements EventMessageHandler {

	private static final Logger LOGGER = Logger.getLogger(BrandDeletedEventHandler.class);

	private final BrandUpdateProcessor brandUpdateProcessor;

	/**
	 * Constructor BrandDeletedEventHandler.
	 *
	 * @param brandUpdateProcessor domain update service capability for processing {@link com.elasticpath.domain.catalog.Brand} update notifications.
	 */
	public BrandDeletedEventHandler(final BrandUpdateProcessor brandUpdateProcessor) {
		this.brandUpdateProcessor = brandUpdateProcessor;
	}

	@Override
	public void handleMessage(final EventMessage eventMessage) {
		LOGGER.debug("Processing BRAND_DELETED event for Brand with guid: " + eventMessage.getGuid());

		brandUpdateProcessor.processBrandDeleted(eventMessage.getGuid());
	}

}
