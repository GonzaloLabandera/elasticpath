/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.commons.handlers.osgi;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

import com.elasticpath.commons.handlers.libjar.LibJarURLStreamHandlerService;

/**
 * Bundle activator for registering {@link LibJarURLStreamHandlerService}.
 */
public class Activator implements BundleActivator {

	private ServiceRegistration<URLStreamHandlerService> registration;

	@SuppressWarnings({"squid:S1149"})
	@Override
	public void start(final BundleContext context) {
		final Hashtable<String, String[]> properties = new Hashtable<>();
		properties.put(URLConstants.URL_HANDLER_PROTOCOL, new String[]{LibJarURLStreamHandlerService.LIB_JAR_PROTOCOL});
		registration = context.registerService(URLStreamHandlerService.class, new LibJarURLStreamHandlerService(), properties);
	}

	@Override
	public void stop(final BundleContext context) {
		registration.unregister();
	}

}
