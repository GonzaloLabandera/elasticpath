/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider.dto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the separate item living in cart context.
 */
public class OrderSkuDTO {
	private String displayName;
	private int quantity;
	private BigDecimal price;
	private BigDecimal taxAmount;
	private BigDecimal total;
	private String skuCode;
	private Map<String, String> fieldValues = new HashMap<>();

	/**
	 * Get the product's display name.
	 *
	 * @return the product's display name.
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Set the product's display name.
	 *
	 * @param displayName the product's display name
	 */
	public void setDisplayName(final String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Get the quantity of this sku.
	 *
	 * @return the quantity
	 */
	public int getQuantity() {
		return quantity;
	}

	/**
	 * Sets the quantity.
	 *
	 * @param quantity the quantity
	 */
	public void setQuantity(final int quantity) {
		this.quantity = quantity;
	}

	/**
	 * Get the unit price for this sku.
	 *
	 * @return the price
	 */
	public BigDecimal getUnitPrice() {
		return price;
	}

	/**
	 * Set the unit price for this sku.
	 *
	 * @param price the price
	 */
	public void setUnitPrice(final BigDecimal price) {
		this.price = price;
	}

	/**
	 * Gets the tax amount.
	 *
	 * @return the tax amount
	 */
	public BigDecimal getTaxAmount() {
		return taxAmount;
	}

	/**
	 * Sets the tax amount.
	 *
	 * @param taxAmount the tax amount
	 */
	public void setTaxAmount(final BigDecimal taxAmount) {
		this.taxAmount = taxAmount;
	}

	/**
	 * Gets the total price for this sku.
	 *
	 * @return The total amount of this sku
	 */
	public BigDecimal getTotal() {
		return total;
	}

	/**
	 * Sets the  total price for this sku.
	 *
	 * @param total The total amount of this sku
	 */
	public void setTotal(final BigDecimal total) {
		this.total = total;
	}

	/**
	 * Get map of fields.
	 *
	 * @return unmodifiable map of all key/value data field pairs
	 */
	public Map<String, String> getFields() {
		Map<String, String> fields = new HashMap<>(fieldValues.size());
		for (final Map.Entry<String, String> fieldEntry : fieldValues.entrySet()) {
			fields.put(fieldEntry.getKey(), fieldEntry.getValue());
		}
		return Collections.unmodifiableMap(fields);
	}

	/**
	 * Assigns a map to the fields.
	 *
	 * @param fieldValues The map of the fields to assign.
	 */
	public void setFields(final Map<String, String> fieldValues) {
		this.fieldValues = fieldValues;
	}

	/**
	 * Get the product SKU code.
	 *
	 * @return the productSku system name
	 */
	public String getSkuCode() {
		return skuCode;
	}

	/**
	 * Set the product SKU code.
	 *
	 * @param skuCode the productSku system name
	 */
	public void setSkuCode(final String skuCode) {
		this.skuCode = skuCode;
	}
}
