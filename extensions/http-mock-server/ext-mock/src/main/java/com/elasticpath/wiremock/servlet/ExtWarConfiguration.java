/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.wiremock.servlet;

import static com.github.tomakehurst.wiremock.extension.ExtensionLoader.asMap;
import static com.github.tomakehurst.wiremock.extension.ExtensionLoader.load;
import static com.github.tomakehurst.wiremock.extension.ExtensionLoader.valueAssignableFrom;
import static com.google.common.collect.Maps.filterEntries;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.util.Arrays.asList;

import java.util.Map;

import javax.servlet.ServletContext;

import com.github.tomakehurst.wiremock.common.ClasspathFileSource;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Extension;
import com.github.tomakehurst.wiremock.servlet.WarConfiguration;

/**
 * An extension of {@link com.github.tomakehurst.wiremock.servlet.WarConfiguration} to allow injection of
 * extensions.
 */
public class ExtWarConfiguration extends WarConfiguration {

	/**
	 * This is the wiremock servlet init parameter name that specifies where to load mappings and static responses.
	 */
	public static final String WIRE_MOCK_CLASSPATH_FILE_SOURCE_ROOT = "WireMockFileSourceRoot";

	private final Map<String, Extension> extensionMap = newLinkedHashMap();

	private final ServletContext servletContext;

	/**
	 * Constructor that initializes the WarConfiguration using values from the ServletContext.
	 *
	 * @param servletContext	the ServletContext to read the configuration values from
	 */
	public ExtWarConfiguration(final ServletContext servletContext) {
		super(servletContext);
		this.servletContext = servletContext;
	}

	/**
	 * Adds extensions to the Map that tracks all extensions.
	 *
	 * @param classNames	the String representation of class names of extension classes that should be added to the list
	 * @return returns this instance of ExtWarConfiguration
	 */
	public ExtWarConfiguration extensions(final String... classNames) {
		extensionMap.putAll(load(classNames));
		return this;
	}

	/**
	 * Adds extensions to the Map that tracks all extensions.
	 *
	 * @param extensionInstances  the Extension objects that will be added directly to the Collection that tracks all extension.
	 * @return this instance of ExtWarConfiguration
	 */
	public ExtWarConfiguration extensions(final Extension... extensionInstances) {
		extensionMap.putAll(asMap(asList(extensionInstances)));
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Extension> Map<String, T> extensionsOfType(final Class<T> extensionType) {
		return (Map<String, T>) filterEntries(extensionMap, valueAssignableFrom(extensionType));
	}

	@Override
	public FileSource filesRoot() {
		String classpathRootForMappingsAndResponses = servletContext.getInitParameter(WIRE_MOCK_CLASSPATH_FILE_SOURCE_ROOT);
		return new ClasspathFileSource(classpathRootForMappingsAndResponses);
	}
}