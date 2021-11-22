/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.performancetools.queryanalyzer.utils;

/**
 * Holder for system properties.
 */
public final class SystemProperties {

	/** JMX system property. */
	public static final String JMX_PORT_SYSTEM_PROPERTY = "com.sun.management.jmxremote.port";
	/** External Cortex log file location system property. */
	public static final String LOG_FILE_PATH_SYSTEM_PROPERTY = "log.file.path";
	/** Should output JSON to console only. */
	public static final String PRINT_JSON_TO_CONSOLE_ONLY_SYSTEM_PROPERTY = "print.json.to.console.only";
	/** Result file name system property. */
	public static final String RESULT_STATS_FILE_NAME_SYSTEM_PROPERTY = "result.stats.file.name";
	/** Result file format system property. */
	public static final String RESULT_STATS_FILE_FORMAT_SYSTEM_PROPERTY = "result.stats.file.format";
	/** The folder path where result stats file will be saved.*/
	public static final String RESULT_STATS_FOLDER_PATH_SYSTEM_PROPERTY = "result.stats.folder.path";


	private SystemProperties() {
		//constant holder
	}
}
