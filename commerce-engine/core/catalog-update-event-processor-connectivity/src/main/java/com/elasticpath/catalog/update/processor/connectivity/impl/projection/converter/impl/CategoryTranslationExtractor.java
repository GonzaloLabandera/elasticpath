/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.impl;

import static java.util.stream.Collectors.toList;

import java.util.List;

import com.elasticpath.catalog.entity.attribute.Attribute;
import com.elasticpath.catalog.entity.translation.CategoryTranslation;
import com.elasticpath.catalog.entity.translation.Translation;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.store.Store;

/**
 * Represents a class which implements extracting translation logic for Category.
 */
public class CategoryTranslationExtractor {

	private final List<CategoryTranslation> categoryTranslations;

	/**
	 * Constructor.
	 *
	 * @param translations list of  Category translation.
	 * @param attributes   list of  Category details.
	 * @param source       source to extract translations.
	 * @param store        {@link Store}.
	 * @param catalog      {@link Catalog}.
	 */
	public CategoryTranslationExtractor(final List<Translation> translations,
										final List<Attribute> attributes,
										final Category source,
										final Store store,
										final Catalog catalog) {
		this.categoryTranslations = translations.stream()
				.map(translation -> new CategoryTranslation(translation,
						createDetailsExtractor(attributes, source, store, catalog, translation).getTranslations()))
				.collect(toList());
	}

	private DetailsExtractor createDetailsExtractor(final List<Attribute> attributes, final Category source, final Store store,
													final Catalog catalog,
													final Translation translation) {
		return new DetailsExtractor(locale -> source.getAttributeValueGroup().getFullAttributeValues(source.getCategoryType().getAttributeGroup(),
				locale),
				translation.getLanguage(),
				source.getAttributeValueGroup(),
				attributes,
				store,
				catalog);
	}

	/**
	 * Returns the category translations.
	 *
	 * @return list of {@link CategoryTranslation}
	 */
	public List<CategoryTranslation> getCategoryTranslations() {
		return categoryTranslations;
	}
}
