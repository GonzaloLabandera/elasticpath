/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */

package com.elasticpath.shipping.connectivity.dto.impl;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.elasticpath.shipping.connectivity.dto.ShippableItem;

/**
 * Implementation of {@link ShippableItem}.
 */
public class ShippableItemImpl implements ShippableItem, Comparable<ShippableItem>, Serializable {

	private static final long serialVersionUID = 5000000001L;

	private String skuGuid;

	private int quantity;

	private BigDecimal weight;

	private BigDecimal height;

	private BigDecimal width;

	private BigDecimal length;

	private Map<String, String> fields;

	public void setSkuGuid(final String skuGuid) {
		this.skuGuid = skuGuid;
	}

	public void setQuantity(final int quantity) {
		this.quantity = quantity;
	}

	public void setWeight(final BigDecimal weight) {
		this.weight = weight;
	}

	public void setHeight(final BigDecimal height) {
		this.height = height;
	}

	public void setWidth(final BigDecimal width) {
		this.width = width;
	}

	public void setLength(final BigDecimal length) {
		this.length = length;
	}

	/**
	 * Sets metadata field.
	 *
	 * @param fields the input metadata field
	 */
	public void setFields(final Map<String, String> fields) {
		Objects.requireNonNull(fields);

		getMutableFields().clear();
		getMutableFields().putAll(fields);
	}

	/**
	 * Sets metadata field with Key and Value.
	 *
	 * @param fieldKey key of the field
	 * @param fieldValue value of the field
	 */
	public void setField(final String fieldKey, final String fieldValue) {
		getMutableFields().put(fieldKey, fieldValue);
	}

	@Override
	public BigDecimal getHeight() {
		return height;
	}

	@Override
	public BigDecimal getWidth() {
		return width;
	}

	@Override
	public BigDecimal getLength() {
		return length;
	}

	@Override
	public String getSkuGuid() {
		return skuGuid;
	}

	@Override
	public BigDecimal getWeight() {
		return weight;
	}

	@Override
	public int getQuantity() {
		return quantity;
	}

	@Override
	public Map<String, String> getFields() {
		return Collections.unmodifiableMap(getMutableFields());
	}

	/**
	 * Gets the modifiable fields of Shippable Item.
	 *
	 * @return the modifiable fields.
	 */
	protected Map<String, String> getMutableFields() {
		if (fields == null) {
			fields = Maps.newHashMap();
		}

		return fields;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	@Override
	public int compareTo(final ShippableItem other) {

		if (other == null) {
			return -1;
		}

		if (this == other) {
			return 0;
		}

		return new CompareToBuilder()
				.append(getSkuGuid(), other.getSkuGuid())
				.append(getWeight(), other.getWeight())
				.append(getHeight(), other.getHeight())
				.append(getWidth(), other.getWidth())
				.append(getLength(), other.getLength())
				.append(getQuantity(), other.getQuantity())
				.append(getFields(), other.getFields())
				.toComparison();

	}

	@Override
	public boolean equals(final Object obj) {

		if (this == obj) {
			return true;
		}

		if (!(obj instanceof ShippableItem)) {
			return false;
		}

		final ShippableItem other = (ShippableItem) obj;

		return Objects.equals(getSkuGuid(), other.getSkuGuid())
				&& Objects.equals(getWeight(), other.getWeight())
				&& Objects.equals(getHeight(), other.getHeight())
				&& Objects.equals(getWidth(), other.getWidth())
				&& Objects.equals(getLength(), other.getLength())
				&& Objects.equals(getQuantity(), other.getQuantity())
				&& Objects.equals(getFields(), other.getFields());

	}

	@Override
	public int hashCode() {

		return Objects.hash(
				getSkuGuid(),
				getWeight(),
				getHeight(),
				getWidth(),
				getLength(),
				getQuantity(),
				getFields());
	}

}
