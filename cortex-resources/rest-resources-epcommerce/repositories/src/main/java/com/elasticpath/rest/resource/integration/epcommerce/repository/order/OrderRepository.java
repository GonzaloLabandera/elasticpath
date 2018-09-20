/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.order;

import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.order.Order;
import com.elasticpath.rest.command.ExecutionResult;

/**
 * The facade for operations with orders.
 */
public interface OrderRepository {

	/**
	 * Find by order GUID.
	 *
	 * @param storeCode the store code
	 * @param orderGuid the order GUID
	 * @return ExecutionResult with the order
	 * @deprecated use findByGuidAsSingle
	 */
	@Deprecated
	ExecutionResult<Order> findByGuid(String storeCode, String orderGuid);

	/**
	 * Find by order GUID.
	 *
	 * @param storeCode the store code
	 * @param orderGuid the order GUID
	 * @return Single with the order
	 */
	Single<Order> findByGuidAsSingle(String storeCode, String orderGuid);

	/**
	 * Find all order Ids for Customer GUID.
	 *
	 * @param storeCode the store code
	 * @param customerGuid the customer GUID
	 * @return ExecutionResult with the order Ids
	 */
	Observable<String> findOrderIdsByCustomerGuid(String storeCode, String customerGuid);

	/**
	 * Get cart order given order id.
	 *
	 * @param scope store code
	 * @param orderId order if
	 * @return cart order
	 */
	Single<CartOrder> getOrderByOrderId(String scope, String orderId);
}
