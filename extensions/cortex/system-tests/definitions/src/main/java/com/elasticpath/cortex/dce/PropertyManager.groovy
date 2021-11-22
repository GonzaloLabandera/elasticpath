package com.elasticpath.cortex.dce

import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.LogManager

/**
 * Simple properties manager class to keep the loading of configuration file in one place.
 */
class PropertyManager {

	private static final Logger LOGGER = LogManager.getLogger(PropertyManager.class);

	/** Singleton instance. **/
	private static PropertyManager instance = null;

	/** Location of configuration file. **/
	private static final String CONFIG_FILENAME = "ep-test-plugin.properties";

	/** Property object used for getting the properties. **/
	private final Properties properties;

	private static final Object LOCKOBJ = new Object();

	/**
	 * s Default constructor.
	 */
	public PropertyManager() {
		properties = new Properties();
		try {
			ClassLoader classLoader = getClass().getClassLoader();
			InputStream stream = classLoader.getResourceAsStream(CONFIG_FILENAME);
			if (stream == null) {
				String.format("Could not find config file %s", CONFIG_FILENAME);
			}
			try {
				properties.load(new InputStreamReader(stream, "UTF-8"));
			} finally {
				stream.close();
			}
		} catch (IOException e) {
			LOGGER.error(String.format("Could not read config file %s. Exception: \n%s", CONFIG_FILENAME), e);
		}
	}

	/**
	 * Fetches property value given the key.
	 *
	 * @param key the key used to identify the requested value
	 * @return value associated with key or null if key doesn't exist
	 */
	public String getProperty(final String key) {
		final String fileProperty = properties.getProperty(key);
		final String envProperty = System.getProperty(key);
		if (envProperty == null) {
			return fileProperty;
		} else {
			return envProperty;
		}
	}

	/**
	 * Fetches property value given the key, with a default value.
	 *
	 * @param key the key used to identify the requested value
	 * @param defaultValue in case neither fileProperty or envProperty get what we need.
	 * @return value associated with key or null if key doesn't exist
	 */
	public String getProperty(final String key, final String defaultValue) {
		final String fileProperty = properties.getProperty(key, defaultValue);
		final String envProperty = System.getProperty(key);
		if (envProperty == null) {
			return fileProperty;
		} else {
			return envProperty;
		}
	}

	/**
	 * get property manager instance.
	 *
	 * @return get the current property manager instance.
	 */
	public static PropertyManager getInstance() {
		synchronized (LOCKOBJ) {
			if (instance == null) {
				instance = new PropertyManager();
			}
			return instance;
		}
	}
}

