/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.importexport.api.routes;

import org.apache.camel.builder.RouteBuilder;

import com.elasticpath.importexport.api.services.ExportAPIService;

/**
 * Import/Export API export route builder.
 */
public class ExportRouteBuilder extends RouteBuilder {

	private final ExportAPIService apiService;

	/**
	 * Constructor.
	 * @param apiService the export API service
	 */
	public ExportRouteBuilder(final ExportAPIService apiService) {
		this.apiService = apiService;
	}

	@Override
	public void configure() {
		from("direct:exportRequest")
				.errorHandler(noErrorHandler())
				.bean(apiService, "doExport");
	}

}
