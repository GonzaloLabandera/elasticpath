/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity.translation;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import com.elasticpath.catalog.entity.TranslatedName;
import com.elasticpath.catalog.entity.view.ProjectionView;

/**
 * Represent translations for {@link com.elasticpath.catalog.entity.option.Option}.
 */
public class OptionTranslation extends Translation {

	private final List<TranslatedName> optionValues;

	/**
	 * Translation constructor.
	 *
	 * @param language     translation language.
	 * @param displayName  translation displayName.
	 * @param optionValues translation optionValues.
	 */
	@JsonCreator
	public OptionTranslation(@JsonProperty("language") final String language,
							 @JsonProperty("displayName") final String displayName,
							 @JsonProperty("optionValues") final List<TranslatedName> optionValues) {
		super(language, displayName);
		this.optionValues = optionValues;
	}

	/**
	 * Return the optionValues.
	 *
	 * @return the list of {@link TranslatedName}.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public List<TranslatedName> getOptionValues() {
		return optionValues;
	}

}
