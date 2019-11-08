/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.brand.helper;

import com.elasticpath.domain.catalog.Brand;
import com.elasticpath.domain.message.handler.EventMessageHandlerHelper;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.service.catalog.BrandService;

/**
 * Implementation of {@link EventMessageHandlerHelper} for {@link Brand}.
 */
public class BrandEventMessageHandlerHelper implements EventMessageHandlerHelper<Brand> {

	private final BrandService brandService;

	/**
	 * Constructor.
	 *
	 * @param brandService {@link BrandService} data service.
	 */
	public BrandEventMessageHandlerHelper(final BrandService brandService) {
		this.brandService = brandService;
	}

	@Override
	public Brand getExchangedEntity(final EventMessage eventMessage) {
		final String guid = eventMessage.getGuid();

		return brandService.findByCode(guid);
	}
}
