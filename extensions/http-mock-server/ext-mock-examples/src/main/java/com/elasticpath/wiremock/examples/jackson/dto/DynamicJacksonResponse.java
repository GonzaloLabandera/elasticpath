/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.wiremock.examples.jackson.dto;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * An example data transfer object that shows how to build a Jackson mapping artifact
 * which allows Jackson to convert a POJO into a JSON object.
 *
 * This particular DTO is used to represent the top level object to construct for an
 * outgoing JSON response.
 *
 * This POJO is serialized from POJO -> JSON in {@link com.elasticpath.wiremock.examples.jackson.DynamicJacksonResponseDefinitionTransformer}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DynamicJacksonResponse {

	private String status;

	@JsonFormat()
	private Date createDate;
	private List<DynamicJacksonResponseLine> responseLines;

	public String getStatus() {
		return status;
	}

	public void setStatus(final String status) {
		this.status = status;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(final Date createDate) {
		this.createDate = createDate;
	}

	public List<DynamicJacksonResponseLine> getResponseLines() {
		return responseLines;
	}

	public void setResponseLines(final List<DynamicJacksonResponseLine> responseLines) {
		this.responseLines = responseLines;
	}
}
