/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import com.elasticpath.catalog.entity.view.ProjectionView;

/**
 * Represents an entity of available rules.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AvailabilityRules {

	private final ZonedDateTime enableDateTime;
	private final ZonedDateTime disableDateTime;

	/**
	 * Constructor.
	 *
	 * @param enableDateTime  is enableDateTime of Entity.
	 * @param disableDateTime is disableDateTime of Entity.
	 */
	@JsonCreator
	public AvailabilityRules(@JsonProperty("enableDateTime") final ZonedDateTime enableDateTime,
							 @JsonProperty("disableDateTime") final ZonedDateTime disableDateTime) {
		this.enableDateTime = enableDateTime;
		this.disableDateTime = disableDateTime;
	}

	/**
	 * Get enableDateTime of Entity.
	 *
	 * @return enableDateTime of Entity.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSxxx")
	public ZonedDateTime getEnableDateTime() {
		return enableDateTime;
	}

	/**
	 * Get disableDateTime of Entity.
	 *
	 * @return disableDateTime of Entity.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSxxx")
	public ZonedDateTime getDisableDateTime() {
		return disableDateTime;
	}

}
