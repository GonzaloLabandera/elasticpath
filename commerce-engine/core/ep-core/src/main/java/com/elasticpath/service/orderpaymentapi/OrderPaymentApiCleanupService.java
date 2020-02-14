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
	 * Removes customer profile payment instruments.
	 *
	 * @param customerUidList list of all customer UIDs being cleaned up
	 */
	void removeByCustomerUidList(List<Long> customerUidList);

	/**
	 * Removes cart order payment instruments.
	 *
	 * @param cartGuid cart order GUID being cleaned up
	 */
	void removeByShoppingCartGuid(String cartGuid);

	/**
	 * Removes cart order payment instruments.
	 *
	 * @param shoppingCartGuids list of all shopping cart GUIDs being cleaned up
	 */
	void removeByShoppingCartGuids(List<String> shoppingCartGuids);

	/**
	 * Removes cart order payment instruments.
	 *
	 * @param shopperUidList list of all shopper UIDs being cleaned up
	 */
	void removeByShopperUidList(List<Long> shopperUidList);

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
