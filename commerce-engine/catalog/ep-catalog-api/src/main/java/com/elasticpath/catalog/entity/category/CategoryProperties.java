/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.category;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.elasticpath.catalog.entity.ProjectionProperties;
import com.elasticpath.catalog.entity.Property;

/**
 * Represents category properties.
 */
public class CategoryProperties {
	private final ProjectionProperties projectionProperties;
	private final List<Property> categorySpecificProperties;

	/**
	 * Constructor.
	 *
	 * @param projectionProperties       projection properties.
	 * @param categorySpecificProperties list of specific properties for category entity.
	 */
	@JsonCreator
	public CategoryProperties(@JsonProperty("projectionProperties") final ProjectionProperties projectionProperties,
							  @JsonProperty("categorySpecificProperties") final List<Property> categorySpecificProperties) {
		this.projectionProperties = projectionProperties;
		this.categorySpecificProperties = categorySpecificProperties;
	}

	/**
	 * Get basic projection properties.
	 *
	 * @return projection properties.
	 */
	public ProjectionProperties getProjectionProperties() {
		return projectionProperties;
	}

	/**
	 * Get category properties.
	 *
	 * @return list of category properties.
	 */
	public List<Property> getCategorySpecificProperties() {
		return categorySpecificProperties;
	}
}
