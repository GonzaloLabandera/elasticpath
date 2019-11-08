/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.skuoption.handler;

import org.apache.log4j.Logger;

import com.elasticpath.catalog.update.processor.capabilities.SkuOptionUpdateProcessor;
import com.elasticpath.domain.message.handler.EventMessageHandler;
import com.elasticpath.domain.message.handler.EventMessageHandlerHelper;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.messaging.EventMessage;

/**
 * Implementation of {@link EventMessageHandler} for {@link SkuOption} created event.
 */
public class SkuOptionCreatedEventHandler implements EventMessageHandler {

	private static final Logger LOGGER = Logger.getLogger(SkuOptionCreatedEventHandler.class);

	private final EventMessageHandlerHelper<SkuOption> eventMessageHandlerHelper;
	private final SkuOptionUpdateProcessor skuOptionUpdateProcessor;

	/**
	 * Constructor SkuOptionCreatedEventHandler.
	 *
	 * @param eventMessageHandlerHelper helper for getting of {@link SkuOption} from {@link org.apache.camel.Exchange}.
	 * @param skuOptionUpdateProcessor  domain update service capability for processing {@link SkuOption} update notifications.
	 */
	public SkuOptionCreatedEventHandler(final EventMessageHandlerHelper<SkuOption> eventMessageHandlerHelper,
										final SkuOptionUpdateProcessor skuOptionUpdateProcessor) {
		this.eventMessageHandlerHelper = eventMessageHandlerHelper;
		this.skuOptionUpdateProcessor = skuOptionUpdateProcessor;
	}

	@Override
	public void handleMessage(final EventMessage eventMessage) {
		final SkuOption skuOption = eventMessageHandlerHelper.getExchangedEntity(eventMessage);

		LOGGER.debug("Processing SKU_OPTION_CREATED event for SkuOption with guid: " + skuOption.getGuid());

		skuOptionUpdateProcessor.processSkuOptionCreated(skuOption);
	}

}
