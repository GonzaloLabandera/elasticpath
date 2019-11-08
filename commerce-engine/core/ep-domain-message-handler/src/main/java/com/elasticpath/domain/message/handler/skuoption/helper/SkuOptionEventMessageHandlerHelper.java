/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.skuoption.helper;

import com.elasticpath.domain.message.handler.EventMessageHandlerHelper;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.service.catalog.SkuOptionService;

/**
 * Implementation of {@link EventMessageHandlerHelper} for {@link SkuOption}.
 */
public class SkuOptionEventMessageHandlerHelper implements EventMessageHandlerHelper<SkuOption> {

	private final SkuOptionService skuOptionService;

	/**
	 * Constructor.
	 *
	 * @param skuOptionService {@link SkuOptionService} data service.
	 */
	public SkuOptionEventMessageHandlerHelper(final SkuOptionService skuOptionService) {
		this.skuOptionService = skuOptionService;
	}

	@Override
	public SkuOption getExchangedEntity(final EventMessage eventMessage) {
		final String guid = eventMessage.getGuid();

		return skuOptionService.findByKey(guid);
	}

}
