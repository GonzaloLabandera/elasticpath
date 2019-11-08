/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity.offer;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import com.elasticpath.catalog.entity.view.ProjectionView;

/**
 * Represents entity for Offer associations.
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssociationValue {

	private final String offer;
	private final ZonedDateTime enableDateTime;
	private final ZonedDateTime disableDateTime;

	/**
	 * Constructor.
	 *
	 * @param offer           is offer code.
	 * @param enableDateTime  enable date for association value.
	 * @param disableDateTime disable date for association value.
	 */
	@JsonCreator
	public AssociationValue(@JsonProperty("offer") final String offer, @JsonProperty("enableDateTime") final ZonedDateTime enableDateTime,
							@JsonProperty("disableDateTime") final ZonedDateTime disableDateTime) {
		this.offer = offer;
		this.enableDateTime = enableDateTime;
		this.disableDateTime = disableDateTime;
	}

	/**
	 * Get offer.
	 *
	 * @return offer.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public String getOffer() {
		return offer;
	}

	/**
	 * Get enableDateTime.
	 *
	 * @return enableDateTime.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSxxx")
	public ZonedDateTime getEnableDateTime() {
		return enableDateTime;
	}

	/**
	 * Get disableDateTime.
	 *
	 * @return disableDateTime.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSxxx")
	public ZonedDateTime getDisableDateTime() {
		return disableDateTime;
	}

}
