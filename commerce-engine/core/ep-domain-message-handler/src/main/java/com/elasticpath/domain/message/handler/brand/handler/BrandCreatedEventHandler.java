/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.brand.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.catalog.update.processor.capabilities.BrandUpdateProcessor;
import com.elasticpath.domain.catalog.Brand;
import com.elasticpath.domain.message.handler.EventMessageHandler;
import com.elasticpath.domain.message.handler.EventMessageHandlerHelper;
import com.elasticpath.messaging.EventMessage;

/**
 * Implementation of {@link EventMessageHandler} for {@link Brand} created event.
 */
public class BrandCreatedEventHandler implements EventMessageHandler {

	private static final Logger LOGGER = LogManager.getLogger(BrandCreatedEventHandler.class);

	private final EventMessageHandlerHelper<Brand> eventMessageHandlerHelper;
	private final BrandUpdateProcessor brandUpdateProcessor;

	/**
	 * Constructor BrandCreatedEventHandler.
	 *
	 * @param eventMessageHandlerHelper helper for getting of {@link Brand} from {@link org.apache.camel.Exchange}.
	 * @param brandUpdateProcessor      domain update service capability for processing {@link Brand} update notifications.
	 */
	public BrandCreatedEventHandler(final EventMessageHandlerHelper<Brand> eventMessageHandlerHelper,
									final BrandUpdateProcessor brandUpdateProcessor) {
		this.eventMessageHandlerHelper = eventMessageHandlerHelper;
		this.brandUpdateProcessor = brandUpdateProcessor;
	}

	@Override
	public void handleMessage(final EventMessage eventMessage) {
		final Brand brand = eventMessageHandlerHelper.getExchangedEntity(eventMessage);

		LOGGER.debug("Processing BRAND_CREATED event for Brand with guid: " + brand.getGuid());

		brandUpdateProcessor.processBrandCreated(brand);
	}

}
