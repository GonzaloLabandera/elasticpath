/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.datapopulation.core.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fusesource.jansi.Ansi;

import com.elasticpath.datapopulation.core.action.DataPopulationAction;
import com.elasticpath.datapopulation.core.context.DataPopulationContext;

/**
 * Utility methods for logging progress of the data population process.
 */
public final class DataPopulationReactorLogger {
	private static final Logger LOG = LogManager.getLogger(DataPopulationReactorLogger.class);
	private static final int WIDTH = 100;
	private static final String EXECUTION_LOG_TITLE = "Elastic Path Data Population";
	private static final int SEPARATOR_LENGTH = 2;
	private static final int SEPARATOR_COUNT = 3; // " <", "> ", ": "

	/**
	 * Private constructor to prevent instantiation.
	 */
	private DataPopulationReactorLogger() {
	}

	/**
	 * Generate log statements before executing the data population action.
	 *
	 * @param context the data population context
	 * @param action the data population action
	 */
	public static void logExecuteAction(final DataPopulationContext context, final DataPopulationAction action) {
		if (action.getDescription(context) != null) {
			LOG.info(Ansi.ansi()
					.bold()
					.a(StringUtils.repeat("-", calculateDashLength(action)))
					.a("< ")
					.fg(Ansi.Color.CYAN)
					.a(EXECUTION_LOG_TITLE)
					.boldOff()
					.a(": ")
					.a(action.getClass().getSimpleName())
					.fg(Ansi.Color.DEFAULT)
					.bold()
					.a(" >")
					.a(StringUtils.repeat("-", calculateDashLength(action)))
					.reset()
					.toString());
			LOG.info(Ansi.ansi()
					.fg(Ansi.Color.GREEN)
					.a(WordUtils.wrap(action.getDescription(context), WIDTH, null, true))
					.reset()
					.toString());
		}
	}

	private static int calculateDashLength(final DataPopulationAction action) {
		return (WIDTH - EXECUTION_LOG_TITLE.length() - SEPARATOR_LENGTH * SEPARATOR_COUNT - action.getClass().getSimpleName().length()) / 2;
	}
}
