/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.webservice;

import java.util.Collections;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import com.elasticpath.catalog.reader.FindAllResponse;
import com.elasticpath.catalog.webservice.request.entity.RequestBody;
import com.elasticpath.catalog.webservice.request.validator.RequestBodyValidator;
import com.elasticpath.catalog.webservice.services.AttributeService;

/**
 * A rout builder for attribute rest endpoints.
 */
public class AttributeWebServiceRouteBuilder extends RouteBuilder {

	private static final String REQUESTED_CODES_AMOUNT = "requestedCodesAmount";
	private final AttributeService attributeService;
	private final RequestBodyValidator requestBodyValidator;

	/**
	 * Constructor.
	 *
	 * @param attributeService is service for reading {@link com.elasticpath.catalog.entity.attribute.Attribute}.
	 * @param requestBodyValidator validator of POST request body.
	 */
	public AttributeWebServiceRouteBuilder(final AttributeService attributeService, final RequestBodyValidator requestBodyValidator) {
		this.attributeService = attributeService;
		this.requestBodyValidator = requestBodyValidator;
	}

	@Override
	public void configure() throws Exception {
		onException(Exception.class)
				.handled(true)
				.setHeader(Exchange.HTTP_RESPONSE_CODE, HttpStatus.BAD_REQUEST::value)
				.setBody(() -> StringUtils.EMPTY);

		from("direct:getAttribute")
				.bean(attributeService, "get")
				.choice()
					.when(body().method("isPresent"))
						.setBody(body().method("get"))
				.endChoice()
				.otherwise()
					.setBody(constant(null))
					.setHeader(Exchange.HTTP_RESPONSE_CODE, HttpStatus.NOT_FOUND::value);

		from("direct:getAttributes")
				.bean(attributeService, "getAllAttributes")
				.transform()
				.body(FindAllResponse.class, response -> response);

		from("direct:attributeevents")
				.validate(exchange -> requestBodyValidator.validate(exchange.getIn().getBody(RequestBody.class)))
				.validate(body().method("getEventType.getEventClass").isEqualTo("CatalogEventType"))
				.validate(body().method("getEventType.getName").isEqualTo("ATTRIBUTES_UPDATED"))
				.validate(body().method("getGuid").isEqualTo("AGGREGATE"))
				.validate(body().method("getData.get(type)").isEqualTo("attribute"))
				.validate(body().method("getData.get(store)").isEqualToIgnoreCase(header("storeCode")))
				.setProperty(REQUESTED_CODES_AMOUNT, body().method("getData.get(codes).size"))
				.bean(attributeService, "getLatestAttributesWithCodes")
				.choice()
					.when(body().method("size").isNotEqualTo(exchangeProperty(REQUESTED_CODES_AMOUNT)))
						.setHeader(Exchange.HTTP_RESPONSE_CODE, HttpStatus.PARTIAL_CONTENT::value)
				.end()
				.setBody(exchange -> Collections.singletonMap("results", exchange.getIn().getBody()));
	}
}
