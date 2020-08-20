/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.importexport.api.routes;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.spi.DataFormat;

import com.elasticpath.importexport.api.aggregations.SummaryAggregationStrategy;
import com.elasticpath.importexport.api.converters.SummaryConverter;
import com.elasticpath.importexport.api.models.SummaryDto;

/**
 * Import/Export API import route builder.
 */
public class ImportRouteBuilder extends RouteBuilder {
	private SummaryAggregationStrategy summaryAggregationStrategy;
	private SummaryConverter summaryConverter;

	@Override
	public void configure() throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(SummaryDto.class);
		DataFormat jaxbDataFormat = new JaxbDataFormat(jaxbContext);

		from("direct:importRequest")
				.streamCaching()
				.errorHandler(noErrorHandler())
				.recipientList(constant("bean:importApiServiceChangeSetStage,bean:importApiServiceProcessStage"))
				.aggregationStrategy(summaryAggregationStrategy)
				.bean(summaryConverter, "convert")
				.marshal(jaxbDataFormat);
	}

	protected SummaryAggregationStrategy getSummaryAggregationStrategy() {
		return summaryAggregationStrategy;
	}

	public void setSummaryAggregationStrategy(final SummaryAggregationStrategy summaryAggregationStrategy) {
		this.summaryAggregationStrategy = summaryAggregationStrategy;
	}

	protected SummaryConverter getSummaryConverter() {
		return summaryConverter;
	}

	public void setSummaryConverter(final SummaryConverter summaryConverter) {
		this.summaryConverter = summaryConverter;
	}
}
