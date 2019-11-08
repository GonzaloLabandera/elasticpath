/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity.offer;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;

import com.elasticpath.catalog.entity.AvailabilityRules;
import com.elasticpath.catalog.entity.Property;
import com.elasticpath.catalog.entity.translation.ItemTranslation;
import com.elasticpath.catalog.entity.view.ProjectionView;

/**
 * Represent a projection entity for ProductSku domain entity.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"itemCode", "properties", "availabilityRules", "shippingProperties", "translations"})
public class Item {

	private final String itemCode;
	private final Object extensions;
	private final List<Property> properties;
	private final AvailabilityRules availabilityRules;
	private final ShippingProperties shippingProperties;
	private final List<ItemTranslation> translations;

	/**
	 * Constructor.
	 *
	 * @param itemCode   skuCode of ProductSku.
	 * @param extensions extensions of Item.
	 * @param properties list of {@link Property} for ProductSku.
	 * @param availabilityRules availability rules of ProductSku.
	 * @param shippingProperties shipping properties of ProductSku.
	 * @param translations translations properties of ProductSku.
	 */
	@JsonCreator
	public Item(@JsonProperty("itemCode") final String itemCode, @JsonProperty("extensions") final Object extensions,
				@JsonProperty("properties") final List<Property> properties,
				@JsonProperty("availabilityRules") final AvailabilityRules availabilityRules,
				@JsonProperty("shippingProperties") final ShippingProperties shippingProperties,
				@JsonProperty("translations") final List<ItemTranslation> translations) {
		this.itemCode = itemCode;
		this.extensions = extensions;
		this.properties = properties;
		this.availabilityRules = availabilityRules;
		this.shippingProperties = shippingProperties;
		this.translations = translations;
	}

	/**
	 * Return itemCode.
	 *
	 * @return itemCode.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public String getItemCode() {
		return itemCode;
	}

	/**
	 * Get extensions.
	 *
	 * @return extensions.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public Object getExtensions() {
		return extensions;
	}

	/**
	 * Return properties.
	 *
	 * @return list of Item properties.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public List<Property> getProperties() {
		return properties;
	}

	/**
	 * Return availability rules of Item.
	 *
	 * @return availability rules of Item.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public AvailabilityRules getAvailabilityRules() {
		return availabilityRules;
	}

	/**
	 * Return shipping properties of Item.
	 *
	 * @return shipping properties of Item.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public ShippingProperties getShippingProperties() {
		return shippingProperties;
	}

	/**
	 * Return translations Item.
	 *
	 * @return translations Item.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public List<ItemTranslation> getTranslations() {
		return translations;
	}
}
