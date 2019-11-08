/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.offer.handler;

import org.apache.log4j.Logger;

import com.elasticpath.catalog.update.processor.capabilities.ProductUpdateProcessor;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.message.handler.EventMessageHandler;
import com.elasticpath.domain.message.handler.EventMessageHandlerHelper;
import com.elasticpath.messaging.EventMessage;

/**
 * Implementation of {@link EventMessageHandler} for {@link Product} created event.
 */
public class ProductCreatedEventHandler implements EventMessageHandler {

	private static final Logger LOGGER = Logger.getLogger(ProductCreatedEventHandler.class);

	private final EventMessageHandlerHelper<Product> eventMessageHandlerHelper;
	private final ProductUpdateProcessor productUpdateProcessor;

	/**
	 * Constructor.
	 *
	 * @param eventMessageHandlerHelper helper for getting of {@link Product} from {@link org.apache.camel.Exchange}.
	 * @param productUpdateProcessor    domain update service capability for processing {@link Product} update notifications.
	 */
	public ProductCreatedEventHandler(final EventMessageHandlerHelper<Product> eventMessageHandlerHelper,
									  final ProductUpdateProcessor productUpdateProcessor) {
		this.eventMessageHandlerHelper = eventMessageHandlerHelper;
		this.productUpdateProcessor = productUpdateProcessor;
	}

	@Override
	public void handleMessage(final EventMessage eventMessage) {
		final Product product = eventMessageHandlerHelper.getExchangedEntity(eventMessage);

		LOGGER.debug("Processing PRODUCT_CREATED event for Product with guid: " + product.getGuid());

		productUpdateProcessor.processProductCreated(product);
	}

}
