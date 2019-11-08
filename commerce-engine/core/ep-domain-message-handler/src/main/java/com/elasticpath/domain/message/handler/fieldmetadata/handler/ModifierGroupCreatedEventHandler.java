/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.fieldmetadata.handler;

import org.apache.log4j.Logger;

import com.elasticpath.catalog.update.processor.capabilities.ModifierGroupUpdateProcessor;
import com.elasticpath.domain.message.handler.EventMessageHandler;
import com.elasticpath.domain.message.handler.EventMessageHandlerHelper;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.messaging.EventMessage;

/**
 * Implementation of {@link EventMessageHandler} for {@link ModifierGroup} created event.
 */
public class ModifierGroupCreatedEventHandler implements EventMessageHandler {

	private static final Logger LOGGER = Logger.getLogger(ModifierGroupCreatedEventHandler.class);

	private final EventMessageHandlerHelper<ModifierGroup> eventMessageHandlerHelper;
	private final ModifierGroupUpdateProcessor cartItemModifierGroupUpdateProcessor;

	/**
	 * Constructor.
	 *
	 * @param eventMessageHandlerHelper            helper for getting of {@link ModifierGroup} from {@link org.apache.camel.Exchange}.
	 * @param cartItemModifierGroupUpdateProcessor domain update service capability for processing {@link ModifierGroup} update
	 *                                             notifications.
	 */
	public ModifierGroupCreatedEventHandler(final EventMessageHandlerHelper<ModifierGroup> eventMessageHandlerHelper,
													final ModifierGroupUpdateProcessor cartItemModifierGroupUpdateProcessor) {
		this.eventMessageHandlerHelper = eventMessageHandlerHelper;
		this.cartItemModifierGroupUpdateProcessor = cartItemModifierGroupUpdateProcessor;
	}

	@Override
	public void handleMessage(final EventMessage eventMessage) {
		final ModifierGroup modifierGroup = eventMessageHandlerHelper.getExchangedEntity(eventMessage);

		LOGGER.debug("Processing MODIFIER_GROUP_CREATED event for ModifierGroup with guid: " + modifierGroup.getGuid());

		cartItemModifierGroupUpdateProcessor.processModifierGroupCreated(modifierGroup);
	}

}
