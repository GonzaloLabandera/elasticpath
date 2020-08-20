/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.offer.helper;

import org.apache.log4j.Logger;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.message.handler.EventMessageHandlerHelper;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.service.catalog.ProductLookup;

/**
 * Implementation of {@link EventMessageHandlerHelper} for {@link Product}.
 */
public class ProductEventMessageHandlerHelper implements EventMessageHandlerHelper<Product>  {

	private static final Logger LOGGER = Logger.getLogger(ProductEventMessageHandlerHelper.class);

	private final ProductLookup productLookup;

	/**
	 * Constructor.
	 *
	 * @param productLookup {@link ProductLookup} data service.
	 */
	public ProductEventMessageHandlerHelper(final ProductLookup productLookup) {
		this.productLookup = productLookup;
	}

	@Override
	public Product getExchangedEntity(final EventMessage eventMessage) {

		final String guid = eventMessage.getGuid();

		Product product = productLookup.findByGuid(guid);

		LOGGER.debug("ProductLookup return product " + product + " for processing PRODUCT_CREATED event.");
		return product;
	}
}