/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.converter.impl;

import java.util.List;

import com.elasticpath.catalog.entity.translation.Translation;

/**
 * Object to parse projection translations.
 *
 * @param <T> type of projection translation.
 */
public class ProjectionTranslations<T extends Translation> {

	private List<T> translations;

	/**
	 * Constructor.
	 */
	public ProjectionTranslations() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param translations is list of translations.
	 */
	public ProjectionTranslations(final List<T> translations) {
		this.translations = translations;
	}

	public List<T> getTranslations() {
		return translations;
	}
}