/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.webservice;

import java.util.Collections;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.http.HttpStatus;

import com.elasticpath.catalog.reader.FindAllResponse;
import com.elasticpath.catalog.webservice.request.entity.RequestBody;
import com.elasticpath.catalog.webservice.request.validator.RequestBodyValidator;
import com.elasticpath.catalog.webservice.services.BrandService;

/**
 * A Brand rest endpoints route builder.
 */
public class BrandWebServiceRouteBuilder extends RouteBuilder {

	private static final String REQUESTED_CODES_AMOUNT = "requestedCodesAmount";
	private static final String EMPTY_STRING = "";
	private final BrandService brandService;
	private final RequestBodyValidator requestBodyValidator;

	/**
	 * Constructor.
	 *
	 * @param brandService is service for reading {@link com.elasticpath.catalog.entity.brand.Brand}.
	 * @param requestBodyValidator validator of POST request body.
	 */
	public BrandWebServiceRouteBuilder(final BrandService brandService, final RequestBodyValidator requestBodyValidator) {
		this.brandService = brandService;
		this.requestBodyValidator = requestBodyValidator;
	}

	@Override
	public void configure() {
		onException(Exception.class)
				.handled(true)
				.setHeader(Exchange.HTTP_RESPONSE_CODE, HttpStatus.BAD_REQUEST::value)
				.setBody(() -> EMPTY_STRING);

		from("direct:getBrand")
				.bean(brandService, "get")
				.choice()
					.when(body().method("isPresent"))
						.setBody(body().method("get"))
				.endChoice()
				.otherwise()
					.setBody(constant(null))
					.setHeader(Exchange.HTTP_RESPONSE_CODE, HttpStatus.NOT_FOUND::value);

		from("direct:getBrands")
				.bean(brandService, "getAllBrands")
				.transform()
				.body(FindAllResponse.class, response -> response);

		from("direct:brandevents")
				.validate(exchange -> requestBodyValidator.validate(exchange.getIn().getBody(RequestBody.class)))
				.validate(body().method("getEventType.getEventClass").isEqualTo("CatalogEventType"))
				.validate(body().method("getEventType.getName").isEqualTo("BRANDS_UPDATED"))
				.validate(body().method("getGuid").isEqualTo("AGGREGATE"))
				.validate(body().method("getData.get(type)").isEqualTo("brand"))
				.validate(body().method("getData.get(store)").isEqualToIgnoreCase(header("storeCode")))
				.setProperty(REQUESTED_CODES_AMOUNT, body().method("getData.get(codes).size"))
				.bean(brandService, "getLatestBrandsWithCodes")
				.choice()
					.when(body().method("size").isNotEqualTo(exchangeProperty(REQUESTED_CODES_AMOUNT)))
						.setHeader(Exchange.HTTP_RESPONSE_CODE, HttpStatus.PARTIAL_CONTENT::value)
				.end()
				.setBody(exchange -> Collections.singletonMap("results", exchange.getIn().getBody()));
	}

}
