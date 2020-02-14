/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.service.provider;

import static com.elasticpath.commons.handlers.libjar.LibJarURLStreamHandlerService.LIB_JAR_PROTOCOL;
import static com.elasticpath.commons.handlers.libjar.LibJarURLStreamHandlerService.parsePath;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import com.elasticpath.commons.handlers.libjar.LibJarURLConnection;

/**
 * {@link URLStreamHandler} implementation supporting jar-in-jar URL streaming.
 */
class LibJarURLStreamHandler extends URLStreamHandler {

	@Override
	protected URLConnection openConnection(final URL url) {
		return new LibJarURLConnection(url);
	}

	@Override
	protected void parseURL(final URL url, final String spec, final int start, final int limit) {
		setURL(url, LIB_JAR_PROTOCOL, "", -1, null, null, parsePath(spec), null, null);
	}

}
