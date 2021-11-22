/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.skuoption.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.catalog.update.processor.capabilities.SkuOptionUpdateProcessor;
import com.elasticpath.domain.message.handler.EventMessageHandler;
import com.elasticpath.domain.message.handler.EventMessageHandlerHelper;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.messaging.EventMessage;

/**
 * Implementation of {@link EventMessageHandler} for {@link SkuOption} updated event.
 */
public class SkuOptionUpdatedEventHandler implements EventMessageHandler {

	private static final Logger LOGGER = LogManager.getLogger(SkuOptionUpdatedEventHandler.class);

	private final EventMessageHandlerHelper<SkuOption> eventMessageHandlerHelper;
	private final SkuOptionUpdateProcessor skuOptionUpdateProcessor;

	/**
	 * Constructor SkuOptionUpdatedEventHandler.
	 *
	 * @param eventMessageHandlerHelper helper for getting of {@link SkuOption} from {@link org.apache.camel.Exchange}.
	 * @param skuOptionUpdateProcessor  domain update service capability for processing {@link SkuOption} update notifications.
	 */
	public SkuOptionUpdatedEventHandler(final EventMessageHandlerHelper<SkuOption> eventMessageHandlerHelper,
										final SkuOptionUpdateProcessor skuOptionUpdateProcessor) {
		this.eventMessageHandlerHelper = eventMessageHandlerHelper;
		this.skuOptionUpdateProcessor = skuOptionUpdateProcessor;
	}

	@Override
	public void handleMessage(final EventMessage eventMessage) {
		final SkuOption skuOption = eventMessageHandlerHelper.getExchangedEntity(eventMessage);

		LOGGER.debug("Processing SKU_OPTION_UPDATED event for SkuOption with guid: " + skuOption.getGuid());

		skuOptionUpdateProcessor.processSkuOptionUpdated(skuOption);
	}

}
