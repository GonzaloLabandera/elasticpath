/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.commons.handlers.libjar;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Optional;

/**
 * {@link java.net.URLStreamHandlerFactory} implementation supporting lib-jar URL protocol.
 */
public class LibJarURLStreamHandlerFactory implements URLStreamHandlerFactory {
	private final URLStreamHandlerFactory existedFactory;

	/**
	 * Constructor.
	 * Used in case there is no existing URLStreamHandlerFactory defined.
	 */
	public LibJarURLStreamHandlerFactory() {
		this(null);
	}

	/**
	 * Constructor.
	 * Used in case there is an existing URLStreamHandlerFactory defined.
	 *
	 * @param existedFactory existed URL stream handler factory
	 */
	public LibJarURLStreamHandlerFactory(final URLStreamHandlerFactory existedFactory) {
		this.existedFactory = existedFactory;
	}

	@Override
	public URLStreamHandler createURLStreamHandler(final String protocol) {
		return LibJarUtil.LIB_JAR_PROTOCOL.equals(protocol)
				? new LibJarURLStreamHandler()
				: Optional.ofNullable(existedFactory)
				.map(factory -> factory.createURLStreamHandler(protocol))
				.orElse(null);
	}
}
