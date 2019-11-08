/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import com.elasticpath.catalog.entity.view.ProjectionView;

/**
 * Class, that represent miscellaneous information from domain object that is not translated.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Property {

	private final String name;
	private final String value;

	/**
	 * Constructor with parameters.
	 *
	 * @param name name of the Property;
	 * @param value value of the Property;
	 */
	@JsonCreator
	public Property(@JsonProperty("name") final String name, @JsonProperty("value") final String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 *
	 * @return name of Property.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public String getName() {
		return name;
	}

	/**
	 *
	 * @return value of Property.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public String getValue() {
		return value;
	}
}
