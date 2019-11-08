/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity.offer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an offer rules.
 */
public class OfferRules {
	private final SelectionRules selectionRules;
	private final OfferAvailabilityRules availabilityRules;

	/**
	 * Constructor.
	 *
	 * @param availabilityRules availability rules.
	 * @param selectionRules    offer selectionRules.
	 */
	@JsonCreator
	public OfferRules(@JsonProperty("availabilityRules") final OfferAvailabilityRules availabilityRules,
					  @JsonProperty("selectionRules") final SelectionRules selectionRules) {
		this.selectionRules = selectionRules;
		this.availabilityRules = availabilityRules;
	}

	/**
	 * Get availability rules.
	 *
	 * @return availability rules.
	 */
	public OfferAvailabilityRules getAvailabilityRules() {
		return availabilityRules;
	}

	/**
	 * Get offer selectionRules.
	 *
	 * @return offer selectionRules.
	 */
	public SelectionRules getSelectionRules() {
		return selectionRules;
	}
}
