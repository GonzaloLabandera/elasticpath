/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.commons.handlers.libjar;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * {@link URLConnection} supporting jar-in-jar resource loading strategy.
 */
public class LibJarURLConnection extends URLConnection {

	/**
	 * Constructor.
	 *
	 * @param url URL to internal jar resource
	 */
	public LibJarURLConnection(final URL url) {
		super(url);
	}

	@Override
	public void connect() {
		// nothing to do
	}

	@Override
	public InputStream getInputStream() throws IOException {
		final String resourceName = URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8.name());
		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		final InputStream inputStream = contextClassLoader.getResourceAsStream(resourceName);
		if (inputStream == null) {
			throw new MalformedURLException("Could not stream resource '" + url + "' using class loader: " + contextClassLoader);
		}
		return inputStream;
	}


}
