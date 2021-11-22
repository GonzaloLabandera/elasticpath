/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.insights.util;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.Set;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for accessing JMX attributes.
 */
public final class MxUtil {
	private static final Logger LOG = LoggerFactory.getLogger(MxUtil.class);
	private static final long MICROSECONDS_PER_SECOND = 1000;

	private static final RuntimeMXBean RUNTIME_MX_BEAN = ManagementFactory.getRuntimeMXBean();
	private static final List<GarbageCollectorMXBean> GC_MX_BEANS = ManagementFactory.getGarbageCollectorMXBeans();

	/**
	 * Private constructor to prevent instantiation.
	 */
	private MxUtil() {
		// No op
	}

	/**
	 * Get the JVM uptime.
	 *
	 * @return JVM uptime in seconds
	 */
	public static Long getUptimeSeconds() {
		return RUNTIME_MX_BEAN.getUptime() / MICROSECONDS_PER_SECOND;
	}

	/**
	 * Get the arguments that were passed to the JVM at startup.
	 *
	 * @param sensitiveJvmArgKeyWords list of sensitive keys in jvm argument
	 * @return a list of JVM arguments
	 */
	public static List<String> getJvmArguments(final List<String> sensitiveJvmArgKeyWords) {
		return SecurityMaskingUtil.maskValuesWithMatchingKeys(sensitiveJvmArgKeyWords, RUNTIME_MX_BEAN.getInputArguments());
	}

	/**
	 * Get a list of garbage collectors configured in the JVM.
	 *
	 * @return a list of garbage collector names
	 */
	public static List<GarbageCollectorMXBean> getGarbageCollectors() {
		return GC_MX_BEANS;
	}

	/**
	 * Determine if the running application server is Tomcat.
	 *
	 * @return true if Tomcat is the running application server
	 */
	public static boolean isTomcat() {
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		try {
			server.getObjectInstance(new ObjectName("Tomcat:type=Server"));
			return true;
		} catch (InstanceNotFoundException | MalformedObjectNameException e) {
			// Do nothing
		}
		return false;
	}

	/**
	 * Retrieve a JMX attribute where the objectName always resolves to a single JMX node.
	 * Returns null if the attribute doesn't exist.
	 *
	 * @param objectName the object name to lookup
	 * @param attribute  the attribute name to retrieve
	 * @param emptyValue the value to return if the attribute exists but is empty
	 * @param <T>        the type of the return value
	 * @return the attribute value
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getSimpleAttribute(final String objectName, final String attribute, final T emptyValue) {
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		try {
			Object value = server.getAttribute(new ObjectName(objectName), attribute);
			if (value == null) {
				return emptyValue;
			}
			return (T) value;
		} catch (InstanceNotFoundException e) {
			// Ignore
		} catch (Exception exception) {
			LOG.error("Unable to retrieve JMX attribute.", exception);
		}
		return null;
	}

	/**
	 * Retrieve a JMX attribute where the objectName could resolve to multiple JMX nodes, and use the first one.
	 * Returns null if the attribute doesn't exist.
	 *
	 * @param objectName the object name to lookup
	 * @param attribute  the attribute name to retrieve
	 * @return the attribute value as an Integer
	 */
	public static Integer getFirstMatchingObjectAttributeInt(final String objectName, final String attribute) {
		return getFirstMatchingObjectAttributeObject(objectName, attribute, null);
	}

	/**
	 * Retrieve a JMX attribute where the objectName could resolve to multiple JMX nodes, and use the first one.
	 * Returns null if the attribute doesn't exist.
	 *
	 * @param objectName the object name to lookup
	 * @param attribute  the attribute name to retrieve
	 * @return the attribute value as a Long
	 */
	public static Long getFirstMatchingObjectAttributeLong(final String objectName, final String attribute) {
		return getFirstMatchingObjectAttributeObject(objectName, attribute, null);
	}

	/**
	 * Retrieve a JMX attribute where the objectName could resolve to multiple JMX nodes, and use the first one.
	 * Returns null if the attribute doesn't exist.
	 *
	 * @param objectName the object name to lookup
	 * @param attribute  the attribute name to retrieve
	 * @return the attribute value as a Double
	 */
	public static Double getFirstMatchingObjectAttributeDouble(final String objectName, final String attribute) {
		return getFirstMatchingObjectAttributeObject(objectName, attribute, null);
	}

	/**
	 * Retrieve a JMX attribute where the objectName could resolve to multiple JMX nodes, and use the first one.
	 * Returns null if the attribute doesn't exist.
	 *
	 * @param objectName the object name to lookup
	 * @param attribute  the attribute name to retrieve
	 * @return the attribute value as a Boolean
	 */
	public static Boolean getFirstMatchingObjectAttributeBoolean(final String objectName, final String attribute) {
		return getFirstMatchingObjectAttributeObject(objectName, attribute, null);
	}

	/**
	 * Retrieve a JMX attribute where the objectName could resolve to multiple JMX nodes, and use the first one.
	 * Returns null if the attribute doesn't exist.
	 *
	 * @param objectName the object name to lookup
	 * @param attribute  the attribute name to retrieve
	 * @return the attribute value as a String
	 */
	public static String getFirstMatchingObjectAttributeString(final String objectName, final String attribute) {
		return getFirstMatchingObjectAttributeObject(objectName, attribute, "");
	}

	/**
	 * Retrieve a JMX attribute where the objectName could resolve to multiple JMX nodes, and use the first one.
	 * Returns null if the attribute doesn't exist.
	 *
	 * @param objectName the object name to lookup
	 * @param attribute  the attribute name to retrieve
	 * @param emptyValue the value to return if the attribute exists but is empty
	 * @param <T>        the type of the return value
	 * @return the attribute value
	 */
	@SuppressWarnings("unchecked")
	private static <T> T getFirstMatchingObjectAttributeObject(final String objectName, final String attribute, final T emptyValue) {
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		try {
			Set<ObjectName> matchingNames = server.queryNames(new ObjectName(objectName), null);
			if (matchingNames.iterator().hasNext()) {
				Object value = server.getAttribute(new ObjectName(matchingNames.iterator().next().getCanonicalName()), attribute);
				if (value == null) {
					return emptyValue;
				}
				return (T) value;
			}
		} catch (Exception exception) {
			LOG.error("Unable to retrieve JMX attribute.", exception);
		}

		return null;
	}
}
