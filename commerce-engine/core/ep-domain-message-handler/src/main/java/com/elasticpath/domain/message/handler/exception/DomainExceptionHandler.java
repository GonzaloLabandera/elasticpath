/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.exception;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implementation of {@link Processor} for processing of exceptions from Domain events consumers.
 */
public class DomainExceptionHandler implements Processor {

	private static final Logger LOGGER = LogManager.getLogger(DomainExceptionHandler.class);

	@Override
	public void process(final Exchange exchange) {
		LOGGER.error("Error processing: " + exchange.getMessage().getBody());
	}

}
