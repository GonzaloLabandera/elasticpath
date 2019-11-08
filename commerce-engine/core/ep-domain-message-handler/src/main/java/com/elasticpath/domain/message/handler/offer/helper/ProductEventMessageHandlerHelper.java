/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.offer.helper;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.message.handler.EventMessageHandlerHelper;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.service.catalog.ProductLookup;

/**
 * Implementation of {@link EventMessageHandlerHelper} for {@link Product}.
 */
public class ProductEventMessageHandlerHelper implements EventMessageHandlerHelper<Product>  {

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

		return productLookup.findByGuid(guid);
	}

}
