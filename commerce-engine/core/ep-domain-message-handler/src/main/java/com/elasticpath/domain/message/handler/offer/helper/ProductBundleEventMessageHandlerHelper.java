/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.offer.helper;

import java.util.Set;

import com.elasticpath.domain.message.handler.EventMessageHandlerHelper;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.service.catalog.ProductBundleService;

/**
 * Implementation of {@link EventMessageHandlerHelper} for {@link com.elasticpath.domain.catalog.ProductBundle}.
 */
public class ProductBundleEventMessageHandlerHelper implements EventMessageHandlerHelper<Set<String>> {

	private final ProductBundleService productBundleService;

	/**
	 * Constructor.
	 *
	 * @param productBundleService {@link ProductBundleService} data service.
	 */
	public ProductBundleEventMessageHandlerHelper(final ProductBundleService productBundleService) {
		this.productBundleService = productBundleService;
	}

	@Override
	public Set<String> getExchangedEntity(final EventMessage eventMessage) {
		final String guid = eventMessage.getGuid();

		return productBundleService.findAllProductBundleCodesContainingProduct(guid);
	}

}
