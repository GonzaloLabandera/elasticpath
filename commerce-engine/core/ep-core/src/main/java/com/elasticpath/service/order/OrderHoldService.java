/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.order;

import java.util.List;
import java.util.Set;

import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderHold;

/**
 * Service for interacting with OrderHold entities.
 */
public interface OrderHoldService {

	/**
	 * Check if all of the holds associated with an order are resolved.
	 *
	 * @param orderUid the order that owns the holds to be queried
	 * @return true if all holds have a status of RESOLVED, false otherwise
	 */
	boolean isAllHoldsResolvedForOrder(long orderUid);

	/**
	 * Find all of the order holds that are associated with an order.
	 *
	 * @param orderUid the uid of the order to search with
	 * @return a List of order hold entities - will be an empty list if there are no holds
	 */
	List<OrderHold> findOrderHoldsByOrderUid(long orderUid);

	/**
	 * Inserts the order holds to the database.
	 *
	 * @param order the order to associate the order holds to
	 * @param orderHolds the set of order hold entities to add to the order
	 */
	void addHoldsToOrder(Order order, Set<OrderHold> orderHolds);

	/**
	 * Adds the orderHolds to the order, updates the order status to ONHOLD, creates an audit record and publishes the order held event.
	 *  @param order the order to hold
	 * @param orderHolds the order holds that should be applied to the order
	 * @return the updated order
	 */
	Order holdOrder(Order order, Set<OrderHold> orderHolds);

	/**
	 * Publishes an event to mark the order hold as unresolvable.
	 *
	 * @param order the order that owns the order hold
	 * @param orderHold the order hold that will be marked as unresolvable
	 * @param cmUserName the CM user who is marking the hold as unresolvable
	 * @param comment the comment that the provides the reason the hold is being marked as unresolvable
	 */
	void markHoldUnresolvable(Order order, OrderHold orderHold, String cmUserName, String comment);

	/**
	 * Publishes an event to mark the order hold as resolved.
	 *
	 * @param order the order that owns the order hold
	 * @param orderHold the order hold that will be marked as resolved
	 * @param cmUserName the CM user who is marking the hold as resolved
	 * @param comment the comment that the provides the reason the hold is being marked as resolved
	 */
	void markHoldResolved(Order order, OrderHold orderHold, String cmUserName, String comment);

	/**
	 * Get a particular order hold entity.
	 *
	 * @param orderHoldUid - the uid of the order hold to get
	 * @return the populated OrderHold entity
	 */
	OrderHold get(long orderHoldUid);

	/**
	 * Get a particular order hold entity by guid.
	 *
	 * @param orderHoldGuid - the guid of the order hold to get
	 * @return the populated OrderHold entity
	 */
	OrderHold getByGuid(String orderHoldGuid);

	/**
	 * Updates the given order hold.
	 *
	 * @param orderHold the order hold to update
	 * @return the persisted instance of order hold
	 * @throws com.elasticpath.base.exception.EpServiceException - in case of any errors
	 */
	OrderHold update(OrderHold orderHold);
}
