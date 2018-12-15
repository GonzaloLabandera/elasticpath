/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shipping;

import com.elasticpath.domain.order.PhysicalOrderShipment;

/**
 * Defining interface to populate shipping cost against {@link PhysicalOrderShipment}.
 */
public interface PhysicalOrderShipmentShippingCostRefresher {

	/**
	 * Calculate shipping cost against {@link PhysicalOrderShipment} and populate its shipping cost field.
	 *
	 * @param physicalOrderShipment physical order shipment.
	 */
	void refresh(PhysicalOrderShipment physicalOrderShipment);

}
