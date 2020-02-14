/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.domain.payment;

import com.elasticpath.domain.order.OrderShipment;

/**
 * Utility container for information related to reauthorization process.
 */
public class ReAuthorizationItem {

	/**
	 * The order shipment for which reauthorization should be made.
	 */
	private OrderShipment shipment;

	/**
	 * @return the shipment
	 */
	public OrderShipment getShipment() {
		return shipment;
	}

	/**
	 * @param shipment the shipment to set
	 */
	public void setShipment(final OrderShipment shipment) {
		this.shipment = shipment;
	}

}
