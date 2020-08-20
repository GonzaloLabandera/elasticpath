/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.importexport.api.aggregations;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.commons.lang3.ObjectUtils;

import com.elasticpath.importexport.common.summary.SummaryLogger;

/**
 * Aggregate multiple exchanges that return summary objects into an exchange containing a combined summary object.
 */
public class SummaryAggregationStrategy implements AggregationStrategy {
	@Override
	public Exchange aggregate(final Exchange oldExchange, final Exchange newExchange) {
		if (newExchange == null && oldExchange == null) {
			return null;
		}

		if (oldExchange == null || newExchange == null) {
			return ObjectUtils.firstNonNull(newExchange, oldExchange);
		}

		propagateException(oldExchange, newExchange);
		if (newExchange.getException() != null) {
			return newExchange;
		}

		combineSummaryObjects(oldExchange, newExchange);
		return newExchange;
	}

	private void combineSummaryObjects(final Exchange oldExchange, final Exchange newExchange) {
		SummaryLogger oldSummary = oldExchange.getIn().getBody(SummaryLogger.class);
		SummaryLogger newSummary = newExchange.getIn().getBody(SummaryLogger.class);
		oldSummary.getFailures().forEach(newSummary::addFailure);
		oldSummary.getWarnings().forEach(newSummary::addWarning);
		oldSummary.getComments().forEach(newSummary::addComment);
		newSummary.addFailedDtos(oldSummary.getFailedDtos());
	}

	private void propagateException(final Exchange oldExchange, final Exchange newExchange) {
		// propagate exception from old exchange if there isn't already an exception
		if (newExchange.getException() == null) {
			newExchange.setException(oldExchange.getException());
			newExchange.setProperty(Exchange.FAILURE_ENDPOINT, oldExchange.getProperty(Exchange.FAILURE_ENDPOINT));
		}
	}

}
