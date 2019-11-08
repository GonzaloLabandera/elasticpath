/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity.offer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import com.elasticpath.catalog.entity.view.ProjectionView;

/**
 * Represents an offer selection rules.
 */
@JsonView(ProjectionView.ContentOnly.class)
public class SelectionRules {
	private final SelectionType selectionType;
	private final int quantity;

	/**
	 * Constructor.
	 *
	 * @param selectionType is selectionType of Offer.
	 * @param quantity      is quantity constituents of a bundle needs to be included when sold.
	 */
	@JsonCreator
	public SelectionRules(@JsonProperty("selectionType") final SelectionType selectionType, @JsonProperty("quantity") final int quantity) {
		this.selectionType = selectionType;
		this.quantity = quantity;
	}

	/**
	 * Get selectionType of SelectionRules.
	 *
	 * @return selectionType of SelectionRules.
	 */
	public SelectionType getSelectionType() {
		return selectionType;
	}

	/**
	 * Get quantity of SelectionRules.
	 *
	 * @return quantity of SelectionRules.
	 */
	public int getQuantity() {
		return quantity;
	}
}
