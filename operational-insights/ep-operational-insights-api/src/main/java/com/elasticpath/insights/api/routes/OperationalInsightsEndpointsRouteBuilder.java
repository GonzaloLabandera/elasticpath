/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.insights.api.routes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.naming.ConfigurationException;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestParamType;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import com.elasticpath.insights.OperationalInsightsConstants;
import com.elasticpath.insights.api.exceptions.EpInvalidRequest;
import com.elasticpath.insights.api.processors.ZoomHeaderStringToSetProcessor;
import com.elasticpath.insights.api.processors.ZoomHeaderToServiceRequestEndpointsHeaderProcessor;
import com.elasticpath.insights.service.InsightsService;

/**
 * Operational Insights API rest endpoint route builder.
 */
public class OperationalInsightsEndpointsRouteBuilder extends RouteBuilder {

	private static final String CONTENT_TYPE = "application/json";
	private static final String HEADER_INSIGHTS_CORRELATION_ID = "InsightsCorrelationId";
	private static final String HEADER_SERVICE_REQUEST_ENDPOINTS = "ServiceRequestEndpoints";
	private static final int WAIT_FOR_SERVICE_REPLIES_MS = 2000;
	private static final String REST_CONTEXT_PATH = "/insights";
	private static final String REST_CONTEXT_DESCRIPTION = "Operational Insights";
	private static final String REST_REQUEST_ENDPOINT_PATH = "/request";
	private static final String REST_REQUEST_ENDPOINT_DESCRIPTION = "Request Operational Insights report";
	private static final String REST_REPORT_ENDPOINT_PATH = "/report";
	private static final String REST_REPORT_ENDPOINT_DESCRIPTION = "Retrieve Operational Insights report";
	private static final String DATA_TYPE_STRING = "string";

	private final Map<String, Set<JSONObject>> configurationReplies = new HashMap<>();
	private final Map<String, Set<JSONObject>> runtimeReplies = new HashMap<>();
	private final Map<String, Set<String>> zooms = new HashMap<>();
	private InsightsService insightsService;

	@Override
	@SuppressWarnings({"unchecked"})
	public void configure() {
		onException(EpInvalidRequest.class, ConfigurationException.class)
				.handled(true)
				.setHeader(Exchange.HTTP_RESPONSE_CODE, HttpStatus.BAD_REQUEST::value)
				.transform(simple("${exception.message}"));

		onException(Exception.class)
				.setHeader(Exchange.HTTP_RESPONSE_CODE, HttpStatus.INTERNAL_SERVER_ERROR::value);

		rest(REST_CONTEXT_PATH)
				.description(REST_CONTEXT_DESCRIPTION)
				.consumes(CONTENT_TYPE)
				.produces(CONTENT_TYPE)
				.get(REST_REQUEST_ENDPOINT_PATH)
				.description(REST_REQUEST_ENDPOINT_DESCRIPTION)
				.param()
					.name(OperationalInsightsConstants.HEADER_ZOOM)
					.type(RestParamType.query)
					.description("The zoom to use to produce the report")
					.dataType(DATA_TYPE_STRING)
					.required(false)
					.endParam()
				.to("direct:operationalInsightsRequest");

		from("direct:operationalInsightsRequest")
				.errorHandler(noErrorHandler())
				.setExchangePattern(ExchangePattern.InOnly)
				.setHeader(HEADER_INSIGHTS_CORRELATION_ID, () -> UUID.randomUUID().toString())
				.process(new ZoomHeaderStringToSetProcessor())
				.process(new ZoomHeaderToServiceRequestEndpointsHeaderProcessor())
				.process(exchange -> {
					String insightsCorrelationIdHeader = exchange.getIn().getHeader(HEADER_INSIGHTS_CORRELATION_ID, String.class);
					Set<String> zoomsHeader = exchange.getIn().getHeader(OperationalInsightsConstants.HEADER_ZOOM, Set.class);
					zooms.put(insightsCorrelationIdHeader, zoomsHeader);
				})
				.recipientList(header(HEADER_SERVICE_REQUEST_ENDPOINTS))
				.delay(WAIT_FOR_SERVICE_REPLIES_MS)
				.setHeader("Location", simple("report?reportId=${header.InsightsCorrelationId}"))
				.setHeader(Exchange.HTTP_RESPONSE_CODE, HttpStatus.TEMPORARY_REDIRECT::value);

		from("jms:ep.insights.configuration.reply")
				.errorHandler(noErrorHandler())
				.process(exchange -> {
					String correlationId = exchange.getIn().getHeader(HEADER_INSIGHTS_CORRELATION_ID, String.class);
					Set<JSONObject> sections = configurationReplies.getOrDefault(correlationId, new HashSet<>());
					if (exchange.getIn().getBody() != null) {
						JSONObject jsonObject = new JSONObject(exchange.getIn().getBody(String.class));
						sections.add(jsonObject);
					}
					configurationReplies.put(exchange.getIn().getHeader(HEADER_INSIGHTS_CORRELATION_ID, String.class), sections);
				});

		from("jms:ep.insights.runtime.reply")
				.errorHandler(noErrorHandler())
				.process(exchange -> {
					String correlationId = exchange.getIn().getHeader(HEADER_INSIGHTS_CORRELATION_ID, String.class);
					Set<JSONObject> sections = runtimeReplies.getOrDefault(correlationId, new HashSet<>());
					if (exchange.getIn().getBody() != null) {
						JSONObject jsonObject = new JSONObject(exchange.getIn().getBody(String.class));
						sections.add(jsonObject);
					}
					runtimeReplies.put(exchange.getIn().getHeader(HEADER_INSIGHTS_CORRELATION_ID, String.class), sections);
				});

		rest(REST_CONTEXT_PATH)
				.description(REST_CONTEXT_DESCRIPTION)
				.consumes(CONTENT_TYPE)
				.produces(CONTENT_TYPE)
				.get(REST_REPORT_ENDPOINT_PATH)
				.description(REST_REPORT_ENDPOINT_DESCRIPTION)
				.param()
					.name(OperationalInsightsConstants.HEADER_REPORT_ID)
					.type(RestParamType.query)
					.description("The id of the report to download")
					.dataType(DATA_TYPE_STRING)
					.required(true)
					.endParam()
				.to("direct:downloadReport");

		from("direct:downloadReport")
				.process(exchange -> {
					log.debug("reportId: {}", exchange.getIn().getHeader(OperationalInsightsConstants.HEADER_REPORT_ID));
					String reportId = exchange.getIn().getHeader(OperationalInsightsConstants.HEADER_REPORT_ID, String.class);
					exchange.getIn().setBody(insightsService.getReport(zooms.get(reportId),
							configurationReplies.get(reportId), runtimeReplies.get(reportId)));
				});

	}

	protected InsightsService getInsightsService() {
		return insightsService;
	}

	public void setInsightsService(final InsightsService insightsService) {
		this.insightsService = insightsService;
	}
}
