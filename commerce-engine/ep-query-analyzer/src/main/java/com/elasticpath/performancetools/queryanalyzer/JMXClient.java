/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.performancetools.queryanalyzer;

import static com.elasticpath.performancetools.queryanalyzer.utils.Defaults.CORTEX_SERVER_NAME;
import static com.elasticpath.performancetools.queryanalyzer.utils.Defaults.EHCACHE_MBEAN_NAME;
import static com.elasticpath.performancetools.queryanalyzer.utils.Defaults.getApplicationEhcacheMBeanNamePrefix;
import static com.elasticpath.performancetools.queryanalyzer.utils.Patterns.LOG_FILENAME_PATTERN;
import static java.lang.String.format;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.performancetools.queryanalyzer.exceptions.QueryAnalyzerException;

/**
 * JMX client used to toggle Logback log level.
 */
public class JMXClient {

	/*
		LOG4J2 NOTE:
			LOG4J2 doesn't support dynamic addition of the loggers, as LOGBACK does. It is *possible* to do that though, by modifying the
			configuration XML (via getConfigText (encoding) and setConfigText (ConfigurationXMLString, encoding) methods) but this is rather
			cumbersome.

			It is strongly recommended to have all required loggers created in the log4j2.xml.
	 */
	/** List of common loggers to toggle levels for. */
	static final String[] CE_LOGGERS = {"openjpa"};
	/** List of Cortex-specific loggers to toggle levels for. */
	static final String[] CORTEX_LOGGERS = {
			"com.elasticpath.rest.resource.dispatch.operator.AnnotatedMethodDispatcher",
			"com.elasticpath.rest.resource.dispatch.linker.ResourceLinkerImpl",
			"com.elasticpath.rest.helix.runtime.server.PrototypeDispatcherResourceServer"};

	private static final Logger LOG = LoggerFactory.getLogger(JMXClient.class);
	private static final String LOGBACK_MBEAN_QUERY = "ch.qos.logback.classic:*";
	private static final String LOG4J2_TRACE_LOGGER_MBEAN_NAME = "org.apache.logging.log4j2:component=Loggers,name=%s,type=%s";
	private static final String LOG4J2_LOGGERS_MBEAN_QUERY = "org.apache.logging.log4j2:component=Loggers,*";
	private static final String LOG4J2_ALL_MBEAN_QUERY = "org.apache.logging.log4j2:*";

	private MBeanServerConnection mBeanServer;

	/**
	 * Custom constructor.
	 *
	 * @param remoteJmxPort Remote JMX port.
	 */
	public JMXClient(final String remoteJmxPort) {
		try {
			final JMXServiceURL jmxServiceURL = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://:" + remoteJmxPort + "/jmxrmi");

			LOG.debug("Connecting to JMX server using url {}", jmxServiceURL);

			final JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxServiceURL, null);

			mBeanServer = jmxConnector.getMBeanServerConnection();

			LOG.debug("JMX connection established successfully");
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Custom constructor.
	 *
	 * @param mBeanServer the MBean server to connect to.
	 */
	public JMXClient(final MBeanServer mBeanServer) {
		this.mBeanServer = mBeanServer;
	}

	/**
	 * Toggle log level via JMX.
	 *
	 * @param logLevel log level to toggle.
	 */
	public void toggleLogLevel(final String logLevel) {
		try {
			LOG.debug("Enabling {} level for loggers: {}", logLevel, Arrays.asList(CE_LOGGERS));
			boolean isLogback = false;

			//search for LOGBACK MBeans
			Set<ObjectName> loggingFrameworkMBeanNames = mBeanServer.queryNames(new ObjectName(LOGBACK_MBEAN_QUERY), null);
			if (loggingFrameworkMBeanNames.isEmpty()) {
				loggingFrameworkMBeanNames = mBeanServer.queryNames(new ObjectName(LOG4J2_LOGGERS_MBEAN_QUERY), null);

				//if LOG4J2 is configured, the query must return exactly 1 MBean
				if (loggingFrameworkMBeanNames.isEmpty()) {
					throw new IllegalStateException("Logback or Log4J2 MBean is missing. "
							+ "Can't turn on OpenJPA TRACE required for further processing");
				}
			} else {
				isLogback = true;
			}

			ObjectName loggingFrameworkMBeanName = loggingFrameworkMBeanNames.iterator().next();

			setLogLevelForLoggers(isLogback, logLevel, loggingFrameworkMBeanName, CE_LOGGERS);

			String serverName = QueryAnalyzerConfigurator.INSTANCE.getApplicationName();
			if (serverName.equalsIgnoreCase(CORTEX_SERVER_NAME)) {
				setLogLevelForLoggers(isLogback, logLevel, loggingFrameworkMBeanName, CORTEX_LOGGERS);
			}


		} catch (Exception e) {
			throw new QueryAnalyzerException("Error occurred while enabling [" + logLevel + "] level for loggers", e);
		}
	}

	private void setLogLevelForLoggers(final boolean isLogback, final String logLevel, final ObjectName loggingFrameworkMBeanName,
									   final String[] loggerNames) {
		String loggingFramework = "";

		try {
			if (isLogback) {
				loggingFramework = "Logback";
				setLogLevelForLogbackLoggers(logLevel, loggingFrameworkMBeanName, loggerNames);
			} else {
				loggingFramework = "Log4J2";
				setLogLevelForLog4J2Loggers(logLevel, loggingFrameworkMBeanName, loggerNames);
			}
		} catch (Exception e) {
			throw new QueryAnalyzerException("Error occurred while setting [" + logLevel + "] for " + loggingFramework + " ["
					+ loggingFrameworkMBeanName + "] logger", e);
		}
	}

	private void setLogLevelForLog4J2Loggers(final String logLevel, final ObjectName loggingFrameworkMBeanName, final String[] loggerNames) {

		String mbeanTypeProperty = loggingFrameworkMBeanName.getKeyProperty("type");
		Attribute logLevelAttribute = new Attribute("Level", logLevel);

		try {
			for (String traceLogger : loggerNames) {
				LOG.debug("Enabling {} level for logger: {}", logLevel, traceLogger);

				String formattedTraceLoggerName = format(LOG4J2_TRACE_LOGGER_MBEAN_NAME, traceLogger, mbeanTypeProperty);
				ObjectName traceLoggerObjectName = new ObjectName(formattedTraceLoggerName);
				mBeanServer.setAttribute(traceLoggerObjectName, logLevelAttribute);
				LOG.debug("{} level enabled for logger: {}", logLevel, traceLogger);
			}
		} catch (Exception e) {
			throw new QueryAnalyzerException(e.getMessage(), e);
		}
	}

	//LOGBACK used for compatibility with older EP versions
	private void setLogLevelForLogbackLoggers(final String logLevel, final ObjectName loggingFrameworkMBeanName, final String[] loggerNames) {
		try {
			for (String traceLogger : loggerNames) {
				LOG.debug("Enabling {} level for logger: {}", logLevel, traceLogger);
				mBeanServer.invoke(loggingFrameworkMBeanName, "setLoggerLevel",
						new Object[]{traceLogger, logLevel},
						new String[]{String.class.getName(), String.class.getName()});

				LOG.debug("{} level enabled for logger: {}", logLevel, traceLogger);
			}
		} catch (Exception e) {
			throw new QueryAnalyzerException(e.getMessage(), e);
		}
	}

	/**
	 * Return the log file path from the logging framework's FILE appender via JMX.
	 * Currently, this is possible only for LOG4J2 and not quite easy since File/@fileName can't be accessed via JMX.
	 * Instead, the method obtains the full LOG4J2 XML and extracts the "fileName" value using RegExp.
	 *
	 * @return null if not found or the actual log file path.
	 */
	public String getLogFilePath() {
		try {
			Set<ObjectName> loggingFrameworkMBeanNames = mBeanServer.queryNames(new ObjectName(LOG4J2_ALL_MBEAN_QUERY), null);
			//looking for an ObjectName with only "type" property; other MBeans will have more properties
			//we need a specific bean to obtain the content of the log4j2.xml and read FILE appender's "fileName" attribute
			// (unfortunately, not accessible via JMX)
			ObjectName configurationMBenName = loggingFrameworkMBeanNames.stream()
					.filter(objName -> objName.getKeyPropertyList().size() == 1)
					.findFirst()
					.get();

			Object result = mBeanServer.invoke(configurationMBenName, "getConfigText",
					new Object[]{"UTF-8"},
					new String[]{String.class.getName()});

			if (result == null || result.toString().isEmpty()) {
				return null;
			}

			String configXML = result.toString();
			Matcher logFileNameMatcher = LOG_FILENAME_PATTERN.matcher(configXML);

			if (logFileNameMatcher.find()) {
				return logFileNameMatcher.group(1);
			}

			return null;

		} catch (Exception e) {
			throw new QueryAnalyzerException("Error occurred while getting the log file path", e);
		}
	}

	/**
	 * Clear all Ehcache caches.
	 *
	 * @return true if clearing operation succeeded.
	 */
	public boolean clearEhCache() {
		try {
			LOG.debug("Clearing ehcache");
			String applicationName = QueryAnalyzerConfigurator.INSTANCE.getApplicationName();
			String applicationEhcacheMBeanNamePrefix = getApplicationEhcacheMBeanNamePrefix(applicationName);
			String formattedEhcacheMbeanName = format(EHCACHE_MBEAN_NAME, applicationEhcacheMBeanNamePrefix);
			final ObjectName ehCache = new ObjectName(formattedEhcacheMbeanName);
			mBeanServer.invoke(ehCache, "clearAll", new Object[]{}, new String[]{});
			return true;
		} catch (Exception e) {
			LOG.error("Error occurred while clearing caches");
		}
		return false;
	}
}
