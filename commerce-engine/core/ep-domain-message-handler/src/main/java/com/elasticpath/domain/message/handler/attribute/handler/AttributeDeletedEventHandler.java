/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.attribute.handler;

import org.apache.log4j.Logger;

import com.elasticpath.catalog.update.processor.capabilities.AttributeUpdateProcessor;
import com.elasticpath.domain.message.handler.EventMessageHandler;
import com.elasticpath.messaging.EventMessage;

/**
 * Implementation of {@link EventMessageHandler} for {@link com.elasticpath.domain.attribute.Attribute} deleted event.
 */
public class AttributeDeletedEventHandler implements EventMessageHandler {

	private static final Logger LOGGER = Logger.getLogger(AttributeDeletedEventHandler.class);

	private final AttributeUpdateProcessor attributeUpdateProcessor;

	/**
	 * Constructor for AttributeCreatedEventHandler.
	 *
	 * @param attributeUpdateProcessor domain update service capability for processing {@link com.elasticpath.domain.attribute.Attribute} update
	 *                                    notifications.
	 */
	public AttributeDeletedEventHandler(final AttributeUpdateProcessor attributeUpdateProcessor) {
		this.attributeUpdateProcessor = attributeUpdateProcessor;
	}

	@Override
	public void handleMessage(final EventMessage eventMessage) {
		LOGGER.debug("Processing ATTRIBUTE_DELETED event for Attribute with guid: " + eventMessage.getGuid());

		attributeUpdateProcessor.processAttributeDeleted(eventMessage.getGuid());
	}
}
