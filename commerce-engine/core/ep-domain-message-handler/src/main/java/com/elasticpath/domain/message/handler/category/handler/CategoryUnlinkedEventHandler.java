/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.message.handler.category.handler;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.catalog.update.processor.capabilities.CategoryUpdateProcessor;
import com.elasticpath.domain.message.handler.EventMessageHandler;
import com.elasticpath.domain.message.handler.category.helper.LinkedCategoryEventMessageHelper;
import com.elasticpath.messaging.EventMessage;

/**
 * Implementation of {@link EventMessageHandler} for Category unlinked event.
 */
public class CategoryUnlinkedEventHandler implements EventMessageHandler {
	private static final Logger LOGGER = LogManager.getLogger(CategoryUnlinkedEventHandler.class);

	private final CategoryUpdateProcessor categoryUpdateProcessor;
	private final LinkedCategoryEventMessageHelper linkedCategoryEventMessageHelper;

	/**
	 * Constructor.
	 *
	 * @param categoryUpdateProcessor          domain update service capability for processing Category update notifications.
	 * @param linkedCategoryEventMessageHelper linked category helper.
	 */
	public CategoryUnlinkedEventHandler(final CategoryUpdateProcessor categoryUpdateProcessor,
										final LinkedCategoryEventMessageHelper linkedCategoryEventMessageHelper) {
		this.categoryUpdateProcessor = categoryUpdateProcessor;
		this.linkedCategoryEventMessageHelper = linkedCategoryEventMessageHelper;
	}

	@Override
	public void handleMessage(final EventMessage eventMessage) {
		LOGGER.debug("Processing CATEGORY_UNLINKED event for Category with compound guid: " + eventMessage.getGuid());

		final String categoryCode = linkedCategoryEventMessageHelper.getUnlinkedCategoryCode(eventMessage);
		final List<String> stores = linkedCategoryEventMessageHelper.getUnlinkedCategoryStores(eventMessage);

		categoryUpdateProcessor.processCategoryUnlinked(categoryCode, stores);
	}
}
