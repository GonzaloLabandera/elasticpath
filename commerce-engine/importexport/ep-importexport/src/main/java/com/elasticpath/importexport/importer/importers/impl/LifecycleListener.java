/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.importexport.importer.importers.impl;

import com.elasticpath.persistence.api.Persistable;

/**
 * Interface for listener that can be added to saving strategy to inspect saving processing.
 */
public interface LifecycleListener {
	/**
	 * Calls before populate action, for both loaded and newly instantiated objects.
	 *
	 * @param persistable the persistable object that will be saved to the database
	 */
	void beforePopulate(Persistable persistable);

	/**
	 * Calls before save action, after object has already been populated.
	 *
	 * @param persistable the persistable object that will be saved to the database
	 */
	void beforeSave(Persistable persistable);

	/**
	 * Calls after save action, when object has already been saved to the database.
	 *
	 * @param persistable the persistable object that has been saved to the database
	 */
	void afterSave(Persistable persistable);

}
