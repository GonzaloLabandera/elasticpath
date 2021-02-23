/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.datapopulation.core.action;

import com.elasticpath.datapopulation.core.context.DataPopulationContext;

/**
 * Represents an action to be executed in the data population pipeline.
 */
public interface DataPopulationAction {

	/**
	 * Executes the data population action given the context.
	 *
	 * @param context the context
	 */
	void execute(DataPopulationContext context);

	/**
	 * Get a description of the action that will be performed.
	 *
	 * @param context the context
	 * @return description of the action that will be performed
	 */
	String getDescription(DataPopulationContext context);
}
