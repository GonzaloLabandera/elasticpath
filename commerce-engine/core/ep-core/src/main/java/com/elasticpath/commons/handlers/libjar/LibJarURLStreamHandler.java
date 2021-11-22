/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.commons.handlers.libjar;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * {@link URLStreamHandler} implementation supporting jar-in-jar URL streaming.
 */
public class LibJarURLStreamHandler extends URLStreamHandler {
	@Override
	protected URLConnection openConnection(final URL url) {
		return new LibJarURLConnection(url);
	}

	@Override
	protected void parseURL(final URL url, final String spec, final int start, final int limit) {
		setURL(url, LibJarUtil.LIB_JAR_PROTOCOL, "", -1, null, null, LibJarUtil.parsePath(spec), null, null);
	}

}
