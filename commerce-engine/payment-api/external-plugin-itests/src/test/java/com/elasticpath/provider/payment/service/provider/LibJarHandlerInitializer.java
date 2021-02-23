/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.provider.payment.service.provider;

import java.net.URL;

import com.elasticpath.commons.handlers.libjar.LibJarURLStreamHandlerService;

/**
 * Registers the LibJarURLStreamHandler with the JVM, ensuring the registration
 * is done only once.
 */
public final class LibJarHandlerInitializer {

    private static boolean initialized = false;

    private LibJarHandlerInitializer() {
        // Prevent construction
    }

    /**
     * Initialize the jar protocol handler, ensuring we only register it once.
     */
    public static void initialize() {
        synchronized (LibJarHandlerInitializer.class) {
            if (initialized) {
                return;  // Prevent re-initialization
            }
            URL.setURLStreamHandlerFactory(protocol -> {
                if (LibJarURLStreamHandlerService.LIB_JAR_PROTOCOL.equals(protocol)) {
                    initialized = true;
                    return new LibJarURLStreamHandler();
                }
                return null;
            });
        }
    }
}
