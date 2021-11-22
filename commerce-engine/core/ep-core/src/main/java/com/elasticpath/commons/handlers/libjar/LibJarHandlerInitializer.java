/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.commons.handlers.libjar;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLStreamHandlerFactory;

/**
 * Set custom {@link URLStreamHandlerFactory} factory to register the LibJarURLStreamHandler with the JVM.
 */
public final class LibJarHandlerInitializer {

	private LibJarHandlerInitializer() {
		// Prevent construction.
	}

	/**
	 * Initialize {@link LibJarURLStreamHandler} the lib-jar protocol handler.
	 *
	 * @throws NoSuchFieldException if necessary
	 * @throws IllegalAccessException if necessary
	 */
	public static void initialize() throws NoSuchFieldException, IllegalAccessException {

		final Field factoryField = URL.class.getDeclaredField("factory");
		factoryField.setAccessible(true);
		final URLStreamHandlerFactory urlStreamHandlerFactory = (URLStreamHandlerFactory) factoryField.get(null);

		//Add URLStreamHandlerFactory to register lib-jar protocol handler if it was not added before.
		if (urlStreamHandlerFactory == null) {
			URL.setURLStreamHandlerFactory(new LibJarURLStreamHandlerFactory());
		} else {
			//Reset existed URLStreamHandlerFactory to register lib-jar protocol handler.
			final Field lockField = URL.class.getDeclaredField("streamHandlerLock");
			lockField.setAccessible(true);
			synchronized (lockField.get(null)) {
				factoryField.set(null, null);
				URL.setURLStreamHandlerFactory(new LibJarURLStreamHandlerFactory(urlStreamHandlerFactory));
			}
		}
	}
}
