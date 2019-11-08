/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.skuoption.handler;

import org.apache.log4j.Logger;

import com.elasticpath.catalog.update.processor.capabilities.SkuOptionUpdateProcessor;
import com.elasticpath.domain.message.handler.EventMessageHandler;
import com.elasticpath.messaging.EventMessage;

/**
 * Implementation of {@link EventMessageHandler} for {@link com.elasticpath.domain.skuconfiguration.SkuOption} deleted event.
 */
public class SkuOptionDeletedEventHandler implements EventMessageHandler {

	private static final Logger LOGGER = Logger.getLogger(SkuOptionDeletedEventHandler.class);

	private final SkuOptionUpdateProcessor skuOptionUpdateProcessor;

	/**
	 * Constructor SkuOptionDeletedEventHandler.
	 *
	 * @param skuOptionUpdateProcessor domain update service capability for processing {@link com.elasticpath.domain.skuconfiguration.SkuOption}
	 *                                   update notifications.
	 */
	public SkuOptionDeletedEventHandler(final SkuOptionUpdateProcessor skuOptionUpdateProcessor) {
		this.skuOptionUpdateProcessor = skuOptionUpdateProcessor;
	}

	@Override
	public void handleMessage(final EventMessage eventMessage) {
		LOGGER.debug("Processing SKU_OPTION_DELETED event for SkuOption with guid: " + eventMessage.getGuid());

		skuOptionUpdateProcessor.processSkuOptionDeleted(eventMessage.getGuid());
	}

}
