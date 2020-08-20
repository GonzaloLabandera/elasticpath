/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.importexport.api.services;

import java.io.InputStream;

import org.apache.camel.language.Simple;

import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.importexport.common.summary.Summary;

/**
 * Service for consuming Import/Export inports as an InputStream.
 */
public interface ImportAPIService {
	/**
	 * Take the passed InputStream containing the XML representation and persist it to the database.
	 * @param inputStream contains the XML representation of the object(s) to persist
	 * @param changesetGuid the guid of a changeset to import the changes into
	 * @return a summary object containing details of success or failures
	 * @throws ConfigurationException if there is an issue with the importconfiguration.xml
	 */
	Summary doImport(@Simple("${body}") InputStream inputStream, @Simple("${header.changesetGuid}") String changesetGuid)
			throws ConfigurationException;
}
