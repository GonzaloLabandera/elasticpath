/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.capabilities;

import com.elasticpath.catalog.update.processor.CatalogUpdateProcessorCapability;
import com.elasticpath.domain.modifier.ModifierGroup;

/**
 * Catalog Update Service capability for processing {@link ModifierGroup} update notifications.
 */
public interface ModifierGroupUpdateProcessor extends CatalogUpdateProcessorCapability {

	/**
	 * Process a {@link ModifierGroup} creation event.
	 *
	 * @param modifierGroup the  Modifier Group that was created
	 */
	void processModifierGroupCreated(ModifierGroup modifierGroup);

	/**
	 * Process a {@link ModifierGroup} update event.
	 *
	 * @param modifierGroup the Group that was updated
	 */
	void processModifierGroupUpdated(ModifierGroup modifierGroup);

	/**
	 * Process a {@link ModifierGroup} deletion event.
	 *
	 * @param guid the Cart Item Modifier Group that was deleted
	 */
	void processModifierGroupDeleted(String guid);

}
