/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.performancetools.queryanalyzer;

import static com.elasticpath.performancetools.queryanalyzer.utils.Defaults.CORTEX_SERVER_NAME;
import static com.elasticpath.performancetools.queryanalyzer.utils.Defaults.DEFAULT_JMX_PORT;
import static com.elasticpath.performancetools.queryanalyzer.utils.Defaults.ERROR_LOG_LEVEL;
import static com.elasticpath.performancetools.queryanalyzer.utils.Defaults.NOT_AVAILABLE;
import static com.elasticpath.performancetools.queryanalyzer.utils.Defaults.TRACE_LOG_LEVEL;
import static com.elasticpath.performancetools.queryanalyzer.utils.SystemProperties.JMX_PORT_SYSTEM_PROPERTY;
import static com.elasticpath.performancetools.queryanalyzer.utils.SystemProperties.LOG_FILE_PATH_SYSTEM_PROPERTY;
import static com.elasticpath.performancetools.queryanalyzer.utils.SystemProperties.RESULT_STATS_FOLDER_PATH_SYSTEM_PROPERTY;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.File;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.performancetools.queryanalyzer.exceptions.QueryAnalyzerException;

/**
 * Configurator class.
 * It invokes {@link JMXClient} to toggle log levels on
 * bundle start/stop, as well as to obtain the reference to cortex log file.
 */

public enum QueryAnalyzerConfigurator {
	/**
	 * Configurator instance.
	 */
	INSTANCE;

	private static final Logger LOG = LoggerFactory.getLogger(QueryAnalyzerConfigurator.class);

	private static final int MAIN_LOG_FILES_NUMBER_OF_EXTENSIONS = 1;

	private String testId;
	private String testName;
	private String applicationName;
	private String resultFolderPath;
	private boolean isEhcacheCleaned;

	private List<String> outputFileExtensions = new ArrayList<>();
	private File logFile;
	private JMXClient jmxClient;

	/**
	 * The wrapper method that calls required configuration methods.
	 * @return this instance.
	 */
	public QueryAnalyzerConfigurator init() {
		this.resultFolderPath = null;
		return enableTraceLogLevelViaJMX()
				.setLogFile()
				.prepareLogFile();
	}

	/**
	 * Clean the instance.
	 */
	public void clean() {
		this.testId = null;
		this.testName = null;
		this.applicationName = null;
		this.jmxClient = null;
		this.resultFolderPath = null;
		this.isEhcacheCleaned = false;
		this.outputFileExtensions = new ArrayList<>();
		this.logFile = null;
	}
	/**
	 * Enable trace log level for target classes via JMX.
	 *
	 * @return this class instance.
	 */
	public QueryAnalyzerConfigurator enableTraceLogLevelViaJMX() {
		final String jmxPort = resolveJmxPort();

		return enableTraceLogLevelViaJMX(jmxPort);
	}

	/**
	 * Enable trace log level via JMX.
	 *
	 * @param jmxPort the jmx port
	 * @return this instance
	 */
	public QueryAnalyzerConfigurator enableTraceLogLevelViaJMX(final String jmxPort) {
		this.jmxClient = new JMXClient(jmxPort);
		this.jmxClient.toggleLogLevel(TRACE_LOG_LEVEL);

		return this;
	}

	/**
	 * Clear all Ehcache caches.
	 *
	 * @param cleanCache the flag
	 * @return this instance
	 */
	public QueryAnalyzerConfigurator clearEhCache(final Boolean cleanCache) {
		if (jmxClient != null && (cleanCache == null || cleanCache)) {
			isEhcacheCleaned = this.jmxClient.clearEhCache();
			LOG.info("Application cache is cleared");
		}
		return this;
	}

	/**
	 * Set a reference to a Cortex log file from logback.xml.
	 *
	 * @return this class instance.
	 */
	public QueryAnalyzerConfigurator setLogFile() {
		LOG.debug("Getting log file path from system property {}", LOG_FILE_PATH_SYSTEM_PROPERTY);
		/*
			The order of setting the log file path:

			1. From system property "log.file.path"
			2. Via JMX - from logging framework
				works for LOG4J2 but not for LOGBACK;
			3. Default to USER_HOME/ep/logs/ep-{SERVER}.log
		 */

		String logFilePath = System.getProperty(LOG_FILE_PATH_SYSTEM_PROPERTY);
		if (StringUtils.isBlank(logFilePath)) {
			if (jmxClient != null) {
				logFilePath = jmxClient.getLogFilePath();
			}

			if (StringUtils.isBlank(logFilePath)) {
				logFilePath = System.getProperty("user.home") + "/ep/logs/ep-" + getApplicationName() + ".log";

				LOG.warn("The log file path couldn't be obtained via JMX or System property {}. Using defaults {}",
						LOG_FILE_PATH_SYSTEM_PROPERTY, logFilePath);
			}
		}

		LOG.debug("Log file path: {}", logFilePath);

		logFile = new File(logFilePath);

		if (!logFile.exists()) {
			throw new IllegalStateException("Log file not found under [" + logFile.getAbsolutePath() + "]! "
					+ "Ensure that system property [-D" + LOG_FILE_PATH_SYSTEM_PROPERTY + "] is specified"
					+ " or file exists under USER_HOME/ep/logs dir");
		}

		return this;
	}

	/**
	 * Delete all existing log files (e.g. ep-cortex.log.1, ep-cortex.log.2 etc) and clears the main log
	 * (because it can't be deleted - at least not on Windows) before starting a new analysis
	 *
	 * @return {@link QueryAnalyzerConfigurator}
	 */
	public QueryAnalyzerConfigurator prepareLogFile()  {
		final String logFileNamePrefix = logFile.getName().split("[.]")[0];
		final File logFolder = logFile.getParentFile();
		final File[] logFiles = logFolder.listFiles((file, fileName) -> fileName.startsWith(logFileNamePrefix));

		if (logFiles == null) {
			throw new IllegalStateException("Log files not found! "
					+ "Ensure that system property [-D" + LOG_FILE_PATH_SYSTEM_PROPERTY + "] is specified"
					+ "or file exists under USER_HOME/ep/logs dir");
		}

		for (File logFile : logFiles) {
			LOG.debug("Deleting log file {}", logFile.getAbsolutePath());

			//look for main log file - must have a single extension e.g. ".log"
			if (StringUtils.countMatches(logFile.getName(), ".") == MAIN_LOG_FILES_NUMBER_OF_EXTENSIONS) {
				try (Writer writer = Files.newBufferedWriter(logFile.toPath(), StandardCharsets.UTF_8, TRUNCATE_EXISTING)) {
					LOG.debug("Clearing log file {}", logFile.getAbsolutePath());

					writer.write("");
					writer.flush();

					LOG.debug("Log file cleared successfully");
				} catch (Exception exception) {
					throw new QueryAnalyzerException("Exception occurred while preparing log file", exception);
				}
			} else if (logFile.delete()) { //delete all other log files, with multiple extensions (e.g. .log.1, log.2 etc) - if possible
				LOG.debug("Log file deleted successfully");
			}
		}
		return this;
	}

	public File getLogFile() {
		return logFile;
	}

	/**
	 * Set ERROR log level for target loggers.
	 */
	public void restoreLogLevels() {
		jmxClient.toggleLogLevel(ERROR_LOG_LEVEL);
	}

	/**
	 * Set the list of extensions/formats to save the reports to.
	 *
	 * @param outputFileExtensions the list of output file extensions
	 * @return this instance
	 */
	public QueryAnalyzerConfigurator setOutputFileExtensions(final List<String> outputFileExtensions) {
		this.outputFileExtensions = outputFileExtensions;
		return this;
	}

	public List<String> getOutputFileExtensions() {
		return outputFileExtensions;
	}

	private String resolveJmxPort() {

		/*	try to resolve the JMX port from "com.sun.management.jmxremote.port" or fallback to "ep.<APP_NAME>.jmx.port" sys property
			if application name is not set, "cortex" will be used
			if JMX port can't be resolved after all attempts, the default port "6969" will be used*/

		final String jmxPort = System.getProperty(JMX_PORT_SYSTEM_PROPERTY, System.getProperty("ep." + getApplicationName() + ".jmx.port"));
		if (!NumberUtils.isCreatable(jmxPort)) {
			LOG.warn("JMX port is not set via -D{} property OR is not a number.", JMX_PORT_SYSTEM_PROPERTY);
			LOG.warn("Default port [{}] will be used.", DEFAULT_JMX_PORT);
			return DEFAULT_JMX_PORT;
		}
		return jmxPort;
	}

	public String getTestId() {
		return defaultIfEmpty(testId, "");
	}

	/**
	 * Set the test id. Used in performance cucumber tests.
	 * @param testId the test id
	 * @return this instance
	 */
	public QueryAnalyzerConfigurator setTestId(final String testId) {
		this.testId = testId;
		return this;
	}

	public String getTestName() {
		return testName;
	}

	/**
	 * Set the test name. Used in performance cucumber tests.
	 *
	 * @param testName the test name
	 * @return this instance
	 */
	public QueryAnalyzerConfigurator setTestName(final String testName) {
		this.testName = testName;
		return this;
	}

	public String getApplicationName() {
		return defaultIfEmpty(applicationName, CORTEX_SERVER_NAME);
	}

	/**
	 * Set the name of the application being profiled.
	 *
	 * @param applicationName the application's name
	 * @return this instance
	 */
	public QueryAnalyzerConfigurator setApplicationName(final String applicationName) {
		this.applicationName = applicationName;
		return this;
	}

	/**
	 * Returns the folder path where results will be saved.
	 * @return the folder path.
	 */
	public String getResultFolderPath() {
		if (isEmpty(resultFolderPath)) {
			resultFolderPath = System.getProperty(RESULT_STATS_FOLDER_PATH_SYSTEM_PROPERTY, System.getProperty("user.home"));
		}
		return resultFolderPath;
	}

	/**
	 * Print configuraiton.
	 *
	 * @return the configuration
	 */
	public String printConfiguration() {
		String configuration =  new StringBuilder("Query analyzer configuration:\n")
				.append("Test ID:").append(defaultIfEmpty(testId, NOT_AVAILABLE)).append("\n")
				.append("Test name:").append(defaultIfEmpty(testName, NOT_AVAILABLE)).append("\n")
				.append("Application name:").append(defaultIfEmpty(getApplicationName(), NOT_AVAILABLE)).append("\n")
				.append("Result folder path:").append(defaultIfEmpty(getResultFolderPath(), NOT_AVAILABLE)).append("\n")
				.append("Are all Ehcache caches cleaned?:").append(isEhcacheCleaned).append("\n")
				.append("Output file extensions:").append(outputFileExtensions).append("\n")
				.append("Input log file:").append(logFile == null
						? "null"
						: logFile.getAbsolutePath()).append("\n")
				.append("JMX Client:").append(jmxClient).append("\n")
				.toString();

		LOG.info(configuration);
		return configuration;
	}
}
