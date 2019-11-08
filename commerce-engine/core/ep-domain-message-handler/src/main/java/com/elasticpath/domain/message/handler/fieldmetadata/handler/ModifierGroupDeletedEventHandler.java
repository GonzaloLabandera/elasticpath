/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.fieldmetadata.handler;

import org.apache.log4j.Logger;

import com.elasticpath.catalog.update.processor.capabilities.ModifierGroupUpdateProcessor;
import com.elasticpath.domain.message.handler.EventMessageHandler;
import com.elasticpath.messaging.EventMessage;

/**
 * Implementation of {@link EventMessageHandler} for {@link com.elasticpath.domain.modifier.ModifierGroup} deleted event.
 */
public class ModifierGroupDeletedEventHandler implements EventMessageHandler {

	private static final Logger LOGGER = Logger.getLogger(ModifierGroupDeletedEventHandler.class);

	private final ModifierGroupUpdateProcessor cartItemModifierGroupUpdateProcessor;

	/**
	 * Constructor.
	 *
	 * @param cartItemModifierGroupUpdateProcessor domain update service capability for processing
	 * {@link com.elasticpath.domain.modifier.ModifierGroup} update notifications.
	 */
	public ModifierGroupDeletedEventHandler(final ModifierGroupUpdateProcessor cartItemModifierGroupUpdateProcessor) {
		this.cartItemModifierGroupUpdateProcessor = cartItemModifierGroupUpdateProcessor;
	}

	@Override
	public void handleMessage(final EventMessage eventMessage) {
		LOGGER.debug("Processing MODIFIER_GROUP_DELETED event for ModifierGroup with guid: " + eventMessage.getGuid());

		cartItemModifierGroupUpdateProcessor.processModifierGroupDeleted(eventMessage.getGuid());
	}

}
