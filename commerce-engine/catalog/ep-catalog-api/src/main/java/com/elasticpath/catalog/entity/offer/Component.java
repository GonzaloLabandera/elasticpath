/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity.offer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import com.elasticpath.catalog.entity.view.ProjectionView;

/**
 * Represents a Component entity.
 */
@JsonView(ProjectionView.ContentOnly.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Component {
	private final String offer;
	private final String item;
	private final int quantity;

	/**
	 * Constructor.
	 *
	 * @param offer    is offer code.
	 * @param item     is item code.
	 * @param quantity is quantity of the constituent item.
	 */
	@JsonCreator
	public Component(@JsonProperty("offer") final String offer, @JsonProperty("item") final String item,
					 @JsonProperty("quantity") final int quantity) {
		this.offer = offer;
		this.item = item;
		this.quantity = quantity;
	}

	/**
	 * Constructor.
	 *
	 * @param offer    is offer code.
	 * @param quantity is quantity of the constituent item.
	 */
	public Component(final String offer, final int quantity) {
		this(offer, null, quantity);
	}

	/**
	 * Get offer of Component.
	 *
	 * @return offer of Component.
	 */
	public String getOffer() {
		return offer;
	}

	/**
	 * Get item of Component.
	 *
	 * @return item of Component.
	 */
	public String getItem() {
		return item;
	}

	/**
	 * Get quantity of Component.
	 *
	 * @return quantity of Component.
	 */
	public int getQuantity() {
		return quantity;
	}
}
