/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.importexport.api.routes;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.http.HttpStatus;

import com.elasticpath.importexport.api.exceptions.EpInvalidRequest;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.ql.parser.SearchExecutionException;
import com.elasticpath.service.changeset.ChangeSetPolicyException;

/**
 * Import/Export API rest endpoint route builder.
 */
public class ImportExportEndpointRouteBuilder extends RouteBuilder {

	private static final String CONTENT_TYPE = "application/xml";
	private static final String CONTEXT_PATH = "/importexport";
	private static final String PARAM_TYPE = "type";
	private static final String PARAM_TYPE_DESC = "Entity type to export";
	private static final String PARAM_PARENT_TYPE = "parentType";
	private static final String PARAM_PARENT_TYPE_DESC = "Parent entity type to use when running query in order to determine related type records to"
			+ " export";
	private static final String PARAM_QUERY = "query";
	private static final String PARAM_QUERY_DESC = "EPQL defining which entities to export";
	private static final String PARAM_CHANGESET_GUID = "changesetGuid";
	private static final String PARAM_CHANGESET_GUID_DESC = "GUID of the changeset to populate when importing";
	private static final String DATA_TYPE_STRING = "string";

	@Override
	@SuppressWarnings({"unchecked"})
	public void configure() {
		rest(CONTEXT_PATH).description("Import/Export API")
				.consumes(CONTENT_TYPE)
				.produces(CONTENT_TYPE)

				.get("/export")
				.description("Export eCommerce XML entities")
				.param()
					.name(PARAM_TYPE)
					.type(RestParamType.query)
					.description(PARAM_TYPE_DESC)
					.dataType(DATA_TYPE_STRING)
					.required(true)
					.endParam()
				.param()
					.name(PARAM_PARENT_TYPE)
					.type(RestParamType.query)
					.description(PARAM_PARENT_TYPE_DESC)
					.dataType(DATA_TYPE_STRING)
					.required(false)
					.endParam()
				.param().name(PARAM_QUERY)
					.type(RestParamType.query)
					.description(PARAM_QUERY_DESC)
					.dataType(DATA_TYPE_STRING)
					.required(false)
					.endParam()
				.to("direct:exportRequest")

				.post("/import")
				.description("Import eCommerce XML entities")
				.param()
					.name(PARAM_CHANGESET_GUID)
					.type(RestParamType.query)
					.description(PARAM_CHANGESET_GUID_DESC)
					.dataType(DATA_TYPE_STRING)
					.required(false)
					.endParam()
				.to("direct:importRequest");

		onException(EpInvalidRequest.class, ConfigurationException.class, ChangeSetPolicyException.class)
				.handled(true)
				.setHeader(Exchange.HTTP_RESPONSE_CODE, HttpStatus.BAD_REQUEST::value)
				.transform(simple("${exception.message}"));

		onException(SearchExecutionException.class)
				.handled(true)
				.setHeader(Exchange.HTTP_RESPONSE_CODE, HttpStatus.BAD_REQUEST::value)
				.transform(simple("${exception.cause.message}"));

		onException(Exception.class)
				.setHeader(Exchange.HTTP_RESPONSE_CODE, HttpStatus.INTERNAL_SERVER_ERROR::value);
	}

}
