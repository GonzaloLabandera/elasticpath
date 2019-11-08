/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.wiremock.examples.jackson.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * An example data transfer object that shows how to build a Jackson mapping artifact
 * which allows Jackson to convert a JSON object into a POJO.
 *
 * This particular DTO is used to represent a single record in a request array.
 *
 * This POJO is deserialized from JSON -> POJO in {@link com.elasticpath.wiremock.examples.jackson.DynamicJacksonResponseDefinitionTransformer}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DynamicJacksonRequestLine {

	@SuppressWarnings("PMD.ShortVariable")
	private String id;

	private Integer optionA;
	private Boolean isValid;

	public String getId() {
		return id;
	}

	public void setId(final String newId) {
		this.id = newId;
	}

	public Integer getOptionA() {
		return optionA;
	}

	public void setOptionA(final Integer optionA) {
		this.optionA = optionA;
	}

	public Boolean getValid() {
		return isValid;
	}

	public void setValid(final Boolean valid) {
		isValid = valid;
	}

}
