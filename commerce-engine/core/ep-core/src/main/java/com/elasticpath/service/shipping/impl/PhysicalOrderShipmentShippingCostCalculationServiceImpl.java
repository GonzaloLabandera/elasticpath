/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shipping.impl;

import java.util.Optional;

import com.elasticpath.domain.order.PhysicalOrderShipment;
import com.elasticpath.money.Money;
import com.elasticpath.service.shipping.PhysicalOrderShipmentShippingCostCalculationService;
import com.elasticpath.service.shipping.transformers.PricedShippableItemContainerFromOrderShipmentTransformer;
import com.elasticpath.shipping.connectivity.dto.PricedShippableItem;
import com.elasticpath.shipping.connectivity.dto.PricedShippableItemContainer;
import com.elasticpath.shipping.connectivity.dto.ShippingCalculationResult;
import com.elasticpath.shipping.connectivity.dto.ShippingOption;
import com.elasticpath.shipping.connectivity.service.ShippingCalculationService;

/**
 * Implements {@link PhysicalOrderShipmentShippingCostCalculationService}.
 */
public class PhysicalOrderShipmentShippingCostCalculationServiceImpl implements PhysicalOrderShipmentShippingCostCalculationService {

	private PricedShippableItemContainerFromOrderShipmentTransformer<PricedShippableItem> transformer;

	private ShippingCalculationService shippingCalculationService;

	@Override
	public Optional<Money> calculateCost(final PhysicalOrderShipment physicalOrderShipment) {

		final PricedShippableItemContainer<PricedShippableItem> pricedShippableItemContainer = transformer.apply(physicalOrderShipment);

		final ShippingCalculationResult shippingOptionResult = shippingCalculationService.getPricedShippingOptions(pricedShippableItemContainer);

		if (!shippingOptionResult.isSuccessful()) {
			return Optional.empty();
		}
		
		final Optional<ShippingOption> foundShippingOptionOptional = shippingOptionResult.getAvailableShippingOptions().stream()
				.filter(shippingOption -> shippingOption.getCode().equals(physicalOrderShipment.getShippingOptionCode()))
				.findFirst();

		if (!foundShippingOptionOptional.isPresent()) {
			return Optional.empty();
		}

		return foundShippingOptionOptional.get().getShippingCost();

	}

	public void setPricedShippableItemContainerTransformer(
			final PricedShippableItemContainerFromOrderShipmentTransformer<PricedShippableItem> transformer) {
		this.transformer = transformer;
	}

	public void setShippingCalculationService(final ShippingCalculationService shippingCalculationService) {
		this.shippingCalculationService = shippingCalculationService;
	}
}
