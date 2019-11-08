/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.wiremock.servlet;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.util.Arrays;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.WireMockApp;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import com.github.tomakehurst.wiremock.servlet.NotImplementedContainer;

import org.apache.commons.lang3.StringUtils;
/**
 * Override of {@link com.github.tomakehurst.wiremock.servlet.WireMockWebContextListener} that parses a
 * list of extensions from the TRANSFORMER_CLASSES_KEY ServletContext init parameter and passes them
 * into ExtWarConfiguration.
 */
public class ExtWireMockWebContextListener implements ServletContextListener {

	private static final String APP_CONTEXT_KEY = "WireMockApp";
	private static final String TRANSFORMER_CLASSES_KEY = "transformerClasses";
	private static final String TRANSFORMER_CLASSES_SEP = ",";
	private static final String LOGGING_ENABLED_KEY = "verboseLoggingEnabled";
	private static final String LOGGING_ENABLED_DEFAULT_VALUE = "true";

	/**
	 * Override to setup the configuration and allow addition of extension transformer classes from web.xml parameter.
	 * @param sce	the ServletContextEvent to obtain the ServletContext to read the init parameters from.
	 */
	@Override
	public void contextInitialized(final ServletContextEvent sce) {
		final ServletContext context = sce.getServletContext();

		boolean verboseLoggingEnabled = Boolean.parseBoolean(
				firstNonNull(context.getInitParameter(LOGGING_ENABLED_KEY), LOGGING_ENABLED_DEFAULT_VALUE));

		final ExtWarConfiguration configuration = new ExtWarConfiguration(context)
				.extensions(new ResponseTemplateTransformer(false));


		if (!StringUtils.isEmpty(context.getInitParameter(TRANSFORMER_CLASSES_KEY))) {
		  Arrays.stream(context.getInitParameter(TRANSFORMER_CLASSES_KEY).split(TRANSFORMER_CLASSES_SEP))
			.forEach(configuration::extensions);
		}

		WireMockApp wireMockApp = new WireMockApp(configuration, new NotImplementedContainer());

		context.setAttribute(APP_CONTEXT_KEY, wireMockApp);
		context.setAttribute(StubRequestHandler.class.getName(), wireMockApp.buildStubRequestHandler());
		context.setAttribute(AdminRequestHandler.class.getName(), wireMockApp.buildAdminRequestHandler());
		context.setAttribute(Notifier.KEY, new Slf4jNotifier(verboseLoggingEnabled));
	}

	/**
	 * Does nothing.
	 * @param sce servlet context event
	 */
	@Override
	public void contextDestroyed(final ServletContextEvent sce) {
		// empty body
	}

}