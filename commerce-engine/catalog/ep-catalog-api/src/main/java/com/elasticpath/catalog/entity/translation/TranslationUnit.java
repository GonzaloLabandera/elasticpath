/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity.translation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import com.elasticpath.catalog.entity.view.ProjectionView;

/**
 * Represents translation units for {@link com.elasticpath.catalog.entity.offer.Offer}.
 */
@JsonView(ProjectionView.ContentOnly.class)
public class TranslationUnit {

	private final String displayName;
	private final String name;

	/**
	 * Constructor.
	 *
	 * @param displayName translation displayName.
	 * @param name        translation name.
	 */
	@JsonCreator
	public TranslationUnit(@JsonProperty("displayName") final String displayName, @JsonProperty("name") final String name) {
		this.displayName = displayName;
		this.name = name;
	}

	/**
	 * Get the displayName.
	 *
	 * @return the displayName.
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Get the name.
	 *
	 * @return the name.
	 */
	public String getName() {
		return name;
	}
}
