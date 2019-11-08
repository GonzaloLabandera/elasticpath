/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.plugin.converter.impl;

import java.util.Collections;
import java.util.List;

import com.elasticpath.catalog.entity.AvailabilityRules;
import com.elasticpath.catalog.entity.Property;
import com.elasticpath.catalog.entity.offer.OfferAvailabilityRules;
import com.elasticpath.catalog.entity.translation.CategoryTranslation;

/**
 * Object to parse Category projection content.
 */
@SuppressWarnings("PMD.ExcessiveParameterList")
public class CategoryContent {
	private final List<String> children;
	private final List<CategoryTranslation> translations;
	private final Object extensions;
	private final List<Property> properties;
	private final AvailabilityRules availabilityRules;
	private final List<String> path;
	private final String parent;

	/**
	 * Constructor.
	 */
	public CategoryContent() {
		this.children = Collections.emptyList();
		this.translations = Collections.emptyList();
		this.extensions = null;
		this.path = Collections.emptyList();
		this.parent = null;
		this.properties = Collections.emptyList();
		this.availabilityRules = null;
	}

	/**
	 * Constructor.
	 *
	 * @param children          children list.
	 * @param extensions        is extensions of Category .
	 * @param translations      list of translations for projection entity.
	 * @param path              is list of path.
	 * @param parent            is parent of Category.
	 * @param properties        is list of {@link Property}.
	 * @param availabilityRules availability rules for Offer.
	 */
	public CategoryContent(final List<String> children, final List<CategoryTranslation> translations, final Object extensions,
						   final List<String> path, final String parent, final List<Property> properties,
						   final OfferAvailabilityRules availabilityRules) {
		this.children = children;
		this.translations = translations;
		this.extensions = extensions;
		this.path = path;
		this.parent = parent;
		this.properties = properties;
		this.availabilityRules = availabilityRules;
	}

	public List<String> getChildren() {
		return children;
	}

	public List<CategoryTranslation> getTranslations() {
		return translations;
	}

	public Object getExtensions() {
		return extensions;
	}

	public List<String> getPath() {
		return path;
	}

	public String getParent() {
		return parent;
	}

	public List<Property> getProperties() {
		return properties;
	}

	public AvailabilityRules getAvailabilityRules() {
		return availabilityRules;
	}
}
