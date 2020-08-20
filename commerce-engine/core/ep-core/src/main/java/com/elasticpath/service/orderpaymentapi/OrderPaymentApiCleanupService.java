/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.service.orderpaymentapi;

import java.util.List;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.order.Order;

/**
 * Payment instrument cleanup service. All payment instrument cleanup work should be done here
 * as we would probably need to account for calling Payment API and invoking cleanup there as well.
 */
public interface OrderPaymentApiCleanupService {

	/**
	 * Removes customer profile payment instruments.
	 *
	 * @param customer customer being cleaned up
	 */
	void removeByCustomer(Customer customer);

	/**
	 * Removes order payment instruments.
	 *
	 * @param order order being cleaned up
	 */
	void removeByOrder(Order order);

	/**
	 * Removes order payment instruments.
	 *
	 * @param orderUidList list of all order UIDs being cleaned up
	 */
	void removeByOrderUidList(List<Long> orderUidList);

}
