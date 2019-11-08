/*
 * Copyright (c) Elastic Path Software Inc., 2019
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
import com.elasticpath.catalog.webservice.services.CategoryService;

/**
 * A rout builder for category rest endpoints.
 */
public class CategoryWebServiceRouteBuilder extends RouteBuilder {

	private static final String REQUESTED_CODES_AMOUNT = "requestedCodesAmount";
	private final CategoryService categoryService;
	private final RequestBodyValidator requestBodyValidator;

	/**
	 * Constructor.
	 *
	 * @param categoryService is service for reading {@link com.elasticpath.catalog.entity.category.Category}.
	 * @param requestBodyValidator validator of POST request body.
	 */
	public CategoryWebServiceRouteBuilder(final CategoryService categoryService, final RequestBodyValidator requestBodyValidator) {
		this.categoryService = categoryService;
		this.requestBodyValidator = requestBodyValidator;
	}

	@Override
	public void configure() throws Exception {
		onException(Exception.class)
				.handled(true)
				.setHeader(Exchange.HTTP_RESPONSE_CODE, HttpStatus.BAD_REQUEST::value)
				.setBody(() -> StringUtils.EMPTY);

		from("direct:getCategory")
				.bean(categoryService, "get")
				.choice()
					.when(body().method("isPresent"))
						.setBody(body().method("get"))
				.endChoice()
				.otherwise()
					.setBody(constant(null))
					.setHeader(Exchange.HTTP_RESPONSE_CODE, HttpStatus.NOT_FOUND::value);

		from("direct:getChildren")
				.bean(categoryService, "getChildren")
				.choice()
					.when(body().isNotNull())
						.setBody(exchange -> Collections.singletonMap("results", exchange.getIn().getBody()))
					.endChoice()
					.otherwise()
						.setBody(constant(null))
						.setHeader(Exchange.HTTP_RESPONSE_CODE, HttpStatus.NOT_FOUND::value);

		from("direct:getCategories")
				.bean(categoryService, "getAllCategories")
				.transform()
				.body(FindAllResponse.class, response -> response);

		from("direct:categoryevents")
				.validate(exchange -> requestBodyValidator.validate(exchange.getIn().getBody(RequestBody.class)))
				.validate(body().method("getEventType.getEventClass").isEqualTo("CatalogEventType"))
				.validate(body().method("getEventType.getName").isEqualTo("CATEGORIES_UPDATED"))
				.validate(body().method("getGuid").isEqualTo("AGGREGATE"))
				.validate(body().method("getData.get(type)").isEqualTo("category"))
				.validate(body().method("getData.get(store)").isEqualToIgnoreCase(header("storeCode")))
				.setProperty(REQUESTED_CODES_AMOUNT, body().method("getData.get(codes).size"))
				.bean(categoryService, "getLatestCategoriesWithCodes")
				.choice()
					.when(body().method("size").isNotEqualTo(exchangeProperty(REQUESTED_CODES_AMOUNT)))
						.setHeader(Exchange.HTTP_RESPONSE_CODE, HttpStatus.PARTIAL_CONTENT::value)
				.end()
				.setBody(exchange -> Collections.singletonMap("results", exchange.getIn().getBody()));
	}

}
