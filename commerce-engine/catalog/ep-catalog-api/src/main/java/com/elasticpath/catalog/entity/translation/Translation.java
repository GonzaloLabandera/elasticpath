/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity.translation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import com.elasticpath.catalog.entity.view.ProjectionView;

/**
 * Represents an entity for Translation.
 */
public class Translation {

	private final String language;
	private final String displayName;

	/**
	 * Translation constructor.
	 *
	 * @param language    translation language.
	 * @param displayName translation displayName.
	 */
	@JsonCreator
	public Translation(@JsonProperty("language") final String language, @JsonProperty("displayName") final String displayName) {
		this.language = language;
		this.displayName = displayName;
	}

	/**
	 * Return the language.
	 *
	 * @return the language.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public String getLanguage() {
		return language;
	}

	/**
	 * Return the displayName.
	 *
	 * @return the displayName.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public String getDisplayName() {
		return displayName;
	}

}
