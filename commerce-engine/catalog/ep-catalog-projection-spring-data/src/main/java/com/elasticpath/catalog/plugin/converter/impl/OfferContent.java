/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.converter.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.elasticpath.catalog.entity.Property;
import com.elasticpath.catalog.entity.offer.Association;
import com.elasticpath.catalog.entity.offer.Components;
import com.elasticpath.catalog.entity.offer.Item;
import com.elasticpath.catalog.entity.offer.OfferAvailabilityRules;
import com.elasticpath.catalog.entity.offer.OfferCategories;
import com.elasticpath.catalog.entity.offer.SelectionRules;
import com.elasticpath.catalog.entity.translation.OfferTranslation;

/**
 * Object to parse Offer projection content.
 */
@SuppressWarnings("PMD.ExcessiveParameterList")
public class OfferContent {

	private final List<Item> items;
	private final Object extensions;
	private final List<Property> properties;
	private final OfferAvailabilityRules availabilityRules;
	private final List<Association> associations;
	private final SelectionRules selectionRules;
	private final Components components;
	private final List<String> formFields;
	private final List<OfferTranslation> translations;
	private final Set<OfferCategories> categories;

	/**
	 * Constructor.
	 */
	public OfferContent() {
		this.items = Collections.emptyList();
		this.extensions = null;
		this.properties = Collections.emptyList();
		this.availabilityRules = null;
		this.associations = Collections.emptyList();
		this.selectionRules = null;
		this.components = null;
		this.formFields = Collections.emptyList();
		this.translations = Collections.emptyList();
		this.categories = Collections.emptySet();
	}

	/**
	 * Constructor.
	 *
	 * @param items             is list of items.
	 * @param extensions        is extensions of Offer.
	 * @param properties        is list of {@link Property}.
	 * @param availabilityRules availability rules for Offer.
	 * @param associations      is list of offer associations.
	 * @param selectionRules    is selection rules.
	 * @param components        is components.
	 * @param formFields        list of form fields for projection entity.
	 * @param translations      list of translations for projection entity.
	 * @param categories        set of offer categories.
	 */
	@SuppressWarnings({"PMD.ExcessiveParameterList", "checkstyle:parameternumber"})
	public OfferContent(final List<Item> items, final Object extensions, final List<Property> properties,
						final OfferAvailabilityRules availabilityRules, final List<Association> associations,
						final SelectionRules selectionRules, final Components components, final List<String> formFields,
						final List<OfferTranslation> translations, final Set<OfferCategories> categories) {
		this.items = items;
		this.extensions = extensions;
		this.properties = properties;
		this.availabilityRules = availabilityRules;
		this.associations = associations;
		this.selectionRules = selectionRules;
		this.components = components;
		this.formFields = formFields;
		this.translations = translations;
		this.categories = categories;
	}

	public List<Item> getItems() {
		return items;
	}

	public Object getExtensions() {
		return extensions;
	}

	public List<Property> getProperties() {
		return properties;
	}

	public OfferAvailabilityRules getAvailabilityRules() {
		return availabilityRules;
	}

	public List<Association> getAssociations() {
		return associations;
	}

	public SelectionRules getSelectionRules() {
		return selectionRules;
	}

	public Components getComponents() {
		return components;
	}

	public List<String> getFormFields() {
		return formFields;
	}

	public List<OfferTranslation> getTranslations() {
		return translations;
	}

	public Set<OfferCategories> getCategories() {
		return categories;
	}
}
