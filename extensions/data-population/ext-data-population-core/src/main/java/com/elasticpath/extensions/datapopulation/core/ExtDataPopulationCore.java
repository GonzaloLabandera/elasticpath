package com.elasticpath.extensions.datapopulation.core;

import java.util.Map;

import com.elasticpath.repo.datapopulation.core.DataPopulationCore;
import com.elasticpath.repo.datapopulation.core.action.DataPopulationAction;
import com.elasticpath.repo.datapopulation.core.context.DataPopulationContext;
import com.elasticpath.repo.datapopulation.core.exceptions.DataPopulationActionException;

/**
 * The data population core contains all the action executors for each high level command (reset-db, update-db and filter-data).
 */
public class ExtDataPopulationCore extends DataPopulationCore {

	private DataPopulationContext dataPopulationContext;
	private Map<String, DataPopulationAction> dataPopulationActions;

	/**
	 * Runs the high level command given as the commandKey.
	 * @param commandKey the command
	 */
    @Override
	public void runActionExecutor(final String commandKey) {
        super.runActionExecutor(commandKey);
	}
}
