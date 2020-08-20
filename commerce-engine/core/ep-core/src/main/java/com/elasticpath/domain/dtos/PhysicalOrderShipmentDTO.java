/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.domain.dtos;

import java.util.Date;

import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.domain.order.impl.OrderImpl;
import com.elasticpath.domain.order.impl.PhysicalOrderShipmentImpl;

/**
 * A lightweight class, representing a {@link com.elasticpath.domain.order.PhysicalOrderShipment}.
 * It is used as a projection in JPQL queries.
 */
public class PhysicalOrderShipmentDTO {
	private final OrderShipment shipment;

	/**
	 * Custom constructor.
	 *
	 * @param shipmentUidPk the shipment uidPk
	 * @param shipmentOrderUidPk the shipment order uidPk
	 * @param shipmentOrderNumber the shipment order number
	 * @param shipmentNumber the shipment number
	 * @param storeCode the store code
	 * @param shipmentCreatedDate the shipment created date
	 */
	public PhysicalOrderShipmentDTO(final long shipmentUidPk, final long shipmentOrderUidPk, final String shipmentOrderNumber,
									final String shipmentNumber, final String storeCode, final Date shipmentCreatedDate) {

		shipment = createShipment(shipmentUidPk, shipmentOrderUidPk, shipmentOrderNumber, shipmentNumber, storeCode, shipmentCreatedDate);
	}

	private OrderShipment createShipment(final long shipmentUidPk, final long shipmentOrderUidPk, final String shipmentOrderNumber,
										 final String shipmentNumber, final String storeCode, final Date shipmentCreatedDate) {

		final PhysicalOrderShipmentImpl internalShipment = new PhysicalOrderShipmentImpl();
		internalShipment.setUidPk(shipmentUidPk);
		internalShipment.setShipmentNumber(shipmentNumber);
		internalShipment.setCreatedDate(shipmentCreatedDate);

		final Order order = new OrderImpl();
		order.setStoreCode(storeCode);
		order.setUidPk(shipmentOrderUidPk);
		order.setOrderNumber(shipmentOrderNumber);
		internalShipment.disableRecalculation();
		internalShipment.setOrder(order);

		return internalShipment;
	}

	/**
	 * Return a semi-initialized {@link com.elasticpath.domain.order.PhysicalOrderShipment}.
	 *
	 * @return the physical order shipment
	 */
	public OrderShipment getShipment() {
		return shipment;
	}

	@Override
	public String toString() {
		return String.valueOf(shipment.getUidPk());
	}
}
