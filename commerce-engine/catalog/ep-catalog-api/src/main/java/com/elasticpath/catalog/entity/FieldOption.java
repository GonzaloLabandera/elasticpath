/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import com.elasticpath.catalog.entity.view.ProjectionView;

/**
 * Data Transfer Object for ModifierFieldOption.
 */
public class FieldOption {
	private final String value;
	private final String displayValue;

	/**
	 * FieldOption constructor.
	 *
	 * @param value        is value of FieldOption.
	 * @param displayValue is translated value of FieldOption.
	 */
	@JsonCreator
	public FieldOption(@JsonProperty("value") final String value, @JsonProperty("displayValue") final String displayValue) {
		this.value = value;
		this.displayValue = displayValue;
	}

	/**
	 * Get the value.
	 *
	 * @return value.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public String getValue() {
		return value;
	}

	/**
	 * Get the displayValue.
	 *
	 * @return displayValue.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public String getDisplayValue() {
		return displayValue;
	}

}