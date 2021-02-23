/**
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.email.handler.order.helper;

import java.util.List;

import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderHold;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.email.domain.EmailProperties;

/**
 * Helper for constructing email properties.
 */
public interface OrderEmailPropertyHelper {

	/**
	 * Gets the {@link com.elasticpath.email.domain.EmailProperties} instance with set properties.
	 *
	 * @param order the order
	 * @return {@link com.elasticpath.email.domain.EmailProperties}
	 */
	EmailProperties getOrderConfirmationEmailProperties(Order order);

	/**
	 * Gets the {@link EmailProperties} with set props.
	 *
	 * @param order the order
	 * @param orderShipment the order shipment
	 * @return {@link EmailProperties}
	 */
	EmailProperties getShipmentConfirmationEmailProperties(Order order, OrderShipment orderShipment);

	/**
	 * Constructs properties for failed shipment.
	 *
	 * @param shipment OrderShipment
	 * @param errorMessage the error message describing the cause of failure
	 * @return EmailProperties
	 */
	EmailProperties getFailedShipmentPaymentEmailProperties(OrderShipment shipment, String errorMessage);

	/**
	 * Constructs properties for order hold notification.
	 *
	 * @param storeCode the store the notification is for
	 * @param heldOrderCount the count of held orders that need action
	 * @return EmailProperties
	 */
	EmailProperties getHoldNotificationEmailProperties(String storeCode, String heldOrderCount);

	/**
	 * Gets the {@link com.elasticpath.email.domain.EmailProperties} instance with set properties for sending an order cancellation
	 * email due to an order hold is unresolvable.
	 *
	 * @param order the order
	 * @param orderHolds the holds associated wth the order
	 * @return {@link com.elasticpath.email.domain.EmailProperties}
	 */
	EmailProperties getOrderRejectedEmailProperties(Order order, List<OrderHold> orderHolds);
}
