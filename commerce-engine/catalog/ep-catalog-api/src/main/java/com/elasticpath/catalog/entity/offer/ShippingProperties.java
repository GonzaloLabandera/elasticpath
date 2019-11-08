/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity.offer;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import com.elasticpath.catalog.entity.view.ProjectionView;

/**
 * Represents a ProductSku shipping properties entity.
 */
@JsonView(ProjectionView.ContentOnly.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShippingProperties {
	private final BigDecimal weight;
	private final BigDecimal width;
	private final BigDecimal length;
	private final BigDecimal height;
	private final String unitsWeight;
	private final String unitsLength;

	/**
	 * Constructor.
	 *
	 * @param weight      is weight of ProductSku.
	 * @param width       is width of ProductSku.
	 * @param length      is length of ProductSku.
	 * @param height      is height of ProductSku.
	 * @param unitsWeight is unitsWeight of ProductSku.
	 * @param unitsLength is unitsLength of ProductSku.
	 */
	@JsonCreator
	public ShippingProperties(@JsonProperty("weight") final BigDecimal weight,
							  @JsonProperty("width") final BigDecimal width,
							  @JsonProperty("length") final BigDecimal length,
							  @JsonProperty("height") final BigDecimal height,
							  @JsonProperty("unitsWeight") final String unitsWeight,
							  @JsonProperty("unitsLength") final String unitsLength) {
		this.weight = weight;
		this.width = width;
		this.length = length;
		this.height = height;
		this.unitsWeight = unitsWeight;
		this.unitsLength = unitsLength;
	}

	/**
	 * Get weight of ProductSku.
	 *
	 * @return weight of ProductSku.
	 */
	public BigDecimal getWeight() {
		return weight;
	}

	/**
	 * Get width of ProductSku.
	 *
	 * @return width of ProductSku.
	 */
	public BigDecimal getWidth() {
		return width;
	}

	/**
	 * Get length of ProductSku.
	 *
	 * @return length of ProductSku.
	 */
	public BigDecimal getLength() {
		return length;
	}

	/**
	 * Get height of ProductSku.
	 *
	 * @return height of ProductSku.
	 */
	public BigDecimal getHeight() {
		return height;
	}

	/**
	 * Get unitsWeight of ProductSku.
	 *
	 * @return unitsWeight of ProductSku.
	 */
	public String getUnitsWeight() {
		return unitsWeight;
	}

	/**
	 * Get unitsLength of ProductSku.
	 *
	 * @return unitsLength of ProductSku.
	 */
	public String getUnitsLength() {
		return unitsLength;
	}
}
