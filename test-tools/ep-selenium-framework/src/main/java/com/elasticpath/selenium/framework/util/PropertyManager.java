package com.elasticpath.selenium.framework.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Simple properties manager class to keep the loading of configuration file in one place.
 */
public class PropertyManager {

	private static final Logger LOGGER = Logger.getLogger(PropertyManager.class);

	/** Singleton instance. **/
	private static PropertyManager instance;

	/** Location of configuration file. **/
	private static final String CONFIG_FILENAME = "selenium.properties";

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
