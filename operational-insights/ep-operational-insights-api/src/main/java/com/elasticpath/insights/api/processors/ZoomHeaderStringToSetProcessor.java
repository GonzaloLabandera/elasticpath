/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.insights.api.processors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.StringUtils;

import com.elasticpath.insights.OperationalInsightsConstants;

/**
 * Convert zoom header from String to Set<String>, splitting comma-separated values.
 */
public class ZoomHeaderStringToSetProcessor implements Processor {
	@Override
	public void process(final Exchange exchange) {
		String zoomHeader = exchange.getIn().getHeader(OperationalInsightsConstants.HEADER_ZOOM, String.class);
		Set<String> zoomsSet;
		if (zoomHeader == null) {
			zoomsSet = new HashSet<>(Arrays.asList(OperationalInsightsConstants.ZOOM_CONFIGURATION, OperationalInsightsConstants.ZOOM_RUNTIME,
					OperationalInsightsConstants.ZOOM_DATA_SHAPE));
		} else {
			zoomsSet = new HashSet<>(Arrays.asList(StringUtils.split(zoomHeader, ",")));
		}
		exchange.getIn().setHeader(OperationalInsightsConstants.HEADER_ZOOM, zoomsSet);
	}
}
