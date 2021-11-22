/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.insights.api.processors;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.elasticpath.insights.OperationalInsightsConstants;

/**
 * Create "ServiceRequestEndpoints" header using zoom header values.
 */
public class ZoomHeaderToServiceRequestEndpointsHeaderProcessor implements Processor {
	private static final String HEADER_SERVICE_REQUEST_ENDPOINTS = "ServiceRequestEndpoints";

	@SuppressWarnings({"unchecked"})
	@Override
	public void process(final Exchange exchange) {
		Set<String> zoomsHeader = exchange.getIn().getHeader(OperationalInsightsConstants.HEADER_ZOOM, Set.class);
		exchange.getIn().setHeader(HEADER_SERVICE_REQUEST_ENDPOINTS, zoomsHeader.stream()
				.filter(value -> Arrays.asList(OperationalInsightsConstants.ZOOM_CONFIGURATION, OperationalInsightsConstants.ZOOM_RUNTIME)
						.contains(value))
				.map(value -> "jms:topic:ep.insights." + value)
				.collect(Collectors.toSet()));
	}
}
