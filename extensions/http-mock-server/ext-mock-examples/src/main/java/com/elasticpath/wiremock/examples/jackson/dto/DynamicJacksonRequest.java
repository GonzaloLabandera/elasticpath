/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.wiremock.examples.jackson.dto;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * An example data transfer object that shows how to build a Jackson mapping artifact
 * which allows Jackson to convert a JSON object into a POJO.
 *
 * This particular DTO is used to represent the top level object to construct for an
 * incoming JSON request.
 *
 * This POJO is deserialized from JSON -> POJO in {@link com.elasticpath.wiremock.examples.jackson.DynamicJacksonResponseDefinitionTransformer}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DynamicJacksonRequest {

	private String status;
	private String code;
	private Date submitDate;
	private List<DynamicJacksonRequestLine> requestLines;

	public String getStatus() {
		return status;
	}

	public void setStatus(final String status) {
		this.status = status;
	}

	public String getCode() {
		return code;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	public Date getSubmitDate() {
		return submitDate;
	}

	public void setSubmitDate(final Date submitDate) {
		this.submitDate = submitDate;
	}

	public List<DynamicJacksonRequestLine> getRequestLines() {
		return requestLines;
	}

	public void setRequestLines(final List<DynamicJacksonRequestLine> requestLines) {
		this.requestLines = requestLines;
	}
}
