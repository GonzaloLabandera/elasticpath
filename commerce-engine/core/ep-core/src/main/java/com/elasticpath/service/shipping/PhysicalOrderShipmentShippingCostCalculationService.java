/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shipping;

import java.util.Optional;

import com.elasticpath.domain.order.PhysicalOrderShipment;
import com.elasticpath.money.Money;

/**
 * Defines interface to calculate shipping cost against {@link PhysicalOrderShipment}.
 */
public interface PhysicalOrderShipmentShippingCostCalculationService {

	/**
	 * Calculate shipping cost against {@link PhysicalOrderShipment}.
	 * <p>
	 * Usually shipping cost has been calculated during creation of {@link PhysicalOrderShipment}.
	 * e.g. checkout process, which is not necessary to recalculate shipping cost.
	 * <p>
	 * But sometimes it will be changed. e.g. cm user may need to manually modify the content of physical shipment before release the shipment. e.g.
	 * * adding/removing order sku
	 * * splitting/merging shipment
	 * * changing quantity of order sku
	 * * changing the shipment method
	 * * changing the order address
	 * * changing discount of sub total of shipment
	 * * changing discount of order sku
	 * * changing price of order sku
	 * * etc.
	 * <p>
	 * Since shipping cost could be overridden manually, e.g. by cm user. So the shipping cost calculation of {@link PhysicalOrderShipment}
	 * should be triggered only the content of shipment be changed. not always do recalculation.
	 *
	 * @param physicalOrderShipment the physical order shipment.
	 * @return the optional shipping cost.
	 */
	Optional<Money> calculateCost(PhysicalOrderShipment physicalOrderShipment);

}
