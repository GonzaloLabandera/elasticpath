/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.bulk.exception;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

/**
 * Implementation of {@link Processor} for processing of exceptions from Bulk events consumers.
 */
public class BulkExceptionHandler implements Processor {

	private static final Logger LOGGER = Logger.getLogger(BulkExceptionHandler.class);

	@Override
	public void process(final Exchange exchange) {
		LOGGER.error("Error processing bulk event: " + exchange.getMessage().getBody());
	}

}
