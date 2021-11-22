/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.commons.handlers.libjar;

/**
 * Utility class for handling lib jar protocol.
 */
public final class LibJarUtil {

	/**
	 * Custom lib jar protocol.
	 */
	public static final String LIB_JAR_PROTOCOL = "lib-jar";
	private static final String LIB_JAR_PROTOCOL_WITH_COLON = LIB_JAR_PROTOCOL + ':';
	private static final String JAR_PATH_START = "!/";
	private static final String LIB_LOCATION = "lib/";

	/**
	 * Constructor.
	 */
	private LibJarUtil() {
		super();
	}

	/**
	 * Parses lib jar path from spec string.
	 *
	 * @param spec the String representing the URL that must be parsed
	 * @return the path to the lib resource inside the container jar
	 */
	public static String parsePath(final String spec) {
		if (spec.startsWith(LIB_JAR_PROTOCOL_WITH_COLON)) {
			final String path = spec.substring(LIB_JAR_PROTOCOL_WITH_COLON.length());
			final int pathStart = path.lastIndexOf(JAR_PATH_START);
			if (pathStart == -1) {
				return path;
			}
			return path.substring(pathStart + JAR_PATH_START.length());
		} else if (spec.startsWith(LIB_LOCATION)) {
			return spec;
		}
		return LIB_LOCATION + spec;
	}
}
