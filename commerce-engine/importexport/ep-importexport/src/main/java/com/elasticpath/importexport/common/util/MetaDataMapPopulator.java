/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.importexport.common.util;

import com.elasticpath.importexport.common.exception.ConfigurationException;

/**
 * Service for populating the metadata map depending on Import/Export configuration.
 */
public interface MetaDataMapPopulator {
	/**
	 * Populate the metadata map with configuration for Import/Export import operations.
	 * @param changeSetGuid the GUID of the changeset to populate or null if changesets are not being populated
	 * @param stage the processing stage to run: "stage1" only adds objects to the specified change set, "stage2" means only import data, null
	 *              means both
	 * @throws ConfigurationException if there was a problem with the chosen options
	 */
	void configureMetadataMapForImport(String changeSetGuid, String stage) throws ConfigurationException;
}
