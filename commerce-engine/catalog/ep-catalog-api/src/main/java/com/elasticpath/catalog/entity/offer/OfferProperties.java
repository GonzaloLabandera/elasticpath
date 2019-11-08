/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity.offer;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.elasticpath.catalog.entity.ProjectionProperties;
import com.elasticpath.catalog.entity.Property;

/**
 * Represents offer properties.
 */
public class OfferProperties {
	private final ProjectionProperties projectionProperties;
	private final List<Property> offerSpecificProperties;

	/**
	 * Constructor.
	 *
	 * @param projectionProperties    projection properties.
	 * @param offerSpecificProperties list of specific properties for offer entity.
	 */
	@JsonCreator
	public OfferProperties(@JsonProperty("projectionProperties") final ProjectionProperties projectionProperties,
						   @JsonProperty("offerSpecificProperties") final List<Property> offerSpecificProperties) {
		this.projectionProperties = projectionProperties;
		this.offerSpecificProperties = offerSpecificProperties;
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
	 * Get specific offer projection properties.
	 *
	 * @return list of offer properties.
	 */
	public List<Property> getOfferSpecificProperties() {
		return offerSpecificProperties;
	}
}
