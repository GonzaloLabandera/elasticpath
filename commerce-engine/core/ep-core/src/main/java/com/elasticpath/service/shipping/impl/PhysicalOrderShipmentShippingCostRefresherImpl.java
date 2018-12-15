/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shipping.impl;

import java.util.Optional;

import com.elasticpath.domain.order.PhysicalOrderShipment;
import com.elasticpath.money.Money;
import com.elasticpath.service.shipping.PhysicalOrderShipmentShippingCostCalculationService;
import com.elasticpath.service.shipping.PhysicalOrderShipmentShippingCostRefresher;

/**
 * Implements {@link PhysicalOrderShipment}.
 */
public class PhysicalOrderShipmentShippingCostRefresherImpl implements PhysicalOrderShipmentShippingCostRefresher {

	private PhysicalOrderShipmentShippingCostCalculationService physicalOrderShipmentShippingCostCalculationService;

	@Override
	public void refresh(final PhysicalOrderShipment physicalOrderShipment) {

		final Optional<Money> shippingCost = physicalOrderShipmentShippingCostCalculationService.calculateCost(physicalOrderShipment);

		if (shippingCost.isPresent()) {
			physicalOrderShipment.setShippingCost(shippingCost.get().getAmount());
		} else {
			physicalOrderShipment.setShippingCost(null);
		}

	}

	public void setPhysicalOrderShipmentShippingCostCalculationService(final PhysicalOrderShipmentShippingCostCalculationService
																			   physicalOrderShipmentShippingCostCalculationService) {
		this.physicalOrderShipmentShippingCostCalculationService = physicalOrderShipmentShippingCostCalculationService;
	}
}
