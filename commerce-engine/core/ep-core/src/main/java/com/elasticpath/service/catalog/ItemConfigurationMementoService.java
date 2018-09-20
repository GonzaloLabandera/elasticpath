/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.service.catalog;

import com.elasticpath.domain.catalog.ItemConfigurationMemento;

/**
 * Provides CRUD services on {@link ItemConfigurationMemento}.
 */
public interface ItemConfigurationMementoService {

	/**
	 * Save.
	 *
	 * @param memento the memento to be saved
	 */
	void saveItemConfigurationMemento(ItemConfigurationMemento memento);

	/**
	 * Item configuration memento exists.
	 *
	 * @param guid the guid
	 * @return true, if successful
	 */
	boolean itemConfigurationMementoExistsByGuid(String guid);

	/**
	 * Find the item configuration memento by GUID.
	 *
	 * @param guid the GUID
	 * @return the item configuration memento
	 */
	ItemConfigurationMemento findByGuid(String guid);
}
