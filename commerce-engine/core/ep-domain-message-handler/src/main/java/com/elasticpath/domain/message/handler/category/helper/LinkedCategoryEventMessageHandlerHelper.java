/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.category.helper;

import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.message.handler.EventMessageHandlerHelper;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.service.catalog.CategoryLookup;

/**
 * Implementation of {@link EventMessageHandlerHelper} for {@link com.elasticpath.domain.catalog.impl.LinkedCategoryImpl}.
 */
public class LinkedCategoryEventMessageHandlerHelper implements EventMessageHandlerHelper<Category> {

	private final CategoryLookup categoryLookup;

	/**
	 * Constructor.
	 *
	 * @param categoryLookup {@link CategoryLookup} data service.
	 */
	public LinkedCategoryEventMessageHandlerHelper(final CategoryLookup categoryLookup) {
		this.categoryLookup = categoryLookup;
	}

	@Override
	public Category getExchangedEntity(final EventMessage eventMessage) {
		final String guid = eventMessage.getGuid();

		return categoryLookup.findByCompoundCategoryAndCatalogCodes(guid);
	}
}
