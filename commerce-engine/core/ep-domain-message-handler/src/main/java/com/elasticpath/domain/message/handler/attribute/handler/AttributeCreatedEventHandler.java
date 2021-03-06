/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.attribute.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.catalog.update.processor.capabilities.AttributeUpdateProcessor;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.message.handler.EventMessageHandler;
import com.elasticpath.domain.message.handler.EventMessageHandlerHelper;
import com.elasticpath.messaging.EventMessage;

/**
 * Implementation of {@link EventMessageHandler} for {@link Attribute} created event.
 */
public class AttributeCreatedEventHandler implements EventMessageHandler {

	private static final Logger LOGGER = LogManager.getLogger(AttributeCreatedEventHandler.class);

	private final EventMessageHandlerHelper<Attribute> eventMessageHandlerHelper;
	private final AttributeUpdateProcessor attributeUpdateProcessor;

	/**
	 * Constructor for AttributeCreatedEventHandler.
	 *
	 * @param eventMessageHandlerHelper helper for getting of {@link Attribute} from {@link org.apache.camel.Exchange}.
	 * @param attributeUpdateProcessor  domain update service capability for processing {@link Attribute} update notifications.
	 */
	public AttributeCreatedEventHandler(final EventMessageHandlerHelper<Attribute> eventMessageHandlerHelper,
										final AttributeUpdateProcessor attributeUpdateProcessor) {
		this.eventMessageHandlerHelper = eventMessageHandlerHelper;
		this.attributeUpdateProcessor = attributeUpdateProcessor;
	}

	@Override
	public void handleMessage(final EventMessage eventMessage) {
		final Attribute attribute = eventMessageHandlerHelper.getExchangedEntity(eventMessage);

		LOGGER.debug("Processing ATTRIBUTE_CREATED event for Attribute with guid: " + attribute.getGuid());

		attributeUpdateProcessor.processAttributeCreated(attribute);
	}
}
