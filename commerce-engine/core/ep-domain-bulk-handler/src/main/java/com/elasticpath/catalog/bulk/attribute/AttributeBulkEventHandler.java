/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.bulk.attribute;

import java.util.List;

import com.elasticpath.catalog.bulk.BulkEventHandler;
import com.elasticpath.messaging.EventMessage;

/**
 * An implementation of {@link BulkEventHandler} for processing ATTRIBUTE_BULK_UPDATE event.
 */
public class AttributeBulkEventHandler implements BulkEventHandler {
	/**
	 * Name of field in Attribute bulk update event which contains list of products required for update.
	 */
	public static final String PRODUCTS = "products";
	private final AttributeBulkUpdateProcessor attributeBulkUpdateProcessor;

	/**
	 * Constructor.
	 *
	 * @param attributeBulkUpdateProcessor attributeBulkUpdateProcessor.
	 */
	public AttributeBulkEventHandler(final AttributeBulkUpdateProcessor attributeBulkUpdateProcessor) {
		this.attributeBulkUpdateProcessor = attributeBulkUpdateProcessor;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void handleBulkEvent(final EventMessage eventMessage) {
		final String attribute = eventMessage.getGuid();

		final List<String> offers = (List<String>) eventMessage.getData().get(PRODUCTS);

		attributeBulkUpdateProcessor.updateAttributeDisplayNameInOffers(offers, attribute);
	}
}
