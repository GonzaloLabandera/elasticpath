/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.impl;

import static com.elasticpath.catalog.entity.constants.CategoryPropertiesNames.PROPERTY_CATEGORY_TYPE;

import java.util.Collections;
import java.util.List;

import com.elasticpath.catalog.entity.Property;
import com.elasticpath.domain.catalog.Category;

/**
 * Represents a class which implements extracting property logic for {@link Category}.
 */
public class CategoryPropertyExtractor {
	private final Category category;
	private final List<Property> propertyList;

	/**
	 * Constructor.
	 *
	 * @param category {@link Category}.
	 */
	public CategoryPropertyExtractor(final Category category) {
		this.category = category;
		this.propertyList = extractCategoryProperties();
	}

	/**
	 * @return list of Properties.
	 */
	public List<Property> getPropertyList() {
		return propertyList;
	}

	private List<Property> extractCategoryProperties() {
		return Collections.singletonList(extractCategoryTypeName(category));
	}

	private Property extractCategoryTypeName(final Category category) {
		return new Property(PROPERTY_CATEGORY_TYPE, category.getCategoryType().getName());
	}
}
