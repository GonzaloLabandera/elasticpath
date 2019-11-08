/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity.translation;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import com.elasticpath.catalog.entity.view.ProjectionView;

/**
 * Represent translations for {@link com.elasticpath.catalog.entity.offer.Item}.
 */
public class ItemTranslation {
	private final String language;
	private final List<DetailsTranslation> details;
	private final List<ItemOptionTranslation> options;

	/**
	 * Constructor.
	 *
	 * @param language translation language.
	 * @param details  list of details.
	 * @param options  list of options.
	 */
	@JsonCreator
	public ItemTranslation(@JsonProperty("language") final String language, @JsonProperty("details") final List<DetailsTranslation> details,
						   @JsonProperty("options") final List<ItemOptionTranslation> options) {
		this.language = language;
		this.details = details;
		this.options = options;
	}

	/**
	 * Get the language.
	 *
	 * @return the language.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public String getLanguage() {
		return language;
	}

	/**
	 * Get the details.
	 *
	 * @return the details.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public List<DetailsTranslation> getDetails() {
		return details;
	}

	/**
	 * Get the options.
	 *
	 * @return the options.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public List<ItemOptionTranslation> getOptions() {
		return options;
	}
}
