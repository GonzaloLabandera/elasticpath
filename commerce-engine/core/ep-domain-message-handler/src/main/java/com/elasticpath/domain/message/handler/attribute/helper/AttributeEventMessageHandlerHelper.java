/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.attribute.helper;

import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.message.handler.EventMessageHandlerHelper;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.service.attribute.AttributeService;

/**
 * Implementation of {@link EventMessageHandlerHelper} for {@link Attribute}.
 */
public class AttributeEventMessageHandlerHelper implements EventMessageHandlerHelper<Attribute> {

	private final AttributeService attributeService;

	/**
	 * Constructor for AttributeEventMessageHandlerHelper.
	 *
	 * @param attributeService {@link AttributeService} data service.
	 */
	public AttributeEventMessageHandlerHelper(final AttributeService attributeService) {
		this.attributeService = attributeService;
	}

	@Override
	public Attribute getExchangedEntity(final EventMessage eventMessage) {
		final String guid = eventMessage.getGuid();

		return attributeService.findByKey(guid);
	}
}
