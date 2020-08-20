/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.importexport.api.services;

import java.io.IOException;
import java.io.InputStream;

import org.apache.camel.language.Simple;

import com.elasticpath.importexport.common.exception.ConfigurationException;

/**
 * Service for generating Import/Export exports as an InputStream.
 */
public interface ExportAPIService {
	/**
	 * Generate an InputStream containing the XML representation of the passed JobType using the passed query.
	 *
	 * @param type the JobType name to export
	 * @param parentType the optional parent JobType to use to identify which related records should be exported
	 * @param query the optional EPQL query to use when generating the type (or parentType, if present)
	 * @return an InputStream containing the XML representation of the JobType data
	 * @throws ConfigurationException if there is an issue with the exportconfiguration.xml
	 * @throws IOException if there is an issue consuming the parent JobType export
	 */
	InputStream doExport(@Simple("${header.type}") String type,
						 @Simple("${header.parentType}") String parentType,
						 @Simple("${header.query}") String query) throws ConfigurationException, IOException;
}
