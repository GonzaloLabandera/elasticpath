/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.fieldmetadata.helper;

import com.elasticpath.domain.message.handler.EventMessageHandlerHelper;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.service.modifier.ModifierService;

/**
 * Implementation of {@link EventMessageHandlerHelper} for {@link ModifierGroup}.
 */
public class ModifierGroupEventMessageHandlerHelper implements EventMessageHandlerHelper<ModifierGroup> {

	private final ModifierService cartItemModifierService;

	/**
	 * Constructor.
	 *
	 * @param cartItemModifierService {@link ModifierService} data service.
	 */
	public ModifierGroupEventMessageHandlerHelper(final ModifierService cartItemModifierService) {
		this.cartItemModifierService = cartItemModifierService;
	}

	@Override
	public ModifierGroup getExchangedEntity(final EventMessage eventMessage) {
		final String guid = eventMessage.getGuid();

		return cartItemModifierService.findModifierGroupByCode(guid);
	}

}
