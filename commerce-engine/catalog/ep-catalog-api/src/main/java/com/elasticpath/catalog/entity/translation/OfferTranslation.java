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
 * Represent translations for {@link com.elasticpath.catalog.entity.offer.Offer}.
 */
@JsonView(ProjectionView.ContentOnly.class)
@JsonPropertyOrder({"language", "details", "options"})
public class OfferTranslation extends Translation {
	private final TranslationUnit brand;
	private final List<TranslationUnit> options;
	private final List<DetailsTranslation> details;

	/**
	 * Constructor.
	 *
	 * @param translation base translation data.
	 * @param brand       translation for brand.
	 * @param options     translation for options.
	 * @param details     translation for details.
	 */
	public OfferTranslation(final Translation translation, final TranslationUnit brand, final List<TranslationUnit> options,
							final List<DetailsTranslation> details) {
		super(translation.getLanguage(), translation.getDisplayName());
		this.brand = brand;
		this.options = options;
		this.details = details;
	}

	/**
	 * Constructor.
	 *
	 * @param language    language of translation .
	 * @param displayName displayName of offer.
	 * @param brand       translation for brand.
	 * @param options     translation for options.
	 * @param details     translation for details.
	 */
	@JsonCreator
	public OfferTranslation(@JsonProperty("language") final String language, @JsonProperty("displayName") final String displayName,
							@JsonProperty("brand") final TranslationUnit brand, @JsonProperty("options") final List<TranslationUnit> options,
							@JsonProperty("details") final List<DetailsTranslation> details) {
		super(language, displayName);
		this.brand = brand;
		this.options = options;
		this.details = details;
	}

	/**
	 * Get the brand translation.
	 *
	 * @return the brand translation.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public TranslationUnit getBrand() {
		return brand;
	}

	/**
	 * Get the options translation.
	 *
	 * @return the options translation.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public List<TranslationUnit> getOptions() {
		return options;
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
