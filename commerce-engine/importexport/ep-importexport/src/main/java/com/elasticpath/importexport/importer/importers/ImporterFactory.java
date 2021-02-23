/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.importexport.importer.importers;

import com.elasticpath.common.dto.Dto;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.importexport.importer.context.ImportContext;
import com.elasticpath.importexport.importer.importers.impl.SavingManager;
import com.elasticpath.persistence.api.Persistable;

/**
 * Interface for importer factory.
 */
public interface ImporterFactory {

	/**
	 * Creates appropriate importer based on tagName.
	 *
	 * @param rootName      the name of root element
	 * @param context       the import context for importer initialization
	 * @param savingManager the saving manager for importer
	 * @return the importer
	 * @throws ConfigurationException if could not create Importer
	 */
	@SuppressWarnings("squid:S1452")
	Importer<? super Persistable, ? super Dto> createImporter(String rootName, ImportContext context,
															  SavingManager<? extends Persistable> savingManager) throws ConfigurationException;

}
