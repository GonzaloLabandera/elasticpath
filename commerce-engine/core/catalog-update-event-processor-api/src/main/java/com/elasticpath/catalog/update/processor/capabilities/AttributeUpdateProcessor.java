/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.capabilities;

import com.elasticpath.catalog.update.processor.CatalogUpdateProcessorCapability;
import com.elasticpath.domain.attribute.Attribute;

/**
 * Catalog Update Service capability for processing {@link Attribute} update notifications.
 */
public interface AttributeUpdateProcessor extends CatalogUpdateProcessorCapability {

	/**
	 * Process an {@link Attribute} creation event.
	 *
	 * @param attribute the Attribute that was created
	 */
	void processAttributeCreated(Attribute attribute);

	/**
	 * Process an {@link Attribute} update event.
	 *
	 * @param attribute the Attribute that was updated
	 */
	void processAttributeUpdated(Attribute attribute);

	/**
	 * Process an {@link Attribute} deletion event.
	 *
	 * @param guid the guid of Attribute that was deleted
	 */
	void processAttributeDeleted(String guid);

}
