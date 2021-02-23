/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.datapopulation.core.action.impl;

import java.io.File;
import java.util.Properties;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.elasticpath.datapopulation.core.action.DataPopulationAction;
import com.elasticpath.datapopulation.core.context.DataPopulationContext;
import com.elasticpath.datapopulation.core.context.configurer.LiquibaseActionConfiguration;
import com.elasticpath.datapopulation.core.exceptions.DatabaseUpdateActionException;

/**
 * This action sets up the resources needed by {@link RunLiquibaseActionImpl} and executes the data import changelogs.
 */
public class ImportDataActionImpl implements DataPopulationAction {

	/**
	 * The name of the property key that is used to refer to the Liquibase contexts that should be used when updating the database.
	 */
	protected static final String LIQUIBASE_CONTEXTS_PROPERTY_KEY = "liquibase.contexts";

	@Autowired
	private RunLiquibaseActionImpl invokeLiquibaseAction;

	@Autowired
	@Qualifier("liquibaseContextProperties")
	private Properties liquibaseContextProperties;

	@Autowired
	@Qualifier("rootLiquibaseChangeLogFile")
	private File liquibaseChangeLogFile;

	@Override
	public void execute(final DataPopulationContext context) {
		// Check that the filtered liquibase change log file now exists
		if (!liquibaseChangeLogFile.exists()) {
			throw new DatabaseUpdateActionException("Error: Unable to find filtered Liquibase change log file : "
					+ liquibaseChangeLogFile.getAbsolutePath());
		}

		// Get the Liquibase contexts to run with from the database update properties configured
		final String liquibaseContexts = getLiquibaseContexts(liquibaseContextProperties);

		// Finally, create the wrapper resource for InvokeLiquibaseAction consumption
		LiquibaseActionConfiguration actionConfiguration = new LiquibaseActionConfiguration();
		actionConfiguration.setLiquibaseChangelog(liquibaseChangeLogFile.getAbsolutePath());
		actionConfiguration.setLiquibaseContexts(liquibaseContexts);
		actionConfiguration.setDropFirst(false);
		actionConfiguration.setUserAdminConnections(false);
		actionConfiguration.setLiquibaseChangelogParameters(null);
		context.setActionConfiguration(actionConfiguration);

		invokeLiquibaseAction.execute(context);
	}

	@Override
	public String getDescription(final DataPopulationContext context) {
		final String liquibaseContexts = getLiquibaseContexts(liquibaseContextProperties);
		return "Updating database using Liquibase change log file '" + liquibaseChangeLogFile.getPath() + "' and contexts: " + liquibaseContexts;
	}

	/**
	 * Gets the Liquibase contexts to run with from the given {@link Properties} object.
	 * Looks up the {@link #LIQUIBASE_CONTEXTS_PROPERTY_KEY} key in the Properties object if it is exists and returns any mapped property value.
	 * Null-tolerant; if the given {@link Properties} object is null, then null is returned as the Liquibase contexts value to use.
	 *
	 * @param databaseUpdateProperties the {@link Properties} object to inspect
	 * @return the stored Liquibase contexts value, or null if no value was present or if no {@link Properties} object was given.
	 */
	private String getLiquibaseContexts(final Properties databaseUpdateProperties) {
		String result = null;

		if (MapUtils.isNotEmpty(databaseUpdateProperties)) {
			result = databaseUpdateProperties.getProperty(LIQUIBASE_CONTEXTS_PROPERTY_KEY);
		}

		return result;
	}
}
