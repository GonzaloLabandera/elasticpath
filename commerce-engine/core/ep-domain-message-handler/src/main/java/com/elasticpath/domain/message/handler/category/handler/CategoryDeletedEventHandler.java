/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.category.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.catalog.update.processor.capabilities.CategoryUpdateProcessor;
import com.elasticpath.domain.message.handler.EventMessageHandler;
import com.elasticpath.domain.message.handler.category.helper.LinkedCategoryEventMessageHelper;
import com.elasticpath.messaging.EventMessage;

/**
 * Implementation of {@link EventMessageHandler} for Category deleted event.
 */
public class CategoryDeletedEventHandler implements EventMessageHandler {

	private static final Logger LOGGER = LogManager.getLogger(CategoryDeletedEventHandler.class);

	private final CategoryUpdateProcessor categoryUpdateProcessor;
	private final LinkedCategoryEventMessageHelper linkedCategoryEventMessageHelper;

	/**
	 * Constructor.
	 *
	 * @param categoryUpdateProcessor          domain update service capability for processing Category update notifications.
	 * @param linkedCategoryEventMessageHelper linked category helper.
	 */
	public CategoryDeletedEventHandler(final CategoryUpdateProcessor categoryUpdateProcessor,
									   final LinkedCategoryEventMessageHelper linkedCategoryEventMessageHelper) {
		this.categoryUpdateProcessor = categoryUpdateProcessor;
		this.linkedCategoryEventMessageHelper = linkedCategoryEventMessageHelper;
	}

	@Override
	public void handleMessage(final EventMessage eventMessage) {
		LOGGER.debug("Processing CATEGORY_DELETED event for Category with guid: " + eventMessage.getGuid());

		final String categoryCode = linkedCategoryEventMessageHelper.getUnlinkedCategoryCode(eventMessage);

		categoryUpdateProcessor.processCategoryDeleted(categoryCode);
	}

}
