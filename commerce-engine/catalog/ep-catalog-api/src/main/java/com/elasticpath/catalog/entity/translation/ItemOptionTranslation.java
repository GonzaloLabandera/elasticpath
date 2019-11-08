/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity.translation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;

import com.elasticpath.catalog.entity.view.ProjectionView;

/**
 * Represent option translations for {@link com.elasticpath.catalog.entity.offer.Item}.
 */
@JsonView(ProjectionView.ContentOnly.class)
@JsonPropertyOrder({"displayName", "displayValue", "name", "value"})
public class ItemOptionTranslation extends TranslationUnit {
	private final String displayValue;
	private final String value;

	/**
	 * Constructor.
	 *
	 * @param displayName translation displayName.
	 * @param name translation name.
	 * @param displayValue translation displayValue.
	 * @param value translation value.
	 */
	@JsonCreator
	public ItemOptionTranslation(@JsonProperty("displayName") final String displayName, @JsonProperty("name") final String name,
								 @JsonProperty("displayValue") final String displayValue, @JsonProperty("value") final String value) {
		super(displayName, name);
		this.displayValue = displayValue;
		this.value = value;
	}

	/**
	 * Get the displayValue.
	 *
	 * @return the displayValue.
	 */
	public String getDisplayValue() {
		return displayValue;
	}

	/**
	 * Get the value.
	 *
	 * @return the value.
	 */
	public String getValue() {
		return value;
	}
}
