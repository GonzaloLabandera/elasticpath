/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.plugin.payment.provider.dto;

import java.math.BigDecimal;

/**
 * OrderSkuDTO builder.
 */
public final class OrderSkuDTOBuilder {
	private String displayName;
	private int quantity;
	private BigDecimal price;
	private BigDecimal taxAmount;
	private BigDecimal total;
	private String skuCode;

	private OrderSkuDTOBuilder() {
	}

	/**
	 * An OrderSkuDTO builder.
	 *
	 * @return builder
	 */
	public static OrderSkuDTOBuilder builder() {
		return new OrderSkuDTOBuilder();
	}

	/**
	 * Configures builder to build with displayName.
	 *
	 * @param displayName the displayName
	 * @return the builder
	 */
	public OrderSkuDTOBuilder withDisplayName(final String displayName) {
		this.displayName = displayName;
		return this;
	}

	/**
	 * Configures builder to build with quantity.
	 *
	 * @param quantity the quantity
	 * @return the builder
	 */
	public OrderSkuDTOBuilder withQuantity(final int quantity) {
		this.quantity = quantity;
		return this;
	}

	/**
	 * Configures builder to build with unit price.
	 *
	 * @param price the price
	 * @return the builder
	 */
	public OrderSkuDTOBuilder withPrice(final BigDecimal price) {
		this.price = price;
		return this;
	}

	/**
	 * Configures builder to build with taxAmount.
	 *
	 * @param taxAmount the taxAmount
	 * @return the builder
	 */
	public OrderSkuDTOBuilder withTaxAmount(final BigDecimal taxAmount) {
		this.taxAmount = taxAmount;
		return this;
	}

	/**
	 * Configures builder to build with total.
	 *
	 * @param total the total
	 * @return the builder
	 */
	public OrderSkuDTOBuilder withTotal(final BigDecimal total) {
		this.total = total;
		return this;
	}

	/**
	 * Configures builder to build with sku code.
	 *
	 * @param skuCode the sku code
	 * @return the builder
	 */
	public OrderSkuDTOBuilder withSkuCode(final String skuCode) {
		this.skuCode = skuCode;
		return this;
	}

	/**
	 * Build OrderSkuDTO.
	 *
	 * @param prototype bean prototype
	 * @return populated object
	 */
	public OrderSkuDTO build(final OrderSkuDTO prototype) {
		if (quantity == 0) {
			throw new IllegalStateException("Builder is not fully initialized, quantity is missing");
		}
		if (skuCode == null) {
			throw new IllegalStateException("Builder is not fully initialized, skuCode is missing");
		}
		prototype.setDisplayName(displayName);
		prototype.setQuantity(quantity);
		prototype.setUnitPrice(price);
		prototype.setTaxAmount(taxAmount);
		prototype.setTotal(total);
		prototype.setSkuCode(skuCode);
		return prototype;
	}
}