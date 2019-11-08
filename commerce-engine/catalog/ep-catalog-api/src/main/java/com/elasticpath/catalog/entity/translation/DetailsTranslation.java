/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity.translation;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;

import com.elasticpath.catalog.entity.view.ProjectionView;

/**
 * Represent details translations for {@link com.elasticpath.catalog.entity.offer.Offer}.
 */
@JsonPropertyOrder({"displayName", "displayValues", "name", "values"})
public class DetailsTranslation extends TranslationUnit {
	private final List<String> displayValues;
	private final List<Object> values;

	/**
	 * Constructor.
	 *
	 * @param displayName   translation displayName.
	 * @param name          translation name.
	 * @param displayValues list of display value.
	 * @param values        list of  value.
	 */
	@JsonCreator
	public DetailsTranslation(@JsonProperty("displayName") final String displayName, @JsonProperty("name") final String name, @JsonProperty(
			"displayValues") final List<String> displayValues, @JsonProperty("values") final List<Object> values) {
		super(displayName, name);
		this.displayValues = displayValues;
		this.values = values;
	}

	/**
	 * Constructor.
	 *
	 * @param baseTranslation base translation.
	 * @param displayValues   list of display value.
	 * @param values          list of  value.
	 */
	public DetailsTranslation(final TranslationUnit baseTranslation, final List<String> displayValues, final List<Object> values) {
		this(baseTranslation.getDisplayName(), baseTranslation.getName(), displayValues, values);
	}

	/**
	 * Get the displayValues.
	 *
	 * @return the displayValues.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public List<String> getDisplayValues() {
		return displayValues;
	}

	/**
	 * Get the values.
	 *
	 * @return the values.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public List<Object> getValues() {
		return values;
	}
}
