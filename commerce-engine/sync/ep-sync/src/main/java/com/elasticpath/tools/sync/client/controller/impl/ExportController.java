/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.tools.sync.client.controller.impl;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.tools.sync.client.SyncJobConfiguration;
import com.elasticpath.tools.sync.job.TransactionJobBuilder;

/**
 * The Export Controller, is used to read data from the source system, save to the database.
 */
public class ExportController extends FullController {

	private static final Logger LOG = LogManager.getLogger(ExportController.class);

	private DataSource dataSource;

	@Override
	protected void synchronizationCompleted(final SyncJobConfiguration syncJobConfiguration) {
		LOG.debug("Export completed");
	}

	/**
	 * Initializes the source system configuration.
	 *
	 * @param sourceSystem the source system configuration
	 * @param targetSystem the target system configuration
	 */
	@Override
	protected void initConfig(final SystemConfig sourceSystem, final SystemConfig targetSystem) {
		// only the source system is required when doing export
		if (dataSource == null) {
			sourceSystem.initSystem();
		} else {
			sourceSystem.initSystem(dataSource);
		}
	}

	/**
	 * Gets a non caching transaction job builder.
	 *
	 * @return a non caching transaction job builder.
	 */
	@Override
	protected TransactionJobBuilder getTransactionJobFromBean() {
		return getSyncBeanFactory().getSourceBean("transactionJobBuilder");
	}

}
