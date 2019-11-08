/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.webservice;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import com.elasticpath.catalog.entity.attribute.Attribute;
import com.elasticpath.catalog.entity.brand.Brand;
import com.elasticpath.catalog.entity.category.Category;
import com.elasticpath.catalog.entity.fieldmetadata.FieldMetadata;
import com.elasticpath.catalog.entity.offer.Offer;
import com.elasticpath.catalog.entity.option.Option;
import com.elasticpath.catalog.reader.FindAllResponse;
import com.elasticpath.catalog.webservice.request.entity.RequestBody;

/**
 * Rest endpoint service.
 */
public class CatalogWebServiceRestEndpointsRouteBuilder extends RouteBuilder {

	private static final String STRING_TYPE = "string";
	private static final String STORE_CODE = "storeCode";
	private static final String STORE_CODE_DESCRIPTION = "Store code";
	private static final String LIMIT = "limit";
	private static final String START_AFTER = "startAfter";
	private static final String INTEGER_TYPE = "integer";
	private static final String CONTENT_TYPE = "application/json";
	private static final String SYNDICATION_URI = "/syndication/v1/catalog";

	@Override
	public void configure() {
		restConfiguration().component("servlet").bindingMode(RestBindingMode.json)
				.apiContextPath(SYNDICATION_URI)
				.apiProperty("cors", "true");

		configureOptions();
		configureOffers();
		configureBrands();
		configureAttributes();
		configureFieldsMetadata();
		configureCategories();

		onException(Exception.class)
				.handled(true)
				.setHeader(Exchange.HTTP_RESPONSE_CODE, HttpStatus.BAD_REQUEST::value)
				.setBody(() -> StringUtils.EMPTY);
	}

	private void configureBrands() {
		rest(SYNDICATION_URI).description("Brand API")
				.consumes(CONTENT_TYPE)
				.produces(CONTENT_TYPE)
				.get("/{storeCode}/brands/{brandCode}")
					.param().name(STORE_CODE).type(RestParamType.path).description(STORE_CODE_DESCRIPTION).dataType(STRING_TYPE).endParam()
					.description("Read the latest version of an Brand").outType(Brand.class)
					.param().name("brandCode").type(RestParamType.path).description("Brand code").dataType(STRING_TYPE).endParam()
					.to("direct:getBrand")
				.get("/{storeCode}/brands")
					.param().name(STORE_CODE).type(RestParamType.path).description(STORE_CODE_DESCRIPTION).dataType(STRING_TYPE).endParam()
					.param().name(LIMIT).type(RestParamType.path).description("The number of Brands to return in a single request")
						.dataType(INTEGER_TYPE).endParam()
						.param().name(START_AFTER).type(RestParamType.path)
						.description("A pagination cursor that instructs the service to return results starting after the provided Brand")
						.dataType(STRING_TYPE).endParam()
						.description("Read the latest versions of all Brands available within a given Store with pagination")
							.outType(FindAllResponse.class)
					.to("direct:getBrands")
				.post("/{storeCode}/brandevents")
					.param().name(STORE_CODE).type(RestParamType.path).description(STORE_CODE_DESCRIPTION).dataType(STRING_TYPE).endParam()
					.type(RequestBody.class)
					.to("direct:brandevents");
	}

	private void configureOffers() {
		rest(SYNDICATION_URI).description("Offer API")
				.consumes(CONTENT_TYPE)
				.produces(CONTENT_TYPE)

				.get("/{storeCode}/offers/{attributeCode}")
					.param().name(STORE_CODE).type(RestParamType.path).description(STORE_CODE_DESCRIPTION).dataType(STRING_TYPE).endParam()
					.description("Read the latest version of an Offer").outType(Offer.class)
					.param().name("attributeCode").type(RestParamType.path).description("Attribute code").dataType(STRING_TYPE).endParam()
					.to("direct:getOffer")
				.get("/{storeCode}/offers")
					.param().name(STORE_CODE).type(RestParamType.path).description(STORE_CODE_DESCRIPTION).dataType(STRING_TYPE).endParam()
					.param().name(LIMIT).type(RestParamType.path).description("The number of Offers to return in a single request")
						.dataType(INTEGER_TYPE).endParam()
					.param().name(START_AFTER).type(RestParamType.path).endParam()
					.param().name(LIMIT).type(RestParamType.path).description("The number of Offers to return in a single request")
						.dataType(INTEGER_TYPE).endParam()
					.param().name(START_AFTER).type(RestParamType.path)
						.description("A pagination cursor that instructs the service to return results starting after the provided Offer")
						.dataType(STRING_TYPE).endParam()
						.description("Read the latest versions of all Offers available within a given Store with pagination")
						.outType(FindAllResponse.class)
					.to("direct:getOffers")
				.post("/{storeCode}/offerevents")
					.param().name(STORE_CODE).type(RestParamType.path).description(STORE_CODE_DESCRIPTION).dataType(STRING_TYPE).endParam()
					.type(RequestBody.class)
					.to("direct:offerevents");
	}

	private void configureAttributes() {
		rest(SYNDICATION_URI).description("Attribute API")
				.consumes(CONTENT_TYPE)
				.produces(CONTENT_TYPE)
				.get("/{storeCode}/attributes/{attributeCode}")
					.param().name(STORE_CODE).type(RestParamType.path).description(STORE_CODE_DESCRIPTION).dataType(STRING_TYPE).endParam()
					.description("Read the latest version of an Attribute").outType(Attribute.class)
					.param().name("attributeCode").type(RestParamType.path).description("Attribute code code").dataType(STRING_TYPE).endParam()
					.to("direct:getAttribute")
				.get("/{storeCode}/attributes")
					.param().name(STORE_CODE).type(RestParamType.path).description(STORE_CODE_DESCRIPTION).dataType(STRING_TYPE).endParam()
					.param().name(LIMIT).type(RestParamType.path).description("The number of Attributes to return in a single request")
						.dataType(INTEGER_TYPE).endParam()
					.param().name(START_AFTER).type(RestParamType.path)
						.description("A pagination cursor that instructs the service to return results starting after the provided Attribute")
						.dataType(STRING_TYPE).endParam()
						.description("Read the latest versions of all Attributes available within a given Store with pagination")
						.outType(FindAllResponse.class)
					.to("direct:getAttributes")
				.post("/{storeCode}/attributeevents")
					.param().name(STORE_CODE).type(RestParamType.path).description(STORE_CODE_DESCRIPTION).dataType(STRING_TYPE).endParam()
					.type(RequestBody.class)
					.to("direct:attributeevents");
	}

	private void configureFieldsMetadata() {
		rest(SYNDICATION_URI).description("FieldMetadata API")
				.consumes(CONTENT_TYPE)
				.produces(CONTENT_TYPE)
				.get("/{storeCode}/fieldmetadata/{fieldMetadataCode}")
					.param().name(STORE_CODE).type(RestParamType.path).description(STORE_CODE_DESCRIPTION).dataType(STRING_TYPE).endParam()
					.description("Read the latest version of a FieldMetadata").outType(FieldMetadata.class)
					.param().name("fieldMetadataCode").type(RestParamType.path).description("FieldMetadata code").dataType(STRING_TYPE).endParam()
					.to("direct:getFieldMetadata")
				.get("/{storeCode}/fieldmetadata")
					.param().name(STORE_CODE).type(RestParamType.path).description(STORE_CODE_DESCRIPTION).dataType(STRING_TYPE).endParam()
					.param().name(LIMIT).type(RestParamType.path).description("The number of FieldMetadata to return in a single request")
						.dataType(INTEGER_TYPE).endParam()
					.param().name(START_AFTER).type(RestParamType.path)
						.description("A pagination cursor that instructs the service to return results starting after the provided FieldMetadata")
						.dataType(STRING_TYPE).endParam()
					.description("Read the latest versions of all FieldMetadata available within a given Store with pagination")
						.outType(FindAllResponse.class)
					.to("direct:getFieldsMetadata")
				.post("/{storeCode}/fieldmetadataevents")
					.param().name(STORE_CODE).type(RestParamType.path).description(STORE_CODE_DESCRIPTION).dataType(STRING_TYPE).endParam()
					.type(RequestBody.class)
					.to("direct:fieldmetadataevents");
	}

	private void configureOptions() {
		rest(SYNDICATION_URI).description("Options API")
				.consumes(CONTENT_TYPE)
				.produces(CONTENT_TYPE)
				.get("/{storeCode}/options/{optionCode}")
					.param().name(STORE_CODE).type(RestParamType.path).description(STORE_CODE_DESCRIPTION).dataType(STRING_TYPE).endParam()
					.description("Read the latest version of an Option").outType(Option.class)
					.param().name("optionCode").type(RestParamType.path).description("Option code").dataType(STRING_TYPE).endParam()
					.to("direct:getOption")
				.get("/{storeCode}/options")
					.param().name(STORE_CODE).type(RestParamType.path).description(STORE_CODE_DESCRIPTION).dataType(STRING_TYPE).endParam()
					.param().name(LIMIT).type(RestParamType.path).description("The number of Options to return in a single request")
						.dataType(INTEGER_TYPE).endParam()
					.param().name(START_AFTER).type(RestParamType.path)
						.description("A pagination cursor that instructs the service to return results starting after the provided Option")
						.dataType(STRING_TYPE).endParam()
						.description("Read the latest versions of all Options available within a given Store with pagination")
						.outType(FindAllResponse.class)
					.to("direct:getOptions")
				.post("/{storeCode}/optionevents")
					.param().name(STORE_CODE).type(RestParamType.path).description(STORE_CODE_DESCRIPTION).dataType(STRING_TYPE).endParam()
					.type(RequestBody.class)
					.to("direct:optionevents");
	}

	private void configureCategories() {
		rest(SYNDICATION_URI).description("Categories API")
				.consumes(CONTENT_TYPE)
				.produces(CONTENT_TYPE)
				.get("/{storeCode}/categories/{categoryCode}")
					.param().name(STORE_CODE).type(RestParamType.path).description(STORE_CODE_DESCRIPTION).dataType(STRING_TYPE).endParam()
					.description("Read the latest version of a Category").outType(Category.class)
					.param().name("categoryCode").type(RestParamType.path).description("Category code").dataType(STRING_TYPE).endParam()
					.to("direct:getCategory")
				.get("/{storeCode}/categories/{categoryCode}/children")
					.param().name(STORE_CODE).type(RestParamType.path).description(STORE_CODE_DESCRIPTION).dataType(STRING_TYPE).endParam()
					.description("Read children of a Category")
					.param().name("categoryCode").type(RestParamType.path).description("Category code").dataType(STRING_TYPE).endParam()
					.to("direct:getChildren")
				.get("/{storeCode}/categories")
					.param().name(STORE_CODE).type(RestParamType.path).description(STORE_CODE_DESCRIPTION).dataType(STRING_TYPE).endParam()
					.param().name(LIMIT).type(RestParamType.path).description("The number of Categories to return in a single request")
						.dataType(INTEGER_TYPE).endParam()
					.param().name(START_AFTER).type(RestParamType.path)
						.description("A pagination cursor that instructs the service to return results starting after the provided Category")
						.dataType(STRING_TYPE).endParam()
						.description("Read the latest versions of all Categories available within a given Store with pagination")
						.outType(FindAllResponse.class)
					.to("direct:getCategories")
				.post("/{storeCode}/categoryevents")
					.param().name(STORE_CODE).type(RestParamType.path).description(STORE_CODE_DESCRIPTION).dataType(STRING_TYPE).endParam()
					.type(RequestBody.class)
					.to("direct:categoryevents");
	}

}
