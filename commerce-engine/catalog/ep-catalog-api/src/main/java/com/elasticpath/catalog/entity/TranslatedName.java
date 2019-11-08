/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import com.elasticpath.catalog.entity.view.ProjectionView;

/**
 * Represents an entity for TranslatedName.
 */
public class TranslatedName {

	private String value;
	private String displayValue;

	/**
	 * TranslatedName constructor.
	 *
	 * @param value        translated value.
	 * @param displayValue translated displayValue.
	 */
	@JsonCreator
	public TranslatedName(@JsonProperty("value") final String value,
						  @JsonProperty("displayValue") final String displayValue) {
		this.value = value;
		this.displayValue = displayValue;
	}

	/**
	 * TranslatedName constructor.
	 */
	public TranslatedName() {
		super();
	}

	/**
	 * Return the value.
	 *
	 * @return the value.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public String getValue() {
		return value;
	}

	/**
	 * Return the displayValue.
	 *
	 * @return the displayValue.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public String getDisplayValue() {
		return displayValue;
	}

}
