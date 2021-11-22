/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.message.handler.category.handler;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.catalog.update.processor.capabilities.CategoryUpdateProcessor;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.message.handler.EventMessageHandler;
import com.elasticpath.domain.message.handler.EventMessageHandlerHelper;
import com.elasticpath.domain.message.handler.category.helper.LinkedCategoryEventMessageHelper;
import com.elasticpath.domain.message.handler.exception.EventMessageProcessingException;
import com.elasticpath.messaging.EventMessage;

/**
 * Implementation of {@link EventMessageHandler} for {@link Category} linked event.
 */
public class LinkedCategoryUpdatedEventHandler implements EventMessageHandler {

	private static final Logger LOGGER = LogManager.getLogger(LinkedCategoryUpdatedEventHandler.class);

	private final EventMessageHandlerHelper<Category> eventMessageHandlerHelper;
	private final LinkedCategoryEventMessageHelper linkedCategoryEventMessageHelper;
	private final CategoryUpdateProcessor categoryUpdateProcessor;

	/**
	 * Constructor.
	 *
	 * @param eventMessageHandlerHelper        helper for getting of {@link Category} from Exchange.
	 * @param linkedCategoryEventMessageHelper linked category helper.
	 * @param categoryUpdateProcessor          domain update service capability for processing {@link Category} update notifications.
	 */
	public LinkedCategoryUpdatedEventHandler(final EventMessageHandlerHelper<Category> eventMessageHandlerHelper,
											 final LinkedCategoryEventMessageHelper linkedCategoryEventMessageHelper,
											 final CategoryUpdateProcessor categoryUpdateProcessor) {
		this.eventMessageHandlerHelper = eventMessageHandlerHelper;
		this.linkedCategoryEventMessageHelper = linkedCategoryEventMessageHelper;
		this.categoryUpdateProcessor = categoryUpdateProcessor;
	}

	@Override
	public void handleMessage(final EventMessage eventMessage) {
		final Category category = Optional.ofNullable(eventMessageHandlerHelper.getExchangedEntity(eventMessage))
				.orElseThrow(() -> new EventMessageProcessingException("Linked Category does not exist with guid: " + eventMessage.getGuid()));
		final List<String> stores = linkedCategoryEventMessageHelper.getUnlinkedCategoryStores(eventMessage);

		LOGGER.debug("Processing CATEGORY_LINKED_UPDATED event for Category with compound guid: " + category.getGuid());

		categoryUpdateProcessor.processCategoryIncludedExcluded(category, stores);
	}
}
