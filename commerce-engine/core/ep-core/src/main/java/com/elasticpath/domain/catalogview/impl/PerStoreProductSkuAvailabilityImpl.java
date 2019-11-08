/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.catalogview.impl;

import java.util.Objects;

import com.elasticpath.common.dto.SkuInventoryDetails;
import com.elasticpath.domain.catalog.Availability;
import com.elasticpath.domain.catalogview.PerStoreProductSkuAvailability;
import com.elasticpath.service.catalogview.impl.InventoryMessage;

/**
 * Implementation of {@link PerStoreProductSkuAvailability}.
 */
public class PerStoreProductSkuAvailabilityImpl implements PerStoreProductSkuAvailability {

	private boolean productSkuAvailable;
	private boolean productSkuDisplayable;
	private Availability skuAvailability;
	private SkuInventoryDetails inventoryDetails;
	private InventoryMessage messageCode;
	private boolean syndicate;

	@Override
	public boolean isProductSkuAvailable() {
		return productSkuAvailable;
	}

	public void setProductSkuAvailable(final boolean productSkuAvailable) {
		this.productSkuAvailable = productSkuAvailable;
	}

	@Override
	public boolean isProductSkuDisplayable() {
		return productSkuDisplayable;
	}

	public void setProductSkuDisplayable(final boolean productSkuDisplayable) {
		this.productSkuDisplayable = productSkuDisplayable;
	}

	@Override
	public Availability getSkuAvailability() {
		return skuAvailability;
	}

	public void setSkuAvailability(final Availability skuAvailability) {
		this.skuAvailability = skuAvailability;
	}

	@Override
	public SkuInventoryDetails getInventoryDetails() {
		return inventoryDetails;
	}

	public void setInventoryDetails(final SkuInventoryDetails inventoryDetails) {
		this.inventoryDetails = inventoryDetails;
	}

	@Override
	public boolean canSyndicate() {
		return syndicate;
	}

	@Override
	public InventoryMessage getMessageCode() {
		return messageCode;
	}

	public void setMessageCode(final InventoryMessage messageCode) {
		this.messageCode = messageCode;
	}

	public void setSkuSyndicate(final boolean syndicate) {
		this.syndicate = syndicate;
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof PerStoreProductSkuAvailability)) {
			return false;
		}

		final PerStoreProductSkuAvailability that = (PerStoreProductSkuAvailability) other;
		return isProductSkuAvailable() == that.isProductSkuAvailable()
				&& isProductSkuDisplayable() == that.isProductSkuDisplayable()
				&& Objects.equals(getSkuAvailability(), that.getSkuAvailability())
				&& Objects.equals(getInventoryDetails(), that.getInventoryDetails())
				&& getMessageCode() == that.getMessageCode();
	}

	@Override
	public int hashCode() {
		return Objects.hash(isProductSkuAvailable(), isProductSkuDisplayable(), getSkuAvailability(), getInventoryDetails(), getMessageCode());
	}
}
