/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.offer.handler;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.catalog.update.processor.capabilities.ProductUpdateProcessor;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.message.handler.EventMessageHandler;
import com.elasticpath.domain.message.handler.EventMessageHandlerHelper;
import com.elasticpath.messaging.EventMessage;

/**
 * Implementation of {@link EventMessageHandler} for {@link Product} updated event.
 */
public class ProductUpdatedEventHandler implements EventMessageHandler {

	private static final Logger LOGGER = LogManager.getLogger(ProductUpdatedEventHandler.class);

	private final EventMessageHandlerHelper<Product> productEventMessageHandlerHelper;
	private final EventMessageHandlerHelper<Set<String>> productBundleEventMessageHandlerHelper;
	private final ProductUpdateProcessor productUpdateProcessor;

	/**
	 * Constructor.
	 *
	 * @param productEventMessageHandlerHelper       helper for getting of {@link Product} from {@link org.apache.camel.Exchange}.
	 * @param productBundleEventMessageHandlerHelper helper for getting list of codes of {@link com.elasticpath.domain.catalog.ProductBundle} from
	 *                                               {@link org.apache.camel.Exchange}.
	 * @param productUpdateProcessor                 domain update service capability for processing {@link Product} update notifications.
	 */
	public ProductUpdatedEventHandler(final EventMessageHandlerHelper<Product> productEventMessageHandlerHelper,
									  final EventMessageHandlerHelper<Set<String>> productBundleEventMessageHandlerHelper,
									  final ProductUpdateProcessor productUpdateProcessor) {
		this.productEventMessageHandlerHelper = productEventMessageHandlerHelper;
		this.productBundleEventMessageHandlerHelper = productBundleEventMessageHandlerHelper;
		this.productUpdateProcessor = productUpdateProcessor;
	}

	@Override
	public void handleMessage(final EventMessage eventMessage) {
		final Product product = productEventMessageHandlerHelper.getExchangedEntity(eventMessage);
		final Set<String> bundles = productBundleEventMessageHandlerHelper.getExchangedEntity(eventMessage);

		LOGGER.debug("Processing PRODUCT_UPDATED event for Product with guid: " + product.getGuid());

		productUpdateProcessor.processProductUpdated(product, bundles.stream().toArray(String[]::new));
	}

}
