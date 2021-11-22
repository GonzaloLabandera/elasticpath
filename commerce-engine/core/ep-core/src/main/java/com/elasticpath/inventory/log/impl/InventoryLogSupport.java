/**
 * Copyright (c) Elastic Path Software Inc., 2011
 */
package com.elasticpath.inventory.log.impl;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class provides Inventory specific logging capabilities. Log4j logger is used for logging purposes. 
 */
public class InventoryLogSupport {

	private static final Logger LOGGER = LogManager.getLogger("com.elasticpath.inventory");

	private InventoryLogFormatter formatter;
	
	private Level level = Level.TRACE;
	
	private static final String TRACE_EXECUTION = "INV001";
	
	/**
	 * Writes a log entry. The information to log is provided within the {@link LogContext}.
	 * 
	 * @param logContext holds the information to log
	 */
	public void logCommandExecution(final InventoryLogContext logContext) {
		if (LOGGER.isEnabled(level)) {
			String formattedLogEntry = formatter.format(TRACE_EXECUTION, logContext);
			LOGGER.log(level, formattedLogEntry);
		}
	}
	
	/**
	 * Writes a separate log entry with a specified logging level.
	 * 
	 * @param level logging level
	 * @param message log message
	 * @param logContext log context
	 */
	public void log(final Level level, final String message, final InventoryLogContext logContext) {
		if (isEnabledFor(level)) {
			String formattedLogEntry = formatter.format(message, logContext);
			LOGGER.log(level, formattedLogEntry);
		}	
	}

	/**
	 * Checks whether the specified log level is enabled.
	 * @param level log level.
	 * @return true if the level is enabled.
	 */
	public boolean isEnabledFor(final Level level) {
		return LOGGER.isEnabled(level);
	}
	
	/**
	 * Sets the {@link LogFormatter} instance.
	 * 
	 * @param formatter log formatter
	 */
	public void setFormatter(final InventoryLogFormatter formatter) {
		this.formatter = formatter;
	}

	/**
	 * Sets the logging level as per appropriate string representation.
	 * 
	 * @param level logging level
	 */
	public void setLevel(final String level) {
		this.level = Level.toLevel(level);
	}

}
