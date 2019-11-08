/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.wiremock.examples.jackson.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * An example data transfer object that shows how to build a Jackson mapping artifact
 * which allows Jackson to convert a POJO into a JSON object.
 *
 * This particular DTO is used to represent a single record in a response List.
 *
 * This POJO is serialized from POJO -> JSON in {@link com.elasticpath.wiremock.examples.jackson.DynamicJacksonResponseDefinitionTransformer}.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("PMD.ShortVariable")
public class DynamicJacksonResponseLine {
	private String id;
	private Double doubleVal;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public Double getDoubleVal() {
		return doubleVal;
	}

	public void setDoubleVal(final Double doubleVal) {
		this.doubleVal = doubleVal;
	}
}
