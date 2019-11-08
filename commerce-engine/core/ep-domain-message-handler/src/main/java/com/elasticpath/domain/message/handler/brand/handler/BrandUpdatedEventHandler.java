/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.brand.handler;

import org.apache.log4j.Logger;

import com.elasticpath.catalog.update.processor.capabilities.BrandUpdateProcessor;
import com.elasticpath.domain.catalog.Brand;
import com.elasticpath.domain.message.handler.EventMessageHandler;
import com.elasticpath.domain.message.handler.EventMessageHandlerHelper;
import com.elasticpath.messaging.EventMessage;

/**
 * Implementation of {@link EventMessageHandler} for {@link Brand} updated event.
 */
public class BrandUpdatedEventHandler implements EventMessageHandler {

	private static final Logger LOGGER = Logger.getLogger(BrandUpdatedEventHandler.class);

	private final EventMessageHandlerHelper<Brand> eventMessageHandlerHelper;
	private final BrandUpdateProcessor brandUpdateProcessor;

	/**
	 * Constructor BrandUpdatedEventHandler.
	 *
	 * @param eventMessageHandlerHelper helper for getting of {@link Brand} from {@link org.apache.camel.Exchange}.
	 * @param brandUpdateProcessor      domain update service capability for processing {@link Brand} update notifications.
	 */
	public BrandUpdatedEventHandler(final EventMessageHandlerHelper<Brand> eventMessageHandlerHelper,
									final BrandUpdateProcessor brandUpdateProcessor) {
		this.eventMessageHandlerHelper = eventMessageHandlerHelper;
		this.brandUpdateProcessor = brandUpdateProcessor;
	}

	@Override
	public void handleMessage(final EventMessage eventMessage) {
		final Brand brand = eventMessageHandlerHelper.getExchangedEntity(eventMessage);

		LOGGER.debug("Processing BRAND_UPDATED event for brand with guid: " + brand.getGuid());

		brandUpdateProcessor.processBrandUpdated(brand);
	}

}
