/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.performancetools.queryanalyzer;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * OSGi bundle activator.
 * <p>
 * Logback TRACE level is automatically enabled upon bundle start, for openjpa,
 * com.elasticpath.rest.resource.dispatch.operator.AnnotatedMethodDispatcher and
 * com.elasticpath.rest.resource.dispatch.linker.ResourceLinkerImpl.
 * <p>
 * Also, reference to cortex.log file is obtained from Logback configuration.
 */
public class QueryAnalyzerBundleActivator implements BundleActivator {

	@Override
	public void start(final BundleContext bundleContext) throws Exception {
		QueryAnalyzerConfigurator.INSTANCE
				.init();
	}

	@Override
	public void stop(final BundleContext bundleContext) throws Exception {
		LogParser.INSTANCE
				.printConfiguration()
				.restoreLogLevels()
				.parse()
				.generateStatistics();
	}
}
