/*
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.commons.util;

import java.util.Comparator;

import com.elasticpath.domain.order.OrderShipment;

/**
 * The comparator factory which uses to create order shipment comparators.
 */
public final class OrderShipmentComparatorFactory {
	
	private OrderShipmentComparatorFactory() {
	}

	/**
	 * Compare by shipment number in ascending order.
	 * 
	 * @return comparator
	 */
	public static Comparator<OrderShipment> getOrderShipmentNumberComparator() {
		return Comparator.comparing(OrderShipment::getShipmentNumber);
	}
}

