/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.insights.messaging;

import org.apache.camel.Endpoint;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

import com.elasticpath.insights.service.InsightsService;

/**
 * Operational Insights JMS message consumer route builder.
 */
public class OperationalInsightsMessageConsumerRouteBuilder extends RouteBuilder {

	private Endpoint configurationRequestEndpoint;

	private Endpoint runtimeRequestEndpoint;

	private InsightsService insightsService;

	/**
	 * Fetches hold resolution messages from message broker.
	 */
	@Override
	public void configure() {
		from(configurationRequestEndpoint)
				.errorHandler(noErrorHandler())
				.log(LoggingLevel.INFO, "Sending configuration reply")
				.bean(insightsService, "getThisEpServiceConfiguration")
				.to("jms:ep.insights.configuration.reply");

		from(runtimeRequestEndpoint)
				.errorHandler(noErrorHandler())
				.log(LoggingLevel.INFO, "Sending runtime reply")
				.bean(insightsService, "getThisEpServiceRuntime")
				.to("jms:ep.insights.runtime.reply");
	}

	protected Endpoint getConfigurationRequestEndpoint() {
		return configurationRequestEndpoint;
	}

	public void setConfigurationRequestEndpoint(final Endpoint configurationRequestEndpoint) {
		this.configurationRequestEndpoint = configurationRequestEndpoint;
	}

	protected Endpoint getRuntimeRequestEndpoint() {
		return runtimeRequestEndpoint;
	}

	public void setRuntimeRequestEndpoint(final Endpoint runtimeRequestEndpoint) {
		this.runtimeRequestEndpoint = runtimeRequestEndpoint;
	}

	protected InsightsService getInsightsService() {
		return insightsService;
	}

	public void setInsightsService(final InsightsService insightsService) {
		this.insightsService = insightsService;
	}
}
