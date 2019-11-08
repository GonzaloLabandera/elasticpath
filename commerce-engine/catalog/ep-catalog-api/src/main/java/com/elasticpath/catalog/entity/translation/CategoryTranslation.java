/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.translation;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;

import com.elasticpath.catalog.entity.view.ProjectionView;

/**
 * Represent translations for {@link com.elasticpath.catalog.entity.category.Category}.
 */
@JsonView(ProjectionView.ContentOnly.class)
@JsonPropertyOrder({"language", "displayName", "details"})
public class CategoryTranslation extends Translation {
	private final List<DetailsTranslation> details;

	/**
	 * Constructor.
	 *
	 * @param translation standard translations fields
	 * @param details     translation for details.
	 */
	public CategoryTranslation(final Translation translation, final List<DetailsTranslation> details) {
		super(translation.getLanguage(), translation.getDisplayName());
		this.details = details;
	}

	/**
	 * Constructor.
	 *
	 * @param language    language of translation .
	 * @param displayName displayName of category.
	 * @param details     translation for details.
	 */
	@JsonCreator
	public CategoryTranslation(@JsonProperty("language") final String language, @JsonProperty("displayName") final String displayName,
							   @JsonProperty("details") final List<DetailsTranslation> details) {
		super(language, displayName);
		this.details = details;
	}

	/**
	 * Get the details translation.
	 *
	 * @return the details translation.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public List<DetailsTranslation> getDetails() {
		return details;
	}
}

