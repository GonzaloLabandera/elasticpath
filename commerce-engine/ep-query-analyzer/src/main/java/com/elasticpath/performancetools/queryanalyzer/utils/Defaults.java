/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.performancetools.queryanalyzer.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Holder for default settings and constants.
 */
public final class Defaults {

	/** Cortex server name. */
	public static final String CORTEX_SERVER_NAME = "cortex";
	/** Cortex cache manager name prefix. */
	public static final String CORTEX_CACHE_MANAGER_NAME_PREFIX = "CoreOSGiBundle";
	/** Ehcache cache manager MBean name. */
	public static final String EHCACHE_MBEAN_NAME = "net.sf.ehcache:type=CacheManager,name=%s-CacheManager";
	/** Error log level constant. */
	public static final String ERROR_LOG_LEVEL = "ERROR";
	/** Trace log level constant. */
	public static final String TRACE_LOG_LEVEL = "TRACE";
	/** The default JMX port. */
	public static final String DEFAULT_JMX_PORT = "6969";
	/** JSON file extension. */
	public static final String JSON_OUTPUT_FILE_EXTENSION = "json";
	/** CSV file extension. */
	public static final String CSV_OUTPUT_FILE_EXTENSION = "csv";

	/** The default file name. */
	public static final String DEFAULT_OUTPUT_FILE_NAME_PREFIX = "db_statistics";

	/** The dot delimiter.*/
	public static final String DOT_DELIMITER = ".";
	/** Not-Available. */
	public static final String NOT_AVAILABLE = "N/A";

	private static final Map<String, String> EHCACHE_MANAGER_NAME_PREFIXES = new HashMap<>();

	static {
		EHCACHE_MANAGER_NAME_PREFIXES.put(CORTEX_SERVER_NAME, CORTEX_CACHE_MANAGER_NAME_PREFIX);
		EHCACHE_MANAGER_NAME_PREFIXES.put("integration", "Integration");
	}

	private Defaults() {
		//private constructor
	}

	/**
	 * Get the Ehcache cache manager MBean name prefix.
	 *
	 * @param application the application to find appropriate mbean prefix
	 * @return the prefix
	 */
	public static String getApplicationEhcacheMBeanNamePrefix(final String application) {
		return EHCACHE_MANAGER_NAME_PREFIXES.get(application);
	}
}
